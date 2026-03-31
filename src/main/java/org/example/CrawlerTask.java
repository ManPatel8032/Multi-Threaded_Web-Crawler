package org.example;

import java.util.Set;
import java.util.concurrent.Phaser;

// Implements runnable interface, because we can implement multiple interface and we can also inherit the class
public class CrawlerTask implements Runnable{
    private final URLStore urlStore;
    private final URLFetcher urlFetcher;
    private final BrokenLinkStore brokenLinkStore;
    private final RobotsTxtParser robotsTxtParser;
    private final int maxDepth;
    private final int currentDepth;
    private final Phaser phaser;

    // File extensions that Jsoup cannot handle (binary/download files)
    private static final Set<String> FILE_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".csv",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp",
            ".zip", ".rar", ".tar", ".gz", ".7z",
            ".mp3", ".mp4", ".avi", ".mov", ".wav",
            ".exe", ".msi", ".dmg", ".sh",
            ".txt", ".json", ".xml", ".md", ".log", ".reg",
            ".h5", ".ppt", ".pptx"
    );

    // Phaser keeps giving information how much task is done by thread and how much task is left and how many threads are working
    // Call resistor function, phaser knows how many threads are working

    public CrawlerTask(URLStore urlStore, URLFetcher urlFetcher, BrokenLinkStore brokenLinkStore, RobotsTxtParser robotsTxtParser, int maxDepth, int currentDepth, Phaser phaser) {
        this.urlStore = urlStore;
        this.urlFetcher = urlFetcher;
        this.brokenLinkStore = brokenLinkStore;
        this.robotsTxtParser = robotsTxtParser;
        this.maxDepth = maxDepth;
        this.currentDepth = currentDepth;
        this.phaser = phaser;
    }

    // Check if a URL points to a file download (not an HTML page)
    private static boolean isFileUrl(String url) {
        String lower = url.toLowerCase().split("\\?")[0]; // remove query params
        for (String ext : FILE_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }

    //When we create thread, and we want to perform task using that thread then we use run method
    @Override
    public void run() {

        // We will know which thread handles which URL using this
        try{
            String url = urlStore.getNextUrl();
            System.out.println(Thread.currentThread().getName()+" "+url);
            if (url == null) return;

            // Skip file download URLs — Jsoup can't parse binary files
            if (isFileUrl(url)) return;

            // Skip URLs disallowed by robots.txt
            if (!robotsTxtParser.isAllowed(url)) {
                System.out.println("  ↳ Skipped (blocked by robots.txt)");
                return;
            }

            // Fetch the page and check its status (even at max depth)
            URLFetcher.FetchResult result = urlFetcher.fetchLinks(url);
            urlStore.incrementProcessed();

            // Check if the current page itself is broken
            int pageStatus = result.getStatusCode();
            if (pageStatus >= 400 || pageStatus == -1) {
                brokenLinkStore.addBrokenLink(url, pageStatus, "(seed or parent page)");
            }

            // Stop processing child links if we've reached max depth
            if (currentDepth >= maxDepth) return;

            Set<String> links = result.getLinks();
            for(String link : links){
                try {
                    // Skip file download links and robots.txt disallowed links
                    if (isFileUrl(link) || !robotsTxtParser.isAllowed(link)) continue;

                    String linkHost = new java.net.URL(link).getHost();
                    boolean isInternal = linkHost.equals(WebCrawler.getBaseDomain());

                    if (isInternal && urlStore.addUrl(link)) {
                        // Internal link, not yet visited → will be crawled by fetchLinks()
                        // which already returns the status code, so no extra request needed
                        phaser.register();
                        try {
                            WebCrawler.submitTask(urlStore, urlFetcher, brokenLinkStore, robotsTxtParser, currentDepth + 1, maxDepth);
                        } catch (Exception e) {
                            phaser.arriveAndDeregister();
                        }
                    } else if (!isInternal) {
                        // External link → won't be crawled, so check its status separately
                        int linkStatus = urlFetcher.checkStatus(link);
                        if (linkStatus >= 400 || linkStatus == -1) {
                            brokenLinkStore.addBrokenLink(link, linkStatus, url);
                        }
                    }

                } catch (Exception ignored) {}
            }
        }
        catch (Exception e){
            System.out.println("Error in CrawlerTask");
        }
        finally{
            phaser.arriveAndDeregister();
        }

    }
}

