package com.buddyapps.pdfstudio.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Hand-drawn monochrome icon set on a 24x24 grid, stroke 1.8 with round caps.
 * Avoids emoji so the interface keeps one consistent visual voice.
 */
public final class Icons {

    private Icons() {
    }

    public static View view(Context context, String name, int sizeDp, int color) {
        IconView v = new IconView(context, name, color);
        int size = DesignSystem.dp(context, sizeDp);
        v.setLayoutParams(new android.view.ViewGroup.LayoutParams(size, size));
        v.setMinimumWidth(size);
        v.setMinimumHeight(size);
        return v;
    }

    public static Drawable drawable(Context context, String name, int sizeDp, int color) {
        IconDrawable d = new IconDrawable(name, color);
        int size = DesignSystem.dp(context, sizeDp);
        d.setBounds(0, 0, size, size);
        return d;
    }

    // ------------------------------------------------------------------
    // Views
    // ------------------------------------------------------------------
    static class IconView extends View {
        private final String name;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        IconView(Context context, String name, int color) {
            super(context);
            this.name = name;
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float size = Math.min(getWidth(), getHeight());
            float scale = size / 24f;
            paint.setStrokeWidth(1.8f * scale);
            canvas.save();
            canvas.translate((getWidth() - size) / 2f, (getHeight() - size) / 2f);
            canvas.scale(scale, scale);
            try {
                Icons.draw(canvas, name, paint);
            } catch (Exception ignored) {
                // A malformed glyph must never take the interface down.
            }
            canvas.restore();
        }
    }

