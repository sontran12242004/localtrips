package utils;

public final class PasswordValidator {

    private PasswordValidator() {
    }

    public static String validate(String password) {

        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống.";
        }

        if (password.length() < 8) {
            return "Mật khẩu phải có ít nhất 8 ký tự.";
        }

        if (password.length() > 64) {
            return "Mật khẩu không được vượt quá 64 ký tự.";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Mật khẩu phải có ít nhất một chữ hoa.";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Mật khẩu phải có ít nhất một chữ thường.";
        }

        if (!password.matches(".*[0-9].*")) {
            return "Mật khẩu phải có ít nhất một chữ số.";
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            return "Mật khẩu phải có ít nhất một ký tự đặc biệt.";
        }

        return null;
    }
}