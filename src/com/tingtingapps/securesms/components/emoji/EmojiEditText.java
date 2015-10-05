package com.tingtingapps.securesms.components.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.util.AttributeSet;


public class EmojiEditText extends AppCompatEditText {
  private static final String TAG = EmojiEditText.class.getSimpleName();

  public EmojiEditText(Context context) {
    this(context, null);
  }

  public EmojiEditText(Context context, AttributeSet attrs) {
    this(context, attrs, com.tingtingapps.securesms.R.attr.editTextStyle);
  }

  public EmojiEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setFilters(new InputFilter[]{ new EmojiFilter(this) });
  }

  public void insertEmoji(String emoji) {
    final int          start = getSelectionStart();
    final int          end   = getSelectionEnd();

    getText().replace(Math.min(start, end), Math.max(start, end), emoji);
    setSelection(end + emoji.length());
  }

  @Override public void invalidateDrawable(@NonNull Drawable drawable) {
    if (drawable instanceof EmojiProvider.EmojiDrawable) invalidate();
    else                                   super.invalidateDrawable(drawable);
  }
}
