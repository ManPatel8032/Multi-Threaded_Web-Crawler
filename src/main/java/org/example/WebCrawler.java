package org.example;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class WebCrawler {
    private static Phaser phaser;
    // static is used, why?
    private static ExecutorService executorService;
    // Executor Service manages how many threads should be created

    private static String baseDomain;

    public static String getBaseDomain() {
        return baseDomain;
    }

    private static String extractDomain(String url) {
        try {
            return new java.net.URL(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter your URL : ");
        String url = sc.nextLine();

        baseDomain = extractDomain(url);

        System.out.println("Enter the depth of the crawler : ");
        final int MAX_DEPTH = sc.nextInt();

        System.out.println("Enter the number of threads : ");
        final int MAX_THREADS = sc.nextInt();

        URLStore urlStore = new URLStore();
        URLFetcher urlFetcher = new URLFetcher();
        BrokenLinkStore brokenLinkStore = new BrokenLinkStore();
        phaser = new Phaser(1);

        executorService = Executors.newFixedThreadPool(MAX_THREADS);

        urlStore.addUrl(url);
        long start = System.currentTimeMillis();

        submitTask(urlStore, urlFetcher, brokenLinkStore, 0, MAX_DEPTH);
        phaser.awaitAdvance(phaser.getPhase());

        executorService.shutdown();
        System.out.println("Total execution time : " + (System.currentTimeMillis() - start) + " ms");

        System.out.println("Total URLs processed: " + urlStore.getTotalProcessed());

        System.out.println("\nVisited URLs:");
        for (String visited : urlStore.getAllVisitedUrls()) {
            System.out.println(visited);
        }

        // Print the Broken Link Report
        brokenLinkStore.printReport();
    }

    public static void submitTask(URLStore urlStore, URLFetcher urlFetcher, BrokenLinkStore brokenLinkStore, int currentDepth, int maxDepth) {
        executorService.submit(new CrawlerTask(urlStore, urlFetcher, brokenLinkStore, maxDepth, currentDepth, phaser));
    }

}
