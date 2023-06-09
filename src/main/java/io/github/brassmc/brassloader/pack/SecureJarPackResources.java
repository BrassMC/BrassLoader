package io.github.brassmc.brassloader.pack;

import com.google.common.base.Joiner;
import cpw.mods.jarhandling.SecureJar;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SecureJarPackResources extends PathPackResources {
    private final SecureJar secureJar;

    public SecureJarPackResources(String name, Path path, SecureJar secureJar) {
        super(name, path, false);
        this.secureJar = secureJar;
    }

    private Path resolvePath(String... paths) {
        String first = paths[0];
        List<String> others = new ArrayList<>(Arrays.asList(paths).subList(1, paths.length));
        return this.secureJar.getPath(first, others.toArray(new String[0]));
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... paths) {
        FileUtil.validatePath(paths);
        Path path = resolvePath(paths);
        return Files.exists(path) ? IoSupplier.create(path) : null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType packType, ResourceLocation location) {
        if (location.getPath().startsWith("lang/")) {
            return super.getResource(PackType.CLIENT_RESOURCES, location);
        } else {
            return super.getResource(packType, location);
        }
    }

    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String pathIn, @NotNull ResourceOutput output) {
        Path root = resolvePath(packType.getDirectory(), namespace).toAbsolutePath();
        Path inputPath = root.getFileSystem().getPath(pathIn);

        if (Files.exists(inputPath, LinkOption.NOFOLLOW_LINKS)) {
            try (Stream<Path> stream = Files.walk(root)) {
                stream.map(root::relativize)
                        .filter(path -> !path.toString().endsWith(".mcmeta") && path.startsWith(inputPath))
                        .map(path -> new ResourceLocation(namespace, Joiner.on('/').join(path)))
                        .forEach(resourceLocation -> output.accept(resourceLocation, IoSupplier.create(resolvePath(packType.getDirectory(), namespace, resourceLocation.getPath()))));
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to list resources for namespace: '%s'".formatted(namespace), exception);
            }
        }
    }
}
