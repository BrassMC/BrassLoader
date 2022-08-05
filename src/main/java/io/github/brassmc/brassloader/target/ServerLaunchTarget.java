package io.github.brassmc.brassloader.target;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ServiceRunner;
import io.github.brassmc.brassloader.util.Environment;

@AutoService(ILaunchHandlerService.class)
public class ServerLaunchTarget extends BaseLaunchTarget {
    public ServerLaunchTarget() {
        super(Environment.DEDICATED_SERVER);
    }

    @Override
    public String name() {
        return "brass:mcserver";
    }

    @Override
    public ServiceRunner launchService(String[] arguments, ModuleLayer gameLayer) {
        return () -> {
            final var mcClass = Class.forName(gameLayer.findModule("minecraft").orElseThrow(), "net.minecraft.server.Main");
            mcClass.getMethod("main", String[].class).invoke(null, (Object) arguments);
        };
    }
}
