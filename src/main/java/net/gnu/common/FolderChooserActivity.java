package net.gnu.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.*;
import android.net.*;
import android.graphics.drawable.*;
import android.content.*;
import android.app.*;
import android.view.*;
import java.io.*;
import android.text.*;
import android.view.animation.*;
import android.os.*;
import android.util.*;
import net.gnu.androidutil.*;
import android.support.v4.content.FileProvider;
import java.util.regex.*;

import android.annotation.*;
import android.view.inputmethod.*;
import android.text.format.Formatter;
import java.util.*;
import net.gnu.common.FileInfo.*;
import net.gnu.common.FolderChooserActivity.*;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import net.gnu.agrep.R;
import android.content.res.TypedArray;

public class FolderChooserActivity extends StorageCheckActivity implements View.OnClickListener {

	private static final String TAG = "FolderChooserActivity";
	
	public static final int LIGHT_BROWN = 0xFFFFE6D9;
	public static final int LIGHT_YELLOW = 0xFFFFFFF0;
	public static final int LIGHT_YELLOW2 = 0xFFFFF8D9;
	public static final int LIGHT_YELLOW3 = 0xFFF7C0C1;

	//public static final String EXTRA_MULTI_SELECT = "org.openintents.extra.MULTI_SELECT";//"multiFiles";
    public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";
    public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";
    //public static final String ACTION_MULTI_SELECT = "org.openintents.action.MULTI_SELECT";

	public static final String EXTRA_FILTER_MIMETYPE = "org.openintents.extra.FILTER_MIMETYPE";
	public static final String EXTRA_FILTER_FILETYPE = "org.openintents.extra.FILTER_FILETYPE";
	//public static final String PREVIOUS_SELECTED_FILES = "net.gnu.explorer.selectedFiles";
	public static final String EXTRA_ABSOLUTE_PATH = "org.openintents.extra.ABSOLUTE_PATH";

//	public static final String EXTRA_ABSOLUTE_PATH = FolderChooserActivity.class.getPackage().getName() + ".selectedDir";
//	public static final String EXTRA_MULTI_SELECT = "multiFiles";
//	public static final String SUFFIX = "suffix";
	public static final String CHOOSER_TITLE = "chooserTitle";
	private String suffix = ".*"; // ".*" : files only,  "" folders only, "; *" split pattern, * filed&folder
	private boolean multiFiles = false;
	private String mimes = "";

	private final ArrayList<FileInfo> dataSourceL1 = new ArrayList<>();
	private final ArrayList<FileInfo> dataSourceL2 = new ArrayList<>();
	private final ArrayList<FileInfo> selectedInList1 = new ArrayList<>();
	private final ArrayList<FileInfo> selectedInList2 = new ArrayList<>();
	private final ArrayList<FileInfo> tempSelectedInList1 = new ArrayList<>();
	
	private String dir = "";
	
	private ImageButton allCbx = null;
	private TextView allName;
	private TextView allDate;
	private TextView allSize;
	private TextView allType;
	private TextView selectionStatus1;
	private TextView rightStatus;

	private ImageButton allCbx2 = null;
	private TextView allName2;
	private TextView allDate2;
	private TextView allSize2;
	private TextView allType2;
	private TextView selectionStatus2;

	private ListView listView1 = null;
	private ListView listView2 = null;
	private ArrayAdapter srcAdapter;
	private ArrayAdapter destAdapter;

	private FileObserver mFileObserver;
	private ImageThreadLoader imageLoader;

	private LinearLayout mDirectoryButtons;
	private HorizontalScrollView scrolltext;
	private ImageView superButton;
	private EditText searchET;
	private ViewFlipper topflipper;
	private View quickLayout;
	private View clearBtn;
	private TextSearch textSearch = new TextSearch();
	protected InputMethodManager imm;

	private String searchVal = "";
	private SearchFileNameTask searchTask = new SearchFileNameTask();
	private LoadFiles loadList;

	private View nofilelayout;
	private View nofilelayout2;
	private ImageView noFileImage;
	private TextView noFileText;

	private Pattern suffixPattern;

	private DoubleCompare fileListSorter;
	private DoubleCompare fileListSorter2;;
	private String order;
	private String order2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate " + savedInstanceState);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		//int mCurTheme = Integer.valueOf(AndroidUtils.getSharedPreferenceUri(this, "theme", android.R.style.Theme_Holo_Light + ""));
		//setTheme(mCurTheme);

//		Log.d("getApplication", getApplication() + ".");
//		Log.d("getApplicationInfo", getApplicationInfo() + ".");
//		Log.d("getApplicationContext", getApplicationContext() + ".");
//		Log.d("getWindow", getWindow() + ".");
//		Log.d("getWindowManager", getWindowManager() + ".");
//		Log.d("getDefaultDisplay", getWindowManager().getDefaultDisplay() + ".");
//		Log.d("getComponentName", getComponentName() + ".");
//		Log.d("getRequestedOrientation()", getRequestedOrientation() + ".");

		if (AndroidUtils.isPortrait(this)) {
			setContentView(R.layout.activity_folder_chooser_vertical);
		} else {
			setContentView(R.layout.activity_folder_chooser);
		}

		allCbx = (ImageButton) findViewById(R.id.allCbx);
		allName = (TextView) findViewById(R.id.allName);
		allDate = (TextView) findViewById(R.id.allDate);
		allSize = (TextView) findViewById(R.id.allSize);
		allType = (TextView) findViewById(R.id.allType);
		selectionStatus1 = (TextView) findViewById(R.id.selectionStatus);
		rightStatus = (TextView) findViewById(R.id.diskStatus);

		allCbx2 = (ImageButton) findViewById(R.id.allCbx2);
		allName2 = (TextView) findViewById(R.id.allName2);
		allDate2 = (TextView) findViewById(R.id.allDate2);
		allSize2 = (TextView) findViewById(R.id.allSize2);
		allType2 = (TextView) findViewById(R.id.allType2);
		selectionStatus2 = (TextView) findViewById(R.id.selectionStatus2);
		clearBtn = findViewById(R.id.clear);

		nofilelayout = findViewById(R.id.nofilelayout);
		nofilelayout2 = findViewById(R.id.nofilelayout2);
		noFileImage = (ImageView)findViewById(R.id.image);
		noFileText = (TextView)findViewById(R.id.nofiletext);

		listView1 = (ListView) findViewById(R.id.files);
		srcAdapter = new ArrayAdapter(this, R.layout.list_item, dataSourceL1);
		listView1.setAdapter(srcAdapter);
		
		//listView1.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		//listView1.setSmoothScrollbarEnabled(true);
        //listView1.setScrollingCacheEnabled(true);
        //listView1.setFocusable(true);
        //listView1.setFocusableInTouchMode(true);
        listView1.setFastScrollEnabled(true);
		listView1.setSelector(R.drawable.ripple);
		
		listView2 = (ListView) findViewById(R.id.selectedFiles);
		listView2.setFastScrollEnabled(true);
		//listView2.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		listView2.setSelector(R.drawable.ripple);
		clearBtn.setOnClickListener(this);

//		tf = Typeface.createFromAsset(getAssets(), "fonts/DejaVuSerifCondensed.ttf");

		scrolltext = (HorizontalScrollView) findViewById(R.id.scroll_text);
		mDirectoryButtons = (LinearLayout) findViewById(R.id.directory_buttons);
		superButton = (ImageView) findViewById(R.id.superButton);
		searchET = (EditText) findViewById(R.id.search_box);
		topflipper = (ViewFlipper) findViewById(R.id.flipper_top);
		quickLayout = findViewById(R.id.quicksearch);
		searchET.addTextChangedListener(textSearch);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		((ImageButton)findViewById(R.id.icon)).setColorFilter(Color.parseColor("#ff666666"));
		((ImageButton)findViewById(R.id.icon2)).setColorFilter(Color.parseColor("#ff666666"));

		final Intent intent = getIntent();
		suffix = intent.getStringExtra(FolderChooserActivity.EXTRA_FILTER_FILETYPE);
		suffix = (suffix == null) ? suffix = "*" : suffix.trim();

		multiFiles = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
			&& intent.getBooleanExtra("android.intent.action.MULTIPLE_PICK", true);//intent.getBooleanExtra(FolderChooserActivity.EXTRA_MULTI_SELECT, true)

		final String type = intent.getType();
		mimes = type;//intent.getStringExtra(FolderChooserActivity.EXTRA_FILTER_MIMETYPE);
		mimes = mimes == null ? "*/*" : mimes.trim().toLowerCase();

