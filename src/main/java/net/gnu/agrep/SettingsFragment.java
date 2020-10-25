package net.gnu.agrep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.gnu.agrep.R;
import net.gnu.common.FolderChooserActivity;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.util.Log;
import android.widget.GridLayout;
import android.util.TypedValue;
import android.net.Uri;
import java.io.File;
import android.content.ClipData;
import java.util.TreeSet;
import java.util.regex.Pattern;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;
import android.support.v4.app.FragmentActivity;
import android.os.Parcelable;
import android.text.SpannableString;
import net.gnu.util.Util;
import android.graphics.Color;
import java.util.regex.Matcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.Spannable;

public class SettingsFragment extends Fragment implements GrepView.Callback {

    private static final String TAG = "SettingsFragment";
	
	final static int REQUEST_CODE_ADDDIC = 0x1001;
	public static final String SEARCH_FILES_SUFFIX = ".fb2; .epub;"
	+ ".txt; .pdf; .htm; .html; .shtm; .shtml; .xhtm; .xhtml;"
	+ ".xml; .rtf; .java; .c; .cpp; .h; .hpp; .md; .lua; .sh; .bat;"
	+ ".list; .depend; .js; .jsp; .mk; .config; .configure;"
	+ ".machine; .asm; .css; .desktop; .inc; .i; .ini; .plist;"
	+ ".pro; .py; .php; .s; .xpm; .bak; .doc; .xls; .ppt; .pps "
	+ ".pub; .vsd; .odt; .ods; .odp; .docx; .xlsx; .pptx;"
	
	+ ".7z; .bz2; .bzip2; .tbz2; .tbz; .gz; .gzip; .tgz;"
	+ ".tar; .swm; .xz; .txz; .zip; .zipx; .jar; .apk; .xpi;"
	+ ".apm;"
	+ ".ar; .a; .deb; .lib; .arj; .cab; .chm; .chw; .chi;"
	+ ".chq; .msi; .msp; .cpio;"
	+ ".cramfs; .dmg; .ext; .ext2; .ext3; .ext4; .img; .fat;"
	+ ".hfs; .hfsx; .hxs; .hxi; .hxr; .hxq; .hxw; .lit;"
	+ ".ihex; .iso; .lzh; .lha; .lzma; .mbr; .mslz; .mub;"
	+ ".nsis; .ntfs; .rar; .r00; .rpm; .ppmd; .qcow; .qcow2;"
	+ ".qcow2c; .squashfs; .udf; .iso; .scap; .uefif; .vdi;"
	+ ".vhd; .vmdk; .wim; .esd; .xar; .pkg; .z; .taz;"
	+ ".sz; .dump";
	
    public static final String SEARCH_FILES_SUFFIX_PATTERN = ".*?(" + SEARCH_FILES_SUFFIX.replaceAll("[\\s;\\.,]+", "|").substring(1) + ")";
	
	Prefs mPrefs;
    private ListView mDirListView;
    private GridLayout mExtListView;
    private View.OnLongClickListener mDirListener;
    private View.OnLongClickListener mExtListener;
    private CompoundButton.OnCheckedChangeListener mCheckListener;
    private AutoCompleteTextView edittext;
    private FragmentActivity mContext;
	
	public SlidingTabsFragment slidingTabsFragment;
	int no = 1;
	int count = 0;
	int index = 0;
	CharSequence status = "Long press to remove item";
	
	boolean fake = false;
	
	private boolean showGrep;
	GrepView mGrepView;
	TextView statusTV;

    private RetainFrag retainFrag;
    
	String mQuery = "";
	private String oldQuery = "";
	Pattern mPattern;
	private ArrayList<String> recent;
	private ArrayAdapter<String> mRecentAdapter;
	
	View upper;
	CheckBox chkRe;
	CheckBox chkIc;
	Parcelable state;
	
	public String getPath() {
		return null;
	}

