# Customer CRM (Android)

A fully offline customer/call tracking app for business development work:
- Add customers (name, phone, company, notes)
- Log calls against each customer (date/time, outcome, notes)
- Set follow-up reminders with date + time
- Reminders fire as on-device notifications, even if the app is closed
- Reminders are also written to a **private local calendar** on your phone (visible in your Calendar app) so you can see them alongside your schedule

**100% offline** — everything is stored in a local SQLite database on your phone. No account, no internet permission, no cloud sync of any kind. The "calendar" it writes to is a local-only calendar (`ACCOUNT_TYPE_LOCAL`), so even if you're signed into Google on your phone, these events are never uploaded.

## How to build the APK (one-time setup, ~10-15 min)

1. **Install Android Studio** (free): https://developer.android.com/studio
2. Unzip this project folder somewhere on your computer.
3. Open Android Studio → **Open** → select the unzipped `CustomerCRM` folder.
4. Wait for Gradle to sync (first time downloads dependencies — needs internet just for this step).
5. Plug in your Android phone via USB (enable Developer Options → USB Debugging), or use an emulator.
6. Click the green **Run ▶** button — this installs it straight onto your phone.

### To get a standalone `.apk` file you can install without a cable:
- In Android Studio: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
- Once done, click "locate" in the popup — you'll find `app-debug.apk`
- Copy that file to your phone (email to yourself, USB transfer, etc.) and tap it to install
- You may need to allow "Install unknown apps" for whichever app you used to open the file

## First-launch permissions
On first open, the app will ask for:
- **Notifications** — required to show reminder alerts
- **Calendar** — required to add follow-up dates to your local calendar
- **Exact alarms** (Android 12+) — opens a system settings screen once; toggle it on so reminders fire at the exact time you pick

## Project structure
- `data/` — Room database (Customer, CallLog, FollowUp tables)
- `util/` — Reminder scheduling (AlarmManager), notification receiver, local calendar writer, boot receiver (reschedules alarms after phone restart)
- `ui/` — Jetpack Compose screens (customer list, customer detail with call log + reminders)

## Customizing
- App name / package: change in `app/build.gradle.kts` (`applicationId`) and `AndroidManifest.xml`
- Colors: `ui/theme/Color.kt`
- Add fields to Customer (e.g. email, address): edit `data/Customer.kt` and the `AddCustomerDialog` in `ui/CustomerListScreen.kt`
