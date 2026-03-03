import java.util.Stack;

/**
 * Represents a single cell in a spreadsheet.
 * Each cell stores:
 *   - its formula as a String (e.g., "5 + B3 * 8")
 *   - its computed integer value
 *   - an ExpressionTree built from the formula, used to evaluate the cell
 *
 * @author Sofiia Kabaldina
 * @version Winter 2026
 */
public class Cell {

    private String formula;
    private int value;
    private ExpressionTree expressionTree;

    /**
     * Constructs a default Cell with formula "0" and value 0.
     */
    public Cell() {
        this.formula = "0";
        this.value = 0;
        this.expressionTree = new ExpressionTree();
    }

    /**
     * Returns the formula string stored in this cell.
     * @return the formula string
     */
    public String getFormula() {
        return formula;
    }

    /**
     * Sets the formula string for this cell.
     * @param formula - the new formula string
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * Returns the most recently computed integer value of this cell.
     * @return the cell's value
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the computed value of this cell directly.
     * @param value - the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Returns the ExpressionTree for this cell.
     * @return the expression tree
     */
    public ExpressionTree getExpressionTree() {
        return expressionTree;
    }

    /**
     * Builds the ExpressionTree for this cell from a postfix token stack.
     * !!!Call this after parsing the formula with getFormula() in Spreadsheet.
     *
     * @param tokenStack - a Stack of Token objects in postfix order
     */
    public void buildExpressionTree(Stack<Token> tokenStack) {
        expressionTree = new ExpressionTree();
        expressionTree.buildExpressionTree(tokenStack);
    }

    /**
     * Evaluates this cell's expression tree using current cell values
     * from the spreadsheet and stores the result.
     * !!!Should only be called after all cells this cell depends on
     * have already been evaluated (i.e., after topological sort).
     *
     * @param spreadsheet - the Spreadsheet used to look up referenced cell values
     */
    public void evaluate(Spreadsheet spreadsheet) {
        if (expressionTree == null || expressionTree.isEmpty()) {
            value = 0;
        } else {
            value = expressionTree.evaluate(spreadsheet);
        }
    }

    /**
     * Prints the formula string of this cell.
     */
    public void printFormula() {
        System.out.print(formula);
    }
}