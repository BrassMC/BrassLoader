package io.github.brassmc.brassloader.mixin;

import com.google.common.collect.Sets;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.github.brassmc.brassloader.boot.MinecraftProvider;
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
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    private IntegratedServer server;

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
        sources.add((packList, factory) -> {
            final Pack packInfo = Pack.create("brass_resources", true, () -> new PathResourcePack(
                            "brass", MinecraftProvider.OWN_PATH.resolve("META-INF/jars/brass.jar")
                    ) {
                        @Override
                        protected Path resolve(String... paths) {
                            final List<String> others = new ArrayList<>();
                            final var first = paths[0];
                            for (var i = 1; i < paths.length; i++) {
                                others.add(paths[i]);
                            }
                            return MinecraftProvider.BRASS_JAR.getPath(first, others.toArray(String[]::new));
                        }
                    },
                    factory, Pack.Position.BOTTOM, PackSource.DEFAULT);
            packList.accept(packInfo);
        });
        access.setSources(Set.copyOf(sources));
        packRepository.reload();
    }

    @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
    private void brassloader$changeWindowTitle(CallbackInfoReturnable<String> ci) {
        ci.setReturnValue("Minecraft Brass" + SharedConstants.getCurrentVersion().getName() + (server != null && server.isRemote()) ? "Multiplayer" : "Singleplayer");
    } 
}
