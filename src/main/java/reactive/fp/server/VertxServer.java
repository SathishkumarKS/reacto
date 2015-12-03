package reactive.fp.server;

import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import reactive.fp.server.handlers.HystrixEventStreamHandler;
import reactive.fp.server.handlers.SSECommandHandler;
import reactive.fp.server.handlers.SSEHandler;
import reactive.fp.server.handlers.WebSocketHandler;

import java.util.Objects;

import static reactive.fp.utils.WebUtils.includeEndDelimiter;
import static reactive.fp.utils.WebUtils.includeStartDelimiter;

/**
 * @author OZY on 2015.11.23.
 */
public class VertxServer implements Server {

    private final String root;
    private final CommandRegistry commands;
    private final HttpServer httpServer;
    private final Router router;
    private final SSEHandler sseStreamHandler = new SSEHandler(response -> {});

    public VertxServer(Router router, HttpServer httpServer, String root, CommandRegistry commands) {
        Objects.requireNonNull(router, "Router cannot be null");
        Objects.requireNonNull(httpServer, "HttpServer cannot be null");
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(commands, "CommandRegistry cannot be null");
        this.router = router;
        this.httpServer = httpServer;
        this.root = root;
        this.commands = commands;
    }

    @Override
    public void start() {
        setupRoutes();
        httpServer.listen();
    }

    @Override
    public void stop() {
        httpServer.close();
    }

    private void setupRoutes() {
        router.route().handler(BodyHandler.create());
        httpServer.websocketHandler(new WebSocketHandler(commands));
        router.route(root() + "hystrix.stream")
                .handler(new SSEHandler(HystrixEventStreamHandler::handle));
        router.route(root() + "sse.stream")
                .handler(sseStreamHandler);
        router.route(root() + "sse.command")
                .handler(new SSECommandHandler(sseStreamHandler, commands));
        httpServer.requestHandler(router::accept);
    }

    private String root() {
        return includeEndDelimiter(includeStartDelimiter(root));
    }
    }
