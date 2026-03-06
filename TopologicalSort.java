import java.util.HashMap;
import java.util.Set;

/**
 * Performs topological sort on the cell dependency graph.
 * Evaluates cells during the sort (post-order) to compute their values.
 * Detects cycles and returns whether the sort was successful (acyclic).
 *
 * @author Jackson Steger
 * @version Winter 2026
 */
public class TopologicalSort {
    
    // Cell states for topsort
    private static final int unevalutedCell = 0;
    private static final int evaluatingCell = 1;
    private static final int evaluatedCell = 2;
    
    private Spreadsheet mySpreadsheet;
    private DependencyGraph myDependencyGraph;
    private HashMap<Cell, Integer> myEvaluateState;
    private boolean myCycleDetected;
    
    /**
     * Constructs a TopologicalSort object with references to the spreadsheet and dependency graph.
     * 
     * @param theSpreadsheet - the Spreadsheet object containing all cells
     * @param dependencyGraph - the DependencyGraph showing cell dependencies
     */
    public TopologicalSort(Spreadsheet theSpreadsheet, DependencyGraph theDependencyGraph) {
        mySpreadsheet = theSpreadsheet;
        myDependencyGraph = theDependencyGraph;
        myEvaluateState = new HashMap<>();
        myCycleDetected = false;
    }
    
    /**
     * Performs a topological sort (DFS-based) on all cells in the spreadsheet.
     * Evaluates each cell after all its dependencies have been evaluated.
     * 
     * @return true if the sort was successful (no cycles), false if a cycle was found
     */
    public boolean topsort() {
        // Initializing all cells as unvisited
        Set<Cell> allCells = myDependencyGraph.getAllCells();
        for (Cell cell : allCells) {
            myEvaluateState.put(cell, unevalutedCell);
        }
        
        myCycleDetected = false;
        
        // Performs a DFS on all unvisited cells
        for (Cell cell : allCells) {
            if (myEvaluateState.get(cell) == unevalutedCell && !myCycleDetected) {
                dfs(cell);
            }
        }
        
        return !myCycleDetected;
    }
    
    /**
     * Depth-First Search to evaluate cells and find cycles.
     * Uses post-order traversal: visit all dependencies first, then evaluate the cell.
     * 
     * @param cell - the cell to visit
     */
    private void dfs(Cell cell) {
        if (myCycleDetected) {
            return;
        }
        
        // Mark as visiting to detect cycles
        myEvaluateState.put(cell, evaluatingCell);
        
        // Visit all cells that this cell depends on (incoming dependencies)
        Set<Cell> dependencies = myDependencyGraph.getDependencies(cell);
        for (Cell dependency : dependencies) {
            int state = myEvaluateState.getOrDefault(dependency, unevalutedCell);
            
            if (state == unevalutedCell) {
                // Recursively visit unvisited dependencies
                dfs(dependency);
            } else if (state == evaluatingCell) {
                // Cycle
                System.out.println("Error: Cycle detected in dependency graph.");
                myCycleDetected = true;
                return;
            }
            // If state == VISITED, we've already processed this dependency
        }
        
        // Post-order
        if (!myCycleDetected) {
            cell.evaluate(mySpreadsheet);
            myEvaluateState.put(cell, evaluatedCell);
        }
    }
    
    /**
     * Returns true if a cycle was found during the sort.
     * 
     * @return true if a cycle exists, false otherwise
     */
    public boolean hasCycle() {
        return myCycleDetected;
    }
}
