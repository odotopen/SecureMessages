/**
 * Copyright (C) 2014 Open Whisper Systems
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
package com.tingtingapps.securesms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tingtingapps.securesms.components.RatingManager;
import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.notifications.MessageNotifier;
import com.tingtingapps.securesms.recipients.RecipientFactory;
import com.tingtingapps.securesms.recipients.Recipients;
import com.tingtingapps.securesms.service.DirectoryRefreshListener;
import com.tingtingapps.securesms.service.KeyCachingService;
import com.tingtingapps.securesms.util.DynamicLanguage;
import com.tingtingapps.securesms.util.TextSecurePreferences;
import com.tingtingapps.securesms.util.DynamicTheme;

public class ConversationListActivity extends PassphraseRequiredActionBarActivity
    implements ConversationListFragment.ConversationSelectedListener
{
  private static final String TAG = ConversationListActivity.class.getSimpleName();

  private final DynamicTheme    dynamicTheme    = new DynamicTheme   ();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  private ConversationListFragment fragment;
  private ContentObserver observer;
  private MasterSecret masterSecret;

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle icicle, @NonNull MasterSecret masterSecret) {
    this.masterSecret = masterSecret;

    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    getSupportActionBar().setTitle(R.string.app_name);
    fragment = initFragment(android.R.id.content, new ConversationListFragment(), masterSecret, dynamicLanguage.getCurrentLocale());

    initializeContactUpdatesReceiver();

    DirectoryRefreshListener.schedule(this);
    RatingManager.showRatingDialogIfNecessary(this);

  }

  @Override
  public void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
    dynamicLanguage.onResume(this);
  }

  @Override
  public void onDestroy() {
    if (observer != null) getContentResolver().unregisterContentObserver(observer);
    super.onDestroy();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuInflater inflater = this.getMenuInflater();
    menu.clear();

    inflater.inflate(R.menu.text_secure_normal, menu);
    inflater.inflate(R.menu.lock, menu);
    menu.findItem(R.id.menu_clear_passphrase).setVisible(true); //!TextSecurePreferences.isPasswordDisabled(this)

    inflater.inflate(R.menu.conversation_list, menu);
    MenuItem menuItem = menu.findItem(R.id.menu_search);
    initializeSearch(menuItem);

    super.onPrepareOptionsMenu(menu);
    return true;
  }

  private void initializeSearch(MenuItem searchViewItem) {
    SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchViewItem);
    searchView.setQueryHint(getString(R.string.ConversationListActivity_search));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        if (fragment != null) {
          fragment.setQueryFilter(query);
          return true;
        }

        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return onQueryTextSubmit(newText);
      }
    });

    MenuItemCompat.setOnActionExpandListener(searchViewItem, new MenuItemCompat.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return true;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        if (fragment != null) {
          fragment.resetQueryFilter();
        }

        return true;
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId()) {
    case R.id.menu_new_group:         createGroup();                  return true;
    case R.id.menu_settings:          handleDisplaySettings();        return true;
    case R.id.menu_clear_passphrase:  handleClearPassphrase();        return true;
    case R.id.menu_mark_all_read:     handleMarkAllRead();            return true;
    case R.id.menu_import_export:     handleImportExport();           return true;
    case R.id.menu_my_identity:       handleMyIdentity();             return true;
    //nganly
    case R.id.menu_share_app:
      Intent sharingIntent = new Intent(
              android.content.Intent.ACTION_SEND);
      sharingIntent.setType("text/plain");
      String shareBody = getString(R.string.socail_share_body);
      sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
              getString(R.string.socail_share_title));
      sharingIntent
              .putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
      startActivity(Intent.createChooser(sharingIntent, getString(R.string.socail_share_via)));
      return true;
    case R.id.menu_more_app:
      Intent intentMoreApp = new Intent(Intent.ACTION_VIEW);
      intentMoreApp.setData(Uri.parse("market://search?q=pub:" + getString(R.string.publisher_name)));
      startActivity(intentMoreApp);
      return true;
    case R.id.menu_rate_app:
      Context context = getApplicationContext();
      String packageName = context.getPackageName();
      Intent intentRateApp = new Intent(Intent.ACTION_VIEW);
      intentRateApp.setData(Uri.parse("market://details?id="+packageName));
      startActivity(intentRateApp);
      return true;
   /* case R.id.menu_donate_remove_ads:
      Intent intentInAppBilling = new Intent(this, InAppBillingActivity.class);
      startActivity(intentInAppBilling);
      return true;*/
    case R.id.menu_lock:
      //nganly
      boolean passDisable = TextSecurePreferences.isPasswordDisabled(this);
      if(passDisable){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.alert_passphare_not_set));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.alert_passphare_yes),
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    handleDisplaySettings();
                  }
                });
        builder.setNegativeButton(getString(R.string.alert_passphare_cancel),
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                  }
                });

        AlertDialog alert = builder.create();
        alert.show();

      }else{
        Intent intent = new Intent(this, KeyCachingService.class);
        intent.setAction(KeyCachingService.CLEAR_KEY_ACTION);
        startService(intent);
      }
      return true;
    }

    return false;
  }

  @Override
  public void onCreateConversation(long threadId, Recipients recipients, int distributionType) {
    createConversation(threadId, recipients, distributionType);
  }

  private void createGroup() {
    Intent intent = new Intent(this, GroupCreateActivity.class);
    startActivity(intent);
  }

  private void createConversation(long threadId, Recipients recipients, int distributionType) {
    Intent intent = new Intent(this, ConversationActivity.class);
    intent.putExtra(ConversationActivity.RECIPIENTS_EXTRA, recipients.getIds());
    intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, threadId);
    intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, distributionType);

    startActivity(intent);
    overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
  }

  private void handleDisplaySettings() {
    Intent preferencesIntent = new Intent(this, ApplicationPreferencesActivity.class);
    startActivity(preferencesIntent);
  }

  private void handleClearPassphrase() {
    //nganly
    boolean passDisable = TextSecurePreferences.isPasswordDisabled(this);
    if(passDisable){
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(getString(R.string.alert_passphare_not_set));
      builder.setCancelable(true);
      builder.setPositiveButton(getString(R.string.alert_passphare_yes),
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  handleDisplaySettings();
                }
              });
      builder.setNegativeButton(getString(R.string.alert_passphare_cancel),
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
                }
              });

      AlertDialog alert = builder.create();
      alert.show();

    }else{
      Intent intent = new Intent(this, KeyCachingService.class);
      intent.setAction(KeyCachingService.CLEAR_KEY_ACTION);
      startService(intent);
    }

  }

  private void handleImportExport() {
    startActivity(new Intent(this, ImportExportActivity.class));
  }

  private void handleMyIdentity() {
    startActivity(new Intent(this, ViewLocalIdentityActivity.class));
  }

  private void handleMarkAllRead() {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        DatabaseFactory.getThreadDatabase(ConversationListActivity.this).setAllThreadsRead();
        MessageNotifier.updateNotification(ConversationListActivity.this, masterSecret);
        return null;
      }
    }.execute();
  }

  private void initializeContactUpdatesReceiver() {
    observer = new ContentObserver(null) {
      @Override
      public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.w(TAG, "Detected android contact data changed, refreshing cache");
        RecipientFactory.clearCache();
        ConversationListActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            fragment.getListAdapter().notifyDataSetChanged();
          }
        });
      }
    };

    getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI,
                                                 true, observer);
  }
}
