package org.example;

import java.util.Set;
import java.util.concurrent.Phaser;

// Implements runnable interface, because we can implement multiple interface and we can also inherit the class
public class CrawlerTask implements Runnable{
    private final URLStore urlStore;
    private final URLFetcher urlFetcher;
    private final int maxDepth;
    private final int currentDepth;
    private final Phaser phaser;

    // Phaser keeps giving information how much task is done by thread and how much task is left and how many threads are working
    // Call resistor function, phaser knows how many threads are working

    public CrawlerTask(URLStore urlStore, URLFetcher urlFetcher, int maxDepth, int currentDepth, Phaser phaser) {
        this.urlStore = urlStore;
        this.urlFetcher = urlFetcher;
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
            if(url == null || currentDepth == maxDepth) return;

            Set<String> links = urlFetcher.fetchLinks(url);
            for(String link : links){
                if(urlStore.addUrl(link)){
                    phaser.register();
                    WebCrawler.submitTask(urlStore,urlFetcher,currentDepth+1,maxDepth);
                    // By passing values in submitTask spawns new url
                }
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
