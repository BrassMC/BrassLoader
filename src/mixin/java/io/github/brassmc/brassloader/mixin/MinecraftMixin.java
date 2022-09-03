package io.github.brassmc.brassloader.mixin;

import com.google.common.collect.Sets;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.github.brassmc.brassloader.boot.MinecraftProvider;
import io.github.brassmc.brassloader.boot.discovery.ModDiscovery;
import io.github.brassmc.brassloader.mixin.access.PackRepositoryAccess;
import io.github.brassmc.brassloader.pack.PathResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Overwrite
    private UserApiService createUserApiService(YggdrasilAuthenticationService service, GameConfig cfg) {
        return UserApiService.OFFLINE;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"),
            method = "Lnet/minecraft/client/Minecraft;<init>(Lnet/minecraft/client/main/GameConfig;)V"
    )
    private void brass$addResources(PackRepository packRepository) {
        final var access = (PackRepositoryAccess) packRepository;
        final var sources = Sets.newHashSet(access.getSources());
        // TODO load on servers as well
        sources.add((packList, factory) -> {
            final Pack brassPack = Pack.create("brass_resources", true, () -> new PathResourcePack.ForSecureJar(
                            "brass", MinecraftProvider.OWN_PATH.resolve("META-INF/jars/brass.secureJar"),
                                MinecraftProvider.BRASS_JAR
                    ),
                    factory, Pack.Position.TOP, PackSource.DEFAULT);
            packList.accept(brassPack);
            ModDiscovery.getMods().forEach(mod -> {
                final Pack packInfo = Pack.create(mod.modid() + "_resources", true, () -> new PathResourcePack.ForSecureJar(
                                "brass", mod.jarPath(), mod.secureJar()
                        ),
                        factory, Pack.Position.TOP, PackSource.DEFAULT);
                packList.accept(packInfo);
            });
        });
        access.setSources(Set.copyOf(sources));
        packRepository.reload();
    }

    @ModifyConstant(method = "createTitle()Ljava/lang/String;", constant = @Constant(stringValue = "Minecraft"))
    private String brass$changeWindowTitle(String s) {
        return "Minecraft Brass";
    }
}
