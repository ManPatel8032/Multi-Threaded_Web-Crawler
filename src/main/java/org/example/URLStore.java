package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class URLStore {
    private final ConcurrentHashMap<String, Boolean> visistedUrl= new ConcurrentHashMap<>();
    private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>(1000);

    public boolean addUrl(String url) {
        if (visistedUrl.putIfAbsent(url, true) == null) {
            urlQueue.offer(url);
            return true;
        }
        return false;
    }

    public String getNextUrl() throws InterruptedException {
        return urlQueue.poll();
    }

    public boolean isQueueEmpty(){
        return urlQueue.isEmpty();
    }

    private final java.util.concurrent.atomic.AtomicInteger totalProcessed = new java.util.concurrent.atomic.AtomicInteger();

    public void incrementProcessed() {
        totalProcessed.incrementAndGet();
    }

    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    public java.util.Set<String> getAllVisitedUrls() {
        return visistedUrl.keySet();
    }
}
