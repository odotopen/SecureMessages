package com.tingtingapps.securesms.jobs;

import android.content.Context;
import android.util.Log;

import com.tingtingapps.securesms.crypto.IdentityKeyUtil;
import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.crypto.PreKeyUtil;
import com.tingtingapps.securesms.dependencies.InjectableType;
import com.tingtingapps.securesms.jobs.requirements.MasterSecretRequirement;
import com.tingtingapps.securesms.util.TextSecurePreferences;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.jobqueue.requirements.NetworkRequirement;
import org.whispersystems.libaxolotl.IdentityKeyPair;
import org.whispersystems.libaxolotl.state.SignedPreKeyRecord;
import org.whispersystems.textsecure.api.TextSecureAccountManager;
import org.whispersystems.textsecure.api.push.exceptions.PushNetworkException;

import java.io.IOException;

import javax.inject.Inject;

public class CreateSignedPreKeyJob extends MasterSecretJob implements InjectableType {

  private static final long serialVersionUID = 1L;

  private static final String TAG = CreateSignedPreKeyJob.class.getSimpleName();

  @Inject transient TextSecureAccountManager accountManager;

  public CreateSignedPreKeyJob(Context context) {
    super(context, JobParameters.newBuilder()
                                .withPersistence()
                                .withRequirement(new NetworkRequirement(context))
                                .withRequirement(new MasterSecretRequirement(context))
                                .withGroupId(CreateSignedPreKeyJob.class.getSimpleName())
                                .create());
  }

  @Override
  public void onAdded() {}

  @Override
  public void onRun(MasterSecret masterSecret) throws IOException {
    if (TextSecurePreferences.isSignedPreKeyRegistered(context)) {
      Log.w(TAG, "Signed prekey already registered...");
      return;
    }

    IdentityKeyPair    identityKeyPair    = IdentityKeyUtil.getIdentityKeyPair(context);
    SignedPreKeyRecord signedPreKeyRecord = PreKeyUtil.generateSignedPreKey(context, identityKeyPair);

    accountManager.setSignedPreKey(signedPreKeyRecord);
    TextSecurePreferences.setSignedPreKeyRegistered(context, true);
  }

  @Override
  public void onCanceled() {}

  @Override
  public boolean onShouldRetryThrowable(Exception exception) {
    if (exception instanceof PushNetworkException) return true;
    return false;
  }
}
