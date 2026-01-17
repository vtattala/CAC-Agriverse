# Copilot Instructions for CAC-Agriverse

## Project Overview

AgriVerse is an AI-powered agricultural assistant Android application built with Java and TensorFlow Lite. The app provides farmers with real-time crop diagnostics, pest identification, plant growing information, and farm management tools.

### Core Features
- **Plant Disease Detection**: Uses TensorFlow Lite CNN models to identify 38 crop diseases
- **Insect Identification**: Recognizes 12 common agricultural insects
- **Plant Information Database**: Comprehensive growing guides for major crops
- **Digital Notepad**: Local note-taking with SharedPreferences storage
- **Space Weather Integration**: Weather and satellite data for agricultural planning
- **Regional Guides**: Location-specific agricultural information

## Technology Stack

- **Language**: Java 11
- **Framework**: Android SDK (minSdk 21, targetSdk 34, compileSdk 34)
- **AI/ML**: TensorFlow Lite 2.12.0 (with support library 0.4.4)
- **Backend**: Firebase Authentication, Google Sign-In
- **HTTP Client**: OkHttp 4.12.0
- **JSON Parsing**: Gson 2.10.1
- **Package**: com.example.plantdisease

## Project Structure

```
app/src/main/
├── java/com/example/plantdisease/    # Main source code
│   ├── MainActivity.java              # Plant disease detection
│   ├── InsectActivity.java            # Insect identification
│   ├── PlantInfoActivity.java         # Plant information search
│   ├── NotepadActivity.java           # Digital notepad
│   ├── HomeActivity.java              # Main navigation hub
│   ├── WelcomeActivity.java           # User onboarding
│   ├── MainContainerActivity.java     # Main container with navigation
│   ├── HomeFragment.java              # Home screen fragment
│   ├── ChatActivity.java              # AI chat interface
│   ├── EncyclopediaActivity.java      # Plant encyclopedia
│   ├── RegionalGuideActivity.java     # Regional farming guides
│   ├── SatelliteActivity.java         # Satellite imagery
│   ├── SpaceWeatherActivity.java      # Space weather data
│   ├── SpaceWeatherFragment.java      # Space weather fragment
│   ├── PhoneAuthActivity.java         # Phone authentication
│   ├── DiseaseModel.java              # TensorFlow Lite disease model wrapper
│   └── InsectModel.java               # TensorFlow Lite insect model wrapper
├── assets/                            # AI models and data files
│   ├── plant_disease_model.tflite    # Disease detection model (~9.8MB)
│   ├── insect_model.tflite           # Insect identification model (~9.8MB)
│   ├── disease_labels.json           # Disease class labels
│   ├── insect_labels.json            # Insect class labels
│   ├── plant_info.json               # Plant growing information
│   ├── cities.json                   # Cities data
│   └── countries.json                # Countries data
└── res/                              # Android resources (layouts, drawables, strings)
```

## Build Instructions

### Prerequisites
1. Android Studio (latest version recommended)
2. JDK 11 or higher
3. Android SDK with API 34
4. Create `local.properties` file in project root with API keys:
   ```properties
   WEATHER_API_KEY=your_weather_api_key
   TREFLE_API_KEY=your_trefle_api_key
   ```

### Build Commands
```bash
# Build the app
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest
```

## Coding Conventions

### Java Style Guidelines
- **Package Structure**: All code in `com.example.plantdisease` package
- **Naming Conventions**:
  - Classes: PascalCase (e.g., `MainActivity`, `DiseaseModel`)
  - Variables: camelCase (e.g., `imageView`, `resultText`)
  - Constants: UPPER_SNAKE_CASE (e.g., `PERMISSION_REQUEST_CODE`)
- **Logging**: Use `android.util.Log` with TAG constant in each class
- **Activity Lifecycle**: Always call `super.onCreate()` before `setContentView()`
- **Resource IDs**: Use descriptive names (e.g., `R.id.imageView`, `R.layout.activity_main`)

### Best Practices
- **Null Safety**: Always check for null before accessing intent extras, JSON objects, and optional data
- **Error Handling**: Wrap file operations, JSON parsing, and ML model inference in try-catch blocks
- **UI Thread**: Use `runOnUiThread()` for UI updates from background threads
- **Permissions**: Request runtime permissions for camera, storage, and location on API 23+
- **Memory Management**: Recycle bitmaps and close resources in `onDestroy()`
- **Compatibility**: Test TensorFlow Lite compatibility across Android versions (min API 21)

