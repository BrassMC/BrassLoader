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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@AutoService(ITransformationService.class)
public class ModDiscovery implements ITransformationService {
    public static final List<ModContainer> MODS = new ArrayList<>();

    private static final Path MODS_FOLDER = Path.of("mods");

    @Override
    public @NotNull String name() {
        return "brass:moddiscovery";
    }

    @Override
    public void initialize(IEnvironment environment) {

    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        try {
            final List<SecureJar> mods = new ArrayList<>();

            Files.walkFileTree(MODS_FOLDER, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        // Read jar
                        if (!file.toString().endsWith(".jar")) return FileVisitResult.CONTINUE;
                        SecureJar secureJar = SecureJar.from(file);
                        mods.add(secureJar);

                        Path metadata = secureJar.getPath("mod.hjson");
                        if (Files.notExists(metadata)) return FileVisitResult.CONTINUE;
                        final var data = Files.readString(metadata);

                        // Parse metadata as hjson
                        JsonObject jsonObject = JsonValue.readHjson(new String(stream.readAllBytes())).asObject();

                        // Get and validate the mandatory mod details
                        String modid = getString(jsonObject, "modId", file);
                        lengthCheck("modid", modid, file, 3, 20);
                        if(!isValidModid(modid))
                            throw new InvalidModidException("Provided modid(" + modid + ") in mod(" + file + ") must match the expression: a-z0-9/._-");

                        String name = getString(jsonObject, "name", file);
                        lengthCheck("name", name, file, 4, 32);

                        String version = getString(jsonObject, "version", file);
                        lengthCheck("version", version, file, 1, 64);

                        String description = getString(jsonObject, "description", file);
                        lengthCheck("description", description, file, 4, 2056);

                        String entrypoint = getString(jsonObject, "entrypoint", file);

                        String license = getString(jsonObject, "license", file);
                        lengthCheck("license", license, file, 3, 64);

                        // Get and validate the people object
                        JsonValue peopleValue = jsonObject.get("people");
                        if(peopleValue == null)
                            throw new MetadataParseException("The people(including at least 1 developer) must be provided for mod: " + file);

                        JsonObject peopleObj = peopleValue.asObject();

                        String[] developers = getArrayOrString(peopleObj, "developers", file, true);
                        String[] artists = getArrayOrString(peopleObj, "artists", file);
                        String[] modellers = getArrayOrString(peopleObj, "modellers", file);
                        String[] animators = getArrayOrString(peopleObj, "animators", file);
                        String[] audioEngineers = getArrayOrString(peopleObj, "audioEngineers", file);
                        String[] additionalCredits = getArrayOrString(peopleObj, "additionalCredits", file);

                        // Get optional icon
                        String iconStr = getString(jsonObject, "icon", "default.png", file);
                        Path icon = Path.of(iconStr);

                        // Get optional mixin usage (boolean)
                        boolean usesMixins = jsonObject.getBoolean("usesMixins", false);

                        // Get and validate contact details
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

                        // Construct contact
                        ModContainer.Contact.Builder contact = new ModContainer.Contact.Builder()
                                .homepage(homepage)
                                .issues(issues)
                                .source(source)
                                .wiki(wiki)
                                .youtube(youtube)
                                .twitter(twitter)
                                .discord(discord);

                        // Construct People
                        ModContainer.People.Builder people = new ModContainer.People.Builder(developers[0], Arrays.copyOfRange(developers, 1, developers.length))
                                .artists(artists)
                                .modellers(modellers)
                                .animators(animators)
                                .audioEngineers(audioEngineers)
                                .additionalCredits(additionalCredits);

                        //Construct container
                        ModContainer.Builder containerBuilder = new ModContainer.Builder(modid)
                                .name(name)
                                .version(version)
                                .description(description)
                                .license(license)
                                .entrypoint(entrypoint)
                                .people(people)
                                .icon(icon)
                                .contact(contact);
                        if(usesMixins) {
                            containerBuilder = containerBuilder.usesMixins();
                        }

                        ModContainer container = containerBuilder.build();
                        MODS.add(container);

                        // Close resources
                        stream.close();

                        return FileVisitResult.CONTINUE;
                    } catch (IOException | ParseException exception) {
                        exception.printStackTrace();
                    }

                    return super.visitFile(file, attrs);
                }
            });

            return mods.stream().map(mod -> new Resource(IModuleLayerManager.Layer.GAME, List.of(mod))).toList();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static String[] getArrayOrString(JsonObject object, String name, Path modLoc, boolean shouldThrowIfMissing) {
        JsonValue itemsJson = object.get(name);
        String[] items;
        if(itemsJson != null && itemsJson.isArray()) {
            String[] arr = itemsJson.asArray().values().stream().map(JsonValue::asString).toArray(String[]::new);
            for (int i = 0; i < arr.length; i++) {
                String author = arr[i];
                lengthCheck(name + " " + (i + 1), author, modLoc, 4, 32);
            }

            items = arr;
        } else if(itemsJson != null && itemsJson.isString()) {
            items = new String[] { itemsJson.asString() };
        } else
            items = new String[0];

        if(shouldThrowIfMissing)
            if(items.length == 0)
                throw new MetadataParseException("At least 1 " + name + " must be provided for mod: " + modLoc);

        return items;
    }

    private static String[] getArrayOrString(JsonObject object, String name, Path modLoc) {
        return getArrayOrString(object, name, modLoc, false);
    }

    private static void lengthCheck(String name, String value, Path modLoc, int minLength, int maxLength) {
        if(value == null || value.isBlank())
            throw new InvalidModidException("Provided " + name + "(" + value + ") in mod(" + modLoc + ") must not be blank!");
        if(value.length() < minLength)
            throw new InvalidModidException("Provided " + name + "(" + value + ") in mod(" + modLoc + ") must be at least " + minLength + " characters!");
        if(value.length() > maxLength)
            throw new InvalidModidException("Provided " + name + "(" + value + ") in mod(" + modLoc + ") must be fewer than " + maxLength + " characters!");
    }

    private static String getString(JsonObject object, String name, String defaultValue, Path path) throws MetadataParseException {
        String value = object.getString(name, defaultValue);
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

    private static boolean isValidModid(String modid) {
        for(int index = 0; index < modid.length(); ++index) {
            if (!modidAllows(modid.charAt(index))) {
                return false;
            }
        }

        return true;
    }

    private static boolean modidAllows(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    @SuppressWarnings("rawtypes")
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }
}
