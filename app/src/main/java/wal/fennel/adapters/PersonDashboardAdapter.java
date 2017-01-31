package wal.fennel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.models.DashboardFieldAgent;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.views.FontTextView;

/**
 * Created by irfanayaz on 12/31/16.
 */

public class PersonDashboardAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<DashboardFieldAgent> dashboardAgents = new ArrayList<>();
    private ArrayList<DashboardFieldAgent> allDashboardAgents = new ArrayList<>();

    // View Type for Separators
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
    // View Type for Regular rows
    private static final int ITEM_VIEW_TYPE_REGULAR = 1;
    // Types of Views that need to be handled
    // -- Separators and Regular rows --
    private static final int ITEM_VIEW_TYPE_COUNT = 2;


    public PersonDashboardAdapter(Context context, ArrayList list) {
        mContext = context;
        dashboardAgents.addAll(list);
        allDashboardAgents.addAll(list);
//        mFarmersList.addAll(list);
    }

    public void setTaskList(ArrayList list) {
        dashboardAgents.addAll(list);
        allDashboardAgents.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dashboardAgents.size();
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
    public int getItemViewType(int position) {
        boolean isSection = dashboardAgents.get(position).isHeader();

        if (isSection) {
            return ITEM_VIEW_TYPE_SEPARATOR;
        }
        else {
            return ITEM_VIEW_TYPE_REGULAR;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        DashboardFieldAgent agent = dashboardAgents.get(position);
        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
                // If its a section ?
                view = inflater.inflate(R.layout.row_section_header, null);
            }
            else {
                // Regular row
                view = inflater.inflate(R.layout.row_my_signups, null);
            }
        }
        else {
            view = convertView;
        }


        if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
            // If separator

            FontTextView separatorView = (FontTextView) view.findViewById(R.id.tv_header);
            separatorView.setText(agent.getAgentName());
        }
        else {
            // If regular

            // Set contact name and number
            FontTextView name = (FontTextView) view.findViewById(R.id.tv_name);
            name.setText( agent.getAgentName());
            LinearLayout villageLayout = (LinearLayout)view.findViewById(R.id.village_view);
            villageLayout.setVisibility(View.GONE);


            CircleImageView thumb = (CircleImageView) view.findViewById(R.id.profile_image);

            String attachmentUrl = NetworkHelper.makeAttachmentUrlFromId(agent.getAgentAttachmentUrl());

            if(attachmentUrl != null && !attachmentUrl.isEmpty())
            {
                if(NetworkHelper.isNetAvailable(mContext))
                    MyPicassoInstance.getInstance().load(attachmentUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).onlyScaleDown().centerCrop().into(thumb);
                else
                    MyPicassoInstance.getInstance().load(attachmentUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).onlyScaleDown().centerCrop().into(thumb);
            }
            else
            {
                thumb.setImageResource(R.drawable.dummy_profile);
            }
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;//getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
    }

    public ArrayList<DashboardFieldAgent> filter(String charText) {

        dashboardAgents.clear();
        if (charText.length() == 0) {
            dashboardAgents.addAll(allDashboardAgents);
        }
        else
        {
            for (DashboardFieldAgent agent : allDashboardAgents)
            {
                if (agent.getAgentName().toLowerCase(Locale.getDefault()).contains(charText) || agent.isHeader())
                {
                    dashboardAgents.add(agent);
                }
            }
        }
        notifyDataSetChanged();
        return dashboardAgents;
    }
}

