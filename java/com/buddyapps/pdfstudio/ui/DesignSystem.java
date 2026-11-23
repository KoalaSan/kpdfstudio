package com.buddyapps.pdfstudio.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Graphite design system: tokens, type scale and reusable components for a
 * framework-only (no AndroidX) Android app. Colours are resolved by
 * {@link #applyTheme(Context, boolean)} so the whole UI can switch between the
 * dark and light palettes without recreating resources.
 */
public class DesignSystem {

    // ------------------------------------------------------------------
    // 1. Colour tokens (mutable: resolved per theme)
    // ------------------------------------------------------------------
    public static int BG_APP = Color.parseColor("#0B0C0F");
    public static int SURFACE_HEADER = Color.parseColor("#0E1014");
    public static int SURFACE_CARD = Color.parseColor("#13151A");
    public static int SURFACE_CARD_PRESSED = Color.parseColor("#1B1E25");
    public static int SURFACE_ELEVATED = Color.parseColor("#191C23");
    public static int SURFACE_NAV = Color.parseColor("#0E1014");
    public static int BORDER_SUBTLE = Color.parseColor("#23262F");
    public static int BORDER_ACTIVE = Color.parseColor("#333845");

    public static int COLOR_PRIMARY = Color.parseColor("#5B8CFF");
    public static int COLOR_PRIMARY_LIGHT = Color.parseColor("#7BA4FF");
    public static int COLOR_PRIMARY_PRESSED = Color.parseColor("#4773E0");
    public static int COLOR_PRIMARY_TINT = Color.parseColor("#16213D");

    public static int COLOR_ACCENT_BLUE = Color.parseColor("#5B8CFF");
    public static int COLOR_ACCENT_GREEN = Color.parseColor("#34D399");
    public static int COLOR_ACCENT_AMBER = Color.parseColor("#F5B544");
    public static int COLOR_ACCENT_ROSE = Color.parseColor("#F26D6D");
    public static int COLOR_ACCENT_VIOLET = Color.parseColor("#A78BFA");

    public static int TEXT_PRIMARY = Color.parseColor("#F4F5F7");
    public static int TEXT_SECONDARY = Color.parseColor("#B0B6C4");
    public static int TEXT_TERTIARY = Color.parseColor("#8A919F");

    public static int SCRIM = Color.parseColor("#B3000000");

    private static boolean darkTheme = true;

    public static boolean isDark() {
        return darkTheme;
    }

    public static void applyTheme(Context context, boolean dark) {
        darkTheme = dark;
        if (dark) {
            BG_APP = Color.parseColor("#0B0C0F");
            SURFACE_HEADER = Color.parseColor("#0E1014");
            SURFACE_CARD = Color.parseColor("#13151A");
            SURFACE_CARD_PRESSED = Color.parseColor("#1B1E25");
            SURFACE_ELEVATED = Color.parseColor("#191C23");
            SURFACE_NAV = Color.parseColor("#0E1014");
            BORDER_SUBTLE = Color.parseColor("#23262F");
            BORDER_ACTIVE = Color.parseColor("#333845");

            COLOR_PRIMARY = Color.parseColor("#5B8CFF");
            COLOR_PRIMARY_LIGHT = Color.parseColor("#7BA4FF");
            COLOR_PRIMARY_PRESSED = Color.parseColor("#4773E0");
            COLOR_PRIMARY_TINT = Color.parseColor("#16213D");

            COLOR_ACCENT_BLUE = Color.parseColor("#5B8CFF");
            COLOR_ACCENT_GREEN = Color.parseColor("#34D399");
            COLOR_ACCENT_AMBER = Color.parseColor("#F5B544");
            COLOR_ACCENT_ROSE = Color.parseColor("#F26D6D");
            COLOR_ACCENT_VIOLET = Color.parseColor("#A78BFA");

            TEXT_PRIMARY = Color.parseColor("#F4F5F7");
            TEXT_SECONDARY = Color.parseColor("#B0B6C4");
            TEXT_TERTIARY = Color.parseColor("#8A919F");
            SCRIM = Color.parseColor("#B3000000");
        } else {
            BG_APP = Color.parseColor("#F6F7F9");
            SURFACE_HEADER = Color.parseColor("#FFFFFF");
            SURFACE_CARD = Color.parseColor("#FFFFFF");
            SURFACE_CARD_PRESSED = Color.parseColor("#F0F2F6");
            SURFACE_ELEVATED = Color.parseColor("#FFFFFF");
            SURFACE_NAV = Color.parseColor("#FFFFFF");
            BORDER_SUBTLE = Color.parseColor("#E3E6EC");
            BORDER_ACTIVE = Color.parseColor("#C9CED8");

            COLOR_PRIMARY = Color.parseColor("#2F6BFF");
            COLOR_PRIMARY_LIGHT = Color.parseColor("#5B8CFF");
            COLOR_PRIMARY_PRESSED = Color.parseColor("#2455D6");
            COLOR_PRIMARY_TINT = Color.parseColor("#E8EEFF");

            COLOR_ACCENT_BLUE = Color.parseColor("#2F6BFF");
            COLOR_ACCENT_GREEN = Color.parseColor("#0E9F6E");
            COLOR_ACCENT_AMBER = Color.parseColor("#B7791F");
            COLOR_ACCENT_ROSE = Color.parseColor("#D64545");
            COLOR_ACCENT_VIOLET = Color.parseColor("#7C5CD6");

            TEXT_PRIMARY = Color.parseColor("#14161B");
            TEXT_SECONDARY = Color.parseColor("#545B69");
            TEXT_TERTIARY = Color.parseColor("#667085");
            SCRIM = Color.parseColor("#66000000");
        }
    }

    // ------------------------------------------------------------------
    // 2. Spacing (8 pt grid), radii and type scale
    // ------------------------------------------------------------------
    public static final int SP_XS = 4;
    public static final int SP_S = 8;
    public static final int SP_M = 12;
    public static final int SP_L = 16;
    public static final int SP_XL = 20;
    public static final int SP_2XL = 24;
    public static final int SP_3XL = 32;

    public static final int RADIUS_SM = 8;
    public static final int RADIUS_MD = 12;
    public static final int RADIUS_LG = 16;
    public static final int RADIUS_XL = 20;

    public static final int TEXT_DISPLAY = 30;
    public static final int TEXT_TITLE = 22;
    public static final int TEXT_SUBTITLE = 17;
    public static final int TEXT_BODY = 15;
    public static final int TEXT_CAPTION = 13;
    public static final int TEXT_MICRO = 12;

    public static final int TOUCH_MIN = 48;
    public static final int TOUCH_COMFORT = 48;

    public static int dp(Context context, float value) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    /** Returns the given color with its alpha replaced (0..1). */
    public static int withAlpha(int color, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(255 * alpha)));
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }

    // ------------------------------------------------------------------
    // 3. Shapes
    // ------------------------------------------------------------------
    public static GradientDrawable createCardDrawable(Context context, int bgColor, int strokeColor, float radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dp(context, radiusDp));
        if (strokeColor != 0) {
            gd.setStroke(dp(context, 1), strokeColor);
        }
        return gd;
    }

    public static GradientDrawable createTopRoundedDrawable(Context context, int bgColor, float radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadii(new float[]{
                dp(context, radiusDp), dp(context, radiusDp),
                dp(context, radiusDp), dp(context, radiusDp),
                0, 0, 0, 0});
        return gd;
    }

    public static StateListDrawable createClickableCardDrawable(Context context, int normalColor, int pressedColor, float radiusDp) {
        StateListDrawable sld = new StateListDrawable();
        GradientDrawable pressed = createCardDrawable(context, pressedColor, BORDER_ACTIVE, radiusDp);
        GradientDrawable normal = createCardDrawable(context, normalColor, BORDER_SUBTLE, radiusDp);
        sld.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sld.addState(new int[]{}, normal);
        return sld;
    }

    /** Card with a subtle offset shadow layer: gives real depth without AndroidX. */
    public static Drawable createElevatedCardDrawable(Context context, int bgColor, float radiusDp) {
        GradientDrawable shadow = new GradientDrawable();
        shadow.setColor(isDark() ? Color.parseColor("#73000000") : Color.parseColor("#22182433"));
        shadow.setCornerRadius(dp(context, radiusDp));
        LayerDrawable layered = new LayerDrawable(new Drawable[]{
                shadow, createCardDrawable(context, bgColor, BORDER_SUBTLE, radiusDp)
        });
        layered.setLayerInset(0, 0, dp(context, 2), 0, 0);
        return layered;
    }

    /** Clickable card with elevation and a visible pressed state. */
    public static StateListDrawable createClickableElevatedCardDrawable(Context context,
                                                                        int normalColor, int pressedColor, float radiusDp) {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_pressed},
                createElevatedCardDrawable(context, pressedColor, radiusDp));
        sld.addState(new int[]{}, createElevatedCardDrawable(context, normalColor, radiusDp));
        return sld;
    }

    public static StateListDrawable createButtonDrawable(Context context, int normalColor, int pressedColor, float radiusDp) {
        StateListDrawable sld = new StateListDrawable();
        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(pressedColor);
        pressed.setCornerRadius(dp(context, radiusDp));

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(normalColor);
        normal.setCornerRadius(dp(context, radiusDp));

        sld.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sld.addState(new int[]{}, normal);
        return sld;
    }

    // ------------------------------------------------------------------
    // 4. Text helpers
    // ------------------------------------------------------------------
    public static TextView text(Context context, String value, int sizeSp, int color, boolean bold) {
        TextView t = new TextView(context);
        t.setText(value);
        t.setTextSize(sizeSp);
        t.setTextColor(color);
        t.setIncludeFontPadding(false);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return t;
    }

    /** Short action-oriented label for a drawn icon, used by TalkBack. */
    public static String iconLabel(String iconName) {
        if (iconName == null) return "Acción";
        switch (iconName) {
            case "back": return "Volver";
            case "prev": return "Anterior";
            case "chevron": return "Siguiente";
            case "next": return "Siguiente";
            case "search": return "Buscar";
            case "hash": return "Ir a página";
            case "share": return "Compartir";
            case "star": return "Añadir a favoritos";
            case "starfill": return "Quitar de favoritos";
            case "moon": return "Activar modo noche";
            case "sun": return "Desactivar modo noche";
            case "up": return "Subir en el orden";
            case "down": return "Bajar en el orden";
            case "zoomout": return "Alejar";
            case "zoomin": return "Acercar";
            case "rotate": return "Girar página";
            case "pen": return "Anotar";
            case "highlighter": return "Resaltador";
            case "signature": return "Firma";
            case "scissors": return "Extraer páginas";
            case "trash": return "Eliminar";
            case "close": return "Cerrar";
            case "check": return "Confirmar";
            case "alert": return "Aviso";
            default: return iconName;
        }
    }

    /**
     * Light press feedback for cards and icon buttons. The listener does not consume
     * the touch, so the existing click handlers and pressed drawables keep working.
     */
    public static void addPressScale(View view) {
        float scale = 1f;
        try {
            scale = Settings.Global.getFloat(view.getContext().getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        } catch (Exception ignored) {
            // Keep the animation when the setting cannot be read.
        }
        if (scale <= 0.01f) return;
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().cancel();
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(90).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().cancel();
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    public static TextView sectionTitle(Context context, String value) {
        TextView t = text(context, value, TEXT_SUBTITLE, TEXT_PRIMARY, true);
        t.setLetterSpacing(-0.01f);
        return t;
    }

    /** Small vertical accent bar used before section titles. */
    public static View createAccentBar(Context context, int color) {
        View bar = new View(context);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(dp(context, 2));
        bar.setBackground(gd);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(context, 3), dp(context, 16));
        lp.setMargins(0, 0, dp(context, SP_S), 0);
        bar.setLayoutParams(lp);
        return bar;
    }

    public static TextView createSectionTitle(Context context, String value) {
        return sectionTitle(context, value);
    }

    public static View divider(Context context) {
        View v = new View(context);
        v.setBackgroundColor(BORDER_SUBTLE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(context, 0.7f)));
        v.setLayoutParams(lp);
        return v;
    }

    // ------------------------------------------------------------------
    // 5. Icons
    // ------------------------------------------------------------------
    /** Rounded square badge with a drawn icon. */
    public static View iconBadge(Context context, String iconName, int accentColor, int boxDp, int iconDp) {
        LinearLayout box = new LinearLayout(context);
        box.setGravity(Gravity.CENTER);
        box.setBackground(createCardDrawable(context,
                withAlpha(accentColor, 0.14f),
                withAlpha(accentColor, 0.28f),
                boxDp * 0.32f));
        box.addView(Icons.view(context, iconName, iconDp, accentColor));
        return box;
    }

    /** 48 dp icon button with a transparent pressed state and a TalkBack label. */
    public static View iconButton(Context context, String iconName, int color, View.OnClickListener onClick) {
        return iconButton(context, iconName, color, iconLabel(iconName), onClick);
    }

    /** 48 dp icon button with an explicit TalkBack label. */
    public static View iconButton(Context context, String iconName, int color, String description,
                                  View.OnClickListener onClick) {
        FrameLike holder = new FrameLike(context);
        holder.setGravity(Gravity.CENTER);
        holder.setMinimumWidth(dp(context, TOUCH_MIN));
        holder.setMinimumHeight(dp(context, TOUCH_MIN));
        holder.setClickable(true);
        holder.setFocusable(true);
        holder.setContentDescription(description);

        StateListDrawable bg = new StateListDrawable();
        GradientDrawable pressed = createCardDrawable(context, withAlpha(color, 0.14f), 0, RADIUS_MD);
        GradientDrawable normal = createCardDrawable(context, Color.TRANSPARENT, 0, RADIUS_MD);
        bg.addState(new int[]{android.R.attr.state_pressed}, pressed);
        bg.addState(new int[]{}, normal);
        holder.setBackground(bg);

        holder.addView(Icons.view(context, iconName, Icons.SIZE_MD, color));
        if (onClick != null) holder.setOnClickListener(onClick);
        addPressScale(holder);
        return holder;
    }

    /** Minimal LinearLayout that centres a single child like a frame. */
    private static class FrameLike extends LinearLayout {
        FrameLike(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER);
        }
    }

    // ------------------------------------------------------------------
    // 6. Buttons
    // ------------------------------------------------------------------
    public static Button createPrimaryButton(Context context, String label, View.OnClickListener onClick) {
        Button btn = new Button(context);
        btn.setText(label);
        btn.setTextSize(15);
        btn.setTextColor(Color.WHITE);
        btn.setAllCaps(false);
        btn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        btn.setStateListAnimator(null);

        StateListDrawable sld = new StateListDrawable();
        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(COLOR_PRIMARY_PRESSED);
        pressed.setCornerRadius(dp(context, RADIUS_MD));
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(COLOR_PRIMARY);
        normal.setCornerRadius(dp(context, RADIUS_MD));
        sld.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sld.addState(new int[]{}, normal);
        btn.setBackground(sld);

        btn.setPadding(dp(context, SP_XL), dp(context, SP_M), dp(context, SP_XL), dp(context, SP_M));
        btn.setMinHeight(dp(context, TOUCH_COMFORT));
        if (onClick != null) btn.setOnClickListener(onClick);
        return btn;
    }

    public static Button createSecondaryButton(Context context, String label, View.OnClickListener onClick) {
        Button btn = new Button(context);
        btn.setText(label);
        btn.setTextSize(14);
        btn.setTextColor(TEXT_PRIMARY);
        btn.setAllCaps(false);
        btn.setStateListAnimator(null);
        btn.setBackground(createClickableCardDrawable(context, SURFACE_ELEVATED, SURFACE_CARD_PRESSED, RADIUS_MD));
        btn.setPadding(dp(context, SP_M), dp(context, SP_S), dp(context, SP_M), dp(context, SP_S));
        btn.setMinWidth(dp(context, TOUCH_MIN));
        btn.setMinHeight(dp(context, TOUCH_MIN));
        btn.setGravity(Gravity.CENTER);
        if (onClick != null) btn.setOnClickListener(onClick);
        return btn;
    }

    public static Button createGhostButton(Context context, String label, View.OnClickListener onClick) {
        Button btn = new Button(context);
        btn.setText(label);
        btn.setTextSize(14);
        btn.setTextColor(COLOR_PRIMARY);
        btn.setAllCaps(false);
        btn.setStateListAnimator(null);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setPadding(dp(context, SP_M), dp(context, SP_S), dp(context, SP_M), dp(context, SP_S));
        btn.setMinHeight(dp(context, TOUCH_MIN));
        if (onClick != null) btn.setOnClickListener(onClick);
        return btn;
    }

    /** Destructive action: filled danger colour. */
    public static Button createDangerButton(Context context, String label, View.OnClickListener onClick) {
        Button btn = new Button(context);
        btn.setText(label);
        btn.setTextSize(15);
        btn.setTextColor(Color.WHITE);
        btn.setAllCaps(false);
        btn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        btn.setStateListAnimator(null);

        StateListDrawable sld = new StateListDrawable();
        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(withAlpha(COLOR_ACCENT_ROSE, 0.8f));
        pressed.setCornerRadius(dp(context, RADIUS_MD));
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(COLOR_ACCENT_ROSE);
        normal.setCornerRadius(dp(context, RADIUS_MD));
        sld.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sld.addState(new int[]{}, normal);
        btn.setBackground(sld);

        btn.setPadding(dp(context, SP_XL), dp(context, SP_M), dp(context, SP_XL), dp(context, SP_M));
        btn.setMinHeight(dp(context, TOUCH_COMFORT));
        if (onClick != null) btn.setOnClickListener(onClick);
        return btn;
    }

    /** Form field with the Graphite surface, focus is shown by the caret and border. */
    public static EditText createField(Context context, String hint, boolean multiline) {
        EditText edit = new EditText(context);
        edit.setHint(hint);
        edit.setHintTextColor(TEXT_TERTIARY);
        edit.setTextColor(TEXT_PRIMARY);
        edit.setTextSize(TEXT_BODY);
        edit.setBackground(createCardDrawable(context, SURFACE_CARD, BORDER_SUBTLE, RADIUS_MD));
        edit.setPadding(dp(context, SP_L), dp(context, SP_M), dp(context, SP_L), dp(context, SP_M));
        edit.setMinHeight(dp(context, TOUCH_COMFORT));
        if (multiline) {
            edit.setGravity(Gravity.TOP | Gravity.START);
            edit.setMinLines(6);
            edit.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        } else {
            edit.setSingleLine(true);
        }
        return edit;
    }

    public static TextView fieldLabel(Context context, String label) {
        TextView t = text(context, label, TEXT_CAPTION, TEXT_SECONDARY, true);
        t.setLetterSpacing(0.02f);
        t.setPadding(0, 0, 0, dp(context, SP_S));
        return t;
    }

    public static TextView bodyText(Context context, String value) {
        TextView t = text(context, value, TEXT_BODY, TEXT_SECONDARY, false);
        t.setLineSpacing(dp(context, 3), 1f);
        return t;
    }

    public static View createQuickActionCard(Context context, String iconName, String title, int accentColor, View.OnClickListener onClick) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(createClickableElevatedCardDrawable(context, SURFACE_CARD, SURFACE_CARD_PRESSED, RADIUS_LG));
        card.setPadding(dp(context, SP_M), dp(context, SP_L), dp(context, SP_M), dp(context, SP_L));
        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription(title);
        card.setMinimumHeight(dp(context, 96));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(context, SP_XS), 0, dp(context, SP_XS), 0);
        card.setLayoutParams(lp);

        card.addView(iconBadge(context, iconName, accentColor, 44, 22));

        TextView label = text(context, title, TEXT_CAPTION, TEXT_PRIMARY, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, dp(context, SP_S), 0, 0);
        card.addView(label);

        card.setOnClickListener(onClick);
        addPressScale(card);
        return card;
    }

    /** Horizontal tool row: badge + title/description + chevron. */
    public static View createToolRow(Context context, String iconName, String title, String desc, int accentColor, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(createClickableElevatedCardDrawable(context, SURFACE_CARD, SURFACE_CARD_PRESSED, RADIUS_LG));
        row.setPadding(dp(context, SP_L), dp(context, SP_M), dp(context, SP_M), dp(context, SP_M));
        row.setClickable(true);
        row.setFocusable(true);
        row.setContentDescription(title + ". " + desc);
        row.setMinimumHeight(dp(context, 76));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(context, SP_M));
        row.setLayoutParams(lp);

        View badge = iconBadge(context, iconName, accentColor, 48, 24);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(dp(context, 48), dp(context, 48));
        badgeLp.setMargins(0, 0, dp(context, SP_M), 0);
        row.addView(badge, badgeLp);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView t = text(context, title, TEXT_SUBTITLE, TEXT_PRIMARY, true);
        textCol.addView(t);

        TextView d = text(context, desc, TEXT_CAPTION, TEXT_SECONDARY, false);
        d.setPadding(0, dp(context, 3), 0, 0);
        d.setMaxLines(2);
        d.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textCol.addView(d);

        row.addView(textCol);
        View chevron = Icons.view(context, "chevron", 20, TEXT_TERTIARY);
        chevron.setPadding(dp(context, SP_S), 0, 0, 0);
        row.addView(chevron);

        row.setOnClickListener(onClick);
        addPressScale(row);
        return row;
    }

    public static View createCategoryChip(Context context, String label, boolean selected, View.OnClickListener onClick) {
        TextView chip = text(context, label, 13, selected ? COLOR_PRIMARY : TEXT_SECONDARY, selected);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(createCardDrawable(context,
                selected ? COLOR_PRIMARY_TINT : Color.TRANSPARENT,
                selected ? withAlpha(COLOR_PRIMARY, 0.45f) : BORDER_SUBTLE,
                RADIUS_SM * 2));
        chip.setPadding(dp(context, SP_L), dp(context, SP_S), dp(context, SP_L), dp(context, SP_S));
        chip.setMinHeight(dp(context, TOUCH_MIN));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, dp(context, SP_S), 0);
        chip.setLayoutParams(lp);
        chip.setClickable(true);
        chip.setFocusable(true);
        if (onClick != null) chip.setOnClickListener(onClick);
        addPressScale(chip);
        return chip;
    }

    /** Search field with a drawn magnifier and no emoji. */
    public static EditText createSearchBar(Context context, String hint) {
        EditText edit = new EditText(context);
        edit.setHint(hint);
        edit.setHintTextColor(TEXT_TERTIARY);
        edit.setTextColor(TEXT_PRIMARY);
        edit.setTextSize(TEXT_BODY);
        edit.setSingleLine(true);
        edit.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        edit.setBackground(createCardDrawable(context, SURFACE_CARD, BORDER_SUBTLE, RADIUS_MD));
        edit.setPadding(dp(context, SP_L), dp(context, SP_M), dp(context, SP_L), dp(context, SP_M));
        edit.setMinHeight(dp(context, TOUCH_COMFORT));
        edit.setCompoundDrawablePadding(dp(context, SP_M));
        android.graphics.drawable.Drawable mag = Icons.drawable(context, "search", 18, TEXT_TERTIARY);
        mag.setBounds(0, 0, dp(context, 18), dp(context, 18));
        edit.setCompoundDrawables(mag, null, null, null);
        return edit;
    }

    // ------------------------------------------------------------------
    // 7. Screen scaffolding
    // ------------------------------------------------------------------
    public static LinearLayout screenColumn(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(context, SP_XL), dp(context, SP_XL), dp(context, SP_XL), dp(context, SP_3XL));
        return content;
    }

    public static ScrollView screenScroll(Context context) {
        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        return scroll;
    }

    /** Large screen title + one-line subtitle. */
    public static LinearLayout screenHeader(Context context, String title, String subtitle) {
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);

        TextView t = text(context, title, TEXT_DISPLAY, TEXT_PRIMARY, true);
        t.setLetterSpacing(-0.02f);
        header.addView(t);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView s = text(context, subtitle, 13, TEXT_SECONDARY, false);
            s.setPadding(0, dp(context, SP_XS), 0, 0);
            header.addView(s);
        }
        return header;
    }

    /** Section header with an optional text action on the right. */
    public static LinearLayout sectionHeader(Context context, String title, String actionText, View.OnClickListener onAction) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(context, SP_S), 0, dp(context, SP_M));

        TextView t = sectionTitle(context, title);
        row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        if (actionText != null) {
            TextView action = text(context, actionText, 13, COLOR_PRIMARY, true);
            action.setPadding(dp(context, SP_M), dp(context, SP_XS), 0, dp(context, SP_XS));
            action.setClickable(true);
            action.setMinHeight(dp(context, TOUCH_MIN));
            action.setGravity(Gravity.CENTER_VERTICAL);
            action.setOnClickListener(onAction);
            row.addView(action);
        }
        return row;
    }

    public static View emptyState(Context context, String iconName, String title, String desc,
                                  String ctaText, View.OnClickListener onCta) {
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(context, SP_2XL), dp(context, SP_3XL), dp(context, SP_2XL), dp(context, SP_3XL));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(context, SP_S), 0, dp(context, SP_L));
        box.setLayoutParams(lp);

        View badge = iconBadge(context, iconName, TEXT_TERTIARY, 56, 26);
        box.addView(badge, new LinearLayout.LayoutParams(dp(context, 56), dp(context, 56)));

        TextView t = text(context, title, TEXT_SUBTITLE, TEXT_PRIMARY, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(context, SP_L), 0, dp(context, SP_XS));
        box.addView(t);

        TextView d = text(context, desc, 13, TEXT_SECONDARY, false);
        d.setGravity(Gravity.CENTER);
        box.addView(d);

        if (ctaText != null) {
            Button cta = createPrimaryButton(context, ctaText, onCta);
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            btnLp.setMargins(0, dp(context, SP_XL), 0, 0);
            box.addView(cta, btnLp);
        }
        return box;
    }

    /** Text-only empty state for inline lists (no CTA). */
    public static View buildEmptyStateView(Context context, String title, String desc) {
        return emptyState(context, "doc", title, desc, null, null);
    }

    /** Small pill with a label, e.g. "100% offline". */
    public static TextView pill(Context context, String label, int color) {
        TextView pill = text(context, label, TEXT_MICRO, color, true);
        pill.setGravity(Gravity.CENTER);
        pill.setBackground(createCardDrawable(context, withAlpha(color, 0.12f), withAlpha(color, 0.28f), RADIUS_SM));
        pill.setPadding(dp(context, SP_S), dp(context, 3), dp(context, SP_S), dp(context, 3));
        return pill;
    }

    /** Compact statistic used in dashboard/summary cards. */
    public static View statBlock(Context context, String value, String label, int accentColor) {
        LinearLayout block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);

        TextView v = text(context, value, TEXT_TITLE, accentColor, true);
        v.setIncludeFontPadding(false);
        v.setMaxLines(1);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        block.addView(v);

        TextView l = text(context, label, TEXT_MICRO, TEXT_SECONDARY, false);
        l.setPadding(0, dp(context, 2), 0, 0);
        block.addView(l);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        block.setLayoutParams(lp);
        return block;
    }

    // ------------------------------------------------------------------
    // 8. Bottom sheet dialog
    // ------------------------------------------------------------------
    public interface SheetAction {
        void onClick(Dialog dialog);
    }

    /**
     * Bottom sheet in the Graphite style. Content is any view; the sheet adds a
     * grabber, title and an action row.
     */
    public static Dialog sheet(Activity activity, String title, View content,
                               String primaryText, final SheetAction onPrimary,
                               String secondaryText) {
        return sheet(activity, title, content, primaryText, onPrimary, secondaryText, false);
    }

    public static Dialog sheet(Activity activity, String title, View content,
                               String primaryText, final SheetAction onPrimary,
                               String secondaryText, boolean danger) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(createTopRoundedDrawable(activity, SURFACE_CARD, RADIUS_XL));
        root.setPadding(dp(activity, SP_XL), dp(activity, SP_S), dp(activity, SP_XL), dp(activity, SP_XL));

        View grabber = new View(activity);
        grabber.setBackground(createCardDrawable(activity, BORDER_ACTIVE, 0, 2));
        LinearLayout.LayoutParams grabLp = new LinearLayout.LayoutParams(dp(activity, 36), dp(activity, 4));
        grabLp.gravity = Gravity.CENTER_HORIZONTAL;
        grabLp.setMargins(0, dp(activity, SP_S), 0, dp(activity, SP_L));
        root.addView(grabber, grabLp);

        if (title != null) {
            TextView t = text(activity, title, 18, TEXT_PRIMARY, true);
            t.setPadding(0, 0, 0, dp(activity, SP_M));
            root.addView(t);
        }

        if (content != null) {
            root.addView(content, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        LinearLayout actions = new LinearLayout(activity);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.END);
        actions.setPadding(0, dp(activity, SP_XL), 0, 0);

        if (secondaryText != null) {
            Button cancel = createGhostButton(activity, secondaryText, v -> dialog.dismiss());
            actions.addView(cancel);
        }
        if (primaryText != null) {
            Button ok = danger
                    ? createDangerButton(activity, primaryText, null)
                    : createPrimaryButton(activity, primaryText, null);
            ok.setOnClickListener(v -> {
                if (onPrimary != null) onPrimary.onClick(dialog);
                else dialog.dismiss();
            });
            LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            okLp.setMargins(dp(activity, SP_S), 0, 0, 0);
            actions.addView(ok, okLp);
        }
        root.addView(actions);

        ScrollView scroller = new ScrollView(activity);
        scroller.setFillViewport(true);
        scroller.addView(root);
        dialog.setContentView(scroller);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attrs = window.getAttributes();
            attrs.dimAmount = 0.6f;
            window.setAttributes(attrs);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.setWindowAnimations(android.R.style.Animation_InputMethod);
        }
        return dialog;
    }
}
