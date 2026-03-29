package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class URLStore {
    private final ConcurrentHashMap<String, Boolean> visistedUrl= new ConcurrentHashMap<>();
    private final BlockingQueue<String> urlQueue= new LinkedBlockingQueue<>();

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

    public isQueueEmpty(){
        return urlQueue.isEmpty();
    }
}
