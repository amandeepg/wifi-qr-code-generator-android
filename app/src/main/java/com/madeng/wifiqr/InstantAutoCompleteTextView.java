package com.madeng.wifiqr;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

public class InstantAutoCompleteTextView extends MaterialAutoCompleteTextView {

    public InstantAutoCompleteTextView(Context context) {
        super(context);
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InstantAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            showDropDown();
            performFiltering(getText(), 0);
        }
    }

}
