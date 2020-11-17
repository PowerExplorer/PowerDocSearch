package net.gnu.androidutil;

import java.io.File;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.net.*;
import android.content.pm.*;
import java.util.*;
import android.content.res.*;
import android.content.pm.PackageManager.*;
import android.app.*;
import android.content.*;
import java.io.*;
import android.net.wifi.*;
import android.provider.*;
import android.preference.*;
import android.database.*;
import android.support.annotation.*;
import android.graphics.drawable.*;
import android.widget.*;
import android.graphics.*;
import android.telephony.*;
import android.os.Bundle;
import android.os.*;
import android.graphics.pdf.*;
import android.print.*;
import android.print.pdf.*;
import android.view.*;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;

public class AndroidUtils {

	private static String TAG = "AndroidUtil";
	
	public static LayoutInflater getLayoutInflater(Context ctx) {
		return (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public static CharSequence getClipboardData(final Context context) {
		final int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) { //Android 2.3 and below
			final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			return clipboard.getText();
		} else { //Android 3.0 and higher
			final android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			return clipboard.getText();
		}
	}
	
	public void showToast (View v, String st) {
//		LayoutInflater inflater = LayoutInflater.from(v.getContext());
//		View layout = inflater. inflate (R.layout.dir , null);
//		TextView text = (TextView) v.findViewById (R.id.name);
		TextView text = new TextView(v.getContext());
		text.setText (st);
		Toast toast = new Toast (v.getContext ()) ;
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView (text);
		toast.show () ;
	}
	
	public void writePdf(Context context) {

		// Create a shiny new (but blank) PDF document in memory
		// We want it to optionally be printable, so add PrintAttributes
		// and use a PrintedPdfDocument. Simpler: new PdfDocument().
		PrintAttributes printAttrs = new PrintAttributes.Builder().
			setColorMode(PrintAttributes.COLOR_MODE_COLOR).
			setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
			setResolution(new PrintAttributes.Resolution("zooey", "PRINT_SERVICE", 300, 300)).
			setMinMargins(PrintAttributes.Margins.NO_MARGINS).
			build();
		PdfDocument document = new PrintedPdfDocument(context, printAttrs);

		// crate a page description
		PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 300, 1).create();

		// create a new page from the PageInfo
		PdfDocument.Page page = document.startPage(pageInfo);

		// repaint the user's text into the page
//		View content = findViewById(R.id.textArea);
//		content.draw(page.getCanvas());
//
//		// do final processing of the page
//		document.finishPage(page);

		// Here you could add more pages in a longer doc app, but you'd have
		// to handle page-breaking yourself in e.g., write your own word processor...

		// Now write the PDF document to a file; it actually needs to be a file
		// since the Share mechanism can't accept a byte[]. though it can
		// accept a String/CharSequence. Meh.
//		try {
//			File pdfDirPath = new File(getFilesDir(), "pdfs");
//			pdfDirPath.mkdirs();
//			File file = new File(pdfDirPath, "pdfsend.pdf");
//			Uri contentUri = FileProvider.getUriForFile(this, "com.example.fileprovider", file);
//			os = new FileOutputStream(file);
//			document.writeTo(os);
//			document.close();
//			os.close();
//
//			//shareDocument(contentUri);
//		} catch (IOException e) {
//			throw new RuntimeException("Error generating file", e);
//		}
	}
	
