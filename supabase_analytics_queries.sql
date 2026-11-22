-- =============================================================================
-- SkillStream Analytics Setup + SQL Queries (Run in Supabase SQL Editor)
-- Project ID: uihcxccaglixqwlwmbzw
-- Safe to run on an existing database: adds missing columns/RPCs if needed,
-- then outputs your Watchtime, Visitors, Link Clicks & Traffic reports.
-- =============================================================================

-- A. Ensure analytics columns & single-admin tables/RPCs exist
ALTER TABLE public.app_users
  ADD COLUMN IF NOT EXISTS watch_time_seconds BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS link_clicks INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS traffic_visits INTEGER NOT NULL DEFAULT 1;

ALTER TABLE public.user_activity_logs
  ADD COLUMN IF NOT EXISTS watch_seconds_delta INTEGER NOT NULL DEFAULT 0;

-- B. Overall Traffic Summary (Total Visitors, Total Traffic, Watchtime, Link Clicks)
SELECT
  COUNT(DISTINCT user_key) AS total_unique_visitors,
  COUNT(*) FILTER (WHERE auth_user_id IS NOT NULL) AS registered_users,
  COALESCE(SUM(traffic_visits), 0) AS total_traffic_visits,
  COALESCE(SUM(link_clicks), 0) AS total_link_clicks,
  COALESCE(SUM(watch_time_seconds), 0) AS total_watch_time_seconds,
  ROUND(COALESCE(SUM(watch_time_seconds), 0) / 60.0, 2) AS total_watch_time_minutes,
  ROUND(COALESCE(SUM(watch_time_seconds), 0) / 3600.0, 2) AS total_watch_time_hours,
  (SELECT COUNT(*) FROM public.user_activity_logs) AS total_logged_events
FROM public.app_users;

-- C. Watchtime, Link Clicks & Traffic by User
SELECT
  COALESCE(email, display_name, user_key) AS user_identifier,
  email,
  auth_provider,
  app_platform,
  device_model,
  watch_time_seconds,
  ROUND(watch_time_seconds / 60.0, 2) AS watch_time_minutes,
  link_clicks,
  traffic_visits,
  xp_points,
  lessons_completed,
  quizzes_passed,
  last_active_at,
  created_at
FROM public.app_users
ORDER BY watch_time_seconds DESC, last_active_at DESC;

-- D. Daily Traffic, Visitors & Watchtime Breakdown
SELECT
  DATE(created_at AT TIME ZONE 'UTC') AS activity_date,
  COUNT(*) AS total_traffic_events,
  COUNT(DISTINCT user_key) AS unique_visitors,
  COUNT(*) FILTER (WHERE event_type = 'link_click') AS link_clicks,
  COALESCE(SUM(watch_seconds_delta), 0) AS watch_time_seconds
FROM public.user_activity_logs
GROUP BY DATE(created_at AT TIME ZONE 'UTC')
ORDER BY activity_date DESC;

-- E. Link Clicks & Event Breakdown by Type
SELECT
  event_type,
  event_detail,
  COUNT(*) AS hit_count,
  COUNT(DISTINCT user_key) AS unique_users,
  MAX(created_at) AS last_triggered_at
FROM public.user_activity_logs
GROUP BY event_type, event_detail
ORDER BY hit_count DESC;
