package hermann.ebbinghaus;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;

public class MusicService extends Service {

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private ITorchAidlInterface.Stub serviceBinder = new ITorchAidlInterface.Stub() {

        @Override
        public void setPlayListData() throws RemoteException {
            initData();
        }

        @Override
        public void startPlay(int cursor) throws RemoteException {
            curPlayList.setPlayCursor(cursor);
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
            PlayService.this.playNext();
        }

        @Override
        public void playPrev() throws RemoteException {
            PlayService.this.playPrev();
        }

    };

    @Override
    public IBinder onBind(Intent arg0) {
        return serviceBinder;
    }

}
