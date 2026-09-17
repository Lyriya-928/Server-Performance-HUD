package dev.saku.serverperformancehud.parser;

import dev.saku.serverperformancehud.metrics.ServerMetrics;

public sealed interface ParseResult permits ParseResult.InProgress, ParseResult.Success, ParseResult.Failure {
    record InProgress() implements ParseResult {}
    record Success(ServerMetrics metrics) implements ParseResult {}
    record Failure(ParseError error) implements ParseResult {}
}

