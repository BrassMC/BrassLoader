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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@AutoService(ITransformationService.class)
public class ModDiscovery implements ITransformationService {
    public static final List<ModContainer> MODS = new ArrayList<>();

    private static final Path MODS_FOLDER = Path.of("mods");

    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        try {
            final List<SecureJar> mods = new ArrayList<>();

            Files.walkFileTree(MODS_FOLDER, Set.of(), 1, new SimpleFileVisitor<>() {
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
                        JsonObject jsonObject = JsonValue.readHjson(data).asObject();

                        // Get and validate the mandatory mod details
                        String modid = MetadataUtils.getStringWithLength(jsonObject, "modId", file, 3, 20);
                        if(!MetadataUtils.isValidModid(modid))
                            throw new InvalidModidException("Provided modid(" + modid + ") in mod(" + file + ") must match the expression: a-z0-9/._-");

                        String name = MetadataUtils.getStringWithLength(jsonObject, "name", file, 4, 32);
                        String version = MetadataUtils.getStringWithLength(jsonObject, "version", file, 1, 64);
                        String description = MetadataUtils.getStringWithLength(jsonObject, "description", file, 4, 2056);
                        String entrypoint = MetadataUtils.getString(jsonObject, "entrypoint", file);
                        String license = MetadataUtils.getStringWithLength(jsonObject, "license", file, 3, 64);

                        // Get and validate the people object
                        JsonValue peopleValue = jsonObject.get("people");
                        if(peopleValue == null)
                            throw new MetadataParseException("The people(including at least 1 developer) must be provided for mod: " + file);

                        JsonObject peopleObj = peopleValue.asObject();

                        String[] developers = MetadataUtils.getArrayOrString(peopleObj, "developers", file, true);
                        String[] artists = MetadataUtils.getArrayOrString(peopleObj, "artists", file);
                        String[] modellers = MetadataUtils.getArrayOrString(peopleObj, "modellers", file);
                        String[] animators = MetadataUtils.getArrayOrString(peopleObj, "animators", file);
                        String[] audioEngineers = MetadataUtils.getArrayOrString(peopleObj, "audioEngineers", file);
                        String[] additionalCredits = MetadataUtils.getArrayOrString(peopleObj, "additionalCredits", file, 256);

                        // Get optional icon
                        String iconStr = MetadataUtils.getString(jsonObject, "icon", "default.png", file);
                        Path icon = Path.of(iconStr);

                        // Get and validate contact details
                        JsonValue contactJson = jsonObject.get("contact");
                        JsonObject contactObj;
                        if(contactJson != null) {
                            contactObj = contactJson.asObject();
                        } else {
                            contactObj = new JsonObject();
                        }

                        UrlValidator validator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES);
                        String homepage = MetadataUtils.validateURL(validator, "homepage", contactObj.getString("homepage", ""), file);
                        String issues = MetadataUtils.validateURL(validator, "issues", contactObj.getString("issues", ""), file);
                        String source = MetadataUtils.validateURL(validator, "source", contactObj.getString("source", ""), file);
                        String wiki = MetadataUtils.validateURL(validator, "wiki", contactObj.getString("wiki", ""), file);
                        String youtube = MetadataUtils.validateURL(validator, "youtube", contactObj.getString("youtube", ""), file);
                        String twitter = MetadataUtils.validateURL(validator, "twitter", contactObj.getString("twitter", ""), file);
                        String discord = MetadataUtils.validateURL(validator, "discord", contactObj.getString("discord", ""), file);

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

                        ModContainer container = containerBuilder.build();
                        MODS.add(container);

                        return FileVisitResult.CONTINUE;
                    } catch (IOException | ParseException exception) {
                        throw new MetadataParseException(exception, "Metadata(mod.hjson) is invalid for mod: " + file);
                    }
                }
            });

            return mods.stream().map(mod -> new Resource(IModuleLayerManager.Layer.GAME, List.of(mod))).toList();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public @NotNull String name() {
        return "brass:moddiscovery";
    }

    @Override
    public void initialize(IEnvironment environment) {}

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {}

    @Override
    @SuppressWarnings("rawtypes")
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }
}
