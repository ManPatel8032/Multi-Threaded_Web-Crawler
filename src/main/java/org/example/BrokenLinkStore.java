package org.example;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BrokenLinkStore {

    // Record to hold info about a broken link
    public record BrokenLink(String brokenUrl, int statusCode, String parentUrl) {}

    private final CopyOnWriteArrayList<BrokenLink> brokenLinks = new CopyOnWriteArrayList<>();

    public void addBrokenLink(String brokenUrl, int statusCode, String parentUrl) {
        brokenLinks.add(new BrokenLink(brokenUrl, statusCode, parentUrl));
    }

    public List<BrokenLink> getBrokenLinks() {
        return brokenLinks;
    }

    public int getCount() {
        return brokenLinks.size();
    }

    public void printReport() {
        System.out.println("\n========================================");
        System.out.println("       BROKEN LINK REPORT");
        System.out.println("========================================");

        if (brokenLinks.isEmpty()) {
            System.out.println("No broken links found!");
        } else {
            System.out.println("Total broken links found: " + brokenLinks.size());
            System.out.println("----------------------------------------");

            int index = 1;
            for (BrokenLink link : brokenLinks) {
                System.out.printf("%d. [HTTP %d] %s%n", index, link.statusCode(), link.brokenUrl());
                System.out.printf("   Found on: %s%n", link.parentUrl());
                index++;
            }
        }

        System.out.println("========================================\n");
    }
}
