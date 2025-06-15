# Edge Detection Android App

An Android application that performs real-time edge detection on camera frames using OpenGL and JNI with a native C++ implementation.

## ✅ Features

- Real-time camera preview with edge detection processing
- OpenGL rendering for efficient preview display
- Native C++ processing for optimal performance
- Toggle between normal camera view and edge detection mode
- Permission handling for camera access

## 📷 Screenshots

![Edge Detection Demo](screenshots/edge_detection_demo.gif)

*Screenshots will be available after running the app*

## ⚙️ Setup Instructions

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or newer
- Android NDK (Side by side) r21 or newer
- CMake 3.18.1 or newer
- OpenCV SDK for Android (4.5.0 or newer)

### Setting up the project

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/edgedetection.git
   cd edgedetection
   ```

2. **Setup OpenCV**
   - Download OpenCV SDK for Android from [https://opencv.org/releases/](https://opencv.org/releases/)
   - Extract the OpenCV SDK to a known location
   - Create a `sdk` directory in the project root and copy the required libraries:
     ```bash
     mkdir -p app/src/main/jniLibs/arm64-v8a
     cp path/to/opencv-android-sdk/sdk/native/libs/arm64-v8a/libopencv_java4.so app/src/main/jniLibs/arm64-v8a/
     ```

3. **Configure CMake**
   - Make sure your `app/build.gradle` includes the correct externalNativeBuild configuration:
     ```gradle
     android {
         // ...
         externalNativeBuild {
             cmake {
                 path "src/main/cpp/CMakeLists.txt"
             }
         }
         // ...
     }
     ```
   - Update the `CMakeLists.txt` to include OpenCV dependencies

4. **Add GLSL Shader Files**
   - Create the directory:
     ```bash
     mkdir -p app/src/main/res/raw
     ```
   - Add the vertex shader as `app/src/main/res/raw/vertex_shader.glsl`
   - Add the fragment shader as `app/src/main/res/raw/fragment_shader.glsl`

5. **Build and Run**
   - Build the project with Gradle
   - Run on a physical device (emulators may have limited camera functionality)

## 🧠 Architecture

### Core Components

1. **MainActivity**: Entry point of the application, manages permissions and initializes the camera and renderer

2. **EdgeDetectionRenderer**: OpenGL renderer that creates a texture for the camera preview and manages the shader program

3. **CameraManager**: Handles camera preview and frame analysis, including sending frames to the native code

4. **ShaderProgram**: Utility class to handle OpenGL shader programs

5. **NativeBridge**: JNI bridge to the C++ code for edge detection processing

### Data Flow

+----------------+       +-------------------+       +----------------------+
|  CameraX API   | ----> |  Frame Analyzer   | ----> |  NativeBridge (JNI)  |
+----------------+       +-------------------+       +----------------------+
        |                                                    |
        |                                                    ▼
        |                                        +------------------------+
        |                                        |  C++ Edge Detection    |
        |                                        +------------------------+
        |                                                    |
        ▼                                                    |
+----------------+                                           |
| SurfaceTexture | <-----------------------------------------+
+----------------+
        |
        ▼
+----------------+
| OpenGL Surface |
+----------------+
        |
        ▼
+----------------+
|    Display     |
+----------------+
