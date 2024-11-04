package wikiRacer;

import java.util.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Multi-threaded WikiRacer game solver.
 */
public class WikiRacer {
    private PriorityBlockingQueue<List<Page>> paths;
    private WikipediaProvider wikiProvider;
    private Graph graph;

    public WikiRacer() {
        wikiProvider = new WikipediaProvider();
    }

    /**
     * Finds a path from startPageTitle to endPageTitle in the English Wikipedia graph
     * @param startPageTitle
     * @param endPageTitle
     * @return
     */
    public List<Page> findWikiPath(String startPageTitle, String endPageTitle){
        graph = Graphs.synchronizedGraph(new SingleGraph("Wikipedia Graph", false, true));
        graph.display();
        graph.setAttribute("ui.stylesheet", "edge { fill-color: grey; arrow-size: 3px, 2px; } node { size: 2px, 2px;} edge.onPath { fill-color: red; z-index: 99999999; } edge.potentialPath { fill-color: yellow; z-index: 88888888; }");

        // Comment the following two lines to lower the quality of the graph display (while taking less time to render)
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        // Used for getNumLinksInCommon
        Set<String> endPageLinks = wikiProvider.getLinkTitles(endPageTitle);

        //TODO: Declare and intialize the comparator and paths PriorityBlockingQueue
        


        // Initialize the algorithm with the starting page
        List<Page> initialPath = new ArrayList<>();
        Set<String> pageLinks = wikiProvider.getLinkTitles(startPageTitle);
        Page startPage = new Page(startPageTitle, pageLinks, getNumLinksInCommon(pageLinks, endPageLinks));
        addLinksToGraph(startPage); // Draws the page and its linked pages as vertices with edges between them.
        initialPath.add(startPage);
        paths.offer(initialPath);

        while(!paths.isEmpty()) {
            List<Page> bestPath = null;
            //TODO:
            // 1. Dequeue the highest priority partial-path from the front of the queue and assign it to a the bestPath variable.
            // 2. Print the partial-path to see the current status
            // 3. Call addLinksToGraph with the last page of the bestPath as a parameter to update the visualization
           
        



            if (bestPath.size() > 1){
                colorPotentialPathEdge(bestPath.get(bestPath.size()-2), bestPath.get(bestPath.size()-1));
            }

            Set<String> links = bestPath.get(bestPath.size()-1).getLinks();

            List<Thread> threads = new ArrayList<>(links.size());

            for(String link : links){
                Page linkPage = new Page(link);
                // Creates a shallow copy of the path so we can add a new page to the end of the path.
                List<Page> bestPathCopy = new ArrayList<>(bestPath);
                
                // Have we found the target end page?
                if (link.equals(endPageTitle)){
                    bestPathCopy.add(linkPage);
                    // Stop all the current threads
                    try{
                        for(Thread thread : threads){
                            thread.interrupt();
                        }
                    } catch (SecurityException ex){}
                    return bestPathCopy;
                }

                // Create a new virtual thread (backed by operating system threads) with a Runnable lambda expression to describe the work.
                Thread thread = Thread.ofVirtual().start(() -> {
                    
                    //TODO:
                    // If the link page is not contained in the partial-path already (we haven't visited it):
                    //     Get the page's links and numLinksInCommonWithEndPage and update the page's variables with this data.
                    //     Add the new page to the bestPathCopy
                    //     Add the copied path to the queue
                    
                    



                });
                threads.add(thread);
            }

            // Wait for all the threads to finish their work before continuing
            try{
                for(Thread thread : threads){
                    thread.join(); // blocks, i.e. temporarily stops, the current thread that is iterating over the links until thread has finished.
                }
            } catch (InterruptedException ex){}
        }
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
        System.setProperty("org.graphstream.ui", "swing"); 
        WikiRacer racer = new WikiRacer();

        long start = System.currentTimeMillis();

        //List<Page> path = racer.findWikiPath("James_Gosling", "Google");
        //List<Page> path = racer.findWikiPath("Fruit", "Strawberry");
        List<Page> path = racer.findWikiPath("Macalester_College", "UN");
        //List<Page> path = racer.findWikiPath("Milkshake", "Gene");
        //List<Page> path = racer.findWikiPath("Apple", "Zebra");
        //List<Page> path = racer.findWikiPath("Tomato", "Taylor_Swift");


        long end = System.currentTimeMillis();
        double elapsedTimeSec = ((double) (end - start))/1000;
        System.out.println("Search took "+ elapsedTimeSec + " seconds");

        // Draw the final path in red in the visualization
        racer.colorPath(path);
        System.out.println("Final Path: "+path.toString());
    }


}
