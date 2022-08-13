package io.github.brassmc.brassloader.mixin;

import io.github.brassmc.brassloader.boot.discovery.InvalidEntrypointException;
import io.github.brassmc.brassloader.boot.discovery.ModDiscovery;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;freezeBuiltins()V"
            ),
            method = "bootStrap"
    )
    private static void init(CallbackInfo callback) {
        ModDiscovery.MODS.forEach(modContainer -> {
            String entrypoint = modContainer.entrypoint();
            try {
                Class<?> mainClass = Class.forName(entrypoint);
                Constructor<?> constructor = mainClass.getConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (ClassNotFoundException exception) {
                throw new InvalidEntrypointException("Entrypoint in mod [" + modContainer.modid() + "] points to an invalid class!");
            } catch (InvocationTargetException exception) {
                throw new InvalidEntrypointException("Entrypoint in mod [" + modContainer.modid() + "] threw an exception!");
            } catch (InstantiationException exception) {
                throw new InvalidEntrypointException("Entrypoint in mod [" + modContainer.modid() + "] is abstract!");
            } catch (IllegalAccessException exception) {
                throw new InvalidEntrypointException("Entrypoint in mod [" + modContainer.modid() + "] is not accessible!");
            } catch (NoSuchMethodException exception) {
                throw new InvalidEntrypointException("Entrypoint in mod [" + modContainer.modid() + "] does not contain a no-args constructor!");
            }
        });
    }
}
