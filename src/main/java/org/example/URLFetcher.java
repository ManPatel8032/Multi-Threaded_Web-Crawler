package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class URLFetcher {

    public static class FetchResult {
        private final Set<String> links;
        private final int statusCode;

        public FetchResult(Set<String> links, int statusCode) {
            this.links = links;
            this.statusCode = statusCode;
        }

        public Set<String> getLinks() {
            return links;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public FetchResult fetchLinks(String url) {
        Set<String> links = new HashSet<>();
        int statusCode = -1;
        int attempts = 0;

        while (attempts < 3) {
            try {
                Connection.Response response = Jsoup.connect(url)
                        .timeout(5000)
                        .ignoreHttpErrors(true)
                        .execute();

                statusCode = response.statusCode();
                Document document = response.parse();

                Elements anchorTags = document.select("a[href]");
                for (Element link : anchorTags) {
                    String extractedUrl = link.absUrl("href");
                    if (!extractedUrl.isEmpty()) {
                        links.add(extractedUrl);
                    }
                }
                break;
            } catch (IOException e) {
                attempts++;
            }
        }

        return new FetchResult(links, statusCode);
    }

    // Quick check: fetches only the status code of a URL without parsing the body
    public int checkStatus(String url) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                Connection.Response response = Jsoup.connect(url)
                        .timeout(5000)
                        .ignoreHttpErrors(true)
                        .followRedirects(true)
                        .execute();
                return response.statusCode();
            } catch (IOException e) {
                attempts++;
            }
        }
        return -1; // -1 means completely unreachable
    }
}
