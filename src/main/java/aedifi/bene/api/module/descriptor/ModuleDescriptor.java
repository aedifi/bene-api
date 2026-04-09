package aedifi.bene.api.module.descriptor;

import aedifi.bene.api.module.ModuleId;
import java.util.Objects;

public record ModuleDescriptor(
        ModuleId id,
        String name,
        String mainClass,
        int apiVersion) {
    public static final String FILE_NAME = "module.yml";

    public ModuleDescriptor {
        Objects.requireNonNull(name, "name");
    }

    public String displayName() {
        return name.isBlank() ? id.value() : name;
    }

    public static final class Keys {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String MAIN = "main";
        public static final String API_VERSION = "api-version";

        private Keys() {}
    }
}
