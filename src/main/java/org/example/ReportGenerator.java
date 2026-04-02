package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

public class ReportGenerator {

    public static void generateReports(URLStore urlStore, BrokenLinkStore brokenLinkStore) {
        System.out.println("Generating CSV reports...");
        generateVisitedUrlsReport(urlStore.getAllVisitedUrls());
        generateBrokenLinksReport(brokenLinkStore.getBrokenLinks());
        System.out.println("\nSUCCESS: Reports successfully generated!");
        System.out.println("Check your project folder for 'visited_urls_report.csv' and 'broken_links_report.csv'.");
    }

    private static void generateVisitedUrlsReport(Set<String> visitedUrls) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("visited_urls_report.csv"))) {
            writer.println("Visited URL");
            for (String url : visitedUrls) {
                writer.println(url);
            }
        } catch (IOException e) {
            System.err.println("Failed to write visited URLs report: " + e.getMessage());
        }
    }

    private static void generateBrokenLinksReport(List<BrokenLinkStore.BrokenLink> brokenLinks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("broken_links_report.csv"))) {
            writer.println("HTTP Status Code,Broken URL,Found On (Parent URL)");
            for (BrokenLinkStore.BrokenLink link : brokenLinks) {
                // Quotes added around URLs in case they contain commas
                writer.printf("%d,\"%s\",\"%s\"%n", link.statusCode(), link.brokenUrl(), link.parentUrl());
            }
        } catch (IOException e) {
            System.err.println("Failed to write broken links report: " + e.getMessage());
        }
    }
}