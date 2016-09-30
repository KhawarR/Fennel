package tintash.fennel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import tintash.fennel.R;
import tintash.fennel.models.Farmer;
import tintash.fennel.utils.Constants;
import tintash.fennel.views.FontTextView;

/**
 * Created by Khawar on 30/9/2016.
 */
public class MySignupsAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Farmer> mList;

    // View Type for Separators
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
    // View Type for Regular rows
    private static final int ITEM_VIEW_TYPE_REGULAR = 1;
    // Types of Views that need to be handled
    // -- Separators and Regular rows --
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public MySignupsAdapter(Context context, ArrayList list) {
        mContext = context;
        mList = list;
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

//    @Override
//    public boolean isEnabled(int position) {
//        return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        Farmer farmer = mList.get(position);
        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
                // If its a section ?
                view = inflater.inflate(R.layout.row_section_header_signups, null);
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

            if(farmer.getSignupStatus().equalsIgnoreCase(Constants.STR_INCOMPLETE))
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
            FontTextView address = (FontTextView) view.findViewById(R.id.tv_address);

            name.setText( farmer.getFullName());
            address.setText( farmer.getAddress() );
        }

        return view;
    }
}
