-- =============================================================================
-- SkillStream Supabase Backend Schema, Analytics Columns, RLS & Single-Admin RPC
-- Project ID: uihcxccaglixqwlwmbzw
-- Run this script in: Supabase Dashboard -> SQL Editor -> New Query -> Run
-- =============================================================================

-- 1. Active App Users Table (Tracks everyone using the app + watchtime/clicks/traffic)
CREATE TABLE IF NOT EXISTS public.app_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_key TEXT UNIQUE NOT NULL,
  auth_user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
  email TEXT,
  display_name TEXT,
  auth_provider TEXT NOT NULL DEFAULT 'guest',
  app_platform TEXT NOT NULL DEFAULT 'android',
  device_model TEXT,
  xp_points INTEGER NOT NULL DEFAULT 0,
  courses_enrolled INTEGER NOT NULL DEFAULT 0,
  lessons_completed INTEGER NOT NULL DEFAULT 0,
  quizzes_passed INTEGER NOT NULL DEFAULT 0,
  watch_time_seconds BIGINT NOT NULL DEFAULT 0,
  link_clicks INTEGER NOT NULL DEFAULT 0,
  traffic_visits INTEGER NOT NULL DEFAULT 1,
  last_active_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Ensure new analytics columns exist if table was already created earlier
ALTER TABLE public.app_users
  ADD COLUMN IF NOT EXISTS watch_time_seconds BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS link_clicks INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS traffic_visits INTEGER NOT NULL DEFAULT 1;

-- 2. User Activity Logs Table (Tracks app opens, watchtime sessions, link clicks, sign-ins, quizzes)
CREATE TABLE IF NOT EXISTS public.user_activity_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_key TEXT NOT NULL,
  auth_user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
  email TEXT,
  event_type TEXT NOT NULL,
  event_detail TEXT,
  watch_seconds_delta INTEGER NOT NULL DEFAULT 0,
  platform TEXT NOT NULL DEFAULT 'android',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.user_activity_logs
  ADD COLUMN IF NOT EXISTS watch_seconds_delta INTEGER NOT NULL DEFAULT 0;

-- 3. Single-Admin Lock Table (Only ONE admin login accepted, claimed only once)
CREATE TABLE IF NOT EXISTS public.admin_config (
  id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
  admin_user_id UUID UNIQUE NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  admin_email TEXT UNIQUE NOT NULL,
  active_device_id TEXT NOT NULL,
  is_claimed BOOLEAN NOT NULL DEFAULT true,
  login_count INTEGER NOT NULL DEFAULT 1,
  claimed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_verified_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Indexes for fast analytics queries
CREATE INDEX IF NOT EXISTS idx_app_users_last_active ON public.app_users (last_active_at DESC);
CREATE INDEX IF NOT EXISTS idx_app_users_watch_time ON public.app_users (watch_time_seconds DESC);
CREATE INDEX IF NOT EXISTS idx_app_users_email ON public.app_users (email);
CREATE INDEX IF NOT EXISTS idx_activity_logs_created_at ON public.user_activity_logs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_logs_event_type ON public.user_activity_logs (event_type);

-- 4. Enable Row Level Security (RLS)
ALTER TABLE public.app_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_activity_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_config ENABLE ROW LEVEL SECURITY;

-- Keep admin_config completely private from direct REST queries (only accessible via SECURITY DEFINER RPC)
DROP POLICY IF EXISTS "No direct access to admin_config" ON public.admin_config;
CREATE POLICY "No direct access to admin_config"
  ON public.admin_config
  FOR ALL
  TO authenticated, anon
  USING (false)
  WITH CHECK (false);

-- 5. RLS Policies for app_users
DROP POLICY IF EXISTS "Users can view own app_users row" ON public.app_users;
CREATE POLICY "Users can view own app_users row"
  ON public.app_users
  FOR SELECT
  TO authenticated
  USING (auth.uid() = auth_user_id);

DROP POLICY IF EXISTS "Authenticated users can insert own app_users row" ON public.app_users;
CREATE POLICY "Authenticated users can insert own app_users row"
  ON public.app_users
  FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = auth_user_id AND user_key = auth.uid()::text);

