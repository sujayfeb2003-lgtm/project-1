//
//  FirebaseAuthManager.swift
//  SkillStreamAuth (iOS / iPhone)
//
//  Complete Firebase Authentication Manager for iOS:
//  - Email ID & Password Sign-In, Account Registration, and Password Reset
//  - Passwordless Email Link Authentication & Universal Links
//

import Foundation
import FirebaseAuth
import Combine

/// State enum for Authentication on iOS
enum PasswordlessAuthState {
    case idle
    case signingIn
    case sendingLink
    case linkSent(email: String, message: String)
    case passwordResetSent(email: String, message: String)
    case verifying
    case authenticated(user: User, email: String, methodLabel: String)
    case error(message: String)
}

@MainActor
class FirebaseAuthManager: ObservableObject {
    
    static let shared = FirebaseAuthManager()
    
    @Published var authState: PasswordlessAuthState = .idle
    @Published var currentUser: User? = nil
    @Published var userEmail: String? = nil
    
    private let userDefaultsEmailKey = "SkillStream_EmailForSignIn"
    
    // Configuration constants matching Firebase Project & Web/Android configuration
    private let defaultContinueURL = "https://skillstream-auth.web.app/emailSignIn"
    private let iosBundleID = "com.aistudio.skillstream"
    private let androidPackageName = "com.aistudio.skillstream.kxmpzq"
    
    private var authListenerHandle: AuthStateDidChangeListenerHandle?
    
