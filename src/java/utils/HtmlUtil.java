package utils;

/** Minimal HTML escaping helper for JSP output. */
public final class HtmlUtil {
    private HtmlUtil() {}

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
