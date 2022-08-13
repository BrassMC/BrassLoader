package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import io.github.brassmc.brassloader.gui.ModsList.FilterDirection;
import io.github.brassmc.brassloader.gui.ModsList.FilterType;

import java.util.Objects;

public class ModsListScreen extends Screen {
    private static final ResourceLocation FILTER_DIRECTION = new ResourceLocation("brassloader", "textures/gui/filter_direction.png");
    protected final Screen lastScreen;
    protected EditBox searchBox;
    protected ModsList list;
    protected Button filterButton;
    protected Button direction;

    public ModsListScreen(Screen lastScreen) {
        super(Component.translatable("brassloader.modsList.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);

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

        this.searchBox = new EditBox(this.font, this.width / 2 - 120, this.height - 25, 100, 20, this.searchBox,
                Component.translatable("brassloader.modsList.search"));
        this.searchBox.setResponder(str -> {
            this.list.updateFilter(str);
            this.searchBox.setSuggestion(str.isBlank() ? "e.g. Brass API": "");
        });
        this.searchBox.setSuggestion("e.g. Brass API");

        this.filterButton = new Button(25, this.height - 25, this.width / 2 - 170, 20, FilterType.NONE.getName(), this.list::switchFilter);
        this.direction = new DirectionButton(this.width / 2 - 145, this.height - 25, 20, 20, this.list::switchDirection, this.list);
        this.direction.active = false;

        addRenderableWidget(this.list);
        addRenderableWidget(this.searchBox);
        addRenderableWidget(this.filterButton);
        addRenderableWidget(this.direction);

        setInitialFocus(this.searchBox);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int modifiers) {
        return super.mouseClicked(mouseX, mouseY, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) || this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(this.minecraft).setScreen(this.lastScreen);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.searchBox.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(0);
        fill(poseStack, width / 2 + 20, 10, width - 10, height - 10, 0xFF8B8B8B);
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
