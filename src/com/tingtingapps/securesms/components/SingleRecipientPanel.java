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
package com.tingtingapps.securesms.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.tingtingapps.securesms.contacts.RecipientsEditor;
import com.tingtingapps.securesms.recipients.Recipient;
import com.tingtingapps.securesms.recipients.RecipientFactory;
import com.tingtingapps.securesms.recipients.RecipientFormattingException;
import com.tingtingapps.securesms.contacts.ContactAccessor;
import com.tingtingapps.securesms.contacts.RecipientsAdapter;
import com.tingtingapps.securesms.recipients.Recipients;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Panel component combining both an editable field with a button for
 * a list-based contact selector.
 *
 * @author Moxie Marlinspike
 */
public class SingleRecipientPanel extends RelativeLayout {
  private final String                         TAG = SingleRecipientPanel.class.getSimpleName();
  private       RecipientsPanelChangedListener panelChangeListener;

  private RecipientsEditor recipientsText;
  private View             panel;

  private static final int RECIPIENTS_MAX_LENGTH = 312;

  public SingleRecipientPanel(Context context) {
    super(context);
    initialize();
  }

  public SingleRecipientPanel(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public SingleRecipientPanel(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initialize();
  }

  public void addRecipient(String name, String number) {
    Log.i(TAG, "addRecipient for " + name + "/" + number);
    if (name != null) recipientsText.append(name + "< " + number + ">, ");
    else recipientsText.append(number + ", ");
  }

  public void addRecipients(Recipients recipients) {
    List<Recipient> recipientList = recipients.getRecipientsList();
    Iterator<Recipient> iterator = recipientList.iterator();

    while (iterator.hasNext()) {
      Recipient recipient = iterator.next();
      addRecipient(recipient.getName(), recipient.getNumber());
    }
  }

  public void addContacts(List<ContactAccessor.ContactData> contacts) {
    for (ContactAccessor.ContactData contact : contacts) {
      for (ContactAccessor.NumberData number : contact.numbers) {
        addRecipient(contact.name, number.number);
      }
    }
  }

  public Recipients getRecipients() throws RecipientFormattingException {
    String rawText = recipientsText.getText().toString();
    Recipients recipients = RecipientFactory.getRecipientsFromString(getContext(), rawText, false);

    if (recipients.isEmpty())
      throw new RecipientFormattingException("Recipient List Is Empty!");

    return recipients;
  }

  public void disable() {
    clear();
    panel.setVisibility(View.GONE);
  }

  public void clear() {
    recipientsText.setText("");
  }

  public void setPanelChangeListener(RecipientsPanelChangedListener panelChangeListener) {
    this.panelChangeListener = panelChangeListener;
  }

  private void initialize() {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(com.tingtingapps.securesms.R.layout.single_recipient_panel, this, true);

    panel = findViewById(com.tingtingapps.securesms.R.id.recipients_panel);
    initRecipientsEditor();
  }

  private void initRecipientsEditor() {
    Recipients recipients;
    recipientsText = (RecipientsEditor)findViewById(com.tingtingapps.securesms.R.id.recipients_text);

    try {
      recipients = getRecipients();
    } catch (RecipientFormattingException e) {
      recipients = RecipientFactory.getRecipientsFor(getContext(), new LinkedList<Recipient>(), true);
    }

    recipientsText.setAdapter(new RecipientsAdapter(this.getContext()));
    recipientsText.populate(recipients);

    recipientsText.setOnFocusChangeListener(new FocusChangedListener());
    recipientsText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (panelChangeListener != null) {
          try {
            panelChangeListener.onRecipientsPanelUpdate(getRecipients());
          } catch (RecipientFormattingException rfe) {
            panelChangeListener.onRecipientsPanelUpdate(null);
          }
        }
        recipientsText.setText("");
      }
    });
  }

  private class FocusChangedListener implements OnFocusChangeListener {
    public void onFocusChange(View v, boolean hasFocus) {
      if (!hasFocus && (panelChangeListener != null)) {
        try {
          panelChangeListener.onRecipientsPanelUpdate(getRecipients());
        } catch (RecipientFormattingException rfe) {
          panelChangeListener.onRecipientsPanelUpdate(null);
        }
      }
    }
  }

  public interface RecipientsPanelChangedListener {
    public void onRecipientsPanelUpdate(Recipients recipients);
  }

}