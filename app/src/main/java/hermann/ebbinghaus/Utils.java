package hermann.ebbinghaus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class Utils {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final String TORCHFOLDER = Environment.getExternalStorageDirectory() + "/HermannEbbinghaus";
	public static final String TORCHIMGFOLDER = Environment.getExternalStorageDirectory() + "/HermannEbbinghaus/images";
	public static final String TORCH = TORCHFOLDER + "/torch";
	public static final String NETEASEFOLDER = Environment.getExternalStorageDirectory() + "/netease/cloudmusic/Music";
	public static final String TORCHPEAK = TORCHFOLDER + "/torch_peak";

	private static final String CREAT_CMD = "CREATE TABLE IF NOT EXISTS torch (_id INTEGER PRIMARY KEY AUTOINCREMENT, create_time VARCHAR UNIQUE, create_date INTEGER, mem_date INTEGER, mem_text TEXT, mem_status INTEGER, mem_desc VARCHAR)";

	public static String getFileNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	public static String getFileNameEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(dot + 1, filename.length());
			}
		}
		return filename;
	}

	public static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	/**
	 * 获取文件夹中文件的MD5值
	 *
	 * @param file
	 * @param listChild
	 *            ;true递归子目录中的文件
	 * @return
	 */
	public static Map<String, String> getDirMD5(File file, boolean listChild) {
		if (!file.isDirectory()) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String md5;
		File files[] = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory() && listChild) {
				map.putAll(getDirMD5(f, listChild));
			} else {
				md5 = getFileMD5(f);
				if (md5 != null) {
					map.put(f.getPath(), md5);
				}
			}
		}
		return map;
	}

	/**
	 * 生成随机字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(0);
		} catch (Exception e) {
		}
		return c;
	}

	public static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	private static File getOutputMediaFile(int type) {
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	public static Point getBestCameraResolution(Camera.Parameters parameters, int screenWidth) {
		float x_d_y = (float) 4 / (float) 3;
		Size best = null;
		List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
		for (Size s : supportedPreviewSizes) {
			if (x_d_y == ((float) s.width / (float) s.height)) {
				if (best == null) {
					best = s;
				} else if (s.width > best.width) {
					best = s;
				}
			}
		}
		if (best == null) {
			x_d_y = (float) 16 / (float) 9;
			for (Size s : supportedPreviewSizes) {
				if (x_d_y == ((float) s.width / (float) s.height)) {
					if (best == null) {
						best = s;
					} else if (s.width > best.width) {
						best = s;
					}
				}
			}
		}
		return new Point(best.width, best.height);
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static ArrayList<BaseData.TorchData> selectTorchSqlite(Context context, String key, String op, String select_date) {
		File file = new File(TORCHFOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		File fileImg = new File(TORCHIMGFOLDER);
		if (!fileImg.exists()) {
			fileImg.mkdirs();
		}
		ArrayList<BaseData.TorchData> memoryDatas = new ArrayList<BaseData.TorchData>();
		SQLiteDatabase db = context.openOrCreateDatabase(TORCH, Context.MODE_MULTI_PROCESS, null);
		db.execSQL(CREAT_CMD);
		Cursor cur = db.rawQuery("SELECT * FROM torch WHERE " + key + op + "?", new String[] { select_date });
		while (cur.moveToNext()) {
			String create_time = cur.getString(cur.getColumnIndex("create_time"));
			int create_date = cur.getInt(cur.getColumnIndex("create_date"));
			int mem_date = cur.getInt(cur.getColumnIndex("mem_date"));
			String mem_text = cur.getString(cur.getColumnIndex("mem_text"));
			Integer mem_status = cur.getInt(cur.getColumnIndex("mem_status"));
			String mem_desc = cur.getString(cur.getColumnIndex("mem_desc"));
			memoryDatas.add(new BaseData.TorchData(create_time, create_date, mem_date, mem_text, mem_status, mem_desc));
		}
		cur.close();
		db.close();
		return memoryDatas;
	}


	public static BaseData.TorchData selectOneTorchSqlite(Context context, String create_time) {
		File file = new File(TORCHFOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		File fileImg = new File(TORCHIMGFOLDER);
		if (!fileImg.exists()) {
			fileImg.mkdirs();
		}
		SQLiteDatabase db = context.openOrCreateDatabase(TORCH, Context.MODE_MULTI_PROCESS, null);
		db.execSQL(CREAT_CMD);
		Cursor cur = db.rawQuery("SELECT * FROM torch WHERE create_time == ?", new String[] { create_time });
		while (cur.moveToNext()) {
			String ct = cur.getString(cur.getColumnIndex("create_time"));
			int create_date = cur.getInt(cur.getColumnIndex("create_date"));
			int mem_date = cur.getInt(cur.getColumnIndex("mem_date"));
			String mem_text = cur.getString(cur.getColumnIndex("mem_text"));
			Integer mem_status = cur.getInt(cur.getColumnIndex("mem_status"));
			String mem_desc = cur.getString(cur.getColumnIndex("mem_desc"));
			return new BaseData.TorchData(ct, create_date, mem_date, mem_text, mem_status, mem_desc);
		}
		cur.close();
		db.close();
		return null;
	}

	public static void insertTorchSqlite(Context context, BaseData.TorchData torchData) {
		SQLiteDatabase db = context.openOrCreateDatabase(TORCH, Context.MODE_MULTI_PROCESS, null);
		db.execSQL(CREAT_CMD);
		ContentValues cv = new ContentValues();
		cv.put("create_time", torchData.create_time);
		cv.put("create_date", torchData.create_date);
		cv.put("mem_date", torchData.mem_date);
		cv.put("mem_text", torchData.mem_text);
		cv.put("mem_status", torchData.mem_status);
		cv.put("mem_desc", torchData.mem_desc);
		db.insert("torch", null, cv);
		db.close();
	}

	public static void updateTorchSqlite(Context context, BaseData.TorchData torchData) {
//		Log.e("@@@@@", torchData.mem_text);
//		Log.e("@@@@@", torchData.mem_desc);
//		Log.e("@@@@@", torchData.mem_date + "");
		SQLiteDatabase db = context.openOrCreateDatabase(TORCH, Context.MODE_MULTI_PROCESS, null);
		db.execSQL(CREAT_CMD);
		ContentValues cv = new ContentValues();
		cv.put("mem_text", torchData.mem_text);
		cv.put("mem_desc", torchData.mem_desc);
		cv.put("mem_date", torchData.mem_date);
		cv.put("mem_status", torchData.mem_status);
		db.update("torch", cv, "create_time = ?", new String[] {torchData.create_time});
		db.close();
	}

	public static void deleteTorchSqlite(Context context, String create_time) {
		SQLiteDatabase db = context.openOrCreateDatabase(TORCH, Context.MODE_MULTI_PROCESS, null);
		db.execSQL(CREAT_CMD);
		db.delete("torch", "create_time = ?", new String[] { create_time });
		db.close();
	}

	public static void clearTorchSqlite(Context context, String create_date) {
		SQLiteDatabase db = context.openOrCreateDatabase(TORCH, Context.MODE_MULTI_PROCESS, null);
		db.execSQL(CREAT_CMD);
		db.delete("torch", "create_date = ?", new String[] { create_date });
		db.close();
	}

	public static BaseData.TorchPeakData selectMusicSqlite(Context context, String filename) {
		SQLiteDatabase db = context.openOrCreateDatabase(TORCHPEAK, Context.MODE_MULTI_PROCESS, null);
		Cursor cur = db.rawQuery("SELECT * FROM torch_encode WHERE filename == ?", new String[] { filename });
		while (cur.moveToNext()) {
			String fn = cur.getString(cur.getColumnIndex("filename"));
			float c0 = cur.getFloat(cur.getColumnIndex("encode_0"));
			float c1 = cur.getFloat(cur.getColumnIndex("encode_1"));
			String rn = new String(Base64.decode(fn, Base64.NO_WRAP));
			return new BaseData.TorchPeakData(fn, rn, c0, c1);
		}
		cur.close();
		db.close();
		return null;
	}

	public static boolean copyFile(String oldPath$Name, String newPath$Name) {
		try {
			File oldFile = new File(oldPath$Name);
			if (!oldFile.exists()) {
				Log.e("--Method--", "copyFile:  oldFile not exist.");
				return false;
			} else if (!oldFile.isFile()) {
				Log.e("--Method--", "copyFile:  oldFile not file.");
				return false;
			} else if (!oldFile.canRead()) {
				Log.e("--Method--", "copyFile:  oldFile cannot read.");
				return false;
			}

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */

			FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
			FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
			byte[] buffer = new byte[1024];
			int byteRead;
			while (-1 != (byteRead = fileInputStream.read(buffer))) {
				fileOutputStream.write(buffer, 0, byteRead);
			}
			fileInputStream.close();
			fileOutputStream.flush();
			fileOutputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String han2zen(String filename) {
		filename = filename.replace("が", "が")
				.replace("ぎ", "ぎ")
				.replace("ぐ", "ぐ")
				.replace("げ", "げ")
				.replace("ご", "ご")
				.replace("ざ", "ざ")
				.replace("じ", "じ")
				.replace("ず", "ず")
				.replace("ぜ", "ぜ")
				.replace("ぞ", "ぞ")
				.replace("だ", "だ")
				.replace("ぢ", "ぢ")
				.replace("づ", "づ")
				.replace("で", "で")
				.replace("ど", "ど")
				.replace("ば", "ば")
				.replace("び", "び")
				.replace("ぶ", "ぶ")
				.replace("べ", "べ")
				.replace("ぼ", "ぼ")
				.replace("ガ", "ガ")
				.replace("ギ", "ギ")
				.replace("グ", "グ")
				.replace("ゲ", "ゲ")
				.replace("ゴ", "ゴ")
				.replace("ザ", "ザ")
				.replace("ジ", "ジ")
				.replace("ズ", "ズ")
				.replace("ゼ", "ゼ")
				.replace("ゾ", "ゾ")
				.replace("ダ", "ダ")
				.replace("ヂ", "ヂ")
				.replace("ヅ", "ヅ")
				.replace("デ", "デ")
				.replace("ド", "ド")
				.replace("バ", "バ")
				.replace("ビ", "ビ")
				.replace("ブ", "ブ")
				.replace("ベ", "ベ")
				.replace("ボ", "ボ")
				.replace("ぱ", "ぱ")
				.replace("ぴ", "ぴ")
				.replace("ぷ", "ぷ")
				.replace("ぺ", "ぺ")
				.replace("ぽ", "ぽ")
				.replace("パ", "パ")
				.replace("ピ", "ピ")
				.replace("プ", "プ")
				.replace("ペ", "ペ")
				.replace("ポ", "ポ");
		return filename;
	}

}