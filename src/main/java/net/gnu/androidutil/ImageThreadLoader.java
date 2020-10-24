package net.gnu.androidutil;

import android.graphics.*;
import java.io.*;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import java.util.LinkedList;
import net.gnu.agrep.R;
import net.gnu.common.SearcherAplication;
import net.gnu.util.FileUtil;
import net.gnu.common.Holder;

//the simplest in-memory cache implementation. This should be replaced with something like SoftReference or BitmapOptions.inPurgeable(since 1.6)
public class ImageThreadLoader {
	
	private static final String TAG = "ImageThreadLoader";
    
	private File cacheDir;
	private final LoaderThread loaderThread = new LoaderThread();

	private final LinkedList<UrlImageView> urlImageViews=new LinkedList<UrlImageView>();
	private final Activity activity;
	final int width;
	final int height;
	final int density;

	public static Drawable apkIcon;
	public static Drawable myfolder72;
	private static Drawable myzip;
	private static Drawable rar;
	private static Drawable pdf_icon;
	private static Drawable textpng;
	private static Drawable html;
	private static Drawable image;
	private static Drawable audio;
	private static Drawable videos_new;
	private static Drawable script_file64;
	private static Drawable build_file64;
	private static Drawable xml64;
	private static Drawable nsword64;
	private static Drawable ppt64;
	private static Drawable spreadsheet64;
	public static Drawable miscellaneous;
	public static Drawable stubIcon;

//	static {
//		final String sdCardPath = System.getenv("SECONDARY_STORAGE");
//		Log.d("SECONDARY_STORAGE", sdCardPath + "");
//		if (sdCardPath == null) {
//			init();
//			PRIVATE_DIR.mkdirs();
//			Log.d("sdCardPath = null", "PRIVATE_PATH = " + PRIVATE_PATH);
//		} else if (!sdCardPath.contains(":")) {
//			PRIVATE_PATH = sdCardPath + "/.net.gnu.searcher";
//			PRIVATE_DIR = new File(PRIVATE_PATH);
//			if (PRIVATE_DIR.exists() || PRIVATE_DIR.mkdirs()) {
//			} else {
//				init();
//				PRIVATE_DIR.mkdirs();
//			}
//			Log.d(sdCardPath, PRIVATE_DIR.getFreeSpace() + " bytes available");
//		} else if (sdCardPath.contains(":")) {
//			//Multiple Sdcards show root folder and remove the Internal storage from that.
//			final File storage = new File("/storage");
//			final File[] fs = storage.listFiles();
//			init();
//			PRIVATE_DIR.mkdirs();
//			if (fs != null) {
//				long maxTotal = PRIVATE_DIR.getFreeSpace();
//				for (File f : fs) {
//					final String absolutePath = f.getAbsolutePath();
//					final long totalSpace = f.getFreeSpace();
//					Log.d(absolutePath, totalSpace + " bytes available, can write " + f.canWrite());
//					final String comPath = absolutePath + "/.net.gnu.searcher";
//					final File compF = new File(comPath);
//					if (totalSpace > maxTotal && f.canWrite() && (compF.exists() || compF.mkdirs())) {
//						PRIVATE_PATH = comPath;
//						PRIVATE_DIR.delete();
//						PRIVATE_DIR = compF;
//						Log.d("PRIVATE_PATH", PRIVATE_PATH);
//						maxTotal = totalSpace;
//					}
//				}
//			}
//		}
//	}

	public ImageThreadLoader(final Activity activity, final int w, final int h) {
		this.activity = activity;

        //Make the background thead low priority. This way it will not affect the UI performance
        loaderThread.setPriority(Thread.NORM_PRIORITY-1);

        //Find the dir to save cached images
        cacheDir = new File(SearcherAplication.PRIVATE_PATH, "cache_img");
        if(!cacheDir.exists())
            cacheDir.mkdirs();

        //final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        //cachefiles=prefs.getBoolean(PREF_CACHE,true);
        
		final Resources res = activity.getResources();
		density = (int)(res.getDisplayMetrics().density);
		width = w * density;
		height = h * density;
		
		if (apkIcon == null) {
			apkIcon = res.getDrawable(R.drawable.apk_file);
			myfolder72 = res.getDrawable(R.drawable.myfolder72);
			myzip = res.getDrawable(R.drawable.myzip);
			rar = res.getDrawable(R.drawable.rar);
			pdf_icon = res.getDrawable(R.drawable.pdf_icon);
			textpng = res.getDrawable(R.drawable.textpng);
			html = res.getDrawable(R.drawable.html);
			image = res.getDrawable(R.drawable.image);
			audio = res.getDrawable(R.drawable.audio);
			videos_new = res.getDrawable(R.drawable.videos_new);
			script_file64 = res.getDrawable(R.drawable.script_file64);
			build_file64 = res.getDrawable(R.drawable.build_file64);
			xml64 = res.getDrawable(R.drawable.xml64);
			nsword64 = res.getDrawable(R.drawable.nsword64);
			ppt64 = res.getDrawable(R.drawable.ppt64);
			spreadsheet64 = res.getDrawable(R.drawable.spreadsheet64);
			miscellaneous = res.getDrawable(R.drawable.miscellaneous);
			apkIcon = res.getDrawable(R.drawable.apk_file);
			stubIcon = res.getDrawable(R.drawable.image);
		}
		
		//start thread if it's not started yet
        //if(loaderThread.getState()==Thread.State.NEW)
        loaderThread.start();
	} 

