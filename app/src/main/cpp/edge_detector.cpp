#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <string>

#define LOG_TAG "EdgeDetector"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static bool processEnabled = true;
static bool g_processingEnabled = true; // Added missing global variable

extern "C" {
JNIEXPORT void JNICALL Java_com_example_edgedetection_NativeBridge_setProcessingMode(
        JNIEnv* env, jobject thiz, jboolean process
);

JNIEXPORT void JNICALL Java_com_example_edgedetection_NativeBridge_processFrame(
        JNIEnv* env, jobject thiz,
        jobject yBuffer, jobject uBuffer, jobject vBuffer,
        jint width, jint height,
        jint yRowStride, jint uvRowStride, jint uvPixelStride
);

JNIEXPORT jboolean JNICALL
Java_com_example_edgedetection_NativeBridge_00024Companion_initializeDetector(JNIEnv *env, jclass clazz);

JNIEXPORT jintArray JNICALL
Java_com_example_edgedetection_NativeBridge_00024Companion_detectEdges(JNIEnv *env, jclass clazz,
                                                        jbyteArray image_data,
                                                        jint width, jint height);

JNIEXPORT jboolean JNICALL
Java_com_example_edgedetection_NativeBridge_setProcessingMode(JNIEnv *env, jclass clazz, jboolean enabled);
}

JNIEXPORT void JNICALL Java_com_example_edgedetection_NativeBridge_setProcessingMode(
        JNIEnv* env, jobject thiz, jboolean process
) {
    processEnabled = process;
}

JNIEXPORT void JNICALL Java_com_example_edgedetection_NativeBridge_processFrame(
        JNIEnv* env, jobject thiz,
        jobject yBuffer, jobject uBuffer, jobject vBuffer,
        jint width, jint height,
        jint yRowStride, jint uvRowStride, jint uvPixelStride
) {
    uint8_t* yPlane = static_cast<uint8_t*>(env->GetDirectBufferAddress(yBuffer));
    uint8_t* uPlane = static_cast<uint8_t*>(env->GetDirectBufferAddress(uBuffer));
    uint8_t* vPlane = static_cast<uint8_t*>(env->GetDirectBufferAddress(vBuffer));

    cv::Mat yMat(height, yRowStride, CV_8UC1, yPlane);
    cv::Mat uMat(height / 2, uvRowStride, CV_8UC1, uPlane);
    cv::Mat vMat(height / 2, uvRowStride, CV_8UC1, vPlane);

    // Crop to actual size
    cv::Rect roi(0, 0, width, height);
    yMat = yMat(roi);
    roi = cv::Rect(0, 0, width / 2, height / 2);
    uMat = uMat(roi);
    vMat = vMat(roi);

    // Convert to RGB
    cv::Mat yuvMat;
    std::vector<cv::Mat> yuvChannels = {yMat, uMat, vMat};
    cv::merge(yuvChannels, yuvMat);
    cv::Mat rgbMat;
    cv::cvtColor(yuvMat, rgbMat, cv::COLOR_YUV2RGB_NV21);

    if (processEnabled) {
        cv::Mat gray, edges;
        cv::cvtColor(rgbMat, gray, cv::COLOR_RGB2GRAY);
        cv::GaussianBlur(gray, gray, cv::Size(5, 5), 1.5);
        cv::Canny(gray, edges, 50, 150);
        cv::cvtColor(edges, rgbMat, cv::COLOR_GRAY2RGB);
    }

    // Convert back to YUV for rendering
    cv::cvtColor(rgbMat, yuvMat, cv::COLOR_RGB2YUV);
    cv::split(yuvMat, yuvChannels);

    // Copy processed data back to original buffers
    cv::Mat yProcessed = yuvChannels[0];
    cv::Mat uProcessed = yuvChannels[1];
    cv::Mat vProcessed = yuvChannels[2];

    memcpy(yPlane, yProcessed.data, yProcessed.total() * yProcessed.elemSize());
    memcpy(uPlane, uProcessed.data, uProcessed.total() * uProcessed.elemSize());
    memcpy(vPlane, vProcessed.data, vProcessed.total() * vProcessed.elemSize());
}

JNIEXPORT jboolean JNICALL
Java_com_example_edgedetection_NativeBridge_00024Companion_initializeDetector(JNIEnv *env, jclass clazz) {
    LOGI("Native edge detector initialized");
    return JNI_TRUE;
}

JNIEXPORT jintArray JNICALL
Java_com_example_edgedetection_NativeBridge_00024Companion_detectEdges(JNIEnv *env, jclass clazz,
                                                        jbyteArray image_data,
                                                        jint width, jint height) {
    LOGI("Detecting edges for image %dx%d", width, height);

    // Create a dummy result - a real implementation would do edge detection here
    jintArray result = env->NewIntArray(width * height);
    if (result == NULL) {
        return NULL; // Out of memory
    }

    // For now, just return an empty array
    jint *buffer = new jint[width * height]();
    env->SetIntArrayRegion(result, 0, width * height, buffer);
    delete[] buffer;

    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_example_edgedetection_NativeBridge_setProcessingMode(JNIEnv *env, jclass clazz, jboolean enabled) {
    g_processingEnabled = enabled;
    LOGI("Processing mode set to: %s", g_processingEnabled ? "enabled" : "disabled");
    return JNI_TRUE;
}