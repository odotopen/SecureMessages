package com.tingtingapps.securesms.dependencies;

import android.content.Context;

import com.tingtingapps.securesms.jobs.DeliveryReceiptJob;
import com.tingtingapps.securesms.jobs.PushMediaSendJob;
import com.tingtingapps.securesms.BuildConfig;
import com.tingtingapps.securesms.DeviceListActivity;
import com.tingtingapps.securesms.crypto.storage.TextSecureAxolotlStore;
import com.tingtingapps.securesms.jobs.AttachmentDownloadJob;
import com.tingtingapps.securesms.jobs.CleanPreKeysJob;
import com.tingtingapps.securesms.jobs.CreateSignedPreKeyJob;
import com.tingtingapps.securesms.jobs.MultiDeviceContactUpdateJob;
import com.tingtingapps.securesms.jobs.MultiDeviceGroupUpdateJob;
import com.tingtingapps.securesms.jobs.PushGroupSendJob;
import com.tingtingapps.securesms.jobs.PushNotificationReceiveJob;
import com.tingtingapps.securesms.jobs.PushTextSendJob;
import com.tingtingapps.securesms.jobs.RefreshPreKeysJob;
import com.tingtingapps.securesms.push.SecurityEventListener;
import com.tingtingapps.securesms.push.TextSecurePushTrustStore;
import com.tingtingapps.securesms.service.MessageRetrievalService;
import com.tingtingapps.securesms.util.TextSecurePreferences;
import org.whispersystems.libaxolotl.util.guava.Optional;
import org.whispersystems.textsecure.api.TextSecureAccountManager;
import org.whispersystems.textsecure.api.TextSecureMessageReceiver;
import org.whispersystems.textsecure.api.TextSecureMessageSender;
import org.whispersystems.textsecure.api.util.CredentialsProvider;

import dagger.Module;
import dagger.Provides;

@Module(complete = false, injects = {CleanPreKeysJob.class,
                                     CreateSignedPreKeyJob.class,
                                     DeliveryReceiptJob.class,
                                     PushGroupSendJob.class,
                                     PushTextSendJob.class,
                                     PushMediaSendJob.class,
                                     AttachmentDownloadJob.class,
                                     RefreshPreKeysJob.class,
                                     MessageRetrievalService.class,
                                     PushNotificationReceiveJob.class,
                                     MultiDeviceContactUpdateJob.class,
                                     MultiDeviceGroupUpdateJob.class,
                                     DeviceListActivity.DeviceListFragment.class})
public class TextSecureCommunicationModule {

  private final Context context;

  public TextSecureCommunicationModule(Context context) {
    this.context = context;
  }

  @Provides TextSecureAccountManager provideTextSecureAccountManager() {
    return new TextSecureAccountManager(BuildConfig.PUSH_URL,
                                        new TextSecurePushTrustStore(context),
                                        TextSecurePreferences.getLocalNumber(context),
                                        TextSecurePreferences.getPushServerPassword(context));
  }

  @Provides TextSecureMessageSenderFactory provideTextSecureMessageSenderFactory() {
    return new TextSecureMessageSenderFactory() {
      @Override
      public TextSecureMessageSender create() {
        return new TextSecureMessageSender(BuildConfig.PUSH_URL,
                                           new TextSecurePushTrustStore(context),
                                           TextSecurePreferences.getLocalNumber(context),
                                           TextSecurePreferences.getPushServerPassword(context),
                                           new TextSecureAxolotlStore(context),
                                           Optional.of((TextSecureMessageSender.EventListener)
                                                           new SecurityEventListener(context)));
      }
    };
  }

  @Provides TextSecureMessageReceiver provideTextSecureMessageReceiver() {
    return new TextSecureMessageReceiver(BuildConfig.PUSH_URL,
                                         new TextSecurePushTrustStore(context),
                                         new DynamicCredentialsProvider(context));
  }

  public static interface TextSecureMessageSenderFactory {
    public TextSecureMessageSender create();
  }

  private static class DynamicCredentialsProvider implements CredentialsProvider {

    private final Context context;

    private DynamicCredentialsProvider(Context context) {
      this.context = context.getApplicationContext();
    }

    @Override
    public String getUser() {
      return TextSecurePreferences.getLocalNumber(context);
    }

    @Override
    public String getPassword() {
      return TextSecurePreferences.getPushServerPassword(context);
    }

    @Override
    public String getSignalingKey() {
      return TextSecurePreferences.getSignalingKey(context);
    }
  }

}
