package net.gnu.pdfplugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import net.gnu.aidl.IOperation;
import android.os.*;
import java.io.*;

public class ITextService extends Service {
	static final String LOG_TAG = "ITextService";

	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public IBinder onBind(final Intent intent) {
      	return addBinder;
	}

    private final IOperation.Stub addBinder = new IOperation.Stub() {

//		@Override
//		public String readGenExcelDict(boolean manySheet) throws RemoteException {
//			try {
//				if (dl == null) {
//					dl = new DictionaryLoader(); 
//				}
//				Dictionary dic = dl.readGenExcelDict(manySheet);
//				OutputStream fos = new BufferedOutputStream(new FileOutputStream(Constants.ORI_EXCEL_FILE + ".gen.ser"));
//				ObjectOutputStream oos = new ObjectOutputStream(fos);
//				oos.writeObject(dic);
//				DocFileUtils.flushClose(oos);
//			} catch (IOException e) {
//				throw new RemoteException(e.getMessage());
//			}
//			return Constants.ORI_EXCEL_FILE + ".gen.ser";
//		}
//
//		@Override
//		public String readOriExcelDict() throws RemoteException {
//			try {
//				if (dl == null) {
//					dl = new DictionaryLoader(); 
//				}
//				Dictionary dic = dl.readOriExcelDict();
//				OutputStream fos = new BufferedOutputStream(new FileOutputStream(Constants.ORI_EXCEL_FILE + ".gen.ser"));
//				ObjectOutputStream oos = new ObjectOutputStream(fos);
//				oos.writeObject(dic);
//				DocFileUtils.flushClose(oos);
//			} catch (IOException e) {
//				throw new RemoteException(e.getMessage());
//			}
//			return Constants.ORI_EXCEL_FILE + ".gen.ser";
//		}
//
//		@Override
//		public void writeNewWordsSheet(String dicFileNameSer, String outputFile, String sheetName) throws RemoteException {
//			try {
//				DictionaryLoader.writeNewWordsSheet(dicFileNameSer, outputFile, sheetName);
//			} catch (Exception e) {
//				throw new RemoteException(e.getMessage());
//			}
//		}
//

		@Override
		public void pdfToText(final String fPath, final String fOutPath) throws RemoteException {
			try {
				ITextUtil.pdfToText(fPath, fOutPath);
			} catch (IOException e) {
				throw new RemoteException(e.getMessage());
//			} finally {
//				ITextService.this.stopSelf();
			}
		}

//		@Override
//		public String readWordFileToText(String fPath) throws RemoteException {
//			try {
//				return DocFileUtils.readWordFileToText(fPath);
//			} catch (IOException e) {
//				throw new RemoteException(e.getMessage());
//			}
//		}
//
//		@Override
//		public String getPublisherText(String fPath) throws RemoteException {
//			try {
//				return DocFileUtils.getPublisherText(fPath);
//			} catch (IOException e) {
//				throw new RemoteException(e.getMessage());
//			}
//		}
//
//		@Override
//		public String getVisioText(String fPath) throws RemoteException {
//			try {
//				return DocFileUtils.getVisioText(fPath);
//			} catch (IOException e) {
//				throw new RemoteException(e.getMessage());
//			}
//		}
//
//		@Override
//		public String getPowerPointText(String fPath) throws RemoteException {
//			try {
//				return DocFileUtils.getPowerPointText(fPath);
//			} catch (IOException e) {
//				throw new RemoteException(e.getMessage());
//			}
//		}
//
//		@Override
//		public String getExcelText(String fPath) throws RemoteException {
//			try {
//				return DocFileUtils.getExcelText(fPath);
//			} catch (IOException e) {
//				throw new RemoteException(e.getMessage());
//			}
//		}
    };
}

