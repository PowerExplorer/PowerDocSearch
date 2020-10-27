package net.gnu.util;

import java.util.*;
import java.io.*;
import java.security.*;
import java.math.*;
import android.util.*;
import java.nio.channels.*;
import java.nio.*;
import java.util.regex.Pattern;
import android.webkit.*;
import android.os.*;
import android.support.v4.provider.*;
import android.content.*;
import android.preference.*;
import android.net.*;
import android.annotation.*;
import net.gnu.util.HtmlUtil;
import org.mozilla.universalchardet.UniversalDetector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mozilla.universalchardet.CharsetListener;

public class FileUtil {

	private static final String TAG = "FileUtil";
	static final MimeTypeMap mime = MimeTypeMap.getSingleton();

    public static final String ALL_MIME_TYPES = "*/*";

    public static final byte[] UTF8_ESCAPE = new byte[] { -17, -69, -65 };
	private static final String ISO_8859_1 = "ISO-8859-1";

	// construct a with an approximation of the capacity
    public static final HashMap<String, String> MIME_TYPES = new HashMap<>(1 + (int)(255 / 0.75));

    static {
		/*
         * ================= MIME TYPES ====================
		 */
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("js", "application/javascript");

        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        //MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        //MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        //MIME_TYPES.put("ini", "text/plain");
        MIME_TYPES.put("md", "text/markdown");

        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");

        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");

        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        //MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        //MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", "application/octet-stream");
        MIME_TYPES.put("jar", "application/java-archive");
        //MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("z", "application/x-compress");

        MIME_TYPES.put("bat", "application/x-msdownload");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");

        MIME_TYPES.put("db", "application/octet-stream");
        MIME_TYPES.put("db3", "application/octet-stream");

        MIME_TYPES.put("otf", "application/x-font-otf");
        MIME_TYPES.put("ttf", "application/x-font-ttf");
        MIME_TYPES.put("psf", "application/x-font-linux-psf");

        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");

        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");

        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");

		MIME_TYPES.put("7z", "application/x-7z-compressed");
		MIME_TYPES.put("bz2", "application/x-bzip2");
		MIME_TYPES.put("gz", "application/x-gzip");
		MIME_TYPES.put("xz", "application/x-xz");
		MIME_TYPES.put("x", "application/x-compress");
		MIME_TYPES.put("rar", "application/x-rar-compressed");
		MIME_TYPES.put("tgz", "application/x-gtar");
		MIME_TYPES.put("zip", "application/zip");
		//s.add(new ComparableEntry<String, String>(".rar", "application/rar");
		//s.add(new ComparableEntry<String, String>(".zip", "application/x-zip");
		MIME_TYPES.put("", "application/octet-stream");
		MIME_TYPES.put("tar", "application/x-tar");
		MIME_TYPES.put("lzh", "application/x-lzh");

		MIME_TYPES.put("xls", "application/vnd.ms-excel");
		MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		MIME_TYPES.put("rtf", "text/rtf");
		MIME_TYPES.put("c", "text/x-csrc");
		MIME_TYPES.put("h", "text/x-chdr");
		MIME_TYPES.put("cpp", "text/x-c++src");
		MIME_TYPES.put("hpp", "text/x-c++hdr");
		MIME_TYPES.put("doc", "application/msword");
		MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		MIME_TYPES.put("odf", "application/vnd.oasis.opendocument.formula");
		MIME_TYPES.put("odp", "application/vnd.oasis.opendocument.presentation");
		MIME_TYPES.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		MIME_TYPES.put("odt", "application/vnd.oasis.opendocument.text");
		MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
		MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		MIME_TYPES.put("html", "text/html");
		MIME_TYPES.put("txt", "text/txt");
		MIME_TYPES.put("ini", "text/plain");
		MIME_TYPES.put("sh", "text/plain");
		MIME_TYPES.put("bat", "text/plain");
		MIME_TYPES.put("java", "text/java");
		MIME_TYPES.put("xml", "text/xml");
		MIME_TYPES.put("aidl", "text/plain");
		MIME_TYPES.put("properties", "text/plain");
		MIME_TYPES.put("md", "text/plain");
		MIME_TYPES.put("apk", "application/vnd.android.package-archive");


    }