	public static void pdfToImage(final File f, final String outDir,
								  final int zoom,
								  boolean extractImages,
								  int left, int top, int right, int bottom,
								  int background,
								  Bitmap.CompressFormat type,
								  int rate) throws IOException {
        Log.d(TAG, "pdfToImage density " + zoom + ", " + f.getAbsolutePath() + ", " + outDir);
		// In this sample, we read a PDF from the assets directory.

		final String name = (outDir.endsWith("/") ? outDir : outDir+"/") + f.getName();
		final float zoom100 = (float)zoom / 100;
		ParcelFileDescriptor mFileDescriptor = null;
		PdfRenderer mPdfRenderer = null;
		PdfRenderer.Page mCurrentPage = null;
		try {
			mFileDescriptor = ParcelFileDescriptor.open(f,  ParcelFileDescriptor.MODE_READ_ONLY);//context.getAssets().openFd("sample.pdf").getParcelFileDescriptor();
			// This is the PdfRenderer we use to render the PDF.
			mPdfRenderer = new PdfRenderer(mFileDescriptor);

			final int count = mPdfRenderer.getPageCount();
			// Use `openPage` to open a specific page in PDF.
			for (int i = 0; i < count; i++) {
				mCurrentPage = mPdfRenderer.openPage(i);
				// Important: the destination bitmap must be ARGB (not RGB).
				int width = (int) (mCurrentPage.getWidth() * zoom100);
				int height = (int) (mCurrentPage.getHeight() * zoom100);
				Bitmap bitmap = Bitmap.createBitmap(width, 
													height,
													Bitmap.Config.ARGB_8888);

				// Here, we render the page onto the Bitmap.
				// To render a portion of the page, use the second and third parameter. Pass nulls to get
				// the default result.
				// Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
				mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
				// Make sure to close the current page before opening another one.
				mCurrentPage.close();
				Bitmap cropedBitmap = Bitmap.createBitmap(
					bitmap,
					left,
					top,
					width - left - right,
					height - top - bottom
				);

				Bitmap newBitmap = Bitmap.createBitmap(cropedBitmap.getWidth(), cropedBitmap.getHeight(), Bitmap.Config.ARGB_8888);

				Canvas canvas = new Canvas(newBitmap);

				canvas.drawColor(background);

				canvas.drawBitmap(cropedBitmap, 0 , 0 , null);

				final String ext = type == Bitmap.CompressFormat.PNG ? ".png" : type == Bitmap.CompressFormat.JPEG ? ".jpg" : ".webp";
				bitmap.recycle();
				cropedBitmap.recycle();
				bitmap = null;
				cropedBitmap = null;
				BitmapUtil.saveBitmap(newBitmap, type, rate, 
									  String.format(name + "_%1$03d", i + 1) + ext);
				newBitmap.recycle();
			}
		} finally {
			mPdfRenderer.close();
			mFileDescriptor.close();
		}
    }
	
	public static void showToast(Context ctx, String st) {
		Toast.makeText(ctx, st, Toast.LENGTH_SHORT).show();
	}

	public static String bundleToString(Bundle b) {
		StringBuilder sb = new StringBuilder();
		if (b != null) {
			Set<String> s = b.keySet();
			for (String st : s) {
				sb.append(st).append("=").append(b.get(st)).append("\n");
			}
		}
		return sb.toString();
	}
	
	public static String fetch_tel_status(Context cx) {
		String result = null;
		TelephonyManager tm = (TelephonyManager) cx
			.getSystemService(Context.TELEPHONY_SERVICE);//
		String str = "";
		str += "DeviceId(IMEI) = " + tm.getDeviceId() + "\n";
		str += "DeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion()
			+ "\n";
		str += "Line1Number = " + tm.getLine1Number() + "\n";
		str += "NetworkCountryIso = " + tm.getNetworkCountryIso() + "\n";
		str += "NetworkOperator = " + tm.getNetworkOperator() + "\n";
		str += "NetworkOperatorName = " + tm.getNetworkOperatorName() + "\n";
		str += "NetworkType = " + tm.getNetworkType() + "\n";
		str += "PhoneType = " + tm.getPhoneType() + "\n";
		str += "SimCountryIso = " + tm.getSimCountryIso() + "\n";
		str += "SimOperator = " + tm.getSimOperator() + "\n";
		str += "SimOperatorName = " + tm.getSimOperatorName() + "\n";
		str += "SimSerialNumber = " + tm.getSimSerialNumber() + "\n";
		str += "SimState = " + tm.getSimState() + "\n";
		str += "SubscriberId(IMSI) = " + tm.getSubscriberId() + "\n";
		str += "VoiceMailNumber = " + tm.getVoiceMailNumber() + "\n";

		int mcc = cx.getResources().getConfiguration().mcc;
		int mnc = cx.getResources().getConfiguration().mnc;
		str += "IMSI MCC (Mobile Country Code):" + String.valueOf(mcc) + "\n";
		str += "IMSI MNC (Mobile Network Code):" + String.valueOf(mnc) + "\n";
		result = str;
		return result;
	}
	
