package com.LastBite.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility to generate URL-friendly slugs from Vietnamese/Unicode text.
 * <p>
 * Example: "Bánh Mì Sài Gòn" → "banh-mi-sai-gon"
 */
public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-{2,}");

    // Vietnamese diacritics mapping
    private static final String[][] VIETNAMESE_MAP = {
            {"à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a"},
            {"è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e"},
            {"ì|í|ị|ỉ|ĩ", "i"},
            {"ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o"},
            {"ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u"},
            {"ỳ|ý|ỵ|ỷ|ỹ", "y"},
            {"đ", "d"},
            {"À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ", "A"},
            {"È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ", "E"},
            {"Ì|Í|Ị|Ỉ|Ĩ", "I"},
            {"Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ", "O"},
            {"Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ", "U"},
            {"Ỳ|Ý|Ỵ|Ỷ|Ỹ", "Y"},
            {"Đ", "D"}
    };

    private SlugUtil() {}

    /**
     * Generate a slug from the given input text.
     *
     * @param input the text to slugify (e.g. "Bánh Mì Sài Gòn")
     * @return URL-friendly slug (e.g. "banh-mi-sai-gon")
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "";

        String result = input.trim();

        // Replace Vietnamese characters first
        for (String[] mapping : VIETNAMESE_MAP) {
            result = result.replaceAll(mapping[0], mapping[1]);
        }

        // Standard Unicode normalization
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        // Lowercase
        result = result.toLowerCase(Locale.ROOT);

        // Replace whitespace with dash
        result = WHITESPACE.matcher(result).replaceAll("-");

        // Remove non-latin chars
        result = NON_LATIN.matcher(result).replaceAll("");

        // Collapse multiple dashes
        result = MULTIPLE_DASHES.matcher(result).replaceAll("-");

        // Trim leading/trailing dashes
        result = result.replaceAll("^-|-$", "");

        return result;
    }

    /**
     * Generate a unique slug by appending a suffix if needed.
     */
    public static String toUniqueSlug(String input, java.util.function.Predicate<String> existsCheck) {
        String base = toSlug(input);
        if (!existsCheck.test(base)) return base;

        for (int i = 2; i <= 100; i++) {
            String candidate = base + "-" + i;
            if (!existsCheck.test(candidate)) return candidate;
        }
        // Fallback: append random suffix
        return base + "-" + java.util.UUID.randomUUID().toString().substring(0, 6);
    }
}
