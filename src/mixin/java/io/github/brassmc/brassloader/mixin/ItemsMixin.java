package io.github.brassmc.brassloader.mixin;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(Items.class)
public class ItemsMixin {
    @Shadow @Final public static Item NETHER_BRICK_WALL;

    @Inject(
            at = @At(value = "TAIL"),
            method = "<clinit>"
    )
    private static void item(CallbackInfo ci) {
        Registry.register(Registry.ITEM, new ResourceLocation("brassloader", "beans"), new Item(new Item.Properties().tab(CreativeModeTab.TAB_FOOD)));
    }
}
