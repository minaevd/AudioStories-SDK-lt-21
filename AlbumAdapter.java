package ru.ejik_land.audiostories;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private final String[] mTitles;

    private String[] mSubTitles;
    private String[] mIcons;

    public AlbumAdapter(Context context, String[] values) {

        super(context, R.layout.list_root, values);

        this.mContext = context;
        this.mTitles = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_root, parent, false);

        TextView titleTextView = (TextView) rowView.findViewById(R.id.title);
        TextView subtitleTextView = (TextView) rowView.findViewById(R.id.subtitle);
        ImageView iconImageView = (ImageView) rowView.findViewById(R.id.icon);

        titleTextView.setText(mTitles[position]);
        subtitleTextView.setText(mSubTitles[position]);
//        iconImageView.setImageURI(Uri.parse(mIcons[position]));
        iconImageView.setImageResource(R.drawable.dummy_frame_mdpi);

        return rowView;
    }

    public void setSubTitles(String[] subTitles) {
        this.mSubTitles = subTitles;
    }

    public void setIcons(String[] icons) {
        this.mIcons = icons;
    }
}
