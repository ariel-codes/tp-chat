package common.utils;

/**
 *
 * @author asantos
 */
public class MsgUtils {

    public static char make2BytesChar(byte maisSig, byte menosSig) {

        int mais, menos;

        mais = (int) maisSig & 0xFF;
        menos = (int) menosSig & 0xFF;

        return (char) (((mais << 8) | menos) & 0xFFFF);

    }

    public static String byteVectorToString(byte[] ByteVector) {

        if ((ByteVector.length % 2) != 0) {
            // Não possui um número par de bytes
            throw new RuntimeException("Can't make a string with an odd number of bytes.");
        }

        String mountedString = "";

        for (int i = 0; i < ByteVector.length; i += 2) {
            char character = make2BytesChar(ByteVector[i], ByteVector[i + 1]);
            mountedString += String.valueOf(character);
        }

        return mountedString;

    }

    public static byte[] stringToByteVector(String text) {

        byte[] tmpByte = new byte[text.length() * 2];
        for (int i = 0; i < text.length(); i++) {
            tmpByte[i * 2 + 0] = (byte) (text.charAt(i) >> 8);
            tmpByte[i * 2 + 1] = (byte) (text.charAt(i) >> 0);
        }
        return tmpByte;

    }

    public static int byteVectorToInteger(byte[] byteVector) {

        if (byteVector.length < 4) {
            // Não dá para converter para inteiro
            throw new RuntimeException("Can't convert a vector with less than 4 bytes to integer.");
        }

        return (((int) byteVector[0] << 24) & 0xFF000000)
                | (((int) byteVector[1] << 16) & 0x00FF0000)
                | (((int) byteVector[2] << 8) & 0x0000FF00)
                | (((int) byteVector[3] << 0) & 0x000000FF);

    }

    public static byte[] integerToByteVector(int integer) {

        byte[] tmpBytes = new byte[4];

        tmpBytes[0] = (byte) (integer >> 24);
        tmpBytes[1] = (byte) (integer >> 16);
        tmpBytes[2] = (byte) (integer >> 8);
        tmpBytes[3] = (byte) (integer >> 0);

        return tmpBytes;

    }

}
