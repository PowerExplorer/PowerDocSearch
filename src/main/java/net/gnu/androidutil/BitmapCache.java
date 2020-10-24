package net.gnu.androidutil;

import android.graphics.*;
import android.util.*;
import android.content.*;
import java.util.*;

public class BitmapCache {
	
	private static LruCache<Integer, Bitmap> sCache;
	private static LruCache<String, Bitmap> sCache2;
	
	private static final int SCALE = 1024; // Measure everything in KB

	private static void init() {

		// Use 1/4th of the available memory for this memory cache.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / SCALE);
		final int cacheSize = maxMemory / 4;

		sCache = new LruCache<Integer, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(final Integer key, final Bitmap bitmap) {
				return bitmap.getByteCount() / SCALE;
			}
		};
	}

	private static void init2() {

		// Use 1/4th of the available memory for this memory cache.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / SCALE);
		final int cacheSize = maxMemory / 4;

		sCache2 = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(final String key, final Bitmap bitmap) {
				return bitmap.getByteCount() / SCALE;
			}
		};
	}

	public static Bitmap getBitmapFromCache(final Context context, final int drawableResId) {
		if (sCache == null) {
			init();
		}
		Bitmap bitmap = sCache.get(drawableResId);
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId);
			sCache.put(drawableResId, bitmap);
		}
		return bitmap;
	}

	public static Bitmap getBitmapFromCache(final String url) {
		if (sCache2 == null) {
			init2();
		}
		final Bitmap bitmap = sCache2.get(url);
		return bitmap;
	}
	
	public static void put(final String url, final Bitmap b) {
		sCache2.put(url, b);
	}
	
	public static void put(final Integer resId, final Bitmap b) {
		sCache.put(resId, b);
	}
}
