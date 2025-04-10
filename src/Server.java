import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Map;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.io.FileInputStream;
import java.io.InputStream;

public class Server {
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Map<String, ClientData> clients;
    public void start(int port) {
        try {
            // load keystore from local
            // TODO: key file should be determined by the user, not hardcoded
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream ksStream = new FileInputStream("server-keystore.jks")) {
                keyStore.load(ksStream, "123456".toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "123456".toCharArray());  // 私钥密码 (与 -storepass 相同)

            // TRUST ALL CLIENTS
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());

            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            serverSocket = factory.createServerSocket(port);

            isRunning = true;
            clients = new ClientMap();

            while (isRunning) {
                System.out.println("Waiting for SSL client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("SSL Client connected");
                new Thread(new ClientHandler(clients, clientSocket, this)).start();
                new Thread(new ConsoleListener(clients, this)).start();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error starting SSL server", e);
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

    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
            }
    };


    public static void main(String[] args) {
        Server server = new Server();
        server.start(2006);
    }
}
