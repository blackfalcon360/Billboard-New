package com.blackfalcon.billboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    static final int ORANGE = 0xFFFF9100;

    Store st;
    int cBg, cCard, cText, cSub;
    TextView[] tabViews;
    ScrollView[] scrolls;
    LinearLayout mainBox, histBox, setBox;
    EditText et;
    BillboardView preview;
    LinearLayout fgRow, bgRow;
    RadioGroup rgLang, rgTheme;
    int[] langIds = new int[3];
    int[] themeIds = new int[2];

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        st = new Store(this);
        buildUI();
        showTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (histBox != null) buildHistory();
    }

    // ---------- helpers ----------
    String tr(String ur, String en, String ar) { return st.tr(ur, en, ar); }

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable round(int color, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(r));
        return g;
    }

    TextView tv(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    LinearLayout cardBox() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(cCard, 14));
        c.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(5), dp(10), dp(5));
        c.setLayoutParams(lp);
        return c;
    }

    Button btn(String s, int color, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(color, 10));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    CheckBox check(String s, boolean on) {
        CheckBox c = new CheckBox(this);
        c.setText(s);
        c.setTextColor(cText);
        c.setTextSize(15);
        c.setChecked(on);
        return c;
    }

    TextView label(String s) {
        TextView t = tv(s, 15, cText, true);
        t.setPadding(0, dp(10), 0, dp(4));
        return t;
    }

    RadioGroup radios(String[] labels, int checked, int[] ids, boolean horizontal) {
        RadioGroup g = new RadioGroup(this);
        g.setOrientation(horizontal ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
        for (int i = 0; i < labels.length; i++) {
            RadioButton r = new RadioButton(this);
            r.setText(labels[i]);
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            ids[i] = r.getId();
            g.addView(r);
        }
        g.check(ids[checked]);
        return g;
    }

    // ---------- UI ----------
    void buildUI() {
        boolean dark = st.dark();
        cBg = dark ? 0xFF000000 : 0xFFF2F2F2;
        cCard = dark ? 0xFF1A1A1A : 0xFFFFFFFF;
        cText = dark ? 0xFFFFFFFF : 0xFF111111;
        cSub = dark ? 0xFFAAAAAA : 0xFF555555;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBg);
        root.setLayoutDirection(st.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        TextView title = tv("Billboard", 22, cText, true);
        title.setPadding(dp(14), dp(14), dp(14), dp(6));
        root.addView(title);

        String[] names = {tr("بل بورڈ", "Billboard", "اللوحة"), tr("محفوظ متن", "History", "السجل"),
            tr("سیٹنگز", "Settings", "الإعدادات")};
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(6), dp(4), dp(6), dp(8));
        tabViews = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView t = tv(names[i], 14, cText, false);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(4), dp(9), dp(4), dp(9));
            t.setOnClickListener(v -> showTab(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            tabs.addView(t, lp);
            tabViews[i] = t;
        }
        root.addView(tabs);

        FrameLayout content = new FrameLayout(this);
        scrolls = new ScrollView[3];
        mainBox = new LinearLayout(this);
        histBox = new LinearLayout(this);
        setBox = new LinearLayout(this);
        LinearLayout[] boxes = {mainBox, histBox, setBox};
        for (int i = 0; i < 3; i++) {
            boxes[i].setOrientation(LinearLayout.VERTICAL);
            boxes[i].setPadding(0, dp(4), 0, dp(12));
            scrolls[i] = new ScrollView(this);
            scrolls[i].addView(boxes[i]);
            content.addView(scrolls[i], new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView credit = tv("By: Black Falcon \uD83E\uDD85", 13, cSub, false);
        credit.setGravity(Gravity.CENTER);
        credit.setPadding(0, dp(8), 0, dp(10));
        root.addView(credit);

        setContentView(root);
        buildMain();
        buildHistory();
        buildSettings();
    }

    void showTab(int i) {
        for (int k = 0; k < 3; k++) {
            boolean sel = k == i;
            tabViews[k].setTextColor(sel ? Color.WHITE : cText);
            tabViews[k].setBackground(round(sel ? ORANGE : cCard, 20));
            scrolls[k].setVisibility(sel ? View.VISIBLE : View.GONE);
        }
    }

    // ---------- billboard tab ----------
    void updatePreview() {
        String t = et.getText().toString();
        preview.set(t.length() == 0 ? tr("آپ کا متن یہاں", "Your text here", "نصك هنا") : t,
                st.scroll(), st.speed(), Store.FG[st.fg()], Store.BG[st.bg()],
                st.bold(), st.blink(), st.dir(), st.led());
    }

    /** The swatch row uses weights, so its parent must give it the full width (otherwise width = 0). */
    LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    LinearLayout palette(final int[] colors, int selected, final boolean isFg) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < colors.length; i++) {
            final int idx = i;
            TextView sw = new TextView(this);
            GradientDrawable g = new GradientDrawable();
            g.setColor(colors[i]);
            g.setCornerRadius(dp(8));
            g.setStroke(dp(i == selected ? 3 : 1), i == selected ? ORANGE : 0xFF888888);
            sw.setBackground(g);
            sw.setOnClickListener(v -> {
                if (isFg) st.setFg(idx);
                else st.setBg(idx);
                fgRow.removeAllViews();
                bgRow.removeAllViews();
                fgRow.addView(palette(Store.FG, st.fg(), true), fullWidth());
                bgRow.addView(palette(Store.BG, st.bg(), false), fullWidth());
                updatePreview();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(38), 1f);
            lp.setMargins(dp(3), dp(2), dp(3), dp(2));
            row.addView(sw, lp);
        }
        return row;
    }

    void buildMain() {
        mainBox.removeAllViews();

        LinearLayout c1 = cardBox();
        c1.addView(label(tr("بل بورڈ پر کیا لکھنا ہے", "Text for the billboard", "النص الذي سيظهر")));
        et = new EditText(this);
        et.setText(st.text());
        et.setHint(tr("یہاں لکھیں…", "Type here…", "اكتب هنا…"));
        et.setHintTextColor(0xFF777777);
        et.setTextColor(Color.BLACK);
        et.setTextSize(18);
        et.setBackground(round(0xFFFFFFFF, 10));
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et.setMinLines(2);
        et.setGravity(Gravity.TOP);
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable e) { updatePreview(); }
        });
        c1.addView(et);
        preview = new BillboardView(this);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(110));
        pp.setMargins(0, dp(10), 0, 0);
        c1.addView(preview, pp);
        mainBox.addView(c1);

        LinearLayout c2 = cardBox();
        c2.addView(label(tr("انداز", "Mode", "النمط")));
        final int[] mid = new int[2];
        RadioGroup rgm = radios(new String[] {tr("چلتا ہوا متن", "Scrolling", "متحرك"),
            tr("ساکن (اسکرین میں فٹ)", "Static (fit screen)", "ثابت (يملأ الشاشة)")},
            st.scroll() ? 0 : 1, mid, true);
        rgm.setOnCheckedChangeListener((g, id) -> {
            st.setScroll(id == mid[0]);
            updatePreview();
        });
        c2.addView(rgm);

        c2.addView(label(tr("رفتار", "Speed", "السرعة")));
        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(Math.max(0, Math.min(100, (st.speed() - 40) / 6)));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                if (!user) return;
                st.setSpeed(40 + p * 6);
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar s) { }
            @Override public void onStopTrackingTouch(SeekBar s) { }
        });
        c2.addView(sb);

        c2.addView(label(tr("متن کا رنگ", "Text color", "لون النص")));
        fgRow = new LinearLayout(this);
        fgRow.addView(palette(Store.FG, st.fg(), true), fullWidth());
        c2.addView(fgRow);
        c2.addView(label(tr("پس منظر کا رنگ", "Background color", "لون الخلفية")));
        bgRow = new LinearLayout(this);
        bgRow.addView(palette(Store.BG, st.bg(), false), fullWidth());
        c2.addView(bgRow);
        mainBox.addView(c2);

        LinearLayout c3 = cardBox();
        CheckBox cb1 = check(tr("موٹا متن", "Bold", "عريض"), st.bold());
        cb1.setOnCheckedChangeListener((x, on) -> { st.setBold(on); updatePreview(); });
        c3.addView(cb1);
        CheckBox cb2 = check(tr("جھلملانا (Blink)", "Blink", "وميض"), st.blink());
        cb2.setOnCheckedChangeListener((x, on) -> { st.setBlink(on); updatePreview(); });
        c3.addView(cb2);
        CheckBox cb3 = check(tr("LED ڈاٹ اسٹائل", "LED dot style", "نمط نقاط LED"), st.led());
        cb3.setOnCheckedChangeListener((x, on) -> { st.setLed(on); updatePreview(); });
        c3.addView(cb3);
        CheckBox cb4 = check(tr("زیادہ سے زیادہ برائٹنس", "Maximum brightness", "أقصى سطوع"), st.bright());
        cb4.setOnCheckedChangeListener((x, on) -> st.setBright(on));
        c3.addView(cb4);
        c3.addView(label(tr("چلنے کی سمت", "Scroll direction", "اتجاه الحركة")));
        final int[] did = new int[3];
        RadioGroup rgd = radios(new String[] {tr("خودکار", "Auto", "تلقائي"),
            "\u2190", "\u2192"}, st.dir(), did, true);
        rgd.setOnCheckedChangeListener((g, id) -> {
            for (int i = 0; i < 3; i++) if (did[i] == id) st.setDir(i);
            updatePreview();
        });
        c3.addView(rgd);
        mainBox.addView(c3);

        LinearLayout c4 = cardBox();
        c4.addView(btn(tr("پوری اسکرین پر دکھائیں (لینڈ اسکیپ)", "Show full screen (landscape)",
                "عرض بملء الشاشة (أفقي)"), ORANGE, v -> showBillboard()));
        mainBox.addView(c4);

        updatePreview();
    }

    void showBillboard() {
        String t = et.getText().toString();
        if (t.trim().length() == 0) {
            Toast.makeText(this, tr("پہلے متن لکھیں", "Write some text first", "اكتب نصًا أولًا"),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        st.setText(t);
        st.addHistory(t);
        startActivity(new Intent(this, BillboardActivity.class));
    }

    // ---------- history tab ----------
    void buildHistory() {
        histBox.removeAllViews();
        final ArrayList<String> h = st.history();
        if (h.isEmpty()) {
            TextView t = tv(tr("ابھی کوئی محفوظ متن نہیں۔", "No saved texts yet.", "لا نصوص محفوظة بعد."),
                    14, cSub, false);
            t.setPadding(dp(18), dp(24), dp(18), dp(24));
            histBox.addView(t);
            return;
        }
        for (int i = 0; i < h.size(); i++) {
            final int idx = i;
            final String s = h.get(i);
            LinearLayout c = cardBox();
            c.setOrientation(LinearLayout.HORIZONTAL);
            c.setGravity(Gravity.CENTER_VERTICAL);
            TextView t = tv(s, 16, cText, false);
            t.setMaxLines(3);
            c.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView del = tv("\u2715", 18, 0xFFE53935, true);
            del.setPadding(dp(12), dp(4), dp(12), dp(4));
            del.setOnClickListener(v -> { st.removeHistory(idx); buildHistory(); });
            c.addView(del);
            c.setOnClickListener(v -> {
                et.setText(s);
                st.setText(s);
                updatePreview();
                showTab(0);
            });
            histBox.addView(c);
        }
        LinearLayout hold = cardBox();
        hold.addView(btn(tr("سب مٹائیں", "Clear all", "مسح الكل"), 0xFF455A64,
                v -> { st.clearHistory(); buildHistory(); }));
        histBox.addView(hold);
    }

    // ---------- settings ----------
    void buildSettings() {
        setBox.removeAllViews();
        LinearLayout c = cardBox();
        c.addView(label(tr("زبان", "Language", "اللغة")));
        String l = st.lang();
        rgLang = radios(new String[] {"اردو", "English", "العربية"},
                l.equals("en") ? 1 : (l.equals("ar") ? 2 : 0), langIds, false);
        c.addView(rgLang);
        c.addView(label(tr("تھیم", "Theme", "المظهر")));
        rgTheme = radios(new String[] {tr("ڈارک موڈ", "Dark mode", "الوضع الداكن"),
            tr("لائٹ موڈ", "Light mode", "الوضع الفاتح")}, st.dark() ? 0 : 1, themeIds, false);
        c.addView(rgTheme);
        c.addView(btn(tr("محفوظ کریں", "Save", "حفظ"), ORANGE, v -> saveSettings()));
        setBox.addView(c);
        TextView info = tv(tr(
            "بل بورڈ اسکرین پر: ٹیپ = رکیں/چلیں، والیوم کی بٹن = رفتار کم/زیادہ، بیک = بند۔ اسکرین آن رہتی ہے۔ اردو اور عربی متن دائیں سے بائیں خود چلتا ہے۔",
            "On the billboard: tap = pause/play, volume keys = slower/faster, Back = close. The screen stays on. Urdu and Arabic text moves right-to-left automatically.",
            "على اللوحة: اضغط = إيقاف/تشغيل، أزرار الصوت = أبطأ/أسرع، رجوع = إغلاق. تبقى الشاشة مضاءة. النص العربي والأردي يتحرك من اليسار لليمين تلقائيًا."),
            12, cSub, false);
        info.setPadding(dp(14), dp(10), dp(14), dp(8));
        setBox.addView(info);
    }

    void saveSettings() {
        String[] langs = {"ur", "en", "ar"};
        int sel = rgLang.getCheckedRadioButtonId();
        for (int i = 0; i < 3; i++) if (langIds[i] == sel) st.setLang(langs[i]);
        st.setDark(rgTheme.getCheckedRadioButtonId() == themeIds[0]);
        st.setText(et.getText().toString());
        buildUI();
        showTab(2);
        Toast.makeText(this, tr("محفوظ ہو گیا", "Saved", "تم الحفظ"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (et != null) st.setText(et.getText().toString());
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle(tr("ایپ بند کریں؟", "Exit app?", "إغلاق التطبيق؟"))
            .setMessage(tr("کیا آپ واقعی باہر نکلنا چاہتے ہیں؟", "Do you really want to exit?",
                    "هل تريد الخروج فعلًا؟"))
            .setPositiveButton(tr("ہاں", "Yes", "نعم"), (dlg, w) -> finish())
            .setNegativeButton(tr("نہیں", "No", "لا"), null)
            .show();
    }
}
