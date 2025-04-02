import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ClientHandler extends Thread implements Runnable{
    public static final String LIST_COMMAND = "LIST";
    public static final String KICK_COMMAND = "KICK";
    public static final String SEND_COMMAND = "SEND";
    public static final String BROADCAST_COMMAND = "BROADCAST";
    public static final String SET_NAME_COMMAND = "SETNAME";
    public static final String STAT_COMMAND = "STAT";
    public static final String GET_COMMAND = "GET";
    private final Map<String, ClientData> clients;
    private final Socket clientSocket;
    private final Server server;
    ClientData clientData;

    InputStream in;
    OutputStream out;
    public ClientHandler(Map<String,ClientData> clients, Socket clientSocket, Server server) {
        this.clients = clients;
        this.clientSocket = clientSocket;
        this.server = server;
        System.out.println(clientSocket.getInetAddress().toString());
    }

    @Override
    public void run() {
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
            clientData = new ClientData(clientSocket.getInetAddress().toString(),
                    String.valueOf(clientSocket.getPort()), clientSocket);
            clients.put(clientSocket.getInetAddress().toString(), clientData);
            while (true) {
                byte[] buffer = new byte[1024];
                int read;
                read = in.read(buffer);
                if (read == -1) {
                    System.out.println("Client disconnected");
                    disconnect();
                    break;
                }
                String message = new String(buffer, 0, read, StandardCharsets.UTF_8);
                System.out.println("Message received: " + message);
                clientData.addExecutedCommand(message);
                resolveMessage(message);
            }
        } catch (Exception e) {
            disconnect();
            System.out.println("A client disconnected");
        }
    }

    /**
     * Resolve the message received from the client
     */
    private void resolveMessage(String message) throws Exception {
        if (message.startsWith(SET_NAME_COMMAND)) {
            String oldName = clientData.getName();
            String name = message.split("_")[1];
            clientData.setName(name);
            clients.remove(oldName);
            clients.put(name, clientData);
            System.out.println("Client name set to: " + name);
        } else if (message.startsWith(LIST_COMMAND)) {
            String response = "LIST_CLIENTS " + clients.toString();
            System.out.println("Sending clients: " + response);
            out.write(response.getBytes());
        } else if (message.startsWith(KICK_COMMAND)) {
            String name = message.split("_")[1];
            ClientData client = clients.get(name);
            if (client != null) {
                client.getOut().write(("KICKED by " + clientData.getName()).getBytes());
                client.getSocket().close();
                server.broadcast("Client " + name + " has been kicked by " + clientData.getName());
                clients.remove(name);
            } else {
                out.write("[KICK] Client not found".getBytes());
            }
        } else if (message.startsWith(BROADCAST_COMMAND)) {
            String messageToSend = message.split("_")[1];
            for (ClientData client : clients.values()) {
                client.getOut().write(("[BROADCAST] " + messageToSend).getBytes());
            }
        } else if (message.startsWith(SEND_COMMAND)) {
            String name = message.split("_")[1];
            String messageToSend = message.split("_")[2];
            ClientData client = clients.get(name);
            if (client != null) {
                client.getOut().write(messageToSend.getBytes());
            }
        } else if (message.startsWith(STAT_COMMAND)) {
            String getStatFor = message.split("_")[1];
            ClientData client = clients.get(getStatFor);
            if (client != null) {
                String response = "STAT_" + client.getName() + "\n" + client.getExecutedCommands();
                out.write(response.getBytes());
            } else {
                out.write(" [STAT] Client not found".getBytes());
            }
        } else if (message.startsWith(GET_COMMAND)) {
            String name = message.split("_")[1];
            ClientData client = clients.get(name);
            if (client != null) {
                String response = "GET_" + client.getName() + "_" + client.getIp() + ":" + client.getPort();
                System.out.println("Sending client: " + response);
                out.write(response.getBytes());
            } else {
                String response = "GET_" + name + "_NOTFOUND";
                System.out.println("Sending: " + response);
                out.write(response.getBytes());
            }
        } else if (message.startsWith("INIT")) {
            String name = message.split("_")[1];
            String port = message.split("_")[2];
            String oldName = clientData.getName();
            clientData.setName(name);
            clients.remove(oldName);
            clients.put(name, clientData);
            clientData.setPort(port);
            server.broadcast("Client " + name + " has connected");
        } else if (message.startsWith("STOP")) {
            server.broadcast(" " + clientData.getName() + " has disconnected");
            disconnect();
        } else {
            String response = "Unknown command";
            System.out.println("Sending response: " + response);
            out.write(response.getBytes());
        }
    }

    public void disconnect() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        server.getOspf().removeClient(clientData);
        clients.remove(clientData.getName());
        System.out.println(clientData.getName() + " disconnected, " + clients.size() + " clients remaining");
    }
}
