package net.gnu.common;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.*;
import java.io.*;
import net.gnu.util.*;
import net.gnu.androidutil.*;
import net.gnu.agrep.RetainFrag;
import net.gnu.agrep.R;

public class TTSTask extends AsyncTask<Object, String, String> {

	private static  final String TAG = "TTSTask";

	private TTSActivity ttsActivity;
	final RetainFrag retainFrag;
	private TreeSet<File> lf;
	final Bundle myBundleAlarm = new Bundle();
	final HashMap<String, String> myHashAlarm = new HashMap<String, String>();

	public TTSTask(final TTSActivity ttsActivity, final RetainFrag retainFrag) {
		this.ttsActivity = ttsActivity;
		this.retainFrag = retainFrag;
	}

	@Override
	protected String doInBackground(Object... p1) {
		lf = retainFrag.fileList;
		PowerManager.WakeLock wl = null;
		String curPath = null;
		int curPartNo = 0;
		int curOffset = 0;
		ForegroundService.ticker = "Text to Speech";
		ForegroundService.title = "Touch to Open";
		ForegroundService.text = "TTS Service";
		ttsActivity.cancel = false;
		try {
			PowerManager pm = (PowerManager)ttsActivity.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			wl.acquire();
			AndroidUtils.startService(ttsActivity, ForegroundService.class, ForegroundService.ACTION_FOREGROUND, TAG);
			publishProgress("Text to speech...");
			
			Log.i(TAG, "isSpeaking " + ttsActivity.tts.isSpeaking());
			int no = 0;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(3);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				ttsActivity.tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
						@Override
						public void onStart(String p1) {
							Log.i(TAG, "tts1 onStart " + ttsActivity.tts.isSpeaking() + ", " + p1);
						}

						@Override
						public void onDone(String p1) {
							Log.i(TAG, "tts1 onDone " + p1);
							ttsActivity.stopService(new Intent(ttsActivity, ForegroundService.class));
						}

						@Override
						public void onError(String p1) {
							Log.i(TAG, "tts1 onError " + p1);
							ttsActivity.stopService(new Intent(ttsActivity, ForegroundService.class));
						}
					});

