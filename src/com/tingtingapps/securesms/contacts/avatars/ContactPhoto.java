package com.tingtingapps.securesms.contacts.avatars;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface ContactPhoto {

  public Drawable asDrawable(Context context, int color);
  public Drawable asDrawable(Context context, int color, boolean inverted);


}
