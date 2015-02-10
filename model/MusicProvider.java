//package ru.ejik_land.audiostories.model;
//
//import android.media.MediaMetadata;
//import android.os.AsyncTask;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URLConnection;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.concurrent.locks.ReentrantLock;
//
//import ru.ejik_land.audiostories.utils.LogHelper;
//
///**
// * Created by dminaev on 2/7/15.
// */
//public class MusicProvider {
//
//    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class.getSimpleName());
//
//    private static final String CATALOG_URL = "http://storage.googleapis.com/automotive-media/music.json";
//
//    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";
//
//    private static String JSON_MUSIC = "music";
//    private static String JSON_TITLE = "title";
//    private static String JSON_ALBUM = "album";
//    private static String JSON_ARTIST = "artist";
//    private static String JSON_GENRE = "genre";
//    private static String JSON_SOURCE = "source";
//    private static String JSON_IMAGE = "image";
//    private static String JSON_TRACK_NUMBER = "trackNumber";
//    private static String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
//    private static String JSON_DURATION = "duration";
//
//    private final ReentrantLock initializationLock = new ReentrantLock();
//
//    // Categorized caches for music track data:
//    private final HashMap<String, List<String>> mMusicListByAlbum;
//    private final HashMap<String, String> mMusicListById;
//
//    enum State {
//        NON_INITIALIZED, INITIALIZING, INITIALIZED
//    }
//
//    private State mCurrentState = State.NON_INITIALIZED;
//
//
//    public interface Callback {
//        void onMusicCatalogReady(boolean success);
//    }
//
//    public MusicProvider() {
//        mMusicListByAlbum = new HashMap<>();
//        mMusicListById = new HashMap<>();
//    }
//
//
//    /**
//     * Get an iterator over the list of genres
//     *
//     * @return
//     */
//    public Iterable<String> getAlbums() {
//        if (mCurrentState != State.INITIALIZED) {
//            return new ArrayList<String>(0);
//        }
//        return mMusicListByAlbum.keySet();
//    }
//
//    public boolean isInitialized() {
//        return mCurrentState == State.INITIALIZED;
//    }
//
//    /**
//     * Get the list of music tracks from a server and caches the track information
//     * for future reference, keying tracks by mediaId and grouping by genre.
//     *
//     * @return
//     */
//    public void retrieveMedia(final Callback callback) {
//
//        if (mCurrentState == State.INITIALIZED) {
//            // Nothing to do, execute callback immediately
//            callback.onMusicCatalogReady(true);
//            return;
//        }
//
//        // Asynchronously load the music catalog in a separate thread
//        new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                retrieveMediaAsync(callback);
//                return null;
//            }
//        }.execute();
//    }
//
//    private void retrieveMediaAsync(Callback callback) {
//        initializationLock.lock();
//
//        try {
//            if (mCurrentState == State.NON_INITIALIZED) {
//                mCurrentState = State.INITIALIZING;
//
//                int slashPos = CATALOG_URL.lastIndexOf('/');
//                String path = CATALOG_URL.substring(0, slashPos + 1);
//                JSONObject jsonObj = parseUrl(CATALOG_URL);
//
//                JSONArray tracks = jsonObj.getJSONArray(JSON_MUSIC);
//                if (tracks != null) {
//                    for (int j = 0; j < tracks.length(); j++) {
//                        MediaMetadata item = buildFromJSON(tracks.getJSONObject(j), path);
//                        String genre = item.getString(MediaMetadata.METADATA_KEY_GENRE);
//                        List<MediaMetadata> list = mMusicListByAlbum.get(genre);
//                        if (list == null) {
//                            list = new ArrayList<>();
//                        }
//                        list.add(item);
//                        mMusicListByAlbum.put(genre, list);
//                        mMusicListById.put(item.getString(MediaMetadata.METADATA_KEY_MEDIA_ID),
//                                item);
//                    }
//                }
//                mCurrentState = State.INITIALIZED;
//            }
//        } catch (RuntimeException | JSONException e) {
//            LogHelper.e(TAG, e, "Could not retrieve music list");
//        } finally {
//            if (mCurrentState != State.INITIALIZED) {
//                // Something bad happened, so we reset state to NON_INITIALIZED to allow
//                // retries (eg if the network connection is temporary unavailable)
//                mCurrentState = State.NON_INITIALIZED;
//            }
//            initializationLock.unlock();
//            if (callback != null) {
//                callback.onMusicCatalogReady(mCurrentState == State.INITIALIZED);
//            }
//        }
//    }
//
//    private List buildFromJSON(JSONObject json, String basePath) throws JSONException {
//        String title = json.getString(JSON_TITLE);
//        String album = json.getString(JSON_ALBUM);
//        String artist = json.getString(JSON_ARTIST);
//        String genre = json.getString(JSON_GENRE);
//        String source = json.getString(JSON_SOURCE);
//        String iconUrl = json.getString(JSON_IMAGE);
//        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
//        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
//        int duration = json.getInt(JSON_DURATION) * 1000; // ms
//
//        LogHelper.d(TAG, "Found music track: ", json);
//
//        // Media is stored relative to JSON file
//        if (!source.startsWith("http")) {
//            source = basePath + source;
//        }
//        if (!iconUrl.startsWith("http")) {
//            iconUrl = basePath + iconUrl;
//        }
//        // Since we don't have a unique ID in the server, we fake one using the hashcode of
//        // the music source. In a real world app, this could come from the server.
//        String id = String.valueOf(source.hashCode());
//
//        // Adding the music source to the MediaMetadata (and consequently using it in the
//        // mediaSession.setMetadata) is not a good idea for a real world music app, because
//        // the session metadata can be accessed by notification listeners. This is done in this
//        // sample for convenience only.
//        return new MediaMetadata.Builder()
//                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, id)
//                .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
//                .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
//                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
//                .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
//                .putString(MediaMetadata.METADATA_KEY_GENRE, genre)
//                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, iconUrl)
//                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
//                .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, trackNumber)
//                .putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS, totalTrackCount)
//                .build();
//    }
//
//    /**
//     * Download a JSON file from a server, parse the content and return the JSON
//     * object.
//     *
//     * @param urlString
//     * @return
//     */
//    private JSONObject parseUrl(String urlString) {
//        InputStream is = null;
//        try {
//            java.net.URL url = new java.net.URL(urlString);
//            URLConnection urlConnection = url.openConnection();
//            is = new BufferedInputStream(urlConnection.getInputStream());
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    urlConnection.getInputStream(), "iso-8859-1"));
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//            return new JSONObject(sb.toString());
//        } catch (Exception e) {
//            LogHelper.e(TAG, "Failed to parse the json for media list", e);
//            return null;
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    // ignore
//                }
//            }
//        }
//    }
//}
