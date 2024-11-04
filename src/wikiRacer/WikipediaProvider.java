package wikiRacer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.concurrent.*;

/**
 * Utility class to get data from the Wikipedia API
 */
public class WikipediaProvider {

    private static final String USER_AGENT = "128-wikiracer/1.0 (https://macalester.edu/academics/mscs/) bjackson@macalester.edu"; // DO NOT MODIFY THIS.
    private static ConcurrentHashMap<String, Set<String>> cache;
    private static ConcurrentLinkedDeque<Long> requestTimestamps;
    private static final int MAX_REQUESTS_PER_SEC = 200;


    public WikipediaProvider() {
        cache = new ConcurrentHashMap<>();
        requestTimestamps = new ConcurrentLinkedDeque<>();
    }

    /**
     * Returns a list of titles for each linked wikipage contained on the wiki page with pageTitle
     * @param pageTitle
     * @return list of pages linked from pageTitle
     */
    public Set<String> getLinkTitles(String pageTitle) {
        //System.out.println("\t"+pageTitle);
        if (cache.containsKey(pageTitle)){
            return cache.get(pageTitle); // If we have already searched for this page, no reason to fetch it again from the web.
        }

        Set<String> links = extractLinks(pageTitle);
        cache.put(pageTitle, links);

        return links;
    }

    /**
     * Extracts the set of links from a wikipedia page. Only includes links starting with "/wiki/" and ignores special
     * pages that include ":" or "#"
     * @param pageTitle
     * @return list of titles
     */
    private Set<String> extractLinks(String pageTitle) {
        String baseUrl = "https://en.wikipedia.org/api/rest_v1/page/html/"+pageTitle;
        try {

            // Rate Limiting Requests with sliding window
            long currentTime = System.currentTimeMillis();
            if (requestTimestamps.isEmpty()){
                requestTimestamps.addLast(currentTime);
            }
            else{
                while (!requestTimestamps.isEmpty() && currentTime - requestTimestamps.peekFirst() > 1000L) {
                    requestTimestamps.pollFirst();
                }
                if (requestTimestamps.size() >= MAX_REQUESTS_PER_SEC) {
                    int offset = requestTimestamps.size() - MAX_REQUESTS_PER_SEC;
                    Iterator<Long> iter = requestTimestamps.descendingIterator();
                    long lastValidTimeStamp = 0;
                    for(int i=0; i < offset && iter.hasNext(); i++){
                        lastValidTimeStamp = iter.next();
                    }
                    Thread.sleep(1000 - (currentTime-lastValidTimeStamp));
                }
                currentTime = System.currentTimeMillis();
                requestTimestamps.addLast(currentTime);
            }


            Document htmlDoc = Jsoup.connect(baseUrl).userAgent(USER_AGENT).get();
            Set<String> links = new HashSet<>();
            Elements elements = htmlDoc.select("body").select("a[rel=\"mw:WikiLink\"]");
            for(Element elem : elements){
                String link = elem.attr("href");
                if (link.startsWith("./") && !link.contains(":") && !link.contains("#")) {
                    link = link.substring(2); //6 remove /wiki/
                    links.add(link);
                }
            }
            return links;

        } catch (Exception ex) {
            //System.err.println("http fetch of '" + baseUrl + "' failed: "+ex.getMessage());
            return new HashSet<>();
        }
    }
}
