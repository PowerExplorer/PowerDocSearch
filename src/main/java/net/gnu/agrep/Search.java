//package net.gnu.agrep;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.mozilla.universalchardet.UniversalDetector;
//
//import android.annotation.SuppressLint;
//import android.app.ActionBar;
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.app.SearchManager;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.text.style.BackgroundColorSpan;
//import android.text.style.ForegroundColorSpan;
//import android.view.MenuItem;
//import android.widget.Toast;
//import android.util.Log;
//import java.util.List;
//import java.util.LinkedList;
//import android.os.PowerManager;
//import android.content.Context;
//import net.gnu.util.FileUtil;
//import net.gnu.util.HtmlUtil;
//import net.gnu.common.SearcherAplication;
//import net.gnu.aidl.IOperation;
//import net.gnu.pdfplugin.OpServiceUtil;
//import org.apache.tika.metadata.Metadata;
//import java.io.StringWriter;
//import net.gnu.util.PoiUtils;
//import com.free.p7zip.ExtractFile;
//import java.util.Collection;
//import java.util.HashSet;
//import org.apache.tika.sax.XHTMLContentHandler;
//import org.apache.tika.sax.WriteOutContentHandler;
//import java.util.TreeSet;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentActivity;
//import net.gnu.pdfplugin.OpServiceConnection;
//import net.gnu.pdfplugin.ITextUtil;
//import net.gnu.util.Util;
//import android.os.AsyncTask.Status;
//
//@SuppressLint("DefaultLocale")
//public class Search extends FragmentActivity implements GrepView.Callback {
//
//	private static final String TAG = "Search";
//
//    private GrepView mGrepView;
//    
//    private RetainFrag retainFrag;
//    private Prefs mPrefs;
//	
//	private String mQuery;
//	private TreeSet<File> fileList;
//	private Pattern mPattern;
//	
//	int no = 1;
//	
//	@Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.d(TAG, "onCreate.savedInstanceState " + savedInstanceState);
//		
//		ActionBar actionBar = getActionBar();
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//
//        setContentView(R.layout.result);
//		FragmentManager supportFragmentManager = getSupportFragmentManager();
//		retainFrag = (RetainFrag)supportFragmentManager.findFragmentByTag("retainFrag");
//		if (retainFrag == null) {
//			retainFrag = new RetainFrag();
//			final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
//			transaction.add(retainFrag, "retainFrag");
//			transaction.commit();
//		}
//		final Intent it = getIntent();
//		final Bundle extras = it.getExtras();
//		no = extras.getInt("no");
//        mPrefs = Prefs.loadPrefes(this, no);
//
//		if (mPrefs.mDirList.size() == 0) {
//            Toast.makeText(getApplicationContext(), R.string.label_no_target_dir, Toast.LENGTH_LONG).show();
//            startActivity(new Intent(this, AGrepActivity.class));
//            finish();
//        }
//		final GrepView.Data[] parcelableArray;
//        if (savedInstanceState != null 
//		&& retainFrag.mData.size() < (parcelableArray = (GrepView.Data[]) savedInstanceState.getParcelableArray("mData")).length) {
//			retainFrag.mData.clear();
//			Log.d(TAG, "onCreate.parcelableArray " + parcelableArray.length);
//			for (GrepView.Data obj : parcelableArray) {
//				//Log.d(TAG, "onCreate.obj " + obj);
//				retainFrag.mData.add(obj);
//			}
//			//retainFrag.mData.addAll((ArrayList<GrepView.Data>)savedInstanceState.getParcelableArrayList("mData"));
//		}
//		if (retainFrag.mAdapter == null) {
//			retainFrag.mAdapter = new GrepView.GrepAdapter(this, R.layout.list_row, R.id.DicView01, retainFrag.mData);
//		}
//		
//        //Log.d(TAG, "onCreate.mData " + retainFrag.mData);
//
//        mGrepView = (GrepView)findViewById(R.id.DicView01);
//		mGrepView.setAdapter(retainFrag.mAdapter);
//        mGrepView.setCallback(this);
//
//        if (it != null &&
//            Intent.ACTION_SEARCH.equals(it.getAction())) {
//            mQuery = extras.getString(SearchManager.QUERY);
//			Log.d(TAG, "onCreate.mQuery " + mQuery + ", no " + no);
//            if (mQuery != null && mQuery.length() > 0) {
//                mPrefs.addRecent(this, no , mQuery);
//
//                String patternText = mQuery;
//                if (!mPrefs.mRegularExrpression) {
//                    patternText = patternText.replaceAll(Util.SPECIAL_CHAR_PATTERNSTR, "\\\\$1");
//                    patternText = "(" + patternText.replaceAll("\\s+", "|") + ")";
//                }
//
//                if (mPrefs.mIgnoreCase) {
//					mPattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
//                } else {
//                    mPattern = Pattern.compile(patternText);
//                }
//
//				Log.d(TAG, "onCreate.retainFrag.mTask " + retainFrag.mTask + "mounted " + Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
//                if (retainFrag.mTask == null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//                    retainFrag.mData.clear();
//                    retainFrag.mAdapter.setFormat(mPattern , mPrefs.mHighlightFg , mPrefs.mHighlightBg , mPrefs.mFontSize);
//                    retainFrag.mTask = new GrepTask(this);
//                    retainFrag.mTask.execute(mQuery);
//                }
//            } else {
//                finish();
//            }
//        }
//	}
//
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		Log.d(TAG, "onSaveInstanceState " + retainFrag.mData);
//		super.onSaveInstanceState(outState);
//		outState.putParcelableArray("mData", retainFrag.mData.toArray(new GrepView.Data[0]));
//		outState.putString("mQuery", mQuery);
//	}
//
//    class RetainFrag extends Fragment {
//		private static final String TAG = "RetainFrag";
//		
//		GrepTask mTask;
//		ArrayList<GrepView.Data> mData = new ArrayList<GrepView.Data>();
//		GrepView.GrepAdapter mAdapter;
//		boolean hidden = false;
//		
//		@Override
//		public void onCreate(Bundle savedInstanceState) {
//			Log.d(TAG, "onCreate");
//			super.onCreate(savedInstanceState);
//			setRetainInstance(true);
//		}
//
//		@Override
//		public void onPause() {
//			Log.d(TAG, "onPause mTask " + mTask);
//			super.onPause();
//			hidden = true;
//			if (mTask != null) {
//				if (mTask.mProgressDialog != null) {
//					mTask.mProgressDialog.dismiss();
//					Log.d(TAG, "onPause mProgressDialog " + mTask.mProgressDialog);
//					mTask.mProgressDialog = null;
//				}
//			}
//		}
//
//		@Override
//		public void onResume() {
//			Log.d(TAG, "onResume hidden " + hidden + ", adapter " + mGrepView.getAdapter() + ", mData " + mData);
//			super.onResume();
//			if (mTask != null & hidden) {
//				AsyncTask.Status status = mTask.getStatus();
//				Log.d(TAG, "onResume status " + status);
//				if (status == AsyncTask.Status.RUNNING) {
//					mTask.searchActivity = Search.this;
//					mTask.runProgress();
//				} else if (status == AsyncTask.Status.FINISHED) {
//					retainFrag.mAdapter.notifyDataSetChanged();
//					mTask.searchActivity.mGrepView.setSelection(0);
//				}
//			}
//			hidden = false;
//		}
//
//		@Override
//		public void onActivityCreated(Bundle savedInstanceState) {
//			Log.d(TAG, "onActivityCreated.mTask " + mTask);
//			super.onActivityCreated(savedInstanceState);
//			if (mTask != null) {
//				mTask.searchActivity = Search.this;
//			}
//		}
//	}
//
//    class GrepTask extends AsyncTask<String, GrepView.Data, Boolean> {
//        private static final String TAG = "GrepTask";
//		
//		private ProgressDialog mProgressDialog;
//        private int mFileCount=0;
//        private int mFoundcount=0;
//        private boolean mCancelled;
//		private Search searchActivity;
//		
//		public GrepTask(Search searchActivity) {
//			this.searchActivity = searchActivity;
//		}
//
//		@Override
//		public String toString() {
//			return "GrepTask " + super.toString() + ", searchActivity " + searchActivity;
//		}
//
//        @Override
//        protected void onPreExecute() {
//            mCancelled = false;
//            runProgress();
//        }
//
//		private void runProgress() {
//			Log.d(TAG, "runProgress");
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
//		}
//
//        @Override
//        protected Boolean doInBackground(String... params) {
//            return grepRoot(params[0]);
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//			Log.d(TAG, "onPostExecute.retainFrag.mData" + retainFrag.mData + ", hidden " + retainFrag.hidden);
//			if (mProgressDialog != null) {
//				mProgressDialog.dismiss();
//				mProgressDialog = null;
//			}
//			
//			Collections.sort(retainFrag.mData);
//			if (!retainFrag.hidden) {
//				synchronized (retainFrag.mData) {
//					retainFrag.mAdapter.notifyDataSetChanged();
//				}
//				searchActivity.mGrepView.setSelection(0);
//			}
//			Toast.makeText(getApplicationContext(), result ?R.string.grep_finished: R.string.grep_canceled, Toast.LENGTH_LONG).show();
////          if (retainFrag.opUtil != null) {
////				retainFrag.opUtil.releaseOpService();
////			}
//        }
//
//        @Override
//        protected void onCancelled() {
//            super.onCancelled();
//            onPostExecute(false);
//        }
//
//        @Override
//        protected void onProgressUpdate(GrepView.Data... progress) {
//            if (isCancelled()) {
//                return;
//            }
//			if (mProgressDialog != null) {
//				mProgressDialog.setMessage(searchActivity.getString(R.string.progress , mQuery, mFileCount));
//			}
//            if (progress != null) {
//                synchronized (retainFrag.mData) {
//                    for (GrepView.Data data : progress) {
//                        retainFrag.mData.add(data);
//                    }
//                    retainFrag.mAdapter.notifyDataSetChanged();
//                    searchActivity.mGrepView.setSelection(retainFrag.mData.size() - 1);
//                }
//            }
//        }
//
//		private boolean grepRoot(String text) {
//
//			PowerManager.WakeLock wl = null;
//			try {
//				PowerManager pm = (PowerManager)searchActivity.getSystemService(
//					Context.POWER_SERVICE);
//				wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
//				wl.acquire();
//
//				fileList = new TreeSet<>();
//				for (CheckedString dir : searchActivity.mPrefs.mDirList) {
//					//Log.d(TAG, "grepRoot " + text + ", dir " + dir);
//					if (dir.checked && !grepDirectory(new File(dir.string))) {
//						return false;
//					}
//				}
//				for (File f : fileList) {
//					grepFile(f);
//				}
//			} finally {
//				if (wl != null) {
//					wl.release();
//				}
//			}
//            return true;
//        }
//
//
//        boolean grepDirectory(final File dir) {
//			Log.d(TAG, "grepDirectory " + dir);
//            if (isCancelled()) {
//                return false;
//            }
//			boolean ret;
//			if (dir.isFile()) {
//				checkExt(dir);
//			} else {
//				final LinkedList<File> folderQueue = new LinkedList<File>();
//				folderQueue.push(dir);
//				File fi = null;
//				File[] fs;
//				while (folderQueue.size() > 0) {
//					fi = folderQueue.pop();
//					fs = fi.listFiles();
//					if (fs != null) {
//						ret = true;
//						for (File f2 : fs) {
//							if (f2.isDirectory()) {
//								folderQueue.push(f2);
//							} else {
//								ret = checkExt(f2);
//							}
//						}
//						if (!ret) {
//							return false;
//						}
//					}
//				}
//			}
//            return true;
//        }
//
//		private boolean checkExt(final File dir) {
//            if (isCancelled()) {
//                return false;
//            }
//			for (CheckedString ext : searchActivity.mPrefs.mExtList) {
//				if (ext.checked) {
//					if (ext.string.equals("*")) {
//						if (dir.getName().indexOf('.') == -1) {
//							try {
//								return convert(dir);
//							} catch (Exception e) {
//
//							}
//						}
//					} else if (dir.getName().toLowerCase().endsWith("." + ext.string)) {
//						try {
//							return convert(dir);
//						} catch (Exception e) {
//
//						}
//					}
//				}
//			}
//			return true;
//		}
//
//		final Pattern htmPat = Pattern.compile("(txt|htm|html|xhtml)");
//		final Pattern htmlPat = Pattern.compile("(htm|html|xhtml|shtm|shtml)");
//		final Pattern plainPat = Pattern.compile("(ini|mk|md|list|config|configure|js|bat|sh|lua|depend|java|c|cpp|h|hpp|jsp|machine|asm|css|desktop|inc|i|plist|pro|py|s|xpm|php|gradle)");
//		final Pattern zipPat = Pattern.compile("(zip|gz|7z|bz2|jar|tar|rar|arj|lzh|chm|xz|z)");
//		long totalSelectedSize;
//
//		private boolean convert(final File inFile) throws IOException, Exception {
//			Log.d(TAG, "convert " + inFile);
//			final String inFilePath = inFile.getAbsolutePath();
//			final File newFile;
//			if (inFilePath.startsWith(SearcherAplication.PRIVATE_PATH)) {
//				newFile = new File(inFilePath + HtmlUtil.CONVERTED_TXT);
//			} else {
//				newFile = new File(SearcherAplication.PRIVATE_PATH + inFilePath + HtmlUtil.CONVERTED_TXT);
//			}
//			// file text được chọn đã được convert từ trước
//			if (newFile.exists() // && "Search".equals(load)
//				&& (newFile.lastModified() > inFile.lastModified())) {
//				//publishProgress("already converted " + inFilePath);
//				fileList.add(newFile);
//				totalSelectedSize += newFile.length();
//				//Log.d("already converted newFile", String.valueOf(newFile));
//				return true;
//			}
//			String fileContent = "";
//			final String name = inFile.getName();
//			final String inFilePathLowerCase = name.toLowerCase();
//			String ext = FileUtil.getExtensionFromName(inFilePathLowerCase);
//			String mimeTypeFromExtension = FileUtil.getMimeType(inFile);
//			// file duoc chon co duoi .converted
//			if (inFilePathLowerCase.endsWith(HtmlUtil.CONVERTED_TXT)
//				|| (mimeTypeFromExtension.startsWith("text")
//				&& !htmPat.matcher(ext).matches())
//				|| plainPat.matcher(ext).matches()
//				) {
//				//publishProgress("adding converted text " + inFilePath);
//				fileList.add(inFile);
//				// file txt được chọn có thể đã được convert từ trước nhưng đã cũ
//			} else if (ext.equals(".txt")) {
//				//publishProgress("converting file " + inFilePath);
//				fileContent = FileUtil.readFileWithCheckEncode(inFilePath);
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//			} else if (htmlPat.matcher(ext).matches()) {
//				fileContent = HtmlUtil.htmlToText(inFile);
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//			} else if (ext.equals("pdf")) {
//				//publishProgress("converting file " + inFilePath);
//				final String pdfTextPath = SearcherAplication.PRIVATE_PATH + inFilePath + ".txt";
//				final File txtFile = new File(pdfTextPath);
//				//if (retainFrag.opService == null) {
////					retainFrag.opUtil = new OpServiceUtil(searchActivity);
////					retainFrag.opUtil.bindOpService("net.gnu.agrep", "net.gnu.pdfplugin.ITextService");
////					Log.d(TAG, retainFrag.opUtil + ".");
////					retainFrag.opService = retainFrag.opUtil.getOpService();
////				OpServiceConnection opServiceConnection = OpServiceUtil.startOpService(getApplicationContext(), "net.gnu.agrep", "net.gnu.pdfplugin.ITextService");
////				IOperation opService = OpServiceUtil.getOpService(opServiceConnection);
////				Log.d(TAG, opService + ".");
//				ITextUtil.pdfToText(inFilePath, pdfTextPath);
//				//} else {
//				//	retainFrag.opService.pdfToText(inFilePath, pdfTextPath);
//				//}
//				if (txtFile.exists()) {
//					fileContent = HtmlUtil.filterCRPDF(txtFile).replaceAll("ƣ", "ư");
//					txtFile.delete();
//				}
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//				// } else if (inFilePathLowerCase.endsWith(".docx")
//				// || inFilePathLowerCase.endsWith(".xlsx")
//				// || inFilePathLowerCase.endsWith(".pptx")) { 
//				// !.pdf" convert sang text rồi tự đoán font
//				// s.currentContent = Writer.getChangedFont(inFile);
//				// FileInputStream fis = new FileInputStream(inFile);
//				// if (currentFName.endsWith("docx")) {
//				// XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(fis));
//				// s.currentContent = extractor.getText();
//				// } else if (currentFName.endsWith("xlsx")) {
//				// XSSFWorkbook workbook = new XSSFWorkbook(fis);
//				// XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
//				// s.currentContent = extractor.getText();
//				// } else if (currentFName.endsWith("pptx")) {
//				// XMLSlideShow slideShow = new XMLSlideShow(fis);
//				// XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slideShow);
//				// s.currentContent = extractor.getText();
//				//}
//				// fis.close();
//			} else if (ext.equals("rtf")) {
//				Metadata metadata = new Metadata();
//				StringWriter writer = new StringWriter();
//				FileInputStream fis = new FileInputStream(inFile);
//				final org.apache.tika.parser.rtf.TextExtractor ert = new org.apache.tika.parser.rtf.TextExtractor(new XHTMLContentHandler(new WriteOutContentHandler(writer), metadata), metadata);
//				ert.extract(fis);
//				fileContent = HtmlUtil.changeToVUTimes(writer.toString()); // RTF2Txt.rtfToText(inFile)
//			} else if (ext.equals("epub")) {
//				fileContent = HtmlUtil.changeToVUTimes(Epub2Txt.epub2txt(inFile));
//			} else if (ext.equals("fb2")) {
//				fileContent = HtmlUtil.changeToVUTimes(FB2Txt.fb2txt(inFile));
//			} else if (ext.equals("docx")) {
//				fileContent = HtmlUtil.changeToVUTimes(DocxToText.docxToText(inFile));
//			} else if (ext.equals("xlsx")) {
//				fileContent = HtmlUtil.changeToVUTimes(XLSX2Text.getText(inFile));
//			} else if (ext.equals("pptx")) {
//				fileContent = HtmlUtil.changeToVUTimes(PPTX2Text.pptx2Text(inFile));
//			} else if (ext.equals("odt")) {
//				fileContent = HtmlUtil.changeToVUTimes(OdtToText.odtToText(inFile));
//			} else if (ext.equals("ods")) {
//				fileContent = HtmlUtil.changeToVUTimes(ODSToText.odsToText(inFile));
//			} else if (ext.equals("odp")) {
//				fileContent = HtmlUtil.changeToVUTimes(ODPToText.odpToText(inFile));
//			} else if (ext.equals("doc")) { // !.pdf"
//				//publishProgress("converting file " + inFilePath);
//				fileContent = PoiUtils.readWordFileToText(inFilePath);
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//			} else if (ext.equals("pub")) {
//				fileContent = PoiUtils.getPublisherText(inFilePath);
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//			} else if (ext.equals("vsd")) {
//				fileContent = PoiUtils.getVisioText(inFilePath);
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//			} else if (ext.equals("ppt")
//					   || ext.equals("pps")) {
//				fileContent = PoiUtils.getPowerPointText(inFilePath);
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//			} else if (ext.equals("xls")) {
//				fileContent = PoiUtils.getExcelText(inFilePath);
//				fileContent = HtmlUtil.changeToVUTimes(fileContent);
//				//try {
//				//AbstractHtmlExporter exporter = new HtmlExporterNG2();
//				//OutputStream os = new FileOutputStream(currentFName + Util.CONVERTED_TXT);
//				//StreamResult result = new StreamResult(os);
//				//WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inFile);
//				//exporter.html(wordMLPackage, result, new HTMLSettings());
//				//result.getOutputStream().close();
//				//} catch (Exception e) {
//				//e.printStackTrace();
//				//}
//			} else if (zipPat.matcher(ext).matches()) {
//				final String outDirFilePath;
//				if (inFilePath.startsWith(SearcherAplication.PRIVATE_PATH)) {
//					int lastIndexOf = name.lastIndexOf(".");
//					outDirFilePath = inFile.getParent() + "/" + name.substring(0, lastIndexOf) + "_" + name.substring(lastIndexOf + 1);
//				} else {
//					outDirFilePath = SearcherAplication.PRIVATE_PATH + inFile;
//				}
//
//				File outDirFile = new File(outDirFilePath);
//				//publishProgress("processing file " + inFilePath);
//				outDirFile.mkdirs();
//				Log.d(TAG, "outDirFilePath " + outDirFilePath);
//
//				ExtractFile extractFile = new ExtractFile(inFilePath, outDirFilePath);
//				try {
//					String zeName;
//					List<File> extractedList = new LinkedList<File>();
//					Collection<String> entryFileList = new HashSet<String>();
//					while ((zeName = extractFile.getNextEntry()) != null) {
//
//						String zeNameLower = zeName.toLowerCase();
//						File entryFile = new File(outDirFilePath + "/" + zeName);
//						File convertedEntryFile = new File(entryFile.getAbsolutePath() + HtmlUtil.CONVERTED_TXT); // khi chạy đệ quy thì tạo thêm getFilesDir()
//						Log.d("convertedEntryFile", convertedEntryFile + " exist: " + convertedEntryFile.exists());
//
//						ext = FileUtil.getExtension(entryFile);
//						mimeTypeFromExtension = FileUtil.getMimeType(entryFile);
//						//Log.d("mime entryFile", mimeType + " is " + fileExtensionFromUrl + " : " + mimeTypeFromExtension + (mimeTypeFromExtension != null && mimeTypeFromExtension.startsWith(("text"))));
//
//						if (!zeName.endsWith("/")//.isDirectory()
//							&& (convertedEntryFile.exists() 
//							&& convertedEntryFile.lastModified() >= inFile.lastModified())) {
//							//publishProgress("adding converted file: " + convertedEntryFile);
//							Log.d("adding converted file: ", convertedEntryFile + " ");
//							fileList.add(convertedEntryFile);
//						} else if (!zeName.endsWith("/")//.isDirectory()
//								   && (entryFile.exists() 
//								   && entryFile.lastModified() >= inFile.lastModified())) {
//							//publishProgress("adding source file: " + entryFile);
//							Log.d("adding source file: ", entryFile + " ");
//							extractedList.add(entryFile);
//						} else if (!zeName.endsWith("/")//.isDirectory()
//								   && (zeNameLower.matches(SettingsFragment.SEARCH_FILES_SUFFIX)
//								   || (mimeTypeFromExtension.startsWith("text")))) {
//							//publishProgress("extracting " + inFile + "/" + zeName);
//							Log.d("extracting zeName", entryFile.toString());
//							//zis.saveToFile(zeName);
//							entryFileList.add(zeName);
//							extractedList.add(entryFile);
//							Log.d("entryFile", entryFile + " written, size: " + entryFile.length());
//						}               
//					}
//					if (entryFileList.size() > 0) {
//						extractFile.extractEntries(entryFileList, false);
//					}
//					String fname;
//					for (File file : extractedList) {
//						checkExt(file);
//						fname = file.getName().toLowerCase();
//						if (fname.matches(".*?\\.(doc|ppt|xls|docx|odt|pptx|xlsx|odp|ods|epub|fb2|htm|html|rtf|pdf)")) {
//							Log.d("delete", file.getAbsolutePath());
//							//tempFList.add(file.getAbsolutePath());
//							file.delete();
//						}
//					}
//					return true;
//				} catch (Exception e) {
//					Log.d("zip process source file", e.getMessage(), e);
//				} finally {
//					Log.d("GetSourceFileTask", "zis.close()");
//					extractFile.close();
//				}
//			}
//			if (fileContent != null && fileContent.length() > 0) {
//				FileUtil.writeFileAsCharset(newFile, fileContent, HtmlUtil.UTF8);
//				fileList.add(newFile);
//				totalSelectedSize += fileContent.getBytes().length;
//				Log.d("newFile exist", newFile + " just written: " + newFile.exists());
//			} 
//			return true;
//		}
//
//        boolean grepFile(final File file) {
//			Log.d(TAG, "grepFile " + file);
//            if (isCancelled()) {
//                return false;
//            }
//            final InputStream is;
//            try {
//                is = new BufferedInputStream(new FileInputStream(file) , 65536);
//                is.mark(65536);
//
//                //  文字コードの判定
//                String encode = null;
//                try {
//                    final UniversalDetector detector = new UniversalDetector();
//                    try {
//                        int nread;
//                        byte[] buff = new byte[4096];
//                        if ((nread = is.read(buff)) > 0) {
//                            detector.handleData(buff, 0, nread);
//                        }
//                        detector.dataEnd();
//                    } catch ( FileNotFoundException e ) {
//                        e.printStackTrace();
//                        is.close();
//                        return true;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        is.close();
//                        return true;
//                    }
//                    encode = detector.getCharset();
//                    detector.reset();
//                    detector.destroy();
//                } catch ( UniversalDetector.DetectorException e) {
//                }
//                is.reset();
//
//                BufferedReader br=null;
//                try {
//                    if (encode != null) {
//                        br = new BufferedReader(new InputStreamReader(is , encode) , 8192);
//
//                    } else {
//                        br = new BufferedReader(new InputStreamReader(is) , 8192);
//                    }
//
//                    String text;
//                    int line = 0;
//                    boolean found = false;
//                    Pattern pattern = mPattern;
//                    Matcher m = null;
//                    ArrayList<GrepView.Data>    data  = null ;
//                    mFileCount++;
//                    while ((text = br.readLine()) != null) {
//                        line ++;
//                        if (m == null) {
//                            m = pattern.matcher(text);
//                        } else {
//                            m.reset(text);
//                        }
//                        if (m.find()) {
//                            found = true;
//
//                            synchronized (retainFrag.mData) {
//                                mFoundcount++;
//                                if (data == null) {
//                                    data = new ArrayList<GrepView.Data>();
//                                }
//                                data.add(new GrepView.Data(file, line, text));
//
//                                if (mFoundcount < 10) {
//                                    publishProgress(data.toArray(new GrepView.Data[0]));
//                                    data = null;
//                                }
//                            }
//                            if (mCancelled) {
//                                break;
//                            }
//                        }
//                    }
//                    br.close();
//                    is.close();
//                    if (data != null) {
//                        publishProgress(data.toArray(new GrepView.Data[0]));
//                        data = null;
//                    }
//                    if (!found) {
//                        if (mFileCount % 10 == 0) {
//                            publishProgress((GrepView.Data[])null);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//            return true;
//        }
//    }
//
//    public static SpannableString highlightKeyword(CharSequence text, Pattern p, int fgcolor, int bgcolor) {
//        SpannableString ss = new SpannableString(text);
//
//        int start = 0;
//        int end;
//        Matcher m = p.matcher(text);
//        while (m.find(start)) {
//            start = m.start();
//            end = m.end();
//
//            BackgroundColorSpan bgspan = new BackgroundColorSpan(bgcolor);
//            ss.setSpan(bgspan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            ForegroundColorSpan fgspan = new ForegroundColorSpan(fgcolor);
//            ss.setSpan(fgspan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            start = end;
//        }
//        return ss;
//    }
//
//    @Override
//    public void onGrepItemClicked(int position) {
//        GrepView.Data data = (GrepView.Data) mGrepView.getAdapter().getItem(position);
//
//        Intent it = new Intent(this, TextViewer.class);
//
//        it.putExtra(TextViewer.EXTRA_PATH , data.mFile.getAbsolutePath());
//        it.putExtra(TextViewer.EXTRA_QUERY, mQuery);
//        it.putExtra(TextViewer.EXTRA_LINE, data.mLinenumber);
//
//        startActivity(it);
//    }
//
//    @Override
//    public boolean onGrepItemLongClicked(int position) {
//        return false;
//    }
//
//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case android.R.id.home:
//                finish();
//                return true;
//        }
//        return super.onMenuItemSelected(featureId, item);
//    }
//}
