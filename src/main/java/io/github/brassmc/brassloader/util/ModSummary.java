package io.github.brassmc.brassloader.util;

import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class ModSummary {
    private final String name;
    private final ResourceLocation icon;
    private final String description;

    public ModSummary(String name, ResourceLocation icon, String description) {
        this.name = name;
        this.icon = icon;
        this.description = description;
    }

    private boolean isDisabled;

    public boolean isDisabled() {
        return this.isDisabled;
    }

    public void setDisabled(boolean disabled) {
        this.isDisabled = disabled;
    }

    public String getName() {
        return this.name;
    }

    public ResourceLocation getIcon() {
        return this.icon;
    }

    public String getDescription() {
        return this.description;
    }
}
