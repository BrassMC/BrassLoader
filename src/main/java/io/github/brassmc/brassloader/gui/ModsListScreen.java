package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ModsListScreen extends Screen {
    protected final Screen lastScreen;
    protected EditBox searchBox;
    protected ModsList list;
    protected Button azFilter;
    protected Button zaFilter;

    public ModsListScreen(Screen lastScreen) {
        super(Component.translatable("brassloader.modsList.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.searchBox = new EditBox(this.font, (this.width / 4) - 15, this.height - 25, 100, 20, this.searchBox,
                Component.translatable("brassloader.modsList.search"));
        this.searchBox.setResponder(str -> {
            this.list.updateFilter(str);
            this.searchBox.setSuggestion(str.isBlank() ? "e.g. Brass API": "");
        });
        this.searchBox.setSuggestion("e.g. Brass API");

        this.list = new ModsList(
                this,
                this.minecraft,
                this.width / 2,
                this.height,
                20,
                this.height - 40,
                20,
                this.width / 2 - 20,
                32
        );

        this.azFilter = new Button(25, this.height - 25, 25, 20, Component.literal("A-Z"),btn -> {});
        this.zaFilter = new Button(55, this.height - 25, 25, 20, Component.literal("Z-A"),btn -> {});

        addRenderableWidget(this.list);
        addRenderableWidget(this.searchBox);
        addRenderableWidget(this.azFilter);
        addRenderableWidget(this.zaFilter);

        setInitialFocus(this.searchBox);
    }

    @Override
    public boolean mouseClicked(double $$0, double $$1, int $$2) {
        return super.mouseClicked($$0, $$1, $$2);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) || this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.searchBox.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(0);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        GuiComponent.drawCenteredString(poseStack, this.font, this.title, this.width / 4, 8, 0xFFFFFF);
        GuiComponent.drawString(poseStack, this.font, Component.translatable("brassloader.modsList.filter").append(":"), 25, this.height - 40 + this.font.lineHeight / 2, 0xFFFFFF);
    }

    @Override
    public void removed() {
        if(this.list != null) {
            this.list.children().forEach(ModsList.ModListEntry::close);
        }
    }

    @Override
    public void tick() {
        this.searchBox.tick();
    }
}
