package com.crawler;

/**
 * Main entry point for the crawler app.
 */
public class Main {

    /**
     * Main method.
     *
     * <p>
     *     Calls the crawl method with a specific hardcoded url, this would of course be dynamic normally.
     * </p>
     * @param args No args needed, we could add the url as one.
     */
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.crawl("https://en.wikipedia.org/wiki/Big_data");
    }
}
