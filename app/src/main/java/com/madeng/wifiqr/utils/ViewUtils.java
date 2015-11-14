package com.madeng.wifiqr.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ViewUtils {

    private ViewUtils() {
        throw new AssertionError("No instances.");
    }

    public static void hideKeyboard(@NotNull final Activity activity) {
        final View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    public static float hypotenous(@NotNull final View view) {
        return (float) Math.sqrt(Math.pow(view.getWidth(), 2) + Math.pow(view.getHeight(), 2));
    }

    public static void setPivotForACenteredOnB(@NotNull final View a, @NotNull final View b) {
        final ViewParent commonParent = getCommonParent(a, b);

        if (commonParent == null) {
            return;
        }

        final float xOffsetGenerateButton = getXOffset(b, commonParent);
        final float xOffsetMainQrView = getXOffset(a, commonParent);
        final float yOffsetGenerateButton = getYOffset(b, commonParent);
        final float yOffsetMainQrView = getYOffset(a, commonParent);

        final float halfWidthGenerateButton = b.getWidth() / 2f;
        final float halfHeightGenerateButton = b.getHeight() / 2f;
        final float halfWidthMainQrView = a.getWidth() / 2f;
        final float halfHeightMainQrView = a.getHeight() / 2f;

        final float xMidDiff =
                (xOffsetGenerateButton + halfWidthGenerateButton) -
                        (xOffsetMainQrView + halfWidthMainQrView);
        final float yMidDiff =
                (yOffsetGenerateButton + halfHeightGenerateButton) -
                        (yOffsetMainQrView + halfHeightMainQrView);

        a.setPivotY(halfHeightMainQrView + yMidDiff);
        a.setPivotX(halfWidthMainQrView + xMidDiff);
    }

    public static float getXOffset(@NotNull View child, @NotNull ViewParent parent) {
        float sum = 0;
        while (child != parent) {
            sum += child.getX();
            child = (View) child.getParent();
        }
        return sum;
    }

    public static float getYOffset(@NotNull View child, @NotNull ViewParent parent) {
        float sum = 0;
        while (child != parent) {
            sum += child.getY();
            child = (View) child.getParent();
        }
        return sum;
    }

    @Nullable
    public static ViewParent getCommonParent(@NotNull View a, @NotNull View b) {
        List<ViewParent> aParents = getParents(a);
        List<ViewParent> bParents = getParents(b);
        for (ViewParent bParent : bParents) {
            if (aParents.contains(bParent)) {
                return bParent;
            }
        }
        return null;
    }

    @NotNull
    public static List<ViewParent> getParents(@NotNull Object view) {
        final ViewParent parent;
        if (view instanceof ViewParent) {
            parent = ((ViewParent) view).getParent();
        } else if (view instanceof View) {
            parent = ((View) view).getParent();
        } else {
            throw new IllegalArgumentException("view must be either a View or ViewParent");
        }
        if (parent == null) {
            return Collections.emptyList();
        } else {
            final List<ViewParent> parentsOfParent = getParents(parent);
            final List<ViewParent> parents = new LinkedList<>();
            parents.add(parent);
            parents.addAll(parentsOfParent);
            return parents;
        }
    }
}
