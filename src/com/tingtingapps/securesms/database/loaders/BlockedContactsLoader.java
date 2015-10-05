package com.tingtingapps.securesms.database.loaders;

import android.content.Context;
import android.database.Cursor;

import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.util.AbstractCursorLoader;

public class BlockedContactsLoader extends AbstractCursorLoader {

  public BlockedContactsLoader(Context context) {
    super(context);
  }

  @Override
  public Cursor getCursor() {
    return DatabaseFactory.getRecipientPreferenceDatabase(getContext())
                          .getBlocked();
  }

}
