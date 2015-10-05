package com.tingtingapps.securesms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.tingtingapps.securesms.R;
import com.tingtingapps.securesms.crypto.MasterSecret;
import com.tingtingapps.securesms.mms.AudioSlide;
import com.tingtingapps.securesms.mms.GifSlide;
import com.tingtingapps.securesms.mms.ImageSlide;
import com.tingtingapps.securesms.mms.PartAuthority;
import com.tingtingapps.securesms.mms.Slide;
import com.tingtingapps.securesms.mms.VideoSlide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ws.com.google.android.mms.ContentType;
import ws.com.google.android.mms.pdu.PduPart;

public class MediaUtil {
  private static final String TAG = MediaUtil.class.getSimpleName();

  public static ThumbnailData generateThumbnail(Context context, MasterSecret masterSecret, Uri uri, String type)
      throws IOException, BitmapDecodingException, OutOfMemoryError
  {
    long   startMillis = System.currentTimeMillis();
    ThumbnailData data;
    if      (ContentType.isImageType(type)) data = new ThumbnailData(generateImageThumbnail(context, masterSecret, uri));
    else                                    data = null;

    if (data != null) {
      Log.w(TAG, String.format("generated thumbnail for part, %dx%d (%.3f:1) in %dms",
                               data.getBitmap().getWidth(), data.getBitmap().getHeight(),
                               data.getAspectRatio(), System.currentTimeMillis() - startMillis));
    }

    return data;
  }

  public static byte[] getPartData(Context context, MasterSecret masterSecret, PduPart part)
      throws IOException
  {
    ByteArrayOutputStream os = part.getDataSize() > 0 && part.getDataSize() < Integer.MAX_VALUE
        ? new ByteArrayOutputStream((int) part.getDataSize())
        : new ByteArrayOutputStream();
    Util.copy(PartAuthority.getPartStream(context, masterSecret, part.getDataUri()), os);
    return os.toByteArray();
  }

  private static Bitmap generateImageThumbnail(Context context, MasterSecret masterSecret, Uri uri)
      throws IOException, BitmapDecodingException, OutOfMemoryError
  {
    int maxSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_max_size);
    return BitmapUtil.createScaledBitmap(context, masterSecret, uri, maxSize, maxSize);
  }

  public static Slide getSlideForPart(Context context, MasterSecret masterSecret, PduPart part, String contentType) {
    Slide slide = null;
    if (isGif(contentType)) {
      slide = new GifSlide(context, masterSecret, part);
    } else if (ContentType.isImageType(contentType)) {
      slide = new ImageSlide(context, masterSecret, part);
    } else if (ContentType.isVideoType(contentType)) {
      slide = new VideoSlide(context, masterSecret, part);
    } else if (ContentType.isAudioType(contentType)) {
      slide = new AudioSlide(context, masterSecret, part);
    }

    return slide;
  }

  public static String getMimeType(Context context, Uri uri) {
    String type = context.getContentResolver().getType(uri);
    if (type == null) {
      final String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
      type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
    return type;
  }

  public static boolean isGif(String contentType) {
    return !TextUtils.isEmpty(contentType) && contentType.trim().equals("image/gif");
  }

  public static boolean isGif(PduPart part) {
    return isGif(Util.toIsoString(part.getContentType()));
  }

  public static boolean isImage(PduPart part) {
    return ContentType.isImageType(Util.toIsoString(part.getContentType()));
  }

  public static boolean isAudio(PduPart part) {
    return ContentType.isAudioType(Util.toIsoString(part.getContentType()));
  }

  public static boolean isVideo(PduPart part) {
    return ContentType.isVideoType(Util.toIsoString(part.getContentType()));
  }

  public static class ThumbnailData {
    Bitmap bitmap;
    float aspectRatio;

    public ThumbnailData(Bitmap bitmap) {
      this.bitmap      = bitmap;
      this.aspectRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
    }

    public Bitmap getBitmap() {
      return bitmap;
    }

    public float getAspectRatio() {
      return aspectRatio;
    }

    public InputStream toDataStream() {
      return BitmapUtil.toCompressedJpeg(bitmap);
    }
  }
}
