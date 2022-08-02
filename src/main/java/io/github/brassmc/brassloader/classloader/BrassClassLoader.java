package io.github.brassmc.brassloader.classloader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class BrassClassLoader extends URLClassLoader {
    private final Transformer[] transformers;

    public BrassClassLoader(URL[] urls, ClassLoader classLoader) {
        super(urls, classLoader);
        this.transformers = new Transformer[0];
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        ClassReader reader = null;
        for (Transformer transformer : transformers) {
            if (transformer.canApply(name)) {
                //BrassLoader.getInstance().getLogger().info("Transforming class {}", name);
                System.out.printf("Transforming class %s\n!", name);
                try {
                    // FIXME: Make this actually apply the transformation!
                    reader = new ClassReader(name);
                    transformer.apply(name, reader);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        // Maybe we'll have to do some duplicate management?!
        if (reader != null) {
            var writer = new ClassWriter(Opcodes.ASM9); // TODO: Make sure this works with Mixin or something?!
            byte[] bytes = writer.toByteArray();
            return this.defineClass(name, bytes, 0, bytes.length);
        }

        if (name.startsWith("java.") || name.startsWith("jdk.") || name.startsWith("sun.")) {
            return this.getClass().getClassLoader().getParent().loadClass(name);
        }

        var stream = this.getResourceAsStream(name.replace(".", "/") + ".class");
        if (stream == null) {
            throw new ClassNotFoundException("Could not locate class %s".formatted(name));
        }

        try {
            var bytes = stream.readAllBytes();
            return this.defineClass(name, bytes, 0, bytes.length);
        } catch (IOException exception) {
            throw new ClassNotFoundException(exception.getMessage());
        }
    }

    static {
        registerAsParallelCapable();
    }
}
