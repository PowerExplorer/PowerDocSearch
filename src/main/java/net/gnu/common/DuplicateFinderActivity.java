package net.gnu.common;

import android.app.*;
import android.os.*;
import android.util.*;
import net.gnu.agrep.R;
import android.widget.*;
import java.util.*;
import android.webkit.*;
import net.gnu.androidutil.*;
import android.view.*;
import android.content.*;
import android.net.*;
import android.view.View.*;
import android.graphics.*;
import java.io.*;
import net.gnu.util.ComparableEntry;
import net.gnu.util.Util;

public class DuplicateFinderActivity extends Activity implements View.OnClickListener {
	
	private static final String TAG = "DuplicateFinderActivity";
	private static final int DUP_REQUEST_CODE = 7;
	
	private DupFinderTask dupTask;
	static final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
	private DupAdapter srcAdapter;
	private ImageThreadLoader imageLoader;
	private LinkedList<List<FileInfo>> groupList;
	private LinkedList<FileInfo> gList;
	private LinkedList<FileInfo> selectedInList = new LinkedList<>();
	private TextView statusView;
	private ListView listView;

	private TextView dupInfo;
	private ImageButton allMenu;
	private TextView allGroup;
	private TextView allName;
	private TextView allDate;
	private TextView allPath;
	private TextView allType;
	private CharSequence status = "";
	
	private boolean groupViewChanged = false;
	private ActionBar actionBar;
	private List<String> fs = new LinkedList<>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.duplicate);
		
		actionBar = getActionBar();
		final View customView = getLayoutInflater().inflate(R.layout.dup_button_title, null);
		actionBar.setCustomView(customView);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.show();
		
		statusView = (TextView) findViewById(R.id.statusView);
		listView = (ListView) findViewById(R.id.files);
		dupInfo = (TextView)findViewById(R.id.dupInfo);

		allMenu = (ImageButton) findViewById(R.id.allMenu);
		allGroup = (TextView) findViewById(R.id.allGroup);
		allName = (TextView) findViewById(R.id.allName);
		allDate = (TextView) findViewById(R.id.allDate);
		allPath = (TextView) findViewById(R.id.allPath);
		allType = (TextView) findViewById(R.id.allType);

		allMenu.setOnClickListener(this);
		allGroup.setOnClickListener(this);
		allName.setOnClickListener(this);
		allDate.setOnClickListener(this);
		allPath.setOnClickListener(this);
		allType.setOnClickListener(this);
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onPostCreate " + savedInstanceState);
		super.onPostCreate(savedInstanceState);
		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		Log.d(TAG, "onNewIntent " + intent);
		super.onNewIntent(intent);
		final int oldSize = fs.size();
		final Uri dataString = intent.getData();
		Log.d(TAG, "intent.data=" + dataString);
		final String scheme = intent.getScheme();
		Log.d(TAG, "intent.scheme=" + scheme);
		Log.d(TAG, "intent.component=" + intent.getComponent());
		Log.d(TAG, "intent.package=" + intent.getPackage());
		Log.d(TAG, "action=" + intent.getAction() + ", cat=" + intent.getCategories() + ", extra=" + intent.getExtras() + ", type " + intent.getType());

		if (dataString != null && "file".equals(scheme)) {
			final String path = dataString.getPath();
			fs.add(path);
		}

		final ClipData clip = intent.getClipData();
		if (clip != null) {
			final int itemCount = clip.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				final Uri uri = clip.getItemAt(i).getUri();
				//Log.d(TAG, "clip " + i + "=" + uri);
				fs.add(uri.getPath());
			}
		}
		if (fs.size() > oldSize) {
			dupTask = new DupFinderTask(DuplicateFinderActivity.this, fs);
			dupTask.execute();
			findViewById(R.id.titleBar).setVisibility(View.VISIBLE);
			findViewById(R.id.horizontalDivider).setVisibility(View.VISIBLE);
			findViewById(R.id.horizontalDivider2).setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("groupList", groupList);
		outState.putSerializable("gList", gList);
		outState.putCharSequence("status", status);
		outState.putBoolean("groupViewChanged", groupViewChanged);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (groupList == null) {
			groupList = (LinkedList<List<FileInfo>>) savedInstanceState.getSerializable("groupList");
			gList = (LinkedList<FileInfo>) savedInstanceState.getSerializable("gList");
			//statusView.setText(status);
			groupViewChanged = savedInstanceState.getBoolean("groupViewChanged");
			srcAdapter = new DupAdapter(this, R.layout.dup_list_item, gList, groupList);
			listView.setAdapter(srcAdapter);
			srcAdapter.notifyDataSetChanged();
			srcAdapter.displayGroup(groupList);
		}
		
	}
	
	

	@Override
    public void onPause() {
        super.onPause();
		Log.d(TAG, "onPause");
		if (imageLoader != null) {
			imageLoader.stopThread();
		}
    }

    @Override
    public void onResume() {
        super.onResume();
		Log.d(TAG, "onResume");
		imageLoader = new ImageThreadLoader(this, 36, 40);
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "data " + data);
		if (requestCode == DUP_REQUEST_CODE) {
			if (data != null) {
				if (resultCode == Activity.RESULT_OK) {
					String[] stringExtra = data.getStringArrayExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH);
					Log.d("DUP_REQUEST_CODE.selectedFiles", Util.arrayToString(stringExtra, true, "\n"));
					fs = new ArrayList<>(stringExtra.length);
					for (String st : stringExtra) {
						fs.add(st);
					}
					dupTask = new DupFinderTask(DuplicateFinderActivity.this, fs);
					dupTask.execute();
					findViewById(R.id.titleBar).setVisibility(View.VISIBLE);
					findViewById(R.id.horizontalDivider).setVisibility(View.VISIBLE);
					findViewById(R.id.horizontalDivider2).setVisibility(View.VISIBLE);
				} else { // RESULT_CANCEL
					showToast("Nothing to find");
					statusView.setText("Nothing to find");
				}
			}
		}
	}
	
	public void setText(CharSequence cs) {
		statusView.setText(cs);
	}

	public void showToast(CharSequence st) {
		Toast.makeText(this, st, Toast.LENGTH_LONG).show();
	}
	
	public void loadFiles(View view) {
		//Log.d("dupFinder.previous selectedFiles", selectedFiles + ".");
		Intent intent = new Intent(DuplicateFinderActivity.this, FolderChooserActivity.class);
		intent.putExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH, fs.toArray(new String[0]));
