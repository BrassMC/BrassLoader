package io.github.brassmc.brassloader.mixin;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.obfuscate.DontObfuscate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {

    /**
     * @author BrassMC
     * @reason change client mod name to brass
     */
    @DontObfuscate
    @Overwrite
    public static String getClientModName() {
        return "brass";
    }
}
