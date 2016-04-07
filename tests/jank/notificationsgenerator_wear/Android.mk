LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    $(call all-Iaidl-files-under, src)

LOCAL_SDK_VERSION := current

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PACKAGE_NAME := NotificationsGeneratorWear

LOCAL_STATIC_JAVA_LIBRARIES := \
    google-common \
    android-support-v4

LOCAL_CERTIFICATE := vendor/unbundled_google/libraries/certs/clockwork

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# For GmsCore.
LOCAL_STATIC_JAVA_LIBRARIES += prebuilt-google-play-services-first-party-for-clockwork
LOCAL_AAPT_FLAGS += --auto-add-overlay --extra-packages com.google.android.gms
LOCAL_RESOURCE_DIR += vendor/unbundled_google/packages/ClockworkPrebuilts/libs/GmsCore/first_party_res

LOCAL_JAVA_LIBRARIES += org.apache.http.legacy

include $(BUILD_PACKAGE)
