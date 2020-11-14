package net.gnu.common;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;
import com.afollestad.materialdialogs.*;
import java.io.*;
import java.util.*;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import com.amaze.filemanager.filesystem.MediaStoreHack;
import com.amaze.filemanager.utils.OnProgressUpdate;
import eu.chainfire.libsuperuser.Shell;
import net.gnu.agrep.R;
import net.gnu.common.StorageCheckActivity;
import net.gnu.util.ComparableEntry;
import net.gnu.androidutil.*;

//app must set targetSdkVersion 23 or above
public class StorageCheckActivity extends FragmentActivity {
	private static String TAG = "StorageCheckActivity";
	
	private static final int FROM_PREVIOUS_IO_ACTION = 3;
	protected static final int REQUEST_WRITE_EXTERNAL = 77;
	
	public static final String KEY_INTENT_PROCESS_VIEWER = "openprocesses";
    public static final String TAG_INTENT_FILTER_FAILED_OPS = "failedOps";
    public static final String TAG_INTENT_FILTER_GENERAL = "general_communications";
    public static final String ARGS_KEY_LOADER = "loader_cloud_args_service";
	
    protected static String[] PERMISSIONS_STORAGE = {
		Manifest.permission.WRITE_EXTERNAL_STORAGE,
		//Manifest.permission.WRITE_MEDIA_STORAGE,
	};
	
	public static boolean rootMode = false;
	public static Shell.Interactive shellInteractive;
    public static Handler handler;
    private static HandlerThread handlerThread;
	
	public List<Runnable> pendingOkTasks = new ArrayList<>();
	public List<Runnable> pendingCancelTasks = new ArrayList<>();
	
    protected boolean checkStorage = true;
	public SharedPreferences sharedPref;
	
	public int operation = -1;
//    public ArrayList<BaseFile> oparrayList;
//    public ArrayList<ArrayList<BaseFile>> oparrayListList;

	public Theme materialTheme = Theme.LIGHT;
	public int accentColor = R.color.lightyellow;
	
	
    // oppathe - the path at which certain operation needs to be performed
    // oppathe1 - the new path which user wants to create/modify
    // oppathList - the paths at which certain operation needs to be performed (pairs with oparrayList)
    public String oppathe, oppathe1;
    public ArrayList<String> oppatheList;

	public MainActivityHelper mainActivityHelper;
	public Runnable callback;
	
    /**
     * Initializes an interactive shell, which will stay throughout the app lifecycle
     * The shell is associated with a handler thread which maintain the message queue from the
     * callbacks of shell as we certainly cannot allow the callbacks to run on same thread because
     * of possible deadlock situation and the asynchronous behaviour of LibSuperSU
     */
    private void initializeInteractiveShell() {
        // only one looper can be associated to a thread. So we're making sure not to create new
        // handler threads every time the code relaunch.
        if (rootMode) {
            handlerThread = new HandlerThread("handler");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            shellInteractive = (new Shell.Builder()).useSU().setHandler(handler).open();

            // TODO: check for busybox
            /*try {
			 if (!RootUtils.isBusyboxAvailable()) {
			 Toast.makeText(this, getString(R.string.error_busybox), Toast.LENGTH_LONG).show();
			 closeInteractiveShell();
			 sharedPref.edit().putBoolean(PreferenceUtils.KEY_ROOT, false).apply();
			 }
			 } catch (RootNotPermittedException e) {
			 e.printStackTrace();
			 sharedPref.edit().putBoolean(PreferenceUtils.KEY_ROOT, false).apply();
			 }*/
        }
    }

    /**
     * Closes the interactive shell and threads associated
     */
    private void closeInteractiveShell() {
        if (rootMode) {
            // close interactive shell and handler thread associated with it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // let it finish up first with what it's doing
                handlerThread.quitSafely();
            } else handlerThread.quit();
            shellInteractive.close();
        }
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		initializeInteractiveShell();
		mainActivityHelper = new MainActivityHelper(this);
		
