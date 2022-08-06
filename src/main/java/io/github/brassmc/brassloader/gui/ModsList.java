package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.brassmc.brassloader.boot.mods.ModSummary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ModsList extends ObjectSelectionList<ModsList.ModListEntry> {
    private final ModsListScreen screen;
    private String filter = "";
    @Nullable
    private List<ModSummary> currentlyDisplayed;
    private CompletableFuture<List<ModSummary>> pending;

    public ModsList(ModsListScreen screen,  Minecraft minecraft, int xPos, int yPos, int width, int height, int itemHeight, @Nullable ModsList modsList) {
        super(minecraft, xPos, yPos, width, height, itemHeight);
        this.screen = screen;

        if(modsList != null) {
            this.pending = modsList.pending;
        } else {
            this.pending = loadMods();
        }

        handleNewMods(pollMods());
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
        return CompletableFuture.completedFuture(List.of(
                new ModSummary("BeansMod", null, "Beans"),
                new ModSummary("PeasMod", null, "Peas")
        ));
    }

    private void handleNewMods(@Nullable List<ModSummary> modSummaryList) {
        if(modSummaryList == null) {
            fillLoadingMods();
        } else {
            fillMods(this.filter, modSummaryList);
        }
    }

    private void fillLoadingMods() {
        clearEntries();
        notifyListUpdated();
    }

    public void updateFilter(String str) {
        if(this.currentlyDisplayed != null && !str.equals(this.filter)) {
            this.fillMods(str, this.currentlyDisplayed);
        }

        this.filter = str;
    }

    private void fillMods(String filter, List<ModSummary> currentlyDisplayed) {
        this.clearEntries();
        filter = filter.toLowerCase(Locale.ROOT);

        for (ModSummary modSummary : currentlyDisplayed) {
            if(matchesFilter(filter, modSummary)) {
                addEntry(new ModListEntry(this, modSummary));
            }
        }

        notifyListUpdated();
    }

    private boolean matchesFilter(String filter,  ModSummary modSummary) {
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
        public Component getNarration() {
            return Component.translatable("brass.narration.modList.entry", this.summary.getName());
        }

        @Override
        public void render(PoseStack poseStack, int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b, float v) {
            this.minecraft.font.draw(poseStack, this.summary.getName(), i + 20, i1 + 20, 0x404040);
        }

        public boolean isSelectable() {
            return !summary.isDisabled();
        }

        public void close() {
        }
    }
}
