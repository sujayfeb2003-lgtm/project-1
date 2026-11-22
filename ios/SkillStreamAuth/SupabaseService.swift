//
//  SupabaseService.swift
//  SkillStreamAuth (iOS / iPhone)
//
//  Connects iOS app to Supabase Backend (Project: uihcxccaglixqwlwmbzw)
//  Read SUPABASE_PUBLISHABLE_KEY from Info.plist / Xcode Build Configuration (never hardcode keys).
//

import Foundation

struct SupabaseAppUserPayload: Encodable {
    let user_key: String
    let auth_user_id: String?
    let email: String?
    let display_name: String?
    let auth_provider: String
    let app_platform: String
    let device_model: String?
    let last_active_at: String
}

final class SupabaseService {
    static let shared = SupabaseService()

    let projectId = "uihcxccaglixqwlwmbzw"
    let baseURL = "https://uihcxccaglixqwlwmbzw.supabase.co"

    private var publishableKey: String {
        (Bundle.main.object(forInfoDictionaryKey: "SUPABASE_PUBLISHABLE_KEY") as? String) ?? ""
    }

    private func getOrCreateDeviceKey() -> String {
        let defaultsKey = "SkillStream_SupabaseDeviceKey"
        if let existing = UserDefaults.standard.string(forKey: defaultsKey), !existing.isEmpty {
            return existing
        }
        let created = "device:\(UUID().uuidString)"
        UserDefaults.standard.set(created, forKey: defaultsKey)
        return created
    }

    func syncUserUsage(
        authUserId: String? = nil,
        accessToken: String? = nil,
        email: String? = nil,
        authProvider: String = "ios_guest"
    ) async throws {
        guard !publishableKey.isEmpty, publishableKey != "YOUR_SUPABASE_PUBLISHABLE_KEY" else {
            throw NSError(domain: "SupabaseService", code: 401, userInfo: [
                NSLocalizedDescriptionKey: "Set SUPABASE_PUBLISHABLE_KEY in Info.plist / build settings."
            ])
        }

        guard let url = URL(string: "\(baseURL)/rest/v1/app_users?on_conflict=user_key") else { return }

        let userKey = authUserId ?? getOrCreateDeviceKey()
        let formatter = ISO8601DateFormatter()
        let payload = SupabaseAppUserPayload(
            user_key: userKey,
            auth_user_id: authUserId,
            email: authUserId != nil ? email : nil,
            display_name: email?.components(separatedBy: "@").first ?? "iPhone User",
            auth_provider: authProvider,
            app_platform: "ios",
            device_model: "iPhone",
            last_active_at: formatter.string(from: Date())
        )

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(publishableKey, forHTTPHeaderField: "apikey")
        request.setValue("resolution=merge-duplicates,return=representation", forHTTPHeaderField: "Prefer")
        if let token = accessToken, !token.isEmpty {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        request.httpBody = try JSONEncoder().encode(payload)

        let (_, response) = try await URLSession.shared.data(for: request)
        if let httpResponse = response as? HTTPURLResponse, !(200...299).contains(httpResponse.statusCode) {
            throw NSError(domain: "SupabaseService", code: httpResponse.statusCode, userInfo: [
                NSLocalizedDescriptionKey: "Supabase sync failed with HTTP \(httpResponse.statusCode)."
            ])
        }
    }
}
