package io.github.brassmc.brassloader.boot.target;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import io.github.brassmc.brassloader.boot.util.Environment;

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
    protected String getMainClassName() {
        return "net.minecraft.server.Main";
    }
}
