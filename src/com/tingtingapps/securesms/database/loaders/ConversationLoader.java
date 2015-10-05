package com.tingtingapps.securesms.database.loaders;

import android.content.Context;
import android.database.Cursor;

import com.tingtingapps.securesms.database.DatabaseFactory;
import com.tingtingapps.securesms.util.AbstractCursorLoader;

public class ConversationLoader extends AbstractCursorLoader {
  private final long                     threadId;

  public ConversationLoader(Context context, long threadId) {
    super(context);
    this.threadId = threadId;
  }

  @Override
  public Cursor getCursor() {
    return DatabaseFactory.getMmsSmsDatabase(context).getConversation(threadId);
  }
}