	public SettingsFragment clone() {
		final SettingsFragment clone = new SettingsFragment();
		clone.setArguments(getArguments());
		clone.no = no;
		clone.index = index;
		clone.fake = true;
		clone.slidingTabsFragment = slidingTabsFragment;
		clone.mPrefs = mPrefs;
		clone.showGrep = showGrep;
		return clone;
	}

	public void clone(final SettingsFragment frag) {
		no = frag.no;
		fake = true;
		index = frag.index;
		slidingTabsFragment = frag.slidingTabsFragment;
		mPrefs = frag.mPrefs;
		Log.d(TAG, "clone fake " + fake + ", index " + index + ", no " + no + ", " + mContext + ", frag.retainFrag " + frag.retainFrag);
		if (mContext != null) {
			edittext.setText(frag.edittext.getText());
			chkRe.setChecked(frag.chkRe.isChecked());
			chkIc.setChecked(frag.chkIc.isChecked());
			refreshDirList();
			refreshExtList();
			if (frag.retainFrag != null) {
				mGrepView.setAdapter(frag.retainFrag.mAdapter);
				Parcelable state = frag.mGrepView.onSaveInstanceState();
				mGrepView.onRestoreInstanceState(state);
			}
			showGrep(frag.showGrep);
		}
		
	}

	public static SettingsFragment newInstance(final AGrepActivity activity) {
		final Bundle bundle = new Bundle();
        
		final int num = ++activity.increaseTabNo;
		bundle.putInt("no", num);
        activity.tabCount++;
		
		final SettingsFragment fragment = new SettingsFragment();
        fragment.no = num;
		fragment.setArguments(bundle);

        return fragment;
	}

