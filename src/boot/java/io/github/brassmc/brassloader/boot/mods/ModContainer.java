package io.github.brassmc.brassloader.boot.mods;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record ModContainer(@NotNull String modid, @NotNull String name, @NotNull String version,
                           @NotNull String description, @NotNull String entrypoint, @NotNull String license, @NotNull People people,
                           @NotNull Path icon, @NotNull Contact contact) {
    public static class Builder {
        private final String modid;
        private String name, version, description, entrypoint, license;
        private Path icon;
        private Contact contact;
        private People people;

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

        public Builder entrypoint(String entrypoint) {
            this.entrypoint = entrypoint;
            return this;
        }

        public Builder license(String license) {
            this.license = license;
            return this;
        }

        public Builder icon(Path icon) {
            this.icon = icon;
            return this;
        }

        public Builder contact(Contact contact) {
            this.contact = contact;
            return this;
        }

        public Builder contact(Contact.Builder contact) {
            return contact(contact.build());
        }

        public Builder people(People people) {
            this.people = people;
            return this;
        }

        public Builder people(People.Builder people) {
            return people(people.build());
        }

        public ModContainer build() {
            return new ModContainer(modid, name, version, description, entrypoint, license, people, icon, contact);
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

    public record People(@NotNull String[] developers, String[] artists, String[] modellers, String[] animators, String[] audioEngineers, String[] additionalCredits) {
        public static class Builder {
            private final String[] developers;
            private String[] artists, modellers, animators, audioEngineers, additionalCredits;

            public Builder(@NotNull String developer, String... additionalDevelopers) {
                this.developers = new String[additionalDevelopers.length + 1];
                this.developers[0] = developer;
                System.arraycopy(additionalDevelopers, 0, this.developers, 1, developers.length - 1);
            }

            public Builder artists(String... artists) {
                this.artists = artists;
                return this;
            }

            public Builder modellers(String... modellers) {
                this.modellers = modellers;
                return this;
            }

            public Builder animators(String... animators) {
                this.animators = animators;
                return this;
            }

            public Builder audioEngineers(String... audioEngineers) {
                this.audioEngineers = audioEngineers;
                return this;
            }

            public Builder additionalCredits(String... additionalCredits) {
                this.additionalCredits = additionalCredits;
                return this;
            }

            public People build() {
                return new People(this.developers, this.artists, this.modellers, this.animators, this.audioEngineers, this.additionalCredits);
            }
        }
    }
}
