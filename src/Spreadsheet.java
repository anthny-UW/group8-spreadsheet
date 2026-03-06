import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Represents a 2D spreadsheet of Cell objects and coordinates all formula updates,
 * dependency tracking, expression tree construction, and recalculation.
 *
 * The Spreadsheet is responsible for:
 *  - Storing a grid of Cell objects
 *  - Updating a cell's formula and expression tree
 *  - Extracting referenced cells from postfix tokens
 *  - Maintaining the DependencyGraph (dependency → dependent)
 *  - Performing a topological sort to determine evaluation order
 *  - Recalculating all affected cells in dependency-safe order
 *
 * This class does NOT parse formulas itself; parsing is handled by SpreadsheetUtils.
 *
 * @author Anthony
 * @version Winter 2026
 */
public class Spreadsheet {
    private Cell[][] cells;
    private DependencyGraph graph = new DependencyGraph();


    /**
     * Constructs a new square spreadsheet of the given size.
     * Each cell is initialized with default formula "0" and value 0.
     *
     * @param size - the number of rows and columns in the spreadsheet
     */
    public Spreadsheet(int size) {
        cells = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    /**
     * Returns the number of rows in the spreadsheet.
     *
     * @return number of rows
     */
    public int getNumRows() {
        return cells.length;
    }

    /**
     * Returns the number of columns in the spreadsheet.
     *
     * @return number of columns
     */
    public int getNumColumns() {
        return cells[0].length;
    }

    /**
     * Returns the Cell at the location specified by a CellToken.
     *
     * @param token - the parsed cell reference
     * @return the corresponding Cell
     */
    public Cell getCell(CellToken token) {
        return cells[token.getRow()][token.getColumn()];
    }

    /**
     * Returns the integer value stored in the cell at the given row and column.
     *
     * @param row - row index
     * @param column - column index
     * @return the evaluated integer of the cell
     */
    public int getCellValue(int row, int column) {
        return cells[row][column].getValue();
    }

    /**
     * Returns the Cell corresponding to a spreadsheet-style name.
     * Uses SpreadsheetUtils to parse the name into a CellToken.
     *
     * @param cellName - the string name of the cell
     * @return the corresponding Cell
     */
    public Cell getCell(String cellName) {
        CellToken token = new CellToken();
        SpreadsheetUtils.getCellToken(cellName, 0, token);
        return cells[token.getRow()][token.getColumn()];
    }

    /**
     * Updates the formula of the specified cell, rebuilds its expression tree,
     * updates dependency edges, performs a topological sort, and evaluates all
     * cells in dependency-safe order.
     *
     * This is the core recalculation pipeline:
     *  1. Store new formula
     *  2. Build expression tree from postfix tokens
     *  3. Extract referenced cells
     *  4. Update DependencyGraph edges
     *  5. Topologically sort all cells
     *  6. Evaluate cells in sorted order
     *
     * @param token - the cell being modified
     * @param formula - the raw formula string
     * @param postfix - the postfix token stack produced by SpreadsheetUtils
     */
    public void changeCellFormulaAndRecalculate(CellToken token, String formula, Stack<Token> postfix) {
        Cell cell = getCell(token);

        cell.setFormula(formula);
        cell.buildExpressionTree(postfix);
        List<Cell> refs = extractReferences(postfix);

        graph.clearDependencies(cell);
        for (Cell ref : refs) {
            graph.addDependencies(ref, cell);

        }

        TopologicalSort sort = new TopologicalSort(this, graph);
        boolean isSorted = sort.topsort();

        if (!isSorted) {
            sort.revertToOldCells();
        }
    }

    /**
     * Prints the evaluated integer values of all cells in row order.
     * Primarily used by driver for testing.
     */
    public void printValues() {
        for (Cell[] cell : cells) {
            for (Cell value : cell) {
                System.out.print(value.getValue());
            }
        }
    }

    /**
     * Prints the formula stored in a specified cell.
     *
     * @param token - the cell to print
     */
    public void printCellFormula(CellToken token) {
        Cell cell = getCell(token);
        System.out.print(cell.getFormula());
    }

    /**
     * Prints the formulas of all cells in row-major order.
     * Mainly used by driver for debugging.
     */
    public void printAllFormulas() {
        for (Cell[] cell : cells) {
            for (Cell value : cell) {
                System.out.print(value.getFormula());
            }
        }
    }

    /**
     * Extracts all referenced cells from a postfix token stack.
     * Only CellToken objects represent cell references; all others are ignored.
     *
     * @param postfix the postfix token sequence for a formula
     * @return a list of all referenced Cell objects
     */
    private List<Cell> extractReferences(Stack<Token> postfix) {
        List<Cell> refs = new ArrayList<>();
        for (Token t : postfix) {
            if (t instanceof CellToken token) {
                int row = token.getRow();
                int column = token.getColumn();
                refs.add(cells[row][column]);
            }
        }
        return refs;
    }
}