	public String getTitle() {
		return "Tab " + no;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		//setRetainInstance(true);
		return inflater.inflate(R.layout.settings, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		final Bundle args = getArguments();
		Log.d(TAG, "onViewCreated args=" + args + ", savedInstanceState " + savedInstanceState);
		super.onViewCreated(view, savedInstanceState);
        mContext = this.getActivity();
		
		if (savedInstanceState != null) {
			no = savedInstanceState.getInt("no");
			index = savedInstanceState.getInt("index");
			mQuery = savedInstanceState.getString("mQuery");
			fake = savedInstanceState.getBoolean("fake");
			mPrefs = Prefs.loadPrefes(mContext, index);
			state = savedInstanceState.getParcelable("state");
			status = savedInstanceState.getCharSequence("status");
		} else { //new frag
			if (args != null) {
				no = args.getInt("no");
				if ((index = args.getInt("index", -1)) != -1) {
					Log.d(TAG, "onViewCreated, index " + index + ", fake " + fake);
					mPrefs = Prefs.loadPrefes(mContext, index);
					if (!fake) {
						index = slidingTabsFragment.getIndex(this);
					}
				} else {
					index = no;
					mPrefs = Prefs.loadPrefes(mContext, index);
				}
			}
		}
        
		FragmentManager supportFragmentManager = mContext.getSupportFragmentManager();
		retainFrag = (RetainFrag)supportFragmentManager.findFragmentByTag("retainFrag" + index);
		Log.d(TAG, "onViewCreated retainFrag.index" + index + ", " + retainFrag);
		if (retainFrag == null) {
			if (!fake) {
				retainFrag = new RetainFrag();
				final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
				transaction.add(retainFrag, "retainFrag" + index);
				transaction.commit();
				retainFrag.searchFragment = this;
			}
		}
		
		final Parcelable[] parcelableArray;
        if (savedInstanceState != null && !fake
			&& retainFrag.mData.size() < (parcelableArray = savedInstanceState.getParcelableArray("mData")).length) {
			retainFrag.mData.clear();
			Log.d(TAG, "onViewCreated.parcelableArray " + parcelableArray.length + ", retainFrag.mAdapter " + retainFrag.mAdapter);
			for (Parcelable obj : parcelableArray) {
				//Log.d(TAG, "onViewCreated.obj " + obj);
				retainFrag.mData.add((GrepView.Data)obj);
			}
			//retainFrag.mData.addAll((ArrayList<GrepView.Data>)savedInstanceState.getParcelableArrayList("mData"));
		}
		if (!fake && retainFrag.mAdapter == null) {
			retainFrag.mAdapter = new GrepView.GrepAdapter(mContext, R.layout.list_row, R.id.DicView01, retainFrag.mData);
			if (mPrefs.mIgnoreCase) {
				mPattern = Pattern.compile(mQuery, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
			} else {
				mPattern = Pattern.compile(mQuery);
			}
			retainFrag.mAdapter.setFormat(mPattern, mPrefs.mHighlightFg, mPrefs.mHighlightBg, mPrefs.mFontSize);
		}

        mGrepView = (GrepView)view.findViewById(R.id.DicView01);
		if (!fake) {
			mGrepView.setAdapter(retainFrag.mAdapter);
		}
        mGrepView.setCallback(this);

		final Intent intent = mContext.getIntent();
		mContext.setIntent(null);
		if (intent != null) {
			final Uri dataString = intent.getData();
			Log.d(TAG, "onViewCreated intent.data=" + dataString);
			final String scheme = intent.getScheme();
//			Log.d(TAG, "intent.scheme=" + scheme);
//			Log.d(TAG, "intent.component=" + intent.getComponent());
//			Log.d(TAG, "intent.package=" + intent.getPackage());
			if (dataString != null && "file".equals(scheme)) {
				final String path = dataString.getPath();
				mPrefs.mDirList.add(new CheckedString(path));
				//Log.d(TAG, "savePrefs1 " + no);
				mPrefs.savePrefs(mContext, index);
			}

			final ClipData clip = intent.getClipData();
			if (clip != null) {
				final int itemCount = clip.getItemCount();
				for (int i = 0; i < itemCount; i++) {
					final Uri uri = clip.getItemAt(i).getUri();
					//Log.d(TAG, "clip " + i + "=" + uri);
					mPrefs.mDirList.add(new CheckedString(uri.getPath()));
				}
				//Log.d(TAG, "savePrefs2 " + no);
				mPrefs.savePrefs(mContext, index);
			}
		}
		
        chkRe = (CheckBox)view.findViewById(R.id.checkre);
        chkIc = (CheckBox)view.findViewById(R.id.checkignorecase);
		statusTV = (TextView)view.findViewById(R.id.status);
        
		statusTV.setText(status);
		chkRe.setTextSize(TypedValue.COMPLEX_UNIT_SP, mPrefs.mFontSize);
		chkIc.setTextSize(TypedValue.COMPLEX_UNIT_SP, mPrefs.mFontSize);
		chkRe.setChecked(mPrefs.mRegularExrpression);
        chkIc.setChecked(mPrefs.mIgnoreCase);

		mDirListView = (ListView)view.findViewById(R.id.listdir);
        mExtListView = (GridLayout)view.findViewById(R.id.listext);
		edittext = (AutoCompleteTextView) view.findViewById(R.id.EditText01);
        
		mDirListView.setSmoothScrollbarEnabled(true);
        mDirListView.setScrollingCacheEnabled(true);
        mDirListView.setFocusable(true);
        mDirListView.setFocusableInTouchMode(true);
        mDirListView.setFastScrollEnabled(true);
        //setBackgroundColor(R.color.lightyellow);
        //setCacheColorHint(Color.WHITE);
        mDirListView.setDividerHeight(0);
		
		upper = view.findViewById(R.id.upper);
		if (savedInstanceState != null) {
			showGrep(savedInstanceState.getBoolean("showGrep"));
		} else {
			showGrep(false);
		}
		if (fake) {
			return;
		}
        
        mDirListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final CheckedString strItem = (CheckedString) view.getTag();
                // Show Dialog
                new AlertDialog.Builder(mContext)
					.setTitle(R.string.label_remove_item_title)
					.setMessage(getString(R.string.label_remove_item, strItem))
					.setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							mPrefs.mDirList.remove(strItem);
							refreshDirList();
							//Log.d(TAG, "savePrefs12 " + no);
							mPrefs.savePrefs(mContext, index);
						}
					})
					.setNegativeButton(R.string.label_CANCEL, null)
					.setCancelable(true)
					.show();
                return true;
            }
        };

        mExtListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String strText = (String) ((TextView)view).getText();
                final CheckedString strItem = (CheckedString) view.getTag();
                // Show Dialog
                new AlertDialog.Builder(mContext)
					.setTitle(R.string.label_remove_item_title)
					.setMessage(getString(R.string.label_remove_item, strText))
					.setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							mPrefs.mExtList.remove(strItem);
							refreshExtList();
							//Log.d(TAG, "savePrefs11 " + no);
							mPrefs.savePrefs(mContext, index);
						}
					})
					.setNegativeButton(R.string.label_CANCEL, null)
					.setCancelable(true)
					.show();
                return true;
            }
        };

        mCheckListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final CheckedString strItem = (CheckedString) buttonView.getTag();
                strItem.checked = isChecked;
                //Log.d(TAG, "savePrefs3 " + no);
				if (strItem.string.startsWith("/")) {
					mPrefs.mDirList.remove(strItem);
					mPrefs.mDirList.add(strItem);
					Collections.sort(mPrefs.mDirList);
				} else {
					mPrefs.mExtList.remove(strItem);
					mPrefs.mExtList.add(strItem);
					Collections.sort(mPrefs.mExtList);
				}
				mPrefs.savePrefs(mContext, index);
            }
        };

        final ImageButton btnAddDir = (ImageButton) view.findViewById(R.id.adddir);
        final ImageButton btnAddExt = (ImageButton) view.findViewById(R.id.addext);
		final ImageButton btnClearDir = (ImageButton) view.findViewById(R.id.ButtonClearDir);
        final ImageButton btnClearExt = (ImageButton) view.findViewById(R.id.ButtonClearExt);
		
        btnAddDir.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					