    static class IconDrawable extends Drawable {
        private final String name;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        IconDrawable(String name, int color) {
            this.name = name;
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect b = getBounds();
            float size = Math.min(b.width(), b.height());
            float scale = size / 24f;
            paint.setStrokeWidth(1.8f * scale);
            canvas.save();
            canvas.translate(b.left + (b.width() - size) / 2f, b.top + (b.height() - size) / 2f);
            canvas.scale(scale, scale);
            try {
                Icons.draw(canvas, name, paint);
            } catch (Exception ignored) {
                // A malformed glyph must never take the interface down.
            }
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------
    private static void draw(Canvas c, String name, Paint p) {
        if (name == null) name = "doc";
        switch (name) {
            case "home":
                path(c, p, "M3.5,10.8 L12,3.6 L20.5,10.8");
                path(c, p, "M6.2,9.4 V19.6 H17.8 V9.4");
                path(c, p, "M10,19.6 v-4.6 h4 v4.6");
                break;
            case "files":
                path(c, p, "M3.5,7.2 C3.5,6.1 4.4,5.2 5.5,5.2 h4.2 l2,2.2 h8.8 c1.1,0 2,0.9 2,2 v9.4 c0,1.1 -0.9,2 -2,2 H5.5 c-1.1,0 -2,-0.9 -2,-2 Z");
                break;
            case "tools":
                line(c, p, 4, 7.5f, 20, 7.5f);
                line(c, p, 4, 12, 20, 12);
                line(c, p, 4, 16.5f, 20, 16.5f);
                dot(c, p, 9, 7.5f, 2.1f);
                dot(c, p, 15, 12, 2.1f);
                dot(c, p, 8, 16.5f, 2.1f);
                break;
            case "settings":
                circle(c, p, 12, 12, 3.1f);
                circle(c, p, 12, 12, 6.6f);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * i / 4.0;
                    float x1 = (float) (12 + Math.cos(a) * 6.1);
                    float y1 = (float) (12 + Math.sin(a) * 6.1);
                    float x2 = (float) (12 + Math.cos(a) * 8.4);
                    float y2 = (float) (12 + Math.sin(a) * 8.4);
                    line(c, p, x1, y1, x2, y2);
                }
                break;
            case "search":
                circle(c, p, 10.8f, 10.8f, 6.2f);
                line(c, p, 15.4f, 15.4f, 20.2f, 20.2f);
                break;
            case "plus":
                line(c, p, 12, 5, 12, 19);
                line(c, p, 5, 12, 19, 12);
                break;
            case "minus":
                line(c, p, 5, 12, 19, 12);
                break;
            case "camera":
                path(c, p, "M8.2,7 L9.6,4.8 h4.8 L15.8,7");
                roundRect(c, p, 3.4f, 7f, 20.6f, 20f, 2.4f);
                circle(c, p, 12, 13.4f, 3.5f);
                break;
            case "pen":
            case "edit":
                path(c, p, "M4.2,19.8 L5.4,15.4 L15.8,5 a2.1,2.1 0 0 1 3,3 L8.4,18.6 Z");
                line(c, p, 14.2f, 6.6f, 17.4f, 9.8f);
                break;
            case "book":
                path(c, p, "M12,7.2 C9.8,5.6 6.8,5 3.8,5 v13.4 c3,0 6,0.6 8.2,2.2 c2.2,-1.6 5.2,-2.2 8.2,-2.2 V5 c-3,0 -6,0.6 -8.2,2.2 Z");
                line(c, p, 12, 7.2f, 12, 20.4f);
                break;
            case "merge":
            case "link":
                roundRect(c, p, 3.6f, 8.4f, 14.4f, 20.4f, 2.2f);
                roundRect(c, p, 9.6f, 3.6f, 20.4f, 15.6f, 2.2f);
                line(c, p, 9.6f, 12f, 14.4f, 12f);
                break;
            case "scan":
                path(c, p, "M4,9 V5 h4");
                path(c, p, "M20,9 V5 h-4");
                path(c, p, "M4,15 v4 h4");
                path(c, p, "M20,15 v4 h-4");
                line(c, p, 4.5f, 12, 19.5f, 12);
                break;
            case "compress":
                line(c, p, 12, 3.5f, 12, 9.2f);
                path(c, p, "M8.8,6.2 L12,9.4 l3.2,-3.2");
                line(c, p, 12, 20.5f, 12, 14.8f);
                path(c, p, "M8.8,17.8 L12,14.6 l3.2,3.2");
                break;
            case "scissors":
                circle(c, p, 7, 7, 2.3f);
                circle(c, p, 7, 17, 2.3f);
                line(c, p, 8.8f, 8.4f, 20, 18);
                line(c, p, 8.8f, 15.6f, 20, 6);
                break;
            case "image":
                roundRect(c, p, 3.4f, 5f, 20.6f, 19f, 2.4f);
                circle(c, p, 8.6f, 10f, 1.6f);
                path(c, p, "M4.4,17.4 L9.2,12.4 l3.8,3.8 l3,-2.8 l4,4");
                break;
            case "back":
                path(c, p, "M15,5 L8,12 l7,7");
                break;
            case "chevron":
                path(c, p, "M9.5,6 L15.5,12 l-6,6");
                break;
            case "star":
                star(c, p, 12, 12.4f, 8.2f, 3.5f);
                break;
            case "starfill": {
                Paint.Style old = p.getStyle();
                p.setStyle(Paint.Style.FILL);
                star(c, p, 12, 12.4f, 8.2f, 3.5f);
                p.setStyle(old);
                break;
            }
            case "moon":
                path(c, p, "M19.4,15.2 C17.6,18.4 14.2,20.3 10.9,19.5 C6.5,18.4 3.9,14 5.1,9.6 C6,6.4 8.6,4.1 11.8,3.4 C9.3,5.7 8.5,9.2 10.1,12.3 C11.6,15.2 15.1,16.6 19.4,15.2 Z");
                break;
            case "sun":
                circle(c, p, 12, 12, 4.2f);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * i / 4.0;
                    line(c, p,
                            (float) (12 + Math.cos(a) * 6.6), (float) (12 + Math.sin(a) * 6.6),
                            (float) (12 + Math.cos(a) * 8.8), (float) (12 + Math.sin(a) * 8.8));
                }
                break;
            case "hash":
                line(c, p, 9.2f, 4.5f, 8.2f, 19.5f);
                line(c, p, 15.2f, 4.5f, 14.2f, 19.5f);
                line(c, p, 4.5f, 9.2f, 19.5f, 9.2f);
                line(c, p, 4.5f, 15.2f, 19.5f, 15.2f);
                break;
            case "share":
                circle(c, p, 6, 12, 2.1f);
                circle(c, p, 17.6f, 5.8f, 2.1f);
                circle(c, p, 17.6f, 18.2f, 2.1f);
                line(c, p, 7.9f, 11.1f, 15.7f, 6.7f);
                line(c, p, 7.9f, 12.9f, 15.7f, 17.3f);
                break;
            case "rotate":
                path(c, p, "M20,12 C20,16.4 16.4,20 12,20 C7.6,20 4,16.4 4,12 C4,7.6 7.6,4 12,4 C14.3,4 16.4,4.9 18,6.5");
                path(c, p, "M20,3.8 v5.4 h-5.4");
                break;
            case "trash":
                line(c, p, 4, 6.8f, 20, 6.8f);
                path(c, p, "M9.2,6.8 V4.6 h5.6 v2.2");
                path(c, p, "M6.2,6.8 L7.2,20 h9.6 l1,-13.2");
                line(c, p, 10, 10.4f, 10, 16.6f);
                line(c, p, 14, 10.4f, 14, 16.6f);
                break;
            case "zoomout":
                circle(c, p, 10.8f, 10.8f, 6.2f);
                line(c, p, 15.4f, 15.4f, 20.2f, 20.2f);
                line(c, p, 8, 10.8f, 13.6f, 10.8f);
                break;
            case "zoomin":
                circle(c, p, 10.8f, 10.8f, 6.2f);
                line(c, p, 15.4f, 15.4f, 20.2f, 20.2f);
                line(c, p, 8, 10.8f, 13.6f, 10.8f);
                line(c, p, 10.8f, 8, 10.8f, 13.6f);
                break;
            case "close":
                line(c, p, 6, 6, 18, 18);
                line(c, p, 18, 6, 6, 18);
                break;
            case "check":
                path(c, p, "M5,12.6 L9.4,17 L19,6.8");
                break;
            case "alert":
                path(c, p, "M12,4 L21,20 H3 Z");
                line(c, p, 12, 9.6f, 12, 14.4f);
                dot(c, p, 12, 17.2f, 1.1f);
                break;
            case "doc":
                path(c, p, "M6.2,3.4 h7.6 l4.2,4.2 v13 H6.2 Z");
                path(c, p, "M13.6,3.4 v4.4 h4.4");
                line(c, p, 9.2f, 12.6f, 15, 12.6f);
                line(c, p, 9.2f, 16, 15, 16);
                break;
            case "folder":
                path(c, p, "M3.6,7.2 C3.6,6.1 4.5,5.2 5.6,5.2 h4 l2,2.2 h8.8 c1.1,0 2,0.9 2,2 v9.4 c0,1.1 -0.9,2 -2,2 H5.6 c-1.1,0 -2,-0.9 -2,-2 Z");
                break;
            case "more":
                dot(c, p, 12, 5.4f, 1.5f);
                dot(c, p, 12, 12, 1.5f);
                dot(c, p, 12, 18.6f, 1.5f);
                break;
            case "grid":
                roundRect(c, p, 4, 4, 10.6f, 10.6f, 2);
                roundRect(c, p, 13.4f, 4, 20, 10.6f, 2);
                roundRect(c, p, 4, 13.4f, 10.6f, 20, 2);
                roundRect(c, p, 13.4f, 13.4f, 20, 20, 2);
                break;
            case "up":
                line(c, p, 12, 19.4f, 12, 5);
                path(c, p, "M6,11 L12,5 l6,6");
                break;
            case "down":
                line(c, p, 12, 4.6f, 12, 19);
                path(c, p, "M6,13 L12,19 l6,-6");
                break;
            case "info":
                circle(c, p, 12, 12, 8.4f);
                line(c, p, 12, 11, 12, 16.4f);
                dot(c, p, 12, 7.8f, 1.1f);
                break;
            case "shield":
                path(c, p, "M12,3.2 L19,6 v5.6 c0,4.2 -3,7.2 -7,9.2 c-4,-2 -7,-5 -7,-9.2 V6 Z");
                path(c, p, "M8.8,12 L11.2,14.4 L15.6,9.6");
                break;
            case "bolt":
                path(c, p, "M13.2,3 L5.6,14 h5.6 l-1,7 7.6,-11 h-5.6 Z");
                break;
            case "select":
                roundRect(c, p, 4, 4, 20, 20, 3.4f);
                path(c, p, "M8,12.4 L11,15.4 L16.2,9.2");
                break;
            case "unselect":
                roundRect(c, p, 4, 4, 20, 20, 3.4f);
                break;
            case "palette":
                path(c, p, "M12,3.4 C16.8,3.4 20.6,5.8 20.6,10.2 C20.6,13.6 18.4,15.6 15.2,15.6 h-2.3 c-1.1,0 -1.9,0.7 -1.9,1.7 c0,1.3 1.6,1.6 1.6,3.2 c0,1 -0.6,1.9 -2,1.9 C6,22.4 3.4,17.9 3.4,12 C3.4,6.9 6.9,3.4 12,3.4 Z");
                dot(c, p, 8.2f, 9.2f, 1.2f);
                dot(c, p, 12, 7.4f, 1.2f);
                dot(c, p, 15.6f, 9.2f, 1.2f);
                break;
            case "storage":
                path(c, p, "M4,6.4 h16 v11.2 H4 Z");
                line(c, p, 4, 12, 20, 12);
                dot(c, p, 8, 9.2f, 1f);
                dot(c, p, 8, 15.2f, 1f);
                break;
            default:
                path(c, p, "M6.2,3.4 h7.6 l4.2,4.2 v13 H6.2 Z");
                break;
        }
    }

