package net.gnu.agrep;

import android.os.AsyncTask;
import android.util.Log;
import java.util.Collections;
import android.widget.Toast;
import java.util.regex.Pattern;
import android.os.PowerManager;
import java.io.File;
import java.util.TreeSet;
import java.util.LinkedList;
import java.io.IOException;
import net.gnu.common.SearcherAplication;
import net.gnu.util.HtmlUtil;
import net.gnu.util.FileUtil;
import java.io.StringWriter;
import java.io.FileInputStream;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
import org.apache.tika.metadata.Metadata;
import net.gnu.util.PoiUtils;
import com.free.p7zip.ExtractFile;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.ArrayList;
import net.gnu.util.Util;
import net.gnu.pdfplugin.ITextUtil;
import android.content.Context;
import java.io.StringReader;

public class GrepTask extends AsyncTask<String, Object, Boolean> {
	private static final String TAG = "GrepTask";

	//private ProgressDialog mProgressDialog;
	private int mFileCount=0;
	private int mFoundcount=0;
	private boolean mCancelled;
	private RetainFrag retainFrag;
	
	private long start = System.nanoTime();

	public GrepTask(RetainFrag retainFrag) {
		this.retainFrag = retainFrag;
	}

	@Override
	public String toString() {
		return "GrepTask " + super.toString() + ", retainFrag " + retainFrag;
	}

	@Override
	protected void onPreExecute() {
		mCancelled = false;
		runProgress();
	}

	void runProgress() {
		Log.d(TAG, "runProgress");
//			mProgressDialog = new ProgressDialog(searchActivity);
//            mProgressDialog.setTitle(R.string.grep_spinner);
//            mProgressDialog.setMessage(mQuery);
//            mProgressDialog.setIndeterminate(true);
//            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            mProgressDialog.setCancelable(true);
//            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//					@Override
//					public void onCancel(DialogInterface dialog) {
//						mCancelled = true;
//						cancel(false);
//					}
//				});
//            mProgressDialog.show();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		return grepRoot(params[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		Log.d(TAG, "onPostExecute.retainFrag.mData" + retainFrag.mData + ", hidden " + retainFrag.hidden);
//			if (mProgressDialog != null) {
//				mProgressDialog.dismiss();
//				mProgressDialog = null;
//			}

		Collections.sort(retainFrag.mData);
		if (!retainFrag.hidden) {
			synchronized (retainFrag.mData) {
				retainFrag.mAdapter.notifyDataSetChanged();
			}
			retainFrag.searchFragment.mGrepView.setSelection(0);
		}
		Toast.makeText(retainFrag.getContext(), result ?R.string.grep_finished: R.string.grep_canceled, Toast.LENGTH_LONG).show();
//          if (retainFrag.opUtil != null) {
//				retainFrag.opUtil.releaseOpService();
//			}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		onPostExecute(false);
	}

	@Override
	protected void onProgressUpdate(Object... progress) {
		if (isCancelled()) {
			return;
		}
		if (retainFrag.cache != null) {
			retainFrag.searchFragment.statusTV.setText("Found " + mFoundcount + " " + retainFrag.getString(R.string.progress, retainFrag.searchFragment.mQuery, mFileCount) + ", cached " + retainFrag.cache.cacheStatus + ", took " + Util.nf.format(System.nanoTime() - start) + " ns");
		}
//			if (mProgressDialog != null) {
//				mProgressDialog.setMessage(searchActivity.getString(R.string.progress, mQuery, mFileCount));
//			}
		if (progress != null) {
			if (progress[0] instanceof String) {
				retainFrag.searchFragment.statusTV.setText((CharSequence)progress[0]);
			} else {
				synchronized (retainFrag.mData) {
					for (Object data : progress) {
						retainFrag.mData.add((GrepView.Data)data);
					}
					retainFrag.mAdapter.notifyDataSetChanged();
					retainFrag.searchFragment.mGrepView.setSelection(retainFrag.mData.size() - 1);
				}
			}
		}
		//}
	}

