package io.github.brassmc.brassloader.target;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ServiceRunner;
import io.github.brassmc.brassloader.util.Environment;

import java.lang.reflect.Method;

@AutoService(ILaunchHandlerService.class)
public class ClientLaunchTarget extends BaseLaunchTarget {
    public ClientLaunchTarget() {
        super(Environment.CLIENT);
    }

    @Override
    public String name() {
        return "brass:mcclient";
    }

    @Override
    public ServiceRunner launchService(String[] arguments, ModuleLayer gameLayer) {
        return () -> {
            Class<?> mainClass = Class.forName(gameLayer.findModule("minecraft").orElseThrow(),"net.minecraft.client.main.Main");
            Method mainMethod = mainClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object)arguments);
        };
    }
}
