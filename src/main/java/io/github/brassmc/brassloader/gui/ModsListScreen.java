package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.brassmc.brassloader.boot.mods.ModContainer;
import io.github.brassmc.brassloader.gui.ModsList.FilterType;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ModsListScreen extends Screen {
    private static final List<Pair<String, String>> LICENSES = List.of(
            Pair.of("AGPLv3", "https://choosealicense.com/licenses/agpl-3.0/"),
            Pair.of("GPLv3", "https://choosealicense.com/licenses/gpl-3.0/"),
            Pair.of("LGPLv3", "https://choosealicense.com/licenses/lgpl-3.0/"),
            Pair.of("Mozilla Public License 2.0", "https://choosealicense.com/licenses/mpl-2.0/"),
            Pair.of("MPL", "https://choosealicense.com/licenses/mpl-2.0/"),
            Pair.of("Apache License 2.0", "https://choosealicense.com/licenses/apache-2.0/"),
            Pair.of("MIT", "https://choosealicense.com/licenses/mit/"),
            Pair.of("Boost Software License 1.0", "https://choosealicense.com/licenses/bsl-1.0/"),
            Pair.of("BSL", "https://choosealicense.com/licenses/bsl-1.0/"),
            Pair.of("The Unlicense", "https://choosealicense.com/licenses/unlicense/"),
            Pair.of("ARR", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
            Pair.of("All Rights Reserved", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    );

    protected final Screen lastScreen;
    protected EditBox searchBox;
    protected ModsList list;
    protected Button filterButton;
    protected Button direction;

    private static final int BOX_PADDING = 10;

    public ModsListScreen(Screen lastScreen) {
        super(Component.translatable("brassloader.modsList.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);

        this.list = new ModsList(this, this.minecraft, this.width / 2, this.height, 20, this.height - 40, 20, this.width / 2 - 20, 32);

        this.searchBox = new EditBox(this.font, this.width / 2 - 120, this.height - 25, 100, 20, this.searchBox, Component.translatable("brassloader.modsList.search"));
        this.searchBox.setResponder(str -> {
            this.list.updateFilter(str);
            this.searchBox.setSuggestion(str.isBlank() ? "e.g. Brass API" : "");
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
        if (this.list.getSelected() != null) {
            ModContainer mod = this.list.getSelected().getMod();
            if (isMouseOver((float) mouseX, (float) mouseY, getBoxX(), getBoxY(), getBoxWidth(), getBoxHeight())) {
                if (isMouseOver((float) mouseX, (float) mouseY, getBoxX() + BOX_PADDING, getBoxY() + BOX_PADDING + 50 + (this.font.wordWrapHeight(mod.description(), getBoxWidth() - (BOX_PADDING * 2))) + BOX_PADDING, this.font.width(mod.license()), this.font.lineHeight)) {
                    String license = mod.license().trim();

                    String url = "";
                    for (Pair<String, String> licenseEntry : LICENSES) {
                        if (license.contains(licenseEntry.getKey())) {
                            url = licenseEntry.getValue();
                            break;
                        }
                    }

                    if (url.isBlank())
                        return false;

                    String finalUrl = url;
                    Objects.requireNonNull(this.minecraft).setScreen(new ConfirmLinkScreen(result -> {
                        if (result) {
                            Util.getPlatform().openUri(finalUrl);
                        }

                        this.minecraft.setScreen(ModsListScreen.this);
                    }, url, true));
                    return true;
                }

                return false;
            }
        }

        return super.mouseClicked(mouseX, mouseY, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) || this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int modifiers, double $$3, double $$4) {
        return super.mouseDragged(mouseX, mouseY, modifiers, $$3, $$4);
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(this.minecraft).setScreen(this.lastScreen);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.searchBox.charTyped(codePoint, modifiers);
    }

    public int getBoxPadding() {
        return BOX_PADDING;
    }

    public int getBoxX() {
        return this.width / 2 + (BOX_PADDING * 2);
    }

    public int getBoxY() {
        return 10;
    }

    public int getBoxX1() {
        return width - BOX_PADDING;
    }

    public int getBoxY1() {
        return height - BOX_PADDING;
    }

    public int getBoxWidth() {
        return getBoxX1() - getBoxX();
    }

    public int getBoxHeight() {
        return getBoxY1() - getBoxY();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(0);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(getBoxX(), getBoxY1(), 0.0).uv(0, 6).color(32, 32, 32, 255).endVertex();
        buffer.vertex(getBoxX1(), getBoxY1(), 0.0).uv(6, 6).color(32, 32, 32, 255).endVertex();
        buffer.vertex(getBoxX1(), getBoxY(), 0.0).uv(6, 0).color(32, 32, 32, 255).endVertex();
        buffer.vertex(getBoxX(), getBoxY(), 0.0).uv(0, 0).color(32, 32, 32, 255).endVertex();
        tesselator.end();

        super.render(poseStack, mouseX, mouseY, partialTicks);

        if (this.list.getSelected() != null) {
            ModContainer mod = this.list.getSelected().getMod();
            InformationPanel.Renderer renderer = InformationPanel.PANELS.get(mod.modid());

            if (renderer != null) {
                renderer.render(this, poseStack, mouseX, mouseY, partialTicks);
            } else {
                renderScrollbar(tesselator, buffer, getBoxX1() - 6, 200, 50, getBoxX(), getBoxY(), getBoxX1(), getBoxY1());

                poseStack.pushPose();
                poseStack.scale(1.5f, 1.5f, 1.5f);
                poseStack.translate((getBoxX() + getBoxWidth() / 2f) / 1.5f, getBoxY() + BOX_PADDING, 0f);
                GuiComponent.drawCenteredString(poseStack, this.font, mod.name(), 0, 0, 0xFFFFFF);
                poseStack.popPose();

                GuiComponent.drawCenteredString(poseStack, this.font, mod.modid(), getBoxX() + getBoxWidth() / 2, getBoxY() + 35, 0xBABABA);
                GuiComponent.drawCenteredString(poseStack, this.font, mod.version(), getBoxX() + getBoxWidth() / 2, getBoxY() + 45, 0x929292);

                List<FormattedCharSequence> description = Language.getInstance().getVisualOrder(this.font.getSplitter().splitLines(mod.description(), getBoxWidth() - 20, Style.EMPTY));
                int descriptionHeight = description.size() * this.font.lineHeight;
                for (FormattedCharSequence line : description) {
                    int lineY = getBoxY() + BOX_PADDING + 45 + 10 + (this.font.lineHeight * description.indexOf(line));
                    if (lineY > getBoxY() && lineY < getBoxY() + getBoxHeight() - this.font.lineHeight) {
                        GuiComponent.drawString(poseStack, this.font, line, getBoxX() + BOX_PADDING, lineY, 0xFFFFFF);
                    }
                }

                MutableComponent license = Component.literal(mod.license());
                if (isMouseOver(mouseX, mouseY, getBoxX() + BOX_PADDING, getBoxY() + BOX_PADDING + 50 + descriptionHeight + BOX_PADDING, this.font.width(mod.license()), this.font.lineHeight)) {
                    license = license.withStyle(ChatFormatting.UNDERLINE);
                }

                GuiComponent.drawString(poseStack, this.font, license, getBoxX() + BOX_PADDING, getBoxY() + BOX_PADDING + 50 + descriptionHeight + BOX_PADDING, 0xFFFFFF);

                GuiComponent.drawString(poseStack, this.font, Component.translatable("brassloader.modsList.developers"), getBoxX() + BOX_PADDING, getBoxY() + BOX_PADDING + 50 + descriptionHeight + BOX_PADDING, 0xFFFFFF);
            }
        }

        RenderSystem.enableBlend();
        renderBottom(tesselator, buffer, getBoxX(), getBoxY(), getBoxX1(), getBoxY1());
        renderTop(tesselator, buffer, getBoxX(), getBoxY(), getBoxY1(), getBoxWidth(), getBoxHeight());
        RenderSystem.disableBlend();

        GuiComponent.drawCenteredString(poseStack, this.font, this.title, this.width / 4, 8, 0xFFFFFF);
        GuiComponent.drawString(poseStack, this.font, Component.translatable("brassloader.modsList.filter").append(":"), 25, this.height - 40 + this.font.lineHeight / 2, 0xFFFFFF);
    }

    private static boolean isMouseOver(float mouseX, float mouseY, int x, int y, int width, int height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    private void renderTop(Tesselator tesselator, BufferBuilder buffer, int x0, int y0, int y1, int width, int height) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(x0, y0, -100.0).uv(0.0F, (float) y0 / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((x0 + width), y0, -100.0).uv((float) width / 32.0F, (float) y0 / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((x0 + width), 0.0, -100.0).uv((float) width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex(x0, 0.0, -100.0).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex(x0, height, -100.0).uv(0.0F, (float) height / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((x0 + width), height, -100.0).uv((float) width / 32.0F, (float) height / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((x0 + width), y1, -100.0).uv((float) width / 32.0F, (float) y1 / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex(x0, y1, -100.0).uv(0.0F, (float) y1 / 32.0F).color(64, 64, 64, 255).endVertex();
        tesselator.end();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
    }

    private void renderBottom(Tesselator tesselator, BufferBuilder buffer, int x0, int y0, int x1, int y1) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(x0, (y0 + 4), 0.0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(x1, (y0 + 4), 0.0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(x1, y0, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(x0, y0, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(x0, y1, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(x1, y1, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(x1, (y1 - 4), 0.0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(x0, (y1 - 4), 0.0).color(0, 0, 0, 0).endVertex();
        tesselator.end();
    }

    @Override
    public void removed() {
        if (this.list != null) {
            this.list.children().forEach(ModsList.ModListEntry::close);
        }
    }

    private static void renderScrollbar(Tesselator tesselator, BufferBuilder buffer, int left, int maxScroll, int scrollAmount, int x0, int y0, int x1, int y1) {
        int right = left + 6;

        if (maxScroll > 0) {
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int clampedMax = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) maxScroll);
            clampedMax = Mth.clamp(clampedMax, 32, y1 - y0 - 8);
            int relativeAmount = scrollAmount * (y1 - y0 - clampedMax) / maxScroll + y0;
            if (relativeAmount < y0) {
                relativeAmount = y0;
            }

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(left, y1, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(right, y1, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(right, y0, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(left, y0, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(left, (relativeAmount + clampedMax), 0.0).color(128, 128, 128, 255).endVertex();
            buffer.vertex(right, (relativeAmount + clampedMax), 0.0).color(128, 128, 128, 255).endVertex();
            buffer.vertex(right, relativeAmount, 0.0).color(128, 128, 128, 255).endVertex();
            buffer.vertex(left, relativeAmount, 0.0).color(128, 128, 128, 255).endVertex();
            buffer.vertex(left, (relativeAmount + clampedMax - 1), 0.0).color(192, 192, 192, 255).endVertex();
            buffer.vertex((right - 1), (relativeAmount + clampedMax - 1), 0.0).color(192, 192, 192, 255).endVertex();
            buffer.vertex((right - 1), relativeAmount, 0.0).color(192, 192, 192, 255).endVertex();
            buffer.vertex(left, relativeAmount, 0.0).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }
    }

    @Override
    public void tick() {
        this.searchBox.tick();
    }
}
