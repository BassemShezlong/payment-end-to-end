package example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Environment {

    private static final Properties properties = new Properties();
    private static boolean printedEnvironment = false;

    static {
        try (InputStream input = Environment.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("❌ Unable to find config.properties file.");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the base URL for the given API type (nest or node) based on the active environment.
     *
     * Environment priority:
     * 1. If a system property (-Denvironment=...) is provided → use it (CI/CD or manual run).
     * 2. Otherwise → fallback to the environment value in config.properties.
     *
     * Example usage in local:
     * - Read from config.properties → environment=staging
     *
     * Example usage in CI/CD:
     * - mvn test -Denvironment=alpha
     * - mvn test -Denvironment=production
     *
     * @param apiType The type of API to get the base URL for ("nest" or "node").
     * @return The base URL for the given API type and environment.
     */
    public static String getBaseUrl(String apiType) {
        // 1️⃣ First, try reading from system property (used in CI/CD pipelines or manual override)
        String environment = System.getProperty("environment");

        // 2️⃣ If not provided, fallback to config.properties
        if (environment == null || environment.isEmpty()) {
            environment = properties.getProperty("environment", "staging"); // default = staging
        }

        // 3️⃣ Log the active environment only once
        if (!printedEnvironment) {
            System.out.println("🌍 Active Environment: " + environment);
            printedEnvironment = true;
        }

        // 4️⃣ Build the property key: environment + "." + apiType (e.g., staging.nest)
        String key = environment + "." + apiType;

        // 5️⃣ Return the base URL from config.properties
        return properties.getProperty(key);
    }
}
