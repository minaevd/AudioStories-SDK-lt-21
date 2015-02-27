package ru.ejik_land.audiostories.model;

import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ejik_land.audiostories.utils.MediaAttrs;

/**
 * Get the list of music tracks from a server and caches the track information
 * for future reference, keying tracks by mediaId and grouping by genre.
 * Asynchronously load the music catalog in a separate thread.
 *
 * @return
 */
public class MusicProvider { // extends AsyncTask<String, Void, String> {

    private static final String TAG = "MDS.AudioStories.MusicProvider";
    private static final String CATALOG_URL = "http://tales.verbery.com/audio/music.json";

    Map<String, MediaAttrs> mListOfTracks = new HashMap<String, MediaAttrs>();
    private String[] mTrackIds;
    HashMap<String, List<MediaAttrs>> mListOfAlbums = new HashMap<String, List<MediaAttrs>>();
    private String[] mAlbums;
    private String[] mSubTitles;
    private String[] mIcons;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private State mCurrentState = State.NON_INITIALIZED;

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

    public Map<String, MediaAttrs> getListOfTracks() {
        return mListOfTracks;
    }

    public List<MediaAttrs> getListOfTracksByAlbum(String album) {
        return mListOfAlbums.get(album);
    }

    public String getTrackSourceByTitle(String title) {
        return mListOfTracks.get(title).source;
    }

    public String[] getTrackIds() {
        return mTrackIds;
    }

    public String[] getAlbums() {
        return mAlbums;
    }

    public String[] getSubTitles() {
        return mSubTitles;
    }

    public String[] getIcons() {
        return mIcons;
    }

    public String getArtistByAlbum(String album) {
        return mListOfAlbums.get(album).get(0).artist;
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by mediaId and grouping by genre.
     *
     * @return
     */
    public void retrieveMedia(final Callback callback) {

        if (mCurrentState == State.INITIALIZED) {
            // Nothing to do, execute callback immediately
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                retrieveMediaAsync();
                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                callback.onMusicCatalogReady(mCurrentState == State.INITIALIZED);
            }
        }.execute();
    }

    private void retrieveMediaAsync() {

        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                // Load music list from web source
                int slashPos = CATALOG_URL.lastIndexOf('/');
                String path = CATALOG_URL.substring(0, slashPos + 1);
                JSONObject jsonObj = parseUrl(CATALOG_URL);

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
                        mListOfAlbums.put(album, new ArrayList<MediaAttrs>());

                        Log.d(TAG, "Found music track: " + json);
                    }
                }

                // create a fixed size array of track ids
                int i=0;
                mTrackIds = new String[mListOfTracks.size()];
                for (MediaAttrs value : mListOfTracks.values()) {
                    mTrackIds[i] = value.title;
                    mListOfAlbums.get(value.album).add(value);
                    i++;
                }

                // create a fixed size array of albums
                i=0;

                mAlbums = new String[mListOfAlbums.size()];
                mSubTitles = new String[mListOfAlbums.size()];
                mIcons = new String[mListOfAlbums.size()];

                for (String album: mListOfAlbums.keySet()) {
                    mAlbums[i] = album;
                    mSubTitles[i] = mListOfAlbums.get(album).get(0).artist;
                    mIcons[i] = mListOfAlbums.get(album).get(0).image;
                    i++;
                }

                mCurrentState = State.INITIALIZED;
            }

        } catch (RuntimeException | JSONException e) {
            Log.e(TAG, "Could not retrieve music list");
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
//            if (callback != null) {
//                callback.onMusicCatalogReady(mCurrentState == State.INITIALIZED);
//            }
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