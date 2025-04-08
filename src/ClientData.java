import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientData {
    private String name, ip, port;
    private double costToServer;
    private int routerId;
    private ArrayList<String> executedCommands;
    private Socket socket;
    private OutputStream out;

    ClientData(String ip, String port, Socket socket) {
        this.name = ip;
        this.ip = ip;
        this.port = port;
        this.socket = socket;
        this.executedCommands = new ArrayList<>();
        try {
            this.out = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("ClientData created, IP = " + ip + ", out = " + out);
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public OutputStream getOut() {
        if (out == null) {
            try {
                out = socket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return out;
    }

    // record the executed commands
    public void addExecutedCommand(String command) {
        executedCommands.add(command);
    }

    /**
     * @return the executedCommands
     * Format: "command1\ncommand2\ncommand3..."
     */
    public String getExecutedCommands() {
        String result = "";
        for (String command : executedCommands) {
            result += command + "\n";
        }
        return result;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setName(String name) {
        this.name = name;
    }

}
