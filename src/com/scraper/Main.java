package com.scraper;

/**
 * Main entry point for the scraper app.
 */
public class Main {

    /**
     * Main method.
     *
     * Calls the crawl method with a specific hardcoded url, this would of course be dynamic normally.
     *
     * @param args No args needed, we could add the url as one.
     */
    public static void main(String[] args) {
        Scraper scraper = new Scraper();
        scraper.crawl("https://en.wikipedia.org/wiki/Big_data");
    }
}
