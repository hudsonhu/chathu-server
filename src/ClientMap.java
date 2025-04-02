import java.util.Hashtable;
import java.util.Map;

public class ClientMap extends Hashtable<String, ClientData> {
    public ClientMap() {
        super();
    }

    /**
     * Returns all the clients in a string
     * Format: "name1_ip1:port1 name2_ip2:port2..."
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ClientData> entry : this.entrySet()) {
            sb.append(entry.getKey()).append("_").append(entry.getValue().getIp()).append(":")
                    .append(entry.getValue().getPort()).append(" ");
        }
        return sb.toString();
    }
}
