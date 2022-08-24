package io.github.brassmc.brassloader.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.brassmc.brassloader.boot.discovery.ModDiscovery;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class InformationPanel {

    static final Map<String, Renderer> PANELS = new HashMap<>();

    public static void registerPanel(@Nonnull String modid, Renderer renderer) {
        if (modid == null || ModDiscovery.MODS.stream().noneMatch(c -> c.modid().equals(modid)))
            throw new IllegalArgumentException("Attempted to create information panel with null, empty or non-present modid.");
        if (renderer != null)
            PANELS.put(modid, renderer);
    }

    @FunctionalInterface
    public interface Renderer {

        void render(ModsListScreen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTicks);
    }
}