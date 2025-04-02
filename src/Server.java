import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Map<String, ClientData> clients;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            clients = new ClientMap();

            while (isRunning) {
                System.out.println("Waiting for client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");
                new Thread(new ClientHandler(clients, clientSocket, this)).start();
                // listen to console input
                new Thread(new ConsoleListener(clients, this)).start();

            }
        } catch (Exception e) {
            throw new RuntimeException("Error starting server", e);
        }
    }

    public void broadcast(String message) {
        for (ClientData client : clients.values()) {
            try {
                client.getOut().write(message.getBytes());
            } catch (IOException e) {
                System.err.println("Error sending message to " + client.getName());
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.start(2006);
    }
}
