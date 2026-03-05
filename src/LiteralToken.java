/**
 * A token representing an integer literal in a formula (e.g., 5, 42).
 *
 * @author Sofiia Kabaldina
 * @version Winter 2026
 */
public class LiteralToken extends Token {

    private final int value;

    /**
     * Constructs a LiteralToken with the given integer value.
     * @param value - the integer value of this token
     */
    public LiteralToken(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this token.
     * @return the value
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
