package io.github.brassmc.brassloader.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Redirect(method = "getGameInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getLaunchedVersion()Ljava/lang/String;"))
    private String brass$getLaunchedVersion(Minecraft instance) {
        String brassApiVersion = "0.0.1";
        return "brass-" + brassApiVersion;
    }
}
