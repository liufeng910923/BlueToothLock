package com.lncosie.ilandroidos.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;

/**
 * Created by galax on 2015/11/27.
 */
public class LanguageTextView extends TextView {

    int resid;

    public LanguageTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public LanguageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LanguageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LanguageTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    public void languageChanged() {
        if (resid != 0)
            setText(resid);
    }

    void init(AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageTextView, defStyleAttr, 0);
        String txt = a.getString(R.styleable.LanguageTextView_lang);
        if (txt == null) {
            a.recycle();
            return;
        }
        resid = getResources().getIdentifier(txt, "string", getContext().getPackageName());
        a.recycle();
    }

    public void setTextRes(int res) {
        super.setText(res);
        resid = res;
    }
}
