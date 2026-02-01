# Deployment Guide

## Prerequisites

### Development Environment
- **Android Studio**: Giraffe (2022.3.1) or later
- **JDK**: Version 17 (included with Android Studio)
- **Android SDK**: 
  - SDK Platform 34 (Android 14)
  - SDK Platform 24 (Android 7.0) or higher
  - Build Tools 34.0.0

### Google Play Console Setup
1. Create app in Google Play Console
2. Set up In-App Products
3. Configure billing product

## Google Play Billing Setup

### Step 1: Create Product in Play Console
1. Go to Play Console → Your App → Monetize → Products → In-app products
2. Click "Create product"
3. Enter details:
   - **Product ID**: `loan_pro_unlock`
   - **Name**: Pro Features
   - **Description**: Unlock all premium features including PDF export, unlimited profile saves, and report sharing
   - **Price**: ₹299.00 INR
   - **Product Type**: One-time purchase

### Step 2: Activate Product
1. Save the product
2. Click "Activate" to make it available

### Step 3: Add Test Users (For Testing)
1. Go to Setup → License testing
2. Add test Gmail accounts
3. Select response type: "License Test Response" → "PURCHASED"

## Building the App

### Debug Build
```bash
# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# Install on connected device
./gradlew installDebug

# Run app
adb shell am start -n com.salaryslip.loaneligibility/.MainActivity
```

### Release Build

#### Step 1: Generate Signing Key
```bash
keytool -genkey -v -keystore loan-eligibility-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias loan-eligibility

# Save the keystore file securely
# Remember the passwords!
```

#### Step 2: Configure Signing
Create `app/keystore.properties`:
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=loan-eligibility
storeFile=../loan-eligibility-key.jks
```

Add to `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... rest of release config
        }
    }
}
```

#### Step 3: Build Release APK/AAB
```bash
# Build release APK
./gradlew assembleRelease

# Build release AAB (for Play Store)
./gradlew bundleRelease

# Outputs:
# APK: app/build/outputs/apk/release/app-release.apk
# AAB: app/build/outputs/bundle/release/app-release.aab
```

## Testing Billing

### Test Environment Setup
1. Use a test device or emulator with Google Play Store
2. Sign in with a test account (added in Play Console)
3. App must be uploaded to Play Console (Internal Testing track minimum)

### Testing Purchase Flow
```
1. Launch app
2. Navigate to Paywall screen
3. Tap "Buy Now - ₹299"
4. Should see "Test purchase" dialog (for test users)
5. Complete purchase
6. Verify Pro features unlock
```

### Testing Restore Purchases
```
1. Uninstall app
2. Reinstall app
3. Go to Settings → Restore Purchases
4. Previous purchase should be restored
5. Pro features should unlock again
```

### Cancel Test Purchase
```
# Via Play Store app
1. Open Play Store
2. Go to Account → Payments & subscriptions → Budget & history
3. Find test purchase
4. Cancel/Refund (test purchases are free)
```

## Play Store Submission

### Step 1: Prepare Store Listing
Required assets:
- App icon (512×512 PNG)
- Feature graphic (1024×500 PNG)
- Screenshots (min 2, recommended 8):
  - Phone: 1080×1920 or higher
  - Tablet: 1200×1920 or higher
- Short description (max 80 chars)
- Full description (max 4000 chars)
- Privacy policy URL

### Step 2: Upload AAB
```bash
# Build AAB
./gradlew bundleRelease

# Upload to Play Console
1. Go to Release → Production
2. Click "Create new release"
3. Upload AAB file
4. Add release notes
5. Review and rollout
```

### Step 3: Content Rating
1. Fill out questionnaire
2. Get rating for India and other regions

### Step 4: Pricing & Distribution
1. Select India and other countries
2. Set as "Free" app (in-app purchases: yes)
3. Review distribution agreement

## Privacy Policy

Required elements:
1. Data collection disclosure:
   - Salary information (stored locally)
   - Purchase history (stored locally)
   - No data sent to external servers

2. Third-party services:
   - Google Play Billing (purchase processing)
   - Google ML Kit (optional OCR, on-device)

3. Data retention:
   - Users can delete all data via Settings
   - Uninstalling removes all local data

Example policy (simplified):
```
Privacy Policy for Salary Slip → Loan Eligibility

Data Collection:
- We process salary slip PDFs locally on your device
- No salary data is transmitted to external servers
- Purchase history is stored locally for verification

Third-Party Services:
- Google Play Billing for payment processing
- Google ML Kit for text recognition (on-device)

Data Deletion:
- Clear all data via Settings → Clear Saved Data
- Uninstalling the app removes all data

Contact: [your-email@example.com]
```

## Post-Launch Monitoring

### Key Metrics to Track
1. **Crash rate**: Should be < 1%
2. **ANR rate**: Should be < 0.5%
3. **Conversion rate**: Free to Pro
4. **User retention**: Day 1, Day 7, Day 30
5. **Purchase refund rate**: Should be < 5%

### Play Console Vitals
1. Check Android vitals daily
2. Address any crash clusters
3. Monitor slow rendering
4. Check wake locks and battery usage

### Billing Issues
Common issues and solutions:
1. **"Item unavailable"**: Product not activated in Play Console
2. **"Already owned"**: Call queryPurchasesAsync first
3. **"Billing unavailable"**: Play Store not updated on device
4. **Test mode**: Upload APK/AAB to Internal Testing track

## Maintenance

### Regular Updates
- Update dependencies quarterly
- Test with latest Android version
- Address Play Store policy changes
- Respond to user reviews

### Monitoring Checklist
- [ ] Weekly: Check crash reports
- [ ] Weekly: Review user reviews
- [ ] Monthly: Check for dependency updates
- [ ] Monthly: Review billing transactions
- [ ] Quarterly: Update target SDK (Play Store requirement)

## Troubleshooting

### Common Build Issues

**Issue**: "Execution failed for task ':app:kspDebugKotlin'"
```bash
# Solution: Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

**Issue**: "SDK location not found"
```bash
# Solution: Create local.properties
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
```

### Common Runtime Issues

**Issue**: PDF parsing fails
- Check if file URI is valid
- Verify READ permission granted
- Test with different PDF formats

**Issue**: Billing not working
- Verify product ID matches Play Console
- Check test account configured
- Ensure app uploaded to Play Console

**Issue**: OCR not working
- Check Google Play Services installed
- Verify ML Kit dependency included
- Test with clear, high-quality scanned PDFs

## Support Resources

- Android Developer Documentation: https://developer.android.com
- Google Play Billing: https://developer.android.com/google/play/billing
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Material Design 3: https://m3.material.io

## Security Checklist

- [ ] ProGuard enabled for release builds
- [ ] Keystore file secured (not in version control)
- [ ] No hardcoded secrets in code
- [ ] HTTPS for all network calls (if any added)
- [ ] Purchase verification implemented
- [ ] Input validation on all user inputs

---

**Ready for Production**: Yes ✅

Follow this guide step-by-step to successfully deploy your app to Google Play Store.
