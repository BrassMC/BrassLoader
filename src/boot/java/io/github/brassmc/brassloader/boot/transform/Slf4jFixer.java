package io.github.brassmc.brassloader.boot.transform;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * SLF4J is placed on the BOOT module layer but MC requests it from the GAME layer,
 * respectively the `minecraft` module, resulting in providers frm the BOOT layer
 * not being found. <br>
 * This transformer forces SLF4J to bind from the bootstrap loader.
 *
 * @see org.slf4j.LoggerFactory#findServiceProviders
 */
@SuppressWarnings("JavadocReference")
@AutoService(ILaunchPluginService.class)
public class Slf4jFixer implements ILaunchPluginService {
    @Override
    public String name() {
        return "brassmc:slf4jfixer";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.noneOf(Phase.class);
    }

    @Override
    public void initializeLaunch(ITransformerLoader transformerLoader, NamedPath[] specialPaths) {
        final var currentThread = Thread.currentThread();
        final var contextClassLoader = currentThread.getContextClassLoader();

        // Set the context CL of the current thread to the bootstrap CL
        currentThread.setContextClassLoader(this.getClass().getClassLoader());

        // Force SLF4J to bind the service providers while we manually set the context classloader to be correct
        LoggerFactory.getILoggerFactory();

        // Set context CL back to TransformingClassLoader
        currentThread.setContextClassLoader(contextClassLoader);
    }
}
