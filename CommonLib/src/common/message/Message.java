package common.message;

/**
 *
 * @author asantos07
 */
public class Message {

    public enum Services {

        Ola((byte) 0x01),
        MudaNick((byte) 0x02),
        ClientesConectados((byte) 0x03),
        PedeNick((byte) 0x04),
        EnviaMensagem((byte) 0x05),
        Tchau((byte) 0x0A),
        Negado((byte) 0x7F),
        Invalido((byte) 0xFF);

        private final byte valor;

        private Services(byte serviceByte) {
            this.valor = serviceByte;
        }

        public byte getByte() {
            return this.valor;
        }

        public static Services getService(byte b) {
            Services s = null;
            switch (b) {
                case 0x01:
                    s = Ola;
                    break;
                case 0x02:
                    s = MudaNick;
                    break;
                case 0x03:
                    s = ClientesConectados;
                    break;
                case 0x04:
                    s = PedeNick;
                    break;
                case 0x05:
                    s = EnviaMensagem;
                    break;
                case 0x0A:
                    s = Tchau;
                    break;
                case 0x7F:
                    s = Negado;
                    break;
                default:
                    s = Invalido;
                    break;
            }
            return s;
        }
    };

    private final byte service;
    private final int size;
    private final byte[] data;

    public static final int PAYLOAD = 5;

    public Message(byte service, int size, byte[] data) {
        this.service = service;
        this.size = size;
        this.data = data;
    }

    public byte getService() {
        return this.service;
    }

    public int getSize() {
        return this.size;
    }

    public byte[] getData() {
        return this.data;
    }

    public static void printMessage(byte[] msg) {

        for (byte b : msg) {
            System.out.print(String.format("%02X ", b));
        }

        System.out.println();

    }

    public static int getCheckSum(Message msg) {

        int checksum = (int) msg.service & 0xFF;

        checksum += (int) (msg.size >> 8) & 0xFF;
        checksum += (int) (msg.size >> 0) & 0xFF;

        for (int i = 0; i < msg.data.length; i++) {
            checksum += ((int) msg.data[i] & 0xFF);
        }

        return checksum & 0xFFFF;

    }

    public static byte[] getMsgAsByteVector(Message msg) {

        byte[] messageAsByte = new byte[msg.size + PAYLOAD];

        int checksum = getCheckSum(msg);

        messageAsByte[0] = msg.service;
        messageAsByte[1] = (byte) (msg.size >> 8);
        messageAsByte[2] = (byte) (msg.size >> 0);

        System.arraycopy(msg.data, 0, messageAsByte, 3, msg.size);

        messageAsByte[msg.size + 3] = (byte) ((checksum >> 8) & 0x000000FF);
        messageAsByte[msg.size + 4] = (byte) ((checksum >> 0) & 0x000000FF);

        return messageAsByte;

    }

}
