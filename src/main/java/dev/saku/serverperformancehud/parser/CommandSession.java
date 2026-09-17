package dev.saku.serverperformancehud.parser;

import java.util.concurrent.atomic.AtomicLong;

public final class CommandSession {
    private static final AtomicLong IDS = new AtomicLong();
    private final long sessionId = IDS.incrementAndGet();
    private final long startTime;
    public CommandSession(long startTime) { this.startTime = startTime; }
    public long sessionId() { return sessionId; }
    public long startTime() { return startTime; }
    public boolean accepts(long receivedAt) { return receivedAt >= startTime; }
}