	public static Drawable getFileIcon(final String ext) {
		if (ext.equals("zip")
			|| ext.equals("jar")) {
			return myzip;
		} else if (ext.equals("apk")) {
			return apkIcon;
		} else if (ext.equals("rar")) {
			return rar;
		} else if (ext.equals("pdf")) {
			return pdf_icon;
		} else if (ext.equals("txt")) {
			return textpng;
		} else if (ext.equals("html")
				   || ext.equals("htm")
				   || ext.equals("xhtm")
				   || ext.equals("xhtml")) {
			return html;
		} else if (ext.equals("jpg")
				   || ext.equals("png")
				   || ext.equals("gif")
				   || ext.equals("jpeg")
				   || ext.equals("tiff")) {
			return image;
		} else if (ext.equals("mp3")
				 || ext.equals("wav")
				 || ext.equals("wma")
				 || ext.equals("opus")
				 || ext.equals("flac")
				 || ext.equals("mka")
				 || ext.equals("amr")
				 || ext.equals("3gp")
				 || ext.equals("m4a")) {
			return audio;
		} else if (ext.equals("mp4")
				   || ext.equals("3gp")
				   || ext.equals("flv")
				   || ext.equals("ogg")
				   || ext.equals("m4v")) {
			return videos_new;
		} else if (ext.equals("sh")
				   || ext.equals("rc")) {
			return script_file64;
		} else if (ext.equals("prop")) {
			return build_file64;
		} else if (ext.equals("xml")) {
			return xml64;
		} else if (ext.equals("doc")
				   || ext.equals("docx")) {
			return nsword64;
		} else if (ext.equals("ppt")
				   || ext.equals("pptx")) {
			return ppt64;
		} else if (ext.equals("xls")
				   || ext.equals("xlsx")) {
			return spreadsheet64;
		} else {
			return miscellaneous;
		}
	}

    /**
     * Display image.
     *
     * @param url the url
     * @param activity the activity
     * @param imageView the image view
     */
    public void displayImage(final String url, final Activity activity, ImageView imageView, final Drawable defaultRes) {
		//Log.d(TAG, "displayImage=" + url);
		final Bitmap b = BitmapCache.getBitmapFromCache(url);
        if (b != null) {
            imageView.setImageBitmap(b);
        } else {
            queuePhoto(url, activity, imageView);
            imageView.setImageDrawable(defaultRes);
        }
	} 

	/**
     * Queue photo.
     *
     * @param url the url
     * @param activity the activity
     * @param imageView the image view
     */
    private void queuePhoto(final String url, final Activity activity, ImageView imageView) {
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        //clean(imageView);
        final UrlImageView urlImageView = new UrlImageView(url, imageView);
		synchronized (urlImageViews) {
			urlImageViews.push(urlImageView);
            urlImageViews.notifyAll();
        } 
    }

