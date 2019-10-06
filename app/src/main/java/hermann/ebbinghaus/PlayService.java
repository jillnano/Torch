package hermann.ebbinghaus;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayService extends Service {

    private String TAG = "@@@@@PlayService";
    private Context mContext;
    private int currentIdx;

    private MediaPlayer mAsukaPlayer = new MediaPlayer();
    private List<String> playList = new ArrayList<>();
    /**
     * 0:未设置，1:播放，2:暂停
     */
    private int stateBeforePhoneCall = 0;

    private ITorchAidlInterface.Stub serviceBinder = new ITorchAidlInterface.Stub() {

        @Override
        public void setPlayListData(List<String> musicList) throws RemoteException {
            startToPlay(musicList);
        }

        @Override
        public void startPlay() throws RemoteException {
//            curPlayList.setPlayCursor(cursor);
            play();
        }

        @Override
        public int getMusicCurPosition() throws RemoteException {
            int pos = 0;
            if (mAsukaPlayer.isPlaying()) {
                pos = mAsukaPlayer.getCurrentPosition();
            }
            return pos;
        }

        @Override
        public void setProcess(int time) throws RemoteException {
        }

        @Override
        public boolean playOrPauseButton() throws RemoteException {
            return playOrPause();
        }

        @Override
        public void playNext() throws RemoteException {
//            PlayService.this.playNext();
        }

        @Override
        public void playPrev() throws RemoteException {
//            PlayService.this.playPrev();
        }

    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            if (action.equals("PlayService.PlayNext")) {
//                playNext();
//            } else if (action.equals("PlayService.PlayPrev")) {
//                playPrev();
//            } else
            Log.e(TAG, action);
            if (action.equals("PlayService.StartToPlay")) {
                Bundle bundle = intent.getBundleExtra("playExtra");
                startToPlay(bundle.getStringArrayList("musicList"));
            } else if (action.equals("PlayService.StopPlay")) {
//                pausePlay();
                playOrPause();
            }
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (stateBeforePhoneCall == 0) {
                stateBeforePhoneCall = mAsukaPlayer.isPlaying() ? 1 : 2;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:// 挂断
                    if (stateBeforePhoneCall == 1) {
                        startPlay();
                    }
                    stateBeforePhoneCall = 0;
                case TelephonyManager.CALL_STATE_OFFHOOK:// 接听
                    pausePlay();
                case TelephonyManager.CALL_STATE_RINGING:// 响铃
                    pausePlay();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        initPlayer();
        initBroadcastReceiver();
        initPhoneCall();
        Log.e(TAG, "onCreate PlayService");
    }

    /**
     * 初始化播放器相关
     */
    private void initPlayer() {
        mAsukaPlayer.setOnCompletionListener(playCompletionListener);
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PlayService.PlayNext");
        intentFilter.addAction("PlayService.PlayPrev");
        intentFilter.addAction("PlayService.PlayOrPause");
        intentFilter.addAction("PlayService.ItemClickPlay");
        intentFilter.addAction("PlayService.StartToPlay");
        intentFilter.addAction("PlayService.StopPlay");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void initPhoneCall() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private MediaPlayer.OnCompletionListener playCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer arg0) {
            playNext();
        }
    };

    private boolean play() {
        try {
            setToPlay();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * true为开始播放，false为暂停播放
     *
     * @return
     */
    public boolean playOrPause() {
        if (mAsukaPlayer.isPlaying()) {
            mAsukaPlayer.pause();
            return false;
        } else {
            mAsukaPlayer.start();
            return true;
        }
    }

    public void pausePlay() {
        if (mAsukaPlayer.isPlaying()) {
            mAsukaPlayer.pause();
        }
    }

    public void startPlay() {
        if (!mAsukaPlayer.isPlaying()) {
            mAsukaPlayer.start();
        }
    }

    public boolean playNext() {
        currentIdx++;
        if (currentIdx >= playList.size()) {
            currentIdx = 0;
            Collections.shuffle(playList);
        }
        play();
        return true;
    }

//    public boolean playPrev() {
//        try {
//            int playCursor = curPlayList.getPlayCursor();
//            if (playCursor > 0) {
//                curPlayList.getMusicList().get(playCursor).setStatus(PlayValues.NotPlay);
//                SqliteUtils.updateAsukaSqliteStatus(mContext, curPlayList.getMusicList().get(playCursor));
//                curPlayList.reducePlayCursor();
//                setToPlay();
//            }
//            return true;
//        } catch (Exception ex) {
//            return false;
//        }
//    }

    private void setToPlay() throws Exception {
//        int playCursor = curPlayList.getPlayCursor();
//        curPlayList.getMusicList().get(playCursor).setStatus(PlayValues.Playing);
        mAsukaPlayer.stop();
        mAsukaPlayer.reset();
//        mAsukaPlayer.setDataSource(this.getDataSource(playCursor));
        Log.e(TAG, playList.get(currentIdx));
        mAsukaPlayer.setDataSource(playList.get(currentIdx));
        mAsukaPlayer.prepare();
        mAsukaPlayer.start();
//        Utils.savePlayCursorToPreferences(mContext, playCursor);
    }

    private void startToPlay(List<String> musicList) {
        currentIdx = 0;
        playList = musicList;
        Log.e(TAG, playList.toString());
        Collections.shuffle(playList);
        play();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.e(TAG, "onBind PlayService");
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }


}
