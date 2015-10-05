package com.tingtingapps.securesms.components;

import android.content.Context;

import com.tingtingapps.securesms.util.Util;

public class ExpiredBuildReminder extends Reminder {

  private static final String TAG = ExpiredBuildReminder.class.getSimpleName();

  public ExpiredBuildReminder() {
    super(com.tingtingapps.securesms.R.drawable.ic_warning_dark,
          com.tingtingapps.securesms.R.string.reminder_header_expired_build,
          com.tingtingapps.securesms.R.string.reminder_header_expired_build_details);
  }

  @Override
  public boolean isDismissable() {
    return false;
  }

  public static boolean isEligible(Context context) {
    return !Util.isBuildFresh();
  }

}
