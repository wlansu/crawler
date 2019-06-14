package com.crawler;

import java.io.IOException;
import java.util.*;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Main class for the crawler app.
 *
 * <p>
 *     Crawl a specific hardcoded url and further crawl all urls found on that page to detect the most
 *     frequently used words. These words are added to a HashMap so we can see what words are most frequent per url.
 * </p>
 */
public class Crawler {

    private Map<String, Map<String, Integer>> crawled = new HashMap<>();
    private String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:60.0) Gecko/20100101 Firefox/60.0";

    /**
     * Main method.
     *
     * <p>
     *     Call the crawl method to do most of the work and then print out the results of the crawl.
     * </p>
     *
     * @param args None
     */
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        long start = System.currentTimeMillis();
        crawler.crawl();
        for (Object url : crawler.crawled.entrySet()) {
            Map.Entry me = (Map.Entry) url;
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
        }
        long end = System.currentTimeMillis();
        float sec = (end - start) / 1000F;
        System.out.printf(
            "Crawled %s urls in %s seconds", crawler.crawled.size(), sec
        );
    }

    /**
     * Crawl a hardcoded url and call the mostFrequentWords method on all urls found on that page.
     *
     * <p>
     *     There is a time limit of 60 seconds, in combination with the printing of the values in the main method
     *     we will run a little over that.
     * </p>
     */
    private void crawl() {
        try
        {
            Connection connection = Jsoup.connect(
                "https://en.wikipedia.org/wiki/Big_data")
                .userAgent(USER_AGENT)
                .referrer("http://www.google.com");
            Document htmlDocument = connection.get();

            Elements urlsToCrawl = htmlDocument.select("a[href]");
            System.out.printf("Found %d urls to crawl.", urlsToCrawl.size());
            long startTime = System.currentTimeMillis();
            for (Element url : urlsToCrawl) {
                if ((System.currentTimeMillis() - startTime) < 100) {
                    this.mostFrequentWords(url.absUrl("href"));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            // Just carry on.
        }
    }

    /**
     * Find the most frequently used words on a webpage and add them to the crawled HashMap.
     *
     * <p>
     *     Use a replace and regex to parse the text on the webpage into something approaching readable text.
     *     This still needs improving though.
     *
     *     Add a word to the countMap when first found in the text and increment the value if found again.
     * </p>
     *
     * @param url The url to visit.
     */
    private void mostFrequentWords(String url) throws IOException {

        Map<String, Integer> countMap = new HashMap<>();

        try {
            Connection connection = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer("http://www.google.com");
            String text = connection.get().body().text();
            System.out.println(text);
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
            // TODO: This sorts by keys, apparently I could use Guava to sort by values.
            Map<String, Integer> sorted = new TreeMap<>(countMap);
            this.crawled.put(url, sorted);
        } catch (HttpStatusException | UnsupportedMimeTypeException e) {
            // This is fine ... ;)
        }
    }
}

