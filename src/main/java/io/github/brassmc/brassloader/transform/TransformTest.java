package io.github.brassmc.brassloader.transform;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

@AutoService(ILaunchPluginService.class)
public class TransformTest implements ILaunchPluginService {
    @Override
    public String name() {
        return "test";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.of(Phase.BEFORE);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        System.out.println("Tried transforming class: " + classNode.name);
        return false;
    }
}
