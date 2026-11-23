package com.buddyapps.pdfstudio.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Single access point to PDF Studio's icon set.
 *
 * The glyphs are REAL VectorDrawable resources generated from Lucide
 * (https://lucide.dev, ISC licence) by tools/build_vectordrawables.py.
 * Original SVG sources and licence texts live in docs/icon-system/.
 *
 * Using resource-backed vectors (instead of hand-drawn Canvas paths) gives us:
 *  - one coherent 24x24 / 2px-stroke / round-cap collection,
 *  - integer resource IDs resolved once and cached,
 *  - native tinting so the same glyph works in light and dark themes,
 *  - selected states (star/starfill, select/unselect) from the same family.
 *
 * Icons are decorative: actionable parents carry the TalkBack description.
 */
public final class Icons {

    private static final String TAG = "Icons";

    /** Normalised sizes; callers use these instead of raw dp numbers. */
    public static final int SIZE_SM = 18;   // inline / search field / chips
    public static final int SIZE_MD = 22;   // buttons, docks, tool chips
    public static final int SIZE_LG = 24;   // badges, empty states
    public static final int SIZE_NAV = 21;  // bottom navigation

    private static final Map<String, Integer> ID_CACHE = new HashMap<>();

    /** Semantic aliases: one resource, several readable caller names. */
    private static String canonical(String name) {
        if (name == null) return "doc";
        switch (name) {
            case "files":    return "folder";
            case "link":     return "merge";
            case "edit":     return "pen";
            case "prev":     return "back";
            case "next":     return "chevron";
            case "zoom_in":  return "zoomin";
            case "zoom_out": return "zoomout";
            case "warning":  return "alert";
            default:         return name;
        }
    }

    /** Resolve a semantic name to its generated drawable resource. */
    private static int resourceId(Context context, String name) {
        String key = canonical(name);
        Integer cached = ID_CACHE.get(key);
        if (cached == null) {
            int id = context.getResources().getIdentifier(
                    "ic_" + key, "drawable", context.getPackageName());
            if (id == 0) {
                Log.e(TAG, "Missing icon resource for '" + key + "', using fallback");
                id = context.getResources().getIdentifier("ic_doc", "drawable", context.getPackageName());
            }
            cached = id;
            ID_CACHE.put(key, cached);
        }
        return cached;
    }

    public static View view(Context context, String name, int sizeDp, int color) {
        ImageView view = new ImageView(context);
        view.setImageResource(resourceId(context, name));
        view.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setAdjustViewBounds(false);
        int size = DesignSystem.dp(context, sizeDp);
        view.setLayoutParams(new android.view.ViewGroup.LayoutParams(size, size));
        view.setMinimumWidth(size);
        view.setMinimumHeight(size);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return view;
    }

    /** Recolours an icon in place, e.g. when a tool chip becomes selected. */
    public static void setColor(View iconView, int color) {
        if (iconView instanceof ImageView) {
            ((ImageView) iconView).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static Drawable drawable(Context context, String name, int sizeDp, int color) {
        Drawable drawable = context.getResources().getDrawable(resourceId(context, name));
        drawable = drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        int size = DesignSystem.dp(context, sizeDp);
        drawable.setBounds(0, 0, size, size);
        return drawable;
    }

    private Icons() {
    }
}
