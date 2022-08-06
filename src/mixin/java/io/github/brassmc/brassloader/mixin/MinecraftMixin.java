package io.github.brassmc.brassloader.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Overwrite
    private UserApiService createUserApiService(YggdrasilAuthenticationService service, GameConfig cfg) {
        return UserApiService.OFFLINE;
    }
}
