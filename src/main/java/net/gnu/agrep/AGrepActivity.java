package net.gnu.agrep;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewAnimator;

import net.gnu.agrep.R;
import net.gnu.common.StorageCheckActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import android.content.res.Configuration;
import net.gnu.util.Util;
import java.util.Calendar;
import net.gnu.common.SearcherAplication;
import android.support.v4.app.FragmentManager;
import java.io.FileWriter;
import android.util.Log;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.MenuInflater;
import java.io.IOException;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.DialogInterface;
import net.gnu.util.FileUtil;
import android.app.AlertDialog;
import net.gnu.common.ExpandableListAdapter;
import net.gnu.util.*;
import net.gnu.androidutil.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import net.gnu.common.DuplicateFinderActivity;
import android.os.*;
import android.support.v4.app.*;
import android.*;
import android.content.pm.*;
import android.support.annotation.*;
import android.view.*;
import android.widget.*;
import android.webkit.*;
import java.io.*;

public class AGrepActivity extends StorageCheckActivity {

    private static final String TAG = "AGrepActivity";
	final static int REQUEST_CODE_PREFS = 0x1002;
    
	public SlidingTabsFragment slideFrag;
	private FragmentManager supportFragmentManager;
	public SettingsFragment main;

	public int saved = 0;
	public int tabCount = 0;
    public int increaseTabNo = 0;
	private int curIndex = -1;
	
	private ClipboardManager mClipboard;
	private FileWriter fw;
	
	private ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener
			= new ClipboardManager.OnPrimaryClipChangedListener() {
    		    public void onPrimaryClipChanged() {
        		    updateClipData(true);
 		       }
	};

	private boolean savingClipboard;

