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
    private static final String ACTION_PLAY = "ru.ejik_land.audiostories.action.PLAY";

    private MediaPlayer mMediaPlayer;
    Intent mPlaybackServiceIntent;
    MusicProvider mMusicProvider;
    int mCurrentlyPlayingTrack = -999;
    String[] mTrackIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Started activity: MainActivity, onCreate");

// TEMPORARILY
        // initialize the MediaPlayer
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);

        // set wake lock not to lose audio when idle and playing
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
//////////////

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

//        // initialize Music player service
//        mPlaybackServiceIntent = new Intent(this, MusicPlayerService.class);
//        mPlaybackServiceIntent.setAction(ACTION_INIT);
//        startService(mPlaybackServiceIntent);
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

        try {
            if(mMediaPlayer.isPlaying()) {
                if(position == mCurrentlyPlayingTrack) {
                    mMediaPlayer.pause();
                } else {
                    // stop currently played track
                    mMediaPlayer.stop();
                    // reset Media Player - required
                    mMediaPlayer.reset();
                    // prepare/load new audio track
                    mMediaPlayer.setDataSource(this, Uri.parse(mListOfTracks.get(item).source));
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    mMediaPlayer.prepareAsync();
                    mCurrentlyPlayingTrack = position;
                }
            } else {
                if(position == mCurrentlyPlayingTrack) {
                    mMediaPlayer.start();
                } else {
                    // reset Media Player - just in case
                    mMediaPlayer.reset();
                    // prepare/load new audio track
                    mMediaPlayer.setDataSource(this, Uri.parse(mListOfTracks.get(item).source));
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    mMediaPlayer.prepareAsync();
                    // save current track session id to mCurrentlyPlayingTrack
                    mCurrentlyPlayingTrack = position;
                }
            }
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.v(TAG, e.getMessage());
        }
    }

//    /**
//     * Get the list of music tracks from a server and caches the track information
//     * for future reference, keying tracks by mediaId and grouping by genre.
//     * Asynchronously load the music catalog in a separate thread.
//     *
//     * @return
//     */
//    private class RetrieveMedia extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... urls) {
//
//            for (String url: urls) {
//
//                retrieveMediaAsync(url);
//            }
//            return "executed";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            onMediaRetrieved();
//        }
//
//        private void retrieveMediaAsync(String url) {
//
//            try {
//                // Load music list from web source
//                int slashPos = url.lastIndexOf('/');
//                String path = url.substring(0, slashPos + 1);
//                JSONObject jsonObj = parseUrl(url);
//
//                JSONArray tracks = jsonObj.getJSONArray(JSON_MUSIC);
//
//                if (tracks != null) {
//                    for (int j = 0; j < tracks.length(); j++) {
//
//                        MediaAttrs currTrack = new MediaAttrs();
//
//                        String title = null;
//                        String album = null;
//                        String artist = null;
//                        String genre = null;
//                        String source = null;
//                        String iconUrl = null;
//
//                        JSONObject json = tracks.getJSONObject(j);
//
//                        try {
//                            title = new String(json.getString(JSON_TITLE).getBytes("ISO-8859-1"), "UTF-8");
//                            album = new String(json.getString(JSON_ALBUM).getBytes("ISO-8859-1"), "UTF-8");
//                            artist = new String(json.getString(JSON_ARTIST).getBytes("ISO-8859-1"), "UTF-8");
//                            genre = new String(json.getString(JSON_GENRE).getBytes("ISO-8859-1"), "UTF-8");
//                            source = new String(json.getString(JSON_SOURCE).getBytes("ISO-8859-1"), "UTF-8");
//                            iconUrl = new String(json.getString(JSON_IMAGE).getBytes("ISO-8859-1"), "UTF-8");
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//
//                        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
//                        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
//                        int duration = json.getInt(JSON_DURATION) * 1000; // ms
//
//                        currTrack.title = title;
//                        currTrack.album = album;
//                        currTrack.artist = artist;
//                        currTrack.genre = genre;
//                        if (!source.startsWith("http")) {
//                            source = path + source;
//                        }
//                        currTrack.source = source;
//                        if (!iconUrl.startsWith("http")) {
//                            iconUrl = path + iconUrl;
//                        }
//                        currTrack.image = iconUrl;
//                        currTrack.trackNumber = trackNumber;
//                        currTrack.totalTrackCount = totalTrackCount;
//                        currTrack.duration = duration;
//
//                        // Since we don't have a unique ID in the server, we fake one using the hashcode of
//                        // the music source. In a real world app, this could come from the server.
//                        String id = String.valueOf(source.hashCode());
//                        currTrack.id = id;
//
//                        mListOfTracks.put(title, currTrack);
//
//                        Log.d(TAG, "Found music track: " + json);
//                    }
//                }
//
//            } catch (RuntimeException | JSONException e) {
//                Log.e(TAG, "Could not retrieve music list");
//            }
//        }
//
//        /**
//         * Download a JSON file from a server, parse the content and return the JSON
//         * object.
//         *
//         * @param urlString
//         * @return
//         */
//        private JSONObject parseUrl(String urlString) {
//            InputStream is = null;
//            try {
//                java.net.URL url = new java.net.URL(urlString);
//                URLConnection urlConnection = url.openConnection();
//                is = new BufferedInputStream(urlConnection.getInputStream());
//                BufferedReader reader = new BufferedReader(new InputStreamReader(
//                        urlConnection.getInputStream(), "iso-8859-1"));
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line);
//                }
//                return new JSONObject(sb.toString());
//            } catch (Exception e) {
//                Log.e(TAG, "Failed to parse the json for media list" + e);
//                return null;
//            } finally {
//                if (is != null) {
//                    try {
//                        is.close();
//                    } catch (IOException e) {
//                        // ignore
//                    }
//                }
//            }
//        }
//    }
}
