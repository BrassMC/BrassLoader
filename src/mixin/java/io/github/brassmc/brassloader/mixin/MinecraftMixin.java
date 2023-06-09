package io.github.brassmc.brassloader.mixin;

import com.google.common.collect.Sets;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.github.brassmc.brassloader.boot.MinecraftProvider;
import io.github.brassmc.brassloader.boot.discovery.ModDiscovery;
import io.github.brassmc.brassloader.mixin.access.PackRepositoryAccess;
import io.github.brassmc.brassloader.pack.SecureJarPackResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
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
    /**
     * @author matyrobbrt
     * @reason Offline mode - no need to contact Mojang servers
     */
    @Overwrite
    private UserApiService createUserApiService(YggdrasilAuthenticationService service, GameConfig cfg) {
        return UserApiService.OFFLINE;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"),
            method = "<init>(Lnet/minecraft/client/main/GameConfig;)V"
    )
    private void brass$addResources(PackRepository packRepository) {
        final var access = (PackRepositoryAccess) packRepository;
        final var sources = Sets.newHashSet(access.getSources());
        // TODO load on servers as well
        sources.add(packList -> {
            final Pack brassPack = Pack.readMetaAndCreate(
                    "brass_resources",
                    Component.literal("Brass' Resources"),
                    true,
                    name -> new SecureJarPackResources(
                            name,
                            MinecraftProvider.OWN_PATH.resolve("META-INF/jars/brass.secureJar"),
                            MinecraftProvider.BRASS_JAR
                    ),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.DEFAULT);
            packList.accept(brassPack);

            ModDiscovery.getMods().forEach(mod -> {
                final Pack modPack = Pack.readMetaAndCreate(
                        mod.modid() + "_resources",
                        Component.literal(mod.name() + "'s Resources"),
                        true,
                        name -> new SecureJarPackResources(
                                name,
                                mod.jarPath(),
                                mod.secureJar()
                        ),
                        PackType.CLIENT_RESOURCES,
                        Pack.Position.TOP,
                        PackSource.DEFAULT);
                packList.accept(modPack);
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
