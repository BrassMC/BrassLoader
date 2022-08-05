package io.github.brassmc.brassloader.util;

import cpw.mods.jarhandling.SecureJar;

import java.nio.file.Path;
import java.security.CodeSigner;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;

public class DelegatingSecureJar implements SecureJar {
    public final SecureJar secureJar;

    @Override
    public ModuleDataProvider moduleDataProvider() {
        return secureJar.moduleDataProvider();
    }

    @Override
    public Path getPrimaryPath() {
        return secureJar.getPrimaryPath();
    }

    @Override
    public CodeSigner[] getManifestSigners() {
        return secureJar.getManifestSigners();
    }

    @Override
    public Status verifyPath(Path path) {
        return secureJar.verifyPath(path);
    }

    @Override
    public Status getFileStatus(String name) {
        return secureJar.getFileStatus(name);
    }

    @Override
    public Attributes getTrustedManifestEntries(String name) {
        return secureJar.getTrustedManifestEntries(name);
    }

    @Override
    public boolean hasSecurityData() {
        return secureJar.hasSecurityData();
    }

    @Override
    public Set<String> getPackages() {
        return secureJar.getPackages();
    }

    @Override
    public List<Provider> getProviders() {
        return secureJar.getProviders();
    }

    @Override
    public String name() {
        return secureJar.name();
    }

    @Override
    public Path getPath(String first, String... rest) {
        return secureJar.getPath(first, rest);
    }

    @Override
    public Path getRootPath() {
        return secureJar.getRootPath();
    }

    public DelegatingSecureJar(SecureJar secureJar) {
        this.secureJar = secureJar;
    }
}
