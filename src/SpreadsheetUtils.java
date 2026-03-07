import java.util.Stack;

/**
 * Utility methods for the Spreadsheet application.
 * They were adapted from util.java.
 */
public class SpreadsheetUtils {

    /**
     * Returns a string representation of a token.
     * @param token - a Token (LiteralToken, CellToken, or OperatorToken)
     * @return a string representing the token
     */
    public static String printExpressionTreeToken(Token token) {
        if (token instanceof OperatorToken) {
            return ((OperatorToken) token).getOperatorToken() + " ";
        } else if (token instanceof CellToken) {
            return printCellToken((CellToken) token) + " ";
        } else if (token instanceof LiteralToken) {
            return ((LiteralToken) token).getValue() + " ";
        } else {
            System.out.println("Error in printExpressionTreeToken.");
            System.exit(0);
            return "";
        }
    }

    /**
     * Returns true if ch is one of the supported operator characters: +, -, *, /, (
     * @param ch - character to check
     * @return true if ch is an operator
     */
    public static boolean isOperator(char ch) {
        return (ch == OperatorToken.Plus   ||
                ch == OperatorToken.Minus  ||
                ch == OperatorToken.Mult   ||
                ch == OperatorToken.Div    ||
                ch == OperatorToken.LeftParen);
    }

    /**
     * Returns the priority of an operator character.
     * +, - -> 0
     * *, / -> 1
     * (    -> 2
     * @param ch  an operator character
     * @return    the priority
     */
    public static int operatorPriority(char ch) {
        switch (ch) {
            case OperatorToken.Plus:
            case OperatorToken.Minus:
                return 0;
            case OperatorToken.Mult:
            case OperatorToken.Div:
                return 1;
            case OperatorToken.LeftParen:
                return 2;
            default:
                System.out.println("Error in operatorPriority: not an operator.");
                System.exit(0);
                return -1;
        }
    }

    /**
     * Parses a cell reference (e.g., "A3", "BC12") from a string starting at startIndex.
     * Sets cellToken's row and column if valid, and sets both to BadCell if invalid.
     * <p>
     * Column letters: A=0, B=1, ..., Z=25, AA=26, AB=27, ...
     * Row: non-negative integer following the letters.
     *
     * @param inputString - the input string
     * @param startIndex - the index of the first char to process
     * @param cellToken - a CellToken to fill in (acts as a return value)
     * @return index corresponding to the position in the string just after the cell reference
     */
    public static int getCellToken(String inputString, int startIndex, CellToken cellToken) {
        char ch;
        int column = 0;
        int row = 0;
        int index = startIndex;

        // handle a bad startIndex
        if ((startIndex < 0) || (startIndex >= inputString.length())) {
            cellToken.setColumn(CellToken.BadCell);
            cellToken.setRow(CellToken.BadCell);
            return index;
        }

        // get rid of leading whitespace characters
        while (index < inputString.length()) {
            ch = inputString.charAt(index);
            if (!Character.isWhitespace(ch)) {
                break;
            }
            index++;
        }

        if (index == inputString.length()) {
            // reached the end of the string before finding a capital letter
            cellToken.setColumn(CellToken.BadCell);
            cellToken.setRow(CellToken.BadCell);
            return index;
        }

        // ASSERT: index now points to the first non-whitespace character

        ch = inputString.charAt(index);

        // must start with a capital letter
        if (!Character.isUpperCase(ch)) {
            cellToken.setColumn(CellToken.BadCell);
            cellToken.setRow(CellToken.BadCell);
            return index;
        } else {
            column = ch - 'A';
            index++;
        }

        // continue reading capital letters for multi-letter column names
        while (index < inputString.length()) {
            ch = inputString.charAt(index);
            if (Character.isUpperCase(ch)) {
                column = ((column + 1) * 26) + (ch - 'A');
                index++;
            } else {
                break;
            }
        }

        // reached the end of the string before fully parsing the cell reference
        if (index == inputString.length()) {
            cellToken.setColumn(CellToken.BadCell);
            cellToken.setRow(CellToken.BadCell);
            return index;
        }

        // ASSERT: We have processed leading whitespace and the
        // capital letters of the cell reference

        // read digits for the row number
        ch = inputString.charAt(index);
        if (Character.isDigit(ch)) {
            row = ch - '0';
            index++;
        } else {
            cellToken.setColumn(CellToken.BadCell);
            cellToken.setRow(CellToken.BadCell);
            return index;
        }

        while (index < inputString.length()) {
            ch = inputString.charAt(index);
            if (Character.isDigit(ch)) {
                row = (row * 10) + (ch - '0');
                index++;
            } else {
                break;
            }
        }

        // successfully parsed a cell reference
        cellToken.setColumn(column);
        cellToken.setRow(row);
        return index;
    }

