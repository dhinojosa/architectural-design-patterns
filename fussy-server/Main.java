import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.time.ZonedDateTime;

public class Main {
    private static final Random random = new Random();
    // Default values that can be overridden by environment variables
    private static double successRate = getEnv("SUCCESS_RATE", 0.6);
    private static double latencyRate = getEnv("LATENCY_RATE", 0.2);
    private static double failureRate = getEnv("FAILURE_RATE", 0.2);
    private static int latencyDuration = (int) getEnv("LATENCY_DURATION", 5); // in seconds

    public static void main(String[] args) throws IOException {
        if (!validateRates(successRate, latencyRate, failureRate)) {
            System.err.println("Invalid configuration: The sum of success, latency, and failure rates must be equal to 1.0.");
            System.exit(1);  // Exit if initial configuration is invalid
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/service", new ServiceHandler());
        server.createContext("/config", new ConfigHandler());

        server.setExecutor(Executors.newFixedThreadPool(10)); // multiple threads for handling requests
        server.start();
        System.out.println("Server started on port 8000");
        System.out.printf("Initial Config -> Success: %.2f, Latency: %.2f, Failure: %.2f, Latency Duration: %d seconds%n",
            successRate, latencyRate, failureRate, latencyDuration);
    }

    static class ServiceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            double randomValue = random.nextDouble();
            String response;
            int statusCode;

            if (randomValue < successRate) {
                response = "{\"status\": \"success\", \"data\": \"Here is your data!\"}";
                statusCode = 200;
                System.out.printf("[%s] Service called successfully", ZonedDateTime.now());
            } else if (randomValue < successRate + latencyRate) {
                try {
                    Thread.sleep(latencyDuration * 1000L); // Introduce latency
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                response = "{\"status\": \"success\", \"data\": \"Here is your data after a delay!\"}";
                statusCode = 200;
                System.out.printf("[%s] Service called with latency", ZonedDateTime.now());
            } else {
                response = "{\"status\": \"error\", \"message\": \"Service failure!\"}";
                statusCode = 500;
                System.out.printf("[%s] Service called with failure", ZonedDateTime.now());
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Map<String, String> params = Utils.parseForm(body);

                double newSuccessRate = Double.parseDouble(params.getOrDefault("success_rate", String.valueOf(successRate)));
                double newLatencyRate = Double.parseDouble(params.getOrDefault("latency_rate", String.valueOf(latencyRate)));
                double newFailureRate = Double.parseDouble(params.getOrDefault("failure_rate", String.valueOf(failureRate)));
                int newLatencyDuration = Integer.parseInt(params.getOrDefault("latency_duration", String.valueOf(latencyDuration)));

                if (validateRates(newSuccessRate, newLatencyRate, newFailureRate)) {
                    successRate = newSuccessRate;
                    latencyRate = newLatencyRate;
                    failureRate = newFailureRate;
                    latencyDuration = newLatencyDuration;

                    String response = "{\"message\": \"Configuration updated\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    String response = "{\"error\": \"Invalid configuration: The sum of success, latency, and failure rates must equal 1.0.\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        }
    }

    // Helper method to get environment variable or default value
    private static double getEnv(String name, double defaultValue) {
        String value = System.getenv(name);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.printf("Invalid value for environment variable %s, using default %.2f%n", name, defaultValue);
            }
        }
        return defaultValue;
    }

    // Validation method to ensure that success, latency, and failure rates sum to 1.0
    private static boolean validateRates(double successRate, double latencyRate, double failureRate) {
        double sum = successRate + latencyRate + failureRate;
        return Math.abs(sum - 1.0) < 0.00001;  // Allowing a tiny tolerance for floating point precision
    }
}

class Utils {
    public static Map<String, String> parseForm(String formData) {
        return Map.of(formData.split("=")[0], formData.split("=")[1]);  // simplistic form parsing for demo
    }
}
