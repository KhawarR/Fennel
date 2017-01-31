package wal.fennel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import wal.fennel.R;
import wal.fennel.models.Farmer;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.Singleton;
import wal.fennel.views.FontTextView;

/**
 * Created by Khawar on 30/9/2016.
 */
public class MySignupsAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Farmer> mList = new ArrayList<>();
//    private ArrayList<Farmer> mFarmersList = new ArrayList<>();

    // View Type for Separators
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
    // View Type for Regular rows
    private static final int ITEM_VIEW_TYPE_REGULAR = 1;
    // Types of Views that need to be handled
    // -- Separators and Regular rows --
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public MySignupsAdapter(Context context, ArrayList list) {
        mContext = context;
        mList.addAll(list);
//        mFarmersList.addAll(list);
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
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        boolean isSection = mList.get(position).isHeader();

        if (isSection) {
            return ITEM_VIEW_TYPE_SEPARATOR;
        }
        else {
            return ITEM_VIEW_TYPE_REGULAR;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return true;//getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
    }

    public void setUpdateDataSet(){
        mList.clear();
        mList.addAll(Singleton.getInstance().mySignupsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        Farmer farmer = mList.get(position);
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
            separatorView.setText(farmer.getFullName());
        }
        else {
            // If regular

            ImageView ivLeftIcon = (ImageView) view.findViewById(R.id.iv_mysignups_icon_left);

            if(farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_ENROLLED))
            {
                ivLeftIcon.setImageResource(R.drawable.ic_arrow_right);
                ivLeftIcon.setVisibility(View.VISIBLE);
            }
            else if(farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_PENDING))
            {
                ivLeftIcon.setVisibility(View.GONE);
            }
            else if(farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_APPROVED))
            {
                ivLeftIcon.setImageResource(R.drawable.ic_approved);
                ivLeftIcon.setVisibility(View.VISIBLE);
            }

            // Set contact name and number
            FontTextView name = (FontTextView) view.findViewById(R.id.tv_name);
            name.setText( farmer.getFullName());

            FontTextView village = (FontTextView) view.findViewById(R.id.tv_village);
            FontTextView sublocation = (FontTextView) view.findViewById(R.id.tv_sublocation);

            if(!farmer.getSubLocation().isEmpty())
                sublocation.setText(farmer.getSubLocation());
            else
                sublocation.setText("");

            if(!farmer.getVillageName().isEmpty())
                village.setText(farmer.getVillageName() + ", ");
            else
                village.setText("");

            CircleImageView thumb = (CircleImageView) view.findViewById(R.id.profile_image);
            String thumbUrl = farmer.getThumbUrl();
            if(thumbUrl != null && !thumbUrl.isEmpty())
            {
                if(Singleton.getInstance().farmerIdtoInvalidate.equalsIgnoreCase(farmer.getFarmerId())) {
                    MyPicassoInstance.getInstance().invalidate(thumbUrl);
                    Singleton.getInstance().farmerIdtoInvalidate = "";
                }
                if(NetworkHelper.isNetAvailable(mContext))
                    MyPicassoInstance.getInstance().load(thumbUrl).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).onlyScaleDown().centerCrop().into(thumb);
                else
                    MyPicassoInstance.getInstance().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).resize(Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM).placeholder(R.drawable.dummy_profile).error(R.drawable.dummy_profile).onlyScaleDown().centerCrop().into(thumb);
            }
            else
            {
                thumb.setImageResource(R.drawable.dummy_profile);
            }
        }

        return view;
    }

    // Filter Class
    public ArrayList filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mList.clear();
        if (charText.length() == 0) {
            mList.addAll(Singleton.getInstance().mySignupsList);
        }
        else
        {
            for (Farmer farmer : Singleton.getInstance().mySignupsList)
            {
                if (farmer.getFullName().toLowerCase(Locale.getDefault()).contains(charText) || farmer.isHeader())
                {
                    mList.add(farmer);
                }
            }
        }
        notifyDataSetChanged();
        return mList;
    }
}
