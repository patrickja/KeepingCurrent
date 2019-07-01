package com.example.android.keepingcurrent.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragment;

import com.example.android.keepingcurrent.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
