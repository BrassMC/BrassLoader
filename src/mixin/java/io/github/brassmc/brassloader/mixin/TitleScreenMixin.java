package io.github.brassmc.brassloader.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component $$0) {
        super($$0);
    }

    @ModifyVariable(at = @At("STORE"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V")
    private String brass$changeTitle(String value) {
        return "Minecraft " + SharedConstants.getCurrentVersion().getName() + " / Brass (Modded)";
    }
}
