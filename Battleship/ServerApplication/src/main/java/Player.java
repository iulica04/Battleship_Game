import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Player {
    private String name;
    private Socket socket;
    private PrintWriter out;


    public Player(String name, Socket socket, PrintWriter out) {
        this.name = name;
        this.socket = socket;
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String message) {

        System.out.println("Sending message: " + message);
        out.println(message);
    }

    public Socket getSocket() {
        return socket;
    }
}
