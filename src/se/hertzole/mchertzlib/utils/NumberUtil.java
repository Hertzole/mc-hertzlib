package se.hertzole.mchertzlib.utils;

import java.text.NumberFormat;
import java.util.Locale;

public final class NumberUtil {

    private static NumberFormat currencyFormat = NumberFormat.getInstance(Locale.US);

    public static String toPrettyCurrency(double value) {
        String str = currencyFormat.format(value);
        if (str.endsWith(".00")) {
            str = str.substring(0, str.length() - 3);
        }

        return str;
    }

    public static boolean canParseInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean canParseDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}