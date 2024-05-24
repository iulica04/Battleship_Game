import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {
    private String host;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connected to server at " + host + ":" + port);

            System.out.println("Welcome to the Battleship! \n To start the game, please enter your name: ");
            String playerName = keyboard.readLine();
            while (playerName == null || playerName.isEmpty()) {

                System.out.println("Please enter a valid name! \n Enter yout name: ");
                playerName = keyboard.readLine();
            }
            out.println(playerName);

            System.out.println("Commands: \n" + "   -> 'exit' to quit\n" + "   -> 'stop' to stop the server\n" + "   -> 'create game' to create a new game \n" + "   -> 'join game gameID' to join in the game with gameID\n" + "   -> 'make move x y' to make a hit in the position x y\n" + "   -> 'set the ships positions' to set the ships positions\n");

            Thread serverListener = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("GoodBye!!!!!!!!!!");
                }
            });
            serverListener.start();

            String input;
            while ((input = keyboard.readLine()) != null) {
                out.println(input);
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            close();

            try {
                serverListener.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GameClient gameClient = new GameClient("localhost", 2024);
        gameClient.start();
    }
}
