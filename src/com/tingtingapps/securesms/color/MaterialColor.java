package com.tingtingapps.securesms.color;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.TypedValue;

public enum MaterialColor {

  RED        (com.tingtingapps.securesms.R.color.red_400,         com.tingtingapps.securesms.R.color.red_900,         com.tingtingapps.securesms.R.color.red_700,         "red"),
  PINK       (com.tingtingapps.securesms.R.color.pink_400,        com.tingtingapps.securesms.R.color.pink_900,        com.tingtingapps.securesms.R.color.pink_700,        "pink"),
  PURPLE     (com.tingtingapps.securesms.R.color.purple_400,      com.tingtingapps.securesms.R.color.purple_900,      com.tingtingapps.securesms.R.color.purple_700,      "purple"),
  DEEP_PURPLE(com.tingtingapps.securesms.R.color.deep_purple_400, com.tingtingapps.securesms.R.color.deep_purple_900, com.tingtingapps.securesms.R.color.deep_purple_700, "deep_purple"),
  INDIGO     (com.tingtingapps.securesms.R.color.indigo_400,      com.tingtingapps.securesms.R.color.indigo_900,      com.tingtingapps.securesms.R.color.indigo_700,      "indigo"),
  BLUE       (com.tingtingapps.securesms.R.color.blue_500,        com.tingtingapps.securesms.R.color.blue_900,        com.tingtingapps.securesms.R.color.blue_700,        "blue"),
  LIGHT_BLUE (com.tingtingapps.securesms.R.color.light_blue_500,  com.tingtingapps.securesms.R.color.light_blue_900,  com.tingtingapps.securesms.R.color.light_blue_700,  "light_blue"),
  CYAN       (com.tingtingapps.securesms.R.color.cyan_500,        com.tingtingapps.securesms.R.color.cyan_900,        com.tingtingapps.securesms.R.color.cyan_700,        "cyan"),
  TEAL       (com.tingtingapps.securesms.R.color.teal_500,        com.tingtingapps.securesms.R.color.teal_900,        com.tingtingapps.securesms.R.color.teal_700,        "teal"),
  GREEN      (com.tingtingapps.securesms.R.color.green_500,       com.tingtingapps.securesms.R.color.green_900,       com.tingtingapps.securesms.R.color.green_700,       "green"),
  LIGHT_GREEN(com.tingtingapps.securesms.R.color.light_green_600, com.tingtingapps.securesms.R.color.light_green_900, com.tingtingapps.securesms.R.color.light_green_700, "light_green"),
  LIME       (com.tingtingapps.securesms.R.color.lime_500,        com.tingtingapps.securesms.R.color.lime_900,        com.tingtingapps.securesms.R.color.lime_700,        "lime"),
  YELLOW     (com.tingtingapps.securesms.R.color.yellow_500,      com.tingtingapps.securesms.R.color.yellow_900,      com.tingtingapps.securesms.R.color.yellow_700,      "yellow"),
  AMBER      (com.tingtingapps.securesms.R.color.amber_600,       com.tingtingapps.securesms.R.color.amber_900,       com.tingtingapps.securesms.R.color.amber_700,       "amber"),
  ORANGE     (com.tingtingapps.securesms.R.color.orange_500,      com.tingtingapps.securesms.R.color.orange_900,      com.tingtingapps.securesms.R.color.orange_700,      "orange"),
  DEEP_ORANGE(com.tingtingapps.securesms.R.color.deep_orange_500, com.tingtingapps.securesms.R.color.deep_orange_900, com.tingtingapps.securesms.R.color.deep_orange_700, "deep_orange"),
  BROWN      (com.tingtingapps.securesms.R.color.brown_500,       com.tingtingapps.securesms.R.color.brown_900,       com.tingtingapps.securesms.R.color.brown_700,       "brown"),
  GREY       (com.tingtingapps.securesms.R.color.grey_500,        com.tingtingapps.securesms.R.color.grey_900,        com.tingtingapps.securesms.R.color.grey_700,        "grey"),
  BLUE_GREY  (com.tingtingapps.securesms.R.color.blue_grey_500,   com.tingtingapps.securesms.R.color.blue_grey_900,   com.tingtingapps.securesms.R.color.blue_grey_700,   "blue_grey"),

  GROUP      (GREY.conversationColorLight, com.tingtingapps.securesms.R.color.textsecure_primary, com.tingtingapps.securesms.R.color.textsecure_primary_dark,
              GREY.conversationColorDark, com.tingtingapps.securesms.R.color.gray95, com.tingtingapps.securesms.R.color.black,
              "group_color");

  private final int conversationColorLight;
  private final int actionBarColorLight;
  private final int statusBarColorLight;
  private final int conversationColorDark;
  private final int actionBarColorDark;
  private final int statusBarColorDark;
  private final String serialized;

  MaterialColor(int conversationColorLight, int actionBarColorLight,
                int statusBarColorLight, int conversationColorDark,
                int actionBarColorDark, int statusBarColorDark,
                String serialized)
  {
    this.conversationColorLight = conversationColorLight;
    this.actionBarColorLight    = actionBarColorLight;
    this.statusBarColorLight    = statusBarColorLight;
    this.conversationColorDark  = conversationColorDark;
    this.actionBarColorDark     = actionBarColorDark;
    this.statusBarColorDark     = statusBarColorDark;
    this.serialized             = serialized;
  }

  MaterialColor(int lightColor, int darkColor, int statusBarColor, String serialized) {
    this(lightColor, lightColor, statusBarColor, darkColor, darkColor, statusBarColor, serialized);
  }

  public int toConversationColor(@NonNull Context context) {
    if (getAttribute(context, com.tingtingapps.securesms.R.attr.theme_type, "light").equals("dark")) {
      return context.getResources().getColor(conversationColorDark);
    } else {
      return context.getResources().getColor(conversationColorLight);
    }
  }

  public int toActionBarColor(@NonNull Context context) {
    if (getAttribute(context, com.tingtingapps.securesms.R.attr.theme_type, "light").equals("dark")) {
      return context.getResources().getColor(actionBarColorDark);
    } else {
      return context.getResources().getColor(actionBarColorLight);
    }
  }

  public int toStatusBarColor(@NonNull Context context) {
    if (getAttribute(context, com.tingtingapps.securesms.R.attr.theme_type, "light").equals("dark")) {
      return context.getResources().getColor(statusBarColorDark);
    } else {
      return context.getResources().getColor(statusBarColorLight);
    }
  }

  public boolean represents(Context context, int colorValue) {
    return context.getResources().getColor(conversationColorDark)  == colorValue ||
           context.getResources().getColor(conversationColorLight) == colorValue ||
           context.getResources().getColor(actionBarColorDark) == colorValue ||
           context.getResources().getColor(actionBarColorLight) == colorValue ||
           context.getResources().getColor(statusBarColorLight) == colorValue ||
           context.getResources().getColor(statusBarColorDark) == colorValue;
  }

  public String serialize() {
    return serialized;
  }

  private String getAttribute(Context context, int attribute, String defaultValue) {
    TypedValue outValue = new TypedValue();

    if (context.getTheme().resolveAttribute(attribute, outValue, true)) {
      return outValue.coerceToString().toString();
    } else {
      return defaultValue;
    }
  }


  public static MaterialColor fromSerialized(String serialized) throws UnknownColorException {
    for (MaterialColor color : MaterialColor.values()) {
      if (color.serialized.equals(serialized)) return color;
    }

    throw new UnknownColorException("Unknown color: " + serialized);
  }

  public static class UnknownColorException extends Exception {
    public UnknownColorException(String message) {
      super(message);
    }
  }

}
