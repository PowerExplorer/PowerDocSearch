package net.gnu.common;

import android.app.DialogFragment;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import net.gnu.common.FolderChooserActivity;
import android.app.Fragment;
import android.support.v4.app.*;

public class GetFileListener implements OnClickListener {

	private FragmentActivity frag;
	private String[] files;
	private String title;
	private String suffix;
	private int requestCode;
	private boolean multi;

	public GetFileListener(FragmentActivity frag, String title, String suffix, String[] saveTo, int requestCode, boolean multi) {
		this.frag = frag;
		this.files = saveTo;
		this.title = title;
		this.suffix = suffix;
		this.requestCode = requestCode;
		this.multi = multi;
	}

	public GetFileListener(FragmentActivity frag, String title, String suffix, String saveTo, int requestCode, boolean multi) {
		this.frag = frag;
		this.files = new String[] {saveTo};
		this.title = title;
		this.suffix = suffix;
		this.requestCode = requestCode;
		this.multi = multi;
	}

	public void onClick(View v) {
		//frag.dismiss();
		Intent intent = new Intent(frag, FolderChooserActivity.class);
		intent.putExtra(FolderChooserActivity.EXTRA_ABSOLUTE_PATH, files);
		intent.putExtra(FolderChooserActivity.EXTRA_FILTER_FILETYPE, suffix); // ""
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multi);
		intent.putExtra(FolderChooserActivity.CHOOSER_TITLE, title); // "Output Folder"
		frag.startActivityForResult(intent, requestCode);
	}
}