		final String action = intent.getAction();
		final ActionBar actionBar = getActionBar();
		if (Intent.ACTION_MAIN.equals(action)) {
			suffix = "*";
			actionBar.hide();
		} else {
			final View customView = getLayoutInflater().inflate(R.layout.filechoosertoolbar, null);
			actionBar.setCustomView(customView);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.show();
			
			if (Intent.EXTRA_ALLOW_MULTIPLE.equals(action)
				|| "android.intent.action.MULTIPLE_PICK".equals(action)) {//both dir & file
				if (suffix.length() == 0) {
					suffix = "*";
				}
			} else if (FolderChooserActivity.ACTION_PICK_DIRECTORY.equals(action)) {//dir
				suffix = "";
			} else if (FolderChooserActivity.ACTION_PICK_FILE.equals(action)) {//file
				if (suffix.length() == 0) {
					suffix = ".*";
				}
			} else if (Intent.ACTION_GET_CONTENT.equals(action)) {
				if (!intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)) {
					multiFiles = false;
					if (suffix.length() == 0) {
						suffix = ".*";
					}
				} else {
					suffix = ".*";
				}
			}
		}

		if ("*".equals(suffix) || ".*".equals(suffix)) {
			suffixPattern = Pattern.compile(".+");
		} else {
			String suffixSpliter = suffix.replaceAll("[;\\s\\*]+", "\b");
			suffixSpliter = suffixSpliter.replaceAll(Util.SPECIAL_CHAR_PATTERNSTR, "\\\\$1");
			suffixSpliter = suffixSpliter.startsWith("\b") ? suffixSpliter.substring(1) : suffixSpliter;
			suffixSpliter = ".*?(" + suffixSpliter.replaceAll("\b", "|") + ")";
			suffixPattern = Pattern.compile(suffixSpliter);
		}
		final Uri dataString = intent.getData();
		Log.d(TAG, "intent.data=" + dataString);
		final String scheme = intent.getScheme();
		Log.d(TAG, "intent.scheme=" + scheme);
		Log.d(TAG, "intent.component=" + intent.getComponent());
		Log.d(TAG, "intent.package=" + intent.getPackage());
		Log.d(TAG, "intent.type=" + type);
		Log.d(TAG, "suffix=" + suffix + ", suffixSpliter=" + suffixPattern + ", mime=" + mimes + ", multi=" + multiFiles);
		Log.d(TAG, "action=" + action + ", cat=" + intent.getCategories() + ", extra=" + intent.getExtras() + ", intent " + intent);

		if (dataString != null && "file".equals(scheme)) {
			final String path = dataString.getPath();
			final File f = new File(path);
			dataSourceL2.add(new FileInfo(f));
			if (f.isFile()) {
				dir = f.getParent();
			} else {
				dir = path;
			}
			multiFiles = false;
		}

		final ClipData clip = intent.getClipData();
		if (clip != null) {
			final int itemCount = clip.getItemCount();
			dataSourceL2.clear();
			for (int i = 0; i < itemCount; i++) {
				final Uri uri = clip.getItemAt(i).getUri();
				//Log.d(TAG, "clip " + i + "=" + uri);
				dataSourceL2.add(new FileInfo(uri.getPath()));
			}
			selectionStatus2.setText("0/" + dataSourceL2.size());
			multiFiles = true;
		}
		if (multiFiles && !Intent.ACTION_MAIN.equals(action)) {
			destAdapter = new ArrayAdapter(this, R.layout.list_item, dataSourceL2);
			listView2.setAdapter(destAdapter);
			destAdapter.setup("", selectedInList2, null, multiFiles, allCbx2);
		} else {
			listView2.setVisibility(View.GONE);
			allCbx2.setVisibility(View.GONE);
			allName2.setVisibility(View.GONE);
			allType2.setVisibility(View.GONE);
			allDate2.setVisibility(View.GONE);
			allSize2.setVisibility(View.GONE);
			selectionStatus2.setVisibility(View.GONE);

			findViewById(R.id.commands).setVisibility(View.GONE);
			findViewById(R.id.frame2).setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider5).setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider8).setVisibility(View.GONE);
			findViewById(R.id.horizontalDivider11).setVisibility(View.GONE);
			findViewById(R.id.rightPane).setVisibility(View.GONE);
		}
		if (savedInstanceState != null) {
			suffix = savedInstanceState.getString(EXTRA_FILTER_FILETYPE, "*");
			mimes = savedInstanceState.getString(FolderChooserActivity.EXTRA_FILTER_MIMETYPE, "");
			multiFiles = savedInstanceState.getBoolean(Intent.EXTRA_ALLOW_MULTIPLE, true);
			dir = savedInstanceState.getString(FolderChooserActivity.EXTRA_ABSOLUTE_PATH);

			searchMode = savedInstanceState.getBoolean("searchMode", false);
			if (searchMode) {
				searchET.removeTextChangedListener(textSearch);
				manageUI();
				searchET.setText(savedInstanceState.getString("searchVal", ""));
				searchET.addTextChangedListener(textSearch);
			}

			dataSourceL1.clear();
			dataSourceL1.addAll((ArrayList<FileInfo>)savedInstanceState.getParcelableArrayList("dataSourceL1"));

			selectedInList1.clear();
			selectedInList1.addAll((ArrayList<FileInfo>)savedInstanceState.getParcelableArrayList("selectedInList1"));
			srcAdapter.notifyDataSetChanged();
			allCbx.setSelected(savedInstanceState.getBoolean("allCbx", false));
			if (allCbx.isSelected()) {
				allCbx.setImageResource(R.drawable.ic_accept);
			} else {
				allCbx.setImageResource(R.drawable.dot);
			}
			if (multiFiles && !Intent.ACTION_MAIN.equals(action)) {
				final ArrayList<FileInfo> stringArrayList = savedInstanceState.getParcelableArrayList("selectedFiles");
				for (FileInfo fn : stringArrayList) {
					if (!dataSourceL2.contains(fn)) 
						dataSourceL2.add(fn);
				}
				selectedInList2.addAll((ArrayList<FileInfo>)savedInstanceState.getParcelableArrayList("selectedInList2"));
				//Log.d("selectedInList2", selectedInList2.toString());
				destAdapter.notifyDataSetChanged();
				allCbx2.setSelected(savedInstanceState.getBoolean("allCbx2", false));
				if (allCbx2.isSelected()) {
					allCbx2.setImageResource(R.drawable.ic_accept);
				} else {
					allCbx2.setImageResource(R.drawable.dot);
				}
			}
		} 

		Log.d(TAG, "onCreate, dir=" + dir);
		if (savedInstanceState == null) {
			if (dir == null || dir.trim().length() == 0) {
				final File f = Environment.getExternalStorageDirectory();
				dir = f.getAbsolutePath();
			} 
			changeDir(dir, false);
		} else {
			mFileObserver = createFileObserver(dir);
			setDirectoryButtons();
		}
		order = AndroidUtils.getSharedPreference(this, "FolderChooserActivity.order", "Name ▲");
		allName.setText("Name");
		allSize.setText("Size   ");
		allDate.setText("Date");
		allType.setText("Type");
		switch (order) {
			case "Name ▼":
				fileListSorter = new FileInfo.SortFolderNameFirstDecrease();
				allName.setText("Name ▼");
				break;
			case "Date ▲":
				fileListSorter = new FileInfo.SortFileDateIncrease();
				allDate.setText("Date ▲");
				break;
			case "Date ▼":
				fileListSorter = new FileInfo.SortFileDateDecrease();
				allDate.setText("Date ▼");
				break;
			case "Size ▲":
				fileListSorter = new FileInfo.SortFileSizeIncrease();
				allSize.setText("Size ▲");
				break;
			case "Size ▼":
				fileListSorter = new FileInfo.SortFileSizeDecrease();
				allSize.setText("Size ▼");
				break;
			case "Type ▲":
				fileListSorter = new FileInfo.SortFileTypeIncrease();
				allType.setText("Type ▲");
				break;
			case "Type ▼":
				fileListSorter = new FileInfo.SortFileTypeDecrease();
				allType.setText("Type ▼");
				break;
			default:
				fileListSorter = new FileInfo.SortFolderNameFirstIncrease();
				allName.setText("Name ▲");
				break;
		}
		order2 = AndroidUtils.getSharedPreference(this, "FolderChooserActivity.order2", "Name ▲");
		allName2.setText("Name");
		allSize2.setText("Size   ");
		allDate2.setText("Date");
		allType2.setText("Type");
		switch (order2) {
			case "Name ▼":
				fileListSorter2 = new FileInfo.SortFolderNameFirstDecrease();
				allName2.setText("Name ▼");
				break;
			case "Date ▲":
				fileListSorter2 = new FileInfo.SortFileDateIncrease();
				allDate2.setText("Date ▲");
				break;
			case "Date ▼":
				fileListSorter2 = new FileInfo.SortFileDateDecrease();
				allDate2.setText("Date ▼");
				break;
			case "Size ▲":
				fileListSorter2 = new FileInfo.SortFileSizeIncrease();
				allSize2.setText("Size ▲");
				break;
			case "Size ▼":
				fileListSorter2 = new FileInfo.SortFileSizeDecrease();
				allSize2.setText("Size ▼");
				break;
			case "Type ▲":
				fileListSorter2 = new FileInfo.SortFileTypeIncrease();
				allType2.setText("Type ▲");
				break;
			case "Type ▼":
				fileListSorter2 = new FileInfo.SortFileTypeDecrease();
				allType2.setText("Type ▼");
				break;
			default:
				fileListSorter2 = new FileInfo.SortFolderNameFirstIncrease();
				allName2.setText("Name ▲");
				break;
		}
		srcAdapter.setup(dir, selectedInList1, dataSourceL2, multiFiles, allCbx);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
    private static String[] getRootMemoryPaths(final Context context) {
        final List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
			final String absolutePath = file.getAbsolutePath();
			Log.d(TAG, "getExtSdCardPaths " + absolutePath);
            if (file != null) {
                final int index = absolutePath.lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = absolutePath.substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
						paths.add(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (paths.isEmpty()) 
			paths.add(System.getenv("EXTERNAL_STORAGE"));
        return paths.toArray(new String[0]);
    }

	@Override
	public void onClick(View p1) {
		switch (p1.getId()) {
			case R.id.clear: 
				searchET.setText("");
				break;
		}
	}

	public void dirMore(final View v) {
		final PopupMenu popup = new PopupMenu(v.getContext(), v);
		popup.getMenuInflater().inflate(R.menu.dir_more, popup.getMenu());
		final String[] extSdPaths = getRootMemoryPaths(this);
		try {
			int i = 1;
			final List<MenuItem> li = new LinkedList<>();
			for (String mi : extSdPaths) {
				li.add(popup.getMenu().add(i, i, i, mi));
				i++;
			}

			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						final int id = item.getItemId();
						switch (id)  {
							case R.id.newFolder:
								final EditText editText = new EditText(FolderChooserActivity.this);
								AlertDialog dialog = new AlertDialog.Builder(FolderChooserActivity.this)
									.setIconAttribute(android.R.attr.dialogIcon)
									.setTitle("New Folder")
									.setView(editText)
									.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
											String name = editText.getText().toString();
											File f = new File(dir, name);
											if (f.exists()) {
												showToast("\"" + f + "\" is existing. Please choose another name");
											} else {
												boolean ok = f.mkdirs();
												if (ok) {
													showToast(f + " was created successfully");
												} else {
													showToast(f + " can't be created");
												}
												dialog.dismiss();
											}
										}
									})
									.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
											dialog.dismiss();
										}
									}).create();
								dialog.show();
								break;
							case R.id.newFile:
								break;
							case R.id.filter:
								final EditText filterText = new EditText(FolderChooserActivity.this);
								filterText.setText(suffix);
								AlertDialog filterDialog = new AlertDialog.Builder(FolderChooserActivity.this)
									.setIconAttribute(android.R.attr.dialogIcon)
									.setTitle("Filter file extentions (* for all files)")
									.setView(filterText)
									.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
											String suffixSpliter = filterText.getText().toString();
											Log.d(TAG, "suffixSpliter=" + suffixSpliter);
											if ("*".equals(suffixSpliter)) {
												suffixPattern = Pattern.compile(".+");
											} else {
												suffixSpliter = suffixSpliter.replaceAll("[;\\s\\*]+", "\b");
												Log.d(TAG, "suffixSpliter1=" + suffixSpliter);
												suffixSpliter = suffixSpliter.replaceAll(Util.SPECIAL_CHAR_PATTERNSTR, "\\\\$1");
												Log.d(TAG, "suffixSpliter2=" + suffixSpliter);
												suffixSpliter = suffixSpliter.startsWith("\b") ? suffixSpliter.substring(1) : suffixSpliter;
												suffixSpliter = ".*?(" + suffixSpliter.replaceAll("\b", "|") + ")";
												Log.d(TAG, "suffixSpliter3=" + suffixSpliter);
												suffixPattern = Pattern.compile(suffixSpliter);
											}
											if (!searchMode) {
												changeDir(dir, true);
											}
											dialog.dismiss();
										}
									})
									.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
											dialog.dismiss();
										}
									}).create();
								filterDialog.show();
								break;
						}
						for (MenuItem mi : li) {
							if (id == mi.getItemId()) {
								changeDir(mi.getTitle() + "", true);
								return true;
							}
						}
						return true;
					}
				});
			popup.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it", "present.
		//getMenuInflater().inflate(R.menu.folder_chooser, menu);
		return true;
	}
