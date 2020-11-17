package net.gnu.common;

import java.io.File;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.*;
import android.content.*;
import net.gnu.androidutil.*;
import android.content.res.Resources;
import net.gnu.pdfplugin.ITextUtil;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import net.gnu.agrep.*;

public class Pdf2ImageTask extends AsyncTask<Void, String, String> {

	private String pdfPath;
	private String outDir;
	private Context ctx;
	private String zoom = "100";
	private boolean extractImages;
	
	private int left;
	private int top;
	private int right;
	private int bottom;
	private int background = 0;
	int type;
	int rate = 95;
	
	public Pdf2ImageTask(Context ctx, String pdfPath,
						 String outDir, String zoom, 
						 boolean extractImages,
						 String left, String top, 
						 String right, String bottom,
						 int background, int type, int rate) {
		this.pdfPath = pdfPath;
		this.outDir = outDir;
		this.ctx = ctx;
		this.zoom = zoom;
		this.extractImages = extractImages;
		this.type = type;
		this.rate = rate;

		this.left = Integer.parseInt(left.length()==0?"0":left);
		this.top = Integer.parseInt(top.length()==0?"0":top);
		this.right = Integer.parseInt(right.length()==0?"0":right);
		this.bottom = Integer.parseInt(bottom.length()==0?"0":bottom);
		this.background = background;

	}

	@Override
	protected String doInBackground(Void... p1) {
		try {
			publishProgress("Starting convert " + pdfPath + " into " + outDir + ". Please wait...");
			if (extractImages) {
				ITextUtil.extractPdfImages(pdfPath, outDir);
			}
			final Bitmap.CompressFormat cf = type == R.id.png ? Bitmap.CompressFormat.PNG : type == R.id.jpg ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.WEBP;
			AndroidUtils.pdfToImage(new File(pdfPath), outDir,
									Integer.parseInt(zoom.length() == 0 ? "100" : zoom),
									extractImages, 
									left, top, right, bottom, 
									background,
									cf, rate);
		} catch (Throwable e) {
			Log.e("Pdf2ImageTask", e.getMessage(), e);
			publishProgress(e.getMessage());
		}
		return null;
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
				AndroidUtils.showToast(ctx, progress[0]);
		}
	}

	protected void onPostExecute(String result) {
		publishProgress("Convert " + pdfPath + " into " + outDir + " finished");
	}
}

