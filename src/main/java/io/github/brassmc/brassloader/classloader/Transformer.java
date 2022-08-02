package io.github.brassmc.brassloader.classloader;

import org.objectweb.asm.ClassReader;

public interface Transformer {
    boolean canApply(String name);

    void apply(String name, ClassReader reader);
}
