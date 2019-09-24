package hermann.ebbinghaus;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SpectralPeakProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.mfcc.MFCC;
import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class TestActivity extends AppCompatActivity {

    private String TAG = "@@@@@";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("@@@@@", error.getMessage());
                // FFmpeg is not supported by device
            }
        });
        initPython();
        setContentView(R.layout.activity_test);
        Button testButton = findViewById(R.id.button_test);
            testButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File[] fileList = new File("/sdcard/Music/").listFiles();
                    for (File f: fileList){
                        Log.e("@@@@@", f.getAbsolutePath());
                        mp3ToWav(f);
                        break;
                    }
                }
            });
    }


    // 初始化Python环境
    private void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }

    private void callPythonCode(String filename){
        Python py = Python.getInstance();
        // 调用hello.py模块中的greet函数，并传一个参数
        PyObject obj1 = py.getModule("TorchPeak").callAttr("getSampleDataJava", filename);
        // 将Python返回值换为Java中的Integer类型
        String sample = obj1.toJava(String.class);
        Log.d(TAG,"getSampleDataJava = " + sample);
    }

    private void mp3ToWav(File audioFile){
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                // So fast? Love it!
                String wavname = convertedFile.getAbsolutePath();
                callPythonCode(wavname);
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("@@@@@", error.getMessage());
                // Oops! Something went wrong
            }
        };
        AndroidAudioConverter.with(this)
                // Your current audio file
                .setFile(audioFile)

                // Your desired audio format
                .setFormat(AudioFormat.WAV)

                // An callback to know when conversion is finished
                .setCallback(callback)

                // Start conversion
                .convert();
    }

}
