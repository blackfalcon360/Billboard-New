package com.blackfalcon.billboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

/** Full-screen landscape billboard. Tap = pause/resume, volume keys = speed, Back = close. */
public class BillboardActivity extends Activity {
    Store st;
    BillboardView v;
    int speed;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        st = new Store(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        speed = st.speed();
        v = new BillboardView(this);
        v.set(st.text(), st.scroll(), speed, Store.FG[st.fg()], Store.BG[st.bg()],
                st.bold(), st.blink(), st.dir(), st.led());
        v.setOnClickListener(x -> {
            v.setPaused(!v.isPaused());
            Toast.makeText(this, v.isPaused()
                ? st.tr("رکا ہوا", "Paused", "متوقف") : st.tr("جاری", "Playing", "يعمل"),
                Toast.LENGTH_SHORT).show();
        });
        setContentView(v);
        if (st.bright()) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 1f;
            getWindow().setAttributes(lp);
        }
        if (Build.VERSION.SDK_INT >= 28) cutout();
        hideBars();
        Toast.makeText(this, st.tr("ٹیپ: رکیں/چلیں  •  والیوم: رفتار  •  بیک: بند",
                "Tap: pause/play  •  Volume: speed  •  Back: close",
                "اضغط: إيقاف/تشغيل  •  الصوت: السرعة  •  رجوع: إغلاق"), Toast.LENGTH_LONG).show();
    }

    @TargetApi(28)
    void cutout() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(lp);
    }

    void hideBars() {
        if (Build.VERSION.SDK_INT >= 30) hide30();
        else hideOld();
    }

    @TargetApi(30)
    void hide30() {
        WindowInsetsController c = getWindow().getInsetsController();
        if (c != null) {
            c.hide(WindowInsets.Type.systemBars());
            c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @SuppressWarnings("deprecation")
    void hideOld() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            double f = keyCode == KeyEvent.KEYCODE_VOLUME_UP ? 1.2 : 1.0 / 1.2;
            speed = (int) Math.max(20, Math.min(1200, Math.round(speed * f)));
            v.setSpeed(speed);
            st.setSpeed(speed);
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }
}
