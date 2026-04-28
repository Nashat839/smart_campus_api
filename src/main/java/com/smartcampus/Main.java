package com.smartcampus;

import com.smartcampus.api.SmartCampusApplication;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public final class Main {
    private static final URI BASE_URI = URI.create("http://0.0.0.0:8080/api/v1/");

    public static void main(String[] args) throws IOException {
        ResourceConfig config = ResourceConfig.forApplication(new SmartCampusApplication());
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, config);

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        System.out.println("Smart Campus API running at: " + BASE_URI);
        System.out.println("Press Enter to stop.");
        System.in.read();
        server.shutdownNow();
    }
}
