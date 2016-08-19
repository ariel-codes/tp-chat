package cliente.negocios;

import common.message.Message;
import common.message.Message.Services;
import common.utils.MsgUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client implements Runnable {

    
    private enum UserOptions {

        HelpOption("/h"),
        ChangeNickOption("/c"),
        PrivateMessageOption("/w"),
        InvalidOption("");

        private final String option;

        private UserOptions(String option) {
            this.option = option;
        }

        
        public static UserOptions getOption(String s) {

            UserOptions op = null;

            switch (s) {
                case "/h":
                    op = HelpOption;
                    break;

                case "/c":
                    op = ChangeNickOption;
                    break;

                case "/w":
                    op = PrivateMessageOption;
                    break;

                default:
                    op = InvalidOption;
                    break;
            }

            return op;

        }

    };

    public static final String INTERFACE_HELP = "To change the nick name go to Option->Change Nick.\n";
    public static final String COMMAND_LINE_HELP =  "\n----------------------------------Usage---------------------------------\n" + 
                                                    "\t[[/option] [parameters]] [Message]\n" + 
                                                    "\n\twhere options include:\n" + 
                                                    "\t/h\tShow this help message\n" + 
                                                    "\t/c\tSend requisition to change your nickname\n" + 
                                                    "\t/w\tSend private message to specified ID available in the client list\n" +
                                                    "------------------------------------------------------------------------\n\n";

    private static final String REQUEST_SENT = "REQUEST SENT TO SERVER!\n";

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private int ClientID;
    private String nick;
    private String tmpNick;
    private boolean isIDSetted = false;
    private int invalidChecksumStack = 0;

    private static final ArrayList<String> msgList = new ArrayList<>();

    static ArrayList<OnlineClient> onlineClientsList = new ArrayList<>();
    String lastOnlineNick = "";

    private static final int BYTES_PER_CHAR = 2;
    private static final int INT_SIZE = 4;
    private static final int FROM_TO_ID_SIZE = 8;
    private static final int CHECKSUM_FIELD_SIZE = 2;

    private static final int GLOBAL_ID = 0;
    private static final int USER_ID = 0;

    final Object staff = new Object();

    
    public Client(String ip, int port, String nick) throws IOException {

        this.socket = new Socket(ip, port);
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        this.tmpNick = nick;

        MessageHandler msgHandler = new MessageHandler(this);

        try {
            this.getConnected(nick);
        } catch (IOException e) {

        }

    }

    
    public String getNick() {
        return this.nick;
    }

    
    public void setNick() {
        this.nick = tmpNick;
    }

    
    public int getClientID() {
        return this.ClientID;
    }

    
    public void setClientID(int id) {
        this.ClientID = id;
    }

    
    public boolean isIDSetted() {
        return this.isIDSetted;
    }

    
    public void setClientIDSetted() {
        this.isIDSetted = true;
    }

    
    public void invalidChecksumStack() {
        this.invalidChecksumStack++;
    }

    
    public String getOnlineClientAsString() {

        String clients = "";

        clients = onlineClientsList.stream().map((oc) -> oc.getClientID() + " ➤ " + oc.getNick() + "\n").reduce(clients, String::concat);

        return clients;

    }

    
    private void sendMuchBytes(byte[] msg) throws IOException {

        for (byte b : msg) 
            this.output.writeByte(b);

        Message.printMessage(msg);

    }

    
    public void getConnected(String nick) throws IOException {

        byte[] msg = Message.getMsgAsByteVector(new Message(Services.Ola.getByte(), nick.length() * BYTES_PER_CHAR, MsgUtils.stringToByteVector(nick)));

        sendMuchBytes(msg);

    }

    
    public void changeNickName(String newNick) throws IOException {

        byte[] msg = Message.getMsgAsByteVector(new Message(Services.MudaNick.getByte(), newNick.length() * BYTES_PER_CHAR, MsgUtils.stringToByteVector(newNick)));

        sendMuchBytes(msg);

    }

    
    public void requestOnlineClients() throws IOException {

        byte[] msg = Message.getMsgAsByteVector(new Message(Services.ClientesConectados.getByte(), 0, new byte[]{}));

        sendMuchBytes(msg);

        synchronized(staff) {
            try {
                staff.wait(); // Waits for the server answer.
            } 
            catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // If it gets here, the array list is ready.
        for ( OnlineClient oc : onlineClientsList ) {
            this.requestNick(oc.getClientID()); // For each online client, requests its nickname.
            synchronized(staff) {
                try {
                    staff.wait(); // ... and wait for the server answer.
                } 
                catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            oc.setNick(lastOnlineNick); // Finally, sets the client's nickname.
        }

    }

    
    private void requestNick(int iD) throws IOException {

        byte[] msg = Message.getMsgAsByteVector(new Message(Services.PedeNick.getByte(), INT_SIZE, MsgUtils.integerToByteVector(iD)));

        sendMuchBytes(msg);

    }

    
    private void sendMessage(String msg, int from, int to) throws IOException {

        byte[] data = new byte[FROM_TO_ID_SIZE + msg.length() * BYTES_PER_CHAR];

        System.arraycopy(MsgUtils.integerToByteVector(from), 0, data, 0, 4);
        System.arraycopy(MsgUtils.integerToByteVector(to), 0, data, 4, 4);
        System.arraycopy(MsgUtils.stringToByteVector(msg), 0, data, 8, msg.length() * BYTES_PER_CHAR);

        byte[] byteMsg = Message.getMsgAsByteVector(new Message(Services.EnviaMensagem.getByte(), data.length, data));

        sendMuchBytes(byteMsg);

    }

    
    public void sendGoodbye() throws IOException {

        byte[] byteMsg = Message.getMsgAsByteVector(new Message(Services.Tchau.getByte(),0,new byte[]{}));

        sendMuchBytes(byteMsg);

    }

    
    private Message receiveMessage() throws IOException {

        byte service;
        int size;
        byte[] data;
        int checksum;

        service = this.input.readByte();
        size = this.input.readShort();

        data = new byte[size];

        for (int i = 0; i < size; i++) {
            data[i] = this.input.readByte();
        }

        return new Message(service, size, data);

    }

    
    public String DecodeUserMessage(String msg) throws IOException {

        String feedBack = "";

        if (msg.startsWith("/")) {

            String[] splittedMsg = msg.split(" ",3);

            switch (UserOptions.getOption(splittedMsg[0])) {

                case HelpOption:
                    feedBack = COMMAND_LINE_HELP;
                    break;

                case ChangeNickOption:
                    this.changeNickName(splittedMsg[1]);
                    break;

                case PrivateMessageOption:

                    int destinyID = this.ClientID;

                    for ( OnlineClient oc : onlineClientsList ) {
                        if ( oc.getNick().equals(splittedMsg[1]) ) {
                            destinyID = oc.getClientID();
                        }
                    }
                    if ( destinyID != this.ClientID ) {
                        this.sendMessage(splittedMsg[2], USER_ID, destinyID);
                    }
                    else {
                        feedBack = "[ERROR]: This client is not online!\n";
                    }

                    break;

                case InvalidOption:
                    feedBack = COMMAND_LINE_HELP;
                    break;

            }

        } else {
            this.sendMessage(msg, USER_ID, GLOBAL_ID);
        }

        return feedBack;

    }

    @Override
    public void run() {

        while (true) {

            try {

                Message msg = this.receiveMessage(); // Receive the message from server.

                int checksum = this.input.readShort(); // Receive the checksum.

                if (Message.getCheckSum(msg) == checksum) { // If the checksum is right...
                    System.out.print("FROM " + this.ClientID + ": "); // Prints the message in the console.
                    Message.printMessage(Message.getMsgAsByteVector(msg)); // ...
                    MessageHandler.addMessage(msg); // Then handle the message.
                    invalidChecksumStack = 0; // Resets the stack.
                }
                else {
                    invalidChecksumStack++; // If not, increments the invalidChecksumStack.
                }

                if ( invalidChecksumStack == 3 ) { // If it stacks the max, closes the chat.
                    System.out.println("Invalid checksum stacked 3 times. Chat is now going to be closed.");
                    System.exit(1);
                }

            } catch (IOException e) {
                return;
            }

        }

    }

}
