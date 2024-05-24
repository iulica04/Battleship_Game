import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private GameServer server;
    private Player player;
    private boolean running = false;

    public ClientThread(Socket clientSocket, GameServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String playerName = in.readLine();
            while (server.isUserExists(playerName)) {
                out.println("User already exists! Enter another name: ");
                playerName = in.readLine();
            }
            out.println("This name is available!");
            System.out.println("The server receive the name for player:  " + playerName);

            player = new Player(playerName, clientSocket, out);
            server.addConnectedClient(player);
            Game game = null;


            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received command from client: " + inputLine);
                if (inputLine.equals("stop")) {
                    out.println("Server stopped... Goodbye from server!");
                    server.stop();
                    break;
                } else if (inputLine.equals("create game")) {
                    int gameId = server.createGame(player);
                    game = server.getGame(gameId);
                    out.println("Game created with " + gameId + "! Waiting for an opponent to join...");
                } else if (inputLine.contains("check")) {
                    String[] parts = inputLine.split(" ");
                    String playerNameToCheck = parts[1];
                    if (server.isUserExists(playerNameToCheck)) {
                        out.println("User exists!");
                    } else {
                        out.println("User does not exist!");
                    }
                } else if (inputLine.contains("join game")) {
                    String[] parts = inputLine.split(" ");
                    String gameIdStr = parts[2];
                    int gameId = Integer.parseInt(gameIdStr);
                    game = server.getGame(gameId);
                    if (game != null) {
                        if (!game.allPlayersAreJoined()) {
                            game.addSecondPlayer(player);
                            out.println("You joined the game with id " + gameId + "! Enter 'set the ships positions' to set the ships positions!");
                        } else {
                            out.println("Game already started with another player!");
                        }
                    } else {
                        out.println("Invalid game id!");
                    }
                } else if (inputLine.equals("start game")) {
                    if (game != null) {
                        game.start();
                        out.println("Game started!");
                    } else {
                        out.println("Invalid game id!");
                    }
                } else if (inputLine.contains("submit move")) {
                    String[] parts = inputLine.split(" ");
                    String gameIdStr = parts[2];
                    int gameId = Integer.parseInt(gameIdStr);
                    game = server.getGame(gameId);
                    if (game != null) {
                        game.start();
                        out.println("Enter 'set the ships positions' to set the ships positions!");

                    } else {
                        out.println("Invalid game id!");
                    }
                } else if (inputLine.equals("set the ships positions") && game != null) {
                    List<AbstractMap.SimpleEntry<Integer, Integer>> shipsPositions = new ArrayList<>();
                    int countOfShips = 5;
                    while (countOfShips > 0) {
                        out.println("Enter the position of the ship with number " + countOfShips + ": ");
                        String position = in.readLine();
                        String[] parts = position.split(" ");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        if (x <= 0 || x > 10 || y <= 0 || y > 10) {
                            out.println("Invalid position! Try again!");
                            continue;
                        } else {
                            x--;
                            y--;
                        }
                        shipsPositions.add(new AbstractMap.SimpleEntry<>(x, y));
                        countOfShips--;
                    }

                    game.setShipsPositions(shipsPositions, player);
                    out.println("Ships positions set! Now you can start the game! Use 'start game' command to start the game!");
                } else if (inputLine.contains("make move")) {
                    String[] parts = inputLine.split(" ");
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);

                    if (x <= 0 || x > 10 || y <= 0 || y > 10) {
                        out.println("Invalid position! Try again!");
                        continue;
                    } else {
                        x--;
                        y--;
                    }

                    if (game != null) {
                        if (game.makeMove(player, x, y)) {
                            out.println("You hit a ship!");
                        } else {
                            out.println("You missed!");
                        }
                    } else {
                        out.println("Invalid game!");
                    }
                } else if (inputLine.equals("exit")) {
                    break;
                } else {
                    out.println("Invalid command " + inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
