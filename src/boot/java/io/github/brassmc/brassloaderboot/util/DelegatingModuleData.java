package io.github.brassmc.brassloaderboot.util;

import cpw.mods.jarhandling.SecureJar;

import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.security.CodeSigner;
import java.util.Optional;
import java.util.jar.Manifest;

public class DelegatingModuleData implements SecureJar.ModuleDataProvider {
    private final SecureJar.ModuleDataProvider moduleDataProvider;

    public DelegatingModuleData(SecureJar.ModuleDataProvider moduleDataProvider) {
        this.moduleDataProvider = moduleDataProvider;
    }

    @Override
    public String name() {
        return moduleDataProvider.name();
    }

    @Override
    public ModuleDescriptor descriptor() {
        return moduleDataProvider.descriptor();
    }

    @Override
    public URI uri() {
        return moduleDataProvider.uri();
    }

    @Override
    public Optional<URI> findFile(String name) {
        return moduleDataProvider.findFile(name);
    }

    @Override
    public Optional<InputStream> open(String name) {
        return moduleDataProvider.open(name);
    }

    @Override
    public Manifest getManifest() {
        return moduleDataProvider.getManifest();
    }

    @Override
    public CodeSigner[] verifyAndGetSigners(String cname, byte[] bytes) {
        return moduleDataProvider.verifyAndGetSigners(cname, bytes);
    }
}
