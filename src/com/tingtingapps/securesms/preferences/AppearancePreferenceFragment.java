package com.tingtingapps.securesms.preferences;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;

import com.tingtingapps.securesms.ApplicationPreferencesActivity;
import com.tingtingapps.securesms.util.TextSecurePreferences;

import java.util.Arrays;

public class AppearancePreferenceFragment extends ListSummaryPreferenceFragment {

  @Override
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    addPreferencesFromResource(com.tingtingapps.securesms.R.xml.preferences_appearance);

    this.findPreference(TextSecurePreferences.THEME_PREF).setOnPreferenceChangeListener(new ListSummaryListener());
    this.findPreference(TextSecurePreferences.LANGUAGE_PREF).setOnPreferenceChangeListener(new ListSummaryListener());
    initializeListSummary((ListPreference)findPreference(TextSecurePreferences.THEME_PREF));
    initializeListSummary((ListPreference)findPreference(TextSecurePreferences.LANGUAGE_PREF));
  }

  @Override
  public void onStart() {
    super.onStart();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener((ApplicationPreferencesActivity)getActivity());
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity) getActivity()).getSupportActionBar().setTitle(com.tingtingapps.securesms.R.string.preferences__appearance);
  }

  @Override
  public void onStop() {
    super.onStop();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener((ApplicationPreferencesActivity) getActivity());
  }

  public static CharSequence getSummary(Context context) {
    String[] languageEntries     = context.getResources().getStringArray(com.tingtingapps.securesms.R.array.language_entries);
    String[] languageEntryValues = context.getResources().getStringArray(com.tingtingapps.securesms.R.array.language_values);
    String[] themeEntries        = context.getResources().getStringArray(com.tingtingapps.securesms.R.array.pref_theme_entries);
    String[] themeEntryValues    = context.getResources().getStringArray(com.tingtingapps.securesms.R.array.pref_theme_values);

    int langIndex  = Arrays.asList(languageEntryValues).indexOf(TextSecurePreferences.getLanguage(context));
    int themeIndex = Arrays.asList(themeEntryValues).indexOf(TextSecurePreferences.getTheme(context));

    return context.getString(com.tingtingapps.securesms.R.string.preferences__theme_summary,    themeEntries[themeIndex]) + ", " +
           context.getString(com.tingtingapps.securesms.R.string.preferences__language_summary, languageEntries[langIndex]);
  }
}
