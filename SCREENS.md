# App Screens Overview

## User Flow

```
┌─────────────┐
│   SPLASH    │ → Entitlement Check (2 seconds)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    HOME     │ → Main screen with:
│             │   - "Select Salary PDF" button
│             │   - Saved profiles list
│             │   - "Go Pro - ₹299" banner (if free tier)
└──────┬──────┘
       │ (Select PDF via SAF)
       ▼
┌─────────────┐
│   PARSING   │ → Extracted data with:
│             │   - Text fields (editable)
│             │   - Confidence badges (HIGH/MEDIUM/LOW)
│             │   - "Calculate Eligibility" button
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ ELIGIBILITY │ → Results with:
│             │   - Loan amount (large display)
│             │   - Monthly EMI
│             │   - Profile risk badge
│             │   - Interactive sliders:
│             │     * Interest rate (5-20%)
│             │     * Tenure (12-240 months)
│             │     * EMI affordability (20-60%)
│             │   - EMI schedule table
│             │   - "Save Profile" button (Pro)
│             │   - "Export PDF" button (Pro)
└──────┬──────┘
       │ (Tap Pro feature)
       ▼
┌─────────────┐
│   PAYWALL   │ → Purchase screen:
│             │   - Feature list with checkmarks
│             │   - Price: ₹299 (one-time)
│             │   - "Buy Now" button
│             │   - "Restore Purchases" link
└─────────────┘
```

## Screen Features

### 1. Splash Screen
- App branding
- Premium status indicator
- Auto-navigates to Home after 2 seconds

### 2. Home Screen
**Free Users:**
- Prominent "Go Pro" card with benefits
- Empty state: "No saved profiles yet"

**Pro Users:**
- List of saved salary profiles
- Quick access to past calculations

**All Users:**
- FAB (Floating Action Button) to select PDF
- Settings icon in toolbar

### 3. Parsing Screen
**Interactive Data Editing:**
```
┌────────────────────────────┐
│ Employer                   │ [MEDIUM]
│ [Acme Corporation      ]   │
└────────────────────────────┘

┌────────────────────────────┐
│ Employee Name              │ [MEDIUM]
│ [John Doe              ]   │
└────────────────────────────┘

┌────────────────────────────┐
│ Net Pay (₹)                │ [HIGH]
│ [45000                 ]   │
└────────────────────────────┘
```

### 4. Eligibility Results Screen
**Top Card:**
```
┌─────────────────────────────────┐
│  Eligible Loan Amount           │
│  ₹9,45,000                      │
│                                  │
│  Monthly EMI: ₹18,000           │
│  Profile: GOOD                  │
└─────────────────────────────────┘
```

**Interactive Sliders:**
```
Interest Rate: 10%
├────────●────────────────────┤
5%                         20%

Tenure: 60 months
├────────────●────────────────┤
12                        240

EMI Affordability: 40%
├────────────●────────────────┤
20%                        60%
```

**EMI Schedule Table:**
```
Month 1:  Principal: ₹10,123  Interest: ₹7,877
Month 2:  Principal: ₹10,207  Interest: ₹7,793
Month 3:  Principal: ₹10,292  Interest: ₹7,708
...
```

### 5. Paywall Screen
**Feature Showcase:**
```
Go Pro

✓ Export PDF Reports
✓ Save Unlimited Profiles
✓ Share Reports
✓ Lifetime Access

┌─────────────────────────┐
│   One-Time Purchase     │
│        ₹299             │
└─────────────────────────┘

┌─────────────────────────┐
│    Buy Now - ₹299       │
└─────────────────────────┘

       Restore Purchases
```

### 6. Settings Screen
**Menu Items:**
```
⚙️ Settings

• Restore Purchases
  Restore your Pro purchase

• Clear Saved Data
  Delete all saved profiles

• Privacy Policy
  View our privacy policy

• Version
  1.0
```

## Color Scheme (Material 3)

**Light Theme:**
- Primary: Purple (#6650a4)
- Secondary: Purple Grey (#625b71)
- Success: Green (#4CAF50)
- Warning: Yellow (#FFC107)
- Error: Red (#F44336)

**Confidence Badges:**
- HIGH: Primary (Purple)
- MEDIUM: Secondary (Grey)
- LOW: Error (Red)

**Risk Status:**
- GOOD: Green background
- MODERATE: Yellow background
- RISKY: Red background

## Interactions

### PDF Selection
1. Tap FAB on Home screen
2. System file picker opens (SAF)
3. Select PDF file
4. Navigate to Parsing screen
5. Data auto-extracted and displayed

### Purchase Flow
1. Tap "Go Pro" or any Pro feature
2. Navigate to Paywall
3. Tap "Buy Now"
4. Google Play Billing sheet appears
5. Complete purchase
6. Auto-navigate back
7. Pro features unlocked

### Export Report
1. Calculate eligibility
2. Tap "Export PDF"
3. System save dialog (SAF)
4. Choose location
5. PDF generated and saved
6. Success message shown

## Accessibility

- Large touch targets (48dp minimum)
- Clear labels for screen readers
- High contrast text
- Descriptive button labels
- Confidence indicators with color + text

## Performance

- Lazy loading for profile lists
- Efficient recomposition in Compose
- Database operations on IO dispatcher
- Billing operations on Main dispatcher
- PDF processing on IO dispatcher

---

**Design Philosophy:**
- Clean, modern Material 3 design
- Clear visual hierarchy
- Minimal cognitive load
- Progressive disclosure of features
- Immediate feedback for actions