DROP POLICY IF EXISTS "Authenticated users can update own app_users row" ON public.app_users;
CREATE POLICY "Authenticated users can update own app_users row"
  ON public.app_users
  FOR UPDATE
  TO authenticated
  USING (auth.uid() = auth_user_id)
  WITH CHECK (auth.uid() = auth_user_id AND user_key = auth.uid()::text);

DROP POLICY IF EXISTS "Guest sessions can read device tracking row for upsert" ON public.app_users;
CREATE POLICY "Guest sessions can read device tracking row for upsert"
  ON public.app_users
  FOR SELECT
  TO anon
  USING (
    auth_user_id IS NULL
    AND email IS NULL
    AND user_key LIKE 'device:%'
  );

DROP POLICY IF EXISTS "Guest sessions can insert device tracking row" ON public.app_users;
CREATE POLICY "Guest sessions can insert device tracking row"
  ON public.app_users
  FOR INSERT
  TO anon
  WITH CHECK (
    auth_user_id IS NULL
    AND email IS NULL
    AND user_key LIKE 'device:%'
  );

DROP POLICY IF EXISTS "Guest sessions can update device tracking row" ON public.app_users;
CREATE POLICY "Guest sessions can update device tracking row"
  ON public.app_users
  FOR UPDATE
  TO anon
  USING (auth_user_id IS NULL AND email IS NULL AND user_key LIKE 'device:%')
  WITH CHECK (auth_user_id IS NULL AND email IS NULL AND user_key LIKE 'device:%');

-- 6. RLS Policies for user_activity_logs
DROP POLICY IF EXISTS "Users can view own activity logs" ON public.user_activity_logs;
CREATE POLICY "Users can view own activity logs"
  ON public.user_activity_logs
  FOR SELECT
  TO authenticated
  USING (auth.uid() = auth_user_id);

DROP POLICY IF EXISTS "Authenticated users can insert own activity logs" ON public.user_activity_logs;
CREATE POLICY "Authenticated users can insert own activity logs"
  ON public.user_activity_logs
  FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = auth_user_id);

DROP POLICY IF EXISTS "Guest sessions can insert anonymous activity logs" ON public.user_activity_logs;
CREATE POLICY "Guest sessions can insert anonymous activity logs"
  ON public.user_activity_logs
  FOR INSERT
  TO anon
  WITH CHECK (
    auth_user_id IS NULL
    AND email IS NULL
    AND user_key LIKE 'device:%'
  );

-- 7. Auto-create app_users row when a user signs up via Supabase Auth
CREATE OR REPLACE FUNCTION public.handle_new_auth_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  INSERT INTO public.app_users (user_key, auth_user_id, email, display_name, auth_provider, last_active_at)
  VALUES (
    NEW.id::text,
    NEW.id,
    NEW.email,
    split_part(COALESCE(NEW.email, 'User'), '@', 1),
    COALESCE(NEW.raw_app_meta_data->>'provider', 'email'),
    now()
  )
  ON CONFLICT (user_key) DO UPDATE SET
    auth_user_id = EXCLUDED.auth_user_id,
    email = EXCLUDED.email,
    last_active_at = now();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_auth_user();

-- 8. Single-Admin One-Time Claim & Verification RPC
-- Accepts ONLY ONE admin login across the entire app (locked to first claimed admin_user_id + device_id)
CREATE OR REPLACE FUNCTION public.admin_claim_and_verify(p_device_id TEXT)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  v_uid UUID := auth.uid();
  v_email TEXT;
  v_cfg RECORD;