//					Intent intent = new Intent(mContext, FolderChooserActivity.class);
//					startActivityForResult(intent, REQUEST_CODE_ADDDIC);
					
					Intent intent = new Intent(mContext, FolderChooserActivity.class);
					//Log.d("newSearch.oldselectedFiles", Util.arrayToString(selectedFiles, true, MainFragment.LINE_SEP));
					intent.putExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
					intent.putExtra(FolderChooserActivity.EXTRA_FILTER_FILETYPE, SEARCH_FILES_SUFFIX);
					intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
					intent.putExtra(FolderChooserActivity.CHOOSER_TITLE, "Pick files/folder");
					startActivityForResult(intent, REQUEST_CODE_ADDDIC);
				}
			});

        btnClearDir.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					if (mPrefs.mDirList.size() > 0)
						new AlertDialog.Builder(mContext)
							.setTitle(R.string.label_remove_all_item_title)
							.setMessage(getString(R.string.label_remove_all_item))
							.setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mPrefs.mDirList.clear();
									refreshDirList();
									//Log.d(TAG, "savePrefs4 " + no);
									mPrefs.savePrefs(mContext, index);
								}
							})
							.setNegativeButton(R.string.label_CANCEL, null)
							.setCancelable(true)
							.show();
				}
			});
			
        btnAddExt.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					// Create EditText
					final EditText edtInput = new EditText(mContext);
					edtInput.setSingleLine();
					// Show Dialog
					new AlertDialog.Builder(mContext)
						.setTitle(R.string.label_addext)
						.setView(edtInput)
						.setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								
								final String ext = edtInput.getText().toString();
								if (ext != null && ext.trim().length() > 0) {
									final String[] arr = ext.split("[\\s;,\\.]+");
									boolean dup;
									for (String s : arr) {
										dup = false;
										for (CheckedString t : mPrefs.mExtList) {
											if (t.string.equalsIgnoreCase(ext)) {
												dup = true;
												break;
											}
										}
										if (!dup) {
											mPrefs.mExtList.add(new CheckedString(s.toLowerCase()));
										}
									}
									refreshExtList();
									//Log.d(TAG, "savePrefs5 " + no);
									mPrefs.savePrefs(mContext, index);
								}
							}
						})
						.setNeutralButton(R.string.label_no_extension, new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int whichButton) {
								
								String ext = "*";
								for (CheckedString t : mPrefs.mExtList) {
									if (t.string.equalsIgnoreCase(ext)) {
										return;
									}
								}
								mPrefs.mExtList.add(new CheckedString(ext));
								refreshExtList();
								//Log.d(TAG, "savePrefs6 " + no);
								mPrefs.savePrefs(mContext, index);
							}
						})
						.setNegativeButton(R.string.label_CANCEL, null)
						.setCancelable(true)
						.show();
					edtInput.requestFocus();
				}
			});

		btnClearExt.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					if (mPrefs.mExtList.size() > 0)
						new AlertDialog.Builder(mContext)
						.setTitle(R.string.label_remove_all_item_title)
						.setMessage(getString(R.string.label_remove_all_item))
						.setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								mPrefs.mExtList.clear();
								refreshExtList();
								//Log.d(TAG, "savePrefs7 " + no);
								mPrefs.savePrefs(mContext, index);
							}
						})
						.setNegativeButton(R.string.label_CANCEL, null)
						.setCancelable(true)
						.show();
				}
			});

        
        chkRe.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mPrefs.mRegularExrpression = chkRe.isChecked();
					//Log.d(TAG, "savePrefs8 " + no);
					mPrefs.savePrefs(mContext, index);
				}
			});
        chkIc.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mPrefs.mIgnoreCase = chkIc.isChecked();
					//Log.d(TAG, "savePrefs9 " + no);
					mPrefs.savePrefs(mContext, index);
				}
			});

        edittext.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
						//String patternText = edittext.getEditableText().toString();
						//Intent it = new Intent(mContext, Search.class);
						//it.setAction(Intent.ACTION_SEARCH);
						//it.putExtra(SearchManager.QUERY, patternText);
						//startActivity(it);
						search();
						return true;
					}
					return false;
				}
			});
        recent = mPrefs.getRecent(mContext, index);
        mRecentAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, recent);
        //mRecentAdapter.addAll(recent);
		edittext.setAdapter(mRecentAdapter);
		if (savedInstanceState != null) {
			edittext.setText(savedInstanceState.getCharSequence("edittext"));
		} 
		
        final ImageButton clrBtn = (ImageButton) view.findViewById(R.id.ButtonClear);
        clrBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					edittext.setText("");
					edittext.requestFocus();
				}
			});

        clrBtn.setOnLongClickListener(new View.OnLongClickListener() {
				public boolean onLongClick(View view) {
					if (mPrefs.mDirList.size() > 0)
						new AlertDialog.Builder(mContext)
							.setTitle(R.string.label_remove_all_item_title)
							.setMessage(getString(R.string.label_remove_all_item))
							.setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mRecentAdapter.clear();
									mRecentAdapter.notifyDataSetChanged();
									mPrefs.clearRecent(mContext, index);
									recent.clear();
									edittext.setText("");
									edittext.requestFocus();
								}
							})
							.setNegativeButton(R.string.label_CANCEL, null)
							.setCancelable(true)
							.show();
					return false;
				}
			});

        final ImageButton searchBtn = (ImageButton)view.findViewById(R.id.ButtonSearch);
        searchBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					//String text = edittext.getText().toString();
					//Intent it = new Intent(mContext, Search.class);
					//it.setAction(Intent.ACTION_SEARCH);
					//it.putExtra(SearchManager.QUERY, text);
					//it.putExtra("no", no);
					//startActivity(it);
					search();
				}
			});

        final ImageButton historyBtn = (ImageButton)view.findViewById(R.id.ButtonHistory);
        historyBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					edittext.showDropDown();
				}
			});
    }
	
	private void search() {
		mQuery = edittext.getEditableText().toString();
		if (mQuery.length() == 0) {
			showGrep = !showGrep;
			showGrep(showGrep);
			return;
		}
		oldQuery = mQuery;
		Log.d(TAG, "search existed " + recent.indexOf(mQuery) + ", mQuery " + mQuery + ", recent " + recent);
		if (recent.indexOf(mQuery) < 0) {
			recent.add(mQuery);
			mRecentAdapter.add(mQuery);
			//edittext.setAdapter(mRecentAdapter);
			((ArrayAdapter<String>)edittext.getAdapter()).notifyDataSetChanged();//.add(mQuery);
			mPrefs.addRecent(mContext, index, mQuery);
		}
		if (!mPrefs.mRegularExrpression) {
			mQuery = mQuery.replaceAll(Util.SPECIAL_CHAR_PATTERNSTR, "\\\\$1");
			//mQuery = "(" + mQuery.replaceAll("\\s+", "|") + ")";
		}

		if (mPrefs.mIgnoreCase) {
			mPattern = Pattern.compile(mQuery, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
		} else {
			mPattern = Pattern.compile(mQuery);
		}
		showGrep(true);
		
		Log.d(TAG, "search.retainFrag.mTask " + retainFrag.mTask + ", index " + index + ", fake " +fake);
		if (retainFrag.mTask != null) {
			retainFrag.mTask.cancel(true);
		}
		retainFrag.mData.clear();
		retainFrag.mAdapter.setFormat(mPattern, mPrefs.mHighlightFg, mPrefs.mHighlightBg, mPrefs.mFontSize);
		retainFrag.mTask = new GrepTask(retainFrag);//SettingsFragment.this);
		retainFrag.mTask.execute(mQuery);
	}
	
	void showGrep(boolean showGrep) {
		this.showGrep = showGrep;
		if (showGrep) {
			mGrepView.onRestoreInstanceState(state);
			upper.setVisibility(View.GONE);
			mGrepView.setVisibility(View.VISIBLE);
		} else {
			state = mGrepView.onSaveInstanceState();
			mGrepView.setVisibility(View.GONE);
			upper.setVisibility(View.VISIBLE);
		}
	}
	
    @Override
    public void onResume() {
        Log.d(TAG, "onResume fake " + fake + ", index " + index + ", retainFrag " + retainFrag);
		super.onResume();
		if (!fake) {
			retainFrag.searchFragment = this;
			refreshDirList();
			refreshExtList();
		}
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putInt("no", no);
		outState.putInt("index", index);
		outState.putParcelable("state", state);
		if (!fake) {
			outState.putParcelableArray("mData", retainFrag.mData.toArray(new GrepView.Data[0]));
			mPrefs.savePrefs(mContext, index);
		}
		outState.putBoolean("showGrep", showGrep);
		outState.putBoolean("fake", fake);
		outState.putString("mQuery", oldQuery);
		outState.putCharSequence("edittext", edittext.getText());
		outState.putCharSequence("status", statusTV.getText());
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult.requestCode " + requestCode);
        if (requestCode == REQUEST_CODE_ADDDIC && resultCode == Activity.RESULT_OK && data != null) {
            final String[] dirname = data.getStringArrayExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH);
			//final String dirname = data.getExtras().getString(FileSelectorActivity.INTENT_FILEPATH);
            boolean addNew = false;
			if (dirname != null && dirname.length > 0) {
				boolean dup;
                for (String s : dirname) {
					dup = false;
					for (CheckedString t : mPrefs.mDirList) {
						if (t.string.equalsIgnoreCase(s)) {
							dup = true;
							break;
						}
					}
					if (!dup) {
						mPrefs.mDirList.add(new CheckedString(s));
						retainFrag.fileList = null;
						addNew = true;
					}
                }
				if (addNew) {
					refreshDirList();
					//Log.d(TAG, "savePrefs10 " + no);
					mPrefs.savePrefs(mContext, index);
				}
            }
        } else {//}if (requestCode == AGrepActivity.REQUEST_CODE_PREFS) {
			mPrefs = Prefs.loadPrefes(mContext, index);
			chkRe.setTextSize(TypedValue.COMPLEX_UNIT_SP, mPrefs.mFontSize);
			chkIc.setTextSize(TypedValue.COMPLEX_UNIT_SP, mPrefs.mFontSize);
			refreshExtList();
			refreshDirList();
		}
    }

    void setListItem(ViewGroup view,
					 ArrayList<CheckedString> list,
					 View.OnLongClickListener logclicklistener,
					 CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        view.removeAllViews();
        Collections.sort(list);
        for (CheckedString s : list) {
            CheckBox v = (CheckBox)View.inflate(mContext, R.layout.list_dir, null);
            v.setTextSize(TypedValue.COMPLEX_UNIT_SP, mPrefs.mFontSize);
            if (s.string.equals("*")) {
                v.setText(R.string.label_no_extension);
            } else {
                v.setText(s.string);
            }
			v.setChecked(s.checked);
			//Log.d(TAG, "setListItem " + s);
			
			v.setTag(s);
            view.addView(v);
			v.setOnLongClickListener(logclicklistener);
            v.setOnCheckedChangeListener(checkedChangeListener);
		}
    }

    private void refreshDirList() {
		mDirListView.setAdapter(new DirAdapter(mContext, android.R.layout.simple_list_item_checked, android.R.id.text1, mPrefs.mDirList));
        //setListItem(mDirListView, mPrefs.mDirList, mDirListener, mCheckListener);
    }
    private void refreshExtList() {
        setListItem(mExtListView, mPrefs.mExtList, mExtListener, mCheckListener);
    }

	@Override
	public void onAttachFragment(Fragment childFragment) {
		Log.d(TAG, "childFragment " + childFragment);
		super.onAttachFragment(childFragment);
//		if (childFragment instanceof RetainFrag mTask != null) {
//			mTask.searchActivity = SettingsFragment.this;
//		}
	}
	
	class DirAdapter extends ArrayAdapter<CheckedString> {

        boolean checked;
		String string;

        class ViewHolder {
            CheckBox checked;
			//TextView string;
        }

        public DirAdapter(final Context context, final int resource, final int textViewResourceId, final ArrayList<CheckedString> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CheckBox view;
            
            if (convertView != null) {
                view = (CheckBox) convertView;
                
            } else {
                view = new CheckBox(mContext);//getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_checked, null);
                view.setTextColor(Color.BLACK);
				view.setOnCheckedChangeListener(mCheckListener);
				view.setOnLongClickListener(mDirListener);
				view.setTextSize(TypedValue.COMPLEX_UNIT_SP, mPrefs.mFontSize);
            }
            final CheckedString d = getItem(position);
			view.setTag(d);
            view.setChecked(d.checked);
            view.setText(d.string);

            return view;
        }

        
    }

    public static SpannableString highlightKeyword(CharSequence text, Pattern p, int fgcolor, int bgcolor) {
        SpannableString ss = new SpannableString(text);

        int start = 0;
        int end;
        Matcher m = p.matcher(text);
        while (m.find(start)) {
            start = m.start();
            end = m.end();

            BackgroundColorSpan bgspan = new BackgroundColorSpan(bgcolor);
            ss.setSpan(bgspan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ForegroundColorSpan fgspan = new ForegroundColorSpan(fgcolor);
            ss.setSpan(fgspan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            start = end;
        }
        return ss;
    }

    @Override
    public void onGrepItemClicked(int position) {
        GrepView.Data data = (GrepView.Data) mGrepView.getAdapter().getItem(position);

        Intent it = new Intent(mContext, TextViewer.class);

        it.putExtra(TextViewer.EXTRA_PATH, data.mFile.getAbsolutePath());
        it.putExtra(TextViewer.EXTRA_QUERY, oldQuery);
        it.putExtra(TextViewer.EXTRA_LINE, data.mLinenumber);

		Log.d(TAG, "onGrepItemClicked EXTRA_PATH " + data.mFile.getAbsolutePath() + ", mQuery " + mQuery + ", data.mLinenumber " + data.mLinenumber);
        startActivity(it);
    }

    @Override
    public boolean onGrepItemLongClicked(int position) {
        return false;
    }
}
