package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.brassmc.brassloader.boot.discovery.ModDiscovery;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public interface InformationPanelRenderer {

    Map<String, InformationPanelRenderer> PANELS = new HashMap<>();

    static void createPanel(@Nonnull String modid, InformationPanelRenderer renderer) {
        if (modid == null || ModDiscovery.MODS.stream().noneMatch(c -> c.modid().equals(modid)))
            throw new IllegalArgumentException("Attempted to create information panel with null, empty or non-present modid.");
        if (renderer != null)
            PANELS.put(modid, renderer);
    }

    void render(ModsListScreen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTicks);
}