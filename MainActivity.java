package ru.ejik_land.audiostories;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class MainActivity extends ListActivity {

    private static final String TAG = "MDS";

    private static final String CATALOG_URL = "http://tales.verbery.com/audio/music.json";

    private static String JSON_MUSIC = "music";
    private static String JSON_TITLE = "title";
    private static String JSON_ALBUM = "album";
    private static String JSON_ARTIST = "artist";
    private static String JSON_GENRE = "genre";
    private static String JSON_SOURCE = "source";
    private static String JSON_IMAGE = "image";
    private static String JSON_TRACK_NUMBER = "trackNumber";
    private static String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static String JSON_DURATION = "duration";
    private static String JSON_ID = "id";

    private MediaPlayer mMediaPlayer;

    Map<String, MediaAttrs> mListOfTracks = new HashMap<String, MediaAttrs>();
//    ArrayList<String> mTrackIds = new ArrayList<String>();
    String[] mTrackIds;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Started activity: MainActivity, onCreate");

        // create MediaPlayer
        mMediaPlayer = new MediaPlayer();

        RetrieveMedia task = new RetrieveMedia();
        task.execute(new String[] { CATALOG_URL });
    }

    protected void onMediaRetrieved() {

        int i=0;
        mTrackIds = new String[mListOfTracks.size()];
        for (MediaAttrs value : mListOfTracks.values()) {
            mTrackIds[i++] = value.title;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mTrackIds);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){

        String item = (String) getListAdapter().getItem(position);

        try {
            mMediaPlayer.setDataSource(this, Uri.parse(mListOfTracks.get(item).source));
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
        } catch (IOException e) {
            Log.v("AUDIOHTTPPLAYER", e.getMessage());
        }
    }

    class MediaAttrs {

        private String id;
        private String title;
        private String album;
        private String artist;
        private String genre;
        private String source;
        private String image;
        private int trackNumber;
        private int totalTrackCount;
        private int duration;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by mediaId and grouping by genre.
     * Asynchronously load the music catalog in a separate thread.
     *
     * @return
     */
    private class RetrieveMedia extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            for (String url: urls) {

                retrieveMediaAsync(url);
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            onMediaRetrieved();
        }

        private void retrieveMediaAsync(String url) {

            try {
                // Load music list from web source
                int slashPos = url.lastIndexOf('/');
                String path = url.substring(0, slashPos + 1);
                JSONObject jsonObj = parseUrl(url);

                JSONArray tracks = jsonObj.getJSONArray(JSON_MUSIC);

                if (tracks != null) {
                    for (int j = 0; j < tracks.length(); j++) {

                        MediaAttrs currTrack = new MediaAttrs();

                        String title = null;
                        String album = null;
                        String artist = null;
                        String genre = null;
                        String source = null;
                        String iconUrl = null;

                        JSONObject json = tracks.getJSONObject(j);

                        try {
                            title = new String(json.getString(JSON_TITLE).getBytes("ISO-8859-1"), "UTF-8");
                            album = new String(json.getString(JSON_ALBUM).getBytes("ISO-8859-1"), "UTF-8");
                            artist = new String(json.getString(JSON_ARTIST).getBytes("ISO-8859-1"), "UTF-8");
                            genre = new String(json.getString(JSON_GENRE).getBytes("ISO-8859-1"), "UTF-8");
                            source = new String(json.getString(JSON_SOURCE).getBytes("ISO-8859-1"), "UTF-8");
                            iconUrl = new String(json.getString(JSON_IMAGE).getBytes("ISO-8859-1"), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
                        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
                        int duration = json.getInt(JSON_DURATION) * 1000; // ms

                        currTrack.title = title;
                        currTrack.album = album;
                        currTrack.artist = artist;
                        currTrack.genre = genre;
                        if (!source.startsWith("http")) {
                            source = path + source;
                        }
                        currTrack.source = source;
                        if (!iconUrl.startsWith("http")) {
                            iconUrl = path + iconUrl;
                        }
                        currTrack.image = iconUrl;
                        currTrack.trackNumber = trackNumber;
                        currTrack.totalTrackCount = totalTrackCount;
                        currTrack.duration = duration;

                        // Since we don't have a unique ID in the server, we fake one using the hashcode of
                        // the music source. In a real world app, this could come from the server.
                        String id = String.valueOf(source.hashCode());
                        currTrack.id = id;

                        mListOfTracks.put(title, currTrack);

                        Log.d(TAG, "Found music track: " + json);
                    }
                }

            } catch (RuntimeException | JSONException e) {
                Log.e(TAG, "Could not retrieve music list");
            }
        }

        /**
         * Download a JSON file from a server, parse the content and return the JSON
         * object.
         *
         * @param urlString
         * @return
         */
        private JSONObject parseUrl(String urlString) {
            InputStream is = null;
            try {
                java.net.URL url = new java.net.URL(urlString);
                URLConnection urlConnection = url.openConnection();
                is = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(), "iso-8859-1"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return new JSONObject(sb.toString());
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse the json for media list" + e);
                return null;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }
}
