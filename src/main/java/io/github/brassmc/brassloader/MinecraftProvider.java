package io.github.brassmc.brassloader;

import com.google.auto.service.AutoService;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import io.github.brassmc.brassloader.util.DelegatingModuleData;
import io.github.brassmc.brassloader.util.DelegatingSecureJar;

import javax.annotation.Nonnull;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * This empty transformation service handles adding MC to the game layer.
 */
@AutoService(ITransformationService.class)
public class MinecraftProvider implements ITransformationService {
    public static final String MC_LOCATION_PROP = "brassloader.mclocation";

    @Nonnull
    @Override
    public String name() {
        return "brass:mcprovider";
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        final var jar = SecureJar.from(Path.of(System.getProperty(MC_LOCATION_PROP)).toAbsolutePath());
        final var delegateMc = new DelegatingSecureJar(jar) {
            private final ModuleDataProvider moduleDataProvider = new DelegatingModuleData(jar.moduleDataProvider()) {
                @Override
                public String name() {
                    return "minecraft";
                }

                @Override
                public ModuleDescriptor descriptor() {
                    final var parent = jar.moduleDataProvider().descriptor();
                    return ModuleDescriptor.newAutomaticModule("minecraft")
                            .packages(parent.packages())
                            .build();
                }
            };

            @Override
            public ModuleDataProvider moduleDataProvider() {
                return moduleDataProvider;
            }

            @Override
            public String name() {
                return "minecraft";
            }
        };
        return List.of(new Resource(
                IModuleLayerManager.Layer.GAME, List.of(delegateMc)
        ));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public @Nonnull List<ITransformer> transformers() {
        return List.of();
    }
}
