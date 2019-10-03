package hermann.ebbinghaus;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

import static hermann.ebbinghaus.Utils.han2zen;
import static hermann.ebbinghaus.Utils.selectMusicSqlite;

public class TorchActivity extends AppCompatActivity {

    private String TAG = "@@@@@";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torch);
        File musicFile = new File(Utils.NETEASEFOLDER);

        ArrayList<BaseData.TorchPeakData> existsList = new ArrayList<>();
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

}
