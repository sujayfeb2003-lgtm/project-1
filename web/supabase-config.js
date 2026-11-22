/**
 * Supabase Web Backend Configuration
 * Project ID: uihcxccaglixqwlwmbzw
 * Provide your SUPABASE_PUBLISHABLE_KEY at runtime or via your hosting environment config.
 */
export const SUPABASE_PROJECT_ID = 'uihcxccaglixqwlwmbzw';
export const SUPABASE_URL = 'https://uihcxccaglixqwlwmbzw.supabase.co';
export const SUPABASE_PUBLISHABLE_KEY =
  (typeof window !== 'undefined' && window.ENV && window.ENV.SUPABASE_PUBLISHABLE_KEY) ||
  'YOUR_SUPABASE_PUBLISHABLE_KEY';

/**
 * Syncs an active web user session to Supabase public.app_users and public.user_activity_logs.
 */
export async function syncWebUserToSupabase({
  accessToken = null,
  authUserId = null,
  email = null,
  authProvider = 'web',
  eventType = 'web_visit',
  eventDetail = null
}) {
  if (!SUPABASE_PUBLISHABLE_KEY || SUPABASE_PUBLISHABLE_KEY === 'YOUR_SUPABASE_PUBLISHABLE_KEY') {
    console.warn('Supabase publishable key not configured for Web.');
    return null;
  }

  let deviceKey = window.localStorage.getItem('skillstream_device_key');
  if (!deviceKey) {
    deviceKey = `device:${crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36)}`;
    window.localStorage.setItem('skillstream_device_key', deviceKey);
  }

  const userKey = authUserId || deviceKey;
  const headers = {
    'Content-Type': 'application/json',
    apikey: SUPABASE_PUBLISHABLE_KEY,
    Prefer: 'resolution=merge-duplicates,return=representation'
  };

  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`;
  }

  const payload = {
    user_key: userKey,
    auth_user_id: authUserId,
    email: authUserId ? email : null,
    display_name: email ? email.split('@')[0] : 'Web Guest',
    auth_provider: authProvider,
    app_platform: 'web',
    device_model: navigator.userAgent.slice(0, 80),
    last_active_at: new Date().toISOString()
  };

  const res = await fetch(`${SUPABASE_URL}/rest/v1/app_users?on_conflict=user_key`, {
    method: 'POST',
    headers,
    body: JSON.stringify(payload)
  });

  if (!res.ok) {
    throw new Error(`Supabase sync failed (${res.status})`);
  }

  await fetch(`${SUPABASE_URL}/rest/v1/user_activity_logs`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      apikey: SUPABASE_PUBLISHABLE_KEY,
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {})
    },
    body: JSON.stringify({
      user_key: userKey,
      auth_user_id: authUserId,
      email: authUserId ? email : null,
      event_type: eventType,
      event_detail: eventDetail,
      platform: 'web'
    })
  });

  return res.json();
}
