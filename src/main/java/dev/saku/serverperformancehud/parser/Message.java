package dev.saku.serverperformancehud.parser;

public record Message(String raw, String normalized, long receivedAt) {
    public static Message of(String raw, long receivedAt) {
        return new Message(raw, TextNormalizer.normalize(raw), receivedAt);
    }
}

