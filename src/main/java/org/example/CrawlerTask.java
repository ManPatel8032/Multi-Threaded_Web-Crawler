package org.example;

import java.util.Set;
import java.util.concurrent.Phaser;

// Implements runnable interface, because we can implement multiple interface and we can also inherit the class
public class CrawlerTask implements Runnable{
    private final URLStore urlStore;
    private final URLFetcher urlFetcher;
    private final BrokenLinkStore brokenLinkStore;
    private final int maxDepth;
    private final int currentDepth;
    private final Phaser phaser;

    // Phaser keeps giving information how much task is done by thread and how much task is left and how many threads are working
    // Call resistor function, phaser knows how many threads are working

    public CrawlerTask(URLStore urlStore, URLFetcher urlFetcher, BrokenLinkStore brokenLinkStore, int maxDepth, int currentDepth, Phaser phaser) {
        this.urlStore = urlStore;
        this.urlFetcher = urlFetcher;
        this.brokenLinkStore = brokenLinkStore;
        this.maxDepth = maxDepth;
        this.currentDepth = currentDepth;
        this.phaser = phaser;
    }

    //When we create thread, and we want to perform task using that thread then we use run method
    @Override
    public void run() {

        // We will know which thread handles which URL using this
        try{
            String url = urlStore.getNextUrl();
            System.out.println(Thread.currentThread().getName()+" "+url);
            if (url == null) return;
            if (currentDepth >= maxDepth) return;

            URLFetcher.FetchResult result = urlFetcher.fetchLinks(url);
            urlStore.incrementProcessed();

            // Check if the current page itself is broken
            int pageStatus = result.getStatusCode();
            if (pageStatus >= 400 || pageStatus == -1) {
                brokenLinkStore.addBrokenLink(url, pageStatus, "(seed or parent page)");
            }

            Set<String> links = result.getLinks();
            for(String link : links){
                try {
                    String linkHost = new java.net.URL(link).getHost();

                    // Check every discovered link's status to detect broken links
                    int linkStatus = urlFetcher.checkStatus(link);
                    if (linkStatus >= 400 || linkStatus == -1) {
                        brokenLinkStore.addBrokenLink(link, linkStatus, url);
                    }

                    if(linkHost.equals(WebCrawler.getBaseDomain()) && urlStore.addUrl(link)){
                        phaser.register();
                        try {
                            WebCrawler.submitTask(urlStore, urlFetcher, brokenLinkStore, currentDepth + 1, maxDepth);
                        } catch (Exception e) {
                            phaser.arriveAndDeregister();
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
