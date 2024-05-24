
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class GameClient {
    private String host;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> messageConsumer; // Adăugăm acest câmp

    public GameClient(String host, int port, Consumer<String> messageConsumer) {
        this.host = host;
        this.port = port;
        this.messageConsumer = messageConsumer;
    }

    public void connect() throws IOException {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            messageConsumer.accept("Connected to server at " + host + ":" + port + " \n");
            messageConsumer.accept("Enter your name: ");

        new Thread(() -> {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    messageConsumer.accept(response);//primeste de la server response si le trimite GameUI
                }
            } catch (IOException e) {
                messageConsumer.accept("Error reading from server: " + e.getMessage());
            }
        }).start();
    }

    public void sendCommand(String command) {
        out.println(command);
    }
    public void close() {
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


}
