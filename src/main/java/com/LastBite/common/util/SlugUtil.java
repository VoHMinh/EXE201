package com.LastBite.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tiện ích tạo slug thân thiện URL từ tiếng Việt/Unicode.
 * <p>
 * Example: "Bánh Mì Sài Gòn" → "banh-mi-sai-gon"
 */
public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-{2,}");

    // Bảng chuyển ký tự tiếng Việt có dấu
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
     * Tạo slug từ chuỗi đầu vào.
     *
     * @param input the text to slugify (e.g. "Bánh Mì Sài Gòn")
     * @return URL-friendly slug (e.g. "banh-mi-sai-gon")
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "";

        String result = input.trim();

        // Chuyển ký tự tiếng Việt trước
        for (String[] mapping : VIETNAMESE_MAP) {
            result = result.replaceAll(mapping[0], mapping[1]);
        }

        // Chuẩn hóa Unicode
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        // Chuyển về chữ thường
        result = result.toLowerCase(Locale.ROOT);

        // Thay khoảng trắng bằng dấu gạch ngang
        result = WHITESPACE.matcher(result).replaceAll("-");

        // Xóa ký tự không thuộc latin
        result = NON_LATIN.matcher(result).replaceAll("");

        // Gộp nhiều dấu gạch ngang liên tiếp
        result = MULTIPLE_DASHES.matcher(result).replaceAll("-");

        // Xóa dấu gạch ngang ở đầu/cuối
        result = result.replaceAll("^-|-$", "");

        return result;
    }

    /**
     * Tạo slug duy nhất bằng cách thêm hậu tố nếu cần.
     */
    public static String toUniqueSlug(String input, java.util.function.Predicate<String> existsCheck) {
        String base = toSlug(input);
        if (!existsCheck.test(base)) return base;

        for (int i = 2; i <= 100; i++) {
            String candidate = base + "-" + i;
            if (!existsCheck.test(candidate)) return candidate;
        }
        // Dự phòng: thêm hậu tố ngẫu nhiên
        return base + "-" + java.util.UUID.randomUUID().toString().substring(0, 6);
    }
}
