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
import wal.fennel.models.Task;
import wal.fennel.network.NetworkHelper;
import wal.fennel.utils.Constants;
import wal.fennel.utils.MyPicassoInstance;
import wal.fennel.utils.Singleton;
import wal.fennel.views.FontTextView;

/**
 * Created by Khawar on 30/9/2016.
 */
public class FarmerStatusAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Task> mList = new ArrayList<>();

    // View Type for Separators
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
    // View Type for Regular rows
    private static final int ITEM_VIEW_TYPE_REGULAR = 1;
    // Types of Views that need to be handled
    // -- Separators and Regular rows --
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public FarmerStatusAdapter(Context context, ArrayList<Task> list) {
        mContext = context;
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
        return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        Task task = mList.get(position);
        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
                // If its a section ?
                view = inflater.inflate(R.layout.row_section_header, null);
            }
            else {
                // Regular row
                view = inflater.inflate(R.layout.row_my_farmers, null);
            }
        }
        else {
            view = convertView;
        }


        if (itemViewType == ITEM_VIEW_TYPE_SEPARATOR) {
            // If separator

            FontTextView separatorView = (FontTextView) view.findViewById(R.id.tv_header);
            separatorView.setText(task.getName());
        }
        else {
            // If regular

            ImageView ivIconRight = (ImageView) view.findViewById(R.id.ivIconRight);

            if(task.getStatus().equalsIgnoreCase(Constants.STR_NOT_STARTED) || task.getStatus().equalsIgnoreCase(Constants.STR_NOT_STARTED))
            {
                ivIconRight.setImageResource(R.drawable.ic_arrow_right);
                ivIconRight.setVisibility(View.VISIBLE);
            }
            else if(task.getStatus().equalsIgnoreCase(Constants.STR_COMPLETED))
            {
                ivIconRight.setImageResource(R.drawable.ic_approved);
                ivIconRight.setVisibility(View.VISIBLE);
            }

            // Set contact name and number
            FontTextView name = (FontTextView) view.findViewById(R.id.tvFarmerName);
            name.setText( task.getName());
        }

        return view;
    }
}
