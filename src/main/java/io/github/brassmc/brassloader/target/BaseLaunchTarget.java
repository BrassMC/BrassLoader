package io.github.brassmc.brassloader.target;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import io.github.brassmc.brassloader.util.Environment;

public abstract class BaseLaunchTarget implements ILaunchHandlerService {
    protected BaseLaunchTarget(Environment environment) {
        try {
            final var method = Environment.class.getDeclaredMethod("setCurrent", Environment.class);
            method.setAccessible(true);
            method.invoke(null, environment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set environment", e);
        }
    }

    @Override
    @SuppressWarnings({"removal"})
    public final void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {

    }
}
