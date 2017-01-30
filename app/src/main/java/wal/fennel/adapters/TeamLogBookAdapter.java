package wal.fennel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.models.LogTaskItem;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.Constants;
import wal.fennel.utils.FennelUtils;
import wal.fennel.utils.MyPicassoInstance;

/**
 * Created by irfanayaz on 12/2/16.
 */
public class TeamLogBookAdapter extends BaseAdapter {

    private Context mContext;
    private List<LogTaskItem> mList;

    public TeamLogBookAdapter(Context context, List<LogTaskItem> list) {
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

        LogTaskItem taskItem = mList.get(position);
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
        String description = getLogDescriptionForTaskItem(taskItem);
//        String logText = taskItem.getAgentName() + " did " + taskItem.getFarmerName()+ "'s task " + taskItem.getName();
        descriptionText.setText(description);
        TextView timeText = (TextView)view.findViewById(R.id.tv_time);
        timeText.setText(FennelUtils.getTimeAgo(taskItem.getDateModified().getTime()));

        CircleImageView profileImageView = (CircleImageView)view.findViewById(R.id.imgLeft);
        String attachmentUrl = NetworkHelper.makeAttachmentUrlFromId(taskItem.getAgentAttachmentId());

        int icon = getRightIconForTaskItem(taskItem);
        ImageView rightIconView = (ImageView)view.findViewById(R.id.right_icon);
        MyPicassoInstance.getInstance().load(icon).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).centerInside().into(rightIconView);

        if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
            if (NetworkHelper.isNetAvailable(mContext))
                MyPicassoInstance.getInstance().load(attachmentUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).onlyScaleDown().centerCrop().into(profileImageView);
            else
                MyPicassoInstance.getInstance().load(attachmentUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).onlyScaleDown().centerCrop().into(profileImageView);
        }

        return view;
    }

    private String getLogDescriptionForTaskItem(LogTaskItem taskItem) {
        String desciption = "";
        if (taskItem == null)
            return desciption;
        if (taskItem.getRecordType().equalsIgnoreCase("Text")) {
            desciption = taskItem.getAgentName() + " updated " +  taskItem.getFarmerName() + "'s " + taskItem.getName() + " to " + taskItem.getTextValue();
        } else if (taskItem.getRecordType().equalsIgnoreCase("Checkbox") || taskItem.getRecordType().equalsIgnoreCase("Options")) {
            desciption = taskItem.getAgentName() + " updated " + taskItem.getName() + "'s value at " + taskItem.getFarmerName() + "'s farm";;
        } else if (taskItem.getRecordType().equalsIgnoreCase("GPS")) {
                desciption = taskItem.getAgentName() + " updated " + taskItem.getName() + ": " + taskItem.getLatitude() + " , " + taskItem.getLongitude() + " at " + taskItem.getFarmerName() + "'s farm";
        } else if (taskItem.getRecordType().equalsIgnoreCase("File")) {
            if (taskItem.getFileActionType().equalsIgnoreCase("View Media")) {
                if (taskItem.getFileType().equalsIgnoreCase("Documents")) {
                    desciption = taskItem.getAgentName() + " viewed a document for " + taskItem.getName() + " at " + taskItem.getFarmerName() + "'s farm";
                } else {
                    desciption = taskItem.getAgentName() + " viewed a picture for " + taskItem.getName() + " at " + taskItem.getFarmerName() + "'s farm";
                }
            } else {
                if (taskItem.getFileType().equalsIgnoreCase("Documents")) {
                    desciption = taskItem.getAgentName() + " uploaded a document for " + taskItem.getName() + " at " + taskItem.getFarmerName() + "'s farm";
                } else {
                    desciption = taskItem.getAgentName() + " uploaded a picture for " + taskItem.getName() + " at " + taskItem.getFarmerName() + "'s farm";
                }
            }
        }
        return desciption;
    }

    private int getRightIconForTaskItem(LogTaskItem taskItem) {
        int icon = 0;
        if (taskItem.getRecordType().equalsIgnoreCase("Text")) {
            icon = R.drawable.icon_text;
        } else if (taskItem.getRecordType().equalsIgnoreCase("Checkbox")) {
            icon = R.drawable.ic_tick_grey;
        } else if (taskItem.getRecordType().equalsIgnoreCase("Options")) {
            icon = R.drawable.ic_dropdown_grey;
        } else if (taskItem.getRecordType().equalsIgnoreCase("GPS")) {
            icon = R.drawable.icon_gps;
        } else if (taskItem.getRecordType().equalsIgnoreCase("File")) {
            if (taskItem.getFileType().equalsIgnoreCase("Documents")) {
                icon = R.drawable.icon_file;
            } else {
                icon = R.drawable.icon_picture;
            }
        }
        return icon;
    }
}
