//package ru.ejik_land.audiostories;
//
//import android.app.Activity;
//import android.app.Fragment;
//import android.content.ComponentName;
//import android.content.Context;
//import android.media.browse.MediaBrowser;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
///**
// * Created by dminaev on 2/8/15.
// */
//public class BrowseFragment extends Fragment {
//
//    public static final String ARG_MEDIA_ID = "media_id";
//
//    // The mediaId to be used for subscribing for children using the MediaBrowser.
////    private String mMediaId;
//
////    private MediaBrowser mMediaBrowser;
//    private BrowseAdapter mBrowserAdapter;
//
//    public static BrowseFragment newInstance(List<Map<String, Object>> listOfTracks) {
//
//        Bundle args = new Bundle();
//
//        String[] values = new String[];
//
////        final ArrayList<String> list = new ArrayList<String>();
////        for (int i = 0; i < values.length; ++i) {
////            list.add(values[i]);
////        }
//
//        for (int i = 0; i < listOfTracks.size(); ++i) {
//
//            String mediaId = null;
//
//            for (Map.Entry<String, Object> entry : listOfTracks.get(i).entrySet()) {
//                String key = entry.getKey();
//                String value = entry.getValue().toString();
//                args.putString(key, value);
//                if(key == "id")
//                    mediaId = value;
//            }
//
//            values[i] = mediaId;
//        }
//
//        BrowseFragment fragment = new BrowseFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
//
//        ListView listView = (ListView) rootView.findViewById(R.id.list_view);
//
//        mBrowserAdapter = new BrowseAdapter(getActivity(), android.R.layout.simple_list_item_1, getArguments());
//        listView.setAdapter(mBrowserAdapter);
//
////        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////            @Override
////            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                MediaBrowser.MediaItem item = mBrowserAdapter.getItem(position);
////                try {
////                    FragmentDataHelper listener = (FragmentDataHelper) getActivity();
////                    listener.onMediaItemSelected(item);
////                } catch (ClassCastException ex) {
////                    Log.e(TAG, "Exception trying to cast to FragmentDataHelper", ex);
////                }
////            }
////        });
////
////        Bundle args = getArguments();
////        mMediaId = args.getString(ARG_MEDIA_ID, null);
////
////        mMediaBrowser = new MediaBrowser(getActivity(),
////                new ComponentName(getActivity(), MusicService.class),
////                mConnectionCallback, null);
//
//        return rootView;
//    }
//
//    // An adapter for showing the list of browsed MediaItem's
//    private static class BrowseAdapter extends ArrayAdapter<String> {
//
//        public BrowseAdapter(Context context, int textViewResourceId, Bundle args) {
//            super(context, textViewResourceId, args);
//        }
//
//        static class ViewHolder {
//            ImageView mImageView;
//            TextView mTitleView;
//            TextView mDescriptionView;
//        }
//
////        @Override
////        public View getView(int position, View convertView, ViewGroup parent) {
////
////            ViewHolder holder;
////
////            if (convertView == null) {
////                convertView = LayoutInflater.from(getContext())
////                        .inflate(R.layout.media_list_item, parent, false);
////                holder = new ViewHolder();
////                holder.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
////                holder.mImageView.setVisibility(View.GONE);
////                holder.mTitleView = (TextView) convertView.findViewById(R.id.title);
////                holder.mDescriptionView = (TextView) convertView.findViewById(R.id.description);
////                convertView.setTag(holder);
////            } else {
////                holder = (ViewHolder) convertView.getTag();
////            }
//
////            MediaBrowser.MediaItem item = getItem(position);
//
////            holder.mTitleView.setText(item.getDescription().getTitle());
////            holder.mDescriptionView.setText(item.getDescription().getDescription());
////            if (item.isPlayable()) {
////                holder.mImageView.setImageDrawable(
////                        getContext().getDrawable(R.drawable.ic_play_arrow_white_24dp));
////                holder.mImageView.setVisibility(View.VISIBLE);
////            }
////            return convertView;
////        }
//    }
//}
