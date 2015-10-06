/**
 * Copyright (C) 2015 Open Whisper Systems
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
package com.tingtingapps.securesms.preferences;

import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.Matchers;
import com.tingtingapps.securesms.ApplicationPreferencesActivity;
import com.tingtingapps.securesms.ConversationListActivity;
import com.tingtingapps.securesms.LogSubmitActivity;
import com.tingtingapps.securesms.R;
import com.tingtingapps.securesms.RegistrationActivity;
import com.tingtingapps.securesms.contacts.ContactIdentityManager;
import com.tingtingapps.securesms.ApplicationPreferencesActivityActions;
import com.tingtingapps.securesms.ConversationListActivityActions;
import com.tingtingapps.securesms.TextSecureEspressoTestCase;
import com.tingtingapps.securesms.util.TextSecurePreferences;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.tingtingapps.securesms.EspressoUtil.waitOn;

@LargeTest
public class AdvancedPreferenceFragmentTest extends TextSecureEspressoTestCase<ConversationListActivity> {

  public AdvancedPreferenceFragmentTest() {
    super(ConversationListActivity.class);
  }

  private void checkAllPreferencesDisplayed() throws Exception {
    onData(Matchers.<Object>allOf(withKey("pref_toggle_push_messaging")))
          .check(matches(isDisplayed()));
    onData(Matchers.<Object>allOf(withKey("pref_enter_sends")))
          .check(matches(isDisplayed()));
    onData(Matchers.<Object>allOf(withKey("pref_submit_debug_logs")))
          .check(matches(isDisplayed()));

    ContactIdentityManager identity = ContactIdentityManager.getInstance(getContext());
    if (!identity.isSelfIdentityAutoDetected()) {
      onData(Matchers.<Object>allOf(withKey("pref_choose_identity")))
            .check(matches(isDisplayed()));
    }
  }

  private void checkViewsMatchPreferences() throws Exception {
    if (TextSecurePreferences.isPushRegistered(getContext())) {
      isChecked().matches(onData(Matchers.<Object>allOf(withKey("pref_toggle_push_messaging"))));
    } else {
      isNotChecked().matches(onData(Matchers.<Object>allOf(withKey("pref_toggle_push_messaging"))));
    }

    if (TextSecurePreferences.isEnterSendsEnabled(getContext())) {
      isChecked().matches(onData(Matchers.<Object>allOf(withKey("pref_enter_sends"))));
    } else {
      isNotChecked().matches(onData(Matchers.<Object>allOf(withKey("pref_enter_sends"))));
    }
  }

  private void clickAdvancedSettingAndCheckState() throws Exception {
    ConversationListActivityActions.clickSettings(getContext());
    waitOn(ApplicationPreferencesActivity.class);
    ApplicationPreferencesActivityActions.clickAdvancedSetting();

    checkAllPreferencesDisplayed();
    checkViewsMatchPreferences();
  }

  public void testEnableTextSecureMessages() throws Exception {
    loadActivity(ConversationListActivity.class, STATE_REGISTRATION_SKIPPED);
    clickAdvancedSettingAndCheckState();
    AdvancedPreferenceFragmentActions.clickTextSecureMessages();
    waitOn(RegistrationActivity.class);
  }

  public void testDisableTextSecureMessages() throws Exception {
    loadActivity(ConversationListActivity.class, STATE_REGISTERED);
    clickAdvancedSettingAndCheckState();
    AdvancedPreferenceFragmentActions.clickTextSecureMessages();
    onView(withText(R.string.ApplicationPreferencesActivity_disable_textsecure_messages))
          .check(matches(isDisplayed()));
    onView(withText(android.R.string.cancel)).perform(click());
  }

  public void testSubmitDebugLog() throws Exception {
    loadActivity(ConversationListActivity.class, STATE_REGISTRATION_SKIPPED);
    clickAdvancedSettingAndCheckState();
    AdvancedPreferenceFragmentActions.clickSubmitDebugLog();
    waitOn(LogSubmitActivity.class);
  }

}
