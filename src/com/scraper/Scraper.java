package com.scraper;

import java.io.IOException;
import java.util.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class Scraper {

    private Map<String, Map<String, Integer>> wordsUsed = new HashMap<>();
    private ArrayList<String> visited = new ArrayList<>();

    /**
     * Crawl a url and return the words used and their count for the url and the urls on the initial page.
     */
    void crawl(String startingUrl) {

        long start = System.currentTimeMillis();
        ArrayList<Element> toVisit = this.scrapeURL(startingUrl);

        // Add the startingUrl as well since we won't be scraping it in the loop.
        visited.add(startingUrl);
        wordsUsed.put(startingUrl, this.scrapeWords(startingUrl));

        if (toVisit != null) {
            for (Element url : toVisit) {
                String href = url.absUrl("href");
                if ((System.currentTimeMillis() - start) < 60000) {
                    if (!visited.contains(href)) {
                        visited.add(href);
                        wordsUsed.put(href, this.scrapeWords(href));
                    }
                } else {
                    break;
                }
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
    private ArrayList<Element> scrapeURL(String url) {

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
    private Map<String, Integer> scrapeWords(String url) {

        Map<String, Integer> countMap = new HashMap<>();
        Document htmlDocument = this.getDocument(url);
        if (htmlDocument != null) {
            String text = htmlDocument.body().text();
            String[] words = text
                    .split(" ");  // TODO: regex needs tuning, depending on what we want.
            for (String word : words) {
                if (word != null && !word.isEmpty()) {
                    // TODO: there is probably a better way to do this.
                    if (countMap.containsKey(word)) {
                        countMap.replace(word, countMap.get(word) + 1);
                    } else {
                        countMap.put(word, 1);
                    }
                }
            }
            // If we only want the most frequently used words we'd have to slice the Map, passing the value in.
            return sortByValue(countMap);
        } else {
            return null;
        }
    }

    /**
     * Sort a Map by value instead of by key.
     *
     * @param unsortedMap The map to be sorted.
     * @return The sorted map.
     */
    private static Map sortByValue(Map unsortedMap) {
        Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }
}


/**
 * Compare the value of a Map instead of the key.
 */
class ValueComparator implements Comparator {

    private Map map;

    /**
     * Set the map for the class.
     *
     * @param map a Map.
     */
    ValueComparator(Map map) {
        this.map = map;
    }

    /**
     * Compare a value with another value.
     *
     * @param keyA First value to compare.
     * @param keyB Second value to compare.
     * @return a negative integer, zero, or a positive integer as this object
     *  is less than, equal to, or greater than the specified object.
     */
    public int compare(Object keyA, Object keyB) {
        Comparable valueA = (Comparable) map.get(keyA);
        Comparable valueB = (Comparable) map.get(keyB);
        return valueB.compareTo(valueA);
    }
}
