package cliente.negocios;

import common.message.Message;
import common.message.Message.Services;
import common.utils.MsgUtils;
import java.io.IOException;
import java.util.ArrayList;

public class MessageHandler implements Runnable {

    public enum Nack {

        ChecksumInvalido((byte) 0xFF),
        ClienteSemHello((byte) 0xEE),
        MalFormada((byte) 0xDD),
        ClienteNIdentificado((byte) 0xCC),
        ClienteJaExiste((byte) 0xBB),
        NickInvalido((byte) 0x01),
        NickIndisponivel((byte) 0x02);

        private final byte nackByte;

        private Nack(byte nackByte) {
            this.nackByte = nackByte;
        }

        public byte getByte() {
            return this.nackByte;
        }

        public static Nack getNack(byte b) {

            Nack n = null;

            switch (b) {

                case (byte) 0xFF:
                    n = ChecksumInvalido;
                    break;

                case (byte) 0xEE:
                    n = ClienteSemHello;
                    break;

                case (byte) 0xDD:
                    n = MalFormada;
                    break;

                case (byte) 0xCC:
                    n = ClienteNIdentificado;
                    break;

                case (byte) 0xBB:
                    n = ClienteJaExiste;
                    break;

                case (byte) 0x01:
                    n = NickInvalido;
                    break;

                case (byte) 0x02:
                    n = NickIndisponivel;

            }

            return n;

        }

    };

    private static ArrayList<Message> msgList = new ArrayList<>();
    static ArrayList<String> stringList = new ArrayList<>();

    private static Client client;

    private static final int INT_SIZE = 4;

    public MessageHandler(Client c) {
        client = c;
    }

    private static void HandleMessage(Message msg) throws IOException {

        switch (Services.getService(msg.getService())) {

            case Ola:
                Handle0x01(msg.getData());
                break;

            case MudaNick:
                Handle0x02(msg.getSize(), msg.getData());
                break;

            case ClientesConectados:
                Handle0x03(msg.getSize(), msg.getData());
                break;

            case PedeNick:
                Handle0x04(msg.getData());
                break;

            case EnviaMensagem:
                Handle0x05(msg.getSize(), msg.getData());
                break;

            case Tchau:
                Handle0x0A(msg.getData());
                break;

            case Invalido:
                Handle0x7F(msg.getData());
                break;

        }
    }

    private static void Handle0x01(byte[] data) throws IOException {

        String newMessage;

        if (!client.isIDSetted()) { // If the client's ID isn't setted, sets its ID and its nickname.
            client.setClientID(MsgUtils.byteVectorToInteger(data));
            client.setNick();
            client.setClientIDSetted();
            newMessage = "[SERVER]: Welcome to the chat " + client.getNick() + "!\n";
        } else { // If not, just append one message warning that someone else connected to the chat.
            newMessage = "[SERVER]: Client " + String.valueOf(MsgUtils.byteVectorToInteger(data)) + " joined the chat!\n";
        }

        stringList.add(newMessage);

    }

    private static void Handle0x02(int size, byte[] data) {

        int nickSize = size - INT_SIZE;
        byte[] idArray = new byte[INT_SIZE];
        byte[] nickArray = new byte[nickSize];

        System.arraycopy(data, 0, idArray, 0, 4);
        System.arraycopy(data, 4, nickArray, 0, nickSize);

        String id = String.valueOf(MsgUtils.byteVectorToInteger(idArray));
        String nick = MsgUtils.byteVectorToString(nickArray);

        String newMessage = "[SERVER]: Client " + id + " changed the nickname to " + nick + ".\n";

        stringList.add(newMessage);

    }

    private static void Handle0x03(int size, byte[] data) {

        int iD;
        int numberOfIDs = size / 4;
        byte[] idByte = new byte[INT_SIZE];

        Client.onlineClientsList.clear();

        for (int i = 0; i < numberOfIDs; i++) {
            for (int j = 0; j < INT_SIZE; j++) {
                idByte[j] = data[j + i * INT_SIZE];
            }
            iD = MsgUtils.byteVectorToInteger(idByte);
            Client.onlineClientsList.add(new OnlineClient(iD));
        }

        synchronized (client.staff) {
            client.staff.notify();
        }

    }

    private static void Handle0x04(byte[] data) {

        client.lastOnlineNick = MsgUtils.byteVectorToString(data);

        synchronized (client.staff) {
            client.staff.notify();
        }

    }

    private static void Handle0x05(int size, byte[] data) {

        String msg = "";

        int msgSize = size - 2 * INT_SIZE;

        byte[] fromID = new byte[4];
        byte[] toID = new byte[4];
        byte[] msgArray = new byte[size - 2 * INT_SIZE];

        System.arraycopy(data, 0, fromID, 0, INT_SIZE);
        System.arraycopy(data, 4, toID, 0, INT_SIZE);
        System.arraycopy(data, 8, msgArray, 0, msgSize);

        int from = MsgUtils.byteVectorToInteger(fromID);
        int to = MsgUtils.byteVectorToInteger(toID);

        if (to != 0) {

            if (from != client.getClientID()) {

                String nick = "";

                for (OnlineClient oc : Client.onlineClientsList) {
                    if (from == oc.getClientID()) {
                        nick = oc.getNick();
                    }
                }

                msg += "[FROM " + nick + "]: ";
            } else {
                String nick = "";

                for (OnlineClient oc : Client.onlineClientsList) {
                    if (to == oc.getClientID()) {
                        nick = oc.getNick();
                    }
                }

                msg += "[TO " + nick + "]: ";
            }

        } else {

            String nick = "";

            for (OnlineClient oc : Client.onlineClientsList) {
                if (from == oc.getClientID()) {
                    nick = oc.getNick();
                }
            }

            msg += "[GLOBAL " + nick + "]: ";
        }

        msg += MsgUtils.byteVectorToString(msgArray);

        if (!msg.endsWith("\n")) {
            msg += "\n";
        }

        stringList.add(msg);

    }

    private static void Handle0x0A(byte[] data) {
        int receivedId = MsgUtils.byteVectorToInteger(data);
        if (receivedId == client.getClientID()) {
            System.exit(0);
        } else {
            stringList.add("[SERVER]: " + String.valueOf(receivedId) + " vazou!\n");
        }

    }

    private static void Handle0x7F(byte[] data) {
        String serverMessage = "[ERRO]: DEU ERRADO\n";
        MessageHandler.stringList.add(serverMessage);

    }

    public static final void addMessage(Message msg) {
        MessageHandler.msgList.add(msg);
    }

    public static String getMessage() {
        return stringList.size() > 0 ? stringList.remove(0) : "";
    }

    @Override
    public void run() {

        while (true) {

            if (MessageHandler.msgList.size() > 0) {

                Message msg = msgList.remove(0);

                try {
                    MessageHandler.HandleMessage(msg);
                } catch (IOException ex) {
                    return;
                }
                synchronized (this) {
                    notify();
                }

            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }

}
