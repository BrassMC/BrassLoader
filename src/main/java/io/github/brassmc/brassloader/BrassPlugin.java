package io.github.brassmc.brassloader;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.EnumSet;

/**
 * Handles early configuration, as one of the first loaded
 * classes.
 */
@AutoService(ILaunchPluginService.class)
public class BrassPlugin implements ILaunchPluginService {

    static {
        MixinBootstrap.init();
        Mixins.addConfiguration("brassloader.mixins.json");
    }

    @Override
    public String name() {
        return "brass";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.noneOf(Phase.class);
    }
}
