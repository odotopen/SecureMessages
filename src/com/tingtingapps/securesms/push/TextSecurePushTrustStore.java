package com.tingtingapps.securesms.push;

import android.content.Context;

import com.tingtingapps.securesms.R;
import org.whispersystems.textsecure.api.push.TrustStore;

import java.io.InputStream;

public class TextSecurePushTrustStore implements TrustStore {

  private final Context context;

  public TextSecurePushTrustStore(Context context) {
    this.context = context.getApplicationContext();
  }

  @Override
  public InputStream getKeyStoreInputStream() {
    return context.getResources().openRawResource(R.raw.whisper);
  }

  @Override
  public String getKeyStorePassword() {
    return "whisper";
  }
}
