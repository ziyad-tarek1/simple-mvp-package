package com.mshrai.larer;

/**
 * Tiny example API for a library published to GitHub Packages.
 */
public final class DummyGreeter {

    private DummyGreeter() {
        throw new UnsupportedOperationException("utility class");
    }

    public static String greet(String name) {
        String n = name == null || name.isBlank() ? "world" : name.trim();
        return "Hello, " + n + "!";
    }
}