	private boolean privateUse = false;

	
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged " + newConfig);
		super.onConfigurationChanged(newConfig);
	}
	
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate main=" + main);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		supportFragmentManager = getSupportFragmentManager();
		slideFrag = (SlidingTabsFragment) supportFragmentManager.findFragmentByTag("slideFrag");
		if (slideFrag == null) {
            slideFrag = new SlidingTabsFragment();
			final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
			transaction.replace(R.id.content_fragment, slideFrag, "slideFrag");
			transaction.commit();
        } 
		mClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		mClipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
        
	}
	
	@Override
	public void onPostCreate(final Bundle savedInstanceState) {
		Log.d(TAG, "onPostCreate main=" + main + ", savedInstanceState=" + savedInstanceState);
		super.onPostCreate(savedInstanceState);
        
		final Intent intent = getIntent();
		
		if (savedInstanceState == null) {
			if (intent != null && intent.getData() != null) {
				Log.d(TAG, "onPostCreate intent.data " + intent.getData());
				slideFrag.addTab(intent);
			} else {
				final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
				tabCount = sp.getInt(Prefs.KEY_TAB_COUNT, 1);
				increaseTabNo = tabCount;
				List<SettingsFragment> l = new ArrayList<>(tabCount);
				
				int i = 1;
				final Set<String> noSet = sp.getStringSet("noSet", null);
				Log.d(TAG, "onPostCreate tabCount=" + tabCount + ", " + noSet);
				if (noSet != null) {
					for (String s : noSet) {
						final SettingsFragment frag = new SettingsFragment();
						frag.no = i++;
						final Bundle bun = new Bundle();
						bun.putInt("index", Integer.parseInt(s));
						bun.putInt("no", frag.no);
						frag.setArguments(bun);
						l.add(frag);
					}
				} else {
					final SettingsFragment frag = new SettingsFragment();
					frag.no = i++;
					final Bundle bun = new Bundle();
					bun.putInt("no", frag.no);
					frag.setArguments(bun);
					l.add(frag);
				}
				slideFrag.addInit(l);
			}
		} else {
			final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			tabCount = sp.getInt(Prefs.KEY_TAB_COUNT, 1);
			increaseTabNo = savedInstanceState.getInt("increaseTabNo");
			curIndex = savedInstanceState.getInt("curIndex");
			slideFrag.setCurrentItem(curIndex);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		Log.d(TAG, "onSaveInstanceState outState=" + outState);
		super.onSaveInstanceState(outState);
		outState.putInt("increaseTabNo", increaseTabNo);
		outState.putInt("curIndex", slideFrag.getCurIndex());
		saveTabsOnFinish(false);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreInstanceState savedInstanceState=" + savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume tabCount=" + tabCount + ", curIndex " + curIndex);
		super.onResume();
		final int i = slideFrag.getCurIndex();
		final int count = slideFrag.getCount();
		if (count > 1) {
			if (i > count - 2) {
				slideFrag.setCurrentItem(i-1);
			} else if (i == 0) {
				slideFrag.setCurrentItem(1);
			}
		}

		if (main == null) {
			main = slideFrag.getCurFrag();
		}
	}

	@Override
	protected void onPostResume() {
		Log.d(TAG, "onPostResume");
		super.onPostResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		//mClipboard.removePrimaryClipChangedListener(mPrimaryChangeListener);
		
	}

	private static final long TIME_INTERVAL = 250000000L;
	private long mBackPressed = System.nanoTime();
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		if (mBackPressed + TIME_INTERVAL >= System.nanoTime()) {
			saveTabsOnFinish(true);
			super.onBackPressed();
		} else {
			mBackPressed = System.nanoTime();
			main.showGrep(false);
		}
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyLongPress.keyCode=" + keyCode + ", event=" + event);
		if (keyCode == KeyEvent.KEYCODE_BACK
			&& event.getAction() == KeyEvent.ACTION_DOWN) {
			saveTabsOnFinish(true);
			super.onBackPressed();
			return true;
		}
		return false;
	}

	private void saveTabsOnFinish(final boolean onFinish) {
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = sp.edit();
		
		tabCount = slideFrag.getCount();
		
		final TreeSet<String> arr = new TreeSet<String>();
		for (int i = 0; i < tabCount; i++) {
			final SettingsFragment item = slideFrag.getItem(i);
			if (!item.fake) {
				arr.add(item.index + "");
				if (onFinish) {
					item.mPrefs.savePrefs(this, item.index);
				}
			}
		}
		editor.putStringSet("noSet", arr);
		
		tabCount = (tabCount > 1) ? (tabCount - 2) : 1;
		editor.putInt(Prefs.KEY_TAB_COUNT, tabCount);
		Log.d(TAG, "setTabCount tabCount=" + tabCount);
		
		editor.commit();
	}

	@Override
    protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent " + intent);
        super.onNewIntent(intent);
		if (intent != null && !Intent.ACTION_MAIN.equals(intent.getAction())) {
			slideFrag.addTab(intent);
		}
	}

	protected static String[] PERMISSIONS_READ_PHONE_STATE = {
		Manifest.permission.READ_PHONE_STATE
	};
	protected static final int REQUEST_READ_PHONE_STATE = 777;
	
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
		Log.d(TAG, "onRequestPermissionsResult " + requestCode + ", " + grantResults.length + ", " + grantResults);
		if (requestCode == REQUEST_READ_PHONE_STATE) {
            //if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSystemFeatures();
            //} else {
            //    Toast.makeText(this, R.string.grantfailed, Toast.LENGTH_SHORT).show();
            //    requestStoragePermission();
            //}
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
	
    protected void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
																Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.

			Log.d(TAG, "shouldShowRequestPermissionRationale");

//			new AlertDialog.Builder(this).setTitle(R.string.grantper).setMessage(getString(R.string.granttext))
//				.setPositiveButton(R.string.grant,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
						ActivityCompat.requestPermissions(AGrepActivity.this, 
														  PERMISSIONS_READ_PHONE_STATE, 
														  REQUEST_READ_PHONE_STATE);
//					}
//				})
//				.setNegativeButton(R.string.cancel,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						finish();
//					}
//				}).show();
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            Log.d(TAG, "requestStoragePermission.requestPermissions");
            ActivityCompat.requestPermissions(this, PERMISSIONS_READ_PHONE_STATE, REQUEST_READ_PHONE_STATE);
        }
    }
	public boolean checkReadPhoneStatePermission() {
        // Verify that all required contact permissions have been granted.
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
			== PackageManager.PERMISSION_GRANTED
			;
    }
	public boolean systemFeatures(MenuItem item) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkStorage) {
            if (!checkReadPhoneStatePermission()) {
                requestReadPhoneStatePermission();
			} else {
				getSystemFeatures();
			}
		} else {
			getSystemFeatures();
		}
		return true;
	}

	private void getSystemFeatures() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		ListView tv = new ListView(this);
		List<CharSequence> features = SystemUtils.getBuild();
		features.addAll(Util.propertiesToListString(System.getProperties()));
		features.addAll(Util.propertiesToListString(System.getenv()));
		List<CharSequence> hardwareInfo = SystemUtils.getHardwareInfo(this);
		hardwareInfo.add(0, "Hardware Info\n");
		features.addAll(hardwareInfo);
		features.add(CommandUtils.fetch_cpu_info().insert(0, "CPU Info\n"));
		features.add(CommandUtils.fetch_disk_info().insert(0, "Disk Info\n"));
		features.add(CommandUtils.fetch_mount_info().insert(0, "Mount Info\n"));
		features.add(CommandUtils.fetch_netcfg_info().insert(0, "NetCfg Info\n"));
		features.add(CommandUtils.fetch_netstat_info().insert(0, "NetStat Info\n"));
		features.add(CommandUtils.fetch_process_info().insert(0, "Process Info\n"));
		features.add("Tel Status\n" + SystemUtils.fetch_tel_status(this));

		ArrayAdapter<CharSequence> arrayAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, features);
		tv.setAdapter(arrayAdapter);

		alert.setIconAttribute(android.R.attr.dialogIcon);
		alert.setTitle("System Features List");

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		alert.setView(tv);
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
	}
	
	public boolean sensors(MenuItem item) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		ListView tv = new ListView(this);
		List<CharSequence>[] sensors = SystemUtils.getSensors(this);
		List<CharSequence>[] systemService = SystemUtils.getSystemService(this);
		sensors[0].addAll(systemService[0]);
		sensors[1].addAll(systemService[1]);
		//Log.d("sensors[0]", CommonUtils.collectionToString(sensors[0], true, "\n"));
		//Log.d("sensors[1]", CommonUtils.collectionToString(sensors[1], true, "\n"));
		ExpandableListAdapter arrayAdapter = new ExpandableListAdapter(this, sensors[0], sensors[1], null);
		tv.setAdapter(arrayAdapter);
		//arrayAdapter.notifyDataSetChanged();
		
		alert.setIconAttribute(android.R.attr.dialogIcon);
		alert.setTitle("Sensor List");
		//alert.setCancelable(true);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		alert.setView(tv);
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
		return true;
	}
	
	public boolean about(MenuItem item) {
		AndroidUtils.copyAssetToDir(this, SearcherAplication.PRIVATE_PATH, "lic.html"); 

		final WebView wv = new WebView(this);
		wv.loadUrl(new File(SearcherAplication.PRIVATE_PATH + "/lic.html").toURI().toString());
		wv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setIconAttribute(android.R.attr.dialogIcon)
			.setTitle("Power DocSearch")
			.setView(wv)
			.setPositiveButton(R.string.ok,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			}).create();
		dialog.show();
		return true;
	}
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
		if (!privateUse) {
			menu.findItem(R.id.menu_save_clipboard).setVisible(false);
		}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.menu_option) {
            Intent intent = new Intent(this, OptionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PREFS);
        } else if (itemId == R.id.menu_save_clipboard) {
			if (savingClipboard) {
				savingClipboard = false;
				fw = null;
				item.setTitle("Start Saving Clipboard");
			} else {
				try {
					fw = new FileWriter(SearcherAplication.PRIVATE_PATH + "/clipboard " + Util.DATETIME_FORMAT.format(Calendar.getInstance().getTime()).replaceAll("[/:\\*?<>|\"']", "_") + ".txt", true);
					savingClipboard = true;
					item.setTitle("Stop Saving Clipboard");
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		} else if (itemId == R.id.menu_duplicate_finder) {
			Intent it = new Intent(this, DuplicateFinderActivity.class);
			startActivity(it);
		} else if (itemId == R.id.menu_clear_cache) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Clear Caching Files");
			alert.setIconAttribute(android.R.attr.alertDialogIcon);
			long[] entry = new long[] {0, 0, 0};
			FileUtil.getDirSize(SearcherAplication.PRIVATE_DIR, entry);
			alert.setMessage("Cache has " + Util.nf.format(entry[2]) + " folders, " + Util.nf.format(entry[0])
							 + " files, " + Util.nf.format(entry[1])
							 + " bytes. " + "\r\nAre you sure you want to clear the cached files? "
							 + "\r\nAfter cleaning searching will be slow for the first times " +
							 "and the searching task maybe incorrect.");
			alert.setCancelable(true);

			alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int num = FileUtil.deleteFiles(SearcherAplication.PRIVATE_DIR, true);
						Log.d(TAG, "Clean cache" + num + " files deleted");

					}
				});

			alert.setPositiveButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

			AlertDialog alertDialog = alert.create();
			alertDialog.show();
		}
        return super.onOptionsItemSelected(item);
    }
	
	void updateClipData(boolean updateType) {
        ClipData clip = mClipboard.getPrimaryClip();

        if (clip != null) {
            if (savingClipboard && fw != null) {
				ClipData.Item item = clip.getItemAt(0);
				CharSequence clipData = item.getText();
				if (clipData != null && clipData.length() > 0) {
					try {
						fw.append(clipData + "\n");
						fw.flush();
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		}
    }

}
