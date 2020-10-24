package net.gnu.pdfplugin;

import net.gnu.aidl.IOperation;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.util.Log;

public class OpServiceConnection implements ServiceConnection {

	private static final String TAG = "OpServiceConnection";
	IOperation opService;

	public void onServiceConnected(ComponentName className,
								   IBinder boundService) {
		Log.i(TAG, "onServiceConnected");
		opService = IOperation.Stub.asInterface(boundService);
		Log.i(TAG, opService + ".");
	}

	public void onServiceDisconnected(ComponentName className) {
		Log.i(TAG, "onServiceDisconnected");
		opService = null;
	}
}
