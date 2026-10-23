package com.blackfalcon.billboard;

/** Pure helpers (no Android classes). */
public class Fit {
    /** Text size (px) that makes n lines of width maxW100 (at size 100) fit a w x h screen. */
    public static double size(double w, double h, double maxW100, double lineH100, int n) {
        if (maxW100 <= 0 || lineH100 <= 0 || n < 1) return 24;
        double byW = w * 0.94 / maxW100 * 100.0;
        double byH = h * 0.88 / (n * lineH100 * 1.05) * 100.0;
        return Math.max(8.0, Math.min(byW, byH));
    }

    /** True if the first strongly-directional character is right-to-left (Urdu, Arabic, Hebrew). */
    public static boolean rtl(String s) {
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            byte d = Character.getDirectionality(cp);
            if (d == Character.DIRECTIONALITY_LEFT_TO_RIGHT) return false;
            if (d == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                    || d == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) return true;
            i += Character.charCount(cp);
        }
        return false;
    }

    /** Keeps an offset inside (-period, period) so a looping scroll never overflows. */
    public static float wrap(float offset, float period) {
        if (period <= 0) return 0;
        while (offset <= -period) offset += period;
        while (offset >= period) offset -= period;
        return offset;
    }
}
