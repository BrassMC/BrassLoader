package io.github.brassmc.brassloader.boot.util;

public enum Environment {
    CLIENT,
    DEDICATED_SERVER;

    private static Environment current;

    static void setCurrent(Environment current) {
        Environment.current = current;
    }

    public static Environment current() {
        return current;
    }
}
