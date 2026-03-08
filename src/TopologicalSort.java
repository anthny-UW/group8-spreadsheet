import javax.swing.*;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

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
    private static final int unevaluatedCell = 0;
    private static final int evaluatingCell = 1;
    private static final int evaluatedCell = 2;

    private final Spreadsheet mySpreadsheet;
    private final DependencyGraph myDependencyGraph;
    private final HashMap<Cell, Integer> myEvaluateState;
    private boolean myCycleDetected;

    /**
     * Constructs a TopologicalSort object with references to the spreadsheet and dependency graph.
     *
     * @param theSpreadsheet - the Spreadsheet object containing all cells
     * @param theDependencyGraph - the DependencyGraph showing cell dependencies
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
        // Initializing all cells as unevaluated
        Set<Cell> allCells = myDependencyGraph.getAllCells();
        for (Cell cell : allCells) {
            myEvaluateState.put(cell, unevaluatedCell);
        }

        myCycleDetected = false;

        // Performs a DFS on all unevaluated cells
        for (Cell cell : allCells) {
            if (myEvaluateState.get(cell) == unevaluatedCell && !myCycleDetected) {
                dfs(cell);
            }
        }

        return !myCycleDetected;
    }

    /**
     * Depth-First Search to evaluate cells and find cycles using an iterative stack-based approach.
     * Uses post-order traversal: visit all dependencies first, then evaluate the cell.
     *
     * @param startingCell - the cell to evaluate
     */
    private void dfs(Cell startingCell) {
        Stack<Cell> stack = new Stack<>();
        stack.push(startingCell);

        while (!stack.isEmpty() && !myCycleDetected) {
            Cell cell = stack.peek();
            int state = myEvaluateState.getOrDefault(cell, unevaluatedCell);

            if (state == evaluatedCell) {
                // Already evaluated
                stack.pop();
                continue;
            }

            if (state == evaluatingCell) {
                // Post-order
                cell.evaluate(mySpreadsheet);
                myEvaluateState.put(cell, evaluatedCell);
                stack.pop();
                continue;
            }

            // Mark as evaluating to detect cycles
            myEvaluateState.put(cell, evaluatingCell);

            // Evaluate all cells that this cell depends on (incoming dependencies)
            Set<Cell> dependencies = myDependencyGraph.getDependencies(cell);
            for (Cell dependency : dependencies) {
                int depState = myEvaluateState.getOrDefault(dependency, unevaluatedCell);

                if (depState == evaluatingCell) {
                    // Cycle found
                    JOptionPane.showMessageDialog(null, "Cycle detected in cell's formula." +
                            "\nReverting Changes.");
                    myCycleDetected = true;
                    return;
                }

                if (depState == unevaluatedCell) {
                    // Push unevaluated dependency onto stack
                    stack.push(dependency);
                }
            }
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
