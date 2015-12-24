package com.lncosie.ilandroidos.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import com.lncosie.ilandroidos.R;

/**
 * Created by galax on 2015/11/27.
 */
public class LanguageEditView extends EditText {
    int resid;

    public LanguageEditView(Context context) {
        super(context);
        init(null, 0);
    }

    public LanguageEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LanguageEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LanguageEditView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    public void languageChanged() {
        setHint(resid);
    }

    void init(AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageEditView, defStyleAttr, 0);
        String txt = a.getString(R.styleable.LanguageEditView_hint);
        resid = getResources().getIdentifier(txt, "string", getContext().getPackageName());
        if (resid == 0)
            return;
        setText(resid);
        a.recycle();
    }
}
