package io.github.brassmc.brassloader.launcher;

import io.github.brassmc.brassloader.BrassLoader;
import io.github.brassmc.brassloader.environment.Environment;

public class ClientLauncher {
    public static void main(String[] args) {
        var loader = new BrassLoader(Environment.CLIENT);
        BrassLoader.setInstance(loader);
        BrassLoader.getInstance().launch(args);
    }
}