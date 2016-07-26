/*
 * Copyright (C) 2015 The CyanogenMod Project
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

package com.cyanogenmod.settings.device;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;

public class TouchscreenGestureSettings extends PreferenceActivity {

    private static final String KEY_AMBIENT_DISPLAY_ENABLE = "ambient_display_enable";
    private static final String KEY_GESTURE_HAND_WAVE = "gesture_hand_wave";
    private static final String KEY_GESTURE_PICK_UP = "gesture_pick_up";
    private static final String KEY_GESTURE_POCKET = "gesture_pocket";
    private static final String KEY_PROXIMITY_WAKE = "proximity_wake_enable";
    private static final String KEY_TAP_TO_WAKE = "tap_to_wake";
	
    private static final String RESOURCE_PACKAGE = "com.android.settings";
    private static final String RESOURCE_KEY_DT_TITLE = "double_tap_to_wake_title";
    private static final String RESOURCE_KEY_DT_SUMMARY = "double_tap_to_wake_summary";
	
    private SwitchPreference mAmbientDisplayPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mPickupPreference;
    private SwitchPreference mPocketPreference;
    private SwitchPreference mProximityWakePreference;
    private SwitchPreference mTapToWakePreference;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gesture_panel);
        boolean dozeEnabled = isDozeEnabled();
        mAmbientDisplayPreference = (SwitchPreference) findPreference(KEY_AMBIENT_DISPLAY_ENABLE);
        mAmbientDisplayPreference.setChecked(dozeEnabled);
        mAmbientDisplayPreference.setOnPreferenceChangeListener(mAmbientDisplayPrefListener);
        mHandwavePreference = (SwitchPreference) findPreference(KEY_GESTURE_HAND_WAVE);
        mHandwavePreference.setEnabled(dozeEnabled);
        mHandwavePreference.setOnPreferenceChangeListener(mGesturePrefListener);
        mPickupPreference = (SwitchPreference) findPreference(KEY_GESTURE_PICK_UP);
        mPickupPreference.setEnabled(dozeEnabled);
        mPocketPreference = (SwitchPreference) findPreference(KEY_GESTURE_POCKET);
        mPocketPreference.setEnabled(dozeEnabled);
        mProximityWakePreference = (SwitchPreference) findPreference(KEY_PROXIMITY_WAKE);
        mProximityWakePreference.setOnPreferenceChangeListener(mGesturePrefListener);
        mTapToWakePreference = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);
        mTapToWakePreference.setOnPreferenceChangeListener(this);
        mTapToWakePreference.setTitle(getString(RESOURCE_PACKAGE, RESOURCE_KEY_DT_TITLE, "fail"));
        mTapToWakePreference.setSummary(getString(RESOURCE_PACKAGE, RESOURCE_KEY_DT_SUMMARY, "fail"));
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void updateState() {
        // Update tap to wake if it is available.
        if (mTapToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, 0);
            mTapToWakePreference.setChecked(value != 0);
        }
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mTapToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, value ? 1 : 0);
        }
        return true;
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        getListView().setPadding(0, 0, 0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private boolean enableDoze(boolean enable) {
        return Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.DOZE_ENABLED, enable ? 1 : 0);
    }

    private boolean isDozeEnabled() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.DOZE_ENABLED, 1) != 0;
    }

    private Preference.OnPreferenceChangeListener mAmbientDisplayPrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean enable = (boolean) newValue;
            boolean ret = enableDoze(enable);
            if (ret) {
                mHandwavePreference.setEnabled(enable);
                mPickupPreference.setEnabled(enable);
                mPocketPreference.setEnabled(enable);
            }
            return ret;
        }
    };

    private Preference.OnPreferenceChangeListener mGesturePrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ((boolean) newValue) {
                final String key = preference.getKey();
                if (KEY_GESTURE_HAND_WAVE.equals(key)) {
                    mProximityWakePreference.setChecked(false);
                } else if (KEY_PROXIMITY_WAKE.equals(key)) {
                    mHandwavePreference.setChecked(false);
                }
            }
            return true;
        }

    };
    // try to get the resource if not fail and return the def value
    private String getString(String packageName, String resourceKey, String defValue){
        try
        {
            // get package manager
            PackageManager manager = getPackageManager();
            // get resources
            Resources resources = manager.getResourcesForApplication(packageName);
            // find the id#
            int resId = resources.getIdentifier(resourceKey, "string", packageName);
            // return the string
            return resources.getString(resId);
        }
        catch (Exception e)
        {
            // epic failure... return the default value 
            return defValue;
            // print the error for good measure.
            e.printStackTrace();
        }
    }
}
