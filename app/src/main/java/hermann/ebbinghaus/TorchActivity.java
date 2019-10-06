package hermann.ebbinghaus;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static hermann.ebbinghaus.Utils.formatFilename;
import static hermann.ebbinghaus.Utils.selectMusicSqlite;

public class TorchActivity extends AppCompatActivity {

    private String TAG = "@@@@@TorchActivity";

    private ArrayList<BaseData.TorchPeakData> existsList = new ArrayList<>();

    private BaseData.TorchPeakData targetPeakData = null;

    private TextView musicTitle;
    private TextView radiusNum;
    private SeekBar seekBar;
    private ScatterChart mScatterChart;
    private Button playButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torch);
        musicTitle = findViewById(R.id.music_title);
        radiusNum = findViewById(R.id.radius);
        seekBar = findViewById(R.id.seekBar);
        mScatterChart = findViewById(R.id.mScatterChart);
        playButton = findViewById(R.id.play_button);
        stopButton = findViewById(R.id.stop_button);

        mScatterChart.setDragEnabled(false);
        mScatterChart.setScaleEnabled(false);
        mScatterChart.getLegend().setEnabled(false);
        mScatterChart.getDescription().setEnabled(false);
        mScatterChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                BaseData.TorchPeakData d = (BaseData.TorchPeakData) e.getData();
                Log.e(TAG, e.getX() + " | " + e.getY());
                musicTitle.setText(d.realname);
                targetPeakData = d;
            }

            @Override
            public void onNothingSelected() {

            }
        });
        seekBar.setProgress(Integer.valueOf(radiusNum.getText().toString()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusNum.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> musicList = getMusicList();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();// 创建 email 内容
                bundle.putStringArrayList("musicList", musicList);
                intent.putExtra("playExtra", bundle);
                intent.setAction("PlayService.StartToPlay");
                sendBroadcast(intent);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("PlayService.StopPlay");
                sendBroadcast(intent);
            }
        });
        initExistsList();
        setData();
        Log.e(TAG, "onCreate TorchActivity");
    }

    private ArrayList<String> getMusicList(){
        int size = Integer.valueOf(radiusNum.getText().toString());
        ArrayList<String> musicList = new ArrayList<>();
        TreeMap<String, Float> musicMap = new TreeMap<>();
        for (BaseData.TorchPeakData t : existsList){
            float v = ((t.encode_0 - targetPeakData.encode_0) * (t.encode_0 - targetPeakData.encode_0)) +
                    ((t.encode_1 - targetPeakData.encode_1) * (t.encode_1 - targetPeakData.encode_1));
            musicMap.put(t.filepath, v);
        }
        List<Map.Entry<String, Float>> list = new ArrayList<>(musicMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            //升序排序
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<String, Float> e: list) {
            musicList.add(e.getKey());
            if (musicList.size() >= size) {
                break;
            }
            Log.e(TAG, e.getKey()+":"+e.getValue());
        }
        Log.e(TAG, targetPeakData.filepath+"|||");
        return musicList;
    }

    private void initExistsList() {
        File musicFile = new File(Utils.NETEASEFOLDER);
        existsList.clear();
        for (File f : musicFile.listFiles()) {
            if (f.getName().endsWith(".mp3")) {
                String fn = Base64.encodeToString(formatFilename(f.getName().replace(".mp3", "")).getBytes(), Base64.NO_WRAP);
//                fn = fn.trim();
                BaseData.TorchPeakData tp = selectMusicSqlite(getApplicationContext(), fn);
                if (tp != null) {
                    tp.filepath = f.getAbsolutePath();
                    existsList.add(tp);
                } else {
//                    Log.e(TAG, fn);
                }
            }
        }
        Collections.shuffle(existsList);
        if (targetPeakData == null) {
            targetPeakData = existsList.get(0);
        }
        Log.e(TAG, "total: " + musicFile.listFiles().length + "; exists: " + existsList.size());
    }


    //设置数据
    private void setData() {
        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < existsList.size(); i++) {
            BaseData.TorchPeakData t = existsList.get(i);
            ArrayList<Entry> yVals = new ArrayList<>();
            yVals.add(new Entry(t.encode_0, t.encode_1, t));
            ScatterDataSet set1 = new ScatterDataSet(yVals, null);
            set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            //设置颜色
            set1.setColor(Color.rgb(155, (int) t.encode_0, (int) t.encode_1));

            set1.setScatterShapeSize(10f);


            dataSets.add(set1);
        }
        //创建一个数据集的数据对象
        ScatterData data = new ScatterData(dataSets);

        mScatterChart.setData(data);
        mScatterChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        Log.e("@@@@@", "onDestroy TorchActivity");
        super.onDestroy();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}
