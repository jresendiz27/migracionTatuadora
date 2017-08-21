package mx.blick.migracion

import java.text.Normalizer

class StringUtil {
    static String sanitizeString(String string) {
        String sanitizedString = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        sanitizedString = sanitizedString.trim()
        sanitizedString = sanitizedString.replaceAll("\\.+", ".")
        sanitizedString = sanitizedString.replaceAll("[\\\\|/|:|\\*|\\?|\\<|\\>|\\|\\[|\\]|\\{|\\}|@|]", "_")
        return sanitizedString
    }

    static String capitalizeString(String currentString) {
        return currentString.tokenize(" ")*.toLowerCase()*.capitalize().join(" ")
    }

}
