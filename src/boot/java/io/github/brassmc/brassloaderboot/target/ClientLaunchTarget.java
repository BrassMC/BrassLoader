package io.github.brassmc.brassloaderboot.target;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import cpw.mods.modlauncher.api.ServiceRunner;

@AutoService(ILaunchHandlerService.class)
public class ClientLaunchTarget implements ILaunchHandlerService {
    @Override
    public String name() {
        return "brass:mcclient";
    }

    @SuppressWarnings("removal")
    @Override
    public void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {

    }

    @Override
    public ServiceRunner launchService(String[] arguments, ModuleLayer gameLayer) {
        return () -> {
            Class.forName(gameLayer.findModule("minecraft").orElseThrow(),"net.minecraft.client.main.Main").getMethod("main", String[].class).invoke(null, (Object)arguments);
        };
    }
}
