package wikiRacer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vendor.org.json.JSONArray;
import vendor.org.json.JSONException;
import vendor.org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class to get data from the Wikipedia API
 */
public class WikipediaProvider {

    private static final String USER_AGENT = "128-wikiracer/1.0 (https://macalester.edu/academics/mscs/; bjackson@macalester.edu)"; // DO NOT MODIFY THIS.
    private static Map<String, Set<String>> cache;


    public WikipediaProvider() {
        cache = new HashMap<>();
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

        Set<String> links = extractLinks(pageTitle);//getLinksFromAPI(pageTitle);
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
        String baseUrl = "https://en.wikipedia.org/wiki/"+pageTitle;
        try {
            Document htmlDoc = Jsoup.connect(baseUrl).get();
            Set<String> links = new HashSet<>();
            Elements elements = htmlDoc.select("div#bodyContent").select("a[href]");
            for(Element elem : elements){
                String link = elem.attr("href");
                if (link.startsWith("/wiki/") && !link.contains(":") && !link.contains("#")) {
                    link = link.substring(6); // remove /wiki/
                    links.add(link);
                }
            }
            //System.out.println("Size: "+links.size());
            return links;

        } catch (Exception ex) {
            System.err.println("http fetch of '" + baseUrl + "' failed: "+ex.getMessage());
            //ex.printStackTrace();
            return new HashSet<>();
        }
    }

    /**
     * Alternative to extract links that uses the wiki api. Probably is slower because it uses many more requests, but this was not tested
     * @param pageTitle
     * @return
     */
    private List<String> getLinksFromAPI(String pageTitle){
        List<String> links = new ArrayList<String>();
        boolean shouldContinue = true;

        String baseQuery = "https://en.wikipedia.org/w/api.php?action=query&titles=" + encodeValue(pageTitle) + "&prop=links&plnamespace=0&pllimit=max&format=json";
        String pageQuery = baseQuery;

        do {
            JSONObject pageResult = sendGET(pageQuery);

            if (pageResult.keySet().contains("continue")){
                shouldContinue = true;
                pageQuery = baseQuery + "&plcontinue="+encodeValue(pageResult.getJSONObject("continue").getString("plcontinue"));
            }
            else {
                shouldContinue = false;
            }

            JSONObject pageObjects = pageResult.getJSONObject("query").getJSONObject("pages");
            parseLinks(links, pageObjects);
        } while (shouldContinue);
        return links;
    }

    private void parseLinks(List<String> links, JSONObject pageObjects){
        Iterator<String> it = pageObjects.keys();
        while(it.hasNext()){
            JSONObject pageObject = pageObjects.getJSONObject(it.next());
            if (pageObject.keySet().contains("links")) {
                JSONArray pageLinks = pageObject.getJSONArray("links");
                for (int j = 0; j < pageLinks.length(); j++) {
                    links.add(pageLinks.getJSONObject(j).getString("title"));
                }
            }
        }
    }

    /**
     * Encode url strings into utf-8 format escaping characters as needed
     * @param value
     * @return
     */
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    /**
     * Sends an HTTP get request to the url that was passed in as a parameter.
     * @param url
     * @return a json object with the response or null if there is an error.
     */
    private static JSONObject sendGET(String url) {
        StringBuffer response = new StringBuffer();
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            //System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response.toString());

                return new JSONObject(response.toString());
            } else {
                System.out.println("GET request failed with response code: "+responseCode);
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            System.out.println(response);
            throw new RuntimeException(e);
        }

        return null;
    }
}