	public static String getExtension(final File f) {
		return getExtensionFromName(f.getName());
	}

	public static String getExtensionFromName(final String fName) {
		if (fName != null) {
			int idx = fName.lastIndexOf(".");
			return (idx >= 0 ? fName.substring(++idx).toLowerCase() : "");
		} else {
			return "";
		}
	}

	public static String getMimeType(final String fileName) {
        String type = ALL_MIME_TYPES;
        final String extension = getExtensionFromName(fileName);

        // mapping extension to system mime types
        if (extension.length() > 0) {
            type = mime.getMimeTypeFromExtension(extension);
            if (type == null) {
                type = MIME_TYPES.get(extension);
            }
        }
        if (type == null)
			type = ALL_MIME_TYPES;
        return type;
    }

    /**
     * Get Mime Type of a file
     * @param file the file of which mime type to get
     * @return Mime type in form of String
     */
    public static String getMimeType(final File file) {
        String type = ALL_MIME_TYPES;
        final String extension = getExtensionFromName(file.getName());

        // mapping extension to system mime types
        if (extension.length() > 0) {
            type = mime.getMimeTypeFromExtension(extension);
            if (type == null) {
                type = MIME_TYPES.get(extension);
            }
        }
        if (type == null) 
			type = ALL_MIME_TYPES;
        return type;
    }

    public static boolean mimeTypeMatch(String mime, String input) {
        return Pattern.matches(mime.replace("*", ".*"), input);
    }

	//
	//
	//

	/**
	 0: number of files
	 1: total length
	 2: number of directories
	 */
	public static void getDirSize(final File f, final long[] l) {
		if (f.isFile()) {
			l[0]++;
			l[1] += f.length();
		} else {
			LinkedList<File> stk = new LinkedList<File>();
			stk.add(f);
			File fi = null;
			File[] fs;
			while (stk.size() > 0) {
				fi = stk.pop();
				fs = fi.listFiles();
				if (fs != null)
					for (File f2 : fs) {
						if (f2.isDirectory()) {
							stk.push(f2);
							l[2]++;
						} else {
							l[0]++;
							l[1] += f2.length();
						}
					}
			}
		}
	}

	public static Collection<File> getFilesAndFolder(final File f) {
		Log.d(TAG, "getFilesAndFolder " + f.getAbsolutePath());
		final List<File> fList = new LinkedList<File>();
		if (f != null) {
			final LinkedList<File> folderQueue = new LinkedList<File>();
			if (f.isDirectory()) {
				folderQueue.push(f);
			}
			fList.add(f);
			File fi = null;
			File[] fs;
			while (folderQueue.size() > 0) {
				fi = folderQueue.pop();
				fs = fi.listFiles();
				if (fs != null) {
					for (File f2 : fs) {
						if (f2.isDirectory()) {
							folderQueue.push(f2);
						} 
						fList.add(f2);
					}
				}
			}
		}
		return fList;
	}

	public static Collection<File> getFiles(final String[] fs) {
		if (fs != null) {
			File[] farr = new File[fs.length];
			int i = 0;
			for (String f : fs) {
				farr[i++] = new File(f);
			}
			return FileUtil.getFiles(farr);
		} else {
			return new LinkedList<File>();
		}
	}

