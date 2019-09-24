package com.energyict.mdc.device.data.impl.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/02/2015 - 14:33
 */
public class DataCompressor {

    private static final String UTF8 = "UTF-8";

    public static byte[] encodeAndCompress(String serializedText, Boolean compress) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        if (compress) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(result);
            gzipOutputStream.write(serializedText.getBytes(UTF8));
            gzipOutputStream.flush();
            gzipOutputStream.close();
        } else {
            result.write(serializedText.getBytes(UTF8));
            result.flush();
            result.close();
        }
        return result.toByteArray();
    }

    public static String decompressAndDecode(byte[] compressedData, Boolean decompress) throws IOException {
        InputStream input = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        if (decompress) {
            input = new GZIPInputStream(input);
        }
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) > 0) {
            result.write(buffer, 0, bytesRead);
        }
        return result.toString(UTF8);
    }
}
