package controller.auth;

import dao.RememberMeDAO;
import model.User;
import utils.TokenUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/** Helper for issuing and clearing the remember-me cookie. */
public final class RememberMeUtil {
    public static final String COOKIE_NAME = "LOCALTRIP_REMEMBER";
    private static final int MAX_AGE_SECONDS = (int) TimeUnit.DAYS.toSeconds(30);
    private static final RememberMeDAO DAO = new RememberMeDAO();

    private RememberMeUtil() { }

    public static void create(HttpServletRequest request, HttpServletResponse response, User user) {
        String rawToken = TokenUtil.generateToken();
        long expires = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30);
        DAO.createToken(user.getUserId(), TokenUtil.sha256(rawToken), expires);

        Cookie cookie = new Cookie(COOKIE_NAME, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(MAX_AGE_SECONDS);
        cookie.setPath(contextCookiePath(request));
        response.addCookie(cookie);
    }

    public static User restore(HttpServletRequest request) {
        Cookie cookie = findCookie(request);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isEmpty()) return null;
        return DAO.findUserByTokenHash(TokenUtil.sha256(cookie.getValue()));
    }

    public static void revoke(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request);
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
            DAO.deleteToken(TokenUtil.sha256(cookie.getValue()));
        }
        clearCookie(request, response);
    }

    private static Cookie findCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) return cookie;
        }
        return null;
    }

    private static void clearCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(0);
        cookie.setPath(contextCookiePath(request));
        response.addCookie(cookie);
    }

    private static String contextCookiePath(HttpServletRequest request) {
        String context = request.getContextPath();
        return context == null || context.isEmpty() ? "/" : context;
    }
}
