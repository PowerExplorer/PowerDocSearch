package net.gnu.agrep;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import android.util.LruCache;
import net.gnu.util.*;
import java.util.Set;

public class Cache implements Serializable, Iterator<File> {

	private static final long serialVersionUID = -7028012255185772017L;

	private List<File> data = null;
	private LruCache<File, String> lru = null;
	private int currentSize = 0;
	private int totalSize = 0;

	private List<File> notYet;
	private int counter = 0;
	private int fileNum = 0;
	private Iterator<File> iter = null;

	private String TAG = "Cache";

	public Cache(final Set<File> files) {
		fileNum = files.size();
		data = new LinkedList<File>();
		lru = new LruCache<File, String>(9999);
		notYet = new LinkedList<File>();
		final long maxMemory = Runtime.getRuntime().maxMemory();
		Log.i("maxMemory 1", "" + maxMemory);
		final int maxSize = (int) (maxMemory >> 2);
		long fileLength;
		String fileName;
		String content;
		for (File file : files) {
//			Log.i("caching file", file.getAbsolutePath() 
//					// + ", isFile: " + file.isFile()
//					+ ", size: " + file.length());
			if (file != null && file.isFile()) {
				fileLength = file.length();
				totalSize += fileLength;
				fileName = file.getAbsolutePath();
				if ((currentSize + fileLength) < maxSize) {
					try {
						content = FileUtil
							.readFileAsCharsetMayCheckEncode(fileName, HtmlUtil.UTF8);
						data.add(file);
						lru.put(file, content);
						currentSize += fileLength;
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				} else {
					notYet.add(file);
				}
			}
		}
		iter = data.iterator();
		Log.i("freeMemory 2: ", "" + Runtime.getRuntime().freeMemory());
	}

	public int getTotalSize() {
		return totalSize;
	}

	public int getCurrentSize() {
		return currentSize;
	}

	private String add(final File file) {
		//Log.i("Cache.add", file.getAbsolutePath());
		if (file != null && file.isFile()) {
			final long fileLength = file.length();
			String fileContent = null;
			final int maxSize = (int) (Runtime.getRuntime().freeMemory() >> 2);
			try {
				if ((currentSize + fileLength) < maxSize) {
					fileContent = FileUtil.readFileAsCharsetMayCheckEncode(file, HtmlUtil.UTF8);
					data.add(file);
					lru.put(file, fileContent);
					currentSize += fileLength;
				} else {
					File f = null;
					while (data.size() > 0) {
						Log.i("Remove in dataSet " + counter + ": ", f + "");
						f = data.remove(0);
						lru.remove(f);
						currentSize -= f.length();
						notYet.add(f);
						if (currentSize + fileLength < maxSize) {
							break;
						}
					}
					fileContent = FileUtil.readFileAsCharsetMayCheckEncode(file, HtmlUtil.UTF8);
					data.add(file);
					lru.put(file, fileContent);
					currentSize += fileLength;
				}
			} catch (IOException e) {
				Log.e("Cache.add", e.getMessage(), e);
			}
			return fileContent;
		} else {
			return null;
		}
	}

	public String add(final File file, final String fileContent) {
		if (file != null && file.isFile()) {
			final long fileLength = file.length();
			final int maxSize = (int) (Runtime.getRuntime().freeMemory() >> 2);
			Log.i("Cache", "add " + file.getAbsolutePath() + ", freeMemory " + maxSize);
			
			if ((currentSize + fileLength) < maxSize) {
				//fileContent = FileUtil.readFileAsCharsetMayCheckEncode(file, HtmlUtil.UTF8);
				data.add(file);
				lru.put(file, fileContent);
				currentSize += fileLength;
			} else {
				File f;
				while (data.size() > 0) {
					f = data.remove(0);
					Log.i("Cache", "Remove at " + counter + ": " + f);
					lru.remove(f);
					currentSize -= f.length();
					notYet.add(f);
					if (currentSize + fileLength < maxSize) {
						break;
					}
				}
				//fileContent = FileUtil.readFileAsCharsetMayCheckEncode(file, HtmlUtil.UTF8);
				data.add(file);
				lru.put(file, fileContent);
				currentSize += fileLength;
			}
			return fileContent;
		} else {
			return null;
		}
	}

	public int cached() {
		return lru.putCount();
	}

	public void reset() {
		counter = 0;
		iter = data.iterator();
	}

	@Override
	public boolean hasNext() {
		return counter < fileNum;
	}

	public String get(final File file) throws IOException {
		final String get = lru.get(file);
		if (get != null) {
			return get;
		} else {
			//final String fileContent = FileUtil.readFileAsCharsetMayCheckEncode(file, HtmlUtil.UTF8);
			//lru.put(file, fileContent);
			return add(file);
		}
	}

	@Override
	public File next() {
		if (counter < fileNum) {
			if (counter++ < data.size()) {
				return iter.next(); // data.get(counter++);
			} else {
				final File file = notYet.remove(0);
				// System.err.println("removed in notYet: " + + counter + ": " +
				// file);
				add(file);
				return file;
			}
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public static void main(String[] args) throws IOException {
		Set<File> files = null; // FileUtil.getFilesDialog(null, null, "/", true, JFileChooser.FILES_AND_DIRECTORIES, true, "Files or Folder");
		int count = 0;
		long start1 = System.currentTimeMillis();
		Cache cache = new Cache(files);
		while (cache.hasNext()) {
			count++;
			File entry = cache.next();
			System.err.println(entry);
		}
		long end1 = System.currentTimeMillis();
		System.err.println("Run 1: " + (end1 - start1));
		System.err.println("Count 1: " + count);

		count = 0;
		long start2 = System.currentTimeMillis();
		cache.reset();
		while (cache.hasNext()) {
			count++;
			File entry = cache.next();
			System.err.println(entry);
		}
		long end2 = System.currentTimeMillis();
		System.err.println("Run 2: " + (end2 - start2));
		System.err.println("Count 2: " + count);
	}

}