	public static void setImageDrawable(ImageView imgView, Context ctx, int resId) {
		imgView.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(), resId));
	}
	
	public static void setImageDrawable(ImageView imgView, String path) {
		imgView.setImageDrawable(Drawable.createFromPath(path) );
	}
	
	public static boolean isRoot() {
		boolean flag = false;

		try {
			if ((!new File("/system/bin/su").exists())
				&& (!new File("/system/xbin/su").exists())) {
				flag = false;
			} else {
				flag = true;
			}
		} catch (Exception e) {

		}
		return flag;
	}

	public static Intent getApkFileIntent(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
							  "application/vnd.android.package-archive");
		return intent;
	}

	public static String getApkVersionName(Context context, String pkgName)
	throws NameNotFoundException {
		PackageInfo pkgInfo = context.getApplicationContext().getPackageManager()
			.getPackageInfo(pkgName, 0);
		return pkgInfo.versionName;
	}

	public static String getApkVersionName(PackageManager context, String pkgName)
	throws NameNotFoundException {
		return context.getPackageInfo(pkgName, 0).versionName;
	}

	public static int getApkVersionCode(Context context, String pkgName)
	throws NameNotFoundException {
		PackageInfo pkgInfo = context.getApplicationContext().getPackageManager()
			.getPackageInfo(pkgName, 0);
		return pkgInfo.versionCode;
	}
	
	public static String getApkMetaData(Context context, String apkPath,
										String key) {

		if (context == null || key == null) {
			return null;
		}

		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
													PackageManager.GET_META_DATA);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;

			if (appInfo == null || appInfo.metaData == null) {
				return null;
			}

			return appInfo.metaData.getString(key);
		}

		return null;
	}
	
	public static ArrayList<String> getApkPath(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> pkginfolist = pm.getInstalledPackages(PackageManager.GET_META_DATA);
		ArrayList<String> apkPathList = new ArrayList<String>(pkginfolist.size());
		for (PackageInfo pi : pkginfolist) {
			ApplicationInfo applicationInfo = pi.applicationInfo;
			if (applicationInfo != null) {
				apkPathList.add(applicationInfo.sourceDir);
			}
		}
		return apkPathList;
	}
	
	public static Drawable getApkIcon(Context ctx, String apkPath, int defRes) {
		Drawable icon = null;
		try{
			PackageManager packageManager = ctx.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
			if (Build.VERSION.SDK_INT >= 5 && packageInfo != null) {
				ApplicationInfo appInfo = packageInfo.applicationInfo;
				if (appInfo != null) {
					appInfo.sourceDir = apkPath;
					appInfo.publicSourceDir = apkPath;
					icon = appInfo.loadIcon(packageManager);
				}
//				if(icon.getIntrinsicHeight() >50 && icon.getIntrinsicWidth()>50){
//					//Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
//					// int dp5 = (int)(activity.getResources().getDisplayMetrics().densityDpi/120);
//					//icon= new BitmapDrawable(activity.getResources(),Bitmap.createScaledBitmap(bitmap, 50*dp5, 50*dp5, true));
//				}
			} else {
				icon = ctx.getResources().getDrawable(defRes); //R.drawable.apk_file
			}
			return icon;
		} catch (Exception e) {
			return ctx.getResources().getDrawable(defRes);
		}
	}
	
	public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (appInfo != null) {
				appInfo.sourceDir = apkPath;
				appInfo.publicSourceDir = apkPath;
       	     	Drawable icon = appInfo.loadIcon(pm);
      	      	return icon;
			}
        }
        return null;
    }
	
	public static boolean isAppRunningTop(Context context) {

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
		if (list.size() > 0) {
			String packageName = context.getPackageName();
			ActivityManager.RunningTaskInfo topRunningTaskinfo  = list.get(0);
			if (topRunningTaskinfo.topActivity.getPackageName().equals(packageName)) {
				return true;
			}
		}

		return false;
	}
	
	public static boolean startApp(Context context, String packageName,
								   String className, Map<String, String> data) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(packageName, className);

		if (data != null) {
			Iterator<Map.Entry<String, String>> iter = data.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				intent.putExtra(entry.getKey(), entry.getValue());
			}
		}
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {

			return false;
		}

		return true;
	}
	
	public static boolean getAirplaneMode(Context context) {
		try {
			int airplaneModeSetting = Settings.System.getInt(
				context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
			return airplaneModeSetting==1?true:false;
		} catch (Settings.SettingNotFoundException e) {
			return false;
		}
	}
	
	public static StringBuilder getLogcat() {
		StringBuilder log=new StringBuilder();
		try {
			java.lang.Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return log;
	}
	
	public static final boolean isWifiEnabled(Context context) {
        return ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
    }
	
	public static boolean isWiFiConnected(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
			.getApplicationContext().getSystemService(
            Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
						&& info[i].isConnected())
						return true;
				}
			}
		}
		return false;
	}
	
	public static void enableWifi(Context ctx) {
		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(true);
//		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
//		for(Method method: wmMethods){
//			if(method.getName().equals("setWifiEnabled")){
//				WifiConfiguration netConfig = new WifiConfiguration();
//				netConfig.SSID = "\"PROVAAP\"";
//				netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//				netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//				netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//				netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//				netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//				netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//				netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
//
//				try {
//					method.invoke(wifi, netConfig,true);
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				} catch (InvocationTargetException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	public static Integer getSystemWifiIpAddress(Context context) {
		WifiManager wManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wManager.getConnectionInfo();

		int ipAddress = wInfo.getIpAddress();
		if (ipAddress == 0)
			return null;
		return ipAddress;
	}
	
	public static boolean canUpload(Context mContext) {

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean allowWiFi = false, allowMobile = false;

        String mSetting = "pref_upload_connection_key";
		mSetting = mPrefs.getString(mSetting, "");//

        // Check for no setting - default to phone
        if ( mSetting.length() == 0 ) { 
			allowWiFi = allowMobile = true;
        } 
        if ( mSetting.contains("wifi"))
			allowWiFi = true;
        if ( mSetting.contains("mobile"))
			allowMobile = true;

		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = cm.getActiveNetworkInfo();
		int netType = info.getType();

		if (netType == ConnectivityManager.TYPE_WIFI) {
			if ( allowWiFi && info.isConnected() ) 
				return true;
		} else if (netType == ConnectivityManager.TYPE_MOBILE) {
			if ( allowMobile && info.isConnected() ) 
				return true;
		}
        return false;
	}
	
	
	// Creates shortcut on Android widget screen
	public static void createShortcutIcon(Context ctx, Activity act, String shortcutName){
		String PREFS_NAME = "PREFS_NAME";
		String PREF_KEY_SHORTCUT_ADDED = "PREF_KEY_SHORTCUT_ADDED";
		// Checking if ShortCut was already added
		SharedPreferences sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		boolean shortCutWasAlreadyAdded = sharedPreferences.getBoolean(PREF_KEY_SHORTCUT_ADDED, false);
		if (shortCutWasAlreadyAdded) 
			return;

		Intent shortcutIntent = new Intent(ctx.getApplicationContext(), act.getClass());
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
		//addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(ctx.getApplicationContext(), R.drawable.ic_launcher));
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		ctx.getApplicationContext().sendBroadcast(addIntent);

		// Remembering that ShortCut was already added
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_SHORTCUT_ADDED, true);
		editor.commit();
	}
	
