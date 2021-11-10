package wikiRacer;

import java.util.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class WikiRacer {
    private PriorityQueue<List<Page>> paths;
    private WikipediaProvider wikiProvider;
    private Graph graph;

    public WikiRacer() {
        wikiProvider = new WikipediaProvider();
    }

    public List<Page> findWikiPath(String startPageTitle, String endPageTitle){
        graph = new SingleGraph("Wikipedia Graph", false, true);
        graph.display();
        graph.addAttribute("ui.stylesheet", "edge { fill-color: grey; arrow-size: 3px, 2px; } node { size: 2px, 2px;} edge.onPath { fill-color: red; z-index: 99999999; } edge.potentialPath { fill-color: yellow; z-index: 88888888; }");

        // Uncomment the following two lines to improve the quality of the graph display (while taking longer to render)
        //graph.addAttribute("ui.quality");
        //graph.addAttribute("ui.antialias");


        //TODO: Find a path between the start page and endPage:
        // Get the set of link titles for the end page from the wikipedia provider. We'll use this later in finding the number of links in common between pages and the endPage.
        // Create an instance of PathComparator
        // Initialize the paths priorityqueue to use your comparator (a path is a List<Page>)
        // Create a Page representing the start page, setting its title, page links, and number of links in common with the end page (Hint: look at the getNumLinksInCommon method)
        // Call addLinksToGraph with the start page as a parameter. This will draw the page and its linked pages as vertices with edges between them.
        // Create/add a path list containing the start page to the queue.
        // While the queue is not empty:
        //      Dequeue the highest priority partial-path from the front of the queue.
        //      Print the partial-path to see the current status
        //      If the partial-path has at least two pages:
        //          Call colorPotentialPathEdge with the last two pages in the path as parameters. This will color the edge yellow in the visualization.
        //      Get the set of links of the current page i.e. the page at the end of the just dequeued path.
        //      For each link:
        //          Create a new page for the link
        //          If the link page is not contained in the partial-path already (we haven't visited it):
        //              Get the page's links and numLinksInCommonWithEndPage and update the page's variables
        //              Call addLinksToGraph with the page as a parameter to update the visualization
        //              Create a copy of the current partial-path (List<Page> partialPathCopy = new ArrayList<>(partialPath);)
        //              Add the new page to the copied path
        //              If the link equals the endPageTitle:
        //                  We found the path!, return the copied path
        //              Otherwise, add the copied path to the queue
        // If while loop exits, no path was found so return an empty List<Page>


        return new ArrayList<>(0);
    }

    /**
     * Gets the set of links that are in common between pageLinks and endPageLinks
     * @param pageLinks
     * @param endPageLinks
     * @return
     */
    private int getNumLinksInCommon(Set<String> pageLinks, Set<String> endPageLinks){
        Set<String> intersection = new HashSet<>(pageLinks);
        intersection.retainAll(endPageLinks);
        return intersection.size();
    }

    /**
     * Adds all the links on page as vertices in the graph and draws directed edges between page and each link
     * @param page
     */
    private void addLinksToGraph(Page page){
        Set<String> links = page.getLinks();
        String title = page.getTitle();
        if (links != null && graph != null){
            for(String link : links) {
                graph.addEdge(title+link, title, link, true);
            }
        }
    }

    /**
     * Color the edge between start and end vertex yellow as a potential path
     * @param startVertex
     * @param endVertex
     */
    public void colorPotentialPathEdge(Page startVertex, Page endVertex){
        Edge edge = graph.getEdge(startVertex.getTitle()+endVertex.getTitle());
        if (edge != null) {
            edge.setAttribute("ui.class", "potentialPath");
        }
    }

    /**
     * Color the path red
     * @param path
     */
    public void colorPath(List<Page> path){
        for(int i=0; i < path.size()-1; i++) {
            Edge edge = graph.getEdge(path.get(i).getTitle()+path.get(i+1).getTitle());
            if (edge != null) {
                edge.setAttribute("ui.class", "onPath");
            }
        }
    }

    public static void main(String[] args){
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        WikiRacer racer = new WikiRacer();

        List<Page> path = racer.findWikiPath("Fruit", "Strawberry");
        //List<Page> path = racer.findWikiPath("Macalester_College", "UN");
        //List<Page> path = racer.findWikiPath("Milkshake", "Gene");

        // Draw the final path in red in the visualization
        racer.colorPath(path);
        System.out.println("Final Path: "+path.toString());
    }


}
