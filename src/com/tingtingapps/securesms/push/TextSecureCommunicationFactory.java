package com.tingtingapps.securesms.push;

import android.content.Context;

import com.tingtingapps.securesms.util.TextSecurePreferences;
import com.tingtingapps.securesms.BuildConfig;

import org.whispersystems.textsecure.api.TextSecureAccountManager;

public class TextSecureCommunicationFactory {

  public static TextSecureAccountManager createManager(Context context) {
    return new TextSecureAccountManager(BuildConfig.PUSH_URL,
                                        new TextSecurePushTrustStore(context),
                                        TextSecurePreferences.getLocalNumber(context),
                                        TextSecurePreferences.getPushServerPassword(context));
  }

  public static TextSecureAccountManager createManager(Context context, String number, String password) {
    return new TextSecureAccountManager(BuildConfig.PUSH_URL, new TextSecurePushTrustStore(context),
                                        number, password);
  }

}
