package hermann.ebbinghaus;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.scwen.editor.RichEditer;
import com.scwen.editor.util.Util;
import com.scwen.editor.weight.ImageActionListener;
import com.scwen.editor.weight.ImageWeight;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddActivity extends AppCompatActivity implements TakePhoto.TakeResultListener, InvokeListener {

	private static final String TAG = AddActivity.class.getName();
	private TakePhoto takePhoto;
	private InvokeParam invokeParam;

	private RichEditer editText;
	private String status = "0";
	private BaseData.TorchData todayData = null;

	private Button saveButton;
	private Button imageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getTakePhoto().onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		status = getIntent().getStringExtra("editStatus");
		Log.e("@@@@@", status + "");

		editText = (RichEditer) findViewById(R.id.text);
		saveButton = (Button) findViewById(R.id.save_button);
		imageButton = (Button) findViewById(R.id.image_button);
		editText.setImageActionListener(new ImageActionListener() {
            @Override
            public void onAction(int action, ImageWeight imageWeight) {
                switch (action) {
                    case ImageActionListener.ACT_PREVIEW:
						Pair<Integer, List<String>> paths = editText.getIndexAndPaths(imageWeight);
						preImage(paths);
                        break;
                }
            }
        });
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				saveButtonAction();
			}
		});
		imageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				imageButtonAction();
			}
		});
	}

	/**
	 * 预览 编辑器中的图片
	 *
	 * @param paths
	 */
	private void preImage(Pair<Integer, List<String>> paths) {
		List<String> path = paths.second;
		List<LocalMedia> mediaList = new ArrayList<>();
		for (String imagePath : path) {
			LocalMedia media = new LocalMedia();
			media.setPath(imagePath);
			mediaList.add(media);
		}

		PictureSelector.create(this).themeStyle(R.style.picture_default_style).openExternalPreview(paths.first, mediaList);

	}

	private BaseData.TorchData setTextContent(String create_time) {
		BaseData.TorchData td = Utils.selectOneTorchSqlite(getApplicationContext(), create_time);
		editText.parseHtml(td.mem_text);
		return td;
	}

	@Override
	public void takeSuccess(TResult result) {
		String oldName = result.getImage().getOriginalPath();
		String newName = Utils.TORCHIMGFOLDER + "/" + Utils.getFileMD5(new File(oldName)) + "." + oldName.substring(oldName.lastIndexOf(".") + 1);
		Log.e("@@@@@","takeSuccess：" + oldName + " >> " + newName);
		Utils.copyFile(oldName, newName);
		editText.insertImage(newName);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_add, menu);
		String today = getIntent().getStringExtra("editToday");
		String status_date = new SimpleDateFormat("yyyyMMdd").format(new Date(Long.valueOf(status)));
		if (status.equals("0")) {
			menu.getItem(0).setVisible(false);
		} else {
			todayData = setTextContent(status);
			if (status_date.equals(today)) {
				menu.getItem(0).setVisible(false);
			}
		}
		return true;
	}

	private void saveButtonAction() {
		if (status.equals("0")) {
			long ts = System.currentTimeMillis();
			String cd = new SimpleDateFormat("yyyyMMdd").format(new Date(ts));
			String md = new SimpleDateFormat("yyyyMMdd").format(new Date(ts + 86400000));
			String desc = "";
			if (editText.getContent().length() <= 12) {
				desc = editText.getContent();
			} else {
				desc = editText.getContent().substring(0, 12);
			}
			BaseData.TorchData sqlData = new BaseData.TorchData(
					String.valueOf(ts),
					Integer.valueOf(cd),
					Integer.valueOf(md),
					editText.toHtml(),
					0,
					desc);
			Utils.insertTorchSqlite(getApplicationContext(), sqlData);
		} else {
			todayData.mem_text = editText.toHtml();
			String desc = "";
			if (editText.getContent().length() <= 12) {
				desc = editText.getContent();
			} else {
				desc = editText.getContent().substring(0, 12);
			}
			todayData.mem_desc = desc;
			Utils.updateTorchSqlite(getApplicationContext(), todayData);
		}
	}

	private void imageButtonAction() {
		getTakePhoto().onPickFromGallery();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
//		if (id == R.id.action_save) {
//			saveButtonAction();
//			return true;
//		}
//		if (id == R.id.action_image) {
//			return true;
//		}
		if (id == R.id.action_mem) {
			long ts = System.currentTimeMillis();
			String md = new SimpleDateFormat("yyyyMMdd").format(new Date(ts + (86400000 * 2)));
			todayData.mem_date = Integer.valueOf(md);
			String td = new SimpleDateFormat("yyyyMMdd").format(new Date(ts));
			todayData.mem_status = Integer.valueOf(td);
			Utils.updateTorchSqlite(getApplicationContext(), todayData);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*****************************/

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		getTakePhoto().onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		getTakePhoto().onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		PermissionManager.TPermissionType type=PermissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
		PermissionManager.handlePermissionsResult(this, type, invokeParam,this);
	}

	/**
	 *  获取TakePhoto实例
	 * @return
	 */
	public TakePhoto getTakePhoto(){
		if (takePhoto==null){
			takePhoto= (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this,this));
		}
		return takePhoto;
	}
	@Override
	public void takeFail(TResult result,String msg) {
		Log.i(TAG, "takeFail:" + msg);
	}
	@Override
	public void takeCancel() {
		Log.i(TAG, getResources().getString(R.string.msg_operation_canceled));
	}

	@Override
	public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
		PermissionManager.TPermissionType type=PermissionManager.checkPermission(TContextWrap.of(this),invokeParam.getMethod());
		if(PermissionManager.TPermissionType.WAIT.equals(type)){
			this.invokeParam=invokeParam;
		}
		return type;
	}

}