BEGIN
  IF v_uid IS NULL THEN
    RETURN jsonb_build_object(
      'allowed', false,
      'message', 'Authentication required before claiming admin access.'
    );
  END IF;

  IF p_device_id IS NULL OR length(trim(p_device_id)) < 4 THEN
    RETURN jsonb_build_object(
      'allowed', false,
      'message', 'Invalid device identifier.'
    );
  END IF;

  SELECT email INTO v_email FROM auth.users WHERE id = v_uid;

  SELECT * INTO v_cfg FROM public.admin_config WHERE id = 1;

  IF NOT FOUND THEN
    -- First-ever admin claim (one-time)
    INSERT INTO public.admin_config (id, admin_user_id, admin_email, active_device_id, is_claimed, login_count)
    VALUES (1, v_uid, COALESCE(v_email, 'admin'), p_device_id, true, 1);

    RETURN jsonb_build_object(
      'allowed', true,
      'status', 'claimed_once',
      'admin_email', COALESCE(v_email, 'admin'),
      'message', 'Admin access permanently claimed for this single account & device.'
    );
  END IF;

  -- Already claimed: only the exact same admin account AND exact same one-time device session may access
  IF v_cfg.admin_user_id = v_uid AND v_cfg.active_device_id = p_device_id THEN
    UPDATE public.admin_config SET last_verified_at = now() WHERE id = 1;
    RETURN jsonb_build_object(
      'allowed', true,
      'status', 'verified',
      'admin_email', v_cfg.admin_email,
      'message', 'Verified single-admin session.'
    );
  ELSE
    RETURN jsonb_build_object(
      'allowed', false,
      'status', 'locked_out',
      'message', 'Only one admin login is accepted and it has already been claimed once. Access denied.'
    );
  END IF;
END;
$$;

REVOKE ALL ON FUNCTION public.admin_claim_and_verify(TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.admin_claim_and_verify(TEXT) TO authenticated;

-- 9. Admin Analytics Dashboard RPC (Returns Watchtime by User, Total Visitors, Link Clicks, Total Traffic)
CREATE OR REPLACE FUNCTION public.get_admin_dashboard_metrics(p_device_id TEXT)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  v_uid UUID := auth.uid();
  v_is_admin BOOLEAN := false;
  v_total_visitors BIGINT := 0;
  v_authenticated_users BIGINT := 0;
  v_total_watch_seconds BIGINT := 0;
  v_total_link_clicks BIGINT := 0;
  v_total_traffic BIGINT := 0;
  v_users JSONB := '[]'::jsonb;
  v_events JSONB := '[]'::jsonb;
BEGIN
  IF v_uid IS NULL THEN
    RAISE EXCEPTION 'Unauthorized: Sign in required.';
  END IF;

  SELECT EXISTS (
    SELECT 1
    FROM public.admin_config
    WHERE id = 1
      AND admin_user_id = v_uid
      AND active_device_id = p_device_id
  ) INTO v_is_admin;

  IF NOT v_is_admin THEN
    RAISE EXCEPTION 'Unauthorized: Admin access is locked to the single claimed administrator.';
  END IF;

  SELECT
    COUNT(DISTINCT user_key),
    COUNT(*) FILTER (WHERE auth_user_id IS NOT NULL),
    COALESCE(SUM(watch_time_seconds), 0),
    COALESCE(SUM(link_clicks), 0),
    COALESCE(SUM(traffic_visits), 0)
  INTO
    v_total_visitors,
    v_authenticated_users,
    v_total_watch_seconds,
    v_total_link_clicks,
    v_total_traffic
  FROM public.app_users;

  SELECT COALESCE(jsonb_agg(row_data), '[]'::jsonb)
  INTO v_users
  FROM (
    SELECT
      user_key,
      email,
      display_name,
      auth_provider,
      app_platform,
      device_model,
      watch_time_seconds,
      link_clicks,
      traffic_visits,
      xp_points,
      courses_enrolled,
      lessons_completed,
      quizzes_passed,
      last_active_at
    FROM public.app_users
    ORDER BY watch_time_seconds DESC, last_active_at DESC
    LIMIT 100
  ) AS row_data;

  SELECT COALESCE(jsonb_agg(ev_data), '[]'::jsonb)
  INTO v_events
  FROM (
    SELECT
      id::text AS id,
      user_key,
      email,
      event_type,
      event_detail,
      watch_seconds_delta,
      platform,
      created_at
    FROM public.user_activity_logs
    ORDER BY created_at DESC
    LIMIT 30
  ) AS ev_data;

  RETURN jsonb_build_object(
    'total_visitors', v_total_visitors,
    'authenticated_users', v_authenticated_users,
    'total_watch_time_seconds', v_total_watch_seconds,
    'total_link_clicks', v_total_link_clicks,
    'total_traffic', v_total_traffic,
    'users', v_users,
    'recent_events', v_events
  );
END;
$$;

REVOKE ALL ON FUNCTION public.get_admin_dashboard_metrics(TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.get_admin_dashboard_metrics(TEXT) TO authenticated;
