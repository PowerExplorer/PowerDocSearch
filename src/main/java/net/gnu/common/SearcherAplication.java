package net.gnu.common;

import org.geometerplus.android.fbreader.*;
import java.io.*;
import android.util.*;
import android.os.*;

public class SearcherAplication extends FBReaderApplication {

	private static final String TAG = "SearcherAplication";
	public static String PRIVATE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.net.gnu.agrep";
	public static File PRIVATE_DIR = new File(PRIVATE_PATH);
	
	@Override
	public void onCreate() {
		super.onCreate();
		long maxSize = 0;
		String absolutePath;
		File[] externalFilesDirs = getExternalFilesDirs("external");
		if (externalFilesDirs != null) {
			for (File file : externalFilesDirs) {
				absolutePath = file.getAbsolutePath();
				Log.d(TAG, "getExtSdCardPaths " + absolutePath);
				if (file != null) {
					final long usableSpace = file.getUsableSpace();
					if (usableSpace > maxSize) {
						PRIVATE_DIR = file;
						PRIVATE_PATH = absolutePath;
						maxSize = usableSpace;
					}
				}
			}
		}
		
	}
	
}