    /**
     * Converts a CellToken to its spreadsheet label string (e.g., column=0, row=3 -> "A3").
     * @param cellToken - the CellToken to convert
     * @return the cell label string
     */
    public static String printCellToken(CellToken cellToken) {
        char ch;
        String returnString = "";
        int col = cellToken.getColumn();
        int largest = 26; // minimum col number with number_of_digits digits
        int number_of_digits = 2;

        // compute the biggest power of 26 <= col
        while (largest <= col) {
            largest *= 26;
            number_of_digits++;
        }
        largest /= 26;
        number_of_digits--;

        // append column letters, one character at a time
        while (number_of_digits > 1) {
            ch = (char) (((col / largest) - 1) + 'A');
            returnString += ch;
            col = col % largest;
            largest /= 26;
            number_of_digits--;
        }

        // handle last digit
        ch = (char) (col + 'A');
        returnString += ch;

        // append the row as an integer
        returnString += cellToken.getRow();

        return returnString;
    }

    /**
     * Parses an infix formula string and returns a Stack of Tokens
     * representing the equivalent postfix expression.
     * Reading from the bottom to the top of the stack gives the postfix order.
     * <p>
     * Supported tokens: integer literals, cell references (e.g., A3), operators +, -, *, /, (, )
     * Operator precedence: * and / have higher precedence than + and -.
     *
     * @param formula - the infix formula string
     * @return a Stack of Token objects in postfix order (bottom=first, top=last)
     */
    public static Stack<Token> getFormula(String formula) {
        Stack<Token> returnStack   = new Stack<>();  // output postfix stack
        Stack<Token> operatorStack = new Stack<>();  // temporary operator stack
        boolean error = false;
        char ch = ' ';
        int index = 0;

        while (index < formula.length()) {
            // skip whitespace
            while (index < formula.length()) {
                ch = formula.charAt(index);
                if (!Character.isWhitespace(ch)) {
                    break;
                }
                index++;
            }

            if (index == formula.length()) {
                break;  // end of formula (not necessarily an error if we had tokens)
            }

            if (isOperator(ch)) {
                // Pop operators from operatorStack to output if they have >= priority
                // (but never pop a LeftParen this way)
                while (!operatorStack.isEmpty()) {
                    OperatorToken stackOperator = (OperatorToken) operatorStack.peek();
                    if (stackOperator.priority() >= operatorPriority(ch) &&
                            stackOperator.getOperatorToken() != OperatorToken.LeftParen) {
                        operatorStack.pop();
                        returnStack.push(stackOperator);
                    } else {
                        break;
                    }
                }
                operatorStack.push(new OperatorToken(ch));
                index++;

            } else if (ch == ')') {
                // Pop operators to output until we hit a LeftParen
                while (!operatorStack.isEmpty()) {
                    OperatorToken stackOperator = (OperatorToken) operatorStack.pop();
                    if (stackOperator.getOperatorToken() == OperatorToken.LeftParen) {
                        break;
                    }
                    returnStack.push(stackOperator);
                }
                index++;

            } else if (Character.isDigit(ch)) {
                // Parse a multi-digit integer literal
                int literalValue = ch - '0';
                index++;
                while (index < formula.length()) {
                    ch = formula.charAt(index);
                    if (Character.isDigit(ch)) {
                        literalValue = (literalValue * 10) + (ch - '0');
                        index++;
                    } else {
                        break;
                    }
                }
                returnStack.push(new LiteralToken(literalValue));

            } else if (Character.isUpperCase(ch)) {
                // Parse a cell reference
                CellToken cellToken = new CellToken();
                index = getCellToken(formula, index, cellToken);
                if (cellToken.getRow() == CellToken.BadCell) {
                    error = true;
                    break;
                }
                returnStack.push(cellToken);

            } else {
                // Unknown character
                error = true;
                break;
            }
        }

        // move remaining operators to output
        while (!operatorStack.isEmpty()) {
            returnStack.push(operatorStack.pop());
        }

        if (error) {
            returnStack.clear();
        }

        return returnStack;
    }
}