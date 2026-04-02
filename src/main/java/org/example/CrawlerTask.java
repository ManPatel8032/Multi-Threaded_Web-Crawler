package org.example;

import java.util.Set;
import java.util.concurrent.Phaser;

public class CrawlerTask implements Runnable {
    private final URLStore urlStore;
    private final URLFetcher urlFetcher;
    private final BrokenLinkStore brokenLinkStore;
    private final RobotsTxtParser robotsTxtParser;
    private final int maxDepth;
    private final int currentDepth;
    private final Phaser phaser;

    private static final Set<String> FILE_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".csv",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp",
            ".zip", ".rar", ".tar", ".gz", ".7z",
            ".mp3", ".mp4", ".avi", ".mov", ".wav",
            ".exe", ".msi", ".dmg", ".sh",
            ".txt", ".json", ".xml", ".md", ".log", ".reg",
            ".h5", ".ppt", ".pptx"
    );

    public CrawlerTask(URLStore urlStore, URLFetcher urlFetcher, BrokenLinkStore brokenLinkStore, RobotsTxtParser robotsTxtParser, int maxDepth, int currentDepth, Phaser phaser) {
        this.urlStore = urlStore;
        this.urlFetcher = urlFetcher;
        this.brokenLinkStore = brokenLinkStore;
        this.robotsTxtParser = robotsTxtParser;
        this.maxDepth = maxDepth;
        this.currentDepth = currentDepth;
        this.phaser = phaser;
    }

    private static boolean isFileUrl(String url) {
        String lower = url.toLowerCase().split("\\?")[0];
        for (String ext : FILE_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }

    @Override
    public void run() {
        String url = null;
        try {
            url = urlStore.getNextUrl();
            if (url == null) return;

            System.out.println(Thread.currentThread().getName() + " crawling: " + url);

            if (isFileUrl(url) || !robotsTxtParser.isAllowed(url)) return;

            URLFetcher.FetchResult result = urlFetcher.fetchLinks(url);
            urlStore.incrementProcessed();

            int pageStatus = result.getStatusCode();
            if (pageStatus >= 400 || pageStatus == -1) {
                brokenLinkStore.addBrokenLink(url, pageStatus, "(seed or parent page)");
            }

            if (currentDepth >= maxDepth) return;

            Set<String> links = result.getLinks();
            for (String link : links) {
                try {
                    if (isFileUrl(link) || !robotsTxtParser.isAllowed(link)) continue;

                    String linkHost = new java.net.URL(link).getHost();
                    boolean isInternal = linkHost.contains(WebCrawler.getBaseDomain());

                    if (isInternal && urlStore.addUrl(link)) {
                        phaser.register();
                        try {
                            WebCrawler.submitTask(urlStore, urlFetcher, brokenLinkStore, robotsTxtParser, currentDepth + 1, maxDepth);
                        } catch (Exception e) {
                            phaser.arriveAndDeregister();
                        }
                    } else if (!isInternal) {
                        int linkStatus = urlFetcher.checkStatus(link);
                        if (linkStatus >= 400 || linkStatus == -1) {
                            brokenLinkStore.addBrokenLink(link, linkStatus, url);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Skipped invalid link format: " + link);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in CrawlerTask for URL [" + url + "]: " + e.getMessage());
        } finally {
            phaser.arriveAndDeregister();
        }
    }
}