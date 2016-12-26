package wal.fennel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import wal.fennel.R;

/**
 * Created by irfanayaz on 12/22/16.
 */
public class TeamDashboardAdapter extends BaseAdapter{

    Context mContext;

    public TeamDashboardAdapter(Context context, ArrayList array) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.team_dashboard_row, null);
        }
        else {
            view = convertView;
        }

        return view;
    }
}
