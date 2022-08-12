package io.github.brassmc.brassloader.boot.mods;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record ModContainer(@NotNull String modid, @NotNull String name, @NotNull String version,
                           @NotNull String description, @NotNull String license, @NotNull String[] authors,
                           @NotNull Path icon, boolean usesMixins, @NotNull Contact contact) {
    public static class Builder {
        private final String modid;
        private String name, version, description, license;
        private String[] authors;
        private Path icon;
        private boolean usesMixins;
        private Contact contact;

        public Builder(String modid) {
            this.modid = modid;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder license(String license) {
            this.license = license;
            return this;
        }

        public Builder authors(String... authors) {
            this.authors = authors;
            return this;
        }

        public Builder icon(Path icon) {
            this.icon = icon;
            return this;
        }

        public Builder usesMixins() {
            this.usesMixins = true;
            return this;
        }

        public Builder contact(Contact contact) {
            this.contact = contact;
            return this;
        }

        public Builder contact(Contact.Builder contact) {
            return contact(contact.build());
        }

        public ModContainer build() {
            return new ModContainer(modid, name, version, description, license, authors, icon, usesMixins, contact);
        }
    }

    public record Contact(String homepage, String issues, String source, String wiki, String youtube, String twitter, String discord) {
        public static class Builder {
            private String homepage, issues, source, wiki, youtube, twitter, discord;

            public Builder homepage(String homepage) {
                this.homepage = homepage;
                return this;
            }

            public Builder issues(String issues) {
                this.issues = issues;
                return this;
            }

            public Builder source(String source) {
                this.source = source;
                return this;
            }

            public Builder wiki(String wiki) {
                this.wiki = wiki;
                return this;
            }

            public Builder youtube(String youtube) {
                this.youtube = youtube;
                return this;
            }

            public Builder twitter(String twitter) {
                this.twitter = twitter;
                return this;
            }

            public Builder discord(String discord) {
                this.discord = discord;
                return this;
            }

            public Contact build() {
                return new Contact(homepage, issues, source, wiki, youtube, twitter, discord);
            }
        }
    }
}
