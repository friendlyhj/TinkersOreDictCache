package youyihj.tinkersoredictcache;

/**
 * @author youyihj
 */
public enum OreDictRecipesState {
    SCAN,
    READ;

    private static OreDictRecipesState currentState = null;

    public static OreDictRecipesState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(OreDictRecipesState currentState) {
        OreDictRecipesState.currentState = currentState;
    }

    public boolean isRead() {
        return this == READ;
    }

    public boolean isScan() {
        return this == SCAN;
    }
}