	public static Collection<File> getFiles(File[] fs) {
		if (fs == null) {
			return new LinkedList<File>();
		}
		final LinkedList<File> ret = new LinkedList<File>();
		final LinkedList<File> folderQueue = new LinkedList<File>();
		for (File f : fs) {
			if (f.isFile()) {
				ret.add(f);
			} else {
				folderQueue.push(f);
			}
		}
		File fi = null;
		while (folderQueue.size() > 0) {
			fi = folderQueue.pop();
			fs = fi.listFiles();
			if (fs != null)
				for (File f : fs) {
					if (f.isFile()) {
						ret.add(f);
					} else {
						folderQueue.push(f);
					}
				}
		}
		return ret;
	}

	public static Collection<File> getFiles(final File f) {
		//Log.d(TAG, "getFiles " + f.getAbsolutePath());
		final List<File> fList = new LinkedList<File>();
		if (f != null) {
			final LinkedList<File> folderQueue = new LinkedList<File>();
			if (f.isDirectory()) {
				folderQueue.push(f);
			} else {
				fList.add(f);
			}
			File fi = null;
			File[] fs;
			while (folderQueue.size() > 0) {
				fi = folderQueue.pop();
				fs = fi.listFiles();
				if (fs != null) {
					for (File f2 : fs) {
						if (f2.isDirectory()) {
							folderQueue.push(f2);
						} else {
							fList.add(f2);
						}
					}
				}
			}
		}
		return fList;
	}

	public static List<File> getFiles(final String ff[], final Pattern includePattern, final Pattern excludePattern) {
		File[] farr = null;
		if (ff != null) {
			farr = new File[ff.length];
			int i = 0;
			for (String f : ff) {
				farr[i++] = new File(f);
			}
		}
		return getFiles(farr, includePattern, excludePattern);
	}

	public static List<File> getFiles(final File ff[], final Pattern includePattern, final Pattern excludePattern) {
		if (ff != null) {
			final Set<File> set = new TreeSet<File>();
			for (File f : ff) {
				set.addAll(getFiles(f, includePattern, excludePattern));
			}
			ArrayList<File> arrayList = new ArrayList<File>(set.size());
			arrayList.addAll(set);
			return arrayList;
		} else {
			return new LinkedList<File>();
		}
	}

	public static Collection<File> getFiles(final File ff, Pattern includePattern, Pattern excludePattern) {
		final List<File> lf = new LinkedList<File>();
		if (ff != null) {
			final LinkedList<File> folders = new LinkedList<File>();
			if (includePattern == null || includePattern.pattern().trim().length() == 0) {
				includePattern = Pattern.compile(".*");
			}
			if (excludePattern == null || excludePattern.pattern().trim().length() == 0) {
				excludePattern = Pattern.compile("[^\u0000-\uffff]+");
			}
			if (ff.isFile()) {
				String fName = ff.getName();
				Matcher inMatcher = includePattern.matcher(fName);
				Matcher exMatcher = excludePattern.matcher(fName);
				if (inMatcher.matches() && !exMatcher.matches()) {
					lf.add(ff);
				}
			} else {
				folders.push(ff);
//				Log.d("ff", ff.getAbsolutePath());
				File[] fs;
				File fi = null;
				while (folders.size() > 0) {
					fi = folders.pop();
					fs = fi.listFiles();
					if (fs != null)
//					Log.d("fs", fs + " != null");
						for (File f : fs) {
							if (f.isDirectory()) {
								folders.push(f);
							} else {
								String fName = f.getName();
								Matcher inMatcher = includePattern.matcher(fName);
								Matcher exMatcher = excludePattern.matcher(fName);
//								Log.d("inMatcher", includePattern.pattern());
//								Log.d("exMatcher", excludePattern.pattern());
//								Log.d("fName", fName);
//								Log.d("in.matches", inMatcher.matches() + "");
//								Log.d("ex.matches", exMatcher.matches() + "");
								if (inMatcher.matches() && !exMatcher.matches()) {
									lf.add(f);
								}
							}
						}
				}
			}
		}
		return lf;
	}