//				for (int i = 0; i < size;i++) {
//					final Drawable icon = menuBuilder.getItem(i).getIcon();
//					icon.setFilterBitmap(true);
//					icon.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//				}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState " + outState);

		outState.putString(FolderChooserActivity.EXTRA_ABSOLUTE_PATH, dir);//EXTRA_DIR_PATH
		outState.putString(FolderChooserActivity.EXTRA_FILTER_FILETYPE, suffix);
		outState.putString(FolderChooserActivity.EXTRA_FILTER_MIMETYPE, mimes);
		outState.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, multiFiles);
		//outState.putStringArray(FolderChooserActivity.PREVIOUS_SELECTED_FILES, previousSelectedStr);
		outState.putBoolean("allCbx", allCbx.isSelected());
		outState.putBoolean("allCbx2", allCbx2.isSelected());

		outState.putBoolean("searchMode", searchMode);
		outState.putString("searchVal", searchVal);
		outState.putParcelableArrayList("selectedInList1", selectedInList1);
		outState.putParcelableArrayList("dataSourceL1", dataSourceL1);

		if (multiFiles) {
			outState.putParcelableArrayList("selectedFiles", dataSourceL2);
			//Log.d("selectedInList2", selectedInList2.toString());
			outState.putParcelableArrayList("selectedInList2", selectedInList2);
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(TAG, "onRestoreInstanceState " +savedInstanceState);
	}

	public void showToast(final CharSequence st) {
		Toast.makeText(this, st, Toast.LENGTH_LONG).show();
	}

    /**
     * Sets up a FileObserver to watch the current directory.
     */
    private FileObserver createFileObserver(final String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
								| FileObserver.MOVED_FROM | FileObserver.MOVED_TO
								| FileObserver.DELETE_SELF | FileObserver.MOVE_SELF) {
            @Override
            public void onEvent(final int event, final String path) {
                if (path != null) {
                    Util.debug(TAG, "FileObserver received event %d, CREATE = 256;DELETE = 512;DELETE_SELF = 1024;MODIFY = 2;MOVED_FROM = 64;MOVED_TO = 128; path %s", event, path);
					FolderChooserActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								refreshDirectory();
							}
						});
                }
            }
        };
    }

    /**
     * Refresh the contents of the directory that", "currently shown.
     */
    private void refreshDirectory() {
		changeDir(dir, false);
    }

	@Override
    public void onPause() {
        super.onPause();
		Log.d(TAG, "onPause");
		if (imageLoader != null) {
			imageLoader.stopThread();
		}
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
		Log.d(TAG, "onResume");
		imageLoader = new ImageThreadLoader(this, 36, 40);
		if (dir != null) {
			mFileObserver = createFileObserver(dir);
			mFileObserver.startWatching();
		}
    }

	private static final long TIME_INTERVAL = 250000000L;
	private long mBackPressed = System.nanoTime();
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		if (mBackPressed + TIME_INTERVAL >= System.nanoTime()) {
			super.onBackPressed();
		} else {
			mBackPressed = System.nanoTime();
		}
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyLongPress.keyCode=" + keyCode + ", event=" + event);
		if (keyCode == KeyEvent.KEYCODE_BACK
			&& event.getAction() == KeyEvent.ACTION_DOWN) {
			super.onBackPressed();
			return true;
		}
		return false;
	}

    public void changeDir(final String curDir, final boolean doScroll) {
		Log.i(TAG, "changeDir " + curDir + ", doScroll " + doScroll);

		if (loadList != null) {
			loadList.cancel(true);
		}
		searchTask.cancel(true);
		selectionStatus1.setText("");
		rightStatus.setText("");
		synchronized (dataSourceL1) {
			loadList = new LoadFiles(curDir, doScroll);
			loadList.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		}
	}

	private class LoadFiles extends AsyncTask<Void, Object, Void> {

		private final boolean toIndex0;
		private final String path;
		private int size = 0;
		private long start;

		public LoadFiles(final String path, final boolean toIndex0) {
			this.toIndex0 = toIndex0;
			this.path = path;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, "LoadFiles.PreExecute " + dir);
			synchronized (dataSourceL1) {
				dataSourceL1.clear();
				selectedInList1.clear();
				srcAdapter.notifyDataSetChanged();
			}
			if (toIndex0) {
				listView1.scrollTo(0,0);
			}
			allCbx.setEnabled(false);
			start = System.nanoTime();
		}

		@Override
		protected void onCancelled() {
			synchronized (dataSourceL1) {
				Log.d(TAG, "LoadFiles.Cancelled " + dir);
				dataSourceL1.clear();
				selectedInList1.clear();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "LoadFiles.Background suffix=" + suffix + ", suffixPattern=" + suffixPattern + ", mimes=" + mimes + ", multi=" + multiFiles + ", path " + path);
			synchronized (dataSourceL1) {
				prevUpdate = System.nanoTime();
				ArrayList<FileInfo> dataSourceL1a = new ArrayList<>(1024);

				if (dir == null) {
					return null;//dataSourceL1a;
				}
				
				try {
					File curDir = new File(path);
					while (!curDir.exists() || curDir.isFile()) {
						publishProgress(curDir.getAbsolutePath() + " is not a existing folder");
						curDir = curDir.getParentFile();
					}
					if (curDir == null) {
						publishProgress("Current directory is not existed. Change to root");
						curDir = new File("/");
					}
					dir = curDir.getAbsolutePath();
					Log.d(TAG, "dir " + dir);

					if (mFileObserver != null) {
						mFileObserver.stopWatching();
					}
					mFileObserver = createFileObserver(dir);
					mFileObserver.startWatching();

					final File[] files = curDir.listFiles();
					size = files.length;
					Arrays.sort(files, fileListSorter.getFileComparator());
					String fName;
					String mi;
					int idx;
					//Log.d(TAG, "suffix=" + suffix + ", suffixPattern=" + suffixPattern);
					for (File f : files) {
						fName = f.getName();
						//Log.d(TAG, mimes.endsWith("*") + ", " + mimes.split("/")[0].equals((FileUtil.getMimeType(f) + "").split("/")[0]) + ", mime=" + mimes + ", suffix=" + suffix + ", getMimeType=" + FileUtil.getMimeType(f) + ", suffixPattern=" + suffixPattern + ", name=" + fName);
						if (f.isDirectory()) {
							dataSourceL1a.add(new FileInfo(f));
						} else if (suffix.length() > 0) {//!mDirectoriesOnly
							if ((
								mimes.equals("*/*") || 
								mimes.equals((mi = FileUtil.getMimeType(fName))) ||
								mimes.endsWith("*") && (idx = mimes.indexOf("/")) == mi.indexOf("/") 
								&& mimes.regionMatches(0, mi, 0, idx))
								&& suffixPattern.matcher(fName).matches()) {
								dataSourceL1a.add(new FileInfo(f));
							} 
						}
						final long present = System.nanoTime();
						if (present - prevUpdate > 200000000 && !busyNoti) {
							prevUpdate = present;
							//Log.d(TAG, "publishProgress 1 " + dir + " " + dataSourceL1a.size());
							publishProgress(dataSourceL1a);
							dataSourceL1a = new ArrayList<>(1024);
						}
					}
					//Log.d(TAG, "publishProgress 2 " + dir + " " + dataSourceL1a.size());
					publishProgress(dataSourceL1a);
				} catch (Throwable e) {
					publishProgress(getString(R.string.rootfailure));
					e.printStackTrace();
					return null;//dataSourceL1a;
				}
				return null;//dataSourceL1a;
			}
		}

		public volatile long prevUpdate = 0;
		public volatile boolean busyNoti = false;

		@Override
		public void onProgressUpdate(final Object... message) {
			if (message != null) {
				if (message[0] instanceof String) {
					Log.d(TAG, "onProgressUpdate " + message[0] + " " + dir);
					showToast("" + message[0]);
				} else {
					busyNoti = true;
					//Log.d(TAG, "onProgressUpdate addAll element " + dir);
					final ArrayList<FileInfo> dataSourceL1a = (ArrayList<FileInfo>)message[0];
					dataSourceL1.addAll(dataSourceL1a);
					//Collections.sort(dataSourceL1, fileListSorter);

					srcAdapter.notifyDataSetChanged();//.notifyItemRangeInserted(lastSize, ((ArrayList)message[0]).size());
					busyNoti = false;
					selectionStatus1.setText(dataSourceL1.size() + "/" + size);
				}
			} 
		}

		@Override
		protected void onPostExecute(Void v) {
			Log.i(TAG, "LoadFiles.Post, took " + Util.nf.format(System.nanoTime() - start) + ", size " + dataSourceL1.size() + ", dir " + dir);
			if (multiFiles) {
				boolean allInclude = (srcAdapter.dataSourceL2.size() > 0 && dataSourceL1.size() > 0) ? true : false;
				if (allInclude) {
					for (FileInfo st : dataSourceL1) {
						if (!srcAdapter.dataSourceL2.contains(st)) {
							allInclude = false;
							break;
						}
					}
				}
				if (allInclude) {
					allCbx.setSelected(true);
					allCbx.setImageResource(R.drawable.ic_accept);
					allCbx.setEnabled(false);
				} else {
					allCbx.setSelected(false);
					allCbx.setImageResource(R.drawable.dot);
					allCbx.setEnabled(true);
				}
			}
			setDirectoryButtons();
		}
	}

	public class SearchFileNameTask extends AsyncTask<String, Object, ArrayList<FileInfo>> {

		protected void onPreExecute() {
			super.onPreExecute();
			searchVal = searchET.getText().toString();
			//showToast("Searching...");
			dataSourceL1.clear();
			selectedInList1.clear();
			srcAdapter.notifyDataSetChanged();
			selectionStatus1.setText("");
		}

		@Override
		protected ArrayList<FileInfo> doInBackground(String... params) {
			Log.d("SearchFileNameTask", "dir " + dir);
			final ArrayList<FileInfo> tempAppList = new ArrayList<>();
//			if (type == Frag.TYPE.SELECTION || openMode == OpenMode.CUSTOM) {
//				//final Collection<LayoutElement> c = 
//				FileUtil.getFilesBy(tempOriDataSourceL1, params[0], true, this);
//				//Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
//				//tempAppList.addAll(c);
//			} else {
			File file = new File(dir);
			if (file.exists()) {
				//Collection<File> c = 
				FileInfo.getFilesBy(file.listFiles(), params[0], this);
				//Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
//					for (File le : c) {
//						tempAppList.add(new LayoutElement(le));
//					}
				//addAllDS1(Util.collectionFile2CollectionString(c));// dataSourceL1.addAll(Util.collectionFile2CollectionString(c));curContentFrag.
				// Log.d("dataSourceL1 new task",
				// Util.collectionToString(dataSourceL1, true, "\n"));
			} else {
				publishProgress(dir + " is not existed");
			}
			//}
			return tempAppList;
		}

		public boolean busyNoti = false;

		public void publish(final Object... message) {
			publishProgress(message);
		}

		@Override
		public void onProgressUpdate(final Object... message) {
			Log.d(TAG, "onProgressUpdate " + message[0]);
			if (message != null) {
				if (message[0] instanceof String) {
					showToast("" + message[0]);
				} else {
					busyNoti = true;
					dataSourceL1.addAll((ArrayList<FileInfo>)message[0]);
					srcAdapter.notifyDataSetChanged();
					busyNoti = false;
					selectionStatus1.setText(selectedInList1.size() + "/" + dataSourceL1.size());
				}
			} 
		}

		@Override
		protected void onPostExecute(ArrayList<FileInfo> result) {
			//dataSourceL1.addAll(result);
			srcAdapter.dirStr = null;
			Collections.sort(dataSourceL1, fileListSorter);
			srcAdapter.notifyDataSetChanged();
			selectionStatus1.setText(selectedInList1.size() + "/" + dataSourceL1.size());
			File file = new File(dir);
			rightStatus.setText(
				"Free " + Formatter.formatFileSize(FolderChooserActivity.this, file.getFreeSpace())
				+ ". Used " + Formatter.formatFileSize(FolderChooserActivity.this, file.getTotalSpace() - file.getFreeSpace())
				+ ". Total " + Formatter.formatFileSize(FolderChooserActivity.this, file.getTotalSpace()));
			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
			} else {
				nofilelayout.setVisibility(View.GONE);
			}
		}
	}


	private class TextSearch implements TextWatcher {
		public void beforeTextChanged(CharSequence s, int start, int end, int count) {
		}

		public void afterTextChanged(final Editable text) {
			if (searchMode) {
				searchVal = text.toString();
				Log.d(TAG, "searchVal " + searchVal);
				if (searchVal.length() > 0) {
					searchTask.cancel(true);
					searchTask = new SearchFileNameTask();
					searchTask.execute(searchVal);
				}
			}
		}

		public void onTextChanged(CharSequence s, int start, int end, int count) {
		}
	}

	private boolean searchMode = false;
	public void manageUI() {

		searchET.setHint("Search " + new File(dir).getName());
		searchET.addTextChangedListener(textSearch);

		if (searchMode == true) {
			superButton.setImageResource(R.drawable.back_icon);
			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
			searchET.requestFocus();
			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
			topflipper.setAnimation(launchAnimation(R.anim.fade));
		} else {
			imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
			searchET.setText("");
			superButton.setImageResource(R.drawable.ic_menu_search);
			topflipper.setAnimation(launchAnimation(R.anim.appear));
			topflipper.setDisplayedChild(topflipper.indexOfChild(scrolltext));
			refreshDirectory();
		}
	}

	private Animation launchAnimation(int id) {
		return AnimationUtils.loadAnimation(FolderChooserActivity.this, id);
	}

	public void superButton(View v) {
		searchMode = !searchMode;
		manageUI();
	}

	private void setDirectoryButtons() {
		mDirectoryButtons.removeAllViews();
		
		selectionStatus1.setText("0/" + dataSourceL1.size());
		if (dataSourceL1.size() == 0) {
			nofilelayout.setVisibility(View.VISIBLE);
		} else {
			nofilelayout.setVisibility(View.GONE);
		}
		if (dir != null) {
			if (dir.startsWith("/")) {
				final File curDir = new File(dir);
				rightStatus.setText(
					"Free " + Formatter.formatFileSize(FolderChooserActivity.this, curDir.getFreeSpace())
					+ ". Used " + Formatter.formatFileSize(FolderChooserActivity.this, curDir.getTotalSpace() - curDir.getFreeSpace())
					+ ". Total " + Formatter.formatFileSize(FolderChooserActivity.this, curDir.getTotalSpace()));
			}
		}
		final OnLongClickListener onLongClick = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View p1) {
				final EditText editText = new EditText(FolderChooserActivity.this);
				final CharSequence clipboardData = AndroidUtils.getClipboardData(FolderChooserActivity.this);
				if (clipboardData.length() > 0 && clipboardData.charAt(0) == '/') {
					editText.setText(clipboardData);
				}
				else {
					editText.setText(dir);
				}
				final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.gravity = Gravity.CENTER;
				editText.setLayoutParams(layoutParams);
				editText.setSingleLine(true);
				editText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
				editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				editText.setMinEms(2);
				//editText.setGravity(Gravity.CENTER);
				final int density = 8 * (int)getResources().getDisplayMetrics().density;
				editText.setPadding(density, density, density, density);

				AlertDialog dialog = new AlertDialog.Builder(FolderChooserActivity.this)
					.setIconAttribute(android.R.attr.dialogIcon)
					.setTitle("Go to...")
					.setView(editText)
					.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String name = editText.getText().toString();
							Log.d(TAG, "new " + name);
							File newF = new File(name);
							if (newF.exists()) {
								if (newF.isDirectory()) {
									dir = name;
								}
								else {
									dir = newF.getParent();
								}
								changeDir(dir, true);
								dialog.dismiss();
							} else {
								showToast("\"" + newF + "\" does not exist. Please choose another name");
							}
						}
					})
					.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
						}
					}).create();
				dialog.show();
				return true;
			}
		};

		final String[] parts = dir.split("/");
		final TextView ib = new TextView(this);
		ib.setOnLongClickListener(onLongClick);
		ib.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		//ib.setImageResource(R.drawable.home);
		ib.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        ib.setText("/");
		ib.setTag("/");
		ib.setMinEms(1);
		//ib.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		ib.setGravity(Gravity.CENTER_HORIZONTAL);
		ib.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					changeDir("/", true);
				}
			});
		mDirectoryButtons.addView(ib);

		String folder = "";
		TextView b = null;
		for (int i = 1; i < parts.length; i++) {
			folder += "/" + parts[i];
			View v = this.getLayoutInflater().inflate(R.layout.dir, null);
			b = (TextView) v.findViewById(R.id.name);
			b.setText(parts[i]);
			b.setTag(folder);
			b.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						String dir2 = (String) view.getTag();
						changeDir(dir2, true);
					}
				});
			mDirectoryButtons.addView(v);
			scrolltext.postDelayed(new Runnable() {
					public void run() {
						HorizontalScrollView hv = (HorizontalScrollView) findViewById(R.id.scroll_text);
						hv.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
					}
				}, 100L);
		}
		if (b != null) {
			b.setOnLongClickListener(onLongClick);
		}
	}

	private boolean firstSelection = true;
	public void mainmenu2(final View v) {
	}
	public void mainmenu(final View v) {
		final PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.allfiles, popup.getMenu());
		final Menu menu = popup.getMenu();
		
		MenuItem mi = menu.findItem(R.id.clearSelection);
		if (selectedInList1.size() == 0) {
			mi.setVisible(false);
		} else {
			mi.setVisible(true);
		}
		
		mi = menu.findItem(R.id.rangeSelection);
		if (selectedInList1.size() > 1) {
			mi.setVisible(true);
		} else {
			mi.setVisible(false);
		}
		
		if (firstSelection) {
			mi = menu.findItem(R.id.undoClearSelection);
			mi.setVisible(false);
			firstSelection = false;
		} 
		
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					Log.d(TAG, "menu " + item.getTitle() + ", id " + item.getItemId() + ", R.id.inversion " + R.id.inversion);
					switch (item.getItemId()) {
						case R.id.delete:
							deletes(v);
							break;
						case R.id.send:
							sends(v);
							break;
						case R.id.copy:
							copys(v);
							break;
						case R.id.cut:
							cuts(v);
							break;
						case R.id.paste:
							pastes(v);
							break;
						case R.id.clearSelection:
							clear();
							break;
						case R.id.inversion:
							inversion();
							break;
						case R.id.rangeSelection:
							rangeSelection();
							break;
						case R.id.undoClearSelection:
							undoSelection();
							break;
					}
					return true;
				}
			});
		popup.show();
	}

	ArrayList<FileInfo> copyl = new ArrayList<>();
	ArrayList<FileInfo> cutl = new ArrayList<>();

	public boolean copys(final View v) {
		copyl.clear();
		cutl.clear();
		if (v.getId() == R.id.icon) {
			copyl.addAll(selectedInList1);
		} else {
			copyl.addAll(selectedInList2);
		}
		return true;
	}

	public boolean cuts(final View v) {
		copyl.clear();
		cutl.clear();
		if (v.getId() == R.id.icon) {
			cutl.addAll(selectedInList1);
		} else {
			cutl.addAll(selectedInList2);
		}
		return true;
	}

	public boolean pastes(final View v) {

		return true;
	}

	public boolean renames(final View v) {
		return true;
	}

	public boolean compresss(final View v) {
		return true;
	}

	public boolean encrypts(final View v) {
		return true;
	}

	boolean clear() {
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(selectedInList1);
		selectedInList1.clear();
		srcAdapter.notifyDataSetChanged();
		allCbx.setSelected(false);
		allCbx.setImageResource(R.drawable.dot);
		selectionStatus1.setText("0/"+ srcAdapter.getCount());
		return true;
	}

	void inversion() {
		Log.d(TAG, "inversion1 " + selectedInList1.size());
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(selectedInList1);
		final ArrayList<FileInfo> listTemp = new ArrayList<>(4096);
		for (FileInfo f : dataSourceL1) {
			if (!selectedInList1.contains(f)) {
				listTemp.add(f);
			}
		}
		selectedInList1.clear();
		selectedInList1.addAll(listTemp);
		Log.d(TAG, "inversion2 " + selectedInList1.size());
		srcAdapter.notifyDataSetChanged();
		final int size = selectedInList1.size();
		if (size == dataSourceL1.size()) {
			allCbx.setSelected(true);
			allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			allCbx.setSelected(false);
			allCbx.setImageResource(R.drawable.dot);
		}
		selectionStatus1.setText(size + "/"+ srcAdapter.getCount());
	}
	
	void rangeSelection() {
		int min = Integer.MAX_VALUE, max = -1;
		int cur = -3;
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(selectedInList1);
		for (FileInfo s : selectedInList1) {
			cur = dataSourceL1.indexOf(s);
			if (cur > max) {
				max = cur;
			}
			if (cur < min && cur >= 0) {
				min = cur;
			}
		}
		selectedInList1.clear();
		for (cur = min; cur <= max; cur++) {
			selectedInList1.add(dataSourceL1.get(cur));
		}
		srcAdapter.notifyDataSetChanged();
		final int size = selectedInList1.size();
		if (size == dataSourceL1.size()) {
			allCbx.setSelected(true);
			allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			allCbx.setSelected(false);
			allCbx.setImageResource(R.drawable.dot);
		}
		selectionStatus1.setText(size + "/"+ srcAdapter.getCount());
	}
	
	void undoSelection() {
		final ArrayList<FileInfo> listTemp = new ArrayList<FileInfo>(selectedInList1);
		selectedInList1.clear();
		selectedInList1.addAll(tempSelectedInList1);
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(listTemp);
		srcAdapter.notifyDataSetChanged();
		final int size = selectedInList1.size();
		if (size == dataSourceL1.size()) {
			allCbx.setSelected(true);
			allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			allCbx.setSelected(false);
			allCbx.setImageResource(R.drawable.dot);
		}
		selectionStatus1.setText(size + "/"+ srcAdapter.getCount());
	}

	public boolean deletes(final View v) {
		final ArrayList<FileInfo> selectedInList;
		if (v.getId() == R.id.icon) {
			selectedInList = selectedInList1;
		} else {
			selectedInList = selectedInList2;
		}
		if (selectedInList.size() > 0) {
			//final ArrayList<FileInfo> selectedInListA = selectedInList;
			final TextView editText = new TextView(this);
			editText.setText(Util.collectionToString(selectedInList.subList(0, Math.min(3, selectedInList.size())), true, "\n") + "...");
			AlertDialog dialog = new AlertDialog.Builder(FolderChooserActivity.this)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle("Delete " + selectedInList.size() + " files?")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int counter = 0;
						int notsuccess = 0;
						String statusDel;
						boolean ret;
						for (FileInfo file : selectedInList) {
							ret = AndroidPathUtils.deleteFile(file.file, FolderChooserActivity.this);// new File(selectedPath).delete();
							if (ret) {
								counter++;
								statusDel = "Deleted file \"" + file.path + "\" successfully";
							} else {
								notsuccess++;
								statusDel = "Cannot delete file \"" + file.path + "\"";
							}
							selectionStatus1.setText(statusDel);
						}
						selectionStatus1.setText("Deleted " + counter + " files successfully, " + notsuccess + " files unsuccessfully");
						if (v.getId() == R.id.icon2) {
							dataSourceL2.removeAll(selectedInList);
							destAdapter.notifyDataSetChanged();
						}
						selectedInList.clear();
					}
				})
				.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create();
			dialog.show();
		} else {
			showToast("No file selected");
		}
		return true;
	}

	public boolean sends(final View v) {
		ArrayList<FileInfo> selectedInList = null;
		if (v.getId() == R.id.icon) {
			selectedInList = selectedInList1;
		} else {
			selectedInList = selectedInList2;
		}
		if (selectedInList.size() > 0) {
			ArrayList<Uri> uris = new ArrayList<Uri>(selectedInList.size());
			Intent send_intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			send_intent.setFlags(0x1b080001);

			send_intent.setType("*/*");
			for(FileInfo st : selectedInList) {
				File file = st.file;
				uris.add(Uri.fromFile(file));
			}
			Log.d(TAG, Util.collectionToString(uris, true, "\n") + ".");
			send_intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			Log.d("send_intent", send_intent + ".");
			Log.d("send_intent.getExtras()", AndroidUtils.bundleToString(send_intent.getExtras()));
			Intent createChooser = Intent.createChooser(send_intent, "Send via..");
			Log.d("createChooser", createChooser + ".");
			Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
			startActivity(createChooser);
		} else {
			showToast("No file selected");
		}
		return true;
	}

