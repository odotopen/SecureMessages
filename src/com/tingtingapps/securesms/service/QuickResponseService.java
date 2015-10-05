package com.tingtingapps.securesms.service;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tingtingapps.securesms.sms.OutgoingTextMessage;
import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.database.ThreadDatabase;
import com.tingtingapps.securesms.mms.OutgoingMediaMessage;
import com.tingtingapps.securesms.mms.SlideDeck;
import com.tingtingapps.securesms.recipients.RecipientFactory;
import com.tingtingapps.securesms.recipients.Recipients;
import com.tingtingapps.securesms.sms.MessageSender;
import com.tingtingapps.securesms.util.Rfc5724Uri;

import java.net.URISyntaxException;
import java.net.URLDecoder;

public class QuickResponseService extends MasterSecretIntentService {

  private static final String TAG = QuickResponseService.class.getSimpleName();

  public QuickResponseService() {
    super("QuickResponseService");
  }

  @Override
  protected void onHandleIntent(Intent intent, @Nullable MasterSecret masterSecret) {
    if (!TelephonyManager.ACTION_RESPOND_VIA_MESSAGE.equals(intent.getAction())) {
      Log.w(TAG, "Received unknown intent: " + intent.getAction());
      return;
    }

    if (masterSecret == null) {
      Log.w(TAG, "Got quick response request when locked...");
      Toast.makeText(this, com.tingtingapps.securesms.R.string.QuickResponseService_quick_response_unavailable_when_TextSecure_is_locked, Toast.LENGTH_LONG).show();
      return;
    }

    try {
      Rfc5724Uri uri        = new Rfc5724Uri(intent.getDataString());
      String     content    = intent.getStringExtra(Intent.EXTRA_TEXT);
      String     numbers    = uri.getPath();
      if(numbers.contains("%")){
        numbers = URLDecoder.decode(numbers);
      }
      Recipients recipients = RecipientFactory.getRecipientsFromString(this, numbers, false);

      if (!TextUtils.isEmpty(content)) {
        if (recipients.isSingleRecipient()) {
          MessageSender.send(this, masterSecret, new OutgoingTextMessage(recipients, content), -1, false);
        } else {
          MessageSender.send(this, masterSecret, new OutgoingMediaMessage(this, recipients, new SlideDeck(), content,
                                                                          ThreadDatabase.DistributionTypes.DEFAULT), -1, false);
        }
      }
    } catch (URISyntaxException e) {
      Toast.makeText(this, com.tingtingapps.securesms.R.string.QuickResponseService_problem_sending_message, Toast.LENGTH_LONG).show();
      Log.w(TAG, e);
    }
  }
}
