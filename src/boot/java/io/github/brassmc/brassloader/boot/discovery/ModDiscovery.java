package io.github.brassmc.brassloader.boot.discovery;

import com.google.auto.service.AutoService;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import io.github.brassmc.brassloader.boot.mods.ModContainer;
import org.apache.commons.validator.routines.UrlValidator;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipException;

@AutoService(ITransformationService.class)
public class ModDiscovery implements ITransformationService {
    private static final Path MODS_FOLDER = Path.of("mods");

    @Override
    public String name() {
        return "brass:moddiscovery";
    }

    @Override
    public void initialize(IEnvironment environment) {

    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        try {
            Files.walkFileTree(MODS_FOLDER, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    try {
//                        SecureJar secureJar = SecureJar.from(file);
//                        Path metadata = secureJar.getPath("mod.hjson");
//                        try(var fileReader = new BufferedReader(new FileReader(metadata.toFile()))) {
//                            var content = fileReader.lines().collect(Collectors.joining());
//                            JsonObject json = JsonValue.readHjson(content).asObject();
//                            System.out.println(json);
//                        }
//                    } catch(ParseException exception) {
//
//                    }

                    try(var jarFile = new JarFile(file.toFile())) {
                        JarEntry metadata = jarFile.getJarEntry("mod.hjson");
                        JsonObject jsonObject = JsonValue.readHjson(new String(jarFile.getInputStream(metadata).readAllBytes())).asObject();
                        String modid = getString(jsonObject, "modid", file);
                        String name = getString(jsonObject, "name", file);
                        String version = getString(jsonObject, "version", file);
                        String description = getString(jsonObject, "license", file);

                        JsonValue authorsJson = jsonObject.get("authors");
                        if(authorsJson == null)
                            throw new MetadataParseException("At least 1 author must be provided for mod: " + file);
                        String[] authors = authorsJson.asArray().values().stream().map(JsonValue::asString).toArray(String[]::new);

                        String iconStr = getString(jsonObject, "icon", "default.png", file);
                        Path icon = Path.of(iconStr);

                        boolean usesMixins = jsonObject.getBoolean("usesMixins", false);

                        JsonValue contactJson = jsonObject.get("contact");
                        JsonObject contactObj;
                        if(contactJson != null) {
                            contactObj = contactJson.asObject();
                        } else {
                            contactObj = new JsonObject();
                        }

                        UrlValidator validator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES);
                        String homepage = validateURL(validator, "homepage", contactObj.getString("homepage", ""), file);
                        String issues = validateURL(validator, "issues", contactObj.getString("issues", ""), file);
                        String source = validateURL(validator, "source", contactObj.getString("source", ""), file);
                        String wiki = validateURL(validator, "wiki", contactObj.getString("wiki", ""), file);
                        String youtube = validateURL(validator, "youtube", contactObj.getString("youtube", ""), file);
                        String twitter = validateURL(validator, "twitter", contactObj.getString("twitter", ""), file);
                        String discord = validateURL(validator, "discord", contactObj.getString("discord", ""), file);

                        ModContainer.Contact.Builder contact = new ModContainer.Contact.Builder()
                                .homepage(homepage)
                                .issues(issues)
                                .source(source)
                                .wiki(wiki)
                                .youtube(youtube)
                                .twitter(twitter)
                                .discord(discord);

                        ModContainer.Builder container = new ModContainer.Builder(modid)
                                .name(name)
                                .version(version)
                                .description(description)
                                .authors(authors)
                                .icon(icon)
                                .contact(contact);
                        if(usesMixins) {
                            container.usesMixins();
                        }

                        ModContainer container1 = container.build();
                    } catch (IOException | ParseException exception) {
                        exception.printStackTrace();
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        return ITransformationService.super.completeScan(layerManager);
    }

    private static String getString(JsonObject object, String name, String defaultValue, Path path) throws MetadataParseException {
        String value = object.getString("license", defaultValue);
        if(value != null && value.isBlank()) {
            throw new MetadataParseException(name + " was not provided for mod: " + path);
        }

        return value;
    }

    private static String getString(JsonObject object, String name, Path path) throws MetadataParseException {
        return getString(object, name, "", path);
    }

    private static String validateURL(UrlValidator validator, String fieldName, String url, Path path) {
        if(!url.isBlank() && !validator.isValid(url))
            throw new MetadataParseException(fieldName + " url is invalid in mod: " + path);

        return url;
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<ITransformer> transformers() {
        return List.of();
    }
}
