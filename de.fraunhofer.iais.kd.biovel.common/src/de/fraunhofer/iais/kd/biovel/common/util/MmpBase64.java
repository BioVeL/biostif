package de.fraunhofer.iais.kd.biovel.common.util;

import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

/**
 * Convenience methods for base64 encoding and decoding using only String
 * Objects. These methods encapsulate the very implementation selected for
 * base64 coding.
 */
public class MmpBase64 {

    static {
        try {
            Class.forName("org.apache.commons.codec.binary.Base64");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(MmpBase64.class.getName());

    public static String encode(String s) {
        return encode(s, false);
    }

    public static String encodeChunked(String s) {
        return encode(s, true);
    }

    public static String encode(String s, boolean chunked) {
        byte[] sb = s.getBytes();
        //Note to TQS: string instatiation from a byte[] is exactly what is wanted here -> do no touch
        byte[] encodedBytes = Base64.encodeBase64(sb, chunked);
        String encodedString = new String(encodedBytes);
        return encodedString;
    }

    public static String encode(byte[] binaryData) {
        byte[] encodedBytes = Base64.encodeBase64(binaryData);
        String encodedString = new String(encodedBytes);
        return encodedString;
    }

    public static String decode(String d) {
        byte[] decodedBytes = Base64.decodeBase64(d.getBytes());
        //Note to TQS: string instatiation from a byte[] is exactly what is wanted here -> do no touch
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    public static byte[] decodeToByte(String d) {
        return Base64.decodeBase64(d.getBytes());
    }

}
