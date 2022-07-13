package org.nameless.device.OnePlusSettings;

import java.util.HashMap;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceManager;

import android.media.AudioManager;

public class Constants {

    // Preference keys
    public static final String KEY_ALERT_SLIDER_TOP_POSITION = "alert_slider_top_position";
    public static final String KEY_ALERT_SLIDER_MIDDLE_POSITION = "alert_slider_middle_position"; 
    public static final String KEY_ALERT_SLIDER_BOTTOM_POSITION = "alert_slider_bottom_position";

    // Slider key codes
    public static final String MODE_TOP = "1";
    public static final String MODE_MIDDLE = "2";
    public static final String MODE_BOTTOM = "3";
    
    // Slider actions
    public static final String ACTION_NONE = "0";
    public static final String ACTION_RING = "1";
    public static final String ACTION_SILENT = "2";
    public static final String ACTION_VIBRATE = "3";
}
