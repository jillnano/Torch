package hermann.ebbinghaus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

	private String titleDate;
	private ListView memoryListView;
	private List<Map<String, String>> memoryData = new ArrayList<Map<String, String>>();
	private boolean isView = false;
	private String currentDate = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCompat.requestPermissions(this, new String[]{android
				.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		ActivityCompat.requestPermissions(this, new String[]{android
				.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

		setContentView(R.layout.activity_main);
		memoryListView = (ListView) findViewById(R.id.memory_list);

		memoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Map<String, String> tt = memoryData.get(i);
				String ct = tt.get("create_time");
				Log.e("@@@@@", ct);
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), AddActivity.class);
				intent.putExtra("editStatus", String.valueOf(ct));
				intent.putExtra("editToday", titleDate);
				startActivity(intent);
			}
		});
		memoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				Map<String, String> tt = memoryData.get(i);
				final String tt_ct = tt.get("create_time");
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("WARNING");
				builder.setMessage("削除：" + tt.get("mem_desc"));
				builder.setPositiveButton("はい", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Utils.deleteTorchSqlite(getApplicationContext(), tt_ct);
						refresh(currentDate);
					}
				});
				builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
					}
				});
				builder.show();
				return true;
			}
		});
		refresh(null);
//		memoryListView.setOnItemLongClickListener(rgbListViewOnItemLongClickListener);
	}

	@Override
	protected void onResume(){
		super.onResume();
		if (!isView) {
			refresh(currentDate);
		}
		isView = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		String dateStr = getIntent().getStringExtra("dateStr");
		if (dateStr != null) {
			refresh(dateStr);
		} else {
			refresh(null);
		}
		isView = true;
	}

	private void refresh(String dateStr) {
		if (dateStr == null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date(System.currentTimeMillis());
			dateStr = simpleDateFormat.format(date);
		}
		currentDate = dateStr;
		this.setTitle("今は" + dateStr);
		titleDate = dateStr;
		getmemoryData(dateStr);
		SimpleAdapter adapter = new SimpleAdapter(this, memoryData, R.layout.list_items_memory,
				new String[]{"mem_desc"}, new int[]{R.id.memory_item});
		adapter.setViewBinder(new ListViewBinder());
		memoryListView.setAdapter(adapter);
	}

	private void getmemoryData(String dateStr) {
		memoryData.clear();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date(System.currentTimeMillis());
		String currentDate = simpleDateFormat.format(date);
		if (dateStr.equals(currentDate)) {
			ArrayList<TorchData> torchListToday = Utils.selectTorchSqlite(getApplicationContext(), "mem_date", " <= ",  dateStr);
			for (TorchData pp : torchListToday) {
				Map<String, String> map = new HashMap<String, String>();
				Log.e("@@@@@", pp.mem_desc);
				map.put("create_time", pp.create_time);
				map.put("mem_desc", pp.mem_desc);
				memoryData.add(map);
			}
			torchListToday.clear();
			torchListToday = Utils.selectTorchSqlite(getApplicationContext(), "mem_status", " == ", dateStr);
			for (TorchData pp : torchListToday) {
				Map<String, String> map = new HashMap<String, String>();
				Log.e("@@@@@", pp.mem_desc);
				map.put("create_time", pp.create_time);
				map.put("mem_desc", "☭ | " + pp.mem_desc);
				memoryData.add(map);
			}
		}
		ArrayList<TorchData> torchList = Utils.selectTorchSqlite(getApplicationContext(), "create_date", " == ", dateStr);
		for (TorchData pp : torchList) {
			Map<String, String> map = new HashMap<String, String>();
			Log.e("@@@@@", pp.mem_desc);
			map.put("create_time", pp.create_time);
			map.put("mem_desc", "\uD83D\uDD28 | " + pp.mem_desc);
			memoryData.add(map);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), AddActivity.class);
            intent.putExtra("editStatus", "0");
			intent.putExtra("editToday", "0");
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_view) {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), ViewActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_today) {
			refresh(null);
			return true;
		}
		if (id == R.id.action_clear) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("WARNING");
			builder.setMessage("クリア：" + currentDate);
			builder.setPositiveButton("はい", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Utils.clearTorchSqlite(getApplicationContext(), currentDate);
					refresh(currentDate);
				}
			});
			builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
				}
			});
			builder.show();
			return true;
		}
		if (id == R.id.action_test) {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), TestActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
