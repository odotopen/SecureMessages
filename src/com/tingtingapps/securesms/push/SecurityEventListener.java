package com.tingtingapps.securesms.push;

import android.content.Context;

import com.tingtingapps.securesms.crypto.SecurityEvent;
import com.tingtingapps.securesms.recipients.RecipientFactory;
import com.tingtingapps.securesms.recipients.Recipients;
import com.tingtingapps.securesms.database.DatabaseFactory;

import org.whispersystems.textsecure.api.TextSecureMessageSender;
import org.whispersystems.textsecure.api.push.TextSecureAddress;

public class SecurityEventListener implements TextSecureMessageSender.EventListener {

  private static final String TAG = SecurityEventListener.class.getSimpleName();

  private final Context context;

  public SecurityEventListener(Context context) {
    this.context = context.getApplicationContext();
  }

  @Override
  public void onSecurityEvent(TextSecureAddress textSecureAddress) {
    Recipients recipients = RecipientFactory.getRecipientsFromString(context, textSecureAddress.getNumber(), false);
    long       threadId   = DatabaseFactory.getThreadDatabase(context).getThreadIdFor(recipients);

    SecurityEvent.broadcastSecurityUpdateEvent(context, threadId);
  }
}
