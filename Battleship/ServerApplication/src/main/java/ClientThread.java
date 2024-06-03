import repository.PlayerRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private GameServer server;
    private PlayerManager player;
    private boolean running = false;
    private int totalOfShips = 0;
    private int gameIdServer;

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
            //name validation
            String playerName;
            while (true) {
                playerName = in.readLine();
                if (playerName == null || playerName.isEmpty()) {
                    out.println("Invalid name! Try again!");
                } else {
                    PlayerRepository playerRepository = new PlayerRepository();
                    if (!playerRepository.findByName(playerName).isEmpty()) {
                        out.println("Invalid name! Try another one!");
                    } else {
                        out.println("Valid name!");
                        player = new PlayerManager(playerName, clientSocket, out);
                        server.addConnectedClient(player);
                        break;
                    }
                }
            }



            Game game = null;


            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                System.out.println("Received command from client " + playerName + ":" + inputLine);

                if (inputLine.equals("stop")) {
                    out.println("Server stopped... Goodbye from server!");
                    server.stop();
                    break;

                } else if (inputLine.equals("create game")) {
                    gameIdServer = server.createGame(player);
                    game = server.getGame(gameIdServer);
                    out.println("Game created with " + gameIdServer + " ! Waiting for an opponent to join...");
                    totalOfShips=0;

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
                    totalOfShips=0;

                    if (game != null) {
                        if (!game.allPlayersAreJoined()) {
                            game.addSecondPlayer(player);
                            out.println("You joined the game with id " + gameId + " ! " + playerName + " waiting for the game to start...");
                        } else {
                            out.println("Game" + gameId + " is already full!");
                        }
                    } else {
                        out.println("Invalid game id!");
                    }

                } else if (inputLine.equals("start game")) {
                    if (game != null) {
                        if (!game.allPlayersAreJoined()) {
                            out.println("Need two players to start the game.");
                        } else if (!game.isReadyToStart()) {
                            out.println("All players need to set the ships positions first!");
                        } else {
                            game.start();
                           // out.println("Game started!");
                        }
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
                    int countOfShips = 5;
                    while (countOfShips > 0) {
                        out.println("Enter the position of the ship with number " + countOfShips + ": ");
                        String position = in.readLine();
                        String[] parts = position.split(" ");
                        int x1 = Integer.parseInt(parts[0]);
                        int y1 = Integer.parseInt(parts[1]);
                        int x2 = Integer.parseInt(parts[2]);
                        int y2 = Integer.parseInt(parts[3]);

                        if ((x1 == x2 && y1 > y2) || (y1 == y2 && x1 > x2)) {
                            out.println("Invalid position! Try again,  must be (x1==x2 && y1 <= y2) || (y1==y2 && x1 <= x2).");
                            continue;
                        }

                        boolean success = player.getBoard().placeShip(x1, y1, x2, y2);
                        if (success) {
                            out.println("Ship placed.");
                        } else {
                            out.println("Failed to place ship.");
                        }

                        countOfShips--;
                    }

                    out.println("Ships positions set! Now you can start the game! Use 'start game' command to start the game!");

                } else if (inputLine.startsWith("place ship") ) {

                    try {
                        String[] parts = inputLine.split(" ");
                        int x1 = Integer.parseInt(parts[2]);
                        int y1 = Integer.parseInt(parts[3]);
                        int x2 = Integer.parseInt(parts[4]);
                        int y2 = Integer.parseInt(parts[5]);

                        if((x1==x2 && y1 >y2) || (y1==y2 && x1 >x2)){
                            out.println( "Invalid coordinates, must be (x1==x2 && y1 <= y2) || (y1==y2 && x1 <= x2).");
                        }else {
                            if(totalOfShips < 5){
                                boolean success = player.getBoard().placeShip(x1, y1, x2, y2);
                                if (success) {
                                    totalOfShips++;
                                    out.println("Ship placed. Am afisat tabla de joc aici: " + player.displayBoard());
                                } else {
                                    out.println( "Failed to place ship.");
                                }
                            }else{
                                out.println("You can't place more than 5 ships.");
                            }
                        }
                    } catch (NumberFormatException e) {
                        out.println( "Invalid coordinates.");
                    }



                } else if (inputLine.startsWith("delete ship") ) {

                    try {
                        String[] parts = inputLine.split(" ");
                        int x1 = Integer.parseInt(parts[2]);
                        int y1 = Integer.parseInt(parts[3]);
                        int x2 = Integer.parseInt(parts[4]);
                        int y2 = Integer.parseInt(parts[5]);

                        if((x1==x2 && y1 >y2) || (y1==y2 && x1 >x2)){
                            out.println( "Invalid coordinates, must be (x1==x2 && y1 <= y2) || (y1==y2 && x1 <= x2).");
                        }else {
                            boolean success = player.getBoard().deleteShip(x1, y1, x2, y2);
                            if (success) {
                                out.println( "Ship deleted. Am afisat tabla de joc aici: " + player.displayBoard());
                                totalOfShips--;
                            } else {
                                out.println( "Failed to delete ship.");
                            }
                        }
                    } catch (NumberFormatException e) {
                        out.println( "Invalid coordinates.");
                    }


                } else if (inputLine.contains("make move")) {
                    String[] parts = inputLine.split(" ");
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);

                    if (x < 0 || x > 10 || y < 0 || y > 10) {
                        out.println("Invalid position! Try again!");
                        continue;
                    }

                    if (game != null) {
                        PlayerManager opponent = game.isPlayer1Turn() ? game.getPlayer2() : game.getPlayer1();
                        boolean hit = game.makeMove(x, y, opponent);

                        if (hit) {
                            if (opponent.allShipsSunk()) {
                                out.println("Hit "+ x +" " + y + " ! Congratulations! You sank all opponent's ships!");
                                opponent.sendMessage("Your ship was miss at "+ x + " " + y + " ! All your ships have been sunk!");
                                //stergem tablele dupa ce se termina jocul
                                totalOfShips=0;
                                opponent.clearBoards();
                                player.clearBoards();
                            } else {
                                out.println("Hit "+ x +" " + y + " ! " + player.getName() + "'s turn again." );
                                opponent.sendMessage("Your ship was hit at "+ x + " " + y + " !");
                            }
                        } else {
                            out.println("Miss " + x +" "+ y + " . "+ opponent.getName() + "'s turn." );
                            opponent.sendMessage("Your ship was miss at "+ x + " " + y + " , but " + player.getName() + " missed!");
                        }


                    } else {
                        out.println("Invalid game!");
                    }

                } else if (inputLine.equals("status")) {
                    GameManager gameManager;
                    if (game == null) {
                        out.println("Player is not in any game.");
                    } else {
                        System.out.println("Comanda este " + inputLine);
                        out.println("Your status:" + game.getGameStatus());
                    }

                } else if (inputLine.equals("display board")) {

                    System.out.println("Comanda este " + inputLine);
                    out.println("Am afisat tabla de joc aici: " + player.displayBoard());

                } else if (inputLine.equals("display opponent view")) {
                    System.out.println("Comanda este " + inputLine);
                    out.println("Am afisat opponent's view" + (player.displayOpponentView()));

                } else if (inputLine.equals("exit")) {
                    player.deletePlayer();
                    break;

                } else if (inputLine.equals("list_games")) {
                    out.println("games_list" + server.getAllGames());

                }else if (inputLine.equals("clear boards")) {
                    player.clearBoards();
                    out.println("Boards cleared!");

                }else if(inputLine.equals("game over")){

                    GameManager games = new GameManager();
                    games.removeGame(gameIdServer);
                    out.println("Game over! The game was removed!");

                }else {
                    out.println("Invalid command " + inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
