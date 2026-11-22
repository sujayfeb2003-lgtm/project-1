# Firebase Passwordless Authentication (Email Link / Magic Link)
## Complete Cross-Platform Guide (Android, Website & iPhone)

This repository contains the complete implementation for **Firebase Passwordless Email Link Authentication** across **Android**, **Web**, and **iOS (iPhone)**.

---

## 1. Firebase Console Configuration (Required for All Platforms)

### Step 1: Enable Email Link (Passwordless Sign-In)
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Select your project.
3. In the left navigation, navigate to **Build** > **Authentication** > **Sign-in method**.
4. Click on **Email/Password**.
5. Enable **Email/Password**.
6. Check the box for **Email link (passwordless sign-in)**.
7. Click **Save**.

### Step 2: Configure Authorized Domains
1. In **Authentication** > **Settings** tab > **Authorized domains**.
2. Add your website domain (e.g. `your-domain.com`, `skillstream-auth.web.app`, `localhost`).

---

## 2. Android Configuration & Implementation

### A. AndroidManifest.xml (Deep Links & App Links)
Deep link intent filters are configured on `MainActivity`:
```xml
<!-- Deep Link Intent Filter for Email Link Auth -->
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="https"
        android:host="skillstream-auth.web.app"
        android:pathPrefix="/emailSignIn" />
</intent-filter>

<!-- Custom scheme fallback -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="skillstream"
        android:host="emailSignIn" />
</intent-filter>
```

### B. Sending the Link (Kotlin)
```kotlin
val actionCodeSettings = ActionCodeSettings.newBuilder()
    .setUrl("https://skillstream-auth.web.app/emailSignIn")
    .setHandleCodeInApp(true) // Must be true
    .setAndroidPackageName(
        "com.aistudio.skillstream.kxmpzq",
        true, /* installIfNotAvailable */
        "1"   /* minimumVersion */
    )
    .setIOSBundleId("com.aistudio.skillstream")
    .build()

FirebaseAuth.getInstance().sendSignInLinkToEmail(email, actionCodeSettings)
// Cache email in SharedPreferences
```

### C. Completing Sign-In on Link Click (Kotlin)
```kotlin
if (auth.isSignInWithEmailLink(intentData)) {
    val email = getSavedEmailFromPrefs() ?: promptUserForEmail()
    auth.signInWithEmailLink(email, intentData).addOnSuccessListener { result ->
        // User is signed in
    }
}
```

---

## 3. Website Implementation (HTML5 / JavaScript SDK v10)

All web code is located in `/web`:
- `web/index.html`: Modern, responsive authentication card.
- `web/auth.js`: Firebase modular SDK v10 implementation.
- `web/firebase-config.js`: Configuration parameters.

### Web Flow:
1. **Send Link**:
   ```javascript
   import { getAuth, sendSignInLinkToEmail } from 'firebase/auth';

   await sendSignInLinkToEmail(auth, email, actionCodeSettings);
   window.localStorage.setItem('emailForSignIn', email);
   ```
2. **Verify & Sign In**:
   ```javascript
   import { getAuth, isSignInWithEmailLink, signInWithEmailLink } from 'firebase/auth';

   if (isSignInWithEmailLink(auth, window.location.href)) {
     let email = window.localStorage.getItem('emailForSignIn');
     if (!email) {
       email = window.prompt('Please provide your email for confirmation:');
     }
     const result = await signInWithEmailLink(auth, email, window.location.href);
     window.localStorage.removeItem('emailForSignIn');
   }
   ```

---

## 4. iPhone / iOS Implementation (Swift & SwiftUI)

All iOS source files are located in `/ios/SkillStreamAuth`:
- `FirebaseAuthManager.swift`: Observable manager for sending links, caching emails, handling Universal Links.
- `PasswordlessAuthView.swift`: SwiftUI user interface with real-time status.
- `SkillStreamApp.swift`: SwiftUI App entry point handling `.onOpenURL` and Universal Link continuation.
- `Info.plist`: URL types and custom scheme definitions.

### A. iOS Firebase Console Registration
1. In Firebase Console, go to **Project Settings** > **General** > **Your apps** > **Add iOS app**.
2. Set iOS bundle ID to `com.aistudio.skillstream`.
3. Add your **App Store ID** and **Team ID** so Firebase can generate the Apple App Site Association (`apple-app-site-association`) file for Universal Links.

### B. Xcode Associated Domains Entitlement
Add the Associated Domains capability in Xcode (`Signing & Capabilities` > `+ Capability` > `Associated Domains`):
```
applinks:skillstream-auth.web.app
applinks:skillstream.firebaseapp.com
```

### C. Swift Logic
```swift
// Send Link:
let settings = ActionCodeSettings()
settings.url = URL(string: "https://skillstream-auth.web.app/emailSignIn")
settings.handleCodeInApp = true
settings.setIOSBundleID("com.aistudio.skillstream")

try await Auth.auth().sendSignInLink(toEmail: email, actionCodeSettings: settings)
UserDefaults.standard.set(email, forKey: "EmailForSignIn")

// Receive Link:
if Auth.auth().isSignIn(withEmailLink: url.absoluteString) {
    let email = UserDefaults.standard.string(forKey: "EmailForSignIn") ?? promptUser()
    let result = try await Auth.auth().signIn(withEmail: email, link: url.absoluteString)
}
```
