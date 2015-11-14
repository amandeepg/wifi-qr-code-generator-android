package com.madeng.wifiqr;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkInfoAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<QrNetworkInfo> mOriginalObjects;
    private List<QrNetworkInfo> mObjects;

    public NetworkInfoAutoCompleteAdapter(Context context, List<QrNetworkInfo> objects) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mOriginalObjects = objects;
        //noinspection unchecked
        mObjects = Collections.EMPTY_LIST;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public QrNetworkInfo getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.wifi_row, parent, false);
        } else {
            view = convertView;
        }

        final QrNetworkInfo data = getItem(position);

        final TextView nameView = (TextView) view.findViewById(R.id.name_view);
        final TextView contextView = (TextView) view.findViewById(R.id.context_view);
        final ImageView contextIcon = (ImageView) view.findViewById(R.id.context_icon);

        nameView.setText(data.getName());
        final int contextTextResId;
        final MaterialDrawableBuilder.IconValue contextImgIcon;
        switch (data.getSource()) {
            case QrNetworkInfo.SOURCE_SAVED: {
                contextTextResId = R.string.saved_network;
                contextImgIcon = MaterialDrawableBuilder.IconValue.BOOKMARK;
                break;
            }
            case QrNetworkInfo.SOURCE_NEARBY: {
                contextTextResId = R.string.nearby_network;
                contextImgIcon = MaterialDrawableBuilder.IconValue.MAP_MARKER_RADIUS;
                break;
            }
            case QrNetworkInfo.SOURCE_REMEMBERED: {
                contextTextResId = R.string.remembered_network;
                contextImgIcon = MaterialDrawableBuilder.IconValue.CONTENT_SAVE;
                break;
            }
            default: {
                contextTextResId = 0;
                contextImgIcon = null;
            }
        }
        if (contextTextResId > 0) {
            contextView.setText(contextTextResId);
            contextIcon.setImageDrawable(MaterialDrawableBuilder.with(mContext)
                    .setColor(ContextCompat.getColor(mContext, R.color.md_grey_600))
                    .setIcon(contextImgIcon)
                    .build());
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        return new MyFilter();
    }

    private class MyFilter extends Filter {
        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            final Filter.FilterResults filterResults = new Filter.FilterResults();
            if (constraint != null) {
                final List<QrNetworkInfo> results = new ArrayList<>();
                for (QrNetworkInfo info : mOriginalObjects) {
                    if (info.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        results.add(info);
                    }
                }

                filterResults.values = results;
                filterResults.count = results.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence contraint, Filter.FilterResults results) {
            // noinspection unchecked
            mObjects = (List<QrNetworkInfo>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return resultValue == null ? "" : ((QrNetworkInfo) resultValue).getName();
        }
    }
}