	private boolean grepRoot(String text) {

		PowerManager.WakeLock wl = null;
		try {
			PowerManager pm = (PowerManager)retainFrag.getContext().getSystemService(
				Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			wl.acquire();

			if (retainFrag.fileList == null || retainFrag.newSearch) {
				retainFrag.fileList = new TreeSet<>();
				for (CheckedString dir : retainFrag.searchFragment.mPrefs.mDirList) {
					//Log.d(TAG, "grepRoot " + text + ", dir " + dir);
					if (dir.checked && !grepDirectory(new File(dir.string))) {
						return false;
					}
				}
				retainFrag.cache = new Cache(retainFrag.fileList);
				retainFrag.newSearch = false;
			}
			File next;
			while (retainFrag.cache.hasNext()) {
				next = retainFrag.cache.next();
				grepFile(next, retainFrag.cache.get(next));
			}
//			for (File f : retainFrag.fileList) {
//				grepFile(f);
//			}
			retainFrag.cache.reset();
		} finally {
			if (wl != null) {
				wl.release();
			}
		}
		return true;
	}


	boolean grepDirectory(final File dir) {
		//Log.d(TAG, "grepDirectory " + dir);
		if (isCancelled()) {
			return false;
		}
		boolean ret = true;
		if (dir.isFile()) {
			return checkExt(dir);
		} else {
			final LinkedList<File> folderQueue = new LinkedList<File>();
			folderQueue.push(dir);
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
							ret = checkExt(f2);
						}
					}
					if (!ret) {
						return false;
					}
				}
			}
		}
		return true;
	}

	boolean grepFile(final File file, final String str) {
		//Log.d(TAG, "grepFile " + file);
		if (isCancelled()) {
			return false;
		}
//		final InputStream is;
//		try {
//			is = new BufferedInputStream(new FileInputStream(file));//, 65536);
//			is.mark(65536);
//
//			//  文字コードの判定
//			String encode = null;
//			try {
//				final UniversalDetector detector = new UniversalDetector();
//				try {
//					int nread;
//					byte[] buff = new byte[4096];
//					if ((nread = is.read(buff)) > 0) {
//						detector.handleData(buff, 0, nread);
//					}
//					detector.dataEnd();
//				} catch ( FileNotFoundException e ) {
//					e.printStackTrace();
//					is.close();
//					return true;
//				} catch (IOException e) {
//					e.printStackTrace();
//					is.close();
//					return true;
//				}
//				encode = detector.getCharset();
//				detector.reset();
//				detector.destroy();
//			} catch ( UniversalDetector.DetectorException e) {
//			}
//			is.reset();
		BufferedReader br=null;
		try {
//				if (encode != null) {
//					br = new BufferedReader(new InputStreamReader(is, encode), 65536);
//				} else {
			br = new BufferedReader(new StringReader(str));//new InputStreamReader(is), 65536);
			//}

			String text;
			int line = 0;
			Pattern pattern = retainFrag.searchFragment.mPattern;
			Matcher m = null;
			ArrayList<GrepView.Data>    data  = null ;
			mFileCount++;
			while ((text = br.readLine()) != null) {
				line ++;
				if (m == null) {
					m = pattern.matcher(text);
				} else {
					m.reset(text);
				}
				if (m.find()) {
					//found = true;

					synchronized (retainFrag.mData) {
						mFoundcount++;
						if (data == null) {
							data = new ArrayList<GrepView.Data>();
						}
						data.add(new GrepView.Data(file, line, text));

						if (mFoundcount < 20) {
							publishProgress(data.toArray(new GrepView.Data[0]));
							data = null;
						}
					}
					if (mCancelled) {
						break;
					}
				}
			}
			//br.close();
			//is.close();
			if (data != null) {
				publishProgress(data.toArray(new GrepView.Data[0]));
				data = null;
			} else {
				publishProgress((GrepView.Data[])null);
			}
			//if (!found) {
			//if (mFileCount % 10 == 0) {
			//publishProgress((GrepView.Data[])null);
			//}
			//}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		return true;
	}

	private boolean checkExt(final File dir) {
		if (isCancelled()) {
			return false;
		}
		for (CheckedString ext : retainFrag.searchFragment.mPrefs.mExtList) {
			if (ext.checked) {
				if (ext.string.equals("*")) {
					if (dir.getName().indexOf('.') == -1) {
						try {
							return convert(dir);
						} catch (Exception e) {
							Log.e(TAG, "checkExt " + dir, e);
						}
					}
				} else if (dir.getName().toLowerCase().endsWith("." + ext.string)) {
					try {
						return convert(dir);
					} catch (Exception e) {

					}
				}
			}
		}
		return true;
	}

	private final Pattern htmlPat = Pattern.compile("(htm|html|xhtml|shtm|shtml)");
	//private final Pattern plainPat = Pattern.compile("(txt|ini|mk|md|list|config|configure|js|bat|sh|lua|depend|java|c|cpp|h|hpp|jsp|machine|asm|css|desktop|inc|i|plist|pro|py|s|xpm|php|gradle)");
	private final Pattern zipPat = Pattern.compile("(zip|gz|7z|bz2|jar|tar|rar|arj|lzh|chm|xz|z)");
	private long totalSelectedSize;

	private boolean convert(final File inFile) throws IOException, Exception {
		//Log.d(TAG, "convert " + inFile);
		final String inFilePath = inFile.getAbsolutePath();
		final File newFile;
		if (inFilePath.startsWith(SearcherAplication.PRIVATE_PATH)) {
			newFile = new File(inFilePath + HtmlUtil.CONVERTED_TXT);
		} else {
			newFile = new File(SearcherAplication.PRIVATE_PATH + inFilePath + HtmlUtil.CONVERTED_TXT);
		}
		// file text được chọn đã được convert từ trước
		if (newFile.exists() 
			&& (newFile.lastModified() > inFile.lastModified())) {
			publishProgress("already converted " + inFilePath);
			retainFrag.fileList.add(newFile);
			totalSelectedSize += newFile.length();
			//Log.d("already converted newFile", String.valueOf(newFile));
			return true;
		}
		String fileContent = "";
		final String name = inFile.getName();
		final String inFilePathLowerCase = name.toLowerCase();
		String ext = FileUtil.getExtensionFromName(inFilePathLowerCase);
		String mimeTypeFromExtension = FileUtil.getMimeType(inFile);
		// file duoc chon co duoi .converted
		if (htmlPat.matcher(ext).matches()) {
			fileContent = HtmlUtil.htmlToText(inFile);
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
		} else if (inFilePathLowerCase.endsWith(HtmlUtil.CONVERTED_TXT)
			|| inFilePathLowerCase.endsWith(".converted.txt")
			) {
			publishProgress("adding converted text " + inFilePath);
			retainFrag.fileList.add(inFile);
			// file txt được chọn có thể đã được convert từ trước nhưng đã cũ
		} else if (ext.equals("pdf")) {
			publishProgress("converting " + inFilePath);
			final String pdfTextPath = SearcherAplication.PRIVATE_PATH + inFilePath + ".txt";
			final File txtFile = new File(pdfTextPath);
			//if (retainFrag.opService == null) {
//					retainFrag.opUtil = new OpServiceUtil(searchActivity);
//					retainFrag.opUtil.bindOpService("net.gnu.agrep", "net.gnu.pdfplugin.ITextService");
//					Log.d(TAG, retainFrag.opUtil + ".");
//					retainFrag.opService = retainFrag.opUtil.getOpService();
//				OpServiceConnection opServiceConnection = OpServiceUtil.startOpService(getApplicationContext(), "net.gnu.agrep", "net.gnu.pdfplugin.ITextService");
//				IOperation opService = OpServiceUtil.getOpService(opServiceConnection);
//				Log.d(TAG, opService + ".");
			ITextUtil.pdfToText(inFilePath, pdfTextPath);
			//} else {
			//	retainFrag.opService.pdfToText(inFilePath, pdfTextPath);
			//}
			if (txtFile.exists()) {
				fileContent = HtmlUtil.filterCRPDF(txtFile).replaceAll("ƣ", "ư");
				txtFile.delete();
			}
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
			// } else if (inFilePathLowerCase.endsWith(".docx")
			// || inFilePathLowerCase.endsWith(".xlsx")
			// || inFilePathLowerCase.endsWith(".pptx")) { 
			// !.pdf" convert sang text rồi tự đoán font
			// s.currentContent = Writer.getChangedFont(inFile);
			// FileInputStream fis = new FileInputStream(inFile);
			// if (currentFName.endsWith("docx")) {
			// XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(fis));
			// s.currentContent = extractor.getText();
			// } else if (currentFName.endsWith("xlsx")) {
			// XSSFWorkbook workbook = new XSSFWorkbook(fis);
			// XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
			// s.currentContent = extractor.getText();
			// } else if (currentFName.endsWith("pptx")) {
			// XMLSlideShow slideShow = new XMLSlideShow(fis);
			// XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slideShow);
			// s.currentContent = extractor.getText();
			//}
			// fis.close();
		} else if (ext.equals("rtf")) {
			publishProgress("converting " + inFilePath);
			Metadata metadata = new Metadata();
			StringWriter writer = new StringWriter();
			FileInputStream fis = new FileInputStream(inFile);
			final org.apache.tika.parser.rtf.TextExtractor ert = new org.apache.tika.parser.rtf.TextExtractor(new XHTMLContentHandler(new WriteOutContentHandler(writer), metadata), metadata);
			ert.extract(fis);
			fileContent = HtmlUtil.changeToVUTimes(writer.toString()); // RTF2Txt.rtfToText(inFile)
		} else if (ext.equals("epub")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(Epub2Txt.epub2txt(inFile));
		} else if (ext.equals("fb2")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(FB2Txt.fb2txt(inFile));
		} else if (ext.equals("docx")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(DocxToText.docxToText(inFile));
		} else if (ext.equals("xlsx")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(XLSX2Text.getText(inFile));
		} else if (ext.equals("pptx")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(PPTX2Text.pptx2Text(inFile));
		} else if (ext.equals("odt")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(OdtToText.odtToText(inFile));
		} else if (ext.equals("ods")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(ODSToText.odsToText(inFile));
		} else if (ext.equals("odp")) {
			publishProgress("converting " + inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(ODPToText.odpToText(inFile));
		} else if (ext.equals("doc")) { // !.pdf"
			publishProgress("converting " + inFilePath);
			fileContent = PoiUtils.readWordFileToText(inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
		} else if (ext.equals("pub")) {
			publishProgress("converting " + inFilePath);
			fileContent = PoiUtils.getPublisherText(inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
		} else if (ext.equals("vsd")) {
			publishProgress("converting " + inFilePath);
			fileContent = PoiUtils.getVisioText(inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
		} else if (ext.equals("ppt")
				   || ext.equals("pps")) {
			publishProgress("converting " + inFilePath);
			fileContent = PoiUtils.getPowerPointText(inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
		} else if (ext.equals("xls")) {
			publishProgress("converting " + inFilePath);
			fileContent = PoiUtils.getExcelText(inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
			//try {
			//AbstractHtmlExporter exporter = new HtmlExporterNG2();
			//OutputStream os = new FileOutputStream(currentFName + Util.CONVERTED_TXT);
			//StreamResult result = new StreamResult(os);
			//WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inFile);
			//exporter.html(wordMLPackage, result, new HTMLSettings());
			//result.getOutputStream().close();
			//} catch (Exception e) {
			//e.printStackTrace();
			//}
		} else if (zipPat.matcher(ext).matches()) {
			final String outDirFilePath;
			if (inFilePath.startsWith(SearcherAplication.PRIVATE_PATH)) {
				int lastIndexOf = name.lastIndexOf(".");
				outDirFilePath = inFile.getParent() + "/" + name.substring(0, lastIndexOf) + "_" + name.substring(lastIndexOf + 1);
			} else {
				outDirFilePath = SearcherAplication.PRIVATE_PATH + inFile;
			}

			File outDirFile = new File(outDirFilePath);
			publishProgress("processing " + inFilePath);
			outDirFile.mkdirs();
			Log.d(TAG, "outDirFilePath " + outDirFilePath);

			ExtractFile extractFile = new ExtractFile(inFilePath, outDirFilePath);
			try {
				String zeName;
				List<File> extractedList = new LinkedList<File>();
				Collection<String> entryFileList = new HashSet<String>();
				while ((zeName = extractFile.getNextEntry()) != null) {

					String zeNameLower = zeName.toLowerCase();
					File entryFile = new File(outDirFilePath + "/" + zeName);
					File convertedEntryFile = new File(entryFile.getAbsolutePath() + HtmlUtil.CONVERTED_TXT); // khi chạy đệ quy thì tạo thêm getFilesDir()
					Log.d("convertedEntryFile", convertedEntryFile + " exist: " + convertedEntryFile.exists());

					ext = FileUtil.getExtension(entryFile);
					mimeTypeFromExtension = FileUtil.getMimeType(entryFile);
					//Log.d("mime entryFile", mimeType + " is " + fileExtensionFromUrl + " : " + mimeTypeFromExtension + (mimeTypeFromExtension != null && mimeTypeFromExtension.startsWith(("text"))));

					if (!zeName.endsWith("/")//.isDirectory()
						&& (convertedEntryFile.exists() 
						&& convertedEntryFile.lastModified() >= inFile.lastModified())) {
						//publishProgress("adding converted file: " + convertedEntryFile);
						Log.d("adding converted file: ", convertedEntryFile + " ");
						retainFrag.fileList.add(convertedEntryFile);
					} else if (!zeName.endsWith("/")//.isDirectory()
							   && (entryFile.exists() 
							   && entryFile.lastModified() >= inFile.lastModified())) {
						//publishProgress("adding source file: " + entryFile);
						Log.d("adding source file: ", entryFile + " ");
						extractedList.add(entryFile);
					} else if (!zeName.endsWith("/")//.isDirectory()
							   && (zeNameLower.matches(SettingsFragment.SEARCH_FILES_SUFFIX)
							   || (mimeTypeFromExtension.startsWith("text")))) {
						//publishProgress("extracting " + inFile + "/" + zeName);
						Log.d("extracting zeName", entryFile.toString());
						//zis.saveToFile(zeName);
						entryFileList.add(zeName);
						extractedList.add(entryFile);
						Log.d("entryFile", entryFile + " written, size: " + entryFile.length());
					}               
				}
				if (entryFileList.size() > 0) {
					extractFile.extractEntries(entryFileList, false);
				}
				String fname;
				for (File file : extractedList) {
					checkExt(file);
					fname = file.getName().toLowerCase();
					if (fname.matches(".*?\\.(doc|ppt|xls|docx|odt|pptx|xlsx|odp|ods|epub|fb2|htm|html|rtf|pdf)")) {
						Log.d("delete", file.getAbsolutePath());
						//tempFList.add(file.getAbsolutePath());
						file.delete();
					}
				}
				return true;
			} catch (Exception e) {
				Log.d("zip process source file", e.getMessage(), e);
			} finally {
				Log.d("GetSourceFileTask", "zis.close()");
				extractFile.close();
			}
		} else {//}if (plainPat.matcher(ext).matches()) {
			publishProgress("converting " + inFilePath);
			fileContent = FileUtil.readFileWithCheckEncode(inFilePath);
			fileContent = HtmlUtil.changeToVUTimes(fileContent);
		} 
		if (fileContent != null && fileContent.length() > 0) {
			FileUtil.writeFileAsCharset(newFile, fileContent, HtmlUtil.UTF8);
			retainFrag.fileList.add(newFile);
			totalSelectedSize += fileContent.getBytes().length;
			Log.d("newFile exist", newFile + " just written: " + newFile.exists());
		} 
		return true;
	}
}
