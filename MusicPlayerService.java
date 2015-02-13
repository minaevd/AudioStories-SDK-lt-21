package ru.ejik_land.audiostories;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

import ru.ejik_land.audiostories.model.MusicProvider;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "MDS_MusicPlayerService";

    private static final String ACTION_INIT = "ru.ejik_land.audiostories.action.INIT";
    private static final String ACTION_PLAY = "ru.ejik_land.audiostories.action.PLAY";
    private static final String ACTION_PAUSE = "ru.ejik_land.audiostories.action.PAUSE";
    private static final String ACTION_STOP = "ru.ejik_land.audiostories.action.STOP";

    MediaPlayer mMediaPlayer = null;

    // set WiFi lock
    WifiManager.WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
            .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        mMediaPlayer.reset();
        return true; // true indicates we handled the error
    }

    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "onCreate");

        // initialize Media Player
        initMediaPlayer();
    }

    public void onDestroy () {

        if (wifiLock.isHeld())
            wifiLock.release();
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }

    public void initMediaPlayer() {
        // initialize the MediaPlayer
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);

        // set wake lock not to lose audio when idle and playing
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        // set error listener to reset player
        mMediaPlayer.setOnErrorListener(this);
    }

    public void onMediaRetrieved(String[] tracks) {


    }

    /*
     * Called every time the service is started via startService()
     * @see
     */
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_INIT)) {

            // everything that we need to do is already done in onCreate
            // so we're just relaxing here

        } else if (intent.getAction().equals(ACTION_PLAY)) {

            wifiLock.acquire();

            // prepare/load new audio track
//            TODO:
//            mMediaPlayer.setDataSource(this, Uri.parse(mListOfTracks.get(item).source));
            mMediaPlayer.setOnPreparedListener(this);

            mMediaPlayer.prepareAsync(); // prepare async to not block main thread

        } else if (intent.getAction().equals(ACTION_PAUSE)) {

            wifiLock.release();


        } else if (intent.getAction().equals(ACTION_STOP)) {

            wifiLock.release();
            stopSelf();

        } else {

            Log.d(TAG, "Unknown intent action passed");
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /*
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer player) {

//        // TODO: create a notification with controls and song name
//        String songName;
//        // assign the song name to songName
//        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
//                new Intent(getApplicationContext(), MainActivity.class),
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = new Notification();
//        notification.tickerText = "ticker text";
//        notification.icon = R.drawable.ic_play_arrow_white_24dp;
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
//        notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
//                "Playing: " + songName, pi);
//        startForeground(NOTIFICATION_ID, notification);

        if (!mMediaPlayer.isPlaying()) {
            Log.d(TAG, "configAndStartMediaPlayer startMediaPlayer.");
            mMediaPlayer.start();
        }
    }

    /*
     * Called when media player is done playing current song.
     * @see android.media.MediaPlayer.OnCompletionListener
     */
    @Override
    public void onCompletion(MediaPlayer player) {

        Log.d(TAG, "onCompletion from MediaPlayer");

        // The media player finished playing the current song, so we go ahead
        // and start the next.
//        if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
//            // In this sample, we restart the playing queue when it gets to the end:
//            mCurrentIndexOnQueue++;
//            if (mCurrentIndexOnQueue >= mPlayingQueue.size()) {
//                mCurrentIndexOnQueue = 0;
//            }
//            handlePlayRequest();
//        } else {
//            // If there is nothing to play, we stop and release the resources:
//            handleStopRequest(null);
//        }

        stopSelf();
    }
}
