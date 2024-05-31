import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private Map<Integer, Game> games;
    private int nextGameId;

    public GameManager() {
        this.games = new HashMap<>();
        this.nextGameId = 1;
    }

    public synchronized int createGame(PlayerManager player) {
        Game game = new Game(player);
        int gameId = nextGameId;
        nextGameId++;
        games.put(gameId, game);
        return gameId;
    }
    public synchronized Game getGame(int gameId) {
        return games.get(gameId);
    }
    public synchronized int getGameId() {
        return nextGameId;
    }

    public synchronized List<String> getAllGames() {
        List<String> gamesList = new ArrayList<>();
        for (Map.Entry<Integer, Game> entry : games.entrySet()) {
            gamesList.add("Game ID: " + entry.getKey() + " - Host: " + entry.getValue().getPlayer1().getName());
        }
        return gamesList;
    }

    // Method to remove a game
    public synchronized void removeGame(int gameId) {
        games.remove(gameId);
    }


}
