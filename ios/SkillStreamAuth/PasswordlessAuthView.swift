//
//  PasswordlessAuthView.swift
//  SkillStreamAuth (iOS / iPhone)
//
//  SwiftUI Interface for Firebase Email & Password and Passwordless Magic Link Authentication
//

import SwiftUI
import FirebaseAuth

enum IOSAuthMode: String, CaseIterable {
    case password = "Email & Password"
    case magicLink = "Magic Link"
}

struct PasswordlessAuthView: View {
    @StateObject private var authManager = FirebaseAuthManager.shared
    
    @State private var selectedMode: IOSAuthMode = .password
    @State private var isSignUpMode: Bool = false
    
    @State private var emailInput: String = ""
    @State private var passwordInput: String = ""
    @State private var confirmPasswordInput: String = ""
    @State private var showPassword: Bool = false
    @State private var localValidationError: String? = nil
    
    @State private var manualLinkInput: String = ""
    @State private var showManualLinkEntry: Bool = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 22) {
                    // Header Logo & Title
                    VStack(spacing: 10) {
                        ZStack {
                            Circle()
                                .fill(Color.indigo.opacity(0.12))
                                .frame(width: 68, height: 68)
                            Image(systemName: selectedMode == .password ? "lock.shield.fill" : "key.fill")
                                .font(.system(size: 28))
                                .foregroundColor(.indigo)
                        }
                        
                        Text(headerTitle)
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                        
                        Text(headerSubtitle)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }
                    .padding(.top, 16)
                    
                    // Auth State Cards
                    switch authManager.authState {
                    case .authenticated(let user, let email, let methodLabel):
                        AuthenticatedProfileCard(user: user, email: email, methodLabel: methodLabel) {
                            authManager.signOut()
                        }
                        
                    case .linkSent(_, let message):
                        InfoBannerCard(title: "Magic Link Dispatched", message: message, icon: "envelope.badge.shield.half.filled")
                        
                    case .passwordResetSent(_, let message):
                        InfoBannerCard(title: "Password Reset Sent", message: message, icon: "lock.rotation")
                        
                    case .error(let message):
                        ErrorBanner(message: message)
                        
                    default:
                        EmptyView()
                    }
                    
                    if let localError = localValidationError {
                        ErrorBanner(message: localError)
                    }
                    
                    // Form controls (only if not authenticated)
                    if case .authenticated = authManager.authState {
                        // Already shown in profile card
                    } else {
                        VStack(spacing: 16) {
                            Picker("Sign-In Method", selection: $selectedMode) {
                                ForEach(IOSAuthMode.allCases, id: \.self) { mode in
                                    Text(mode.rawValue).tag(mode)
                                }
                            }
                            .pickerStyle(.segmented)
                            .onChange(of: selectedMode) { _ in
                                localValidationError = nil
                            }
                            
                            // Email Field
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Email ID")
                                    .font(.caption)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.secondary)
                                
                                HStack {
                                    Image(systemName: "envelope.fill")
                                        .foregroundColor(.secondary)
                                    TextField("name@example.com", text: $emailInput)
                                        .keyboardType(.emailAddress)
                                        .autocapitalization(.none)
                                        .disableAutocorrection(true)
                                }
                                .padding(12)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(10)
                            }
                            
                            if selectedMode == .password {
                                // Password Field
                                VStack(alignment: .leading, spacing: 8) {
                                    Text("Password")
                                        .font(.caption)
                                        .fontWeight(.semibold)
                                        .foregroundColor(.secondary)
                                    
                                    HStack {
                                        Image(systemName: "lock.fill")
                                            .foregroundColor(.secondary)
                                        if showPassword {
                                            TextField(isSignUpMode ? "Minimum 6 characters" : "Enter your password", text: $passwordInput)
                                                .autocapitalization(.none)
                                                .disableAutocorrection(true)
                                        } else {
                                            SecureField(isSignUpMode ? "Minimum 6 characters" : "Enter your password", text: $passwordInput)
                                        }
                                        Button(action: { showPassword.toggle() }) {
                                            Image(systemName: showPassword ? "eye.slash.fill" : "eye.fill")
                                                .foregroundColor(.secondary)
                                        }
                                    }
                                    .padding(12)
                                    .background(Color(.secondarySystemBackground))
                                    .cornerRadius(10)
                                }
                                
                                if isSignUpMode {
                                    VStack(alignment: .leading, spacing: 8) {
                                        Text("Confirm Password")
                                            .font(.caption)
                                            .fontWeight(.semibold)
                                            .foregroundColor(.secondary)
                                        
                                        HStack {
                                            Image(systemName: "lock.fill")
                                                .foregroundColor(.secondary)
                                            SecureField("Re-enter password", text: $confirmPasswordInput)
                                        }
                                        .padding(12)
                                        .background(Color(.secondarySystemBackground))
                                        .cornerRadius(10)
                                    }
                                }
                                
                                // Primary Email & Password Action Button
                                Button(action: handlePasswordAction) {
                                    HStack {
                                        if case .signingIn = authManager.authState {
                                            ProgressView()
                                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                                .padding(.trailing, 6)
                                            Text(isSignUpMode ? "Creating Account..." : "Signing In...")
                                        } else {
                                            Image(systemName: isSignUpMode ? "person.badge.plus" : "lock.open.fill")
                                            Text(isSignUpMode ? "Create Account" : "Sign In with Email")
                                                .fontWeight(.bold)
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(14)
                                    .background(isPasswordButtonDisabled ? Color.indigo.opacity(0.5) : Color.indigo)
                                    .foregroundColor(.white)
                                    .cornerRadius(12)
                                }
                                .disabled(isPasswordButtonDisabled)
                                
                                // Toggle Sign-Up / Forgot Password
                                HStack {
                                    Button(action: {
                                        withAnimation {
                                            isSignUpMode.toggle()
                                            localValidationError = nil
                                        }
                                    }) {
                                        Text(isSignUpMode ? "Already have an account?" : "Create new account")
                                            .font(.footnote)
                                            .fontWeight(.semibold)
                                            .foregroundColor(.indigo)
                                    }
                                    
                                    Spacer()
                                    
                                    if !isSignUpMode {
                                        Button(action: {
                                            localValidationError = nil
                                            Task {
                                                await authManager.sendPasswordReset(toEmail: emailInput)
                                            }
                                        }) {
                                            Text("Forgot password?")
                                                .font(.footnote)
                                                .foregroundColor(.indigo)
                                        }
                                        .disabled(isBusy)
                                    }
                                }
                            } else {
                                // Send Magic Link Button
                                Button(action: {
                                    Task {
                                        await authManager.sendSignInLink(toEmail: emailInput)
                                    }
                                }) {
                                    HStack {
                                        if case .sendingLink = authManager.authState {
                                            ProgressView()
                                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                                .padding(.trailing, 6)
                                            Text("Sending Magic Link...")
                                        } else {
                                            Image(systemName: "paperplane.fill")
                                            Text("Send Magic Link")
                                                .fontWeight(.bold)
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(14)
                                    .background(emailInput.isEmpty ? Color.indigo.opacity(0.5) : Color.indigo)
                                    .foregroundColor(.white)
                                    .cornerRadius(12)
                                }
                                .disabled(emailInput.isEmpty || isBusy)
                                
                                // Manual Link Entry toggle for testing
                                Button(action: {
                                    withAnimation {
                                        showManualLinkEntry.toggle()
                                    }
                                }) {
                                    HStack(spacing: 4) {
                                        Image(systemName: "link")
                                        Text(showManualLinkEntry ? "Hide manual link input" : "Have a magic link? Paste here")
                                    }
                                    .font(.footnote)
                                    .foregroundColor(.indigo)
                                }
                                
                                if showManualLinkEntry {
                                    VStack(spacing: 10) {
                                        TextField("Paste magic link URL from email", text: $manualLinkInput)
                                            .font(.caption)
                                            .padding(10)
                                            .background(Color(.secondarySystemBackground))
                                            .cornerRadius(8)
                                        
                                        Button(action: {
                                            if let url = URL(string: manualLinkInput) {
                                                Task {
                                                    _ = await authManager.handleIncomingURL(url, fallbackEmail: emailInput.isEmpty ? nil : emailInput)
                                                }
                                            }
                                        }) {
                                            HStack {
                                                if case .verifying = authManager.authState {
                                                    ProgressView()
                                                        .progressViewStyle(CircularProgressViewStyle(tint: .indigo))
                                                    Text("Verifying Link...")
                                                } else {
                                                    Image(systemName: "checkmark.shield.fill")
                                                    Text("Verify & Complete Sign In")
                                                        .fontWeight(.semibold)
                                                }
                                            }
                                            .font(.subheadline)
                                            .frame(maxWidth: .infinity)
                                            .padding(10)
                                            .background(Color.indigo.opacity(0.12))
                                            .foregroundColor(.indigo)
                                            .cornerRadius(8)
                                        }
                                        .disabled(manualLinkInput.isEmpty || isBusy)
                                    }
                                    .padding(12)
                                    .background(Color(.tertiarySystemBackground))
                                    .cornerRadius(10)
                                }
                            }
                        }
                        .padding(.horizontal, 4)
                    }
                    
                    Spacer(minLength: 40)
                }
                .padding(20)
            }
            .navigationTitle("Authentication")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
    
    private var headerTitle: String {
        if selectedMode == .password {
            return isSignUpMode ? "Create Eduverse Account" : "Eduverse Sign-In"
        }
        return "Eduverse Passwordless"
    }
    
    private var headerSubtitle: String {
        if selectedMode == .password {
            return "Sign in using your registered Email ID and password."
        }
        return "Sign in with a secure, passwordless magic email link."
    }
    
    private var isBusy: Bool {
        switch authManager.authState {
        case .signingIn, .sendingLink, .verifying:
            return true
        default:
            return false
        }
    }
    
    private var isPasswordButtonDisabled: Bool {
        emailInput.isEmpty || passwordInput.isEmpty || (isSignUpMode && confirmPasswordInput.isEmpty) || isBusy
    }
    
    private func handlePasswordAction() {
        localValidationError = nil
        if isSignUpMode {
            if passwordInput.count < 6 {
                localValidationError = "Password must be at least 6 characters."
                return
            }
            if passwordInput != confirmPasswordInput {
                localValidationError = "Passwords do not match."
                return
            }
            Task {
                await authManager.createAccount(email: emailInput, password: passwordInput)
            }
        } else {
            Task {
                await authManager.signIn(email: emailInput, password: passwordInput)
            }
        }
    }
}

// MARK: - Subviews

struct AuthenticatedProfileCard: View {
    let user: User
    let email: String
    let methodLabel: String
    let onSignOut: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 44))
                .foregroundColor(.green)
            
            VStack(spacing: 4) {
                Text("Authenticated")
                    .font(.headline)
                    .foregroundColor(.green)
                Text(email)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text(methodLabel)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text("UID: \(user.uid)")
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .fontDesign(.monospaced)
            }
            
            Button(action: onSignOut) {
                Text("Sign Out")
                    .fontWeight(.bold)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity)
                    .padding(12)
                    .background(Color.red.opacity(0.1))
                    .cornerRadius(10)
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity)
        .background(Color.green.opacity(0.08))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.green.opacity(0.2), lineWidth: 1)
        )
    }
}

struct InfoBannerCard: View {
    let title: String
    let message: String
    let icon: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.indigo)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundColor(.indigo)
                Text(message)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.indigo.opacity(0.08))
        .cornerRadius(12)
    }
}

struct ErrorBanner: View {
    let message: String
    
    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.red)
            Text(message)
                .font(.caption)
                .foregroundColor(.red)
                .multilineTextAlignment(.leading)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.red.opacity(0.1))
        .cornerRadius(10)
    }
}
