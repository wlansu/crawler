package com.crawler;

import java.io.IOException;
import java.util.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Main class for the crawler app.
 *
 * <p>
 *     Crawl a url and further crawl all urls found on that page to detect the most used words.
 *     These words are added to a HashMap so we can see what words are used on each url.
 * </p>
 */
class Crawler {

    /**
     * Crawl a url and return the words used and their count for the url and the urls on the initial page.
     *
     * <p>
     *     Call the Scraper class to do most of the work and then print out the results of the crawl.
     * </p>
     */
    void crawl(String startingUrl) {
        Map<String, Map<String, Integer>> wordsUsed = new HashMap<>();
        ArrayList<String> visited = new ArrayList<>();
        Scraper scraper = new Scraper();
        long start = System.currentTimeMillis();
        ArrayList<Element> toVisit = scraper.scrapeURL(startingUrl);
        for (Element url : toVisit) {
            String href = url.absUrl("href");
            if ((System.currentTimeMillis() - start) < 60000) {
                if (!visited.contains(href)) {
                    visited.add(href);
                    wordsUsed.put(href, scraper.scrapeWords(href));
                }
            } else {
                break;
            }
        }
        for (Object url : wordsUsed.entrySet()) {
            Map.Entry entry = (Map.Entry) url;
            System.out.print(entry.getKey() + ": ");
            System.out.println(entry.getValue());
        }
        long end = System.currentTimeMillis();
        float sec = (end - start) / 1000F;
        System.out.printf(
            "Crawled %s urls in %s seconds", wordsUsed.size(), sec
        );
    }
}


class Scraper {

    /**
     * Get the document from a url and return it.
     *
     * @param url The url to retrieve the document from.
     * @return The HTMLDocument.
     */
    private Document getDocument(String url) {

        String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:60.0) Gecko/20100101 Firefox/60.0";

        try
        {
            Connection connection = Jsoup.connect(url)
                // Set user agent and referrer to avoid getting detected as a bot. Probably not needed.
                .userAgent(USER_AGENT)
                .referrer("http://www.google.com");
            return connection.get();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Crawl a url and return all other urls on that page.
     *
     * @param url The url to scrape.
     * @return The list of urls on the page.
     */
    ArrayList<Element> scrapeURL(String url) {

        Document htmlDocument = this.getDocument(url);
        if (htmlDocument != null) {
            Elements urlsToCrawl = htmlDocument.select("a[href]");
            System.out.printf("Found %d urls on %s.", urlsToCrawl.size(), url);
            return urlsToCrawl;
        } else {
            return null;
        }
    }

    /**
     * Scrape a url to get all the words used on that page.
     *
     * @param url The url to scrape.
     * @return HashMap of all words used on a url along with their count.
     */
    Map<String, Integer> scrapeWords(String url) {

        Map<String, Integer> countMap = new HashMap<>();
        Document htmlDocument = this.getDocument(url);
        if (htmlDocument != null) {
            String text = htmlDocument.body().text();
            String[] words = text
                    .replaceAll("\\r\\n. |\\r. |\\n", " ")
                    .split("(?<!\\S)\\p{L}+(?!\\S)");  // TODO: regex needs tuning.
            for (String word : words) {
                if (countMap.containsKey(word)) {
                    countMap.replace(word, countMap.get(word) + 1);  // TODO: there is probably a better way to do this.
                } else {
                    countMap.put(word, 1);
                }
            }
            return countMap;
        } else {
            return null;
        }
    }
}
