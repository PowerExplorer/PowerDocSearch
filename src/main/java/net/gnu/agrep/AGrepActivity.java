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
//	private ClipboardManager mClipboard;
//	private FileWriter fw;
//	
//	private ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener
//			= new ClipboardManager.OnPrimaryClipChangedListener() {
//    		    public void onPrimaryClipChanged() {
//        		    updateClipData(true);
// 		       }
//	};
//
//	private boolean savingClipboard;

	
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
//		mClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
//		mClipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
        
	}
	
	@Override
	public void onPostCreate(final Bundle savedInstanceState) {
		Log.d(TAG, "onPostCreate main=" + main + ", savedInstanceState=" + savedInstanceState);
		super.onPostCreate(savedInstanceState);
        
		final Intent intent = getIntent();
		
		if (savedInstanceState == null) {
			if (intent != null && intent.getData() != null) {
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
        super.onNewIntent(intent);
		if (intent != null && !Intent.ACTION_MAIN.equals(intent.getAction())) {
			slideFrag.addTab(intent);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_option) {
            Intent intent = new Intent(this, OptionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PREFS);
//        } else if (item.getItemId() == R.id.menu_save_clipboard) {
//			if (savingClipboard) {
//				savingClipboard = false;
//				fw = null;
//				item.setTitle("Start Saving Clipboard");
//			} else {
//				try {
//					fw = new FileWriter(SearcherAplication.PRIVATE_PATH + "/clipboard " + Util.DATETIME_FORMAT.format(Calendar.getInstance().getTime()).replaceAll("[/:\\*?<>|\"']", "_") + ".txt", true);
//					savingClipboard = true;
//					item.setTitle("Stop Saving Clipboard");
//				} catch (IOException e) {
//					Log.e(TAG, e.getMessage(), e);
//				}
//			}
		} else if (item.getItemId() == R.id.menu_clear_cache) {
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
	
//	void updateClipData(boolean updateType) {
//        ClipData clip = mClipboard.getPrimaryClip();
//
//        if (clip != null) {
//            if (savingClipboard && fw != null) {
//				ClipData.Item item = clip.getItemAt(0);
//				CharSequence clipData = item.getText();
//				if (clipData != null && clipData.length() > 0) {
//					try {
//						fw.append(clipData + "\n");
//						fw.flush();
//					} catch (IOException e) {
//						Log.e(TAG, e.getMessage(), e);
//					}
//				}
//			}
//		}
//    }

}