//		intent.putExtra(FolderChooserActivity.EXTRA_FILTER_FILETYPE, ALL_SUFFIX);
//		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, MULTI_FILES);
//		intent.putExtra(FolderChooserActivity.CHOOSER_TITLE, ALL_SUFFIX_TITLE);
		DuplicateFinderActivity.this.startActivityForResult(intent, DUP_REQUEST_CODE);
		
	}
	
	public void cancel(View view) {
//		Log.d("select previous file", Util.arrayToString(previousSelectedStr, true, "\r\n"));
		Intent intent = this.getIntent();
//		if (previousSelectedStr != null && previousSelectedStr.length > 0) {
//			Arrays.sort(previousSelectedStr);
//		}
//		intent.putExtra(EXTRA_ABSOLUTE_PATH, previousSelectedStr);
		setResult(RESULT_CANCELED, intent);
	    this.finish();
	}
	
	public void setDupList(final LinkedList<List<FileInfo>> groupList) {
		this.groupList = groupList;
		allGroup.setText("Group ▼");
		groupViewChanged = true;
		gList = new LinkedList<>();
		for (List<FileInfo> lf : groupList) {
			for (FileInfo fi : lf) {
				gList.add(fi);
			}
		}
		srcAdapter = new DupAdapter(this, R.layout.dup_list_item, gList, groupList);
		listView.setAdapter(srcAdapter);
		srcAdapter.notifyDataSetChanged();
		srcAdapter.displayGroup(groupList);
	}

	@Override
	public void onClick(View p1) {
		switch (p1.getId()) {
			case R.id.allGroup:
				allGroup(p1);
				break;
			case R.id.allMenu:
				mainmenu(p1);
				break;
			case R.id.allName:
				allName(p1);
				break;
			case R.id.allDate:
				allDate(p1);
				break;
			case R.id.allPath:
				allPath(p1);
				break;
			case R.id.allType:
				allType(p1);
				break;
		}
	}

	public void mainmenu(final View v) {
		final PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.dupallmain, popup.getMenu());
		if (selectedInList.size() == 0) {
			popup.getMenu().removeItem(R.id.clear);
		}
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
						case R.id.delete:
							deletes(item);
							break;
						case R.id.send:
							sends(item);
							break;
						case R.id.all:
							all(item);
							break;
						case R.id.invert:
							invert(item);
							break;
						case R.id.clear:
							clear(item);
							break;
					}
					return true;
				}
			});
		popup.show();
	}


	public boolean deletes(final MenuItem item) {
		if (selectedInList.size() > 0) {
			final TextView editText = new TextView(this);
			editText.setText(Util.collectionToString(selectedInList.subList(0, Math.min(3, selectedInList.size())), true, "\n") + "...");
			AlertDialog dialog = new AlertDialog.Builder(this)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle("Delete " + selectedInList.size() + " files?")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int counter = 0;
						String statusDel;
						for (FileInfo ff : selectedInList) {
							boolean ret = AndroidPathUtils.deleteFile(ff.file, DuplicateFinderActivity.this);// new File(selectedPath).delete();
							if (ret) {
								counter++;
								statusDel = "Deleted file \"" + ff.path + "\" successfully";
							} else {
								statusDel = "Cannot delete file \"" + ff.path + "\"";
							}
							statusView.setText(statusDel);
						}
						statusView.setText("Deleted " + counter + " files");
						if (counter > 0) {
							srcAdapter.notifyDataSetChanged();
							srcAdapter.displayGroup(groupList);
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

	public boolean sends(final MenuItem item) {
		if (selectedInList.size() > 0) {
			ArrayList<Uri> uris = new ArrayList<Uri>(selectedInList.size());
			Intent send_intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			send_intent.setFlags(0x1b080001);

			send_intent.setType("*/*");
			for(FileInfo st : selectedInList) {
				uris.add(Uri.fromFile(st.file));
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

	public boolean clear(final MenuItem item) {
		selectedInList.clear();
		srcAdapter.notifyDataSetChanged();
		statusView.setText(selectedInList.size() + "/"+ srcAdapter.getCount());
		return true;
	}

	public boolean all(final MenuItem item) {
		selectedInList.clear();
		for (FileInfo fi : gList) {
			if (fi.file.exists()) {
				selectedInList.add(fi);
			}
		}
		srcAdapter.notifyDataSetChanged();
		statusView.setText(selectedInList.size() + "/"+ srcAdapter.getCount());
		return true;
	}

	public boolean invert(final MenuItem item) {
		List<FileInfo> tempL = new LinkedList<>();
		for (FileInfo fi : gList) {
			if (!selectedInList.contains(fi) && fi.file.exists()) {
				tempL.add(fi);
			}
		}
		selectedInList.clear();
		selectedInList.addAll(tempL);
		srcAdapter.notifyDataSetChanged();
		statusView.setText(selectedInList.size() + "/"+ srcAdapter.getCount());
		return true;
	}

	public void allGroup(View view) {
		if (gList == null) {
			return;
		}
		groupViewChanged = true;
		if (allGroup.getText().toString().equals("Group ▲")) {
			allGroup.setText("Group ▼");
			Collections.sort(gList, new FileInfo.SortGroupDecrease());
		} else {
			allGroup.setText("Group ▲");
			Collections.sort(gList, new FileInfo.SortGroupIncrease());
		}
		allName.setText("Name");
		allDate.setText("Date");
		allPath.setText("Path   ");
		allType.setText("Type");
		srcAdapter.notifyDataSetChanged();
	}

	public void allName(View view) {
		if (gList == null) {
			return;
		}
		groupViewChanged = false;
		if (allName.getText().toString().equals("Name ▲")) {
			allName.setText("Name ▼");
			Collections.sort(gList, new FileInfo.SortFileOnlyNameDecrease());
		} else {
			allName.setText("Name ▲");
			Collections.sort(gList, new FileInfo.SortFileOnlyNameIncrease());
		}
		allGroup.setText("Group");
		allDate.setText("Date");
		allPath.setText("Path   ");
		allType.setText("Type");
		srcAdapter.notifyDataSetChanged();
	}

	public void allDate(View view) {
		if (gList == null) {
			return;
		}
		groupViewChanged = false;
		if (allDate.getText().toString().equals("Date ▲")) {
			allDate.setText("Date ▼");
			Collections.sort(gList, new FileInfo.SortFileOnlyDateDecrease());
		} else {
			allDate.setText("Date ▲");
			Collections.sort(gList, new FileInfo.SortFileOnlyDateIncrease());
		}
		allGroup.setText("Group");
		allName.setText("Name");
		allPath.setText("Path   ");
		allType.setText("Type");
		srcAdapter.notifyDataSetChanged();
	}

	public void allPath(View view) {
		if (gList == null) {
			return;
		}
		groupViewChanged = false;
		if (allPath.getText().toString().equals("Path ▲")) {
			allPath.setText("Path ▼");
			Collections.sort(gList, new FileInfo.SortFilePathDecrease());
		} else {
			allPath.setText("Path ▲");
			Collections.sort(gList, new FileInfo.SortFilePathIncrease());
		}
		allGroup.setText("Group");
		allName.setText("Name");
		allDate.setText("Date");
		allType.setText("Type");
		srcAdapter.notifyDataSetChanged();
	}

	public void allType(View view) {
		if (gList == null) {
			return;
		}
		groupViewChanged = false;
		if (allType.getText().toString().equals("Type ▲")) {
			allType.setText("Type ▼");
			Collections.sort(gList, new FileInfo.SortFileOnlyTypeDecrease());
		} else {
			allType.setText("Type ▲");
			Collections.sort(gList, new FileInfo.SortFileOnlyTypeIncrease());
		}
		allGroup.setText("Group");
		allName.setText("Name");
		allDate.setText("Date");
		allPath.setText("Path   ");
		srcAdapter.notifyDataSetChanged();
	}

	class DupAdapter extends ArrayAdapter<FileInfo> implements OnLongClickListener, OnClickListener {
		private final String TAG = "MainFragment.DupAdapter";

		private static final int LIGHT_BROWN = 0xFFFFE6D9;
		private static final int LIGHT_YELLOW = 0xFFFFFFF0;
		private static final int LIGHT_YELLOW2 = 0xFFFFF8D9;
		private static final int LIGHT_YELLOW3 = 0xFFF7C0C1;

		LinkedList<List<FileInfo>> groupList;
		public DupAdapter(Context context, int textViewResourceId,
						  List<FileInfo> objects, 
						  final LinkedList<List<FileInfo>> groupList) {
			super(context, textViewResourceId, objects);
			this.groupList = groupList;
		}

		private class Holder extends net.gnu.common.Holder {
			final CheckBox cbx;
			final TextView folder;
			final TextView name;
			final TextView items;
			final TextView attr;
			final TextView lastModified;
			final TextView type;

			final TextView group;
			final ImageView image;
			final ImageButton more;
			//FileInfo fileInfo;
			Holder(final View convertView) {
				cbx = (CheckBox) convertView.findViewById(R.id.cbx);
				folder = (TextView) convertView.findViewById(R.id.folder);
				name = (TextView) convertView.findViewById(R.id.name);
				items = (TextView) convertView.findViewById(R.id.items);
				attr = (TextView) convertView.findViewById(R.id.attr);
				lastModified = (TextView) convertView.findViewById(R.id.lastModified);
				type = (TextView) convertView.findViewById(R.id.type);

				group = (TextView) convertView.findViewById(R.id.group);
				image = (ImageView)convertView.findViewById(R.id.icon);
				more = (ImageButton)convertView.findViewById(R.id.more);

				cbx.setOnClickListener(DupAdapter.this);
				folder.setOnLongClickListener(DupAdapter.this);
				folder.setOnClickListener(DupAdapter.this);
				name.setOnLongClickListener(DupAdapter.this);
				name.setOnClickListener(DupAdapter.this);
				items.setOnLongClickListener(DupAdapter.this);
				items.setOnClickListener(DupAdapter.this);
				attr.setOnLongClickListener(DupAdapter.this);
				attr.setOnClickListener(DupAdapter.this);
				lastModified.setOnLongClickListener(DupAdapter.this);
				lastModified.setOnClickListener(DupAdapter.this);
				type.setOnLongClickListener(DupAdapter.this);
				type.setOnClickListener(DupAdapter.this);
				group.setOnLongClickListener(DupAdapter.this);
				group.setOnClickListener(DupAdapter.this);
				convertView.setOnLongClickListener(DupAdapter.this);
				convertView.setOnClickListener(DupAdapter.this);
				image.setOnLongClickListener(DupAdapter.this);
				image.setOnClickListener(DupAdapter.this);
				more.setOnClickListener(DupAdapter.this);

				final int parseColor = Color.parseColor("#ff666666");
				folder.setTextColor(parseColor);
				items.setTextColor(parseColor);
				attr.setTextColor(parseColor);
				lastModified.setTextColor(parseColor);
				type.setTextColor(parseColor);
				more.setColorFilter(parseColor);

				cbx.setTag(this);
				folder.setTag(this);
				image.setTag(this);
				name.setTag(this);
				items.setTag(this);
				attr.setTag(this);
				lastModified.setTag(this);
				type.setTag(this);
				group.setTag(this);
				more.setTag(this);
				convertView.setTag(this);
			}
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			final Holder holder;
			if (convertView == null) {
                convertView = DuplicateFinderActivity.this.getLayoutInflater().inflate(R.layout.dup_list_item, parent, false);
				holder = new Holder(convertView);
            } else {
				holder = (Holder) convertView.getTag();
			}

            final CheckBox cbx = holder.cbx;
			final TextView folder = holder.folder;
			final TextView name = holder.name;
			final TextView items = holder.items;
			final TextView attr = holder.attr;
			final TextView lastModified = holder.lastModified;
			final TextView type = holder.type;

			final TextView group = holder.group;
			final ImageView image = holder.image;
			final ImageButton more = holder.more;

			final FileInfo item = getItem(position);
			final File f = item.file;
			final String fileName = f.getName();
			//Log.d(TAG, "getView fileName " + fileName);
			final String fPath = f.getAbsolutePath();

			holder.fileInfo = item;

			folder.setText(f.getParent());
			name.setText(fileName);
			group.setText(Util.nf.format(item.groupNo));
			items.setText(Util.nf.format(item.length) + " B");
			lastModified.setText(Util.dtf.format(item.file.lastModified()));
			final int lastIndexOf = fileName.lastIndexOf(".");
			final String ext = (lastIndexOf >= 0 && lastIndexOf < fileName.length() - 1) ? fileName.substring(lastIndexOf + 1).toLowerCase() : "";
			type.setText(ext);
			if (ext.equals("apk")) { //decide are the file folder or file
				imageLoader.displayImage(fPath, DuplicateFinderActivity.this, image, imageLoader.apkIcon);
			} else if (ext.equals("jpg")
					   ||ext.equals("png")
					   ||ext.equals("gif")
					   ||ext.equals("jpeg")
					   ||ext.equals("tiff")){
				  imageLoader.displayImage(fPath, DuplicateFinderActivity.this, image, imageLoader.stubIcon);
			} else {
				image.setImageDrawable(ImageThreadLoader.getFileIcon(ext));
			}
			if (selectedInList.contains(item)) {
				cbx.setChecked(true);
			} else {
				cbx.setChecked(false);
			}
	        if (!f.exists()) {
				cbx.setChecked(false);
				cbx.setEnabled(false);
				attr.setText("---");
				cbx.setBackgroundColor(LIGHT_YELLOW3);
				folder.setBackgroundColor(LIGHT_YELLOW3);
				name.setBackgroundColor(LIGHT_YELLOW3);
				items.setBackgroundColor(LIGHT_YELLOW3);
				attr.setBackgroundColor(LIGHT_YELLOW3);
				lastModified.setBackgroundColor(LIGHT_YELLOW3);
				type.setBackgroundColor(LIGHT_YELLOW3);
				convertView.setBackgroundColor(LIGHT_YELLOW3);
				image.setBackgroundColor(LIGHT_YELLOW3);
				more.setBackgroundColor(LIGHT_YELLOW3);
				group.setBackgroundColor(LIGHT_YELLOW3);
				name.setTextColor(Color.RED);
				return convertView;
			}
			cbx.setEnabled(true);
	        if (item.groupNo % 2 == 0 || !groupViewChanged) {
				name.setTextColor(Color.BLACK);
				cbx.setBackgroundColor(LIGHT_YELLOW);
				folder.setBackgroundColor(LIGHT_YELLOW);
				name.setBackgroundColor(LIGHT_YELLOW);
				items.setBackgroundColor(LIGHT_YELLOW);
				attr.setBackgroundColor(LIGHT_YELLOW);
				lastModified.setBackgroundColor(LIGHT_YELLOW);
				type.setBackgroundColor(LIGHT_YELLOW);
				convertView.setBackgroundColor(LIGHT_YELLOW);
				image.setBackgroundColor(LIGHT_YELLOW);
				more.setBackgroundColor(LIGHT_YELLOW);
				group.setBackgroundColor(LIGHT_YELLOW);
			} else {
				name.setTextColor(Color.BLACK);
				cbx.setBackgroundColor(LIGHT_YELLOW2);
				folder.setBackgroundColor(LIGHT_YELLOW2);
				name.setBackgroundColor(LIGHT_YELLOW2);
				items.setBackgroundColor(LIGHT_YELLOW2);
				attr.setBackgroundColor(LIGHT_YELLOW2);
				lastModified.setBackgroundColor(LIGHT_YELLOW2);
				type.setBackgroundColor(LIGHT_YELLOW2);
				convertView.setBackgroundColor(LIGHT_YELLOW2);
				image.setBackgroundColor(LIGHT_YELLOW2);
				more.setBackgroundColor(LIGHT_YELLOW2);
				group.setBackgroundColor(LIGHT_YELLOW2);
			}

			final boolean canRead = f.canRead();
			final boolean canWrite = f.canWrite();

			final String st;
			if (canWrite) {
				st = "-rw";
			} else if (canRead) {
				st = "-r-";
			} else {
				st = "---";
				cbx.setChecked(false);
				cbx.setEnabled(false);
			}
			attr.setText(st);

			return convertView;
	    }

		public boolean rename(View item) {
			final FileInfo fileInfo = ((Holder) item.getTag()).fileInfo;
			final File oldPath = fileInfo.file;
			Log.d("oldPath", oldPath.getAbsolutePath());
			final EditText editText = new EditText(DuplicateFinderActivity.this);
			final String dir = oldPath.getParent();
			editText.setText(oldPath.getName());
			AlertDialog dialog = new AlertDialog.Builder(DuplicateFinderActivity.this)
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
								fileInfo.file = newF;
								notifyDataSetChanged();
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

		private LinkedList<List<FileInfo>> displayGroup(final LinkedList<List<FileInfo>> lset) {
			int groupSize = 0;
			int noDup = 0;
			long dupSize = 0;
			long realSize = 0;
			if (lset != null && lset.size() > 0) {
				int one = 0;
				List<FileInfo> newFileInfoList;
				for (List<FileInfo> fileInfoList : lset) {
					one = 0;
					newFileInfoList = new LinkedList<FileInfo>();
					for (FileInfo ff : fileInfoList) {
						newFileInfoList.add(ff);
						if (ff.file.exists()) {
							if (++one == 2) {
								groupSize++;
								realSize += ff.length;
								dupSize += ff.length;
								noDup++;
							} else if (one > 2) {
								dupSize += ff.length;
								noDup++;
							}
						} 
					}
				}
			}
			final StringBuilder statusViewResult = new StringBuilder();
			statusViewResult.append(Util.nf.format(groupSize)).append(" origin files (")
				.append(Util.nf.format(realSize)).append(" bytes)\n")
				.append(Util.nf.format(noDup)).append(" duplicate files (").append(Util.nf.format(dupSize)).append(" bytes)\n")
				.append((realSize != 0) ? Util.nf.format(dupSize * 100 / (double) realSize) : "0").append("% duplicate")
				;
			dupInfo.setText(statusViewResult);
			return groupList;
		}



		public boolean delete(final View item) {
			final FileInfo fileInfo = ((Holder) item.getTag()).fileInfo;
			final String name = fileInfo.file.getAbsolutePath();
			final TextView editText = new TextView(getContext());
			if (groupList == null) {
				groupList = FileInfo.buildGroupList(gList);
			}
			if (fileInfo.existDuplicate()) {
				editText.setText("Delete \"" + name + "\"?");
			} else {
				editText.setText("Delete origin file \"" + name + "\"?");
			}
			AlertDialog dialog = new AlertDialog.Builder(DuplicateFinderActivity.this)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle("Delete ?")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						boolean ret = AndroidPathUtils.deleteFile(fileInfo.file, DuplicateFinderActivity.this);// new File(selectedPath).delete();
						String statusDel = "deleteFile";
						if (ret) {
							statusDel = "Deleted \"" + name + "\"";
							notifyDataSetChanged();
							displayGroup(groupList);
						} else {
							statusDel = "Cannot delete file \"" + name + "\"";
						}
						Log.d(TAG, statusDel + Util.dtf.format(System.currentTimeMillis()));
						statusView.setText(statusDel);
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

		private void deleteSubFolder(final View v) {
			final String parentPath = ((Holder)v.getTag()).fileInfo.file.getParentFile().getAbsolutePath() + "/";
			final TextView editText = new TextView(getContext());
			editText.setText("Delete Sub Folder \"" + parentPath + "\"?");
			AlertDialog dialog = new AlertDialog.Builder(DuplicateFinderActivity.this)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle("Delete ?")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String statusDel = "deleteSubFolder";
						int counter = 0;
						if (groupList == null) {
							groupList = FileInfo.buildGroupList(gList);
						}
						for (FileInfo ff : gList) {
							if (ff.existDuplicate()) {
								String path = ff.file.getParentFile().getAbsolutePath();
								if (ff.file.exists() && path.startsWith(parentPath)) {
									boolean ret = AndroidPathUtils.deleteFile(ff.file, DuplicateFinderActivity.this); 
									if (ret) {
										counter++;
										statusDel = "Deleted \"" + ff.path + "\"";
									} else {
										statusDel = "Cannot delete \"" + ff.path + "\"";
									}
									Log.d("deleteSubFolder", statusDel + Util.dtf.format(System.currentTimeMillis()));
									statusView.setText(statusDel);
								}
							}
						}
						statusView.setText("Deleted " + counter + " files");
						if (counter > 1) {
							notifyDataSetChanged();
							displayGroup(groupList);
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
		}

		private void deleteGroup(View v) {
			final FileInfo fi = ((Holder)v.getTag()).fileInfo;
			if (groupList == null) {
				groupList = FileInfo.buildGroupList(gList);
			}
			final List<FileInfo> l = fi.gList;

			final TextView editText = new TextView(getContext());
			editText.setText("Delete duplicate files in group " + fi.groupNo + "?");
			AlertDialog dialog = new AlertDialog.Builder(DuplicateFinderActivity.this)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle("Delete ?")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						boolean ret;
						int counter = 0;
						String statusDel = "deleteGroup";
						String selectedPath = fi.path;
						File selectedF = fi.file;
						for (FileInfo ff : l) {
							if (selectedF.exists()) {
								if (!ff.path.equals(selectedPath) && ff.file.exists()) {
									ret = AndroidPathUtils.deleteFile(ff.file, DuplicateFinderActivity.this); // ff.file.delete();
									if (ret) {
										counter++;
										statusDel = "Deleted \"" + ff.path + "\"";
									} else {
										statusDel = "Cannot delete \"" + ff.path + "\"";
									}
									Log.d("deleteGroup", statusDel);
									statusView.setText(statusDel);
								}
							} else if (ff.file.exists()) {
								selectedF = ff.file;
								selectedPath = ff.path;
							}
						}
						statusView.setText("Deleted " + counter + " files");
						if (counter > 0) {
							notifyDataSetChanged();
							displayGroup(groupList);
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

		}

		private void deleteFolder(View v) {
			final String parentPath = ((Holder)v.getTag()).fileInfo.file.getParentFile().getAbsolutePath();
			final TextView editText = new TextView(getContext());
			editText.setText("Delete duplicate files in folder \"" + parentPath + "\"?");
			AlertDialog dialog = new AlertDialog.Builder(DuplicateFinderActivity.this)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle("Delete ?")
				.setView(editText)
				.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String statusDel = "deleteFolder";
						int counter = 0;
						boolean ret;
						if (groupList == null) {
							groupList = FileInfo.buildGroupList(gList);
						}
						for (FileInfo ff : gList) {
							if (ff.existDuplicate()) {
								if (ff.file.exists() && ff.file.getParentFile().getAbsolutePath().equals(parentPath)) {
									ret = AndroidPathUtils.deleteFile(ff.file, DuplicateFinderActivity.this); //ff.file.delete();
									if (ret) {
										counter++;
										statusDel = "Deleted \"" + ff.path + "\"";
									} else {
										statusDel = "Cannot delete \"" + ff.path + "\". ";
									}
									Log.d("deleteFolder", statusDel);
									statusView.setText(statusDel);
								} 
							}
						}

						statusView.setText("Deleted " + counter + " files");
						if (counter > 0) {
							notifyDataSetChanged();
							displayGroup(groupList);
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
		}

		public boolean send(View item) {
			File f = ((Holder) item.getTag()).fileInfo.file;
			Uri uri = Uri.fromFile(f);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setFlags(0x1b080001);

			initMimemap();
			String suff = net.gnu.util.FileUtil.getExtension(f);
			ComparableEntry<String, String> comparableEntry = new ComparableEntry<String, String>(suff, "");
			ComparableEntry<String, String> floor = null;
			if ((floor = mimeMap.floor(comparableEntry)).equals(comparableEntry)) {
				i.setDataAndType(uri, floor.value);
				Log.d(TAG, "i.setDataAndType " + floor + ", " + i);
			} else {
				i.setData(uri);
				Log.d(TAG, "i.setData(uri) " + uri + ", " + i);
			}

			Log.d(TAG, "send" + i);
			Log.d(TAG, "send.getExtras " + AndroidUtils.bundleToString(i.getExtras()));
			Intent createChooser = Intent.createChooser(i, "Send via..");
			Log.d(TAG, "createChooser" + createChooser);
			Log.d(TAG, "createChooser.getExtras " + AndroidUtils.bundleToString(createChooser.getExtras()));
			startActivity(createChooser);
			return true;
		}

		public boolean copyName(View item) {
			String data = ((Holder) item.getTag()).fileInfo.file.getName();
			copyToClipboard(data);
			return true;
		}

		public boolean copyPath(View item) {
			String data = ((Holder) item.getTag()).fileInfo.file.getParent();
			copyToClipboard(data);
			return true;
		}

		public boolean copyFullName(View item) {
			copyToClipboard(((Holder) item.getTag()).fileInfo.file.getAbsolutePath());
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

		private final TreeSet<ComparableEntry<String, String>> mimeMap = new TreeSet<>();
		public void onClick(final View v) {
			final int id = v.getId();
			if (id == R.id.more) {
				final FileInfo fileInfo = ((Holder) v.getTag()).fileInfo;
				if (!fileInfo.file.exists()) {
					showToast("\"" + fileInfo.path + "\" isn't exised");
					return;
				}
				final PopupMenu popup = new PopupMenu(v.getContext(), v);
				popup.getMenuInflater().inflate(R.menu.dup_actions, popup.getMenu());
				if (groupList == null) {
					groupList = FileInfo.buildGroupList(gList);
				}
				if (!fileInfo.existDuplicate()) {
					popup.getMenu().removeItem(R.id.deleteGroup);
				}
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId()) {
								case R.id.rename:
									rename(v);
									break;
								case R.id.delete:
									delete(v);
									break;
								case R.id.deleteFolder:
									deleteFolder(v);
									break;
								case R.id.deleteGroup:
									deleteGroup(v);
									break;
								case R.id.deleteSubFolder:
									deleteSubFolder(v);
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
						}
					});
				popup.show();
				return;
			}
			initMimemap();
			FileInfo fPath = ((Holder) v.getTag()).fileInfo;
			File f = fPath.file;
			Log.d(TAG, "onClick, " + fPath.path + ", " + v);
			Log.d("currentSelectedList", Util.collectionToString(selectedInList, true, "\r\n"));
			Log.d("selectedInList.contains(f)", "" + selectedInList.contains(f));
			Log.d("f.exists()", f.exists() + "");

			if (f.exists()) {
				if (!f.canRead()) {
					showToast(f + " cannot be read");
				} else {
					if ((id == R.id.cbx) || (id == R.id.icon)) {//file and folder
						if (selectedInList.contains(fPath)) {
							selectedInList.remove(fPath);
							v.setBackgroundColor(LIGHT_YELLOW);
						} else {
							selectedInList.add(fPath);
							v.setBackgroundColor(LIGHT_BROWN);
						}
						status = selectedInList.size() + "/"+ getCount();
						statusView.setText(status);
					} else if (f.isFile()) { //file
						try{
							Uri uri = Uri.fromFile(f);
							Intent i = new Intent(Intent.ACTION_VIEW); 
							i.addCategory(Intent.CATEGORY_DEFAULT);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
							i.setData(uri);
							Log.d("i.setData(uri)", uri + "." + i);
							String suff = net.gnu.util.FileUtil.getExtension(f);
							ComparableEntry<String, String> comparableEntry = new ComparableEntry<String, String>(suff, "");
							ComparableEntry<String, String> floor = null;
							if ((floor = mimeMap.floor(comparableEntry)).equals(comparableEntry)) {
								i.setDataAndType(uri, floor.value);
								Log.d("floor", floor + ", " + i);
							}
							Intent createChooser = Intent.createChooser(i, "View");
							Log.i("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
							startActivity(createChooser);
						} catch (Throwable e) {
							Toast.makeText(DuplicateFinderActivity.this, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
					statusView.setText(selectedInList.size() + "/"+ getCount());
				}

				notifyDataSetChanged();

			} else {
				showToast(f + " isn't existed");
			}
		}

		private void initMimemap()
		{
			if (mimeMap.size() == 0)
			{
				mimeMap.add(new ComparableEntry<String, String>(".7z", "application/x-7z-compressed"));
				mimeMap.add(new ComparableEntry<String, String>(".bz2", "application/x-bzip2"));
				mimeMap.add(new ComparableEntry<String, String>(".gz", "application/x-gzip"));
				mimeMap.add(new ComparableEntry<String, String>(".xz", "application/x-xz"));
				mimeMap.add(new ComparableEntry<String, String>(".x", "application/x-compress"));
				mimeMap.add(new ComparableEntry<String, String>(".rar", "application/x-rar-compressed"));
				mimeMap.add(new ComparableEntry<String, String>(".gtz", "application/x-gtar"));
				mimeMap.add(new ComparableEntry<String, String>(".zip", "application/zip"));
				//s.add(new ComparableEntry<String, String>(".rar", "application/rar"));
				//s.add(new ComparableEntry<String, String>(".zip", "application/x-zip"));
				mimeMap.add(new ComparableEntry<String, String>("", "application/octet-stream"));
				mimeMap.add(new ComparableEntry<String, String>(".tar", "application/x-tar"));
				mimeMap.add(new ComparableEntry<String, String>(".lzh", "application/x-lzh"));

				mimeMap.add(new ComparableEntry<String, String>(".xls", "application/vnd.ms-excel"));
				mimeMap.add(new ComparableEntry<String, String>(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
				mimeMap.add(new ComparableEntry<String, String>(".rtf", "text/rtf"));
				mimeMap.add(new ComparableEntry<String, String>(".c", "text/x-csrc"));
				mimeMap.add(new ComparableEntry<String, String>(".h", "text/x-chdr"));
				mimeMap.add(new ComparableEntry<String, String>(".cpp", "text/x-c++src"));
				mimeMap.add(new ComparableEntry<String, String>(".hpp", "text/x-c++hdr"));
				mimeMap.add(new ComparableEntry<String, String>(".doc", "application/msword"));
				mimeMap.add(new ComparableEntry<String, String>(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
				mimeMap.add(new ComparableEntry<String, String>(".odf", "application/vnd.oasis.opendocument.formula"));
				mimeMap.add(new ComparableEntry<String, String>(".odp", "application/vnd.oasis.opendocument.presentation"));
				mimeMap.add(new ComparableEntry<String, String>(".ods", "application/vnd.oasis.opendocument.spreadsheet"));
				mimeMap.add(new ComparableEntry<String, String>(".odt", "application/vnd.oasis.opendocument.text"));
				mimeMap.add(new ComparableEntry<String, String>(".ppt", "application/vnd.ms-powerpoint"));
				mimeMap.add(new ComparableEntry<String, String>(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"));
				mimeMap.add(new ComparableEntry<String, String>(".html", "text/html"));
				mimeMap.add(new ComparableEntry<String, String>(".txt", "text/txt"));
				mimeMap.add(new ComparableEntry<String, String>(".ini", "text/plain"));
				mimeMap.add(new ComparableEntry<String, String>(".sh", "text/plain"));
				mimeMap.add(new ComparableEntry<String, String>(".bat", "text/plain"));
				mimeMap.add(new ComparableEntry<String, String>(".java", "text/java"));
				mimeMap.add(new ComparableEntry<String, String>(".xml", "text/xml"));
				mimeMap.add(new ComparableEntry<String, String>(".aidl", "text/plain"));
				mimeMap.add(new ComparableEntry<String, String>(".properties", "text/plain"));
				mimeMap.add(new ComparableEntry<String, String>(".md", "text/plain"));
				mimeMap.add(new ComparableEntry<String, String>(".apk", "application/vnd.android.package-archive"));


			}
		}

		public boolean onLongClick(View v) {
			FileInfo fPath = ((Holder) v.getTag()).fileInfo;
			File f = fPath.file;

			if (!f.canRead()) {
				showToast(f + " cannot be read");
				return true;
			}
			Log.d(TAG, "onLongClick, " + fPath);
			Log.d("currentSelectedList", Util.collectionToString(selectedInList, true, "\r\n"));
			Log.d("selectedInList.contains(f)", "" + selectedInList.contains(f));

			if (selectedInList.contains(fPath)) {
				selectedInList.remove(fPath);
				v.setBackgroundColor(LIGHT_YELLOW);
			} else {
				selectedInList.add(fPath);
				v.setBackgroundColor(LIGHT_BROWN);
			}
			statusView.setText(selectedInList.size() 
							   + "/"+ getCount());
			notifyDataSetChanged();

			return true;
		}
	}
	
}
