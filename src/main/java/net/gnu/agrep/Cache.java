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

	private static final String TAG = "Cache";
	private static final long serialVersionUID = -7028012255185772017L;

	private List<File> data = null;
	private LruCache<File, String> lru = null;
	public int currentSize = 0;
	public int totalSize = 0;

	private List<File> notYet;
	private int counter = 0;
	private int fileNum = 0;
	private Iterator<File> iter = null;
	String cacheStatus = "";
	
	public Cache(final Set<File> files) {
		fileNum = files.size();
		data = new LinkedList<File>();
		lru = new LruCache<File, String>(9999);
		notYet = new LinkedList<File>();
		final long maxMemory = Runtime.getRuntime().maxMemory();
		Log.i(TAG, "maxMemory " + maxMemory);
		final int maxSize = (int) (maxMemory >> 4);
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
		cacheStatus = Util.nf.format(currentSize) + "/" + Util.nf.format(totalSize) + " bytes";
		Log.i(TAG, "cached " + cacheStatus + " freeMemory " + Runtime.getRuntime().freeMemory());
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
						//Log.i(TAG, "Remove in dataSet " + counter + ": " + f + "");
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
				Log.e(TAG, "Cache.add " + e.getMessage(), e);
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
			//Log.i(TAG, "add " + file.getAbsolutePath() + ", freeMemory " + maxSize);
			
			if ((currentSize + fileLength) < maxSize) {
				//fileContent = FileUtil.readFileAsCharsetMayCheckEncode(file, HtmlUtil.UTF8);
				data.add(file);
				lru.put(file, fileContent);
				currentSize += fileLength;
			} else {
				File f;
				while (data.size() > 0) {
					f = data.remove(0);
					//Log.i(TAG, "Remove at " + counter + ": " + f);
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

	public String get(final File file) {
		final String get = lru.get(file);
		if (get != null) {
			return get;
		} else {
			//final String fileContent = FileUtil.readFileAsCharsetMayCheckEncode(file, HtmlUtil.UTF8);
			//lru.put(file, fileContent);
			Log.d(TAG, "missed " + file.getAbsolutePath());
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

	public static void main(String[] args) {
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