				myBundleAlarm.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM,
									 AudioManager.STREAM_MUSIC);
				myBundleAlarm.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,
									   (float)ttsActivity.volBar.getProgress()/100);
				AudioAttributes audioAttributes = 
					new AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
					.setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build();

				ttsActivity.tts.setAudioAttributes(audioAttributes);
			} else {
				ttsActivity.tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
						@Override
						public void onUtteranceCompleted(String p1) {
							Log.i(TAG, "tts2 onUtteranceCompleted");
							ttsActivity.stopService(new Intent(ttsActivity, ForegroundService.class));
						}
					});

				myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
								String.valueOf(AudioManager.STREAM_MUSIC));
				myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_VOLUME,
								String.valueOf((float)ttsActivity.volBar.getProgress()/100));
			}
			if (ttsActivity.toWav) {
				for (File ss : lf) {
					if (TTSActivity.cancel) 
						break;
					final String outputFile;
					String pTemp = ss.getName();
					//if (srcPath.startsWith(SearcherAplication.PRIVATE_PATH)) {//}.lastIndexOf("/") > SearcherAplication.PRIVATE_PATH.length()) {
					//	pTemp = srcPath.substring(SearcherAplication.PRIVATE_PATH.length());
					//}
					//Log.i(TAG, "srcPath " + srcPath + ", pTemp " + pTemp);
					File file = new File(ttsActivity.saveTo);
					if (file.exists() && file.isDirectory()) {
						outputFile = ttsActivity.saveTo + "/" + pTemp;
					} else {
						outputFile = SearcherAplication.PRIVATE_PATH + "/" + pTemp;
					}
					File oF = new File(outputFile);
					if (!oF.getParentFile().exists()) {
						oF.getParentFile().mkdirs();
					}
					Log.i(TAG, "outputFile " + outputFile);

					RandomAccessFile f = new RandomAccessFile(ss, "r");
					publishProgress(ss.getAbsolutePath() + ": " + Util.nf.format(ss.length()) + " bytes");
					if (f.length() > 4002) {
						List<ComparableEntry<Integer, Integer>> l = split(f, 3999);
						for (ComparableEntry<Integer, Integer> stPart : l) {
							if (ttsActivity.cancel) 
								break;
							byte[] bytes = new byte[stPart.getValue().intValue()];
							f.seek(stPart.getKey());
							f.readFully(bytes);
							String substring = new String(bytes);
							//Log.i("splitStringToArray" + stPart.getKey() + ", " + stPart.getValue(), substring);
							++no;
							if (!ttsActivity.cancel) {
								FileUtil.writeFileAsCharset(outputFile + "-" + nf.format(no) + ".txt", substring, "utf-8");
							}
							String[] replaces = ttsActivity.replaceET.getText().toString().split("\r?\n");
							String[] bys = ttsActivity.byET.getText().toString().split("\r?\n");
							substring = Util.replace(substring, replaces, bys, ttsActivity.isRegex, ttsActivity.caseSensitive);
							if (ss.getFreeSpace() > 100000000) {
								if (ttsActivity.toWav && !ttsActivity.cancel) {
									final String fileName = outputFile + "-" + nf.format(no) + ".wav";
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
										ttsActivity.tts.synthesizeToFile(substring, myBundleAlarm, new File(fileName), fileName);
									} else {
										ttsActivity.tts.synthesizeToFile(substring, myHashAlarm, fileName);
									}
								}
							} else {
								publishProgress("no more space");
								return "no more space";
							}
						}
					} else {
						byte[] readFileToMemory = FileUtil.readFileToMemory(ss);
						String substring = new String(readFileToMemory, 3, readFileToMemory.length - 3, "utf-8");
						Log.i(TAG, "tts toSpeak " + substring.length() + ", " + substring.getBytes().length);

						if (!ttsActivity.cancel) {
							FileUtil.writeFileAsCharset(outputFile, substring, "utf-8");
						}
						String[] replaces = ttsActivity.replaceET.getText().toString().split("\r?\n");
						String[] bys = ttsActivity.byET.getText().toString().split("\r?\n");
						substring = Util.replace(substring, replaces, bys, ttsActivity.isRegex, ttsActivity.caseSensitive);
						if (ttsActivity.toWav && !ttsActivity.cancel) {
							final String fileName = outputFile + ".wav";
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
								ttsActivity.tts.synthesizeToFile(substring, myBundleAlarm, new File(fileName), fileName);
							} else {
								ttsActivity.tts.synthesizeToFile(substring, myHashAlarm, fileName);
							}
						}
					}
					f.close();
				}
			}
			if (ttsActivity.speak) {
				for (File ss : lf) {
					if (ttsActivity.cancel) 
						break;
					curOffset = 0;
					curPath = ss.getAbsolutePath();
					if (ttsActivity.curFile != null) {
						if (!curPath.equals(ttsActivity.curFile)) {
							continue;
						} else {
							ttsActivity.curFile = null;
						}
					} else {
						ttsActivity.offset = 0;
					}
					Log.d(TAG, "curOffset " + curOffset + ", ttsActivity.offset " + ttsActivity.offset + ", curPath " + curPath);
					RandomAccessFile f = new RandomAccessFile(ss, "r");
					publishProgress(ss.getAbsolutePath() + ": " + Util.nf.format(ss.length()) + " bytes");
					Log.i(TAG, "RandomAccessFile " + f.length() + ", " + ss);
					if (f.length() > 4002) {
						List<ComparableEntry<Integer, Integer>> l = split(f, 3999);
						for (ComparableEntry<Integer, Integer> stPart : l) {
							if (ttsActivity.cancel) 
								break;
							byte[] bytes = new byte[stPart.getValue().intValue()];
							f.seek(stPart.getKey());
							f.readFully(bytes);
							String substring = new String(bytes);
							//Log.i("splitStringToArray" + stPart.getKey() + ", " + stPart.getValue(), substring);

							String[] replaces = ttsActivity.replaceET.getText().toString().split("\r?\n");
							String[] bys = ttsActivity.byET.getText().toString().split("\r?\n");
							substring = Util.replace(substring, replaces, bys, ttsActivity.isRegex, ttsActivity.caseSensitive);
							if (ttsActivity.offset > curOffset) {
								substring = substring.substring(ttsActivity.offset);
								curOffset = ttsActivity.offset;
							}
							speak(ttsActivity, substring, myBundleAlarm, myHashAlarm, curPath + "-" + nf.format(no) + ".wav", Integer.valueOf(ttsActivity.dot), Integer.valueOf(ttsActivity.para), curOffset);
							curOffset += substring.length();
						}

					} else {
						byte[] readFileToMemory = FileUtil.readFileToMemory(ss);
						Log.i(TAG, "tts2 readFileToMemory " + ss.getAbsolutePath() + ", len " + ss.length());
						String substring = new String(readFileToMemory, 3, readFileToMemory.length - 3, "utf-8");
						Log.i(TAG, "tts2 toSpeak " + substring.length() + ", " + substring.getBytes().length);
						String[] replaces = ttsActivity.replaceET.getText().toString().split("\r?\n");
						String[] bys = ttsActivity.byET.getText().toString().split("\r?\n");
						substring = Util.replace(substring, replaces, bys, ttsActivity.isRegex, ttsActivity.caseSensitive);
						if (ttsActivity.offset > curOffset) {
							substring = substring.substring(ttsActivity.offset);
							curOffset = ttsActivity.offset;
						}
						Log.i(TAG, "tts3 toSpeak " + substring + ", " + substring.length() + ", " + substring.getBytes().length);
						speak(ttsActivity, substring, myBundleAlarm, myHashAlarm, curPath + ".wav", Integer.valueOf(ttsActivity.dot), Integer.valueOf(ttsActivity.para), curOffset);
						curOffset += substring.length();
					}
					f.close();
				}
			}
			if (!ttsActivity.cancel) {
				curPartNo = -1;
				curPath = null;
				ttsActivity.offset = -1;
			}
		} catch (Throwable e) {
			publishProgress(e.getMessage());
			Log.e(TAG, e.getMessage(), e);
		} finally {
			try {
				if (ttsActivity.tts != null && !ttsActivity.tts.isSpeaking()) {
					if (wl != null) {
						wl.release();
					}
					ttsActivity.stopService(new Intent(ttsActivity, ForegroundService.class));
				}
			} catch (Throwable t) {
				Log.e(TAG, t.getMessage(), t);
			}
			
			Log.i(TAG, "in ttstask2 final " + curPath);
			if ("pause".equals(ttsActivity.command)) {
				ttsActivity.partNo = curPartNo;
				ttsActivity.curFile = curPath;
				ttsActivity.command = "";
			}
		}
		return "";
	}

	private final Pattern pat = Pattern.compile(".+?[\\.!?,;:\n]+");
	public void speak(final TTSActivity ttsFrag, final String substring, final Bundle myBundleAlarm, final HashMap<String, String> myHashAlarm, final String utteranceId, final int dotPause, final int paraPause,
					  int curOffset) {
		Log.i(TAG, "substring offset2 " + curOffset + " " + substring);
		final Matcher mat = pat.matcher(substring);
		int end = 0;
		while (mat.find() && !ttsFrag.cancel) {
			end = mat.end();
			String group = mat.group();
			ttsFrag.offset = curOffset + mat.start();
			Log.i(TAG, "while offset2 " + ttsFrag.offset + ", " + mat.start());
		 	ttsFrag.ttsTask.publishProgress(group);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				ttsFrag.tts.speak(group, TextToSpeech.QUEUE_ADD, myHashAlarm);
			} else {
				ttsFrag.tts.speak(group, TextToSpeech.QUEUE_ADD, myBundleAlarm, utteranceId);
			}

			try {
				while (!ttsFrag.cancel && ttsFrag.tts != null && ttsFrag.tts.isSpeaking()) {
					Thread.sleep(100);
				}
				if (!ttsFrag.cancel) {
					if (group.endsWith("\n")) {
						Thread.sleep(paraPause);
					} else if (group.endsWith(",") || group.endsWith(";") || group.endsWith(":")) {
						Thread.sleep(Integer.parseInt(ttsActivity.comma));
					} else {
						Thread.sleep(dotPause);
					}
				}
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		if (!ttsFrag.cancel
			&& end < substring.length()) {
			String substring2 = substring.substring(end);
			ttsFrag.offset = curOffset + end;
			Log.i(TAG, "no while line offset2 " + ttsFrag.offset + ", substring2 " + substring2);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				ttsFrag.tts.speak(substring2, TextToSpeech.QUEUE_ADD, myHashAlarm);
			} else {
				ttsFrag.tts.speak(substring2, TextToSpeech.QUEUE_ADD, myBundleAlarm, utteranceId);
			}
		}
		Log.e(TAG, "offset end " + ttsFrag.offset);
		if (!ttsFrag.cancel) {
			ttsFrag.offset = curOffset + substring.length();
		}
		Log.e(TAG, "offset end2 " + ttsFrag.offset);
	}

	public static List<ComparableEntry<Integer, Integer>> split(byte[] f, int sizeOfPart) {
		int length = f.length;
		int written = 0;
		List<ComparableEntry<Integer, Integer>> split = new ArrayList<ComparableEntry<Integer, Integer>>();

		while (length - written > sizeOfPart) {
			written = writeClose(f, written, sizeOfPart, split);
		}
		writeClose(f, written, length - written, split);
		return split;
	}

	private static int writeClose(byte[] f, int start, int size, List<ComparableEntry<Integer, Integer>> split) {
		int len = size - 1;
		byte read = 0;
		if (start + size < f.length) {
			while (len > 0) {
				read = f[start + len];
				if (read == '\n' || read == '.' || read == '?' || read == '!') {
					break;
				} else {
					len--;
				}
			}
		} else {
			split.add(new ComparableEntry<Integer, Integer>(start, size));
			return start + size;
		}
		if (len > 0) {
			split.add(new ComparableEntry<Integer, Integer>(start, len + 1));
			return start + len + 1;
		} else {
			split.add(new ComparableEntry<Integer, Integer>(start, size));
			return start + size;
		}
	}

	public static List<ComparableEntry<Integer, Integer>> split(File f, int sizeOfPart) throws IOException {
		return split(new RandomAccessFile(f, "r"), sizeOfPart);
	}

	public static List<ComparableEntry<Integer, Integer>> split(RandomAccessFile f, int sizeOfPart) throws IOException {
		long length = f.length();
		long written = 0;
		List<ComparableEntry<Integer, Integer>> split = new ArrayList<ComparableEntry<Integer, Integer>>();

		while (length - written > sizeOfPart) {
			written = writeClose(f, written, sizeOfPart, split);
		}
		writeClose(f, written, length - written, split);
		return split;
	}

	private static long writeClose(RandomAccessFile f, long start, long size, List<ComparableEntry<Integer, Integer>> split) throws IOException {
		long len = size - 1;
		int read = 0;
		if (start + size < f.length()) {
			while (len > 0) {
				f.seek(start + len);
				read = f.read();
				if (read == '\n' || read == '.' || read == '?' || read == '!') {
					break;
				} else {
					len--;
				}
			}
		} else {
			f.seek(start);
			split.add(new ComparableEntry<Integer, Integer>((int)start, (int)size));
			f.seek(start + size);
			return start + size;
		}
		if (len > 0) {
			f.seek(start);
			split.add(new ComparableEntry<Integer, Integer>((int)start, (int)len + 1));
			f.seek(start + len + 1);
			return start + len + 1;
		} else {
			f.seek(start);
			split.add(new ComparableEntry<Integer, Integer>((int)start, (int)size));
			f.seek(start + size);
			return start + size;
		}

	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].length() > 0) {
			Log.i(TAG, progress[0]);
			ttsActivity.statusTV.setText(progress[0]);
//			if (ttsFrag.tts.isSpeaking()) {
//				ttsFrag.startBtn.setText("Pause");
//			} else {
//				ttsFrag.startBtn.setText("Start");
//			}
		}
	}

	@Override
	protected void onPostExecute(String d) {
		if (ttsActivity.curFile == null) {
			ttsActivity.startBtn.setImageResource(R.drawable.exo_controls_play);
			ttsActivity.startBtn.setTag("Play");
		}

	}
}
