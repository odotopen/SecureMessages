package com.tingtingapps.securesms.jobs;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tingtingapps.securesms.database.model.SmsMessageRecord;
import com.tingtingapps.securesms.crypto.AsymmetricMasterCipher;
import com.tingtingapps.securesms.crypto.AsymmetricMasterSecret;
import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.crypto.MasterSecretUnion;
import com.tingtingapps.securesms.crypto.MasterSecretUtil;
import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.database.EncryptingSmsDatabase;
import com.tingtingapps.securesms.database.MmsDatabase;
import com.tingtingapps.securesms.database.SmsDatabase;
import com.tingtingapps.securesms.database.model.MessageRecord;
import com.tingtingapps.securesms.jobs.requirements.MasterSecretRequirement;
import com.tingtingapps.securesms.notifications.MessageNotifier;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.libaxolotl.InvalidMessageException;

import java.io.IOException;

public class MasterSecretDecryptJob extends MasterSecretJob {

  private static final long   serialVersionUID = 1L;
  private static final String TAG              = MasterSecretDecryptJob.class.getSimpleName();

  public MasterSecretDecryptJob(Context context) {
    super(context, JobParameters.newBuilder()
                                .withRequirement(new MasterSecretRequirement(context))
                                .create());
  }

  @Override
  public void onRun(MasterSecret masterSecret) {
    EncryptingSmsDatabase smsDatabase = DatabaseFactory.getEncryptingSmsDatabase(context);
    SmsDatabase.Reader    smsReader   = smsDatabase.getDecryptInProgressMessages(masterSecret);

    SmsMessageRecord smsRecord;

    while ((smsRecord = smsReader.getNext()) != null) {
      try {
        String body = getAsymmetricDecryptedBody(masterSecret, smsRecord.getBody().getBody());
        smsDatabase.updateMessageBody(new MasterSecretUnion(masterSecret), smsRecord.getId(), body);
      } catch (InvalidMessageException e) {
        Log.w(TAG, e);
      }
    }

    MmsDatabase        mmsDatabase = DatabaseFactory.getMmsDatabase(context);
    MmsDatabase.Reader mmsReader   = mmsDatabase.getDecryptInProgressMessages(masterSecret);

    MessageRecord mmsRecord;

    while ((mmsRecord = mmsReader.getNext()) != null) {
      try {
        String body = getAsymmetricDecryptedBody(masterSecret, mmsRecord.getBody().getBody());
        mmsDatabase.updateMessageBody(new MasterSecretUnion(masterSecret), mmsRecord.getId(), body);
      } catch (InvalidMessageException e) {
        Log.w(TAG, e);
      }
    }

    smsReader.close();
    mmsReader.close();

    MessageNotifier.updateNotification(context, masterSecret);
  }

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    return false;
  }

  @Override
  public void onAdded() {

  }

  @Override
  public void onCanceled() {

  }

  private String getAsymmetricDecryptedBody(MasterSecret masterSecret, String body)
      throws InvalidMessageException
  {
    try {
      AsymmetricMasterSecret asymmetricMasterSecret = MasterSecretUtil.getAsymmetricMasterSecret(context, masterSecret);
      AsymmetricMasterCipher asymmetricMasterCipher = new AsymmetricMasterCipher(asymmetricMasterSecret);

      if (TextUtils.isEmpty(body)) return "";
      else                         return asymmetricMasterCipher.decryptBody(body);

    } catch (IOException e) {
      throw new InvalidMessageException(e);
    }
  }


}
