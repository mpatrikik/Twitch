package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebSrever {
    public static void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/action", new ActionHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("🌍 HTTP Server started on port 8081");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String response = "Invalid request";

            if (query != null) {
                if (query.contains("shutdown")) {
                    StreamMonitor.shPC();
                    response = "🔴 PC shutting down!";
                } else if (query.contains("closechrome")) {
                    StreamMonitor.clsChrome();
                    response = "❌ Chrome closed!";
                } else if (query.contains("noop")) {
                    response = "✔️ No action taken.";
                }
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
