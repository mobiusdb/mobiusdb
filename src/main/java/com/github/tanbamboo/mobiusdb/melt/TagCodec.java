package com.github.tanbamboo.mobiusdb.melt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

final class TagCodec {
    private TagCodec() {
    }

    static String encodeTags(Map<String, String> tags) {
        if (tags.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            joiner.add(urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()));
        }
        return joiner.toString();
    }

    static Map<String, String> decodeTags(String encoded) {
        Map<String, String> tags = new LinkedHashMap<>();
        if (encoded == null || encoded.isBlank()) {
            return tags;
        }
        String[] pairs = encoded.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = urlDecode(parts[0]);
            String value = parts.length > 1 ? urlDecode(parts[1]) : "";
            tags.put(key, value);
        }
        return tags;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }
}
