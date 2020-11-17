package net.gnu.agrep;

import android.support.v4.app.Fragment;
import java.util.ArrayList;
import android.os.Bundle;
import android.util.Log;
import android.os.AsyncTask;
import java.util.TreeSet;
import java.io.File;
import java.util.*;
import android.widget.*;

public class RetainFrag extends Fragment {
	private static final String TAG = "RetainFrag";

	public ConvertTask mTask;
	public AsyncTask callBackTask;
	
	ArrayList<GrepView.Data> mData = new ArrayList<GrepView.Data>();
	GrepView.GrepAdapter mAdapter;
	boolean hidden = false;
	SettingsFragment searchFragment;
	public TextView statusTV;
	public Collection<? extends Object> oriList;
	public TreeSet<File> fileList;
	Cache cache;
	boolean newSearch = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause mTask " + mTask);
		super.onPause();
		hidden = true;
//			if (mTask != null) {
////				if (mTask.mProgressDialog != null) {
////					mTask.mProgressDialog.dismiss();
////					Log.d(TAG, "onPause mProgressDialog " + mTask.mProgressDialog);
////					mTask.mProgressDialog = null;
////				}
//			}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mTask != null) {
			if (searchFragment != null && !searchFragment.fake && hidden) {
				Log.d(TAG, "onResume hidden " + hidden + ", mTask " + (mTask != null ? mTask.getStatus() : "null") + ", adapter " + searchFragment.mGrepView.getAdapter() + ", mData " + mData);
				AsyncTask.Status status = mTask.getStatus();
				if (status == AsyncTask.Status.RUNNING) {
					mTask.runProgress();
				} else if (status == AsyncTask.Status.FINISHED) {
					mAdapter.notifyDataSetChanged();
					searchFragment.mGrepView.setSelection(0);
					mTask = null;
				}
			}
		}
		hidden = false;
	}

}