    init() {
        self.currentUser = Auth.auth().currentUser
        self.userEmail = Auth.auth().currentUser?.email
        if let user = self.currentUser, let email = user.email {
            self.authState = .authenticated(user: user, email: email, methodLabel: "Signed in with Firebase Auth")
        }
        
        // Listen to Auth State Changes
        self.authListenerHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                self?.currentUser = user
                self?.userEmail = user?.email
                if let user = user, let email = user.email {
                    if case .authenticated = self?.authState {
                        // Keep existing specific method label
                    } else {
                        self?.authState = .authenticated(user: user, email: email, methodLabel: "Signed in with Firebase Auth")
                    }
                } else if case .authenticated = self?.authState {
                    self?.authState = .idle
                }
            }
        }
    }
    
    deinit {
        if let handle = authListenerHandle {
            Auth.auth().removeStateDidChangeListener(handle)
        }
    }
    
    // MARK: - Email ID & Password Authentication
    
    /// Signs in an existing user with Email ID and Password
    func signIn(email: String, password: String) async {
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedEmail.isEmpty, trimmedEmail.contains("@") else {
            self.authState = .error(message: "Please enter a valid email address.")
            return
        }
        guard !password.isEmpty else {
            self.authState = .error(message: "Please enter your password.")
            return
        }
        
        self.authState = .signingIn
        
        do {
            let authResult = try await Auth.auth().signIn(withEmail: trimmedEmail, password: password)
            let user = authResult.user
            let resolvedEmail = user.email ?? trimmedEmail
            
            self.currentUser = user
            self.userEmail = resolvedEmail
            self.authState = .authenticated(
                user: user,
                email: resolvedEmail,
                methodLabel: "Signed in with Email & Password"
            )
        } catch {
            self.authState = .error(message: error.localizedDescription)
        }
    }
    
    /// Registers a new account with Email ID and Password
    func createAccount(email: String, password: String) async {
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedEmail.isEmpty, trimmedEmail.contains("@") else {
            self.authState = .error(message: "Please enter a valid email address.")
            return
        }
        guard password.count >= 6 else {
            self.authState = .error(message: "Password must be at least 6 characters long.")
            return
        }
        
        self.authState = .signingIn
        
        do {
            let authResult = try await Auth.auth().createUser(withEmail: trimmedEmail, password: password)
            let user = authResult.user
            let resolvedEmail = user.email ?? trimmedEmail
            
            self.currentUser = user
            self.userEmail = resolvedEmail
            self.authState = .authenticated(
                user: user,
                email: resolvedEmail,
                methodLabel: "Account created with Email & Password"
            )
        } catch {
            self.authState = .error(message: error.localizedDescription)
        }
    }
    
    /// Sends a password reset email
    func sendPasswordReset(toEmail email: String) async {
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedEmail.isEmpty, trimmedEmail.contains("@") else {
            self.authState = .error(message: "Please enter a valid email address to reset your password.")
            return
        }
        
        self.authState = .signingIn
        
        do {
            try await Auth.auth().sendPasswordReset(withEmail: trimmedEmail)
            self.authState = .passwordResetSent(
                email: trimmedEmail,
                message: "Password reset instructions sent to \(trimmedEmail). Check your inbox."
            )
        } catch {
            self.authState = .error(message: error.localizedDescription)
        }
    }
    
    // MARK: - Passwordless Email Link Authentication
    
    /// Constructs the ActionCodeSettings with universal links, bundle ID, and fallback package name
    func buildActionCodeSettings(
        url: String = "https://skillstream-auth.web.app/emailSignIn",
        bundleID: String = "com.aistudio.skillstream"
    ) -> ActionCodeSettings {
        let actionCodeSettings = ActionCodeSettings()
        actionCodeSettings.url = URL(string: url)
        actionCodeSettings.handleCodeInApp = true
        actionCodeSettings.setIOSBundleID(bundleID)
        
        actionCodeSettings.setAndroidPackageName(
            androidPackageName,
            installIfNotAvailable: true,
            minimumVersion: "1"
        )
        
        return actionCodeSettings
    }
    
    /// Step 1: Send Passwordless Magic Link to Email
    func sendSignInLink(toEmail email: String) async {
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedEmail.isEmpty, trimmedEmail.contains("@") else {
            self.authState = .error(message: "Please enter a valid email address.")
            return
        }
        
        self.authState = .sendingLink
        let settings = buildActionCodeSettings()
        
        do {
            try await Auth.auth().sendSignInLink(toEmail: trimmedEmail, actionCodeSettings: settings)
            UserDefaults.standard.set(trimmedEmail, forKey: userDefaultsEmailKey)
            
            let message = "Magic sign-in link sent to \(trimmedEmail). Please check your email on this iPhone."
            self.authState = .linkSent(email: trimmedEmail, message: message)
        } catch {
            self.authState = .error(message: error.localizedDescription)
        }
    }
    
    /// Checks if an incoming URL or string is a valid Firebase Sign-in with Email Link
    func isSignInWithEmailLink(_ linkString: String) -> Bool {
        return Auth.auth().isSignIn(withEmailLink: linkString)
    }
    
    /// Step 2: Complete Sign-In with Incoming Universal Link or Custom URL Scheme
    func handleIncomingURL(_ url: URL, fallbackEmail: String? = nil) async -> Bool {
        let linkString = url.absoluteString
        
        guard isSignInWithEmailLink(linkString) else {
            return false
        }
        
        self.authState = .verifying
        
        let email = fallbackEmail ?? UserDefaults.standard.string(forKey: userDefaultsEmailKey)
        
        guard let finalEmail = email, !finalEmail.isEmpty else {
            self.authState = .error(message: "Email address required to complete sign in from this link.")
            return false
        }
        
        do {
            let authResult = try await Auth.auth().signIn(withEmail: finalEmail, link: linkString)
            let user = authResult.user
            
            UserDefaults.standard.removeObject(forKey: userDefaultsEmailKey)
            
            let resolvedEmail = user.email ?? finalEmail
            self.currentUser = user
            self.userEmail = resolvedEmail
            self.authState = .authenticated(
                user: user,
                email: resolvedEmail,
                methodLabel: "Signed in with passwordless magic link"
            )
            return true
        } catch {
            self.authState = .error(message: "Sign-in failed: \(error.localizedDescription)")
            return false
        }
    }
    
    /// Sign out the current user
    func signOut() {
        do {
            try Auth.auth().signOut()
            UserDefaults.standard.removeObject(forKey: userDefaultsEmailKey)
            self.currentUser = nil
            self.userEmail = nil
            self.authState = .idle
        } catch {
            self.authState = .error(message: "Failed to sign out: \(error.localizedDescription)")
        }
    }
    
    /// Reset error or state back to idle
    func resetState() {
        if let user = self.currentUser, let email = user.email {
            self.authState = .authenticated(user: user, email: email, methodLabel: "Signed in with Firebase Auth")
        } else {
            self.authState = .idle
        }
    }
}
