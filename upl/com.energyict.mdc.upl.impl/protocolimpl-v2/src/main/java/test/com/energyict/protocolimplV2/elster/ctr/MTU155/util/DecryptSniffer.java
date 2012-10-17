package test.com.energyict.protocolimplV2.elster.ctr.MTU155.util;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.encryption.CTREncryption;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRException;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRParsingException;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CtrCipheringException;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.frame.GPRSFrame;

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
                    sb.append(format(decrypt(line)));
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

    private static String format(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i += (3 * 16)) {
            int end = i + (3 * 16);
            if (end > input.length()) {
                end = input.length();
            }
            sb.append(input.substring(i, end));
            sb.append("\r\n");
        }
        return sb.toString();
    }

    private static String decrypt(String packet) throws CTRParsingException, CtrCipheringException {
        CTREncryption encryption = new CTREncryption("30303030303030303030303030303031", "30303030303030303030303030303031", "30303030303030303030303030303031", 1);
        GPRSFrame frame = new GPRSFrame().parse(ProtocolTools.getBytesFromHexString(packet, ""), 0);
        GPRSFrame decryptedFrame = (GPRSFrame) encryption.decryptFrame(frame);
        decryptedFrame.doParse();

        System.out.println("");
        System.out.println(decryptedFrame.getData());
        System.out.println("");

        return ProtocolTools.getHexStringFromBytes(decryptedFrame.getBytes(), " ");
    }

}
