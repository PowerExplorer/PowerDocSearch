package net.gnu.common;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import net.gnu.util.Util;
import net.gnu.util.FileUtil;
import net.gnu.common.FileInfo;
import java.util.Collection;
import java.util.Arrays;
import android.app.*;
import java.util.*;

public class DupFinderTask extends AsyncTask<Void, Object, CharSequence> {

	private static final String TAG = "DupFinderTask";
	private LinkedList<List<FileInfo>> groupList;
	private DuplicateFinderActivity activity;
	private List<String> fs;
	
	public DupFinderTask(final DuplicateFinderActivity activity, final String... fileNames) {
		this.activity = activity;
		fs = new ArrayList<String>(fileNames.length);
		for (String ss : fileNames) {
			fs.add(ss);
		}
	}
	
	public DupFinderTask(final DuplicateFinderActivity activity, final List<String> fileNames) {
		this.activity = activity;
		fs = fileNames;
	}

	@Override
	protected CharSequence doInBackground(final Void... fileNames) {
		try {
			if (fileNames != null) {
				return duplicateFinder(fs);
			}
		} catch (IOException e) {
			publishProgress(e.getMessage());
		}
		return "";
	}

	protected void onProgressUpdate(final Object... progress) {
		if (progress != null) {
			final StringBuilder sb = new StringBuilder();
			for (Object cs : progress) {
				sb.append(cs);
			}
			activity.setText(sb);
		}
	}

	@Override
	protected void onPostExecute(final CharSequence result) {
		Log.d(TAG, result + "");
		activity.showToast("Duplicate finder finished");
		activity.setText("Duplicate finder finished");
		if (groupList != null) {
			activity.setDupList(groupList);
		}
		
	}

	public CharSequence duplicateFinder(final List<String> files) throws IOException {
		final File[] fs = new File[files.size()];
		int i = 0;
		for (String st : files) {
			fs[i++] = new File(st);
		}
		return duplicateFinder(fs);
	}

	public CharSequence duplicateFinder(final File[] files) throws IOException {
		publishProgress("Getting file list...");
		
		final long start =  System.nanoTime();
		final Collection<File> lf = FileUtil.getFiles(files);
		groupList = dupFinder(lf);
		publishProgress(groupList.size(), 
						" origin files, took ", 
						Util.nf.format(System.nanoTime() - start),
						" nano seconds");
		return "";
	}

	private LinkedList<List<FileInfo>> dupFinder(final Collection<File> oriListFile) throws IOException {
		
		final File[] oriArrayFile = new File[oriListFile.size()];
		oriListFile.toArray(oriArrayFile);
		Arrays.sort(oriArrayFile, new FileInfo.SortFileOnlySizeDecrease());

		long length;
		FileInfo fileInfo;
		File file;
		boolean same;
		List<FileInfo> filesInGroupList; //same size, many group, 1 size 1 set
		final LinkedList<List<FileInfo>> groupList = new LinkedList<List<FileInfo>>();
		long curSize = -1;
		for (File f : oriArrayFile) {
			length = f.length();
			if (length < curSize) {
				filesInGroupList = new LinkedList<FileInfo>();
				curSize = length;
				filesInGroupList.add(new FileInfo(f));
				groupList.addFirst(filesInGroupList);
			} else {
				same = false;
				for (List<FileInfo> curGroupList : groupList) {
					fileInfo = curGroupList.get(0);
					if (length == fileInfo.length) {
						file = fileInfo.file;
						publishProgress("comparing \"", fileInfo.path, "\" and \"", f.getAbsolutePath(), "\"");
						same = FileUtil.compareFileContent(file, f);
						if (same) {
							curGroupList.add(new FileInfo(f));
							break;
						}
					} else {
						break;
					}
				}
				if (!same) {
					filesInGroupList = new LinkedList<FileInfo>();
					curSize = length;
					filesInGroupList.add(new FileInfo(f));
					groupList.addFirst(filesInGroupList);
				}
			}
		}
		final LinkedList<List<FileInfo>> retGroupList = new LinkedList<List<FileInfo>>();
		int curGroup = 0;
		int size;
		for (List<FileInfo> lf : groupList) {
			size = lf.size();
			if (size > 1) {
				retGroupList.addFirst(lf);
				curGroup++;
				for (FileInfo ff : lf) {
					ff.groupNo = curGroup;
					ff.gList = lf;
				}
			}
		}
		return retGroupList;
	}

}
