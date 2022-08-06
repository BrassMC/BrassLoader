package io.github.brassmc.brassloader.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.brassmc.brassloader.gui.ModsListScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component $$0) {
        super($$0);
    }

    @ModifyVariable(at = @At("STORE"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V")
    private String brass$changeTitle(String value) {
        return "Minecraft " + SharedConstants.getCurrentVersion().getName() + " / Brass (Modded)";
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 2),
            method = "createNormalMenuOptions"
    )
    private GuiEventListener brass$addModsButton(TitleScreen instance, GuiEventListener guiEventListener) {
        Button button = (Button) guiEventListener;
        addRenderableWidget(new Button(button.x, button.y, button.getWidth() / 2 - 2, button.getHeight(),
                Component.translatable("brass.menu.mods"),
                btn -> this.minecraft.setScreen(new ModsListScreen(this))));
        button.x += button.getWidth() / 2 + 2;
        button.setWidth(button.getWidth() / 2);
        return addRenderableWidget(button);
    }
}
