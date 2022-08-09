package io.github.brassmc.brassloader.mixin;

import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    /**
     * @author BrassMc
     * @reason change server mod name to brass
     */
    @DontObfuscate
    @Overwrite(remap = false)
    public String getServerModName() {
        return "brass";
    }
}
