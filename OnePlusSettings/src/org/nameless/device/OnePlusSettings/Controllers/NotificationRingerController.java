/*
 * Copyright (C) 2018 The LineageOS Project
 * Copyright (C) 2022 The Nameless-AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nameless.device.OnePlusSettings.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.KeyEvent;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import androidx.annotation.Keep;

import com.android.internal.os.DeviceKeyHandler;

import org.nameless.device.OnePlusSettings.Constants;
import org.nameless.device.OnePlusSettings.Utils.FileUtils;
import org.nameless.device.OnePlusSettings.Utils.VibrationUtils;
import org.nameless.device.OnePlusSettings.Utils.VolumeUtils;

public final class NotificationRingerController {
    private static final String TAG = "NotificationRingerController";

    private final Context mContext;
    private final AudioManager mAudioManager;

    private final SharedPreferences mSharedPreferences;

    public NotificationRingerController(Context context) {
        mContext = context;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        
        mAudioManager = mContext.getSystemService(AudioManager.class);
    }

    public boolean processAction(String actionCode, String lastActionCode) {

        // Get Top action value from SharedPreferences
        String topAction = mSharedPreferences.getString(Constants.KEY_ALERT_SLIDER_TOP_POSITION, "2");
        
        // Get Middle action value from SharedPreferences
        String middleAction = mSharedPreferences.getString(Constants.KEY_ALERT_SLIDER_MIDDLE_POSITION, "3");

        // Get Bottom action value from SharedPreferences
        String bottomAction = mSharedPreferences.getString(Constants.KEY_ALERT_SLIDER_BOTTOM_POSITION, "1");

        Log.d(TAG, "AlertSlider: topAction: " + topAction);
        Log.d(TAG, "AlertSlider: middleAction: " + middleAction);
        Log.d(TAG, "AlertSlider: bottomAction: " + bottomAction);

        if (actionCode.equals(Constants.ACTION_NONE)) {
            Log.d(TAG, "AlertSlider: actionCode=ACTION_NONE");
            return false;
        }

        if (actionCode.equals(lastActionCode)) {
            Log.d(TAG, "AlertSlider: actionCode=lastActionCode");
            return false;
        }

        switch (actionCode) {
            case Constants.ACTION_SILENT:
                Log.d(TAG, "AlertSlider: actionCode=ACTION_SILENT");
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
                VolumeUtils.changeMediaVolume(mAudioManager, mContext);
                break;
            case Constants.ACTION_VIBRATE:
                Log.d(TAG, "AlertSlider: actionCode=ACTION_VIBRATE");
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
                VibrationUtils.doHapticFeedback(mContext, VibrationEffect.EFFECT_DOUBLE_CLICK, true);
                if (lastActionCode.equals(Constants.ACTION_SILENT)) VolumeUtils.changeMediaVolume(mAudioManager, mContext);
                break;
            case Constants.ACTION_RING:
                Log.d(TAG, "AlertSlider: actionCode=ACTION_RING");
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                VibrationUtils.doHapticFeedback(mContext, VibrationEffect.EFFECT_HEAVY_CLICK, true);
                if (lastActionCode.equals(Constants.ACTION_SILENT)) VolumeUtils.changeMediaVolume(mAudioManager, mContext);
                break;
            default:
                Log.d(TAG, "AlertSlider: actionCode=default");
                return false;
        }
        return true;
    }
}