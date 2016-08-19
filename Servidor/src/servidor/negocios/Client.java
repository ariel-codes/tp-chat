package servidor.negocios;

import common.message.Message;
import common.message.Message.Services;
import servidor.negocios.MessageHandler.Nack;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asantos07
 */
public class Client implements Runnable {

    private final Socket sc;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final int ClientID;
    private boolean isConnected = false;
    private String nick = "";

    private int wrongChecksums = 0;

    private static int lastID = 0;

    public Client(Socket sock) throws IOException {
        this.sc = sock;
        this.in = new DataInputStream(sc.getInputStream());
        this.out = new DataOutputStream(sc.getOutputStream());
        this.ClientID = ++lastID;

        ClientManager.addClient(this); 
    }

    public Socket getSc() {
        return this.sc;
    }

    public int getClientID() {
        return this.ClientID;
    }

    public String getNick() {
        return this.nick;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void sendMessage(byte[] msg) {
        try {
            System.out.print("TO " + this.ClientID + ": ");
            for (byte b : msg) {
                this.out.writeByte(b);
            }
            Message.printMessage(msg);
        } catch (IOException e) {
            ClientManager.removeClient(this);
        }
    }

    private Message receiveMessage() throws IOException {
        byte service;
        int size;
        byte[] data;
        int checksum;
        service = this.in.readByte();
        size = this.in.readShort();
        data = new byte[size];

        for (int i = 0; i < size; i++) {
            data[i] = this.in.readByte();
        }
        return new Message(service, size, data);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message msg = this.receiveMessage();
                int checksum = this.in.readShort();
                if (Message.getCheckSum(msg) == checksum) {
                    System.out.print("FROM " + this.ClientID + ": ");
                    Message.printMessage(Message.getMsgAsByteVector(msg));
                    MessageHandler.addNewRequest(new Request(this, msg));
                    synchronized (Server.msgHandler) {
                        Server.msgHandler.notify();
                    }
                } else {
                    this.wrongChecksums++;
                    Message wrongCheckSum = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ChecksumInvalido.getByte()});
                    byte[] wrongCheckSumAnswer = Message.getMsgAsByteVector(wrongCheckSum);
                    this.sendMessage(wrongCheckSumAnswer);
                }
                if (this.wrongChecksums == 3) {
                    ClientManager.removeClient(this);
                    return;
                }
            } catch (IOException ex) {
                ClientManager.removeClient(this);
                return;
            }
        }
    }

    @Override
    protected void finalize() {
        try {
            this.in.close();
            this.out.close();
            this.sc.close();
        } catch (IOException e) {
        } finally {
            try {
                super.finalize();
            } catch (Throwable ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
