package io.github.brassmc.brassloader.mixin;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {
    @ModifyConstant(method = "getClientModName", constant = @Constant(stringValue = "vanilla"))
    private static String brass$modifyClientBrandName(String original) {
        return "brass";
    }
}
