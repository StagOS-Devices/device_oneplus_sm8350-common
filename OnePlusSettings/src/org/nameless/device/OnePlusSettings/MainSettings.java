/*
 * Copyright (C) 2016 The OmniROM Project
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

package org.nameless.device.OnePlusSettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.telephony.SubscriptionManager;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel;

import org.nameless.device.OnePlusSettings.Constants;
import org.nameless.device.OnePlusSettings.Doze.DozeSettingsActivity;
import org.nameless.device.OnePlusSettings.Preferences.CustomSeekBarPreference;
import org.nameless.device.OnePlusSettings.Preferences.SwitchPreference;
import org.nameless.device.OnePlusSettings.Preferences.VibratorStrengthPreference;
import org.nameless.device.OnePlusSettings.Utils.FileUtils;
import org.nameless.device.OnePlusSettings.Utils.FpsUtils;
import org.nameless.device.OnePlusSettings.Utils.HBMUtils;
import org.nameless.device.OnePlusSettings.Utils.Protocol;
import org.nameless.device.OnePlusSettings.Utils.SwitchUtils;
import org.nameless.device.OnePlusSettings.Utils.VibrationUtils;
import org.nameless.device.OnePlusSettings.Utils.VolumeUtils;

public class MainSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_MUTE_MEDIA = "mute_media";
    public static final String KEY_DC_SWITCH = "dc_dim";
    public static final String KEY_AUTO_HBM_SWITCH = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_VIBSTRENGTH = "vib_strength";

    public static final String KEY_ALERT_SLIDER_TOP_POSITION = "alert_slider_top_position";
    public static final String KEY_ALERT_SLIDER_MIDDLE_POSITION = "alert_slider_middle_position"; 
    public static final String KEY_ALERT_SLIDER_BOTTOM_POSITION = "alert_slider_bottom_position";

    private static final String KEY_PREF_DOZE = "advanced_doze_settings";
    private static final String KEY_FPS_INFO_POSITION = "fps_info_position";
    private static final String KEY_FPS_INFO_COLOR = "fps_info_color";
    private static final String KEY_FPS_INFO_TEXT_SIZE = "fps_info_text_size";
    private static final String KEY_NR_MODE_SWITCHER = "nr_mode_switcher";

    // Mutable strings
    public static String mAlertSliderTopActionValue = Constants.ACTION_SILENT;
    public static String mAlertSliderMiddleActionValue = Constants.ACTION_VIBRATE;
    public static String mAlertSliderBottomActionValue = Constants.ACTION_RING;

    private ListPreference mFpsInfoColor;
    private ListPreference mFpsInfoPosition;
    private ListPreference mNrModeSwitcher;
    private ListPreference mAlertSliderTopAction;
    private ListPreference mAlertSliderMiddleAction;
    private ListPreference mAlertSliderBottomAction;
    private Preference mDozeSettings;
    private SwitchPreference mMuteMedia;
    private SwitchPreference mDCModeSwitch;
    private SwitchPreference mAutoHBMSwitch;
    private SwitchPreference mHBMModeSwitch;
    private SwitchPreference mFpsInfo;
    private CustomSeekBarPreference mFpsInfoTextSizePreference;
    private VibratorStrengthPreference mVibratorStrengthPreference;

    private ModeSwitch DCModeSwitch;
    private ModeSwitch HBMModeSwitch;

    private Protocol mProtocol;
    private Runnable mUnbindService;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final Context context = getContext();
        addPreferencesFromResource(R.xml.main);

        Intent intent = new Intent();
        intent.setClassName("com.qualcomm.qcrilmsgtunnel", "com.qualcomm.qcrilmsgtunnel.QcrilMsgTunnelService");
        context.bindServiceAsUser(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                IQcrilMsgTunnel tunnel = IQcrilMsgTunnel.Stub.asInterface(service);
                if (tunnel != null)
                    mProtocol = new Protocol(tunnel);

                ServiceConnection serviceConnection = this;

                mUnbindService = () -> context.unbindService(serviceConnection);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mProtocol = null;
            }
        }, context.BIND_AUTO_CREATE, UserHandle.CURRENT);

        mMuteMedia = (SwitchPreference) findPreference(KEY_MUTE_MEDIA);
        mMuteMedia.setChecked(VolumeUtils.isCurrentlyEnabled(context));
        mMuteMedia.setOnPreferenceChangeListener(this);

        mDCModeSwitch = (SwitchPreference) findPreference(KEY_DC_SWITCH);
        DCModeSwitch = SwitchUtils.getDCModeSwitch(context, mDCModeSwitch);
        if (DCModeSwitch.isSupported()) {
            mDCModeSwitch.setEnabled(true);
        } else {
            mDCModeSwitch.setEnabled(false);
            mDCModeSwitch.setSummary(getString(R.string.unsupported_feature));
        }

        mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled());
        mDCModeSwitch.setOnPreferenceChangeListener(DCModeSwitch);

        mHBMModeSwitch = (SwitchPreference) findPreference(KEY_HBM_SWITCH);
        HBMModeSwitch = SwitchUtils.getHBMModeSwitch(context, mHBMModeSwitch);
        if (HBMModeSwitch.isSupported()) {
            mHBMModeSwitch.setEnabled(true);
        } else {
            mHBMModeSwitch.setEnabled(false);
            mHBMModeSwitch.setSummary(getString(R.string.unsupported_feature));
        }
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
        mHBMModeSwitch.setOnPreferenceChangeListener(HBMModeSwitch);

        mAutoHBMSwitch = (SwitchPreference) findPreference(KEY_AUTO_HBM_SWITCH);
        if (mHBMModeSwitch.isEnabled()) {
            mAutoHBMSwitch.setEnabled(true);
        } else {
            mAutoHBMSwitch.setEnabled(false);
            mAutoHBMSwitch.setSummary(getString(R.string.unsupported_feature));
        }
        mAutoHBMSwitch.setChecked(HBMUtils.isAutoHBMEnabled(context));
        mAutoHBMSwitch.setOnPreferenceChangeListener(this);

        mDozeSettings = (Preference) findPreference(KEY_PREF_DOZE);
        mDozeSettings.setOnPreferenceClickListener(preference -> {
            Intent i = new Intent(getActivity().getApplicationContext(), DozeSettingsActivity.class);
            startActivity(i);
            return true;
        });

        mFpsInfo = (SwitchPreference) findPreference(KEY_FPS_INFO);
        mFpsInfo.setChecked(FpsUtils.isFPSOverlayRunning(context));
        mFpsInfo.setOnPreferenceChangeListener(this);

        mFpsInfoPosition = (ListPreference) findPreference(KEY_FPS_INFO_POSITION);
        mFpsInfoPosition.setOnPreferenceChangeListener(this);

        mFpsInfoColor = (ListPreference) findPreference(KEY_FPS_INFO_COLOR);
        mFpsInfoColor.setOnPreferenceChangeListener(this);

        mAlertSliderTopAction = (ListPreference) findPreference(KEY_ALERT_SLIDER_TOP_POSITION);
        mAlertSliderTopAction.setOnPreferenceChangeListener(this);

        mAlertSliderMiddleAction = (ListPreference) findPreference(KEY_ALERT_SLIDER_MIDDLE_POSITION);
        mAlertSliderMiddleAction.setOnPreferenceChangeListener(this);

        mAlertSliderBottomAction = (ListPreference) findPreference(KEY_ALERT_SLIDER_BOTTOM_POSITION);
        mAlertSliderBottomAction.setOnPreferenceChangeListener(this);

        mFpsInfoTextSizePreference = (CustomSeekBarPreference) findPreference(KEY_FPS_INFO_TEXT_SIZE);
        mFpsInfoTextSizePreference.setOnPreferenceChangeListener(this);

        mNrModeSwitcher = (ListPreference) findPreference(KEY_NR_MODE_SWITCHER);
        mNrModeSwitcher.setOnPreferenceChangeListener(this);

        mVibratorStrengthPreference =  (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (FileUtils.isFileWritable(VibrationUtils.FILE_LEVEL)) {
            mVibratorStrengthPreference.setValue(PreferenceManager.getDefaultSharedPreferences(context).
                    getInt(KEY_VIBSTRENGTH, VibrationUtils.getVibStrength()));
            mVibratorStrengthPreference.setOnPreferenceChangeListener(this);
        } else {
            mVibratorStrengthPreference.setEnabled(false);
            mVibratorStrengthPreference.setSummary(getString(R.string.unsupported_feature));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbindService != null) {
            mUnbindService.run();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
        mFpsInfo.setChecked(FpsUtils.isFPSOverlayRunning(getContext()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        if (preference == mMuteMedia) {
            Boolean enabled = (Boolean) newValue;
            VolumeUtils.setEnabled(context, enabled);
        } else if (preference == mAutoHBMSwitch) {
            Boolean enabled = (Boolean) newValue;
            HBMUtils.setAutoHBMEnabled(context, enabled);
            HBMUtils.enableService(context);
        } else if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            FpsUtils.setFpsService(context, enabled);
        } else if (preference == mFpsInfoPosition) {
            int position = Integer.parseInt(newValue.toString());
            if (FpsUtils.isPositionChanged(context, position)) {
                FpsUtils.setPosition(context, position);
                FpsUtils.notifySettingsUpdated(context);
            }
        } else if (preference == mFpsInfoColor) {
            int color = Integer.parseInt(newValue.toString());
            if (FpsUtils.isColorChanged(context, color)) {
                FpsUtils.setColorIndex(context, color);
                FpsUtils.notifySettingsUpdated(context);
            }
        } else if (preference == mFpsInfoTextSizePreference) {
            int size = Integer.parseInt(newValue.toString());
            if (FpsUtils.isSizeChanged(context, size - 1)) {
                FpsUtils.setSizeIndex(context, size - 1);
                FpsUtils.notifySettingsUpdated(context);
            }
        } else if (preference == mNrModeSwitcher) {
            int mode = Integer.parseInt(newValue.toString());
            return setNrModeChecked(mode);
        } else if (preference == mVibratorStrengthPreference) {
            int value = Integer.parseInt(newValue.toString());
            PreferenceManager.getDefaultSharedPreferences(context).edit().
                    putInt(KEY_VIBSTRENGTH, value).commit();
            VibrationUtils.setVibStrength(context, value);
            VibrationUtils.doHapticFeedback(context, VibrationEffect.EFFECT_CLICK, true);
        } else if (preference == mAlertSliderTopAction) {
            String value = newValue.toString();
            mAlertSliderTopActionValue = value;
        } else if (preference == mAlertSliderMiddleAction) {
            String value = newValue.toString();
            mAlertSliderMiddleActionValue = value;
        } else if (preference == mAlertSliderBottomAction) {
            String value = newValue.toString();
            mAlertSliderBottomActionValue = value;
        }
        return true;
    }

    private boolean setNrModeChecked(int mode) {
        if (mode == 0) {
            return setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_SA);
        } else if (mode == 1) {
            return setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_NSA);
        } else {
            return setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE.NAS_NR5G_DISABLE_MODE_NONE);
        }
    }

    private boolean setNrModeChecked(Protocol.NR_5G_DISABLE_MODE_TYPE mode) {
        if (mProtocol == null) {
            Toast.makeText(getContext(), R.string.service_not_ready, Toast.LENGTH_LONG).show();
            return false;
        }
        int index = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
        if (index == SubscriptionManager.INVALID_SIM_SLOT_INDEX) {
            Toast.makeText(getContext(), R.string.unavailable_sim_slot, Toast.LENGTH_LONG).show();
            return false;
        }
        new Thread(() -> mProtocol.setNrMode(index, mode)).start();
        return true;
    }
}
