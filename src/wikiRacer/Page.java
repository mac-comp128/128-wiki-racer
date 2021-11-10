package wikiRacer;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a wikipedia page containing the page title and other wiki pages that this page links.
 */
public class Page {
    private String title;
    private Set<String> links;
    private int numLinksInCommonWithEndPage;

    public Page(String title, Set<String> links, int numLinksInCommonWithEndPage){
        this.title = title;
        this.links = links;
        this.numLinksInCommonWithEndPage = numLinksInCommonWithEndPage;
    }

    public Page(String title){
        this.title = title;
        numLinksInCommonWithEndPage = 0;
        links = null;
    }

    public void setLinks(Set<String> links) {
        this.links = links;
    }

    public void setNumLinksInCommonWithEndPage(int numLinksInCommonWithEndPage) {
        this.numLinksInCommonWithEndPage = numLinksInCommonWithEndPage;
    }

    public String getTitle() {
        return title;
    }

    public int getNumLinksInCommonWithEndPage() {
        return numLinksInCommonWithEndPage;
    }

    public Set<String> getLinks() {
        return links;
    }

    /**
     * Pages are equal if they have the same title. Ignore links and numLinksInCommonWithEndPage since they may not be set
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page page = (Page) o;
        return Objects.equals(title, page.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    public String toString(){
        return "("+title+", "+numLinksInCommonWithEndPage+")";
    }
}
