package com.tingtingapps.securesms.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.tingtingapps.securesms.preferences.NotificationPrivacyPreference;
import com.tingtingapps.securesms.database.RecipientPreferenceDatabase;

public class FailedNotificationBuilder extends AbstractNotificationBuilder {

  public FailedNotificationBuilder(Context context, NotificationPrivacyPreference privacy, Intent intent) {
    super(context, privacy);

    setSmallIcon(com.tingtingapps.securesms.R.drawable.icon_notification);
    setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                              com.tingtingapps.securesms.R.drawable.ic_action_warning_red));
    setContentTitle(context.getString(com.tingtingapps.securesms.R.string.MessageNotifier_message_delivery_failed));
    setContentText(context.getString(com.tingtingapps.securesms.R.string.MessageNotifier_failed_to_deliver_message));
    setTicker(context.getString(com.tingtingapps.securesms.R.string.MessageNotifier_error_delivering_message));
    setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
    setAutoCancel(true);
    setAlarms(null, RecipientPreferenceDatabase.VibrateState.DEFAULT);
  }



}
