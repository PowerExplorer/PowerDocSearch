package net.gnu.common;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Comparator;
import android.os.Parcelable;
import android.os.Parcel;
import java.util.*;
import net.gnu.util.FileUtil;

public class FileInfo implements Comparable<FileInfo>, Comparator<FileInfo>, Serializable, Parcelable {

	public transient File file;
	public transient List<FileInfo> gList;
	
	public int groupNo;
	
	public final String path;
	public final long length;
	
	public FileInfo(final File file) {
		this.file = file;
		this.path = file.getAbsolutePath();
		this.length = file.length();
	}

	public FileInfo(final String path) {
		this.file = new File(path);
		this.path = path;
		this.length = file.length();
	}

	public FileInfo(final Parcel im) {
        path = im.readString();
        length = im.readLong();
		groupNo = im.readInt();
		file = new File(path);
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel p1, final int p2) {
		p1.writeString(path);
		p1.writeLong(length);
		p1.writeInt(groupNo);
    }

    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
		public FileInfo createFromParcel(final Parcel in) {
			return new FileInfo(in);
		}

		public FileInfo[] newArray(final int size) {
			return new FileInfo[size];
		}
	};

	@Override
	public int compare(final FileInfo p1, final FileInfo p2) {
		return p1.path.compareTo(p2.path);
	}

	@Override
	public int compareTo(final FileInfo p1) {
		return path.compareTo(p1.path);
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof FileInfo) {
			return this.path == ((FileInfo)o).path
				|| this.path.equals(((FileInfo)o).path);
		} else {
			return false;
		}
	}

	public static LinkedList<List<FileInfo>> buildGroupList(final List<FileInfo> l) {
		final FileInfo[] arr = new FileInfo[l.size()];
		l.toArray(arr);
		Arrays.sort(arr, new FileInfo.SortGroupDecrease());
		int no = Integer.MIN_VALUE;
		int curGroupNo = Integer.MAX_VALUE;
		final LinkedList<List<FileInfo>> groupList = new LinkedList<List<FileInfo>>();
		List<FileInfo> filesInGroupList = new LinkedList<FileInfo>(); //same size, many group, 1 size 1 set
		for (FileInfo fi : arr) {
			no = fi.groupNo;
			if (no < curGroupNo) {
				filesInGroupList = new LinkedList<FileInfo>();
				fi.gList = filesInGroupList;
				curGroupNo = no;
				filesInGroupList.add(fi);
				groupList.add(filesInGroupList);
			} else {
				filesInGroupList.add(fi);
			}
		}
		return groupList;
	}
	
	public boolean existDuplicate() {
		if (gList != null) {
			int counter = 0;
			for (FileInfo ff : gList) {
				if (ff.file.exists() && ++counter > 1) {
					return true;
				}
			}
		}
		return false;
	}

	public static Collection<FileInfo> getFilesBy(final File[] ff, String in, final FolderChooserActivity.SearchFileNameTask updater) {
		if (ff != null && ff.length > 0) {
			ArrayList<FileInfo> lf = new ArrayList<>();
			long prevUpdate = System.nanoTime();
			Collection<File> filesBy;
			for (File f : ff) {
				filesBy = FileUtil.getFilesBy(f, in);
				for (File le : filesBy) {
					lf.add(new FileInfo(le));
				}
				final long present = System.nanoTime();
				if (updater != null && present - prevUpdate > 2000000000 && !updater.busyNoti) {
					prevUpdate = present;
					updater.publish(lf);
					lf = new ArrayList<>(1024);
				}
			}
			if (updater != null) {
				updater.publish(lf);
			}
			return lf;
		} else {
			return new LinkedList<FileInfo>();
		}
	}
	
	public interface DoubleCompare extends Comparator<FileInfo> {
		public Comparator<File> getFileComparator();
	}

	
	public static class SortFileOnlySizeDecrease implements Comparator<File> {
		@Override
		public int compare(final File f1, final File f2) {
			final long length1 = f1.length();
			final long length2 = f2.length();
			if (length1 < length2) {
				return 1;
			} else if (length1 > length2) {
				return -1;
			} else {
				return 0;
			}
		}

		@Override
		public String toString() {
			return "SortFileOnlySizeDecrease";
		}
	}
	
	/*
	*/
	
	public static class SortFolderNameFirstDecrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo f1, final FileInfo f2) {
			final boolean isFile1 = f1.file.isFile();
			final boolean isFile2 = f2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					return f2.file.getName().compareToIgnoreCase(f1.file.getName());
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return f2.file.getName().compareToIgnoreCase(f1.file.getName());
				}
			}
		}
		
		public Comparator<File> getFileComparator() {
			return new SortFolderNameFirstDec();
		}

		@Override
		public String toString() {
			return "SortFolderNameFirstDecrease";
		}
	}
	
	public static class SortFolderNameFirstDec implements Comparator<File> {
		@Override
		public int compare(final File f1, final File f2) {
			final boolean isFile1 = f1.isFile();
			final boolean isFile2 = f2.isFile();
			if (isFile1) {
				if (isFile2) {
					return f2.getName().compareToIgnoreCase(f1.getName());
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return f2.getName().compareToIgnoreCase(f1.getName());
				}
			}
		}

		@Override
		public String toString() {
			return "SortFolderNameFirstDec";
		}
	}
	
	//
	
	public static class SortFolderNameFirstIncrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo f1, final FileInfo f2) {
			final boolean isFile1 = f1.file.isFile();
			final boolean isFile2 = f2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					return f1.file.getName().compareToIgnoreCase(f2.file.getName());
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return f1.file.getName().compareToIgnoreCase(f2.file.getName());
				}
			}
		}

		public Comparator<File> getFileComparator() {
			return new SortFolderNameFirstInc();
		}

		@Override
		public String toString() {
			return "SortFolderNameFirstIncrease";
		}
	}
	
	public static class SortFolderNameFirstInc implements Comparator<File> {
		@Override
		public int compare(final File f1, final File f2) {
			final boolean isFile1 = f1.isFile();
			final boolean isFile2 = f2.isFile();
			if (isFile1) {
				if (isFile2) {
					return f1.getName().compareToIgnoreCase(f2.getName());
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			}
		}

		@Override
		public String toString() {
			return "SortFolderNameFirstInc";
		}
	}
	
	//
	
	public static class SortFileDateDecrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final boolean isFile1 = p1.file.isFile();
			final boolean isFile2 = p2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					final long lastModified1 = p1.file.lastModified();
					final long lastModified2 = p2.file.lastModified();
					if (lastModified1 < lastModified2) {
						return 1;
					} else if (lastModified1 > lastModified2) {
						return -1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final long lastModified1 = p1.file.lastModified();
					final long lastModified2 = p2.file.lastModified();
					if (lastModified1 < lastModified2) {
						return 1;
					} else if (lastModified1 > lastModified2) {
						return -1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				}
			}
		}

		public Comparator<File> getFileComparator() {
			return new SortFileDateDec();
		}

		@Override
		public String toString() {
			return "SortFileDateDecrease";
		}
	}

	public static class SortFileDateDec implements Comparator<File> {
		@Override
		public int compare(final File p1, final File p2) {
			final boolean isFile1 = p1.isFile();
			final boolean isFile2 = p2.isFile();
			if (isFile1) {
				if (isFile2) {
					final long lastModified1 = p1.lastModified();
					final long lastModified2 = p2.lastModified();
					if (lastModified1 < lastModified2) {
						return 1;
					} else if (lastModified1 > lastModified2) {
						return -1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final long lastModified1 = p1.lastModified();
					final long lastModified2 = p2.lastModified();
					if (lastModified1 < lastModified2) {
						return 1;
					} else if (lastModified1 > lastModified2) {
						return -1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				}
			}
		}

		@Override
		public String toString() {
			return "SortFileDateDec";
		}
	}
	
	//
	
	public static class SortFileDateIncrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final boolean isFile1 = p1.file.isFile();
			final boolean isFile2 = p2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					final long lastModified1 = p1.file.lastModified();
					final long lastModified2 = p2.file.lastModified();
					if (lastModified1 < lastModified2) {
						return -1;
					} else if (lastModified1 > lastModified2) {
						return 1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final long lastModified1 = p1.file.lastModified();
					final long lastModified2 = p2.file.lastModified();
					if (lastModified1 < lastModified2) {
						return -1;
					} else if (lastModified1 > lastModified2) {
						return 1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				}
			}
		}
		
		public Comparator<File> getFileComparator() {
			return new SortFileDateInc();
		}

		@Override
		public String toString() {
			return "SortFileDateIncrease";
		}
	}

	public static class SortFileDateInc implements Comparator<File> {
		@Override
		public int compare(final File p1, final File p2) {
			final boolean isFile1 = p1.isFile();
			final boolean isFile2 = p2.isFile();
			if (isFile1) {
				if (isFile2) {
					final long lastModified1 = p1.lastModified();
					final long lastModified2 = p2.lastModified();
					if (lastModified1 < lastModified2) {
						return -1;
					} else if (lastModified1 > lastModified2) {
						return 1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final long lastModified1 = p1.lastModified();
					final long lastModified2 = p2.lastModified();
					if (lastModified1 < lastModified2) {
						return -1;
					} else if (lastModified1 > lastModified2) {
						return 1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				}
			}
		}

		@Override
		public String toString() {
			return "SortFileDateInc";
		}
	}
	
	//
	
	public static class SortFileSizeDecrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final boolean isFile1 = p1.file.isFile();
			final boolean isFile2 = p2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					final long length1 = p1.file.length();
					final long length2 = p2.file.length();
					if (length1 < length2) {
						return 1;
					} else if (length1 > length2) {
						return -1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final String[] list1 = p1.file.list();
					final int length1 = ((list1 == null) ? 0 : list1.length);
					final String[] list2 = p2.file.list();
					final int length2 = ((list2 == null) ? 0 : list2.length);
					if (length1 < length2) {
						return 1;
					} else if (length1 > length2) {
						return -1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				}
			}
		}

		public Comparator<File> getFileComparator() {
			return new SortFileSizeDec();
		}

		@Override
		public String toString() {
			return "SortFileSizeDecrease";
		}
	}

	public static class SortFileSizeDec implements Comparator<File> {
		@Override
		public int compare(final File p1, final File p2) {
			final boolean isFile1 = p1.isFile();
			final boolean isFile2 = p2.isFile();
			if (isFile1) {
				if (isFile2) {
					final long length1 = p1.length();
					final long length2 = p2.length();
					if (length1 < length2) {
						return 1;
					} else if (length1 > length2) {
						return -1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final String[] list1 = p1.list();
					final int length1 = ((list1 == null) ? 0 : list1.length);
					final String[] list2 = p2.list();
					final int length2 = ((list2 == null) ? 0 : list2.length);
					if (length1 < length2) {
						return 1;
					} else if (length1 > length2) {
						return -1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				}
			}
		}

		@Override
		public String toString() {
			return "SortFileSizeDec";
		}
	}
	
	//
	
	public static class SortFileSizeIncrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final boolean isFile1 = p1.file.isFile();
			final boolean isFile2 = p2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					final long length1 = p1.file.length();
					final long length2 = p2.file.length();
					if (length1 < length2) {
						return -1;
					} else if (length1 > length2) {
						return 1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final String[] list1 = p1.file.list();
					final int length1 = ((list1 == null) ? 0 : list1.length);
					final String[] list2 = p2.file.list();
					final int length2 = ((list2 == null) ? 0 : list2.length);
					if (length1 < length2) {
						return -1;
					} else if (length1 > length2) {
						return 1;
					} else {
						return p1.path.compareToIgnoreCase(p2.path);
					}
				}
			}
		}

		public Comparator<File> getFileComparator() {
			return new SortFileSizeInc();
		}

		@Override
		public String toString() {
			return "SortFileSizeIncrease";
		}
	}

	public static class SortFileSizeInc implements Comparator<File> {
		@Override
		public int compare(final File p1, final File p2) {
			final boolean isFile1 = p1.isFile();
			final boolean isFile2 = p2.isFile();
			if (isFile1) {
				if (isFile2) {
					final long length1 = p1.length();
					final long length2 = p2.length();
					if (length1 < length2) {
						return -1;
					} else if (length1 > length2) {
						return 1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					final String[] list1 = p1.list();
					final int length1 = ((list1 == null) ? 0 : list1.length);
					final String[] list2 = p2.list();
					final int length2 = ((list2 == null) ? 0 : list2.length);
					if (length1 < length2) {
						return -1;
					} else if (length1 > length2) {
						return 1;
					} else {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					}
				}
			}
		}

		@Override
		public String toString() {
			return "SortFileSizeInc";
		}
	}
	
	//
	
	public static class SortFileTypeDecrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final boolean isFile1 = p1.file.isFile();
			final boolean isFile2 = p2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					final String namef1 = p1.file.getName();
					int lastIndexOf = namef1.lastIndexOf(".");
					final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "");

					final String namef2 = p2.file.getName();
					lastIndexOf = namef2.lastIndexOf(".");
					final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "");

					final int comp = type2.compareToIgnoreCase(type1);
					if (comp == 0) {
						return p1.path.compareToIgnoreCase(p2.path);
					} else {
						return comp;
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return p1.path.compareToIgnoreCase(p2.path);
				}
			}
		}

		public Comparator<File> getFileComparator() {
			return new SortFileTypeDec();
		}

		@Override
		public String toString() {
			return "SortFileTypeDecrease";
		}
	}

	public static class SortFileTypeDec implements Comparator<File> {
		@Override
		public int compare(final File p1, final File p2) {
			final boolean isFile1 = p1.isFile();
			final boolean isFile2 = p2.isFile();
			if (isFile1) {
				if (isFile2) {
					final String namef1 = p1.getName();
					int lastIndexOf = namef1.lastIndexOf(".");
					final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "");

					final String namef2 = p2.getName();
					lastIndexOf = namef2.lastIndexOf(".");
					final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "");

					final int comp = type2.compareToIgnoreCase(type1);
					if (comp == 0) {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					} else {
						return comp;
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
				}
			}
		}

		@Override
		public String toString() {
			return "SortFileTypeDec";
		}
	}
	
	//
	
	public static class SortFileTypeIncrease implements DoubleCompare {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final boolean isFile1 = p1.file.isFile();
			final boolean isFile2 = p2.file.isFile();
			if (isFile1) {
				if (isFile2) {
					final String namef1 = p1.file.getName();
					int lastIndexOf = namef1.lastIndexOf(".");
					final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "");

					final String namef2 = p2.file.getName();
					lastIndexOf = namef2.lastIndexOf(".");
					final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "");

					final int comp = type1.compareToIgnoreCase(type2);
					if (comp == 0) {
						return p1.path.compareToIgnoreCase(p2.path);
					} else {
						return comp;
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return p1.path.compareToIgnoreCase(p2.path);
				}
			}
		}

		public Comparator<File> getFileComparator() {
			return new SortFileTypeInc();
		}

		@Override
		public String toString() {
			return "SortFileTypeIncrease";
		}
	}

	public static class SortFileTypeInc implements Comparator<File> {
		@Override
		public int compare(final File p1, final File p2) {
			final boolean isFile1 = p1.isFile();
			final boolean isFile2 = p2.isFile();
			if (isFile1) {
				if (isFile2) {
					final String namef1 = p1.getName();
					int lastIndexOf = namef1.lastIndexOf(".");
					final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "");

					final String namef2 = p2.getName();
					lastIndexOf = namef2.lastIndexOf(".");
					final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "");

					final int comp = type1.compareToIgnoreCase(type2);
					if (comp == 0) {
						return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
					} else {
						return comp;
					}
				} else {
					return 1;
				}
			} else {
				if (isFile2) {
					return -1;
				} else {
					return p1.getAbsolutePath().compareToIgnoreCase(p2.getAbsolutePath());
				}
			}
		}

		@Override
		public String toString() {
			return "SortFileTypeInc";
		}
	}
	//
	//
	//
	public static class SortFileOnlyNameDecrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo f1, final FileInfo f2) {
			final int compareToIgnoreCase = f2.file.getName()
				.compareToIgnoreCase(f1.file.getName());
			if (compareToIgnoreCase != 0) {
				return compareToIgnoreCase;
			} else {
				return f1.path.compareToIgnoreCase(f2.path);
			}
		}

		@Override
		public String toString() {
			return "SortFileOnlyNameDecrease";
		}
	}

	public static class SortFileOnlyNameIncrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo f1, final FileInfo f2) {
			final int compareToIgnoreCase = f1.file.getName()
				.compareToIgnoreCase(f2.file.getName());
			if (compareToIgnoreCase != 0) {
				return compareToIgnoreCase;
			} else {
				return f1.path.compareToIgnoreCase(f2.path);
			}
		}

		@Override
		public String toString() {
			return "SortFileOnlyNameIncrease";
		}
	}

	public static class SortFileOnlyDateDecrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final long lastModified1 = p1.file.lastModified();
			final long lastModified2 = p2.file.lastModified();
			if (lastModified1 < lastModified2) {
				return 1;
			} else if (lastModified1 > lastModified2) {
				return -1;
			} else {
				return p1.path.compareToIgnoreCase(p2.path);
			}
		}

		@Override
		public String toString() {
			return "SortFileOnlyDateDecrease";
		}
	}

	public static class SortFileOnlyDateIncrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final long lastModified1 = p1.file.lastModified();
			final long lastModified2 = p2.file.lastModified();
			if (lastModified1 < lastModified2) {
				return -1;
			} else if (lastModified1 > lastModified2) {
				return 1;
			} else {
				return p1.path.compareToIgnoreCase(p2.path);
			}
		}

		@Override
		public String toString() {
			return "SortFileOnlyDateIncrease";
		}
	}

	public static class SortFilePathDecrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo f1, final FileInfo f2) {
			return f2.file.getParent()
				.compareToIgnoreCase(f1.file.getParent());
		}

		@Override
		public String toString() {
			return "SortFilePathDecrease";
		}
	}

	public static class SortFilePathIncrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo f1, final FileInfo f2) {
			return f1.file.getParent()
				.compareToIgnoreCase(f2.file.getParent());
		}

		@Override
		public String toString() {
			return "SortFilePathIncrease";
		}
	}

	public static class SortFileOnlyTypeDecrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final String namef1 = p1.file.getName();
			int lastIndexOf = namef1.lastIndexOf(".");
			final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "").toLowerCase();

			final String namef2 = p2.file.getName();
			lastIndexOf = namef2.lastIndexOf(".");
			final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "").toLowerCase();

			if (type2.equals(type1)) {
				return p1.path.compareToIgnoreCase(p2.path);
			} else {
				return type2.compareTo(type1);
			}
		}

		@Override
		public String toString() {
			return "SortFileOnlyTypeDecrease";
		}
	}

	public static class SortFileOnlyTypeIncrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final String namef1 = p1.file.getName();
			int lastIndexOf = namef1.lastIndexOf(".");
			final String type1 = (lastIndexOf >= 0 ? namef1.substring(lastIndexOf) : "").toLowerCase();

			final String namef2 = p2.file.getName();
			lastIndexOf = namef2.lastIndexOf(".");
			final String type2 = (lastIndexOf >= 0 ? namef2.substring(lastIndexOf) : "").toLowerCase();
			if (type1.equals(type2)) {
				return p1.path.compareToIgnoreCase(p2.path);
			} else {
				return type1.compareTo(type2);
			}
		}

		@Override
		public String toString() {
			return "SortFileOnlyTypeIncrease";
		}
	}

	public static class SortGroupIncrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final int group1 = p1.groupNo;
			final int group2 = p2.groupNo;
			if (group1 < group2) {
				return -1;
			} else if (group1 > group2) {
				return 1;
			} else {
				return p1.path.compareToIgnoreCase(p2.path);
			}
		}

		@Override
		public String toString() {
			return "SortGroupIncrease";
		}
	}

	public static class SortGroupDecrease implements Comparator<FileInfo> {
		@Override
		public int compare(final FileInfo p1, final FileInfo p2) {
			final int group1 = p1.groupNo;
			final int group2 = p2.groupNo;
			if (group1 < group2) {
				return 1;
			} else if (group1 > group2) {
				return -1;
			} else {
				return p1.path.compareToIgnoreCase(p2.path);
			}
		}

		@Override
		public String toString() {
			return "SortGroupDecrease";
		}
	}

}
