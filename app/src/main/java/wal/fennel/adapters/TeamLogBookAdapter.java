package wal.fennel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wal.fennel.R;
import wal.fennel.models.TaskItem;
import wal.fennel.utils.FennelUtils;

/**
 * Created by irfanayaz on 12/2/16.
 */
public class TeamLogBookAdapter extends BaseAdapter {

    private Context mContext;
    private List<TaskItem> mList;

    public TeamLogBookAdapter(Context context, List<TaskItem> list) {
        mContext = context;
        mList = new ArrayList<>();
        mList.addAll(list);
    }

    @Override
    public int getCount() {
        return mList.size();
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

        TaskItem taskItem = mList.get(position);
        View view;

        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.team_logbook_row, null);
        }
        else {
            view = convertView;
        }

        TextView descriptionText = (TextView) view.findViewById(R.id.tv_desc);
        String logText = taskItem.getAgentName() + " did " + taskItem.getFarmerName()+ "'s task " + taskItem.getName();
        descriptionText.setText(logText);
        TextView timeText = (TextView)view.findViewById(R.id.tv_time);
        timeText.setText(FennelUtils.getTimeAgo(taskItem.getDateModified().getTime()));

        return view;
    }
}