	public static Collection<File> getFilesBySuffix(final File ff, final String suffix, boolean includeFolder) {
		final List<File> lf = new LinkedList<File>();
		if (ff != null) {
			boolean isSuffixEmpty = Util.isEmpty(suffix);
			String[] suffixes = null;
			if (!isSuffixEmpty) {
				suffixes = suffix.toLowerCase().split(";\\s*");
				Arrays.sort(suffixes);
			}
			if (ff.isFile()) {
				if (isSuffixEmpty) {
					lf.add(ff);
				} else {
					String fName = ff.getName();
					int lastIndexOf = fName.lastIndexOf(".");
					if (lastIndexOf >= 0) {
						String extLower = fName.substring(lastIndexOf).toLowerCase();
						boolean chosen = Arrays.binarySearch(suffixes, extLower) >= 0;
						if (chosen) {
							lf.add(ff);
						}
					}
				}
			} else {
				File[] fs;
				File fi = null;
				final LinkedList<File> folders = new LinkedList<>();
				if (includeFolder) {
					lf.add(ff);
				}
				while (folders.size() > 0) {
					fi = folders.pop();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f : fs) {
							if (f.isDirectory()) {
								folders.push(f);
								if (includeFolder) {
									lf.add(f);
								}
							} else {
								if (isSuffixEmpty) {
									lf.add(f);
								} else {
									String fName = f.getName();
									int lastIndexOf = fName.lastIndexOf(".");
									if (lastIndexOf >= 0) {
										String extLower = fName.substring(lastIndexOf).toLowerCase();
										boolean chosen = Arrays.binarySearch(suffixes, extLower) >= 0;
										if (chosen) {
											lf.add(f);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return lf;
	}

	//TODO need change doc tree
	public static void stringToFile(String fileName, String contents)
	throws IOException {
		Log.d(TAG, "stringToFile " + fileName);
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileWriter fw = new FileWriter(tempFile);
		BufferedWriter bw = new BufferedWriter(fw);
		if (contents != null && contents.length() > 0) {
			bw.write(contents);
			flushClose(bw); //, bo fw vi loi
			f.delete();
			tempFile.renameTo(f);
		}
	}

	public static void charSequenceToFile(String fileName, CharSequence contents)
	throws IOException {
		Log.d(TAG, "charSequenceToFile " + fileName);
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileWriter fw = new FileWriter(tempFile);
		BufferedWriter bw = new BufferedWriter(fw);
		if (contents != null && contents.length() > 0) {
			bw.append(contents);
			flushClose(bw); //, bo fw vi loi
			f.delete();
			tempFile.renameTo(f);
		}
	}

	public static void writeAppendFileAsCharset(File file, String contents,
												String newCharset) throws IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.seek(raf.length());
		FileChannel fileChannel = raf.getChannel();
		byte[] oldCharArr = contents.getBytes(newCharset);
		if (raf.length() == 0 && "UTF-8".equalsIgnoreCase(newCharset) && 
			! (oldCharArr[0] == -17 && oldCharArr[1] == -69 && oldCharArr[2] == -65
			|| oldCharArr[0] == -61 && oldCharArr[1] == -96 && oldCharArr[2] == 13
			|| oldCharArr[0] == 49 && oldCharArr[1] == 48 && oldCharArr[2] == 41
			|| oldCharArr[0] == 77 && oldCharArr[1] == -31 && oldCharArr[2] == -69)
			) {
			fileChannel.write(ByteBuffer.wrap(UTF8_ESCAPE));
		}
		fileChannel.write(ByteBuffer.wrap(oldCharArr));
		fileChannel.close();
		raf.close();
	}
	
	public static String detectCharset(final File file) {
		//Log.d(TAG, "detectCharset" + file);
		InputStream is = null;
		FileInputStream fis = null;
		String encode = "utf-8";
        try {
			final long length = file.length();
			final int len = (int)(length < 65536 ? length : 65536);
            fis = new FileInputStream(file);
			is = new BufferedInputStream(fis , len);

            // preread leading 64KB
            int nread;
			int start = 0;
            byte[] buff = new byte[len];
            //nread = is.read(buff);
			if (length > 0) {
				while ((nread = is.read(buff, start, len - start)) > 0) {
					start += nread;
				}
			}
//            if ( USE_JUNIVERSALCHARDET ){
			//try {
			final UniversalDetector detector = new UniversalDetector(
				new CharsetListener() {
					public void report(String name) {
						Log.d(TAG, "detectCharset = " + name + ", " + file.getAbsolutePath());
					}
				}
			);
			detector.handleData(buff, 0, start);
			detector.dataEnd();
			encode = detector.getDetectedCharset();
			detector.reset();
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}

//            }else{
//                // Detect charset
//                NativeUniversalDetector detector;
//                if ( encode ==null || encode.length() == 0 ){
//
//                    try {
//                        detector = new NativeUniversalDetector();
//                        detector.handleData(buff, 0, nread);
//                        detector.dataEnd();
//                        encode = detector.getCharset();
//                        detector.destroy();
//                    } catch (DetectorException e1) {
//                    }
//                }
//            }

		} catch (IOException e1) {
            e1.printStackTrace();
        } finally {
			FileUtil.close(fis, is);
		}
		return encode;
	}

	public static String readFileAsCharset(File fileName, String charsetName)
	throws IOException {
		byte[] arr = readFileToMemory(fileName);
		String st = new String(arr, charsetName);
		return st;
	}

	public static String readFileWithCheckEncode(String filePath, byte[] byteArr)
	throws IOException, UnsupportedEncodingException {
		if (byteArr.length > 3) {
			//Log.d("readFileWithCheckEncode", filePath);
			Log.d(TAG, "readFileWithCheckEncode " + (char) byteArr[0] + ": " + (int) byteArr[0]
				  + ", " + (char) byteArr[1] + ": " + (int) byteArr[1]
				  + ", " + (char) byteArr[2] + ": " + (int) byteArr[2]
				  + ", " + filePath);

			if (byteArr.length > 3 
				&& (byteArr[0] == -17 && byteArr[1] == -69 && byteArr[2] == -65
				|| byteArr[0] == -61 && byteArr[1] == -96 && byteArr[2] == 13
				|| byteArr[0] == 49 && byteArr[1] == 48 && byteArr[2] == 41
				|| byteArr[0] == 77 && byteArr[1] == -31 && byteArr[2] == -69)) {
				String str = new String(byteArr, 3, byteArr.length - 3, HtmlUtil.UTF8);//
				Log.d(TAG, "is UTF8 1: " + filePath);
				return str;
			} else {
				String cs = detectCharset(new File(filePath));
				String str;
				if (cs != null) {
					str = new String(byteArr, cs);
				} else {
					str = new String(byteArr, "utf-8");
				}
				// System.err.println("utf8: " + utf8);
				String fontName = HtmlUtil.guessFontName(str);
				Log.d(TAG, cs + ": " + fontName + ": " + filePath);

				if (HtmlUtil.VU_TIMES.equals(fontName)
					|| HtmlUtil.TIMES_CSX.equals(fontName)
					|| HtmlUtil.TIMES_CSX_1.equals(fontName)) {
					Log.d(TAG, "is UTF8 2: " + filePath);
					return str;
				} else if (filePath.toLowerCase().contains(".pdf.")) {
					Log.d(TAG, "is not UTF8: " + filePath + " pdf String(byteArr)");
					return new String(byteArr);
				} else {
					String notUTF8 = new String(byteArr, ISO_8859_1);
					Log.d(TAG, "is not UTF8: " + filePath + " txt String(byteArr, ISO_8859_1)");
					return notUTF8;
				}
			}
		} else {
			return new String(byteArr);
		}
	}

	public static String readFileWithCheckEncode(String filePath)
	throws IOException, UnsupportedEncodingException {
		byte[] byteArr = FileUtil.readFileToMemory(filePath);
		return readFileWithCheckEncode(filePath, byteArr);
	}

	public static String readFileAsCharsetMayCheckEncode(String fileName, String charsetName)
	throws IOException {
		byte[] arr = readFileToMemory(fileName);
		String st = "";
		try {
			st = new String(arr, charsetName);
		} catch (UnsupportedEncodingException e) {
			st = readFileWithCheckEncode(fileName, arr);
		}
		return st;
	}

	public static String readFileAsCharsetMayCheckEncode(File file, String charsetName)
	throws IOException {
		return readFileAsCharsetMayCheckEncode(file.getAbsolutePath(), charsetName);
	}

	public static String readFileByMetaTag(File file)
	throws FileNotFoundException, IOException {
		String content = "";
		String charsetName = "";
		if ((charsetName = HtmlUtil.readValue(content, "charset")).length() > 0) {
			Log.d(TAG, "readFileByMetaTag charset "  + charsetName + ", " + file.getAbsolutePath());
		}
		try {
			if (charsetName.length() > 0) {
				content = new String(FileUtil.readFileToMemory(file.getAbsolutePath()), charsetName);
				// Log.d(content);
			} else {
				content = FileUtil.readFileWithCheckEncode(file.getAbsolutePath());
			}
		} catch (UnsupportedEncodingException e) {
			content = FileUtil.readFileWithCheckEncode(file.getAbsolutePath());
			e.printStackTrace();
		}
		return content;
	}

	public static Collection<File> getFilesBy(final File[] ff, final String in) {
		if (ff != null && ff.length > 0) {
			final List<File> lf = new LinkedList<File>();
			for (File f : ff) {
				lf.addAll(getFilesBy(f, in));
			}
			return lf;
		} else {
			return new LinkedList<File>();
		}
	}

	public static void writeFileAsCharset(String fileName, String contents,
										  String newCharset) throws IOException {
		writeFileAsCharset(new File(fileName), contents, newCharset);
	}

	public static void writeFileAsCharset(File file, String contents,
										  String newCharset) throws IOException {
		Log.d("Writing file", file.getAbsolutePath());
		//		System.out.println(contents);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		File tempF = new File(file.getAbsolutePath() + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempF);
		FileChannel fileChannel = fos.getChannel();
		byte[] oldCharArr = contents.getBytes(newCharset);
		if ("UTF-8".equalsIgnoreCase(newCharset) && 
			!(oldCharArr[0] == -17 && oldCharArr[1] == -69 && oldCharArr[2] == -65
			|| oldCharArr[0] == -61 && oldCharArr[1] == -96 && oldCharArr[2] == 13
			|| oldCharArr[0] == 49 && oldCharArr[1] == 48 && oldCharArr[2] == 41
			|| oldCharArr[0] == 77 && oldCharArr[1] == -31 && oldCharArr[2] == -69)
			) {
			fileChannel.write(ByteBuffer.wrap(UTF8_ESCAPE));
		}
		fileChannel.write(ByteBuffer.wrap(oldCharArr));
		fileChannel.force(true);
		close(fileChannel);
		flushClose(fos);
		file.delete();
		tempF.renameTo(file);
		//boolean renameRet = tempF.renameTo(file);
		//		if (renameRet) {
		//			Log.d("writeFileAsCharset", "rename " + " to " + file + " successfully");
		//		} else {
		//			Log.d("writeFileAsCharset", "rename " + " to " + file + " unsuccessfully");
		//		}
	}

	public static void is2OsNotCloseOs(InputStream is, BufferedOutputStream bos)
	throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] barr = new byte[32768];
		int read = 0;
		while ((read = bis.read(barr)) > 0) {
			bos.write(barr, 0, read);
		}
		close(bis, is);
		bos.flush();
	}

	public static byte[] is2Barr(InputStream is, boolean autoClose) throws IOException {
		int count = 0;
		int len = 65536;
		byte[] buffer = new byte[len];
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream bb = new ByteArrayOutputStream(20 << 1);
		while ((count = bis.read(buffer, 0, len)) > 0) {
			bb.write(buffer, 0, count);
		}
		if (autoClose) {
			FileUtil.close(bis, is);
		} 

		return bb.toByteArray();
	}

	public static int is2Barr(final BufferedInputStream in, final byte[] bytes)
	throws IOException {
		final int length = bytes.length;
		int count = 0;
		int read = 0;
		while (length > read && (count = in.read(bytes, read, length - read)) > 0) {
			read += count;
			//Log.d(TAG, "count " + count);
		}
		return read;
	}

	public static void bArr2File(byte[] barr, String fileName)
	throws IOException {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		try {
			bos.write(barr, 0, barr.length);
		} finally {
			flushClose(bos, fos);
			file.delete();
			tempFile.renameTo(file);
		}
	}

	public static boolean delete(File file) {
		file.setWritable(true);
		try {
			if (!file.delete()) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(0);
				fos.flush();
				fos.close();
			}
			Log.d("delete", "Deleted file: " + file + " successfully");
			return true;
		} catch (IOException e) {
			Log.d("delete", "The deleting file: " + file + " is not successfully", e);
			return false;
		}
	}

	public static StringBuilder deleteFiles(File file, String lineSep, boolean includeFolder, String ext) {
		StringBuilder sb = new StringBuilder();
		return deleteFiles(file, sb, includeFolder, ext);
	}

	/**
	 * @param file file to delete
	 * @param lineSep lineSep for summary
	 * @param sb StringBuilder to store results
	 * @param includeFolder whether also delete folder or not
	 * @param ext file extension nào sẽ bị xóa
	 * @return
	 */
	public static StringBuilder deleteFiles(File file, 
											StringBuilder sb, boolean includeFolder, 
											String ext) {
		Collection<File> l = getFilesBySuffix(file, ext, includeFolder);
		if (sb == null) {
			sb = new StringBuilder();
		}
		for (File f : l) {
			delete(f, sb);
		}
		return sb;
	}

	private static void delete(File file, StringBuilder sb) {
		long length = file.length();
		boolean deleted = file.delete();
		if (deleted) {
			sb.append(file.getAbsolutePath() + " length " + length + " bytes, deleted.\n");
		} else {
			sb.append(file.getAbsolutePath() + " length " + length + " bytes, can't delete.\n");
		}
	}

	public static int deleteFiles(File file, boolean includeFolder) {
		Collection<File> l = getFilesAndFolder(file);
		for (File f : l) {
			delete(f);
		}
		return l.size();
	}

	public static boolean compareFileContent(final File f1, final File f2) throws IOException {
		//Log.d("compare file ", f1.getName() + " and " + f2.getName());
		final long len = f1.length();
		if (len != f2.length()) {
			return false;
		} else if (len == 0 || f1.equals(f2)) {
			return true;
		}
		FileInputStream fis1 = null;
		FileInputStream fis2 = null;
		BufferedInputStream bis1 = null;
		BufferedInputStream bis2 = null;
		try {
			fis1 = new FileInputStream(f1);
			fis2 = new FileInputStream(f2);
			bis1 = new BufferedInputStream(fis1);
			bis2 = new BufferedInputStream(fis2);

			final int BUFFER_SIZE = 65536;
			final byte[] bArr1 = new byte[BUFFER_SIZE];
			final byte[] bArr2 = new byte[BUFFER_SIZE];

			int read = 0;
			long counter = 0;
			int i;
			while (counter < len && (read = is2Barr(bis1, bArr1)) == is2Barr(bis2, bArr2)) {
				counter += read;
				for (i = 0; i < read; i++) {
					if (bArr1[i] != bArr2[i]) {
						return false;
					}
				}
			} 
			return true;
		} finally {
			close(bis1, bis2, fis1, fis2);
		}
	}



	//
	//
	//
	public static Collection<File> getFilesBy(final File ff, String in) {
		final List<File> lf = new LinkedList<File>();
		if (ff != null && in != null && in.length() > 0) {
			in = in.replaceAll(Util.SPECIAL_CHAR_PATTERNSTR, "\\\\$1");
			in = ".*?" + in + ".*?";
			final Pattern p = Pattern.compile(in, Pattern.CASE_INSENSITIVE);
			if (!ff.isDirectory()) {
				if (p.matcher(ff.getName()).matches()) {
					lf.add(ff);
				}
			} else {
				File[] fs;
				File fi = null;
				final LinkedList<File> folders = new LinkedList<>();
				folders.push(ff);
				if (p.matcher(ff.getName()).matches()) {
					lf.add(ff);
				}
				while (folders.size() > 0) {
					fi = folders.pop();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f : fs) {
							if (f.isDirectory()) {
								folders.push(f);
							}
							if (p.matcher(f.getName()).matches()) {
								lf.add(f);
							}
						}
					}
				}
			}
		}
		return lf;
	}

