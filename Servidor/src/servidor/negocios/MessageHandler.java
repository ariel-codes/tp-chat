package servidor.negocios;

import common.message.Message;
import common.message.Message.Services;
import common.utils.MsgUtils;
import java.util.ArrayList;
import static common.message.Message.Services.Ola;
import static common.message.Message.Services.MudaNick;
import static common.message.Message.Services.ClientesConectados;
import static common.message.Message.Services.EnviaMensagem;
import static common.message.Message.Services.Tchau;

/**
 *
 * @author asantos07
 */
public class MessageHandler implements Runnable {

    public enum Nack {

        ChecksumInvalido((byte) 0xFF),
        ClienteSemHello((byte) 0xEE),
        MalFormada((byte) 0xDD),
        ClienteNIdentificado((byte) 0xCC),
        ClienteJaExiste((byte) 0xBB);

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

            }

            return n;

        }

    };

    private static final ArrayList<Request> requestList = new ArrayList<>();

    private static final int BYTES_PER_CHAR = 2;
    private static final int INT_SIZE = 4;

    public MessageHandler() {

    }

    public static void addNewRequest(Request r) {
        requestList.add(r);
    }

    private static void HandleMessage(Client client, Message msg) {

        if (msg.getSize() < 0) {

            Message badformedmsg = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.MalFormada.getByte()});
            byte[] badformed = Message.getMsgAsByteVector(badformedmsg);
            client.sendMessage(badformed);

        } else {

            switch (Services.getService(msg.getService())) {

                case Ola:
                    Handle0x01(client, msg.getData());
                    break;

                case MudaNick:
                    Handle0x02(client, msg.getData());
                    break;

                case ClientesConectados:
                    Handle0x03(client, msg.getData());
                    break;

                case PedeNick:
                    Handle0x04(client, msg.getData());
                    break;

                case EnviaMensagem:
                    Handle0x05(client, msg.getData());
                    break;

                case Tchau:
                    Handle0x0A(client);
                    break;

                default:
                    Message badformed = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.MalFormada.getByte()});
                    byte[] badformedmsg = Message.getMsgAsByteVector(new Message(Nack.MalFormada.getByte(), 0, new byte[]{}));
                    break;

            }
        }
    }

    private static void Handle0x01(Client client, byte[] data) {

        String nick = MsgUtils.byteVectorToString(data);
        boolean isAvailable = true;
        byte[] answer;

        if (!client.isConnected()) {

            ArrayList<Client> clientList = ClientManager.getClientsList();

            if (clientList.size() > 0) {
                for (Client c : clientList) {
                    if (c.getNick().equals(nick) && c.isConnected()) {
                        isAvailable = false;
                    }
                }
            }

            if (!isAvailable) {
                answer = Message.getMsgAsByteVector(new Message(Services.Negado.getByte(), 1, new byte[]{Services.Tchau.getByte()}));
                client.sendMessage(answer);
            } else {
                client.setNick(nick);
                client.setConnected(true);
                ClientManager.addBroadcast(new Message(Services.Ola.getByte(), INT_SIZE, MsgUtils.integerToByteVector(client.getClientID())));
                synchronized (Server.clientManager) {
                    Server.clientManager.notify();
                }
            }

        } else {
            Message registered = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteJaExiste.getByte()});
            byte[] registeredAnswer = Message.getMsgAsByteVector(registered);
            client.sendMessage(registeredAnswer);
        }

    }

    private static void Handle0x02(Client client, byte[] data) {

        if (client.isConnected()) {

            String nick = MsgUtils.byteVectorToString(data);
            boolean isAvailable = true;
            byte[] dataToBeSent;
            byte[] answerToClient = null;

            ArrayList<Client> clientList = ClientManager.getClientsList();

            if (clientList.size() > 0) {
                for (Client c : clientList) {
                    if (c.getNick().equals(nick)) {
                        isAvailable = false;
                        break;
                    }
                }
            }

            if (!isAvailable) {
                answerToClient = Message.getMsgAsByteVector(new Message(Services.Negado.getByte(),
                        1,
                        new byte[]{Services.MudaNick.getByte()})
                );
                client.sendMessage(answerToClient);
            } else {
                client.setNick(nick);
                int dataSize = INT_SIZE + nick.length() * BYTES_PER_CHAR;
                dataToBeSent = new byte[dataSize];

                System.arraycopy(MsgUtils.integerToByteVector(client.getClientID()), 0, dataToBeSent, 0, INT_SIZE);
                System.arraycopy(MsgUtils.stringToByteVector(nick), 0, dataToBeSent, 4, nick.length() * BYTES_PER_CHAR);

                ClientManager.addBroadcast(new Message(Services.MudaNick.getByte(),
                        dataSize,
                        dataToBeSent));

                synchronized (Server.clientManager) {
                    Server.clientManager.notify();
                }

            }

        } else {
            Message notgreeted = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteSemHello.getByte()});
            byte[] notGreetedAnswer = Message.getMsgAsByteVector(notgreeted);
            client.sendMessage(notGreetedAnswer);
        }

    }

    private static void Handle0x03(Client client, byte[] data) {

        if (client.isConnected()) {

            ArrayList<Client> clientList = ClientManager.getClientsList();
            ArrayList<byte[]> idList = new ArrayList<>();

            clientList.stream().filter((c) -> (c.isConnected())).forEach((c) -> {
                idList.add(MsgUtils.integerToByteVector(c.getClientID()));
            });

            byte[] clientIDs = new byte[idList.size() * INT_SIZE];

            for (int i = 0; i < idList.size(); i++) {
                System.arraycopy(idList.get(i), 0, clientIDs, i * INT_SIZE, INT_SIZE);
            }

            byte[] answer = Message.getMsgAsByteVector(new Message(Services.ClientesConectados.getByte(), clientIDs.length, clientIDs));

            client.sendMessage(answer);

        } else {
            Message notgreeted = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteSemHello.getByte()});
            byte[] notGreetedAnswer = Message.getMsgAsByteVector(notgreeted);
            client.sendMessage(notGreetedAnswer);
        }

    }

    private static void Handle0x04(Client client, byte[] data) {

        if (client.isConnected()) {
            int clientID = MsgUtils.byteVectorToInteger(data);
            String nick = "";
            byte[] answer;

            for (Client c : ClientManager.getClientsList()) {
                if (c.isConnected() && c.getClientID() == clientID) {
                    nick = c.getNick();
                }
            }

            if (nick.equals("")) {
                answer = Message.getMsgAsByteVector(new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteNIdentificado.getByte()}));
            } else {
                answer = Message.getMsgAsByteVector(new Message(Services.PedeNick.getByte(),
                        nick.length() * BYTES_PER_CHAR,
                        MsgUtils.stringToByteVector(nick)));
            }
            client.sendMessage(answer);
        } else {
            Message notgreeted = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteSemHello.getByte()});
            byte[] notGreetedAnswer = Message.getMsgAsByteVector(notgreeted);
            client.sendMessage(notGreetedAnswer);
        }

    }

    private static void Handle0x05(Client client, byte[] data) {

        if (client.isConnected()) {

            int msgLength = data.length - 2 * INT_SIZE;

            byte[] answer;
            byte[] toID = new byte[4];
            byte[] dataToBeSent = new byte[data.length];
            byte[] fromID = MsgUtils.integerToByteVector(client.getClientID());

            System.arraycopy(data, 4, toID, 0, INT_SIZE);

            int toClient = MsgUtils.byteVectorToInteger(toID);

            System.arraycopy(fromID, 0, dataToBeSent, 0, INT_SIZE);
            System.arraycopy(toID, 0, dataToBeSent, 4, INT_SIZE);
            System.arraycopy(data, 8, dataToBeSent, 8, msgLength);

            Message msgToBeSent = new Message(Services.EnviaMensagem.getByte(), dataToBeSent.length, dataToBeSent);

            answer = Message.getMsgAsByteVector(msgToBeSent);

            if (toClient == 0) {
                ClientManager.addBroadcast(msgToBeSent);
                synchronized (Server.clientManager) {
                    Server.clientManager.notify();
                }
            } else {
                Client tmp = null;
                for (Client c : ClientManager.getClientsList()) {
                    if (toClient == c.getClientID()) {
                        tmp = c;
                    }
                }

                if (tmp != null) {
                    tmp.sendMessage(answer);
                    client.sendMessage(answer);
                } else {
                    Message notIdentified = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteNIdentificado.getByte()});
                    byte[] notIdentifiedAnswer = Message.getMsgAsByteVector(notIdentified);
                    client.sendMessage(notIdentifiedAnswer);
                }
            }

        } else {
            Message notgreeted = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteSemHello.getByte()});
            byte[] notGreetedAnswer = Message.getMsgAsByteVector(notgreeted);
            client.sendMessage(notGreetedAnswer);
        }

    }

    private static void Handle0x0A(Client client) {

        if (client.isConnected()) {
            byte[] byteID = MsgUtils.integerToByteVector(client.getClientID());
            Message msg = new Message(Services.Tchau.getByte(), INT_SIZE, byteID);

            client.sendMessage(Message.getMsgAsByteVector(msg));

            ClientManager.addBroadcast(msg);

            ClientManager.removeClient(client);

            synchronized (Server.clientManager) {
                Server.clientManager.notify();
            }
        } else {
            Message notgreeted = new Message(Services.Negado.getByte(), 1, new byte[]{Nack.ClienteSemHello.getByte()});
            byte[] notGreetedAnswer = Message.getMsgAsByteVector(notgreeted);
            client.sendMessage(notGreetedAnswer);
        }

    }

    @Override
    public void run() {

        while (true) {

            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {

            }

            if (requestList.size() > 0) {

                Request request = requestList.remove(0);

                HandleMessage(request.getClient(), request.getMsg());

            }

        }

    }

}
