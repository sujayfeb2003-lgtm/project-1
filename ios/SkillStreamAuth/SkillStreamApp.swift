//
//  SkillStreamApp.swift
//  SkillStreamAuth (iOS / iPhone)
//
//  Main App Entry point handling Firebase initialization, Universal Links, and Deep Links.
//

import SwiftUI
import FirebaseCore
import FirebaseAuth

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Initialize Firebase
        FirebaseApp.configure()
        return true
    }
}

@main
struct SkillStreamApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @StateObject private var authManager = FirebaseAuthManager.shared
    
    var body: some Scene {
        WindowGroup {
            PasswordlessAuthView()
                // Handle Universal Links and Custom URL Schemes when tapped from Email
                .onOpenURL { url in
                    Task {
                        let handled = await authManager.handleIncomingURL(url)
                        if handled {
                            print("Successfully authenticated from incoming URL: \(url)")
                        }
                    }
                }
                // Handle Universal Link User Activities
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { userActivity in
                    guard let webpageURL = userActivity.webpageURL else { return }
                    Task {
                        _ = await authManager.handleIncomingURL(webpageURL)
                    }
                }
        }
    }
}
