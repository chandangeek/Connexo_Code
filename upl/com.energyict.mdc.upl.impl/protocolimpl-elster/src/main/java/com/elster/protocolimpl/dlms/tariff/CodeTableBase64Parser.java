package com.elster.protocolimpl.dlms.tariff;

import com.elster.protocolimpl.dlms.tariff.objects.CodeObject;
import com.energyict.protocolimpl.utils.ProtocolTools;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Copyrights EnergyICT
 * Date: 30/03/11
 * Time: 10:27
 */
public class CodeTableBase64Parser {

    public static CodeObject getCodeTableFromBase64(byte[] base64Content) throws IOException {
        return getCodeTableFromBase64(new String(base64Content));
    }

    public static CodeObject getCodeTableFromBase64(String content) throws IOException {
        try {
            byte[] decodedContent = new BASE64Decoder().decodeBuffer(content);
            ByteArrayInputStream in = new ByteArrayInputStream(decodedContent);
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
            Object object = ois.readObject();
            if (object instanceof CodeObject) {
                return (CodeObject) object;
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static CodeObject getCodeTableFromBase64(File file) throws IOException {
        return getCodeTableFromBase64(ProtocolTools.readBytesFromFile(file));
    }
}
