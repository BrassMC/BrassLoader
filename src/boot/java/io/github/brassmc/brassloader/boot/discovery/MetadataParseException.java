package io.github.brassmc.brassloader.boot.discovery;

public class MetadataParseException extends IllegalStateException {
    public MetadataParseException(String msg) {
        super(msg);
    }

    public MetadataParseException(Throwable throwable, String msg) {
        super(msg, throwable);
    }
}
