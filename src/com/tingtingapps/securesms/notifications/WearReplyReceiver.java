/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tingtingapps.securesms.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;

import com.tingtingapps.securesms.sms.OutgoingTextMessage;
import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.mms.OutgoingMediaMessage;
import com.tingtingapps.securesms.recipients.RecipientFactory;
import com.tingtingapps.securesms.recipients.Recipients;
import com.tingtingapps.securesms.sms.MessageSender;

import ws.com.google.android.mms.pdu.PduBody;

/**
 * Get the response text from the Wearable Device and sends an message as a reply
 */
public class WearReplyReceiver extends MasterSecretBroadcastReceiver {

  public static final String TAG                 = WearReplyReceiver.class.getSimpleName();
  public static final String REPLY_ACTION        = "com.tingtingapps.securesms.notifications.WEAR_REPLY";
  public static final String RECIPIENT_IDS_EXTRA = "recipient_ids";

  @Override
  protected void onReceive(final Context context, Intent intent,
                           final @Nullable MasterSecret masterSecret)
  {
    if (!REPLY_ACTION.equals(intent.getAction())) return;

    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

    if (remoteInput == null) return;

    final long[]       recipientIds = intent.getLongArrayExtra(RECIPIENT_IDS_EXTRA);
    final CharSequence responseText = remoteInput.getCharSequence(MessageNotifier.EXTRA_VOICE_REPLY);
    final Recipients   recipients   = RecipientFactory.getRecipientsForIds(context, recipientIds, false);

    if (masterSecret != null && responseText != null) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          long threadId;

          if (recipients.isGroupRecipient()) {
            OutgoingMediaMessage reply = new OutgoingMediaMessage(context, recipients, new PduBody(), responseText.toString(), 0);
            threadId = MessageSender.send(context, masterSecret, reply, -1, false);
          } else {
            OutgoingTextMessage reply = new OutgoingTextMessage(recipients, responseText.toString());
            threadId = MessageSender.send(context, masterSecret, reply, -1, false);
          }

          DatabaseFactory.getThreadDatabase(context).setRead(threadId);
          MessageNotifier.updateNotification(context, masterSecret);

          return null;
        }
      }.execute();
    }

  }
}
