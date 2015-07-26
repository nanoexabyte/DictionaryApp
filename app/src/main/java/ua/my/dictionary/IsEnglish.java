package ua.my.dictionary;

/**
 * Created by Andrew Ponomarev on 7/26/2015.
 */
public class IsEnglish {
    public static boolean IS_ENGLISH = true;

    public static void isEnglish(String s) {

        for (char c : s.toCharArray()) {
            if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN) {
                IS_ENGLISH = false;
                break;
            } else {
                IS_ENGLISH = true;
                break;
            }

        }

    }
}
