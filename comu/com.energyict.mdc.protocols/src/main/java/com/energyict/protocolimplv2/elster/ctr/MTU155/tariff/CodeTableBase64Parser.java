package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
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

    public static CodeObject getCodeTableFromBase64(File file) throws IOException {
        return getCodeTableFromBase64(ProtocolTools.readBytesFromFile(file));
    }

    public static CodeObject getCodeTableFromBase64(String content) throws IOException {
        try {
            byte[] decodedContent = Base64.getDecoder().decode(content);
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
}
