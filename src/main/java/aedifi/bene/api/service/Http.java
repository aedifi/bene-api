package aedifi.bene.api.service;

import aedifi.bene.api.module.ModuleId;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface Http {
    enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD
    }

    record Route(Method method, String path) {
        public Route {
            if (method == null) {
                throw new IllegalArgumentException("Method cannot be null.");
            }
            if (path == null || path.isBlank() || !path.startsWith("/")) {
                throw new IllegalArgumentException("Path must be non-blank and start with '/'.");
            }
        }
    }

    interface Request {
        Method method();

        String path();

        Map<String, String> pathParameters();

        Map<String, List<String>> queryParameters();

        Map<String, List<String>> headers();

        InputStream body();
    }

    interface Response {
        Response status(int code);

        Response header(String name, String value);

        void body(byte[] bytes) throws IOException;

        void body(String text) throws IOException;

        void bodyStream(StreamWriter writer) throws IOException;

        @FunctionalInterface
        interface StreamWriter {
            void write(OutputStream out) throws IOException;
        }
    }

    @FunctionalInterface
    interface Handler {
        void handle(Request request, Response response) throws Exception;
    }

    default void register(final ModuleId owner, final Method method, final String path, final Handler handler) {
        register(owner, new Route(method, path), handler);
    }

    void register(ModuleId owner, Route route, Handler handler);

    void unregisterOwnerRoutes(ModuleId owner);
}
