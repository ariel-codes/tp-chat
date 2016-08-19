package servidor.negocios;

import common.message.Message;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asantos07
 */
public class ClientManager implements Runnable {

    private static final ArrayList<Client> clientsList = new ArrayList<>();
    private static final ArrayList<Message> msgsList = new ArrayList<>();

    public ClientManager() {
    }

    public static ArrayList<Client> getClientsList() {
        return clientsList;
    }

    public static void addClient(Client c) {
        clientsList.add(c);
    }

    public static void removeClient(Client c) {
        clientsList.remove(c);
    }

    public static void addBroadcast(Message msg) {
        msgsList.add(msg);
    }

    private static void broadcastMessage(Message msg) {
        byte[] msgToBeSent = Message.getMsgAsByteVector(msg);
        clientsList.stream().filter((c) -> (c.isConnected())).forEach((c) -> {
            c.sendMessage(msgToBeSent);
        });
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (msgsList.size() > 0) {
                Message msg = msgsList.remove(0);
                broadcastMessage(msg);
            }
        }
    }

}
