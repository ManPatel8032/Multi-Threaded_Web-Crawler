package org.example;

import org.jsoup.Jsoup;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RobotsTxtParser {
    private final List<String> disallowedPaths = new ArrayList<>();

    public RobotsTxtParser(String baseUrl) {
        try {
            URL url = new URL(baseUrl);
            String robotsUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";

            String robotsText = Jsoup.connect(robotsUrl)
                    .userAgent("MyCrawlerBot/1.0")
                    .ignoreContentType(true)
                    .timeout(5000)
                    .execute()
                    .body();

            boolean relevantSection = false;
            boolean inUserAgentBlock = true; // tracks if we're still reading User-agent lines

            for (String line : robotsText.split("\n")) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.toLowerCase().startsWith("user-agent:")) {
                    if (!inUserAgentBlock) {
                        relevantSection = false;
                        inUserAgentBlock = true;
                    }
                    String agent = line.substring(11).trim();
                    if (agent.equals("*")) {
                        relevantSection = true;
                    }
                } else {
                    inUserAgentBlock = false;

                    if (relevantSection && line.toLowerCase().startsWith("disallow:")) {
                        String path = line.substring(9).trim();
                        if (!path.isEmpty()) {
                            disallowedPaths.add(path);
                        }
                    }
                }
            }

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

