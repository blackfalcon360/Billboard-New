package com.blackfalcon.billboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;

public class Store {
    public static final int[] FG = {0xFFFFFFFF, 0xFFFFEB3B, 0xFFFF1744, 0xFF00E676,
        0xFF00E5FF, 0xFFFF9100, 0xFFE040FB, 0xFF000000};
    public static final int[] BG = {0xFF000000, 0xFFFFFFFF, 0xFF0D47A1, 0xFFB71C1C,
        0xFF1B5E20, 0xFFFFD600, 0xFF4A148C, 0xFFE65100};

    private final SharedPreferences p;

    public Store(Context c) {
        p = c.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String lang() { return p.getString("lang", "en"); }
    public void setLang(String l) { p.edit().putString("lang", l).apply(); }
    public boolean dark() { return p.getBoolean("dark", true); }
    public void setDark(boolean v) { p.edit().putBoolean("dark", v).apply(); }
    public boolean rtl() { return !lang().equals("en"); }

    public String text() { return p.getString("text", ""); }
    public void setText(String s) { p.edit().putString("text", s).apply(); }
    public boolean scroll() { return p.getBoolean("scroll", true); }
    public void setScroll(boolean v) { p.edit().putBoolean("scroll", v).apply(); }
    /** Scroll speed in dp per second. */
    public int speed() { return p.getInt("speed", 220); }
    public void setSpeed(int v) { p.edit().putInt("speed", v).apply(); }
    public int fg() { return p.getInt("fg", 1); }
    public void setFg(int v) { p.edit().putInt("fg", v).apply(); }
    public int bg() { return p.getInt("bg", 0); }
    public void setBg(int v) { p.edit().putInt("bg", v).apply(); }
    public boolean bold() { return p.getBoolean("bold", true); }
    public void setBold(boolean v) { p.edit().putBoolean("bold", v).apply(); }
    public boolean blink() { return p.getBoolean("blink", false); }
    public void setBlink(boolean v) { p.edit().putBoolean("blink", v).apply(); }
    public boolean led() { return p.getBoolean("led", false); }
    public void setLed(boolean v) { p.edit().putBoolean("led", v).apply(); }
    public boolean bright() { return p.getBoolean("bright", true); }
    public void setBright(boolean v) { p.edit().putBoolean("bright", v).apply(); }
    /** 0 = automatic (by script), 1 = moves left, 2 = moves right. */
    public int dir() { return p.getInt("dir", 0); }
    public void setDir(int v) { p.edit().putInt("dir", v).apply(); }

    public String tr(String ur, String en, String ar) {
        String l = lang();
        if (l.equals("en")) return en;
        if (l.equals("ar")) return ar;
        return ur;
    }

    // ----- history of shown texts (newest first, max 20) -----
    public ArrayList<String> history() {
        ArrayList<String> out = new ArrayList<String>();
        try {
            JSONArray a = new JSONArray(p.getString("history", "[]"));
            for (int i = 0; i < a.length(); i++) out.add(a.getString(i));
        } catch (Exception e) { }
        return out;
    }

    private void saveHistory(ArrayList<String> l) {
        JSONArray a = new JSONArray();
        for (String s : l) a.put(s);
        p.edit().putString("history", a.toString()).apply();
    }

    public void addHistory(String t) {
        if (t.trim().length() == 0) return;
        ArrayList<String> l = history();
        l.remove(t);
        l.add(0, t);
        while (l.size() > 20) l.remove(l.size() - 1);
        saveHistory(l);
    }

    public void removeHistory(int idx) {
        ArrayList<String> l = history();
        if (idx >= 0 && idx < l.size()) {
            l.remove(idx);
            saveHistory(l);
        }
    }

    public void clearHistory() { saveHistory(new ArrayList<String>()); }
}
