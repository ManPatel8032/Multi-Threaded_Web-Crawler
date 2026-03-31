package org.example;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class URLStore {
    private final ConcurrentHashMap<String, Boolean> visitedUrl = new ConcurrentHashMap<>();
    private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>(1000);

    public boolean addUrl(String url) {
        if (visitedUrl.putIfAbsent(url, true) == null) {
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

    private final AtomicInteger totalProcessed = new AtomicInteger();

    public void incrementProcessed() {
        totalProcessed.incrementAndGet();
    }

    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    public Set<String> getAllVisitedUrls() {
        return visitedUrl.keySet();
    }
}