    // ------------------------------------------------------------------
    // Primitives
    // ------------------------------------------------------------------
    private static void path(Canvas c, Paint p, String data) {
        c.drawPath(parsePath(data), p);
    }

    private static void line(Canvas c, Paint p, float x1, float y1, float x2, float y2) {
        c.drawLine(x1, y1, x2, y2, p);
    }

    private static void circle(Canvas c, Paint p, float cx, float cy, float r) {
        c.drawCircle(cx, cy, r, p);
    }

    private static void dot(Canvas c, Paint p, float cx, float cy, float r) {
        Paint.Style old = p.getStyle();
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, cy, r, p);
        p.setStyle(old);
    }

    private static void roundRect(Canvas c, Paint p, float l, float t, float r, float b, float radius) {
        c.drawRoundRect(new RectF(l, t, r, b), radius, radius, p);
    }

    private static void star(Canvas c, Paint p, float cx, float cy, float outer, float inner) {
        Path path = new Path();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            float radius = (i % 2 == 0) ? outer : inner;
            float x = (float) (cx + Math.cos(angle) * radius);
            float y = (float) (cy + Math.sin(angle) * radius);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
        c.drawPath(path, p);
    }

    /**
     * Small SVG path parser (M L H V C Z, absolute and relative). Any
     * unexpected token stops the path instead of throwing, so a bad icon can
     * never crash the UI.
     */
    private static Path parsePath(String data) {
        Path path = new Path();

        StringBuilder spaced = new StringBuilder();
        for (int k = 0; k < data.length(); k++) {
            char ch = data.charAt(k);
            if (Character.isLetter(ch)) {
                spaced.append(' ').append(ch).append(' ');
            } else {
                spaced.append(ch);
            }
        }
        String[] tokens = spaced.toString().replace(",", " ").trim().split("\\s+");

        int i = 0;
        float cx = 0, cy = 0;
        String cmd = "M";

        while (i < tokens.length) {
            String tok = tokens[i];
            if (tok.length() == 1 && Character.isLetter(tok.charAt(0))) {
                cmd = tok;
                i++;
                if (cmd.equalsIgnoreCase("z")) {
                    path.close();
                    continue;
                }
            }
            boolean rel = cmd.equals(cmd.toLowerCase());
            try {
                switch (cmd.toUpperCase()) {
                    case "M": {
                        float x = f(tokens[i++]);
                        float y = f(tokens[i++]);
                        if (rel) { x += cx; y += cy; }
                        cx = x;
                        cy = y;
                        path.moveTo(cx, cy);
                        cmd = rel ? "l" : "L";
                        break;
                    }
                    case "L": {
                        float x = f(tokens[i++]);
                        float y = f(tokens[i++]);
                        if (rel) { x += cx; y += cy; }
                        cx = x;
                        cy = y;
                        path.lineTo(cx, cy);
                        break;
                    }
                    case "H": {
                        float x = f(tokens[i++]);
                        if (rel) x += cx;
                        cx = x;
                        path.lineTo(cx, cy);
                        break;
                    }
                    case "V": {
                        float y = f(tokens[i++]);
                        if (rel) y += cy;
                        cy = y;
                        path.lineTo(cx, cy);
                        break;
                    }
                    case "C": {
                        float x1 = f(tokens[i++]), y1 = f(tokens[i++]);
                        float x2 = f(tokens[i++]), y2 = f(tokens[i++]);
                        float x = f(tokens[i++]), y = f(tokens[i++]);
                        if (rel) {
                            x1 += cx; y1 += cy;
                            x2 += cx; y2 += cy;
                            x += cx; y += cy;
                        }
                        path.cubicTo(x1, y1, x2, y2, x, y);
                        cx = x;
                        cy = y;
                        break;
                    }
                    default:
                        i++;
                        break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return path;
    }

    private static float f(String s) {
        return Float.parseFloat(s);
    }

}
