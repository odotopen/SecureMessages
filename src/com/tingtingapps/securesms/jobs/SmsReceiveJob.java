package com.tingtingapps.securesms.jobs;

import android.content.Context;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Pair;

import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.crypto.MasterSecretUnion;
import com.tingtingapps.securesms.crypto.MasterSecretUtil;
import com.tingtingapps.securesms.notifications.MessageNotifier;
import com.tingtingapps.securesms.recipients.RecipientFactory;
import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.database.EncryptingSmsDatabase;
import com.tingtingapps.securesms.protocol.WirePrefix;
import com.tingtingapps.securesms.recipients.Recipients;
import com.tingtingapps.securesms.service.KeyCachingService;
import com.tingtingapps.securesms.sms.IncomingEncryptedMessage;
import com.tingtingapps.securesms.sms.IncomingTextMessage;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.libaxolotl.util.guava.Optional;

import java.util.LinkedList;
import java.util.List;

public class SmsReceiveJob extends ContextJob {

  private static final String TAG = SmsReceiveJob.class.getSimpleName();

  private final Object[] pdus;

  public SmsReceiveJob(Context context, Object[] pdus) {
    super(context, JobParameters.newBuilder()
                                .withPersistence()
                                .withWakeLock(true)
                                .create());

    this.pdus = pdus;
  }

  @Override
  public void onAdded() {}

  @Override
  public void onRun() {
    Optional<IncomingTextMessage> message      = assembleMessageFragments(pdus);
    MasterSecret masterSecret = KeyCachingService.getMasterSecret(context);

    MasterSecretUnion masterSecretUnion;

    if (masterSecret == null) {
      masterSecretUnion = new MasterSecretUnion(MasterSecretUtil.getAsymmetricMasterSecret(context, null));
    } else {
      masterSecretUnion = new MasterSecretUnion(masterSecret);
    }

    if (message.isPresent() && !isBlocked(message.get())) {
      Pair<Long, Long> messageAndThreadId = storeMessage(masterSecretUnion, message.get());
      MessageNotifier.updateNotification(context, masterSecret, messageAndThreadId.second);
    } else if (message.isPresent()) {
      Log.w(TAG, "*** Received blocked SMS, ignoring...");
    }
  }

  @Override
  public void onCanceled() {

  }

  @Override
  public boolean onShouldRetry(Exception exception) {
    return false;
  }

  private boolean isBlocked(IncomingTextMessage message) {
    if (message.getSender() != null) {
      Recipients recipients = RecipientFactory.getRecipientsFromString(context, message.getSender(), false);
      return recipients.isBlocked();
    }

    return false;
  }

  private Pair<Long, Long> storeMessage(MasterSecretUnion masterSecret, IncomingTextMessage message) {
    EncryptingSmsDatabase database = DatabaseFactory.getEncryptingSmsDatabase(context);

    Pair<Long, Long> messageAndThreadId;

    if (message.isSecureMessage()) {
      IncomingTextMessage placeholder = new IncomingTextMessage(message, "");
      messageAndThreadId = database.insertMessageInbox(placeholder);
      database.markAsLegacyVersion(messageAndThreadId.first);
    } else {
      messageAndThreadId = database.insertMessageInbox(masterSecret, message);
    }

    return messageAndThreadId;
  }

  private Optional<IncomingTextMessage> assembleMessageFragments(Object[] pdus) {
    List<IncomingTextMessage> messages = new LinkedList<>();

    for (Object pdu : pdus) {
      messages.add(new IncomingTextMessage(SmsMessage.createFromPdu((byte[])pdu)));
    }

    if (messages.isEmpty()) {
      return Optional.absent();
    }

    IncomingTextMessage message =  new IncomingTextMessage(messages);

    if (WirePrefix.isEncryptedMessage(message.getMessageBody()) ||
        WirePrefix.isKeyExchange(message.getMessageBody())      ||
        WirePrefix.isPreKeyBundle(message.getMessageBody())     ||
        WirePrefix.isEndSession(message.getMessageBody()))
    {
      return Optional.<IncomingTextMessage>of(new IncomingEncryptedMessage(message, message.getMessageBody()));
    } else {
      return Optional.of(message);
    }
  }
}
