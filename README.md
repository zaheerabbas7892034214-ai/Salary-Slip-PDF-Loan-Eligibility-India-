# Salary Slip → Loan Eligibility (India)

A complete Android application built with Kotlin, Jetpack Compose, and Material 3 that helps users calculate their loan eligibility based on their salary slip PDFs.

## Features

### Core Functionality
- **PDF Parsing**: Extract salary information from PDF salary slips using PDFBox
- **OCR Fallback**: ML Kit text recognition for scanned PDFs
- **Loan Calculator**: Calculate loan eligibility based on EMI affordability (default 40%)
- **Interactive UI**: Adjust interest rates, tenure, and EMI affordability with live calculations
- **Risk Assessment**: Profile risk evaluation based on deductions and salary structure

### Monetization (Google Play Billing v6+)
- **Free Tier**: 
  - Parse PDFs
  - View eligibility calculations
  - Preview results
- **Pro Tier (₹299 one-time)**:
  - Export loan reports as PDF
  - Save unlimited profiles
  - Share reports
  - Restore purchases

### Technical Stack
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI**: Jetpack Compose with Material 3
- **Database**: Room Database for persistence
- **File Access**: Storage Access Framework (SAF)
- **Billing**: Google Play Billing Library 6.1.0
- **PDF**: Apache PDFBox Android 2.0.27.0
- **OCR**: Google ML Kit Text Recognition 16.0.0

## Project Structure

```
app/src/main/java/com/salaryslip/loaneligibility/
├── data/
│   ├── AppDatabase.kt         # Room database
│   ├── SalaryProfile.kt       # Profile entity
│   ├── Purchase.kt            # Purchase entity
│   ├── BillingManager.kt      # Billing integration
│   └── DAOs...
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── ParsingScreen.kt
│   │   ├── EligibilityScreen.kt
│   │   ├── PaywallScreen.kt
│   │   └── SettingsScreen.kt
│   ├── theme/
│   └── Navigation.kt
├── utils/
│   ├── PDFParser.kt           # PDF text extraction
│   ├── OCRFallback.kt         # ML Kit OCR
│   ├── LoanCalculator.kt      # Eligibility calculations
│   └── PDFReportGenerator.kt  # Report generation
├── viewmodel/
│   └── MainViewModel.kt
└── MainActivity.kt
```

## Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Android Studio**: Giraffe or later
- **Gradle**: 8.0+
- **Kotlin**: 1.9.10

## Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on emulator or device

```bash
./gradlew assembleDebug
```

## Screens

1. **Splash Screen**: Shows app branding and performs entitlement check
2. **Home Screen**: Lists saved profiles with "Select PDF" CTA and "Go Pro" button
3. **Parsing Screen**: Displays extracted data with confidence indicators and manual editing
4. **Eligibility Screen**: Shows calculated loan amount with interactive sliders for parameters
5. **Paywall**: Premium feature showcase with purchase flow
6. **Settings**: Restore purchases, clear data, privacy policy

## Loan Calculation Formula

```
EMI = [P × R × (1+R)^N] / [(1+R)^N - 1]

Where:
- P = Principal loan amount
- R = Monthly interest rate (annual rate / 12 / 100)
- N = Tenure in months

Eligible Amount = EMI × [(1+R)^N - 1] / [R × (1+R)^N]
Max EMI = Net Salary × EMI Affordability %
```

## Security & Privacy

- Purchase tokens stored securely in Room database
- No sensitive data transmitted to external servers
- PDF processing done locally on device
- Billing handled through Google Play's secure infrastructure

## License

Copyright © 2024. All rights reserved.
