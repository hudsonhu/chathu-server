# ChatHu Server

The ChatHu Server is a Java-based application that facilitates communication between ChatHu clients. It uses SSL for secure connections and manages user presence and message relaying.

## Features

* Secure SSL/TLS communication with clients.
* Manages multiple client connections concurrently.
* Broadcasts user join/leave events.
* Relays messages as requested by clients (e.g., for user address lookup, stats, kick).
* Server-side console for basic administration (list users, broadcast messages).

## Prerequisites

* **Java Development Kit (JDK):** Version 8 or higher. You can download it from [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or use an alternative like [OpenJDK](https://openjdk.java.net/).
* **Keytool Utility:** This utility is included with the JDK and is used for generating the SSL keystore.

## Setup

Before you can compile and run the server, you need to set up the SSL certificate keystore and the configuration file.

### 1. Create the Keystore

The server requires a Java Keystore (`.jks`) file for SSL communication. You can generate one using the `keytool` command-line utility that comes with the JDK.

Open your terminal or command prompt and run the following command. This will create a file named `server-keystore.jks` in your current directory with a password of `123456`.

```bash
keytool -genkeypair -alias chathuserver -keyalg RSA -keysize 2048 \
-storetype JKS -keystore server-keystore.jks -validity 365 \
-storepass 123456 -keypass 123456 \
-dname "CN=localhost, OU=ChatServer, O=MyOrg, L=MyCity, ST=MyState, C=US"
```

**Explanation of `keytool` options:**

* `-genkeypair`: Generates a key pair (public and private key).
* `-alias chathuserver`: A unique name for the key/certificate entry in the keystore.
* `-keyalg RSA`: Specifies the algorithm to be RSA.
* `-keysize 2048`: Specifies the key size.
* `-storetype JKS`: Specifies the keystore type as JKS.
* `-keystore server-keystore.jks`: The name of the keystore file to be created.
* `-validity 365`: The number of days the certificate will be valid.
* `-storepass 123456`: The password for the keystore. **Remember this password.**
* `-keypass 123456`: The password for the private key itself (can be the same as `storepass` for simplicity in this case).
* `-dname "CN=localhost, ..."`: The distinguished name information. For local testing, `CN=localhost` is appropriate. If deploying, `CN` should match the server's actual hostname.

**Important:**
* Ensure the `server-keystore.jks` file is in the same directory where you will run the server, or update the path in the configuration file.
* The keystore password (`storepass`) used here is `123456`. If you choose a different password, you **must** update it in the `server-config.properties` file.

### 2. Create `server-config.properties` File

In the root directory of your server project (the same directory where you plan to run the server from, and where `server-keystore.jks` is located), create a file named `server-config.properties` with the following content:

```properties
keystore.path=./server-keystore.jks
keystore.password=123456
```

* `keystore.path`: Specifies the path to your keystore file. If it's in the same directory as the server JAR or where you run the `java` command, `./server-keystore.jks` is correct.
* `keystore.password`: The password you set for the keystore when using `keytool` (`123456` in the example above).

## Compilation

Since this is a pure Java project without a build tool like Maven or Gradle, you will compile it using the `javac` command.

1.  **Navigate to your project's root directory** in the terminal (this directory should contain the `src` folder).
2.  **Create an output directory** for the compiled class files (e.g., `bin`):
    ```bash
    mkdir bin
    ```
3.  **Compile the Java source files:**
    ```bash
    javac -d bin src/*.java
    ```
    If your `.java` files are in subdirectories within `src`, you might need to adjust the command (e.g., `javac -d bin src/com/example/*.java`). Based on your provided file structure, all files seem to be in the top-level `src` directory.

## Running the Server

After successful compilation and setup:

1.  **Ensure you are in the project's root directory** (the one containing the `bin` directory, `server-keystore.jks`, and `server-config.properties`).
2.  **Run the server using the `java` command:**
    ```bash
    java -cp bin Server
    ```
    * `-cp bin`: Adds the `bin` directory (where your compiled `.class` files are) to the classpath.
    * `Server`: The name of your main class.

The server will start and, by default, listen for SSL client connections on port `2006`. You should see output in the console indicating it's waiting for clients:

```
Waiting for SSL client...
```

## Server Console Commands

Once the server is running, you can issue commands directly in the terminal where the server was started. The server listens for console input to perform administrative actions:

* **`HELP`**: Displays a list of available console commands.
    ```
    HELP
    ```
* **`LIST`**: Lists the usernames of all currently connected clients.
    ```
    LIST
    ```

## Troubleshooting

* **`RuntimeException: Failed to load configuration file`**: Ensure `server-config.properties` exists in the same directory where you are running the `java` command and is readable.
* **`RuntimeException: Missing keystore.path or keystore.password in configuration file`**: Check `server-config.properties` for these two properties and ensure they are correctly set.
* **`java.io.FileNotFoundException: ./server-keystore.jks` (or similar path issues)**:
    * Make sure `server-keystore.jks` exists.
    * Verify that `keystore.path` in `server-config.properties` points to the correct location of your keystore file relative to where you are executing the `java -cp bin Server` command.
* **SSL Handshake Errors / `javax.net.ssl.SSLHandshakeException`**:
    * Verify the keystore password in `server-config.properties` matches the one used to create `server-keystore.jks`.
    * Ensure the keystore file is not corrupted.
    * The client application must be configured to trust this server's certificate (or trust all certificates, as your client code seems to do for development).
* **`java.net.BindException: Address already in use`**: Another application is already using port `2006`. Stop that application or change the port in `Server.java` (and recompile).
ADME provides a comprehensive guide for someone to set up, compile, and run your server application. Remember to adjust any paths or commands if your project structure differs slightly.