package net.gnu.pdfplugin;

import android.content.*;

import android.os.IBinder;
import android.util.Log;
import net.gnu.aidl.IOperation;

public class OpServiceUtil {

	private static final String TAG = "OpServiceUtil";
	private OpServiceConnection opServiceConnection;
	private Context context;

	public OpServiceUtil(Context context) {
		this.context = context;
	}

	public OpServiceConnection bindOpService(String pkgName, String fullClassName) {
		Log.i(TAG, "bindOpService " + pkgName + ", " + fullClassName);
		opServiceConnection = new OpServiceConnection();
		Intent intent = new Intent();//ActivityMain.ACTION_PICK_PLUGIN);
		intent.setClassName(pkgName, fullClassName);
		context.bindService(intent, opServiceConnection, Context.BIND_AUTO_CREATE);
		Log.i(TAG, "opServiceConnection " + opServiceConnection);
		return opServiceConnection;
	}

	public OpServiceConnection startOpService(String pkgName, String fullClassName) {
		Log.i(TAG, "startOpService " + pkgName + ", " + fullClassName);
		opServiceConnection = new OpServiceConnection();
		Intent intent = new Intent();//ActivityMain.ACTION_PICK_PLUGIN);
		intent.setClassName(pkgName, fullClassName);
		context.startService(intent);
		Log.i(TAG, "opServiceConnection " + opServiceConnection);
		return opServiceConnection;
	}

	public static IOperation getOpService(OpServiceConnection opServiceConnection) {
		if (opServiceConnection.opService == null) {
			for (int i = 0; i < 50 && opServiceConnection.opService == null; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
			return opServiceConnection.opService;
		} else {
			return opServiceConnection.opService;
		}
	}

	public void releaseOpService(OpServiceConnection opServiceConnection) {
		Log.i(TAG, "releaseOpService " + opServiceConnection);
		context.unbindService(opServiceConnection);
		opServiceConnection = null;
	}
}
