package dev.saku.serverperformancehud.parser;

public interface CommandParser { ParseResult accept(Message message); void reset(); }

