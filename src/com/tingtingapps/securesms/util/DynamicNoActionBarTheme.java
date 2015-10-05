package com.tingtingapps.securesms.util;

import android.app.Activity;

public class DynamicNoActionBarTheme extends DynamicTheme {
  @Override
  protected int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity);

    if (theme.equals("dark")) return com.tingtingapps.securesms.R.style.TextSecure_DarkNoActionBar;

    return com.tingtingapps.securesms.R.style.TextSecure_LightNoActionBar;
  }
}
