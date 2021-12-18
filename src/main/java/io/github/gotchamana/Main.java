package io.github.gotchamana;

import static java.util.logging.Level.*;

import java.io.*;
import java.net.BindException;
import java.util.Optional;
import java.util.logging.*;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        loadLoggerConfig();

        ConfigParser.parse(args, Config.getDefault())
            .run(Main::printMessage, Main::runServer);
    }

    private static void printMessage(String errMsg) {
        System.err.println(errMsg);
        System.err.println("Usage: simple-http-server [-p port] [-r rootDir]");
    }

    private static void runServer(Config config) {
        try {
            var server = new SimpleHttpServer(config.port(), config.rootDir());
            server.run();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Stop server");
                server.stop(0);
            }));
        } catch (BindException e) {
            log.log(SEVERE, "Binding socket failed", e);
        } catch (IOException e) {
            log.log(SEVERE, "Creating server failed", e);
        }
    }

    private static void loadLoggerConfig() {
        try {
            LogManager.getLogManager().readConfiguration(getResourceAsStream("/logging.properties"));
		} catch (IOException e) {
            log.log(WARNING, "Reading logger config failed", e);
		}
    }

    private static InputStream getResourceAsStream(String name) {
        return Optional.ofNullable(Main.class.getResourceAsStream(name))
            .map(in -> (InputStream) new BufferedInputStream(in))
            .orElse(InputStream.nullInputStream());
    }
}