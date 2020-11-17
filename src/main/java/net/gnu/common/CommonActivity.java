package net.gnu.common;

import android.support.v4.app.*;
import android.widget.*;
import android.content.*;
import android.app.*;
import net.gnu.util.*;

public class CommonActivity extends FragmentActivity {
	
	protected transient static final String ALL_SUFFIX = ".*";
	protected transient static final String ALL_SUFFIX_TITLE = "Select Files/Folders";
	protected transient static final int FILES_REQUEST_CODE = 13;
	protected transient static final int SAVETO_REQUEST_CODE = 14;
	protected transient static final int STARDICT_REQUEST_CODE = 15;
	protected transient static final boolean MULTI_FILES = true;
	
	protected transient EditText fileET;
	protected transient EditText saveToET;
	protected transient EditText stardictET;
	
	String files = "";
	String saveTo = "";
	String stardict = "";
	
	public void showToast(final String st) {
		Toast.makeText(this, st, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == FILES_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				final String[] stringExtra = data.getStringArrayExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH);
				files = Util.arrayToString(stringExtra, false, "|");
				fileET.setText(files);
			} else { // RESULT_CANCEL
				showToast("No file selected");
			}
		} else if (requestCode == SAVETO_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				final String[] stringExtra = data.getStringArrayExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH);
				saveTo = stringExtra[0];
				saveToET.setText(saveTo);
			} else { // RESULT_CANCEL
				showToast("No folder selected");
			}
		} else if (requestCode == STARDICT_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				final String[] stringExtra = data.getStringArrayExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH);
				stardict = Util.arrayToString(stringExtra, false, "|");
				stardictET.setText(stardict);
			} else { // RESULT_CANCEL
				showToast("No folder selected");
			}
		} 
	}
}
