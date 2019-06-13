LOCAL_PATH := $(call my-dir)

ifeq ($(WITH_GAPPS), true)
#GCam
include $(CLEAR_VARS)
LOCAL_MODULE := Gcam
LOCAL_SRC_FILES := priv-app/Gcam.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_PRIVILEGED_MODULE := true
LOCAL_OVERRIDES_PACKAGES := SnapdragonCamera Snap Camera2 GoogleCamera
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_DEX_PREOPT := false
include $(BUILD_PREBUILT)
endif