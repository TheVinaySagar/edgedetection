cmake_minimum_required(VERSION 3.10.2)
project(edge-detector)

# Set path to OpenCV SDK
set(OpenCV_DIR /home/VinaySagar/Libraries/OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Add the native edge detector library
add_library(
        edge-detector
        SHARED
        edge_detector.cpp
)

# Find the Android log library
find_library(
        log-lib
        log
)

# Link the libraries
target_link_libraries(
        edge-detector
        ${OpenCV_LIBS}
        ${log-lib}
)

# Print some debugging info
message(STATUS "OpenCV libraries: ${OpenCV_LIBS}")
message(STATUS "OpenCV include dirs: ${OpenCV_INCLUDE_DIRS}")
