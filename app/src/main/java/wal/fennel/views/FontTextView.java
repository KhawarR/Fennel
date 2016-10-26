package wal.fennel.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import wal.fennel.R;


public class FontTextView extends TextView {

    public FontTextView(Context context) {
        super(context);
        init(null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    protected static float pixelsToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView);
            String fontName = typedArray.getString(R.styleable.FontTextView_fontName);
            if (fontName != null) {
                Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
                setTypeface(myTypeface);
            }
            typedArray.recycle();
        }
        float scale = getResources().getConfiguration().fontScale;
//        System.out.println("Scale :: " + scale);
//        String log = "";
        float size = pixelsToSp(getContext(), getTextSize());
//        log = log + "initial size : " + size + " scale factor: " + scale;
        if (scale > 1.2f) {

//            if (scale > 1.5f)
//                size = size - size * (scale - 1.3f);
//            else
            size = size - size * (scale - 1.2f);
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

        }

        if (scale < 0.8f) {
            size = size + size * (scale - 0.89f);
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }


//        log = log + " scaled size : " + size;
//        System.out.println(log);

    }

    public void setFontName(final String fontName) {
        Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
        setTypeface(myTypeface);
    }

}
