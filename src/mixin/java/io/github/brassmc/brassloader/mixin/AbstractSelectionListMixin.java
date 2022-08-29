package io.github.brassmc.brassloader.mixin;

import io.github.brassmc.brassloader.util.HoveredProvider;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSelectionList.class)
public class AbstractSelectionListMixin<E extends AbstractSelectionList.Entry<E>> implements HoveredProvider<E> {
    @Override
    public E brass$getHovered() {
        return this.hovered;
    }

    @Override
    public void brass$setHovered(E hovered) {
        this.hovered = hovered;
    }

    @Shadow
    private E hovered;
}
