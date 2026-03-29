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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter your URL : ");
        String url = sc.nextLine();

        System.out.println("Enter the depth of the crawler : ");
        final int MAX_DEPTH = sc.nextInt();

        System.out.println("Enter the number of threads : ");
        final int MAX_THREADS = sc.nextInt();

        URLStore urlStore = new URLStore();
        URLFetcher urlFetcher = new URLFetcher();
        phaser = new Phaser(1);

        executorService = Executors.newFixedThreadPool(MAX_THREADS);

        urlStore.addUrl(url);
        long start = System.currentTimeMillis();

        submitTask(urlStore,urlFetcher,0,MAX_DEPTH);
        phaser.awaitAdvance(phaser.getPhase());

        executorService.shutdown();
        System.out.println("Total execution time : " + (System.currentTimeMillis() - start));
    }

    public static void submitTask(){}

    public static void submitTask(URLStore urlStore, URLFetcher urlFetcher, int currentDepth, int maxDepth) {
        executorService.submit(new CrawlerTask(urlStore,urlFetcher,maxDepth,currentDepth,phaser));
    }
}
