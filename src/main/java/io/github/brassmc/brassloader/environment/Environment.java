package io.github.brassmc.brassloader.environment;

/**
 * The current environment the loader is running in.
 */
public enum Environment {
    /**
     * CLIENT: Indicates this is a Minecraft client, started from the Minecraft Launcher.
     */
    CLIENT,
    /**
     * DEDICATED_SERVER: Indicates this is a Minecraft server, started from a server Jar.
     */
    DEDICATED_SERVER;
}
