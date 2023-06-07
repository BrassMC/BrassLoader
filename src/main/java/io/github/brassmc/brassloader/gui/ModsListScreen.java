package io.github.brassmc.brassloader.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModsListScreen extends Screen {
    protected final Screen lastScreen;
    protected EditBox searchBox;
    protected ModsList list;

    public ModsListScreen(Screen lastScreen) {
        super(Component.translatable("brass.modsList.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox,
                Component.translatable("brass.modsList.search"));
        this.searchBox.setResponder(str -> this.list.updateFilter(str));

        this.list = new ModsList(this, this.minecraft, 0, 0, this.width, this.height, 36, this.list);
        addWidget(this.searchBox);
        addWidget(this.list);

        setInitialFocus(this.searchBox);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.list.render(graphics, mouseX, mouseY, partialTicks);
        this.searchBox.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTicks);
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
