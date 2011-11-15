import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

public class FieldProvider implements IFieldProvider {
    private static final String READY = "ready";
    private static final String GO = "go";
    private static final String TURN = "turn";
    private static final String END = "end";
    private static final char COMMENT_CHAR = '#';
    private boolean gameOver = false;

    private enum SetupToken {
        LOADTIME, TURNTIME, ROWS, COLS, TURNS, VIEWRADIUS2, ATTACKRADIUS2, SPAWNRADIUS2, PLAYER_SEED;
    }

    private enum UpdateToken {
        W, A, F, D, H;
    }

    private StreamTokenizer input;
    private IMutableField field;
    private int expectedTurnNumber = 0;

    private boolean assertCorrectTurn() throws IOException {
        String token = nextToken();

        if(!TURN.equals(token)) {
            if(END.equals(token)) {
                return true;
            } else {
                throw new RuntimeException("Unexpected token: " + token + ", expeted: \"" + TURN + "\"");
            }
        }

        int turnNumber = nextInt();

        if(turnNumber != expectedTurnNumber) {
            throw new RuntimeException("Unexpected turn number: " + turnNumber + ", expeted: " + expectedTurnNumber);
        }

        if(field != null) {
            field.setTurnNumber(turnNumber);
        }

        expectedTurnNumber ++;

        return false;
    }

    @Override
    public GameInfo init(InputStream stream) throws IOException {

        input = new StreamTokenizer(new InputStreamReader(stream));
        input.eolIsSignificant(false);
        input.commentChar(COMMENT_CHAR);
        input.wordChars('_', '_');

        long loadTime = 0;
        long turnTime = 0;
        int rows = 0;
        int cols = 0;
        int turns = 0;
        int viewRadiusSquared = 0;
        int attackRadiusSquared = 0;
        int spawnRadiusSquared = 0;
        long seed = 0;


        this.gameOver = assertCorrectTurn();

        String token = null;
        while(!READY.equals(token = nextToken())) {
            SetupToken setupToken = SetupToken.valueOf(token.toUpperCase());
            switch (setupToken) {
                case LOADTIME:
                    loadTime = nextLong();
                    break;
                case TURNTIME:
                    turnTime = nextLong();
                    break;
                case ROWS:
                    rows = nextInt();
                    break;
                case COLS:
                    cols = nextInt();
                    break;
                case TURNS:
                    turns = nextInt();
                    break;
                case VIEWRADIUS2:
                    viewRadiusSquared = nextInt();
                    break;
                case ATTACKRADIUS2:
                    attackRadiusSquared = nextInt();
                    break;
                case SPAWNRADIUS2:
                    spawnRadiusSquared = nextInt();
                    break;
                case PLAYER_SEED:
                    seed = nextLong();
                    break;

            }
        }

        GameInfo info = new GameInfo(loadTime, turnTime, rows, cols, turns, viewRadiusSquared, attackRadiusSquared, spawnRadiusSquared, seed);

        field = new Field(info);

        return info;
    }

    @Override
    public IField getNextFieldState() throws IOException {

        gameOver = assertCorrectTurn();

        String token = null;

        // TODO: some info should be somehow kept in mind, so we can
        // have some guess despite the FOW covering up the map
        field.clear(Cell.Type.FOOD, Cell.Type.ANT);

        while(!gameOver && !GO.equals(token = nextToken())) {

            UpdateToken updateToken = UpdateToken.valueOf(token.toUpperCase());
            int row = nextInt();
            int col = nextInt();
            switch (updateToken) {
                case W:
                    field.setCell(row, col,Cell.Type.WATER, Owned.OWNER_NOBODY);
                break;
                case A:
                    field.setCell(row, col, Cell.Type.ANT, nextInt());
                break;
                case F:
                    field.setCell(row, col, Cell.Type.FOOD, Owned.OWNER_NOBODY);
                break;
                case D:
                    field.setCell(row, col, Cell.Type.DEAD, nextInt());
                break;
                case H:
                    field.setCell(row, col, Cell.Type.HILL, nextInt());
                break;
            }
        }

        field.updateExplored();

        return field;
    }
    /**
     * Gets next token type, skipping insignificant tokens (i.e. new lines, empty lines and whitespace-filled lines)
     * @return
     * @throws IOException
     */
    private int getNextSignificantTokenType() throws IOException {
        int ttype = input.nextToken();
        while(ttype == StreamTokenizer.TT_EOL ||
                (ttype == StreamTokenizer.TT_WORD && input.sval.trim().isEmpty())) {
            ttype = input.nextToken();
        }
        return ttype;
    }

    private String nextToken() throws IOException {
        int ttype = getNextSignificantTokenType();
        if(ttype != StreamTokenizer.TT_WORD) {
            throw new RuntimeException("Unexpected token type: "+ ttype + ", expected: TT_WORD");
        }
        return input.sval.trim();
    }

    private int nextInt() throws IOException {
        int ttype = getNextSignificantTokenType();
        if(ttype != StreamTokenizer.TT_NUMBER) {
            throw new RuntimeException("Unexpected token type: "+ ttype + ", expected: TT_NUMBER");
        }
        return (int) input.nval;
    }

    private long nextLong() throws IOException {
        int ttype = getNextSignificantTokenType();
        if(ttype != StreamTokenizer.TT_NUMBER) {
            throw new RuntimeException("Unexpected token type: "+ ttype + ", expected: TT_NUMBER");
        }
        return (long) input.nval;
    }

    @Override
    public boolean gameOver() {
        return gameOver;
    }
}