	/**
     * Cache image.
     *
     * @param bmp the Bitmap
     * @param url the filepath
     */
	public Bitmap createCacheFile(final Bitmap bmp, final String url) {
		if (url.startsWith(SearcherAplication.PRIVATE_PATH)) {
			return bmp;
		} else {
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				final File img = new File(cacheDir, FileUtil.getPathHash(url) + ".png");
				fos = new FileOutputStream(img);
				bos = new BufferedOutputStream(fos);
				//final Bitmap bitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true);
//			if (!bOut.equals(bmp)) {
//				bmp.recycle();
//			}
				bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
				return bmp;
			} catch (Exception e) {
				e.printStackTrace();
				return bmp;
			} finally {
				FileUtil.flushClose(bos, fos);
			}
		}
	}

	/**
     * Gets the bitmap.
     *
     * @param url the url
     * @return the bitmap
     */
    private Bitmap getBitmap(final String url) {
        final Bitmap b1 = BitmapCache.getBitmapFromCache(url);
        if(b1!=null) {
            return b1;
		}
		//I identify images by hashcode. Not a perfect solution, good for the demo.
		final String filename=String.valueOf(FileUtil.getPathHash(url) + ".png");
        File f=new File(cacheDir, filename);
		if (f.exists()) {
			final Bitmap d = BitmapFactory.decodeFile(f.getAbsolutePath());
			//from SD cache
			if(d!=null) {
				BitmapCache.put(url, d);  
				return d;
			}
		}

        //from origin file
		f = new File(url);
		if ("apk".equals(FileUtil.getExtension(f))) {
			final Drawable apkIco = AndroidUtils.getApkIcon(activity, url);
			if (apkIco != null) {
				final Bitmap drawableToBitmap = BitmapUtil.drawableToBitmap(apkIco);
				BitmapCache.put(url, drawableToBitmap);
				return drawableToBitmap;
			} else {
				final Bitmap drawableToBitmap = BitmapUtil.drawableToBitmap(apkIcon);
				BitmapCache.put(url, drawableToBitmap);
				return drawableToBitmap;
			}
		} else {
			try {
				Bitmap mBitmap = null;
				final byte[] barr = FileUtil.readFileToMemory(f);

				final BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				opts.inSampleSize = 1;

				mBitmap = BitmapFactory.decodeByteArray(barr, 0, barr.length, opts);
//				int inSampleSize = 1;
//				while ((opts.outHeight / opts.inSampleSize) > size
//					   || (opts.outWidth / opts.inSampleSize) > size) {
//					inSampleSize <<= 1;
//				}
				final int w = opts.outWidth / width;
				final int h = opts.outHeight / height;
				if (w > h && w > 1) {
					opts.inSampleSize = w;
				} else if (w < h && h > 1) {
					opts.inSampleSize = h;
				}  
				opts.inJustDecodeBounds = false;
				mBitmap = BitmapFactory.decodeByteArray(barr, 0, barr.length, opts);
				BitmapCache.put(url, mBitmap);  
				return mBitmap;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
	} 

    public void stopThread() {
        loaderThread.interrupt();
    } 

	//removes all instances of this ImageView
	/**
	 * Clean.
	 *
	 * @param image the image
	 */
//	public void clean(ImageView image) {
//		int i = 0;
//		for (UrlImageView u : urlImageViews) {
//			if(u.imageView==image
//			|| !u.url.equals(u.imageView.getTag())) {
//				synchronized (urlImageViews) {
//				 if(u.imageView==image
//					|| !u.url.equals(u.imageView.getTag())) {
//						urlImageViews.remove(i);
//					}
//				}
//				break;
//			} else {
//				i++;
//			}
//		}
//	}

	//Task for the queue
	private class UrlImageView {

		private final String url;
		private final ImageView imageView;

		private UrlImageView(final String u, final ImageView i) {
			url=u; 
			imageView=i;
		} 
	}

    private class LoaderThread extends Thread {
        public void run() {
            try {
				while (true) {
                    //thread waits until there are any images to load in the queue
                    synchronized(urlImageViews) {
						if(urlImageViews.size() == 0)
							urlImageViews.wait();
					}
					if(urlImageViews.size()!=0) {
						UrlImageView urlImageView = null;
						synchronized (urlImageViews) {
							urlImageView = urlImageViews.pop();
						}
						if (urlImageView.url.equals(((Holder)urlImageView.imageView.getTag()).fileInfo.path)) {//FolderChooserActivity.ArrayAdapter.
							final Bitmap bmp = getBitmap(urlImageView.url);
							if (bmp != null) {
								//if(cachefiles) {
									createCacheFile(bmp, urlImageView.url);
								//}
								final BitmapDisplayer bd = new BitmapDisplayer(bmp, urlImageView);
								activity.runOnUiThread(bd);
							}
						}
					}
					if (Thread.interrupted())
                        break;
                }
			} catch (InterruptedException e) {
                e.printStackTrace();
            }
		}
	}

    //Used to display bitmap in the UI thread
    private class BitmapDisplayer implements Runnable {

        private final Bitmap bitmap;
        private final UrlImageView urlImageView;

        private BitmapDisplayer(final Bitmap b, final UrlImageView i) {
			bitmap = b;
			urlImageView = i;
		}

        public void run() {
			if(urlImageView.url.equals(((Holder)urlImageView.imageView.getTag()).fileInfo.path)) {//FolderChooserActivity.ArrayAdapter.
				synchronized (urlImageView.imageView) {
					if(urlImageView.url.equals(((Holder)urlImageView.imageView.getTag()).fileInfo.path)) {//FolderChooserActivity.ArrayAdapter.
						urlImageView.imageView.setImageBitmap(bitmap);
					}
				}
			}
        }
	}	
}

