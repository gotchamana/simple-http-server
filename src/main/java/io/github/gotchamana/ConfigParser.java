package io.github.gotchamana;

import java.nio.file.*;
import java.security.*;
import java.util.*;

public class ConfigParser {

    private ConfigParser() {}

    public static Either<String, Config> parse(String[] cmdArgs, Config defaultValue) {
        return getInvalidMessage(cmdArgs)
            .map(Either::<String, Config>left)
            .orElseGet(() -> {
                var list = Arrays.asList(cmdArgs);
                var port = getPort(list).orElse(defaultValue.port());
                var rootDir = getRootDir(list)
                    .orElse(defaultValue.rootDir())
                    .toAbsolutePath()
                    .normalize();

                return Either.right(new Config(port, rootDir));
            });
    }

    private static Optional<String> getInvalidMessage(String[] args) {
        var skip = false;
        var portExists = false;
        var rootExists = false;

        for (int i = 0; i < args.length; i++) {
            if (skip) {
                skip = false;
                continue;
            }

            var arg = args[i];
            var isPortArg = arg.equals("-p") || arg.equals("--port");
            var isRootArg = arg.equals("-r") || arg.equals("--root");
			var isOutOfBound = i + 1 == args.length;

            if (isPortArg && isOutOfBound)
                return Optional.of("Not specified port");
            
            if (isPortArg && portExists) {
                return Optional.of("Duplicate port option");
            } else if (isPortArg) {
                portExists = true;
            }

            if (isPortArg && isInvalidPort(args[i + 1])) {
                return Optional.of("Invalid port");
            } else {
                skip = true;
            }

            if (isRootArg && isOutOfBound)
                return Optional.of("Not specified root directory");

            if (isRootArg && rootExists) {
                return Optional.of("Duplicate root directory option");
            } else if (isRootArg) {
                rootExists = true;
            }

            if (isRootArg && isInvalidDir(args[i + 1])) {
                return Optional.of("Invalid root directory");
            } else {
                skip = true;
            }

            if (!isPortArg && !isRootArg)
                return Optional.of("Unknown option");
        }

        return Optional.empty();
    }

    private static boolean isInvalidPort(String port) {
        var rlt = port.chars()
            .reduce(0, (acc, ch) -> {
                var digit = Character.digit(ch, 10);

                if (acc < 0 || acc > 65535 || digit == -1)
                    return -1;

                return acc * 10 + digit;
            });

        return port.isEmpty() || rlt < 0;
    }

    private static boolean isInvalidDir(String dir) {
        return !Files.isDirectory(Path.of(dir));
    }

    private static OptionalInt getPort(List<String> args) {
        var index = args.indexOf("-p");
        index = index < 0 ? args.indexOf("--port") : index;

        return index < 0 ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(args.get(index + 1)));
    }

    private static Optional<Path> getRootDir(List<String> args) {
        var index = args.indexOf("-r");
        index = index < 0 ? args.indexOf("--root") : index;

        return index < 0 ? Optional.empty() : Optional.of(Path.of(args.get(index + 1)));
    }
}