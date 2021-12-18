package io.github.gotchamana;

import java.nio.file.*;
import java.security.*;
import java.util.Optional;

public record Config(int port, Path rootDir) {

    public static Config getDefault() {
        var rootDir = getClassFileDirectory(Config.class)
            .orElse(Path.of(System.getProperty("user.dir")));
        return new Config(8080, rootDir);
    }

    private static <T> Optional<Path> getClassFileDirectory(Class<T> clazz) {
        return Optional.of(clazz.getProtectionDomain())
            .map(ProtectionDomain::getCodeSource)
            .map(CodeSource::getLocation)
            .map(url -> {
                var path = Path.of(url.getPath());
                return Files.isDirectory(path) ? path : path.getParent();
            });
    }
}