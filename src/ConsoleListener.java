import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class ConsoleListener implements Runnable {
    private static final String HELP_COMMAND = "HELP";
    private static final String LIST_COMMAND = "LIST";
    private static final String BROADCAST_COMMAND = "BROADCAST";
    private static final String MULTICAST_COMMAND = "MULTICAST";

    private Map<String, ClientData> clients;
    private BufferedReader consoleReader;
    private Server server;


    private static final String HELP_MESSAGE = "HELP - show this message\n" +
            "LIST - list all connected clients\n" +
            "BROADCAST - broadcast a message to all clients\n";


    @Override
    public void run() {
        try {
            System.out.println("Console listener started");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String command = reader.readLine();
                if (command.startsWith(BROADCAST_COMMAND)) {
                    String message = command.split("_")[1];
                    server.broadcast(message);
                } else if (command.startsWith(HELP_COMMAND)) {
                    System.out.println(HELP_MESSAGE);
                } else if (command.startsWith(LIST_COMMAND)) {
                    System.out.println("Connected clients: " + clients.keySet());
                } else {
                    System.out.println("Unknown command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ConsoleListener(Map<String, ClientData> clients, Server server) {
        this.clients = clients;
        this.server = server;
    }
}
