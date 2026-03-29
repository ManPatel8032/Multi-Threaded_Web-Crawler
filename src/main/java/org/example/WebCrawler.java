package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

public class WebCrawler {
    private static Phaser phaser;
    // static is used, why?
    private static ExecutorService executorService;

    public static void main(String[] args) {
        CrawlerTask ct = new CrawlerTask();
        Thread t = new Thread(ct);
        t.start();
    }
}
