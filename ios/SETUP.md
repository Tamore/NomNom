# iOS Project Configuration

## Xcode Project Setup

### Package.swift (if using SPM)

```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "NomNom",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(name: "NomNom", targets: ["NomNom"]),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "NomNom",
            dependencies: [],
            resources: []
        ),
    ]
)
```

## Info.plist Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>NomNom</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>UIApplicationSceneManifest</key>
    <dict>
        <key>UIApplicationSupportsMultipleScenes</key>
        <true/>
    </dict>
    <key>UIApplicationSupportsIndirectInputEvents</key>
    <true/>
    <key>NSLocalNetworkUsageDescription</key>
    <string>This app needs access to your local network.</string>
    <key>NSBonjourServices</key>
    <array>
        <string>_nomnom._tcp</string>
    </array>
</dict>
</plist>
```

## Build Settings

- **Target iOS Version**: 16.0+
- **Swift Version**: 5.9
- **Deployment Target**: iOS 16.0

## Xcode Project Structure

```
NomNom.xcodeproj/
├── project.pbxproj
├── project.xcworkspace/
│   └── contents.xcworkspacedata
└── xcshareddata/
    └── xcschemes/
```

## Third-party Dependencies

### Needed Packages (via SPM)
- (None yet - using standard library)

### Future packages for Phase 2+
- Network library for Supabase API calls
- Image handling library (when implementing recipe images)

## Setup Instructions

1. **Create Xcode Project**
   ```bash
   mkdir -p ios/NomNom
   cd ios/NomNom
   ```

2. **Open in Xcode**
   - File > New > Project
   - Select "App" template
   - Choose Swift + SwiftUI
   - Save to this directory

3. **Configure Signing**
   - Select NomNomApp target
   - Go to Signing & Capabilities
   - Select your team

4. **Add Files**
   - Drag and drop the Swift files from the NomNom/ folder into Xcode

5. **Build and Run**
   - Select simulator or device
   - Cmd + R to build and run

## Running on Simulator

```bash
# List available simulators
xcrun simctl list devices

# Launch simulator
open -a Simulator

# Build and run
xcodebuild -scheme NomNom -configuration Debug -sdk iphonesimulator -derivedDataPath build
```

## Running on Device

1. Connect iPhone via USB
2. Select device from Xcode dropdown
3. Press Cmd + R
4. Trust the app on your iPhone (Settings > General > Device Management)

