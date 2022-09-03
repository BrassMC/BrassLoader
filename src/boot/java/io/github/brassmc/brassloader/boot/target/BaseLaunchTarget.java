package io.github.brassmc.brassloader.boot.target;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import cpw.mods.modlauncher.api.ServiceRunner;
import io.github.brassmc.brassloader.boot.discovery.ModDiscovery;
import io.github.brassmc.brassloader.boot.util.Environment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Method;

public abstract class BaseLaunchTarget implements ILaunchHandlerService {
    protected BaseLaunchTarget(Environment environment) {
        try {
            final var method = Environment.class.getDeclaredMethod("setCurrent", Environment.class);
            method.setAccessible(true);
            method.invoke(null, environment);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to set environment", exception);
        }
    }

    @Override
    @SuppressWarnings({"removal"})
    public final void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {

    }

    @Override
    public ServiceRunner launchService(String[] arguments, ModuleLayer gameLayer) {
        return () -> {
            Mixins.addConfiguration("brassloader.mixins.json");
            ModDiscovery.getMods().forEach(it -> Mixins.addConfigurations(it.mixins()));
            Class<?> mainClass = Class.forName(gameLayer.findModule("minecraft").orElseThrow(),getMainClassName());
            Method mainMethod = mainClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object)arguments);
        };
    }

    protected abstract String getMainClassName();
}
