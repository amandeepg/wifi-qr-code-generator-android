package com.madeng.wifiqr.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;


public class AnimationUtils {

    private AnimationUtils() {
        throw new AssertionError("No instances.");
    }

    public interface EndListener {
        void onAnimationEnd();
    }

    public static class EndListenerAdapter extends AnimatorListenerAdapter {

        private EndListener endListener;

        public EndListenerAdapter(EndListener endListener) {
            this.endListener = endListener;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            endListener.onAnimationEnd();
        }
    }

    public static void hideAndShowTwoView(View hidingView, View showingView) {
        hidingView.setVisibility(View.GONE);
        showingView.setVisibility(View.VISIBLE);
    }
}
