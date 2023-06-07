package io.github.brassmc.brassloader.mixin;

import io.github.brassmc.brassloader.gui.ModsListScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void brass$addModsButton(CallbackInfo ci) {
        addRenderableWidget(Button.builder(Component.translatable("brassloader.menu.mods"), button -> {
                    if(minecraft != null) {
                        minecraft.setScreen(new ModsListScreen(this));
                    }
                })
                .pos(this.width / 2 - 102, this.height / 4 + 104)
                .size(204, 20)
                .build());
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> @NotNull T addRenderableWidget(@NotNull T widget) {
        if (widget instanceof Button button && button.getMessage() instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents contents) {
            if (contents.getKey().equals("menu.returnToMenu") || contents.getKey().equals("menu.disconnect")) {
                button.setY(button.getY() + 24);
            }
        }

        return super.addRenderableWidget(widget);
    }
}