### TensorFlow Lite Considerations
- **Model Loading**: Load models from assets folder using `AssetManager`
- **Input Preprocessing**: Resize images to 224x224, normalize to [0,1] range
- **Version Lock**: Use TensorFlow Lite 2.12.0 for compatibility with older Android devices
- **Operation Support**: Use baseline TensorFlow Lite built-in operations only (no custom ops)
- **Threading**: Run model inference on background threads to avoid UI blocking

### Android-Specific
- **Assets**: Place TensorFlow Lite models and JSON data in `app/src/main/assets/`
- **Layouts**: Use ScrollView for content that may exceed screen height
- **Material Design**: Use CardView, MaterialButton, and Material themes for consistent UI
- **SharedPreferences**: Use for simple key-value storage (e.g., user settings, notepad data)
- **Intents**: Pass user data between activities using extras

## Testing Approach

### Manual Testing Priorities
1. Test on physical devices, not just emulators (especially for camera and ML features)
2. Test on older devices (API 21-23) for TensorFlow Lite compatibility
3. Test with poor lighting conditions and various image qualities
4. Verify offline functionality (all features except weather should work offline)
5. Test permission handling (grant, deny, and revoke scenarios)

### Automated Testing
- Unit tests in `app/src/test/` for model logic and data parsing
- Instrumented tests in `app/src/androidTest/` for UI and integration tests
- Focus on critical paths: disease detection, insect identification, data persistence

## Common Issues and Solutions

### Known Issues
1. **TensorFlow Lite Compatibility**: Version 2.13+ fails on older devices with native library errors
   - Solution: Use version 2.12.0 exactly as specified in build.gradle
2. **Model Operation Version**: Newer converters may use unsupported operations
   - Solution: Configure TensorFlow converter to use baseline operations only
3. **Asset Packaging**: Files must be in `assets/` folder to be included in APK
   - Solution: Verify with APK Analyzer, use exact case-matching filenames
4. **Permission Crashes**: Runtime permissions required on API 23+
   - Solution: Implement `ActivityCompat.requestPermissions()` and handle results
5. **Scrolling Issues**: Content may be cut off on small screens
   - Solution: Wrap layouts in ScrollView

### Performance Optimization
- Model inference: 2-3 seconds on typical devices
- Image preprocessing: Keep images under 224x224 to reduce memory usage
- Progress indicators: Always show during ML inference operations
- Battery efficiency: Avoid continuous camera preview when not needed

## File Modification Guidelines

### When Adding New Features
1. Create new Activity class in `java/com/example/plantdisease/`
2. Add corresponding layout XML in `res/layout/`
3. Register activity in `AndroidManifest.xml`
4. Add navigation from `HomeActivity` or appropriate parent
5. Update `strings.xml` for new UI text
6. Follow existing patterns for camera/gallery selection, ML inference, and result display

### When Modifying ML Models
1. Convert models to TensorFlow Lite format with proper optimization settings
2. Keep individual models under 10MB for reasonable APK size (current models are ~9.8MB each)
3. Update corresponding label JSON files in assets
4. Test thoroughly on multiple devices before committing
5. Document model architecture, training data, and expected accuracy

### When Updating Dependencies
1. Check TensorFlow Lite compatibility before upgrading
2. Test on minimum supported Android version (API 21)
3. Verify APK size impact
4. Update ProGuard rules if necessary
5. Document breaking changes in commit messages

## Security Considerations

- **API Keys**: Store in `local.properties`, never commit to repository
- **BuildConfig**: API keys accessed via BuildConfig in code
- **Permissions**: Request only necessary permissions with clear rationale
- **Data Privacy**: User data (name, region, notes) stored locally only
- **Network Security**: Use HTTPS for all network requests

## Accessibility

- Use high-contrast colors for outdoor visibility
- Large touch targets (minimum 48dp) for ease of use
- Content descriptions for ImageViews for screen readers
- Support for different screen sizes and densities
- Simple navigation patterns for users with limited technical literacy

## Future Enhancement Areas

- Multi-language support (i18n)
- Treatment recommendations for detected diseases
- Community features and farmer forums
- Crop health tracking with photo timelines
- Enhanced weather integration
- Expanded plant database
- Voice interface for low-literacy users

## Additional Resources

- [TensorFlow Lite Android Guide](https://www.tensorflow.org/lite/android)
- [Android Developer Documentation](https://developer.android.com)
- [Material Design Guidelines](https://material.io/design)
- [PlantVillage Dataset](https://github.com/spMohanty/PlantVillage-Dataset)
