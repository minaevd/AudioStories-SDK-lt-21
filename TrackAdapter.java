package ru.ejik_land.audiostories;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dminaev on 2/19/15.
 */
public class TrackAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private final String[] mTracks;

    public TrackAdapter(Context context, String[] values) {

        super(context, R.layout.list_leaf, values);

        this.mContext = context;
        this.mTracks = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_leaf, parent, false);

        TextView titleTextView = (TextView) rowView.findViewById(R.id.label);
        ImageView iconImageView = (ImageView) rowView.findViewById(R.id.icon);

        titleTextView.setText(mTracks[position]);
        iconImageView.setImageResource(R.drawable.ic_play_arrow_white_24dp);

        return rowView;
    }
}
