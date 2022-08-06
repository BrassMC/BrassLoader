package io.github.brassmc.brassloader.util;

import net.minecraft.client.gui.components.AbstractSelectionList;

public interface HoveredProvider<E extends AbstractSelectionList.Entry<E>> {
    E brass$getHovered();
    void brass$setHovered(E hovered);
}
