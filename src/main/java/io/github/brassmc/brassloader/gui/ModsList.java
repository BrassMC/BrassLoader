package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.brassmc.brassloader.util.ModSummary;
import io.github.brassmc.brassloader.util.HoveredProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class ModsList extends ObjectSelectionList<ModsList.ModListEntry> {
    private final ModsListScreen screen;
    private String filter = "";
    @Nullable
    private List<ModSummary> currentlyDisplayed;
    private CompletableFuture<List<ModSummary>> pending;

    private final List<ModSummary> items = new ArrayList<>();

    public ModsList(ModsListScreen screen,  Minecraft minecraft, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
        super(minecraft, 0, 0, 0, 0, itemHeight);
        this.width = width;
        this.height = height;
        this.x0 = left;
        this.x1 = right;
        this.y0 = top;
        this.y1 = bottom;

        this.screen = screen;
        setRenderTopAndBottom(false);

        this.pending = loadMods();
        handleNewMods(pollMods());
    }



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int keyCode) {
        setSelected((ModListEntry) getChildAt(mouseX, mouseY).orElse(null));
        return super.mouseClicked(mouseX, mouseY, keyCode);
    }

    @Override
    public int getRowLeft() {
        return this.x0;
    }

    @Override
    public int getRowRight() {
        return this.x1;
    }

    @Override
    protected int getRowTop(int index) {
        return this.y0 + (index * (this.itemHeight)) + 5;
    }

    @Override
    public boolean mouseDragged(double $$0, double $$1, int $$2, double $$3, double $$4) {
        return super.mouseDragged($$0, $$1, $$2, $$3, $$4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        List<ModSummary> modSummaries = pollMods();
        if(modSummaries != this.currentlyDisplayed) {
            handleNewMods(currentlyDisplayed);
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        renderBackground(tesselator, buffer);
        renderList(poseStack, mouseX, mouseY, partialTicks);
        renderTopAndBottom(tesselator, buffer);
        renderScrollbar(tesselator, buffer);

        HoveredProvider<ModsList.ModListEntry> access = (HoveredProvider<ModsList.ModListEntry>)this;
        access.brass$setHovered(this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderList(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        int left = getRowLeft();
        int right = getRowRight();
        int width = right - left;
        int height = this.itemHeight - 4;
        int count = getItemCount();

        for(int index = 0; index < count; ++index) {
            int top = getRowTop(index);
            int bottom = top + this.itemHeight;
            if (bottom >= this.y0 && top <= this.y1) {
                renderItem(stack, mouseX, mouseY, partialTicks, index, top, left, width, height);
            }
        }
    }

    protected void renderItem(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks, int index, int top, int left, int width, int height) {
        ModListEntry entry = getEntry(index);
        if (isSelectedItem(index)) {
            renderSelection(stack, top, left, width, height);
        }
        entry.render(stack, index, top, left, width, height, mouseX, mouseY, Objects.equals(getHovered(), entry), partialTicks);
    }

    protected void renderSelection(@NotNull PoseStack stack, int top, int left, int width, int height) {
        fill(stack, left - 1, top - 1, left + width + 1, top + height + 1, isFocused() ? 0 : 0xFF808080);
        fill(stack, left, top, left + width, top + height, 0xFF121212);
    }

    private void renderBackground(Tesselator tesselator, BufferBuilder buffer) {
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(this.x0, this.y1, 0.0).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        buffer.vertex(this.x1, this.y1, 0.0).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        buffer.vertex(this.x1, this.y0, 0.0).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        buffer.vertex(this.x0, this.y0, 0.0).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        tesselator.end();
    }

    private void renderTop(Tesselator tesselator, BufferBuilder buffer) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(this.x0, this.y0, -100.0).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((this.x0 + this.width), this.y0, -100.0).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((this.x0 + this.width), 0.0, -100.0).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex(this.x0, 0.0, -100.0).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex(this.x0, this.height, -100.0).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((this.x0 + this.width), this.height, -100.0).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex((this.x0 + this.width), this.y1, -100.0).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.vertex(this.x0, this.y1, -100.0).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
        tesselator.end();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
    }

    private void renderBottom(Tesselator tesselator, BufferBuilder buffer) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(this.x0, (this.y0 + 4), 0.0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(this.x1, (this.y0 + 4), 0.0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(this.x1, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(this.x0, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(this.x0, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(this.x1, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
        buffer.vertex(this.x1, (this.y1 - 4), 0.0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(this.x0, (this.y1 - 4), 0.0).color(0, 0, 0, 0).endVertex();
        tesselator.end();
    }

    private void renderTopAndBottom(Tesselator tesselator, BufferBuilder builder) {
        renderTop(tesselator, builder);
        renderBottom(tesselator, builder);
    }

    private void renderScrollbar(Tesselator tesselator, BufferBuilder buffer) {
        int left = this.getScrollbarPosition();
        int right = left + 6;

        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int clampedMax = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            clampedMax = Mth.clamp(clampedMax, 32, this.y1 - this.y0 - 8);
            int relativeAmount = (int)this.getScrollAmount() * (this.y1 - this.y0 - clampedMax) / maxScroll + this.y0;
            if (relativeAmount < this.y0) {
                relativeAmount = this.y0;
            }

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(left, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(right, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(right, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
            buffer.vertex(left, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
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
    protected int getScrollbarPosition() {
        return this.width - 6;
    }

    @Nullable
    private List<ModSummary> pollMods() {
        try {
            return this.pending.getNow(null);
        } catch (CancellationException | CompletionException exception) {
            return null;
        }
    }

    // TODO: Get from mod discovery
    private CompletableFuture<List<ModSummary>> loadMods() {
        List<ModSummary> modSummaryList = new ArrayList<>();
        for(int i = 0; i < 20; i++) {
            boolean isBeans = ThreadLocalRandom.current().nextBoolean();
            ModSummary summary = new ModSummary(isBeans ? "Beans Mod" : "Peas Mod",
                    isBeans ? new ResourceLocation("brassloader:beans.png") : new ResourceLocation("brassloader:peas.png"),
                    isBeans ? "A mod about beans!" : "A mod about peas!");
            modSummaryList.add(summary);
            this.items.add(summary);
        }

        return CompletableFuture.completedFuture(modSummaryList);
    }

    private void handleNewMods(@Nullable List<ModSummary> modSummaryList) {
        if(modSummaryList == null) {
            fillLoadingMods();
        } else {
            fillMods(this.filter);
        }
    }

    private void fillLoadingMods() {
        clearEntries();
        this.pending.getNow(List.of()).forEach(summary -> {
            addEntry(new ModListEntry(this, summary));
        });
        notifyListUpdated();
    }

    public void updateFilter(String str) {
        if(this.currentlyDisplayed != null && !str.equals(this.filter)) {
            this.fillMods(str);
        }

        this.filter = str;
    }

    private void fillMods(String filter) {
        this.clearEntries();
        filter = filter.toLowerCase(Locale.ROOT);

        if(this.currentlyDisplayed == null) {
            this.currentlyDisplayed = new ArrayList<>();
        } else {
            this.currentlyDisplayed.clear();
        }

        for (ModSummary modSummary : this.items) {
            if(matchesFilter(filter, modSummary)) {
                addEntry(new ModListEntry(this, modSummary));
                this.currentlyDisplayed.add(modSummary);
            }
        }

        notifyListUpdated();
    }

    private boolean matchesFilter(String filter, ModSummary modSummary) {
        return modSummary.getName().toLowerCase(Locale.ROOT).contains(filter);
    }

    private void notifyListUpdated() {
        this.screen.triggerImmediateNarration(true);
    }

    public static final class ModListEntry extends ObjectSelectionList.Entry<ModsList.ModListEntry> implements AutoCloseable {
        private final Minecraft minecraft;
        private final ModsListScreen screen;
        private final ModSummary summary;

        public ModListEntry(ModsList list,  ModSummary summary) {
            this.minecraft = list.minecraft;
            this.screen = list.screen;
            this.summary = summary;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("brassloader.narration.modList.entry", this.summary.getName());
        }

        @Override
        public void render(@NotNull PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            // TODO: Draw Icon
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.summary.getIcon());
            this.screen.blit(poseStack, left + height / 2 - 12, top + height / 2 - 12, 0, 0, 24, 24, 24, 24);

            this.minecraft.font.draw(
                    poseStack,
                    this.summary.getName(),
                    left + height / 2f - this.minecraft.font.lineHeight / 2f + 20,
                    top + height / 2f - this.minecraft.font.lineHeight / 2f,
                    0xFFFFFF
            );
        }

        public void close() {
        }
    }
}
