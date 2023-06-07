package io.github.brassmc.brassloader.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @ModifyConstant(method = "getServerModName", constant = @Constant(stringValue = "vanilla"))
    public String brass$modifyServerBrandName(String original) {
        return "brass";
    }
}