	public static String getPathHash(final String filepath) {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte data[] = (filepath + new File(filepath).lastModified()).getBytes();
			final BigInteger bigInteger = new BigInteger(1, md.digest(data));
			return bigInteger.toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("cannot initialize MD5 hash function", e);
		}
	}

	public static byte[] readFileToMemory(final File file) throws IOException {
		final long length = file.length();
		if (length < Integer.MAX_VALUE) {
			final FileInputStream fis = new FileInputStream(file);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final int len = (int) length;
			final byte[] data = new byte[len];
			int start = 0;
			int read = 0;
			try {
				while ((read = bis.read(data, start, len - start)) > 0) {
					start += read;
				}
			} finally {
				close(bis, fis);
			}
			return data;
		} else {
			throw new IOException("File is bigger than " + Util.nf.format(Integer.MAX_VALUE) + " bytes");
		}
	}

	public static byte[] readFileToMemory(String file)
	throws IOException {
		return readFileToMemory(new File(file));
	}

	public static void is2OS(final InputStream is, final OutputStream os) throws IOException {
		final BufferedOutputStream bos = new BufferedOutputStream(os);
		final BufferedInputStream bis = new BufferedInputStream(is);
		final byte[] barr = new byte[32768];
		int read = 0;
		try {
			while ((read = bis.read(barr)) > 0) {
				bos.write(barr, 0, read);
			}
		} finally {
			close(bis);
			flushClose(bos);
		}
	}

	//TODO need change doc tree
	public static void is2File(final InputStream is, final String fileName) throws IOException {
		final File file = new File(fileName);
		file.getParentFile().mkdirs();
		final File tempFile = new File(fileName + ".tmp");
		final FileOutputStream fos = new FileOutputStream(tempFile);
		final BufferedOutputStream bos = new BufferedOutputStream(fos);
		final BufferedInputStream bis = new BufferedInputStream(is);
		final byte[] barr = new byte[32768];
		int read = 0;
		try {
			while ((read = bis.read(barr)) > 0) {
				bos.write(barr, 0, read);
			}
		} finally {
			close(is, bis);
			flushClose(bos, fos);
			file.delete();
			tempFile.renameTo(file);
		}
	}
	public static void close(final Closeable... closable) {
		if (closable != null && closable.length > 0) {
			for (Closeable c : closable) {
				try {
					if (c != null) {
						c.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void flushClose(final OutputStream... closable) {
		if (closable != null && closable.length > 0) {
			for (OutputStream c : closable) {
				if (c != null) {
					try {
						c.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						c.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void flushClose(final Writer... closable) {
		if (closable != null && closable.length > 0) {
			for (Writer c : closable) {
				if (c != null) {
					try {
						c.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						c.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}



}
