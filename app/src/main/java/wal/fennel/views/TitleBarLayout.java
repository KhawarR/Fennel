package wal.fennel.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import wal.fennel.R;


public class TitleBarLayout extends RelativeLayout implements View.OnClickListener {


    private TextView tvTitle;
    private ImageView imgRight;
    private TitleBarIconClickListener iconClickListener;
    private TextView txtLeft;


    public TitleBarLayout(Context context) {
        super(context);
        init();
    }


    public TitleBarLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.TitleBarLayout,
                0, 0);
        init();

        try {
            this.setupView(
                    a.getResourceId(R.styleable.TitleBarLayout_titleText, -1),
                    a.getResourceId(R.styleable.TitleBarLayout_drawableRight, -1),
                    a.getResourceId(R.styleable.TitleBarLayout_drawableLeft, -1),
                    a.getResourceId(R.styleable.TitleBarLayout_txtLeft, -1)
            );
        } finally {
            a.recycle();
        }
    }

    private void setupView(int titleId, int rightResId, int leftResId, int lftTxtResId) {
        if (titleId != -1)
            tvTitle.setText(titleId);
        if (rightResId != -1)
            imgRight.setImageResource(rightResId);
        if (leftResId != -1)
            txtLeft.setCompoundDrawablesWithIntrinsicBounds(leftResId, 0, 0, 0);
        if (lftTxtResId != -1) {
            txtLeft.setText(lftTxtResId);
        }
    }

    private void init() {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.title_bar_layout, this, true);
        tvTitle = (TextView) view.findViewById(R.id.txtTitle);
        imgRight = (ImageView) view.findViewById(R.id.imgRight);
//        imgLeft = (ImageView) view.findViewById(R.id.imgLeft);
        txtLeft = (TextView) view.findViewById(R.id.txtLeft);
        imgRight.setOnClickListener(this);
//        imgLeft.setOnClickListener(this);
        txtLeft.setOnClickListener(this);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(4.0f);
        }
        else
        {
            setBackgroundResource(R.drawable.title_divider);
        }
        int pad = (int)getResources().getDimension(R.dimen.padding_10);
        setPadding(pad, pad, pad, pad);
    }

    public void setOnIconClickListener(TitleBarIconClickListener aListener) {
        this.iconClickListener = aListener;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.imgRight:
                if (iconClickListener != null)
                    iconClickListener.onTitleBarRightIconClicked(v);
                break;

//            case R.id.imgLeft:
//                if (iconClickListener != null)
//
//                break;
            case R.id.txtLeft:
                if (iconClickListener != null)
                    iconClickListener.onTitleBarLeftIconClicked(v);
                break;
        }
    }

    public void setTitleText(String title) {
        tvTitle.setText(title);
    }

    public void setRightImage(Bitmap bitmap) {
        imgRight.setImageBitmap(bitmap);
    }

    public void setRightImage(int resId) {
        imgRight.setImageResource(resId);
    }

    public interface TitleBarIconClickListener {
        public void onTitleBarRightIconClicked(View view);
        public void onTitleBarLeftIconClicked(View view);
    }

}
