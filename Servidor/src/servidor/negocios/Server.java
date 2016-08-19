package servidor.negocios;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author asantos07
 */
public class Server {

    private static int DEFAULT_PORT = 8885;

    private ServerSocket server;
    static ClientManager clientManager;
    static MessageHandler msgHandler;
    private Thread cM;
    private Thread msgHandlerThread;

    public Server(int port) throws IOException {

        this.server = new ServerSocket(port);

        msgHandler = new MessageHandler();
        this.msgHandlerThread = new Thread(msgHandler);

        clientManager = new ClientManager();
        this.cM = new Thread(clientManager);

        this.msgHandlerThread.setName("Message-Handler-Thread");
        msgHandlerThread.start();

        this.cM.setName("Client-Manager-Thread");
        this.cM.start();

        System.out.println("SERVER>" + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort() + " (" + server.getInetAddress().getCanonicalHostName() + ")");
    }

    public Server() throws IOException {
        this(DEFAULT_PORT);
    }

    public void startServer() throws IOException {

        while (true) {

            Client client = new Client(this.server.accept()); // Keeps listening for a new connection.
            Thread t = new Thread(client);
            t.start();

            Socket socket = client.getSc();
            System.out.println("CLIENT>" + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort() + " (" + socket.getInetAddress().getCanonicalHostName() + ")");

        }

    }

}
