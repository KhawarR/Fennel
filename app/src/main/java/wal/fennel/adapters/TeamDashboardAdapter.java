package wal.fennel.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.models.DashboardTask;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FennelUtils;

/**
 * Created by irfanayaz on 12/22/16.
 */
public class TeamDashboardAdapter extends BaseAdapter{

    Context mContext;
    private List<DashboardTask> mList;

    public TeamDashboardAdapter(Context context, List<DashboardTask> list) {
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
        View view;

        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.team_dashboard_row, null);
        }
        else {
            view = convertView;
        }

        DashboardTask task = mList.get(position);

        TextView countText = (TextView) view.findViewById(R.id.count_text);
        countText.setText(String.valueOf(task.getCompleted()));

        TextView totalText = (TextView) view.findViewById(R.id.count_total);
        totalText.setText(String.valueOf(task.getTotalCount()));

        TextView nameText = (TextView) view.findViewById(R.id.task_name);
        nameText.setText(task.getTaskName());

        String dueDateString = task.getDueDate();
        String completionDateString = task.getCompletionDate();
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dueDate = null;
        Date completionDate = null;
        try {
            dueDate = serverFormat.parse(dueDateString);
            completionDate = serverFormat.parse(completionDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TextView dueDateText = (TextView) view.findViewById(R.id.due_date);
        TextView monthText = (TextView) view.findViewById(R.id.due_date_month);

        if (dueDate == null) {
            dueDateText.setVisibility(View.GONE);
            monthText.setVisibility(View.GONE);
        } else {
            dueDateText.setVisibility(View.VISIBLE);
            monthText.setVisibility(View.VISIBLE);
            dueDateText.setText(String.valueOf(dueDate.getDate()));
            monthText.setText(FennelUtils.getMonthString(dueDate.getMonth()));
        }

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.task_progress);
        int totalTask = task.getTotalCount();
        int completedTask = task.getCompleted();

        progressBar.setMax(totalTask);
        progressBar.setProgress(completedTask);

        CircleImageView leftImageView = (CircleImageView) view.findViewById(R.id.img_left);
        if (dueDate != null) {

            if (completedTask >= totalTask) {
                if (task.getState() == Constants.FARMING_STATE_ONTIME) {
                    leftImageView.setBorderColor(ContextCompat.getColor(mContext, R.color.app_green));
                    leftImageView.setImageDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.app_green)));
                    countText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                } else {
                    leftImageView.setBorderColor(ContextCompat.getColor(mContext, R.color.dark_red));
                    leftImageView.setImageDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.dark_red)));
                    countText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                }
            } else {
                leftImageView.setImageDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.white)));
                if (task.getState() == Constants.FARMING_STATE_ONTIME) {
                    leftImageView.setBorderColor(ContextCompat.getColor(mContext, R.color.app_green));
                    countText.setTextColor(ContextCompat.getColor(mContext, R.color.app_green));
                } else {
                    leftImageView.setBorderColor(ContextCompat.getColor(mContext, R.color.dark_red));
                    countText.setTextColor(ContextCompat.getColor(mContext, R.color.dark_red));
                }
            }
        } else {
            leftImageView.setImageDrawable(null);
            leftImageView.setBorderColor(ContextCompat.getColor(mContext, R.color.dark_red));
        }
        return view;
    }
}
