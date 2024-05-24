import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private Map<Integer, Game> games;
    private int nextGameId;

    public GameManager() {
        this.games = new HashMap<>();
        this.nextGameId = 1;
    }

    public synchronized int createGame(Player player) {
        Game game = new Game(player);
        int gameId = nextGameId;
        nextGameId++;
        games.put(gameId, game);
        return gameId;
    }

    public synchronized Game getGame(int gameId) {
        return games.get(gameId);
    }

}
