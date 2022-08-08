package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.brassmc.brassloader.util.HoveredProvider;
import io.github.brassmc.brassloader.util.ModSummary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

public class ModsList extends ObjectSelectionList<ModsList.ModListEntry> {
    private final ModsListScreen screen;
    private String filter = "";
    @Nullable
    private List<ModSummary> currentlyDisplayed;
    private CompletableFuture<List<ModSummary>> pending;
    private FilterDirection filterDirection = FilterDirection.NONE;
    private FilterType filterType = FilterType.NONE;

    private final List<ModSummary> items = new ArrayList<>();

    public ModsList(ModsListScreen screen, Minecraft minecraft, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
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
        this.pending = null;
    }

    public FilterDirection getFilterDirection() {
        return this.filterDirection;
    }

    public void switchDirection(Button button) {
        if(this.filterType != FilterType.NONE) {
            this.filterDirection = getFilterDirection().next(this.filterType != FilterType.NONE);
            if (this.filterType == FilterType.ALPHABETICAL) {
                this.screen.filterButton.setMessage(this.filterDirection == FilterDirection.ASCENDING ? Component.literal("A-Z") : Component.literal("Z-A"));
            }

            filterUsingType();
        } else {
            this.filterDirection = FilterDirection.NONE;
        }
    }

    public void switchFilter(Button button) {
        this.filterType = getFilterType().next();
        button.setMessage(getFilterType().getName());

        if(this.filterType != FilterType.NONE) {
            if (this.filterDirection == FilterDirection.NONE) {
                switchDirection(this.screen.direction);
            } else {
                filterUsingType();
            }

            if (this.filterType == FilterType.ALPHABETICAL) {
                button.setMessage(this.filterDirection == FilterDirection.ASCENDING ? Component.literal("A-Z") : Component.literal("Z-A"));
            }

            this.screen.direction.active = true;
        } else {
            this.filterDirection = FilterDirection.NONE;
            this.screen.direction.setMessage(getFilterDirection().asLiteral());
            this.screen.direction.active = false;

            clearEntries();
            this.currentlyDisplayed = new ArrayList<>();
            this.items.forEach(item -> {
                addEntry(new ModListEntry(this, item));
                this.currentlyDisplayed.add(item);
            });
            notifyListUpdated();
        }
    }

    private void filterUsingType() {
        clearEntries();

        if(this.filterDirection == FilterDirection.ASCENDING) {
            this.currentlyDisplayed = this.filterType.ascendingSorter.apply(this.currentlyDisplayed);
        } else {
            this.currentlyDisplayed = this.filterType.descendingSorter.apply(this.currentlyDisplayed);
        }

        this.currentlyDisplayed.forEach(modSummary -> addEntry(new ModListEntry(this, modSummary)));

        notifyListUpdated();
    }

    public FilterType getFilterType() {
        return this.filterType;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int keyCode) {
        Optional<GuiEventListener> clicked = getChildAt(mouseX, mouseY);
        if(clicked.isPresent() && clicked.get() instanceof ModListEntry entry) {
            setSelected(entry);
            return super.mouseClicked(mouseX, mouseY, keyCode);
        }

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
        return this.y0 - (int)getScrollAmount() + (index * (this.itemHeight)) + 5;
    }

    @Override
    public boolean mouseDragged(double $$0, double $$1, int $$2, double $$3, double $$4) {
        return super.mouseDragged($$0, $$1, $$2, $$3, $$4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        List<ModSummary> modSummaries = pollMods();
        if(modSummaries != this.currentlyDisplayed && modSummaries != null) {
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

    @Override
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    protected void renderItem(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks, int index, int top, int left, int width, int height) {
        ModListEntry entry = getEntry(index);
        if (isSelectedItem(index)) {
            renderSelection(stack, top, left, width, height);
        }

        entry.render(stack, index, top, left, width, height, mouseX, mouseY, Objects.equals(getHovered(), entry), partialTicks);
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return Objects.equals(getSelected(), getEntry(index));
    }

    @Override
    public @NotNull Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        if(mouseX < x0 || mouseX > x1 || mouseY < y0 || mouseY > y1)
            return super.getChildAt(mouseX, mouseY);

        for(int index = 0; index < getItemCount(); index++) {
            ModListEntry entry = getEntry(index);
            if(mouseY > getRowTop(index) && mouseY < getRowTop(index) + this.itemHeight) {
                return Optional.of(entry);
            }
        }

        return super.getChildAt(mouseX, mouseY);
    }

    protected void renderSelection(@NotNull PoseStack stack, int top, int left, int width, int height) {
        fill(stack, left, top - 1, left + width, top + height + 1, 0xFF808080);
        fill(stack, left + 1, top, left + width - 1, top + height, 0xFF121212);
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
        return this.width - this.x0;
    }

    @Nullable
    private List<ModSummary> pollMods() {
        try {
            return this.pending.getNow(null);
        } catch (CancellationException | CompletionException | NullPointerException exception) {
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

        filterUsingType();

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
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.summary.getIcon());
            blit(poseStack, left + height / 2 - 12, top + height / 2 - 12, 0, 0, 24, 24, 24, 24);

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

    public enum FilterDirection {
        ASCENDING("▲"),
        NONE("-"),
        DESCENDING("▼");

        private final String symbol;

        FilterDirection(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return this.symbol;
        }

        public Component asLiteral() {
            return Component.literal(getSymbol());
        }

        public FilterDirection next() {
            int ordinal = ordinal();
            if(ordinal + 1 < values().length)
                return values()[ordinal + 1];
            return values()[0];
        }

        public FilterDirection next(boolean skipNone) {
            if(!skipNone) {
                return next();
            }

            int ordinal = ordinal();

            if(ordinal + 1 == FilterDirection.NONE.ordinal()) {
                ordinal++;
            }

            if(ordinal + 1 < values().length)
                return values()[ordinal + 1];
            return values()[0];
        }
    }

    public enum FilterType {
        ALPHABETICAL("brassloader.modsList.filter.alphabetical",
                mods -> mods.stream().sorted(Comparator.comparing(ModSummary::getName)).toList(),
                mods -> mods.stream().sorted(Comparator.comparing(ModSummary::getName).reversed()).toList()
        ),
        NONE("brassloader.modsList.filter.none", mods -> mods, mods -> mods);

        private final Component name;
        private final UnaryOperator<List<ModSummary>> ascendingSorter, descendingSorter;

        FilterType(Component name, UnaryOperator<List<ModSummary>> ascendingSorter, UnaryOperator<List<ModSummary>> descendingSorter) {
            this.name = name;
            this.ascendingSorter = ascendingSorter;
            this.descendingSorter = descendingSorter;
        }

        FilterType(String name, UnaryOperator<List<ModSummary>> ascendingSorter, UnaryOperator<List<ModSummary>> descendingSorter) {
            this(Component.translatable(name), ascendingSorter, descendingSorter);
        }

        public Component getName() {
            return this.name;
        }

        public List<ModSummary> getAscendingSorter(List<ModSummary> modSummaries) {
            return this.ascendingSorter.apply(modSummaries);
        }

        public List<ModSummary> getDescendingSorter(List<ModSummary> modSummaries) {
            return this.descendingSorter.apply(modSummaries);
        }

        public FilterType next() {
            int ordinal = ordinal();
            if(ordinal + 1 < values().length)
                return values()[ordinal + 1];
            return values()[0];
        }
    }
}
