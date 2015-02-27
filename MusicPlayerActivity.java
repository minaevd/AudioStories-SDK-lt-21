package ru.ejik_land.audiostories;

import android.app.ListActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ru.ejik_land.audiostories.model.MusicProvider;
import ru.ejik_land.audiostories.utils.MediaAttrs;

public class MusicPlayerActivity extends ListActivity {

    private static final String TAG = "MDS_AudioStories";

    private static final String ACTION_INIT = "ru.ejik_land.audiostories.action.INIT";
    private static final String ACTION_PLAY_PAUSE = "ru.ejik_land.audiostories.action.PLAY_PAUSE";

    private MediaPlayer mMediaPlayer;
    Intent mPlaybackServiceIntent;
    MusicProvider mMusicProvider;

    boolean album_or_track = true; // true - album, false - track
    String mCurrAlbum = "NONE";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums);

        Log.d(TAG, "Started activity: MainActivity, onCreate");

        // show loading text
        TextView loadingHolder = (TextView) this.findViewById(R.id.loadingHolder);
        loadingHolder.setVisibility(View.VISIBLE);

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

        String[] titles = mMusicProvider.getAlbums();
        String[] subtitles = mMusicProvider.getSubTitles();
        String[] icons = mMusicProvider.getIcons();

        album_or_track = true;

        // hide loading text
        TextView loadingHolder = (TextView) this.findViewById(R.id.loadingHolder);
        loadingHolder.setVisibility(View.GONE);

        AlbumAdapter adapter = new AlbumAdapter(this, titles);
        adapter.setSubTitles(subtitles);
        adapter.setIcons(icons);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){

        String item = (String) getListAdapter().getItem(position);

        if(album_or_track) {
            // user clicked on album

            // set title
            TextView headerTitle = (TextView) this.findViewById(R.id.headerTitle);
            headerTitle.setText(item);

            // set subtitle
            TextView headerSubTitle = (TextView) this.findViewById(R.id.headerSubTitle);
            headerSubTitle.setText("Читает " + mMusicProvider.getArtistByAlbum(item));
            headerSubTitle.setVisibility(View.VISIBLE);

            // set header splitter line
            ImageView headerSplitter = (ImageView) this.findViewById(R.id.headerSplitter);
            headerSplitter.setImageResource(R.drawable.line2);

            List<MediaAttrs> tracks = mMusicProvider.getListOfTracksByAlbum(item);

            int i=0;
            String[] trackIds = new String[tracks.size()];
            for (MediaAttrs track: tracks) {
                trackIds[i++] = track.title;
            }

            // save current album
            mCurrAlbum = item;

            TrackAdapter adapter = new TrackAdapter(this, trackIds);
            setListAdapter(adapter);

            album_or_track = false;

        } else {
            // user clicked on audio track

            String src = mMusicProvider.getTrackSourceByTitle(item);

            mPlaybackServiceIntent.setAction(ACTION_PLAY_PAUSE);
            mPlaybackServiceIntent.setData(Uri.parse(src));
            startService(mPlaybackServiceIntent);
        }
    }
}
