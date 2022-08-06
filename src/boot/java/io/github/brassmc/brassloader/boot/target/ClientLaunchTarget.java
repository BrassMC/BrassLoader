package io.github.brassmc.brassloader.boot.target;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import io.github.brassmc.brassloader.boot.util.Environment;

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
    protected String getMainClassName() {
        return "net.minecraft.client.main.Main";
    }
}
