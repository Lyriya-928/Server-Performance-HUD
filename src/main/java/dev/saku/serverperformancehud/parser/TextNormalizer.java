package dev.saku.serverperformancehud.parser;

import java.text.Normalizer;

public final class TextNormalizer {
    private TextNormalizer() {}
    public static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
    }
}

