package com.tingtingapps.securesms.dependencies;

import android.content.Context;

import com.tingtingapps.securesms.crypto.storage.TextSecureAxolotlStore;
import com.tingtingapps.securesms.jobs.CleanPreKeysJob;
import org.whispersystems.libaxolotl.state.SignedPreKeyStore;

import dagger.Module;
import dagger.Provides;

@Module (complete = false, injects = {CleanPreKeysJob.class})
public class AxolotlStorageModule {

  private final Context context;

  public AxolotlStorageModule(Context context) {
    this.context = context;
  }

  @Provides SignedPreKeyStoreFactory provideSignedPreKeyStoreFactory() {
    return new SignedPreKeyStoreFactory() {
      @Override
      public SignedPreKeyStore create() {
        return new TextSecureAxolotlStore(context);
      }
    };
  }

  public static interface SignedPreKeyStoreFactory {
    public SignedPreKeyStore create();
  }
}
