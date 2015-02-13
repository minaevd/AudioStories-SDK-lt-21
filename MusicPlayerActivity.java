package ru.ejik_land.audiostories;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import ru.ejik_land.audiostories.model.MusicProvider;
import ru.ejik_land.audiostories.utils.MediaAttrs;

public class MusicPlayerActivity extends ListActivity {

    private static final String TAG = "MDS_AudioStories";

    private static final String ACTION_INIT = "ru.ejik_land.audiostories.action.INIT";
    private static final String ACTION_PLAY_PAUSE = "ru.ejik_land.audiostories.action.PLAY_PAUSE";

    private MediaPlayer mMediaPlayer;
    Intent mPlaybackServiceIntent;
    MusicProvider mMusicProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Started activity: MainActivity, onCreate");

        // fetch all the tracks and albums from the music.json
        // file on a remote server
        mMusicProvider = new MusicProvider();
        mMusicProvider.retrieveMedia(new MusicProvider.Callback() {
            @Override
            public void onMusicCatalogReady(boolean success) {
                if(success) {
                    Log.d(TAG, "music catalog retrieved successfully");
                    mediaRetrieved();
                } else {
                    Log.d(TAG, "error on music catalog retrieval");
                }
            }
        });

        // initialize Music player service
        mPlaybackServiceIntent = new Intent(this, MusicPlayerService.class);
        mPlaybackServiceIntent.setAction(ACTION_INIT);
        startService(mPlaybackServiceIntent);
    }

    public void mediaRetrieved() {

        String[] tracks = mMusicProvider.getTrackIds();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, tracks);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){

        String item = (String) getListAdapter().getItem(position);
        Map<String, MediaAttrs> mListOfTracks = mMusicProvider.getListOfTracks();

        mPlaybackServiceIntent.setAction(ACTION_PLAY_PAUSE);
        mPlaybackServiceIntent.setData(Uri.parse(mListOfTracks.get(item).source));
        startService(mPlaybackServiceIntent);
    }
}
