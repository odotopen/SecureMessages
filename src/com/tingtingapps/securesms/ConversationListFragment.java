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
package com.tingtingapps.securesms;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.RecyclerListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.AlertDialogWrapper;
/*import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;*/
import com.melnykov.fab.FloatingActionButton;
import com.tingtingapps.securesms.components.DefaultSmsReminder;
import com.tingtingapps.securesms.components.ExpiredBuildReminder;
import com.tingtingapps.securesms.components.PushRegistrationReminder;
import com.tingtingapps.securesms.components.Reminder;
import com.tingtingapps.securesms.components.ReminderView;
import com.tingtingapps.securesms.components.SystemSmsImportReminder;
import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.notifications.MessageNotifier;
import com.tingtingapps.securesms.recipients.Recipients;

import com.tingtingapps.securesms.database.loaders.ConversationListLoader;

import org.whispersystems.libaxolotl.util.guava.Optional;

import java.util.Calendar;
import java.util.Locale;
import java.util.Set;


public class ConversationListFragment extends Fragment
  implements LoaderManager.LoaderCallbacks<Cursor>, ActionMode.Callback, ConversationListAdapter.ItemClickListener
{
  private MasterSecret masterSecret;
  private ActionMode           actionMode;
  private RecyclerView         list;
  private ReminderView reminderView;
  private FloatingActionButton fab;
  private Locale               locale;
  private String               queryFilter  = "";

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    masterSecret = getArguments().getParcelable("master_secret");
    locale       = (Locale) getArguments().getSerializable(PassphraseRequiredActionBarActivity.LOCALE_EXTRA);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
    final View view = inflater.inflate(R.layout.conversation_list_fragment, container, false);
    reminderView = (ReminderView) view.findViewById(R.id.reminder);
    list         = (RecyclerView) view.findViewById(R.id.list);
    fab          = (FloatingActionButton) view.findViewById(R.id.fab);
    list.setHasFixedSize(true);
    list.setLayoutManager(new LinearLayoutManager(getActivity()));

    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    SharedPreferences sp1 = getActivity().getSharedPreferences("myPrefs", Activity.MODE_PRIVATE);
    int dayShowPopup = sp1.getInt("dayShowPopup", 0);
    /*
    //test data
    day = 7;
    dayShowPopup = 15;
    dayOfMonth = 26;*/

    if(day == 7 && dayOfMonth != dayShowPopup){

      SharedPreferences sp2 = getActivity().getSharedPreferences("myPrefs", Activity.MODE_PRIVATE);
      SharedPreferences.Editor editor = sp2.edit();
      editor.putInt("dayShowPopup", dayOfMonth);
      editor.commit();

      final Dialog dialogRating = new Dialog(getActivity());
      dialogRating.requestWindowFeature(Window.FEATURE_NO_TITLE);
      dialogRating.setContentView(R.layout.dlg_rating_sharing_donating);
      int height = 850;
      dialogRating.getWindow().setLayout(LinearLayout.LayoutParams.FILL_PARENT,height);
      dialogRating.setCancelable(true);

      dialogRating.show();
      Button btnRate = (Button) dialogRating.findViewById(R.id.btn_rate);
      btnRate.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Context context = getActivity().getApplicationContext();
          String packageName = context.getPackageName();
          Intent intentRateApp = new Intent(Intent.ACTION_VIEW);
          intentRateApp.setData(Uri.parse("market://details?id=" + packageName));
          startActivity(intentRateApp);
        }
      });

      /*Button btnRemoveAds = (Button) dialogRating.findViewById(R.id.btn_remove_ads);
      btnRemoveAds.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intentInAppBilling = new Intent(getActivity(), InAppBillingActivity.class);
          startActivity(intentInAppBilling);
        }
      });*/

      Button btnShare = (Button) dialogRating.findViewById(R.id.btn_share);
      btnShare.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent sharingIntent = new Intent(
                  android.content.Intent.ACTION_SEND);
          sharingIntent.setType("text/plain");
          String shareBody = getString(R.string.socail_share_body);
          sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                  getString(R.string.socail_share_title));
          sharingIntent
                  .putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
          startActivity(Intent.createChooser(sharingIntent, getString(R.string.socail_share_via)));
        }
      });


      Button btnClose = (Button) dialogRating.findViewById(R.id.btn_close);
      btnClose.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dialogRating.dismiss();
        }
      });
    }

    return view;
  }

  @Override
  public void onActivityCreated(Bundle bundle) {
    super.onActivityCreated(bundle);

    setHasOptionsMenu(true);
    fab.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), NewConversationActivity.class));
      }
    });
    initializeListAdapter();
  }

  @Override
  public void onResume() {
    super.onResume();

    initializeReminders();
    list.getAdapter().notifyDataSetChanged();
  }

  public ConversationListAdapter getListAdapter() {
    return (ConversationListAdapter) list.getAdapter();
  }

  public void setQueryFilter(String query) {
    this.queryFilter = query;
    getLoaderManager().restartLoader(0, null, this);
  }

  public void resetQueryFilter() {
    if (!TextUtils.isEmpty(this.queryFilter)) {
      setQueryFilter("");
    }
  }

  private void initializeReminders() {
    reminderView.hide();
    new AsyncTask<Context, Void, Optional<? extends Reminder>>() {
      @Override protected Optional<? extends Reminder> doInBackground(Context... params) {
        final Context context = params[0];
        if (ExpiredBuildReminder.isEligible(context)) {
          return Optional.of(new ExpiredBuildReminder());
        } else if (DefaultSmsReminder.isEligible(context)) {
          return Optional.of(new DefaultSmsReminder(context));
        } else if (SystemSmsImportReminder.isEligible(context)) {
          return Optional.of((new SystemSmsImportReminder(context, masterSecret)));
        } else if (PushRegistrationReminder.isEligible(context)) {
          return Optional.of((new PushRegistrationReminder(context, masterSecret)));
        } else {
          return Optional.absent();
        }
      }

      @Override protected void onPostExecute(Optional<? extends Reminder> reminder) {
        if (reminder.isPresent() && getActivity() != null && !isRemoving()) {
          reminderView.showReminder(reminder.get());
        }
      }
    }.execute(getActivity());
  }

  private void initializeListAdapter() {
    list.setAdapter(new ConversationListAdapter(getActivity(), masterSecret, locale, null, this));
    list.setRecyclerListener(new RecyclerListener() {
      @Override
      public void onViewRecycled(ViewHolder holder) {
        ((ConversationListItem)holder.itemView).unbind();
      }
    });
    getLoaderManager().restartLoader(0, null, this);
  }

  private void handleDeleteAllSelected() {
    AlertDialogWrapper.Builder alert = new AlertDialogWrapper.Builder(getActivity());
    alert.setIconAttribute(R.attr.dialog_alert_icon);
    alert.setTitle(R.string.ConversationListFragment_delete_threads_question);
    alert.setMessage(R.string.ConversationListFragment_are_you_sure_you_wish_to_delete_all_selected_conversation_threads);
    alert.setCancelable(true);

    alert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        final Set<Long> selectedConversations = (getListAdapter())
            .getBatchSelections();

        if (!selectedConversations.isEmpty()) {
          new AsyncTask<Void, Void, Void>() {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
              dialog = ProgressDialog.show(getActivity(),
                                           getActivity().getString(R.string.ConversationListFragment_deleting),
                                           getActivity().getString(R.string.ConversationListFragment_deleting_selected_threads),
                                           true, false);
            }

            @Override
            protected Void doInBackground(Void... params) {
              DatabaseFactory.getThreadDatabase(getActivity()).deleteConversations(selectedConversations);
              MessageNotifier.updateNotification(getActivity(), masterSecret);
              return null;
            }

            @Override
            protected void onPostExecute(Void result) {
              dialog.dismiss();
              if (actionMode != null) {
                actionMode.finish();
                actionMode = null;
              }
            }
          }.execute();
        }
      }
    });

    alert.setNegativeButton(android.R.string.cancel, null);
    alert.show();
  }

  private void handleSelectAllThreads() {
    getListAdapter().selectAllThreads();
    actionMode.setSubtitle(getString(R.string.conversation_fragment_cab__batch_selection_amount,
                           ((ConversationListAdapter)this.getListAdapter()).getBatchSelections().size()));
  }

  private void handleCreateConversation(long threadId, Recipients recipients, int distributionType) {
    ((ConversationSelectedListener)getActivity()).onCreateConversation(threadId, recipients, distributionType);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
    return new ConversationListLoader(getActivity(), queryFilter);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
    getListAdapter().changeCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    getListAdapter().changeCursor(null);
  }

  @Override
  public void onItemClick(ConversationListItem item) {
    if (actionMode == null) {
      handleCreateConversation(item.getThreadId(), item.getRecipients(),
                               item.getDistributionType());
    } else {
      ConversationListAdapter adapter = (ConversationListAdapter)list.getAdapter();
      adapter.toggleThreadInBatchSet(item.getThreadId());

      if (adapter.getBatchSelections().size() == 0) {
        actionMode.finish();
      } else {
        actionMode.setSubtitle(getString(R.string.conversation_fragment_cab__batch_selection_amount,
                                         adapter.getBatchSelections().size()));
      }

      adapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onItemLongClick(ConversationListItem item) {
    actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(ConversationListFragment.this);

    getListAdapter().initializeBatchMode(true);
    getListAdapter().toggleThreadInBatchSet(item.getThreadId());
    getListAdapter().notifyDataSetChanged();
  }

  public interface ConversationSelectedListener {
    void onCreateConversation(long threadId, Recipients recipients, int distributionType);
}

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuInflater inflater = getActivity().getMenuInflater();
    inflater.inflate(R.menu.conversation_list_batch, menu);

    mode.setTitle(R.string.conversation_fragment_cab__batch_selection_mode);
    mode.setSubtitle(null);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getActivity().getWindow()
        .setStatusBarColor(getResources().getColor(R.color.action_mode_status_bar));
    }

    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_select_all:      handleSelectAllThreads(); return true;
    case R.id.menu_delete_selected: handleDeleteAllSelected(); return true;
    }

    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode mode) {
    getListAdapter().initializeBatchMode(false);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      TypedArray color = getActivity().getTheme()
        .obtainStyledAttributes(new int[] { android.R.attr.statusBarColor });
      getActivity().getWindow().setStatusBarColor(color.getColor(0, Color.BLACK));
      color.recycle();
    }

    actionMode = null;
  }

}


