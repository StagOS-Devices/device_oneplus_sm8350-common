#! /vendor/bin/sh
#=============================================================================
# Copyright (c) 2019-2020 Qualcomm Technologies, Inc.
# All Rights Reserved.
# Confidential and Proprietary - Qualcomm Technologies, Inc.
#=============================================================================


# Copy gcam config
rm -r /data/data/com.google.android.GoogleCameraEng/shared_prefs/com.google.android.GoogleCameraEng_preferences.xml
cp /vendor/etc/com.google.android.GoogleCameraEng_preferences.xml /data/data/com.google.android.GoogleCameraEng/shared_prefs/com.google.android.GoogleCameraEng_preferences.xml
cp /vendor/etc/com.google.android.GoogleCameraEng_preferences.xml /data/data/com.google.android.GoogleCameraEng/shared_prefs/diditwork.xml
