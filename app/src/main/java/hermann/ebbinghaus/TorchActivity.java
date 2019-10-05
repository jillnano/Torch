package hermann.ebbinghaus;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

import static hermann.ebbinghaus.Utils.han2zen;
import static hermann.ebbinghaus.Utils.selectMusicSqlite;

public class TorchActivity extends AppCompatActivity {

    private String TAG = "@@@@@";

    private ArrayList<BaseData.TorchPeakData> existsList = new ArrayList<>();

    private TextView musicTitle;
    private TextView radiusNum;
    private SeekBar seekBar;
    private ScatterChart mScatterChart;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torch);
        musicTitle = findViewById(R.id.music_title);
        radiusNum = findViewById(R.id.radius);
        seekBar = findViewById(R.id.seekBar);
        mScatterChart = findViewById(R.id.mScatterChart);
        playButton = findViewById(R.id.play_button);
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

            }
        });
        initExistsList();
        setData();
    }

    private void initExistsList() {
        File musicFile = new File(Utils.NETEASEFOLDER);
        existsList.clear();
        for (File f : musicFile.listFiles()) {
            if (f.getName().endsWith(".mp3")) {
                String fn = Base64.encodeToString(han2zen(f.getName().replace(".mp3", "")).getBytes(), Base64.NO_WRAP);
//                fn = fn.trim();
                BaseData.TorchPeakData tp = selectMusicSqlite(getApplicationContext(), fn);
                if (tp != null) {
                    tp.filepath = f.getAbsolutePath();
                    existsList.add(tp);
                }
            }
        }
        Log.e(TAG, "total: " + musicFile.listFiles().length + "; exists: " + existsList.size() + "|" + existsList);
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

}
