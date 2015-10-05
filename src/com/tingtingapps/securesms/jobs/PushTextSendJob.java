package com.tingtingapps.securesms.jobs;

import android.content.Context;
import android.util.Log;

import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.database.NoSuchMessageException;
import com.tingtingapps.securesms.database.SmsDatabase;
import com.tingtingapps.securesms.database.model.SmsMessageRecord;
import com.tingtingapps.securesms.dependencies.InjectableType;
import com.tingtingapps.securesms.dependencies.TextSecureCommunicationModule;
import com.tingtingapps.securesms.notifications.MessageNotifier;
import com.tingtingapps.securesms.recipients.RecipientFactory;
import com.tingtingapps.securesms.recipients.Recipients;
import com.tingtingapps.securesms.transport.InsecureFallbackApprovalException;
import com.tingtingapps.securesms.ApplicationContext;
import com.tingtingapps.securesms.database.EncryptingSmsDatabase;
import com.tingtingapps.securesms.transport.RetryLaterException;
import org.whispersystems.textsecure.api.TextSecureMessageSender;
import org.whispersystems.textsecure.api.crypto.UntrustedIdentityException;
import org.whispersystems.textsecure.api.messages.TextSecureDataMessage;
import org.whispersystems.textsecure.api.push.TextSecureAddress;
import org.whispersystems.textsecure.api.push.exceptions.UnregisteredUserException;
import org.whispersystems.textsecure.api.util.InvalidNumberException;

import java.io.IOException;

import javax.inject.Inject;

public class PushTextSendJob extends PushSendJob implements InjectableType {

  private static final long serialVersionUID = 1L;

  private static final String TAG = PushTextSendJob.class.getSimpleName();

  @Inject transient TextSecureCommunicationModule.TextSecureMessageSenderFactory messageSenderFactory;

  private final long messageId;

  public PushTextSendJob(Context context, long messageId, String destination) {
    super(context, constructParameters(context, destination));
    this.messageId = messageId;
  }

  @Override
  public void onAdded() {
    SmsDatabase smsDatabase = DatabaseFactory.getSmsDatabase(context);
    smsDatabase.markAsSending(messageId);
    smsDatabase.markAsPush(messageId);
  }

  @Override
  public void onSend(MasterSecret masterSecret) throws NoSuchMessageException, RetryLaterException {
    EncryptingSmsDatabase database = DatabaseFactory.getEncryptingSmsDatabase(context);
    SmsMessageRecord record   = database.getMessage(masterSecret, messageId);

    try {
      Log.w(TAG, "Sending message: " + messageId);

      deliver(record);
      database.markAsPush(messageId);
      database.markAsSecure(messageId);
      database.markAsSent(messageId);

    } catch (InsecureFallbackApprovalException e) {
      Log.w(TAG, e);
      database.markAsPendingInsecureSmsFallback(record.getId());
      MessageNotifier.notifyMessageDeliveryFailed(context, record.getRecipients(), record.getThreadId());
      ApplicationContext.getInstance(context).getJobManager().add(new DirectoryRefreshJob(context));
    } catch (UntrustedIdentityException e) {
      Log.w(TAG, e);
      Recipients recipients  = RecipientFactory.getRecipientsFromString(context, e.getE164Number(), false);
      long       recipientId = recipients.getPrimaryRecipient().getRecipientId();

      database.addMismatchedIdentity(record.getId(), recipientId, e.getIdentityKey());
      database.markAsSentFailed(record.getId());
      database.markAsPush(record.getId());
    }
  }

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    if (exception instanceof RetryLaterException) return true;

    return false;
  }

  @Override
  public void onCanceled() {
    DatabaseFactory.getSmsDatabase(context).markAsSentFailed(messageId);

    long       threadId   = DatabaseFactory.getSmsDatabase(context).getThreadIdForMessage(messageId);
    Recipients recipients = DatabaseFactory.getThreadDatabase(context).getRecipientsForThreadId(threadId);

    if (threadId != -1 && recipients != null) {
      MessageNotifier.notifyMessageDeliveryFailed(context, recipients, threadId);
    }
  }

  private void deliver(SmsMessageRecord message)
      throws UntrustedIdentityException, InsecureFallbackApprovalException, RetryLaterException
  {
    try {
      TextSecureAddress       address           = getPushAddress(message.getIndividualRecipient().getNumber());
      TextSecureMessageSender messageSender     = messageSenderFactory.create();
      TextSecureDataMessage   textSecureMessage = TextSecureDataMessage.newBuilder()
                                                                       .withTimestamp(message.getDateSent())
                                                                       .withBody(message.getBody().getBody())
                                                                       .asEndSessionMessage(message.isEndSession())
                                                                       .build();


      messageSender.sendMessage(address, textSecureMessage);
    } catch (InvalidNumberException | UnregisteredUserException e) {
      Log.w(TAG, e);
      throw new InsecureFallbackApprovalException(e);
    } catch (IOException e) {
      Log.w(TAG, e);
      throw new RetryLaterException(e);
    }
  }
}
