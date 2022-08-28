package io.github.brassmc.brassloader.boot.discovery;

import org.apache.commons.validator.routines.UrlValidator;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.nio.file.Path;

public final class MetadataUtils {
    static String[] getArrayOrString(JsonObject object, String name, Path modLoc, boolean shouldThrowIfMissing) {
        return getArrayOrString(object, name, modLoc, shouldThrowIfMissing, 32);
    }

    static String[] getArrayOrString(JsonObject object, String name, Path modLoc, int maxLength) {
        return getArrayOrString(object, name, modLoc, false, maxLength);
    }

    private static String[] getArrayOrString(JsonObject object, String name, Path modLoc, boolean shouldThrowIfMissing, int maxLength) {
        JsonValue itemsJson = object.get(name);
        String[] items;
        if(itemsJson != null && itemsJson.isArray()) {
            String[] arr = itemsJson.asArray().values().stream().map(JsonValue::asString).toArray(String[]::new);
            for (int i = 0; i < arr.length; i++) {
                String author = arr[i];
                lengthCheck(name + " " + (i + 1), author, modLoc, 4, maxLength);
            }

            items = arr;
        } else if(itemsJson != null && itemsJson.isString()) {
            items = new String[] { itemsJson.asString() };
        } else {
            items = new String[0];
        }

        if(shouldThrowIfMissing) {
            if (items.length == 0)
                throw new MetadataParseException("At least 1 " + name + " must be provided for mod: " + modLoc);
        }

        return items;
    }

    static String[] getArrayOrString(JsonObject object, String name, Path modLoc) {
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

    static String getStringWithLength(JsonObject object, String name, Path modLoc, int minLength, int maxLength) {
        String value = getString(object, name, modLoc);
        lengthCheck(name, value, modLoc, minLength, maxLength);
        return value;
    }

    static String getString(JsonObject object, String name, String defaultValue, Path path) throws MetadataParseException {
        String value = object.getString(name, defaultValue);
        if(value != null && value.isBlank()) {
            throw new MetadataParseException(name + " was not provided for mod: " + path);
        }

        return value;
    }

    static String getString(JsonObject object, String name, Path path) throws MetadataParseException {
        return getString(object, name, "", path);
    }

    static String validateURL(UrlValidator validator, String fieldName, String url, Path path) {
        if(!url.isBlank() && !validator.isValid(url))
            throw new MetadataParseException(fieldName + " url is invalid in mod: " + path);

        return url;
    }

    static boolean isValidModid(String modid) {
        for(int index = 0; index < modid.length(); ++index) {
            if (!modidAllows(modid.charAt(index))) {
                return false;
            }
        }

        return true;
    }

    private static boolean modidAllows(char c) {
        return c == '_' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }
}