//	public static String getSharedPreferenceUri(final Context ctx, final String id) {
//		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
//		return defaultSharedPreferences.getString(id, null);
//	}


	public static String getSharedPreference(final Context ctx, final String id, final String defaultValue) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		return defaultSharedPreferences.getString(id, defaultValue);
	}

	public static void setSharedPreference(final Context ctx, final String id, @Nullable final String value) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		final SharedPreferences.Editor editor = defaultSharedPreferences.edit();
		editor.putString(id, value);
		editor.commit();
	}
	
	public static void startService(Activity activity, Class<? extends Service> service, String action, String tag) {
		Log.e(tag, "Starting service");
		Intent intent = new Intent(action);//ForegroundService.ACTION_FOREGROUND);//ACTION_BACKGROUND
		intent.setClass(activity, service);
		activity.startService(intent);
	}

	public static boolean isPhone(Activity activity) {
		Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		defaultDisplay.getMetrics(outMetrics);
		Log.i("defaultDisplay.outMetrics", outMetrics + ".");
		Point op = new Point();
		defaultDisplay.getSize(op);
		Log.i("defaultDisplay.getSize()", op + ".");
		return op.x / outMetrics.xdpi < 3;
	}
	
	public static void uninstall(Context ctx, String pkgName) {
		Uri packageUri = Uri.parse("package:" + pkgName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
		//Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
		uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
		ctx.startActivity(uninstallIntent);
	}
	
	public boolean uninstallPackage(Context context, String packageName) {
		//ComponentName name = new ComponentName(MyAppName, MyDeviceAdminReceiver.class.getCanonicalName());
		PackageManager packageManger = context.getPackageManager();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			PackageInstaller packageInstaller = packageManger.getPackageInstaller();
			PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
			params.setAppPackageName(packageName);
			int sessionId = 0;
			try {
				sessionId = packageInstaller.createSession(params);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			packageInstaller.uninstall(packageName, PendingIntent.getBroadcast(context, sessionId,
																			   new Intent("android.intent.action.MAIN"), 0).getIntentSender());
			return true;
		}
		System.err.println("old sdk");
		return false;
	}

	public boolean installPackage(Context context,
								  String packageName, String packagePath) {
		//ComponentName name = new ComponentName(MyAppName, MyDeviceAdminReceiver.class.getCanonicalName());
		PackageManager packageManger = context.getPackageManager();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			PackageInstaller packageInstaller = packageManger.getPackageInstaller();
			PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
			params.setAppPackageName(packageName);
			try {
				int sessionId = packageInstaller.createSession(params);
				PackageInstaller.Session session = packageInstaller.openSession(sessionId);
				OutputStream out = session.openWrite(packageName + ".apk", 0, -1);
				FileUtil.is2OS(new FileInputStream(packagePath), out); //read the apk content and write it to out
				session.fsync(out);
				out.close();
				System.out.println("installing...");
				session.commit(PendingIntent.getBroadcast(context, sessionId,
														  new Intent("android.intent.action.MAIN"), 0).getIntentSender());
				System.out.println("install request sent");
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		System.err.println("old sdk");
		return false;
	}
	
	public static PackageInfo getPackageInfo(Context ctx, String pkg) {
		Log.d(TAG, "getPackageInfo " + pkg);
		PackageManager pm = ctx.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);
		PackageInfo info1 = null;//=item.getPackageInfo().applicationInfo;
		for (PackageInfo info:packages) {
			if (info.packageName.equals(pkg)) {
				info1 = info;
			}
		}
		return info1;
	}

	public static ApplicationInfo getAppInfo(Context ctx, String pkg) {
		PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		ApplicationInfo info1 = null;//=item.getPackageInfo().applicationInfo;
		for (ApplicationInfo info:packages) {
			if (info.packageName.equals(pkg)) {
				info1 = info;
			}
		}
		return info1;
	}

	public static void investigateApps(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		StringBuilder sb = new StringBuilder();
		for (ApplicationInfo packageInfo : packages) {
			sb.append("\nInstalled package :" + packageInfo.packageName);
			String sourceDir = packageInfo.sourceDir;
			sb.append("\nSource dir : " + sourceDir);
			
			File f = new File(sourceDir);
			sb.append("\nSize: " + Util.nf.format(f.length()) + " byte(s) can read " + f.canRead() + ", can write " + f.canWrite());
			System.out.println("Package Name :" + packageInfo.packageName);

			System.out.println("Launch Intent For Package :"   +  
							   pm.getLaunchIntentForPackage(packageInfo.packageName));

			System.out.println("Application Label :"   + pm.getApplicationLabel(packageInfo));

//			try {
//				System.out.println("Application Label :"   + 
//								   pm.getApplicationIcon(packageInfo.packageName).toString());
//			} catch (PackageManager.NameNotFoundException e) {}


			/*if(i==2) {
			 startActivity(pm.getLaunchIntentForPackage(packageInfo.packageName));
			 break;
			 }*/


			// the getLaunchIntentForPackage returns an intent that you can use with startActivity() 
			sb.append("\nLaunch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName) + "\n"); 
		}

		List<PackageInfo> packages2 = pm.getInstalledPackages(PackageManager.GET_META_DATA);
		for (PackageInfo p : packages2) {
			sb.append("\"" + p.packageName + "_" + p.versionName + "\" is System app " + isSystemPackage(p) + "\n");
			if (!isSystemPackage(p)) {
				ctx.startActivity(pm.getLaunchIntentForPackage(p.packageName));
				break;
			}
		}
	}
	
	public static List<String> getInstalledComponentList(Context ctx)
	throws PackageManager.NameNotFoundException {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ril = ctx.getPackageManager().queryIntentActivities(mainIntent, 0);
        List<String> componentList = new ArrayList<String>();
        String name = null;

        PackageManager packageManager = ctx.getPackageManager();
		for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                Resources res = packageManager.getResourcesForApplication(ri.activityInfo.applicationInfo);
                if (ri.activityInfo.labelRes != 0) {
                    name = res.getString(ri.activityInfo.labelRes);
					System.out.println("has res " + name);
                } else {
                    name = ri.activityInfo.applicationInfo.loadLabel(
						packageManager).toString();
					System.out.println("no res " + name);
                }

                componentList.add(name);
            }
        }
        return componentList;
    }

	public static boolean isSystemPackage(PackageInfo pkgInfo) {
		return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? 
		true : false;
	}
	
	public static String getRunningApps(Context ctx) {
		ActivityManager actvityManager = (ActivityManager)ctx.getSystemService(ctx.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
		StringBuilder sb = new StringBuilder();
		for (ActivityManager.RunningAppProcessInfo procInfo : procInfos) {
			sb.append(procInfo.processName + "\n");
		}
		return sb.toString();
	}
	
	public static boolean isPortrait(Activity activity) {
		Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
		//Log.i("defaultDisplay", defaultDisplay + ".");
		Point op = new Point();
		defaultDisplay.getSize(op);
		//Log.i("defaultDisplay.getSize()", op + ".");
		return op.x < op.y;
	}

	// FileUtils.copyAssetToDir(this, ImageThreadLoader.PRIVATE_PATH,
	// "data/lic.html");
	public static void copyAssetToDir(Activity activity, String dest, String src) {
		try {
			String newDest = dest + "/" + src;
			File file = new File(newDest);
			if (!file.exists()) {
				Log.d(TAG, "copyAssetToDir " + newDest);
				InputStream ins = activity.getAssets().open(src);
				FileUtil.is2File(ins, newDest);
			} else {
				Log.d(TAG, "already existed " + newDest);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public File getAlbumStorageDir(String albumName) {
		// Get the directory for the user's public pictures directory.
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				albumName);
		if (!file.mkdirs()) {
			Log.d("getAlbumStorageDir", "Directory not created");
		}
		return file;
	}

	public File getAlbumStorageDir(Context context, String albumName) {
		// Get the directory for the app's private pictures directory.
		File file = new File(
				context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				albumName);
		if (!file.mkdirs()) {
			Log.d("getAlbumStorageDir", "Directory not created");
		}
		return file;
	}

	public File getReservedStorageDir(Context context, String albumName) {
		// Get the directory for the app's private pictures directory.
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				albumName);
		if (!file.mkdirs()) {
			Log.d("getAlbumStorageDir", "Directory not created");
		}
		return file;
	}
}
