package io.github.brassmc.brassloader;

import io.github.brassmc.brassloader.classloader.BrassClassLoader;
import io.github.brassmc.brassloader.environment.Environment;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BrassLoader {
    private static boolean frozen = false;
    private static BrassLoader instance;

    private final Environment env;
    //private final Logger logger = LoggerFactory.getLogger(BrassLoader.class);
    private final BrassClassLoader classLoader;

    public BrassLoader(Environment env) {
        this.env = env;

        // TODO: Unhardcode this
        try {
            File runFolder = Paths.get("./run/").toFile();
            URL[] jars = locateJars(runFolder);
            this.classLoader = new BrassClassLoader(jars, BrassLoader.class.getClassLoader());
        } catch (MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static URL[] locateJars(File directory) throws MalformedURLException {
        List<URL> jars = new ArrayList<>();
        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(file.getName().endsWith(".jar")) {
                jars.add(file.toURI().toURL());
            } else if(file.isDirectory()){
                URL[] subJars = locateJars(file);
                jars.addAll(Arrays.asList(subJars));
            }
        }

        return jars.toArray(new URL[0]);
    }

    public static BrassLoader getInstance() {
        return instance;
    }

    public Environment getEnv() {
        return this.env;
    }

    public void launch(String[] args) {
        String mainClass = switch (this.env) {
            case CLIENT -> "net.minecraft.client.main.Main";
            case DEDICATED_SERVER -> "net.minecraft.server.Main";
        };

        try {
            var main = Class.forName(mainClass, true, this.classLoader);
            main.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (ClassNotFoundException exception) {
//            this.logger.error(exception.getMessage());
//            this.logger.error("{}", Arrays.toString(exception.getStackTrace()));
            exception.printStackTrace();
            System.exit(1);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void setInstance(BrassLoader loader) {
        if (!frozen) {
            instance = loader;
            freeze();
        } else {
            throw new IllegalCallerException("Attempted to override the BrassLoader instance!");
        }
    }

    public static void freeze() {
        frozen = true;
    }

    //public Logger getLogger() {
        //return this.logger;
    //}

    public BrassClassLoader getClassLoader() {
        return this.classLoader;
    }
}
