package io.github.brassmc.brassloader.pack;

import com.google.common.base.Joiner;
import cpw.mods.jarhandling.SecureJar;
import io.github.brassmc.brassloader.boot.MinecraftProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PathResourcePack extends AbstractPackResources {
    private final Path source;
    private final String packName;

    public PathResourcePack(String packName, final Path source) {
        super(new File("dummy"));
        this.source = source;
        this.packName = packName;
    }

    /**
     * Returns the source path containing the resource pack.
     * This is used for error display.
     *
     * @return the root path of the resources.
     */
    public Path getSource() {
        return this.source;
    }

    /**
     * Returns the identifying name for the pack.
     *
     * @return the identifier of the pack.
     */
    @Override
    public String getName() {
        return packName;
    }

    /**
     * Implement to return a file or folder path for the given set of path components.
     *
     * @param paths One or more path strings to resolve. Can include slash-separated paths.
     * @return the resulting path, which may not exist.
     */
    protected Path resolve(String... paths) {
        Path path = getSource();
        for (String name : paths)
            path = path.resolve(name);
        return path;
    }

    @Override
    protected InputStream getResource(String name) throws IOException {
        final Path path = resolve(name);
        if (!Files.exists(path))
            throw new FileNotFoundException("Can't find resource " + name + " at " + getSource());
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    @Override
    protected boolean hasResource(String name) {
        final Path path = resolve(name);
        return Files.exists(path);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String resourceNamespace, String pathIn, Predicate<ResourceLocation> filter) {
        try {
            Path root = resolve(type.getDirectory(), resourceNamespace).toAbsolutePath();
            Path inputPath = root.getFileSystem().getPath(pathIn);

            return Files.walk(root)
                    .map(root::relativize)
                    .filter(path -> !path.toString().endsWith(".mcmeta") && path.startsWith(inputPath))
                    .filter(path -> filter.test(new ResourceLocation(path.getFileName().toString())))
                    // It is VERY IMPORTANT that we do not rely on Path.toString as this is inconsistent between operating systems
                    // Join the path names ourselves to force forward slashes
                    .map(path -> new ResourceLocation(resourceNamespace, Joiner.on('/').join(path)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        try {
            Path root = resolve(type.getDirectory());
            return Files.walk(root, 1)
                    .map(root::relativize)
                    .filter(path -> path.getNameCount() > 0) // skip the root entry
                    .map(p -> p.toString().replaceAll("/$", "")) // remove the trailing slash, if present
                    .filter(s -> !s.isEmpty()) //filter empty strings, otherwise empty strings default to minecraft in ResourceLocations
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            if (type == PackType.SERVER_DATA) //We still have to add the resource namespace if client resources exist, as we load langs (which are in assets) on server
            {
                return this.getNamespaces(PackType.CLIENT_RESOURCES);
            } else {
                return Collections.emptySet();
            }
        }
    }

    @Override
    public InputStream getResource(PackType type, ResourceLocation location) throws IOException {
        if (location.getPath().startsWith("lang/")) {
            return super.getResource(PackType.CLIENT_RESOURCES, location);
        } else {
            return super.getResource(type, location);
        }
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation location) {
        if (location.getPath().startsWith("lang/")) {
            return super.hasResource(PackType.CLIENT_RESOURCES, location);
        } else {
            return super.hasResource(type, location);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s: %s", getClass().getName(), getSource());
    }

    public static final class ForSecureJar extends PathResourcePack {
        private final SecureJar secureJar;

        public ForSecureJar(String packName, Path path, SecureJar secureJar) {
            super(packName, path);
            this.secureJar = secureJar;
        }

        @Override
        protected Path resolve(String... paths) {
            final List<String> others = new ArrayList<>();
            final var first = paths[0];
            for (var i = 1; i < paths.length; i++) {
                others.add(paths[i]);
            }
            return secureJar.getPath(first, others.toArray(String[]::new));
        }
    }
}
