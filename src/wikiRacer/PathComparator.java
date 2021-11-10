package wikiRacer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to sort lists of pages in the priority queue.
 * Orders based on the number of links in common between the last page in the path and the target end page
 */
public class PathComparator implements Comparator<List<Page>> {

    public int compare(List<Page> path1, List<Page> path2){
        int path1LinksInCommon = path1.get(path1.size()-1).getNumLinksInCommonWithEndPage();
        int path2LinksInCommon = path2.get(path2.size()-1).getNumLinksInCommonWithEndPage();
        return -1 * Integer.compare(path1LinksInCommon, path2LinksInCommon); // Multiply by -1 since priority queue is a min-heap and we want pages with the most in common to be the highest priority!
    }

}
