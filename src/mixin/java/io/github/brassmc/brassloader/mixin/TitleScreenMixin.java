package io.github.brassmc.brassloader.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component $$0) {
        super($$0);
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;drawString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"),
            method = "render"
    )
    private void brass$changeText(PoseStack matrixStack, Font font, String s, int i, int j, int k) {
        String text = "Minecraft " + SharedConstants.VERSION_STRING + " / Brass (Modded)";
        drawString(matrixStack, this.font, text, 2, this.height - 10, 16777215);
    }
}