        //requesting storage permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkStorage)
            if (!checkStoragePermission())
                requestStoragePermission();
			
    }

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO: 6/5/2017 Android may choose to not call this method before destruction
        // TODO: https://developer.android.com/reference/android/app/Activity.html#onDestroy%28%29
        closeInteractiveShell();
	}
	
    public boolean checkStoragePermission() {
        // Verify that all required contact permissions have been granted.
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
			== PackageManager.PERMISSION_GRANTED;
    }

    protected void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
																Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
			
			Log.d(TAG, "shouldShowRequestPermissionRationale");
			
			new AlertDialog.Builder(this).setTitle(R.string.grantper).setMessage(getString(R.string.granttext))
				.setPositiveButton(R.string.grant,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						ActivityCompat.requestPermissions(StorageCheckActivity.this, 
														  PERMISSIONS_STORAGE, 
														  REQUEST_WRITE_EXTERNAL);
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				}).show();
			
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            Log.d(TAG, "requestStoragePermission.requestPermissions");
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_WRITE_EXTERNAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
		Log.d(TAG, "onRequestPermissionsResult " + requestCode + ", " + grantResults.length + ", " + grantResults);
		if (requestCode == REQUEST_WRITE_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //refreshDrawer();
            } else {
                Toast.makeText(this, R.string.grantfailed, Toast.LENGTH_SHORT).show();
                requestStoragePermission();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		super.onActivityResult(requestCode, responseCode, intent);
		if (requestCode == FROM_PREVIOUS_IO_ACTION) {
			Uri treeUri;
			if (responseCode == Activity.RESULT_OK) {
				// Get Uri from Storage Access Framework.
				treeUri = intent.getData();
				Log.d(TAG, "treeUri " + treeUri);
				// Persist URI - this is required for verification of writability.
				if (treeUri != null) 
					sharedPref.edit().putString("treeUri", treeUri.toString()).commit();
			} else {
				// If not confirmed SAF, or if still not writable, then revert settings.
				/* DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false, currentFolder);
				 ||!FileUtil.isWritableNormalOrSaf(currentFolder)*/
				return;
			}
			// After confirmation, update stored value of folder.
			// Persist access permissions.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
																  | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			}
			for (int i = pendingOkTasks.size() - 1; i >= 0; i--) {
				pendingOkTasks.remove(0).run();
			}
		}
	}

	public class MainActivityHelper {

		public static final int DOESNT_EXIST = 0;
		public static final int WRITABLE_OR_ON_SDCARD = 1;
		//For Android 5
		public static final int CAN_CREATE_FILES = 2;
		
		private StorageCheckActivity activity;
		
		public MainActivityHelper(StorageCheckActivity activity) {
			this.activity = activity;
		}
		
		public String getFileFromUri(final Uri data) {
			String path = null;
			if ( data != null ){
				final String scheme = data.getScheme();
				if (ContentResolver.SCHEME_FILE.equals(scheme)) {
					path = Uri.decode(data.getEncodedPath());
				} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
					final ContentResolver cr = activity.getContentResolver();
					Cursor cur = null;
					try {
						cur = cr.query(data, null, null, null, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (cur != null) {
						cur.moveToFirst();
						try {
							path = cur.getString(cur.getColumnIndex("_data"));
							if (path == null
								|| !path.startsWith(Environment.getExternalStorageDirectory()
													.getPath())) {
								// from content provider
								path = data.toString();
							}
						} catch (Exception e) {
							path = data.toString();
						}
					} else {
						path = data.toString();
					}
				} else {
				}
			}
			return path;
		}

		public int checkFolder(final File folder, Context context) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				if (AndroidFileUtil.isOnExtSdCard(folder, context)) {
					if (!folder.exists() || !folder.isDirectory()) {
						return DOESNT_EXIST;
					}

					// On Android 5, trigger storage access framework.
					if (!AndroidFileUtil.isWritableNormalOrSaf(folder, context)) {
						guideDialogForLEXA(folder.getPath());
						return DOESNT_EXIST;
					}

					return WRITABLE_OR_ON_SDCARD;
				} else if (AndroidFileUtil.isWritable(new File(folder, "DummyFile"))) {
					return WRITABLE_OR_ON_SDCARD;
				} else return DOESNT_EXIST;
			} else if (Build.VERSION.SDK_INT == 19) {
				if (AndroidFileUtil.isOnExtSdCard(folder, context)) {
					// Assume that Kitkat workaround works
					return WRITABLE_OR_ON_SDCARD;
				} else if (AndroidFileUtil.isWritable(new File(folder, "DummyFile"))) {
					return WRITABLE_OR_ON_SDCARD;
				} else return DOESNT_EXIST;
			} else if (AndroidFileUtil.isWritable(new File(folder, "DummyFile"))) {
				return WRITABLE_OR_ON_SDCARD;
			} else {
				return DOESNT_EXIST;
			}
		}

		public void guideDialogForLEXA(String path) {
			final MaterialDialog.Builder x = new MaterialDialog.Builder(activity);
			x.theme(materialTheme);
			x.title(R.string.needsaccess);
			LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.lexadrawer, null);
			x.customView(view, true);
			// textView
			TextView textView = (TextView) view.findViewById(R.id.description);
			textView.setText(activity.getResources().getString(R.string.needsaccesssummary) + path + activity.getResources().getString(R.string.needsaccesssummary1));
			((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.sd_operate_step);
			x.positiveText(R.string.open);
			x.negativeText(R.string.cancel);
			x.positiveColor(accentColor);
			x.negativeColor(accentColor);
			x.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog materialDialog) {
						triggerStorageAccessFramework();
					}

					@Override
					public void onNegative(MaterialDialog materialDialog) {
						Toast.makeText(StorageCheckActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
					}
				});
			final MaterialDialog y = x.build();
			y.show();
		}

		private void triggerStorageAccessFramework() {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			startActivityForResult(intent, FROM_PREVIOUS_IO_ACTION);
		}
		
	}
	
	public static class AndroidFileUtil {

		/**
		 * Check for a directory if it is possible to create files within this directory, either via normal writing or via
		 * Storage Access Framework.
		 *
		 * @param folder The directory
		 * @return true if it is possible to write in this directory.
		 */
		public static boolean isWritableNormalOrSaf(final File folder, Context c) {
			// Verify that this is a directory.
			if (folder == null)
				return false;
			if (!folder.exists() || !folder.isDirectory()) {
				return false;
			}

			// Find a non-existing file in this directory.
			int i = 0;
			File file;
			do {
				String fileName = "AugendiagnoseDummyFile" + (++i);
				file = new File(folder, fileName);
			} while (file.exists());

			// First check regular writability
			if (isWritable(file)) {
				return true;
			}

			// Next check SAF writability.
			DocumentFile document = getDocumentFile(file, false, c);

			if (document == null) {
				return false;
			}

			// This should have created the file - otherwise something is wrong with access URL.
			boolean result = document.canWrite() && file.exists();

			// Ensure that the dummy file is not remaining.
			deleteFile(file, c);
			return result;
		}

		/**
		 * Delete a file. May be even on external SD card.
		 *
		 * @param file the file to be deleted.
		 * @return True if successfully deleted.
		 */
		public static boolean deleteFile(@NonNull final File file, Context context) {
			// First try the normal deletion.
			if (file == null) return true;
			boolean fileDelete = deleteFilesInFolder(file, context);
			if (file.delete() || fileDelete)
				return true;

			// Try with Storage Access Framework.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && AndroidFileUtil.isOnExtSdCard(file, context)) {

				DocumentFile document = getDocumentFile(file, false, context);
				return document.delete();
			}

			// Try the Kitkat workaround.
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
				ContentResolver resolver = context.getContentResolver();

				try {
					Uri uri = MediaStoreHack.getUriFromFile(file.getAbsolutePath(), context);
					resolver.delete(uri, null, null);
					return !file.exists();
				} catch (Exception e) {
					Log.e("AmazeFileUtils", "Error when deleting file " + file.getAbsolutePath(), e);
					return false;
				}
			}

			return !file.exists();
		}

		/**
		 * Delete all files in a folder.
		 *
		 * @param folder the folder
		 * @return true if successful.
		 */
		public static final boolean deleteFilesInFolder(final File folder, Context context) {
			boolean totalSuccess = true;
			if (folder == null)
				return false;
			if (folder.isDirectory()) {
				for (File child : folder.listFiles()) {
					deleteFilesInFolder(child, context);
				}

				if (!folder.delete())
					totalSuccess = false;
			} else {

				if (!folder.delete())
					totalSuccess = false;
			}
			return totalSuccess;
		}

		/**
		 * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
		 * existing, it is created.
		 *
		 * @param file        The file.
		 * @param isDirectory flag indicating if the file should be a directory.
		 * @return The DocumentFile
		 */
		public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
			String baseFolder = getExtSdCardFolder(file, context);
			boolean originalDirectory = false;
			Log.d("FileUtil", "getDocumentFile.file " + file + ", " + isDirectory + ", baseFolder " + baseFolder);
			if (baseFolder == null) {
				return null;
			}

			String relativePath = null;
			try {
				String fullPath = file.getCanonicalPath();
				if (!baseFolder.equals(fullPath))
					relativePath = fullPath.substring(baseFolder.length() + 1);
				else 
					originalDirectory = true;
			} catch (IOException e) {
				Log.d("FileUtil", e.getMessage(), e);
				return null;
			} catch (Exception f) {
				Log.d("FileUtil", f.getMessage(), f);
				originalDirectory = true;
				//continue
			}
			String as = PreferenceManager.getDefaultSharedPreferences(context).getString("treeUri", null);

			Uri treeUri = null;
			if (as != null) 
				treeUri = Uri.parse(as);
			Log.d("FileUtil", "originalDirectory " + originalDirectory + ", as " + as + ", relativePath " + relativePath + ", treeUri " + treeUri);
			if (treeUri == null) {
				return null;
			}

			// start with root of SD card and then parse through document tree.
			DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
			if (originalDirectory) 
				return document;
			if (document == null) {
				return null;
			}
			String[] parts = relativePath.split("\\/");
			for (int i = 0; i < parts.length; i++) {
				Log.d("FileUtil", "document " + document + ", parts[] " + parts[i]);
				DocumentFile nextDocument = document.findFile(parts[i]);

				if (nextDocument == null) {
					if ((i < parts.length - 1) || isDirectory) {
						nextDocument = document.createDirectory(parts[i]);
					} else {
						nextDocument = document.createFile("image", parts[i]);
					}
				}
				Log.d("FileUtil", "nextDocument " + nextDocument);
				document = nextDocument;
			}

			return document;
		}

		/**
		 * Check if a file is writable. Detects write issues on external SD card.
		 *
		 * @param file The file
		 * @return true if the file is writable.
		 */
		public static boolean isWritable(final File file) {
			if (file == null)
				return false;
			boolean isExisting = file.exists();

			try {
				FileOutputStream output = new FileOutputStream(file, true);
				try {
					output.close();
				} catch (IOException e) {
					// do nothing.
				}
			} catch (FileNotFoundException e) {
				return false;
			}
			boolean result = file.canWrite();

			// Ensure that file is not created during this process.
			if (!isExisting) {
				file.delete();
			}

			return result;
		}

		/**
		 * Determine if a file is on external sd card. (Kitkat or higher.)
		 *
		 * @param file The file.
		 * @return true if on external sd card.
		 */
		@TargetApi(Build.VERSION_CODES.KITKAT)
		public static boolean isOnExtSdCard(final File file, Context c) {
			return getExtSdCardFolder(file, c) != null;
		}

		/**
		 * Determine the main folder of the external SD card containing the given file.
		 *
		 * @param file the file.
		 * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
		 * null is returned.
		 */
		@TargetApi(Build.VERSION_CODES.KITKAT)
		public static String getExtSdCardFolder(final File file, Context context) {
			final String[] extSdPaths = getExtSdCardPaths(context);
			Log.d("FileUtil", "getExtSdCardFolder " + extSdPaths);
			try {
				final String canonicalPath = file.getCanonicalPath();
				for (int i = 0; i < extSdPaths.length; i++) {
					if (canonicalPath.startsWith(extSdPaths[i])) {
						return extSdPaths[i];
					}
				}
			} catch (IOException e) {
				return null;
			}
			return null;
		}

		/**
		 * Get a list of external SD card paths. (Kitkat or higher.)
		 *
		 * @return A list of external SD card paths.
		 */
		@TargetApi(Build.VERSION_CODES.KITKAT)
		public static String[] getExtSdCardPaths(Context context) {
			List<String> paths = new ArrayList<>();
			for (File file : context.getExternalFilesDirs("external")) {
				Log.d("FileUtil", "getExtSdCardPaths.file " + file);
				if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
					int index = file.getAbsolutePath().lastIndexOf("/Android/data");
					if (index < 0) {
						Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
					} else {
						String path = file.getAbsolutePath().substring(0, index);
						try {
							path = new File(path).getCanonicalPath();
						} catch (IOException e) {
							// Keep non-canonical path.
						}
						paths.add(path);
						Log.d("FileUtil", "getExtSdCardPaths " + path);
					}
				}
			}
			if (paths.isEmpty()) paths.add("/storage/sdcard1");
			return paths.toArray(new String[paths.size()]);
		}

		
		public static FolderInfo getFolderInfo(final List<File> fList, final OnProgressUpdate<String> updateState) {
			final FolderInfo ret = new FolderInfo();
			for (File st : fList) {
				final FolderInfo folderInfo = getFolderInfo(st, updateState);
				ret.size += folderInfo.size;
				ret.filesNum += folderInfo.filesNum;
				ret.foldersNum += folderInfo.foldersNum;
				ret.fileList.addAll(folderInfo.fileList);
			}
			return ret;
		}

		
		public static FolderInfo getFolderInfo(final File f, final OnProgressUpdate<String> updateState) {
			final long start = System.currentTimeMillis();
			Log.d("getFolderInfo", "f " + f);
			final FolderInfo ret = new FolderInfo();
			if (f == null) {
				return ret;
			}
			if (f.exists()) {
				final String oriPath = f.getParentFile().getAbsolutePath();
				final int oriPathLength = oriPath.length();
				final LinkedList<File> folderQueue = new LinkedList<File>();
				if (f.isDirectory()) {
					folderQueue.push(f);
				} else {
					ret.size += f.length();
					ret.filesNum++;
					ret.fileList.add(new ComparableEntry<File, String>(f, f.getAbsolutePath().substring(oriPathLength)));
				}
				File fi = null;
				File[] fs;
				if (updateState != null) {
					while (folderQueue.size() > 0) {
						fi = folderQueue.removeFirst();
						ret.fileList.add(new ComparableEntry<File, String>(fi, fi.getAbsolutePath().substring(oriPathLength)));
						fs = fi.listFiles();
						if (fs != null) {
							for (File f2 : fs) {
								if (f2.isDirectory()) {
									folderQueue.push(f2);
									ret.foldersNum++;
								} else {
									ret.size += f2.length();
									ret.filesNum++;
								}
								ret.fileList.add(new ComparableEntry<File, String>(f2, f2.getAbsolutePath().substring(oriPathLength)));
							}
							updateState.onUpdate(ret.toString());
						}
					}
				} else {
					while (folderQueue.size() > 0) {
						fi = folderQueue.removeFirst();
						ret.fileList.add(new ComparableEntry<File, String>(fi, fi.getAbsolutePath().substring(oriPathLength)));
						fs = fi.listFiles();
						if (fs != null) {
							for (File f2 : fs) {
								if (f2.isDirectory()) {
									folderQueue.push(f2);
									ret.foldersNum++;
								} else {
									ret.size += f2.length();
									ret.filesNum++;
								}
								ret.fileList.add(new ComparableEntry<File, String>(f2, f2.getAbsolutePath().substring(oriPathLength)));
							}
						}
					}
				}

			}
			Log.d("getFolderInfo f", f.getAbsolutePath() + ": " + ret + ", took " + (System.currentTimeMillis() - start));
			return ret;
		}


		public static TreeSet<String> getFileNameInFolder(File folder) {
			Log.d(TAG, "getFileNameInFolder " + folder.getAbsolutePath());
			final TreeSet<String> fList = new TreeSet<>();
			if (folder != null && folder.isDirectory()) {
				final LinkedList<File> folderQueue = new LinkedList<File>();
				folderQueue.push(folder);
				final String oriFolderPath = folder.getAbsolutePath();
				final int oriFolderPathLenght = oriFolderPath.length();
				File fi = null;
				File[] fs;
				while (folderQueue.size() > 0) {
					fi = folderQueue.removeFirst();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f2 : fs) {
							if (f2.isDirectory()) {
								folderQueue.push(f2);
							}
							fList.add(f2.getAbsolutePath().substring(oriFolderPathLenght));
						}
					}
				}
			}
			return fList;
		}
		
		
	}
	
	public static class FolderInfo {
		public long size = 0;
		public long filesNum = 0;
		public long foldersNum = 0;
		public List<ComparableEntry<File, String>> fileList = new LinkedList<>();

		@Override
		public String toString() {
			return size + " bytes, " + foldersNum + " folders, " + filesNum + " files";
		}
		
	}
	
}
