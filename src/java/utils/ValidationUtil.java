package utils;

import java.util.regex.Pattern;

/** Kiem tra du lieu dau vao o phia server (khong tin HTML5 validation). */
public class ValidationUtil {

    public static final int NAME_MAX = 100;   // khop Users.full_name NVARCHAR(100)
    public static final int EMAIL_MAX = 150;  // khop Users.email NVARCHAR(150)
    public static final int PASSWORD_MIN = 6;
    public static final int PASSWORD_MAX = 64;

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= EMAIL_MAX && EMAIL.matcher(email).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() <= NAME_MAX;
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && password.length() >= PASSWORD_MIN
                && password.length() <= PASSWORD_MAX;
    }
}
