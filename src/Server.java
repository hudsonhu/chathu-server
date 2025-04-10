import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Map;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Server {
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Map<String, ClientData> clients;
    public void start(int port) {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("./server-config.properties")) {
                props.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load configuration file", e);
            }

            String keystorePath = props.getProperty("keystore.path");
            String keystorePassword = props.getProperty("keystore.password");

            if (keystorePath == null || keystorePassword == null) {
                throw new RuntimeException("Missing keystore.path or keystore.password in configuration file");
            }

            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream ksStream = new FileInputStream(keystorePath)) {
                keyStore.load(ksStream, keystorePassword.toCharArray());
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keystorePassword.toCharArray());

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
