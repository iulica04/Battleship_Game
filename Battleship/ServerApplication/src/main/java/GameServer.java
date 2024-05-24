import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer {
    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private List<Player> connectedClients;
    private GameManager gameManager;

    public GameServer(int port) {
        this.port = port;
        this.running = false;
        this.connectedClients = new ArrayList<>();
        this.gameManager = new GameManager();
    }


    public synchronized void addConnectedClient(Player player) {
        connectedClients.add(player);
    }

    public synchronized boolean isUserExists(String playerName) {
        return connectedClients.stream().anyMatch(player -> player.getName().equals(playerName));
    }

    public int createGame(Player player) {
        return gameManager.createGame(player);
    }

    public synchronized Game getGame(int gameId) {
        return gameManager.getGame(gameId);
    }


    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Game server started on port " + port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientThread clientThread = new ClientThread(clientSocket, this);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                running = false;
                System.out.println("Server stopped....Goodbye!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        GameServer gameServer = new GameServer(2024);
        gameServer.start();
    }
}
