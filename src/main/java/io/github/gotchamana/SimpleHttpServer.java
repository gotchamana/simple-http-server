package io.github.gotchamana;

import static java.util.logging.Level.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.*;

public class SimpleHttpServer {

    private static final Logger log = Logger.getLogger(SimpleHttpServer.class.getName());

    private final HttpServer server;

    public SimpleHttpServer(int port, Path rootDir) throws IOException {
        Objects.requireNonNull(rootDir);

        log.log(FINE, "Listen at {0} port", Integer.toString(port));
        log.log(FINE, "The root directory is {0}", rootDir);

        server = HttpServer.create(new InetSocketAddress(port), 100);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/", handle(exchange -> {
            var path = exchange.getRequestURI().getPath().substring(1);
            log.log(FINE, "Request path: /{0}", path);

            Path resource = rootDir.resolve(path.isEmpty() ? "index.html" : path);

            if (Files.isDirectory(resource) || Files.notExists(resource)) {
                sendNotFound(exchange);
                return;
            }

            try (var in = new BufferedInputStream(Files.newInputStream(resource))) {
                exchange.sendResponseHeaders(200, Files.size(resource));
                in.transferTo(exchange.getResponseBody());
            }
        }));
    }

    private HttpHandler handle(HttpHandler handler) {
        return exchange -> {
            try (
                var in = exchange.getRequestBody();
                var out = exchange.getResponseBody();
                exchange
            ) {
                handler.handle(exchange);
            } catch (Exception e) {
                log.log(SEVERE, "Handle failed", e);
            }
        };
    }

    private void sendNotFound(HttpExchange exchange) throws IOException {
        var response = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta http-equiv='X-UA-Compatible' content='IE=edge'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>404 Not Found</title></head><body><h1>404 Not Found</h1></body></html>".getBytes();
        exchange.sendResponseHeaders(404, response.length);
        exchange.getResponseBody().write(response);
    }

    public void run() {
        server.start();
    }

    public void stop(int delay) {
        server.stop(delay);
    }
}