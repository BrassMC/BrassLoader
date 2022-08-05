package io.github.brassmc.brassloader.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MinecraftMixin {
    @Inject(at = @At("HEAD"), method = "main")
    private static void brassloader$main(String[] args, CallbackInfo ci) {
        System.out.println("Hello world!");
        // System.exit(0);
    }
}
