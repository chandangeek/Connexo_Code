package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.elster.ctr.encryption.CTREncryption;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 27-okt-2010
 * Time: 9:32:10
 */
public class DecryptSniffer {

    public static void main(String[] args) {
        String fileContent = new String(ProtocolTools.readBytesFromFile("c:\\dump.txt"));

        fileContent = fileContent.replaceAll("[ ]{3,}+.{1,}", "");
        fileContent = fileContent.replace("\r", "");
        fileContent = fileContent.replace("\n", "");
        fileContent = fileContent.replace(" ", "");
        fileContent = fileContent.replace("Req", "\nReq");
        fileContent = fileContent.replace("Ans", "\nAns");
        fileContent = fileContent.replace(")", ")\n");

        String[] lines = fileContent.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.length() == 284) {
                try {
                    sb.append(decrypt(line));
                } catch (CTRException e) {
                    sb.append(e.getMessage()).append(" => ").append(line);
                }
            } else {
                sb.append(line);
            }
            sb.append("\r\n");
        }

        ProtocolTools.writeBytesToFile("c:\\dump_decrypted.txt", sb.toString().getBytes(), false);

    }

    private static String decrypt(String packet) throws CTRParsingException, CtrCipheringException {
        CTREncryption encryption = new CTREncryption("32323232323232323232323232323232", "32323232323232323232323232323232", "32323232323232323232323232323232", 1);
        GPRSFrame frame = new GPRSFrame().parse(ProtocolTools.getBytesFromHexString(packet, ""), 0);
        GPRSFrame decryptedFrame = (GPRSFrame) encryption.decryptFrame(frame);
        decryptedFrame.doParse();

        System.out.println("");
        System.out.println(decryptedFrame.getData());
        System.out.println("");

        return ProtocolTools.getHexStringFromBytes(decryptedFrame.getBytes(), " ");
    }

}
