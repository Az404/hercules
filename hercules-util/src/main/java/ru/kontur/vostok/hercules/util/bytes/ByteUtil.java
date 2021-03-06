package ru.kontur.vostok.hercules.util.bytes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author Gregory Koshelev
 */
public class ByteUtil {

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * Check if array starts with subarray
     *
     * @param array
     * @param subarray
     * @return true if array starts with subarray
     */
    public static boolean isSubarray(byte[] array, byte[] subarray) {
        if (array.length < subarray.length) {
            return false;
        }
        for (int i = 0; i < subarray.length; i++) {
            if (subarray[i] != array[i]) {
                return false;
            }
        }
        return true;
    }

    public static long toLong(byte[] bytes) {
        if (bytes == null || bytes.length != 8) {
            throw new IllegalArgumentException("The length of byte array should be equal to 8");
        }
        return ((bytes[0] & 0xFFL) << 56)
                | ((bytes[1] & 0xFFL) << 48)
                | ((bytes[2] & 0xFFL) << 40)
                | ((bytes[3] & 0xFFL) << 32)
                | ((bytes[4] & 0xFFL) << 24)
                | ((bytes[5] & 0xFFL) << 16)
                | ((bytes[6] & 0xFFL) << 8)
                | (bytes[7] & 0xFFL);
    }

    public static byte[] fromByteBuffer(ByteBuffer buffer) {
        int size = buffer.remaining();
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        return bytes;
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2 + 2];
        hexChars[0] = '0';
        hexChars[1] = 'x';

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2 + 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 3] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
