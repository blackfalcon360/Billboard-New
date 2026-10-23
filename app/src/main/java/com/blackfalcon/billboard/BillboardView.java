package com.blackfalcon.billboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

/** Draws the text as a big billboard / LED sign: scrolling or fitted to the screen. */
public class BillboardView extends View {
    String[] lines = {""};
    String single = "";
    boolean scroll = true, blink = false, led = false, paused = false, rtlText = false, bold = true;
    int fg = Color.YELLOW, bg = Color.BLACK, dir = 0;
    float speedPx = 400f;
    float offset = 0f, textW = 0f, gap = 0f, size = 40f;
    long last = 0L;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint ledP = new Paint();
    final float density;

    public BillboardView(Context c) {
        super(c);
        density = c.getResources().getDisplayMetrics().density;
    }

    public void set(String text, boolean scroll, float speedDp, int fg, int bg,
                    boolean bold, boolean blink, int dir, boolean led) {
        String t = text == null ? "" : text;
        this.lines = t.split("\n");
        if (lines.length == 0) lines = new String[] {""};
        this.single = t.replace('\n', ' ').trim();
        this.rtlText = Fit.rtl(single);
        this.scroll = scroll;
        this.speedPx = speedDp * density;
        this.fg = fg;
        this.bg = bg;
        this.bold = bold;
        this.blink = blink;
        this.dir = dir;
        this.led = led;
        recompute();
        invalidate();
    }

    public void setSpeed(float speedDp) { speedPx = speedDp * density; }

    public void setPaused(boolean v) { paused = v; }

    public boolean isPaused() { return paused; }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        recompute();
    }

    void recompute() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        p.setTypeface(Typeface.create(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL));
        p.setTextSize(100f);
        Paint.FontMetrics fm = p.getFontMetrics();
        float lineH100 = fm.descent - fm.ascent;
        if (scroll) {
            size = 0.72f * h / lineH100 * 100f;
            p.setTextSize(size);
            textW = p.measureText(single);
            gap = Math.max(w * 0.5f, size);
        } else {
            float maxW = 1f;
            for (String l : lines) maxW = Math.max(maxW, p.measureText(l));
            size = (float) Fit.size(w, h, maxW, lineH100, lines.length);
            p.setTextSize(size);
        }
        offset = 0f;
        last = 0L;
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        c.drawColor(bg);
        long now = System.nanoTime();
        float dt = last == 0L ? 0f : (now - last) / 1e9f;
        last = now;
        if (dt > 0.1f) dt = 0.1f;
        boolean visible = !blink || ((System.currentTimeMillis() / 500L) % 2L == 0L);
        p.setColor(fg);
        Paint.FontMetrics fm = p.getFontMetrics();
        if (visible) {
            if (scroll) {
                boolean right = dir == 2 || (dir == 0 && rtlText);
                float period = textW + gap;
                if (!paused) offset = Fit.wrap(offset + (right ? 1 : -1) * speedPx * dt, period);
                float base = h / 2f - (fm.ascent + fm.descent) / 2f;
                p.setTextAlign(Paint.Align.LEFT);
                for (float x = offset - period; x < w; x += period) {
                    if (x + textW > 0) c.drawText(single, x, base, p);
                }
            } else {
                p.setTextAlign(Paint.Align.CENTER);
                float lh = (fm.descent - fm.ascent) * 1.05f;
                float total = lh * lines.length;
                float y0 = (h - total) / 2f - fm.ascent;
                for (int i = 0; i < lines.length; i++) {
                    c.drawText(lines[i], w / 2f, y0 + i * lh, p);
                }
            }
        }
        if (led) {
            float cell = Math.max(3f, h / 56f);
            ledP.setColor(bg);
            ledP.setStrokeWidth(cell * 0.3f);
            for (float x = 0; x < w; x += cell) c.drawLine(x, 0, x, h, ledP);
            for (float y = 0; y < h; y += cell) c.drawLine(0, y, w, y, ledP);
        }
        postInvalidateOnAnimation();
    }
}
