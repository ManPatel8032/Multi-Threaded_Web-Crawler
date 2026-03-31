package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RobotsTxtParser {
    private final List<String> disallowedPaths = new ArrayList<>();

    // Fetches and parses robots.txt from the given domain
    public RobotsTxtParser(String baseUrl) {
        try {
            URL url = new URL(baseUrl);
            String robotsUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new URL(robotsUrl).openStream())
            );

            boolean relevantSection = false;
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Check if this section applies to us (User-agent: *)
                if (line.toLowerCase().startsWith("user-agent:")) {
                    String agent = line.substring(11).trim();
                    relevantSection = agent.equals("*");
                }

                // Parse Disallow rules for our section
                if (relevantSection && line.toLowerCase().startsWith("disallow:")) {
                    String path = line.substring(9).trim();
                    if (!path.isEmpty()) {
                        disallowedPaths.add(path);
                    }
                }
            }

            reader.close();

            if (!disallowedPaths.isEmpty()) {
                System.out.println("Loaded robots.txt — " + disallowedPaths.size() + " disallowed path(s):");
                for (String path : disallowedPaths) {
                    System.out.println("  Disallow: " + path);
                }
            } else {
                System.out.println("robots.txt: No restrictions found.");
            }

        } catch (Exception e) {
            System.out.println("No robots.txt found or could not be read. Proceeding without restrictions.");
        }
    }

    // Checks if a URL is allowed to be crawled
    public boolean isAllowed(String url) {
        try {
            String path = new URL(url).getPath();
            for (String disallowed : disallowedPaths) {
                if (path.startsWith(disallowed)) {
                    return false;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }
}
