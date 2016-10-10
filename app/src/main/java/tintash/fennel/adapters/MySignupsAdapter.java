package tintash.fennel.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tintash.fennel.R;
import tintash.fennel.models.Farmer;
import tintash.fennel.network.Session;
import tintash.fennel.utils.Constants;
import tintash.fennel.views.FontTextView;

/**
 * Created by Khawar on 30/9/2016.
 */
public class MySignupsAdapter extends BaseAdapter {

    private Picasso picasso;

    private Context mContext;
    private ArrayList<Farmer> mList = new ArrayList<>();
    private ArrayList<Farmer> mFarmersList = new ArrayList<>();

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
        mFarmersList.addAll(list);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", Session.getAuthToken())
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .build();
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
            name.setText( farmer.getFullName());

            FontTextView location = (FontTextView) view.findViewById(R.id.tv_location);
            FontTextView sublocation = (FontTextView) view.findViewById(R.id.tv_sublocation);

            if(!farmer.getSubLocation().isEmpty())
                sublocation.setText(farmer.getSubLocation() + ", ");
            else
                sublocation.setText("");

            if(!farmer.getLocation().isEmpty())
                location.setText(farmer.getLocation());
            else
                location.setText("");

            CircleImageView thumb = (CircleImageView) view.findViewById(R.id.profile_image);
            if(farmer.getThumbUrl() != null && !farmer.getThumbUrl().isEmpty())
            {
                String thumbUrl = "https://cs25.salesforce.com/services/data/v36.0/sobjects/Attachment/%s/body";
                thumbUrl = String.format(thumbUrl, farmer.getThumbUrl());
                picasso.load(thumbUrl).into(thumb);
            }
            else
            {
                thumb.setImageResource(R.drawable.dummy_profile);
            }
        }

        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mList.clear();
        if (charText.length() == 0) {
            mList.addAll(mFarmersList);
        }
        else
        {
            for (Farmer farmer : mFarmersList)
            {
                if (farmer.getFullName().toLowerCase(Locale.getDefault()).contains(charText) || farmer.isHeader())
                {
                    mList.add(farmer);
                }
            }
        }
        notifyDataSetChanged();
    }
}