//	public void addFolder(View view) {
//		final EditText editText = new EditText(this);
//		AlertDialog dialog = new AlertDialog.Builder(this)
//			.setIconAttribute(android.R.attr.dialogIcon)
//			.setTitle("New Folder")
//			.setView(editText)
//			.setPositiveButton(R.string.ok,
//			new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					try {
//						String name = editText.getText().toString();
//						File f = new File(dir, name);
//						if (f.exists()) {
//							showToast("\"" + f + "\" is existing. Please choose another name");
//						} else {
//							boolean ok = f.mkdirs();
//							if (ok) {
//								showToast(f + " was created successfully");
//							} else {
//								showToast(f + " can't be created");
//							}
//							dialog.dismiss();
//						}
//					} catch (NumberFormatException nfe) {
//						showToast("Invalid number. Keep the old value");
//					}
//				}
//			})
//			.setNegativeButton(R.string.cancel,
//			new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					dialog.dismiss();
//				}
//			}).create();
//		dialog.show();
//
//	}

//	public void up(View view) {
//		File curDir = new File(dir);
//		Log.d("up curDir", curDir.getAbsolutePath());
//		File parentFile = curDir.getParentFile();
//		if (parentFile != null) {
//			Log.d("curDir.getParentFile()", parentFile.getAbsolutePath());
//			changeDir(parentFile, true);
//		}
//	}

	public void allCbx(View view) {
		if (multiFiles) {
			selectedInList1.clear();
			if (allCbx.isSelected()) {
				allCbx.setSelected(false);
				allCbx.setImageResource(R.drawable.dot);
			} else {
				allCbx.setSelected(true);
				allCbx.setImageResource(R.drawable.ic_accept);
				for (FileInfo st : dataSourceL1) {
					if (st.file.canRead() && !dataSourceL2.contains(st)) {
						selectedInList1.add(st);
					}
				}
			}
			selectionStatus1.setText(selectedInList1.size() 
									 + "/"+ dataSourceL1.size());
			srcAdapter.notifyDataSetChanged();
		}
	}

	public void allCbx2(View view) {
		if (multiFiles) {
			selectedInList2.clear();
			if (allCbx2.isSelected()) {
				allCbx2.setSelected(false);
				allCbx2.setImageResource(R.drawable.dot);
			} else {
				allCbx2.setSelected(true);
				allCbx2.setImageResource(R.drawable.ic_accept);
				selectedInList2.addAll(dataSourceL2);
			}
			selectionStatus2.setText(selectedInList2.size() 
									 + "/"+ dataSourceL2.size());
			destAdapter.notifyDataSetChanged();
		}
	}

	public void allName(View view) {
		if (allName.getText().toString().equals("Name ▲")) {
			allName.setText("Name ▼");
			fileListSorter = new FileInfo.SortFolderNameFirstDecrease();
			Collections.sort(dataSourceL1, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Name ▼");
		} else {
			allName.setText("Name ▲");
			fileListSorter = new FileInfo.SortFolderNameFirstIncrease();
			Collections.sort(dataSourceL1, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Name ▲");
		}
		allDate.setText("Date");
		allSize.setText("Size  ");
		allType.setText("Type");
		srcAdapter.notifyDataSetChanged();
	}

	public void allName2(View view) {
		//Log.i("allName2", Util.collectionToString(dataSourceL2, true, "\n"));
		if (allName2.getText().toString().equals("Name ▲")) {
			allName2.setText("Name ▼");
			fileListSorter = new FileInfo.SortFolderNameFirstDecrease();
			Collections.sort(dataSourceL2, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Name ▼");
		} else {
			allName2.setText("Name ▲");
			fileListSorter = new FileInfo.SortFolderNameFirstIncrease();
			Collections.sort(dataSourceL2, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Name ▲");
		}
		//Log.i("allName2", Util.collectionToString(dataSourceL2, true, "\n"));
		allDate2.setText("Date");
		allSize2.setText("Size  ");
		allType2.setText("Type");
		destAdapter.notifyDataSetChanged();
	}

	public void allDate(View view) {
		if (allDate.getText().toString().equals("Date ▲")) {
			allDate.setText("Date ▼");
			fileListSorter = new FileInfo.SortFileDateDecrease();
			Collections.sort(dataSourceL1, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Date ▼");
		} else {
			allDate.setText("Date ▲");
			fileListSorter = new FileInfo.SortFileDateIncrease();
			Collections.sort(dataSourceL1, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Date ▲");
		}
		allName.setText("Name");
		allSize.setText("Size  ");
		allType.setText("Type");
		srcAdapter.notifyDataSetChanged();
	}

	public void allDate2(View view) {
		//Log.i("date", Util.collectionToString(dataSourceL2, true, "\n"));
		if (allDate2.getText().toString().equals("Date ▲")) {
			allDate2.setText("Date ▼");
			fileListSorter = new FileInfo.SortFileDateDecrease();
			Collections.sort(dataSourceL2, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Date ▼");
		} else {
			allDate2.setText("Date ▲");
			fileListSorter = new FileInfo.SortFileDateIncrease();
			Collections.sort(dataSourceL2, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Date ▲");
		}
		//Log.i("date", Util.collectionToString(dataSourceL2, true, "\n"));
		allName2.setText("Name");
		allSize2.setText("Size  ");
		allType2.setText("Type");
		destAdapter.notifyDataSetChanged();
	}

	public void allSize(View view) {
		if (allSize.getText().toString().equals("Size ▲")) {
			allSize.setText("Size ▼");
			fileListSorter = new FileInfo.SortFileSizeDecrease();
			Collections.sort(dataSourceL1, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Size ▼");
		} else {
			allSize.setText("Size ▲");
			fileListSorter = new FileInfo.SortFileSizeIncrease();
			Collections.sort(dataSourceL1, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Size ▲");
		}
		allName.setText("Name");
		allDate.setText("Date");
		allType.setText("Type");
		srcAdapter.notifyDataSetChanged();
	}

	public void allSize2(View view) {
		if (allSize2.getText().toString().equals("Size ▲")) {
			allSize2.setText("Size ▼");
			fileListSorter = new FileInfo.SortFileSizeDecrease();
			Collections.sort(dataSourceL2, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Size ▼");
		} else {
			allSize2.setText("Size ▲");
			fileListSorter = new FileInfo.SortFileSizeIncrease();
			Collections.sort(dataSourceL2, fileListSorter);
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Size ▲");
		}
		allName2.setText("Name");
		allDate2.setText("Date");
		allType2.setText("Type");
		destAdapter.notifyDataSetChanged();
	}

	public void allType(View view) {
		if (allType.getText().toString().equals("Type ▲")) {
			allType.setText("Type ▼");
			Collections.sort(dataSourceL1, new FileInfo.SortFileTypeDecrease());
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Type ▼");
		} else {
			allType.setText("Type ▲");
			Collections.sort(dataSourceL1, new FileInfo.SortFileTypeIncrease());
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order", "Type ▲");
		}
		allName.setText("Name");
		allDate.setText("Date");
		allSize.setText("Size  ");
		srcAdapter.notifyDataSetChanged();
	}

	public void allType2(View view) {
		if (allType2.getText().toString().equals("Type ▲")) {
			allType2.setText("Type ▼");
			Collections.sort(dataSourceL2, new FileInfo.SortFileTypeDecrease());
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Type ▼");
		} else {
			allType2.setText("Type ▲");
			Collections.sort(dataSourceL2, new FileInfo.SortFileTypeIncrease());
			AndroidUtils.setSharedPreference(this, "FolderChooserActivity.order2", "Type ▲");
		}
		allName2.setText("Name");
		allDate2.setText("Date");
		allSize2.setText("Size  ");
		destAdapter.notifyDataSetChanged();
	}

	public void removeFiles(View view) {
		if (selectedInList2.size() > 0) {
			if (selectedInList2.size() == dataSourceL2.size()) {
				allCbx2.setSelected(false);
				allCbx2.setImageResource(R.drawable.dot);
				allCbx2.setEnabled(false);
				nofilelayout2.setVisibility(View.VISIBLE);
			}
			if (multiFiles) {
				dataSourceL2.removeAll(selectedInList2);
			} else {
				dataSourceL2.clear();
			}
			allCbx.setEnabled(true);
			selectedInList2.clear();
			destAdapter.notifyDataSetChanged();
			srcAdapter.notifyDataSetChanged();
			selectionStatus2.setText(selectedInList2.size() 
									 + "/"+ dataSourceL2.size());
		}
	}

	public void removeAllFiles(View view) {
		dataSourceL2.clear();
		selectedInList2.clear();
		allCbx.setEnabled(true);
		allCbx2.setSelected(false);
		allCbx2.setImageResource(R.drawable.dot);
		allCbx2.setEnabled(false);
		destAdapter.notifyDataSetChanged();
		srcAdapter.notifyDataSetChanged();
		nofilelayout2.setVisibility(View.VISIBLE);
		selectionStatus2.setText(selectedInList2.size() 
								 + "/"+ dataSourceL2.size());
	}

	public void addFiles(View view) {
		addFileInfos(selectedInList1);
	}

	private void addFileInfos(final ArrayList<FileInfo> sel) {
		if (sel.size() > 0) {
			nofilelayout2.setVisibility(View.GONE);
			if (multiFiles) {
				File file;
				int size;
				String path;
				String dataSourceL2Path;
				for (FileInfo st : sel) {
					file = st.file;
					path = st.path + "/";
					if (file.isDirectory()) {
						size = dataSourceL2.size();
						for (int i = 0; i < size; i++) {
							dataSourceL2Path = dataSourceL2.get(i).path;
							if (dataSourceL2Path.equals(st.path) || dataSourceL2Path.startsWith(path)) {
								dataSourceL2.remove(i);
								i--;
								size--;
							}
						}
					}
					if (!dataSourceL2.contains(st) && file.exists()) {
						dataSourceL2.add(st);
					}
				}
				if (sel == dataSourceL1) {
					allCbx.setSelected(true);
					allCbx.setImageResource(R.drawable.ic_accept);
					allCbx.setEnabled(false);
				} else {
					boolean allInclude = true;
					for (FileInfo st : dataSourceL1) {
						if (!dataSourceL2.contains(st)) {
							allInclude = false;
							break;
						}
					}
					if (allInclude) {
						allCbx.setSelected(true);
						allCbx.setImageResource(R.drawable.ic_accept);
						allCbx.setEnabled(false);
					}
				}
				
			} else {
				dataSourceL2.clear();
				dataSourceL2.addAll(selectedInList1);
			}
			Collections.sort(dataSourceL2, fileListSorter2);
			allCbx2.setEnabled(true);
			selectedInList1.clear();
			srcAdapter.notifyDataSetChanged();
			destAdapter.notifyDataSetChanged();
			selectionStatus1.setText(selectedInList1.size() 
									 + "/"+ dataSourceL1.size());
			selectionStatus2.setText(selectedInList2.size() 
									 + "/"+ dataSourceL2.size());
		}
	}

	public void addAllFiles(View view) {
		addFileInfos(dataSourceL1);
//		if (multiFiles && dataSourceL1.size() > 0) {
//			nofilelayout2.setVisibility(View.GONE);
//			for (FileInfo st : dataSourceL1) {
//				//String st2 = dirSt + st;
//				File file = st.file;
//				if (!dataSourceL2.contains(st) && file.exists() && file.canRead()) {
//					dataSourceL2.add(st);
//				}
//			}
//			allCbx.setSelected(true);
//			allCbx.setImageResource(R.drawable.ic_accept);
//			selectedInList1.clear();
//			Collections.sort(dataSourceL2, fileListSorter2);
//			srcAdapter.notifyDataSetChanged();
//			destAdapter.notifyDataSetChanged();
//			allCbx.setEnabled(false);
//			allCbx2.setEnabled(true);
//			selectionStatus1.setText(selectedInList1.size() 
//									 + "/"+ dataSourceL1.size());
//			selectionStatus2.setText(selectedInList2.size() 
//									 + "/"+ dataSourceL2.size());
//		} else {
//			File file = dataSourceL1.get(0).file;
//			if (dataSourceL1.size() == 1 && file.exists() && file.isFile()) {
//				dataSourceL2.clear();
//				dataSourceL2.add(dataSourceL1.get(0));
//				selectedInList1.clear();
//				srcAdapter.notifyDataSetChanged();
//				destAdapter.notifyDataSetChanged();
//			}
//		}
	}

	public void ok(View view) {
		// if (currentSelectedList.size() == 0 && multiFiles) {
		// 	currentSelectedList.add(new File(dir.getText().toString()));
		// } else 
		if (dataSourceL2.size() == 0 && selectedInList1.size() == 0 && !multiFiles && suffix.length() > 0) {
			Toast.makeText(this, "Please select a file", Toast.LENGTH_LONG).show();
			return;
		}
		Log.d("selected file", Util.collectionToString(dataSourceL2, false, "\r\n"));
		String[] fileArr = null;
		if (multiFiles) {
			if (dataSourceL2.size() > 0) {
				fileArr = new String[dataSourceL2.size()];
				int i = 0;
				for (FileInfo fi : dataSourceL2) {
					fileArr[i++] = fi.path;
				}
				Arrays.sort(fileArr);
			} else if (selectedInList1.size() > 0) {
				fileArr = new String[selectedInList1.size()];
				int i = 0;
				for (FileInfo fi : selectedInList1) {
					fileArr[i++] = fi.path;
				}
				Arrays.sort(fileArr);
			} else if ("".equals(suffix)) {
				fileArr = new String[] {dir};
			} else {
				Toast.makeText(this, "Please select a file", Toast.LENGTH_LONG).show();
				return;
			}
		} else {
			if (selectedInList1.size() > 0) {
				fileArr = new String[] {selectedInList1.get(0).path};
			} else if ("".equals(suffix)) {
				fileArr = new String[] {dir};
			} else {
				Toast.makeText(this, "Please select a file", Toast.LENGTH_LONG).show();
				return;
			}
		}

		Intent intent = this.getIntent();
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intent.setData(FileProvider.getUriForFile(this, "net.gnu.agrep.fileprovider", new File(fileArr[0])));//Uri.parse("content://net.gnu.explorer" + fileArr.get(0)));
        } else {
			intent.setData(Uri.fromFile(new File(fileArr[0])));
		}

		//intent.putExtra(FolderChooserActivity.PREVIOUS_SELECTED_FILES, fileArr);
		//intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiFiles);

		intent.putExtra(EXTRA_ABSOLUTE_PATH, fileArr);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiFiles);
	    final int size = fileArr.length;
		if (size > 1) {
			final ClipData clipData = ClipData.newRawUri(
				"", FileProvider.getUriForFile(this, "net.gnu.agrep.fileprovider", new File(fileArr[0])));
			for (int i = 1; i < size; i ++) {
				clipData.addItem(new ClipData.Item(FileProvider.getUriForFile(this, "net.gnu.agrep.fileprovider", new File(fileArr[i]))));
			}
			intent.setClipData(clipData);
		}
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
						| Intent.FLAG_GRANT_WRITE_URI_PERMISSION
						| Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		setResult(RESULT_OK, intent);
	    this.finish();
	}

	public void cancel(View view) {
//		Log.d("select previous file", Util.arrayToString(previousSelectedStr, true, "\r\n"));
		Intent intent = this.getIntent();
//		if (previousSelectedStr != null && previousSelectedStr.length > 0) {
//			Arrays.sort(previousSelectedStr);
//		}
//		intent.putExtra(EXTRA_ABSOLUTE_PATH, previousSelectedStr);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiFiles);
		setResult(RESULT_CANCELED, intent);
	    this.finish();
	}

	private class ArrayAdapter extends android.widget.ArrayAdapter<FileInfo> implements OnLongClickListener, OnClickListener {

		private View selectedTV = null;
		private String dirStr;
		private List<FileInfo> selectedInList;
		private List<FileInfo> dataSourceL2;
		private boolean multiFiles;
		private ImageButton all;
		
		private final int backgroundResource;

		public ArrayAdapter(Context context, int textViewResourceId,
							List<FileInfo> objects) {
			super(context, textViewResourceId, objects);
			final int[] attrs = new int[]{R.attr.selectableItemBackground};
			final TypedArray typedArray = obtainStyledAttributes(attrs);
			backgroundResource = typedArray.getResourceId(0, 0);
			typedArray.recycle();
		}

		public void setup(String dir, List<FileInfo> selectedInList, 
						  List<FileInfo> dataSource2, boolean multi, ImageButton all) {
			this.dirStr = dir;
			this.selectedInList = selectedInList;
			this.dataSourceL2 = dataSource2;
			this.multiFiles = multi;
			this.all = all;
		}

		public class Holder extends net.gnu.common.Holder {
			final TextView name;
			final TextView items;
			final TextView attr;
			final TextView lastModified;
			final TextView type;

			final ImageButton cbx;
			final ImageView image;
			final ImageButton more;
			//public FileInfo fileInfo;

			Holder(View convertView) {
				name = (TextView) convertView.findViewById(R.id.name);
				items = (TextView) convertView.findViewById(R.id.items);
				attr = (TextView) convertView.findViewById(R.id.attr);
				lastModified = (TextView) convertView.findViewById(R.id.lastModified);
				type = (TextView) convertView.findViewById(R.id.type);

				cbx = (ImageButton) convertView.findViewById(R.id.cbx);
				image = (ImageView)convertView.findViewById(R.id.icon);
				more = (ImageButton)convertView.findViewById(R.id.more);

				name.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				name.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				items.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				items.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				attr.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				attr.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				lastModified.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				lastModified.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				type.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				type.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				cbx.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				cbx.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				convertView.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				convertView.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				image.setOnLongClickListener(FolderChooserActivity.ArrayAdapter.this);
				image.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);
				more.setOnClickListener(FolderChooserActivity.ArrayAdapter.this);

				image.setTag(this);
				name.setTag(this);
				items.setTag(this);
				attr.setTag(this);
				lastModified.setTag(this);
				type.setTag(this);
				cbx.setTag(this);
				more.setTag(this);
				convertView.setTag(this);
			}
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			final Holder holder;
			if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
				holder = new Holder(convertView);
            } else {
				holder = (Holder) convertView.getTag();
			}

            final TextView name = holder.name;
			final TextView items = holder.items;
			final TextView attr = holder.attr;
			final TextView lastModified = holder.lastModified;
			final TextView type = holder.type;

			final ImageButton cbx = holder.cbx;
			final ImageView image = holder.image;
			final ImageButton more = holder.more;

			final FileInfo fileInfo = getItem(position);
			//Log.d("getView fileInfo", fileInfo.path);

			final File f = fileInfo.file;
			final String fname = f.getName();
			name.setText(fname);
			lastModified.setText(Util.dtf.format(f.lastModified()));

			final String fPath = f.getAbsolutePath();
			holder.fileInfo = fileInfo;

			if (dirStr == null || dirStr.length() > 0) {
				name.setEllipsize(TruncateAt.MIDDLE);
			} else {
				name.setEllipsize(TruncateAt.START);
			}

	        if (f.isDirectory()) {
	        	name.setTextColor(Color.BLUE);
				items.setTextColor(Color.BLUE);
				attr.setTextColor(Color.BLUE);
				lastModified.setTextColor(Color.BLUE);
				type.setTextColor(Color.BLUE);
				more.setColorFilter(Color.BLUE);
	        } else {
	        	name.setTextColor(Color.BLACK);
				items.setTextColor(Color.parseColor("#ff666666"));
				attr.setTextColor(Color.parseColor("#ff666666"));
				lastModified.setTextColor(Color.parseColor("#ff666666"));
	        	type.setTextColor(Color.parseColor("#ff666666"));
				more.setColorFilter(Color.parseColor("#ff666666"));
	        }

			if(f.isDirectory()){ //decide are the file folder or file
				image.setImageDrawable(imageLoader.myfolder72);
			} else {
				final String ext = FileUtil.getExtensionFromName(fname);
				if(ext.equals("apk")) { //decide are the file folder or file
					imageLoader.displayImage(fPath, FolderChooserActivity.this, image, imageLoader.apkIcon);
				} else if (ext.equals("jpg")
						   ||ext.equals("png")
						   ||ext.equals("gif")
						   ||ext.equals("jpeg")
						   ||ext.equals("tiff")){
					imageLoader.displayImage(fPath, FolderChooserActivity.this, image, imageLoader.stubIcon);
				} else {
					image.setImageDrawable(imageLoader.getFileIcon(ext));
				}
			}
			final boolean canRead = f.canRead();
			final boolean canWrite = f.canWrite();
			if (f.isFile()) {
				items.setText(Util.nf.format(f.length()) + " B");

				String st;
				if (canWrite) {
					st = "-rw";
				} else if (canRead) {
					st = "-r-";
				} else {
					st = "---";
					cbx.setEnabled(false);
				}
				final String namef = f.getName();
				int lastIndexOf = namef.lastIndexOf(".");
				type.setText(lastIndexOf >= 0 && lastIndexOf < namef.length()-1? namef.substring(++lastIndexOf) : "");
				attr.setText(st);
			} else {
				final String[] list = f.list();
				final int length = list == null ? 0 : list.length;
				items.setText(Util.nf.format(length) + " item");

				String st;
				if (canWrite) {
					st = "drw";
				} else if (canRead) {
					st = "dr-";
				} else {
					st = "d--";
					cbx.setEnabled(false);
				}
				type.setText("Folder");
				attr.setText(st);
			}
			
			boolean inSelectedFiles = false;
			boolean isPartial = false;
			//Log.d("dataSource2", CommonUtils.collectionToString(dataSource2, true, "\n"));
			if (multiFiles && dataSourceL2 != null)
				for (FileInfo fi : dataSourceL2) {
					if (fi.path.equals(fPath) || fPath.startsWith(fi.path + "/")) {
						inSelectedFiles = true;
						break;
					} else if (fi.path.startsWith(fPath + "/")) {
						isPartial = true;
					}
				}
			//Log.d("inSelectedFiles", inSelectedFiles + ".");
			//Log.d(TAG, "fi=" + fileInfo.path + ", " + selectedInList);
			//Log.d("curSelectedFiles", curSelectedFiles.toString());
			if (inSelectedFiles) {
				convertView.setBackgroundResource(R.drawable.ripple_light_yellow2);
				cbx.setSelected(true);
				cbx.setImageResource(R.drawable.ic_accept);
				cbx.setEnabled(false);
				if (dataSourceL2 != null && selectedInList.size() == dataSourceL1.size()) {
					all.setSelected(true);
				}
				if (dataSourceL2 == null && selectedInList.size() == dataSourceL2.size()) {
					all.setSelected(true);
				}
			} else if (selectedInList.contains(fileInfo)) {
	        	convertView.setBackgroundResource(R.drawable.ripple_light_brown);
				cbx.setSelected(true);
				cbx.setImageResource(R.drawable.ic_accept);
				cbx.setEnabled(true);
				if (dataSourceL2 != null && selectedInList.size() == dataSourceL1.size()) {
					all.setSelected(true);
				}
				if (dataSourceL2 == null && selectedInList.size() == dataSourceL1.size()) {
					all.setSelected(true);
				}
			} else if (isPartial) {
				convertView.setBackgroundResource(R.drawable.ripple_light_yellow3);
				cbx.setSelected(false);
				cbx.setImageResource(R.drawable.dot);
				cbx.setEnabled(true);
				all.setSelected(false);
	        } else {
	        	convertView.setBackgroundResource(backgroundResource);
				cbx.setSelected(false);
				cbx.setImageResource(R.drawable.dot);
				cbx.setEnabled(true);
				all.setSelected(false);
	        }
			
			if (!f.exists()) {
				convertView.setBackgroundResource(R.drawable.ripple_red_light);
			}
			return convertView;
	    }

		private void setBackgroundColor(Holder h, int color) {
			h.cbx.setBackgroundColor(color);
			h.image.setBackgroundColor(color);
			h.name.setBackgroundColor(color);
			h.items.setBackgroundColor(color);
			h.attr.setBackgroundColor(color);
			h.lastModified.setBackgroundColor(color);
			h.type.setBackgroundColor(color);
			h.more.setBackgroundColor(color);
		}

		public boolean rename(View item) {
			final File oldPath = ((Holder)item.getTag()).fileInfo.file;
			Log.d("oldPath", oldPath.getAbsolutePath());
			final EditText editText = new EditText(FolderChooserActivity.this);
			editText.setText(oldPath.getName());
			AlertDialog dialog = new AlertDialog.Builder(FolderChooserActivity.this)
				.setIconAttribute(android.R.attr.dialogIcon)
				.setTitle("New Name")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String name = editText.getText().toString();
						File newF = new File(dir, name);
						Log.d("newF", newF.getAbsolutePath());
						if (newF.exists()) {
							showToast("\"" + newF + "\" is existing. Please choose another name");
						} else {
							boolean ok= oldPath.renameTo(newF);
							if (ok) {
								showToast("Rename successfully");
							} else {
								showToast("Rename unsuccessfully");
							}
							dialog.dismiss();
						}
					}
				})
				.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create();
			dialog.show();
			return true;
		}

		public boolean delete(final View item) {
			final FileInfo fileInfo = ((Holder)item.getTag()).fileInfo;
			final String name = fileInfo.path;
			final TextView editText = new TextView(getContext());
			editText.setText("Delete " + name + "?");
			AlertDialog dialog = new AlertDialog.Builder(FolderChooserActivity.this)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle("Delete ?")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final String statusDel;
						final boolean ret = AndroidPathUtils.deleteFile(fileInfo.file, FolderChooserActivity.this);// new File(selectedPath).delete();
						if (ret) {
							statusDel = "Deleted file \"" + name + "\" successfully";
						} else {
							statusDel = "Cannot delete file \"" + name + "\"";
						}
						if (dirStr.length() == 0) {
							selectionStatus2.setText(statusDel);
						} else {
							selectionStatus1.setText(statusDel);
						}
					}
				})
				.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create();
			dialog.show();
			return true;
		}

		public boolean share(View item) {

			return true;
		}

		public boolean send(View item) {
			File f = ((Holder)item.getTag()).fileInfo.file;
			Uri uri = Uri.fromFile(f);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setFlags(0x1b080001);

			i.setData(uri);
			Log.d("i.setData(uri)", uri + "." + i);
			String floor = null;
			if ((floor = FileUtil.getMimeType(f)) != null) {
				i.setDataAndType(uri, floor);
				Log.d("floor", floor + ", " + i);
			}

			Log.d("send", i + ".");
			Log.d("send.getExtras()", AndroidUtils.bundleToString(i.getExtras()));
			Intent createChooser = Intent.createChooser(i, "Send via..");
			Log.d("createChooser", createChooser + ".");
			Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
			startActivity(createChooser);
			return true;
		}

		public boolean copyName(View item) {
			String data = ((Holder)item.getTag()).fileInfo.file.getName();
			copyToClipboard(data);
			return true;
		}

		public boolean copyPath(View item) {
			String data = ((Holder)item.getTag()).fileInfo.file.getParent();
			copyToClipboard(data);
			return true;
		}

		public boolean copyFullName(View item) {
			copyToClipboard(((Holder)item.getTag()).fileInfo.path);
			return true;
		}

		private void copyToClipboard(String name) {
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) { //Android 2.3 and below
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(name);
			} else { //Android 3.0 and higher
				try {
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
					android.content.ClipData clip = android.content.ClipData.newPlainText("pasted data", name);
					clipboard.setPrimaryClip(clip);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			showToast("Copied " + name + " to clipboard");
		}

		public void onClick(final View v) {
			final int id = v.getId();
			if (id == R.id.more) {
				final PopupMenu popup = new PopupMenu(v.getContext(), v);
				popup.getMenuInflater().inflate(R.menu.itemfileactions, popup.getMenu());
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId())  {
								case R.id.rename:
									rename(v);
									break;
								case R.id.delete:
									delete(v);
									break;
								case R.id.send:
									send(v);
									break;
								case R.id.name:
									copyName(v);
									break;
								case R.id.path:
									copyPath(v);
									break;
								case R.id.fullname:
									copyFullName(v);
									break;
							}
							return true;
						}});
				popup.show();
				return;
			}
			FileInfo fPath = ((Holder)v.getTag()).fileInfo;
			File f = fPath.file;
			Log.d(TAG, "onClick, " + fPath + ", " + v);
			if (f.exists()) {
				if (!f.canRead()) {
					showToast(f + " cannot be read");
				} else {
					boolean inSelected = false;
					if (dataSourceL2 != null)
						for (FileInfo st : dataSourceL2) {
							if (fPath.equals(st) || fPath.path.startsWith(st.path + "/")) {
								inSelected = true;
								break;
							}
						}
					if (!inSelected) {
						if (multiFiles) {
							if ((id == R.id.cbx) || (id == R.id.icon)) {//file and folder
								if (selectedInList.contains(fPath)) {
									selectedInList.remove(fPath);
								} else {
									selectedInList.add(fPath);
								}
							} else if (f.isDirectory() //&& curSelectedFiles.size() == 0 
									   && (dirStr == null || dirStr.length() > 0)) { 
								changeDir(fPath.path, true);
							} else if (f.isFile()) { //file
								try{
									Uri uri = Uri.fromFile(f);
									Intent i = new Intent(Intent.ACTION_VIEW); 
									i.addCategory(Intent.CATEGORY_DEFAULT);
									i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
									i.setData(uri);
									Log.d("i.setData(uri)", uri + "." + i);
									String floor = null;
									if ((floor = FileUtil.getMimeType(f)) != null) {
										i.setDataAndType(uri, floor);
										Log.d("floor", floor + ", " + i);
									}
									Intent createChooser = Intent.createChooser(i, "View");
									Log.i("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
									startActivity(createChooser);
								} catch (Throwable e) {
									Toast.makeText(FolderChooserActivity.this, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							}
							if (dataSourceL2 != null) {
								selectionStatus1.setText(selectedInList.size() 
														 + "/"+ dataSourceL1.size());
							} else {
								selectionStatus2.setText(selectedInList.size() 
														 + "/"+ dataSourceL1.size());
							}
						} else { //!multifile
							if (id == R.id.cbx || id == R.id.icon) {
								if (selectedInList.contains(fPath)) { // đã chọn
									selectedInList.clear();
									selectedTV = null;
								} else { // chọn mới bỏ cũ
									selectedInList.clear();
									selectedInList.add(fPath);
									selectedTV = v;
								}
							} else if (f.isFile()) {
								try{
									Uri uri = Uri.fromFile(f);
									Intent i = new Intent(Intent.ACTION_VIEW); 
									i.addCategory(Intent.CATEGORY_DEFAULT);
									i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
									i.setData(uri);
									Log.d("i.setData(uri)", uri + "." + i);
									String floor = null;
									if ((floor = FileUtil.getMimeType(f)) != null) {
										i.setDataAndType(uri, floor);
										Log.d("floor", floor + ", " + i);
									}
									Intent createChooser = Intent.createChooser(i, "View");
									Log.i("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
									startActivity(createChooser);
								} catch (Throwable e) {
									Toast.makeText(FolderChooserActivity.this, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							} else { //", "Directory
								selectedInList.clear();
								if (selectedTV != null) {
									selectedTV = null;
								}
								if (dirStr == null || dirStr.length() > 0) {
									changeDir(fPath.path, true);
								}
							}
						}
						notifyDataSetChanged();
//					} else if (v instanceof CheckBox) {
//						CheckBox v2 = (CheckBox)v;
//						(v2).setChecked(v2.isChecked());
					}
				}
			} else {
				changeDir(f.getParent(), true);
			}
		}

		public boolean onLongClick(View v) {
			FileInfo fPath = ((Holder)v.getTag()).fileInfo;
			File f = fPath.file;

			if (!f.exists()) {
				changeDir(fPath.path, true);
				//changeDir(new File(dirStr), true);
				return true;
			} else if (!f.canRead()) {
				showToast(f + " cannot be read");
				return true;
			}
			Log.d(TAG, "onLongClick, " + fPath);
			Log.d("currentSelectedList", Util.collectionToString(selectedInList, true, "\r\n"));
			Log.d("selectedInList.contains(f)", "" + selectedInList.contains(f));
			Log.d("multiFiles", multiFiles + "");

			boolean inSelectedFiles = false;
			if (dataSourceL2 != null)
				for (FileInfo st : dataSourceL2) {
					if (fPath.equals(st) || fPath.path.startsWith(st.path + "/")) {
						inSelectedFiles = true;
						break;
					}
				}
			if (!inSelectedFiles) {
				if (multiFiles || suffix.length() == 0) {
					if (selectedInList.contains(fPath)) {
						selectedInList.remove(fPath);
						v.setBackgroundResource(R.drawable.ripple);
					} else {
						selectedInList.add(fPath);
						v.setBackgroundResource(R.drawable.ripple_light_brown);
					}
					selectionStatus1.setText(selectedInList.size() 
												 + "/"+ dataSourceL1.size());
				} else { // single file
					if (f.isFile()) {
						// chọn mới đầu tiên
						if (selectedInList.size() == 0) {
							selectedInList.add(fPath);
							selectedTV = v;
							v.setBackgroundResource(R.drawable.ripple_light_brown);
						} else if (selectedInList.size() > 0) {
							if (selectedInList.contains(fPath)) {
								// đã chọn
								selectedTV = null;
								v.setBackgroundResource(R.drawable.ripple);
								selectedInList.clear();
							} else {
								// chọn mới bỏ cũ
								selectedInList.clear();
								selectedInList.add(fPath);
								selectedTV.setBackgroundResource(R.drawable.ripple);
								v.setBackgroundResource(R.drawable.ripple_light_brown);
								selectedTV = v;
							}
						}
					} else { //", "Directory
						selectedInList.clear();
						if (selectedTV != null) {
							selectedTV.setBackgroundResource(R.drawable.ripple);
							selectedTV = null;
						}
						if (dirStr == null || dirStr.length() > 0) {
							changeDir(dirStr, true);
						}
					}
				}
				notifyDataSetChanged();
			} 
			return true;
		}
	}
}
