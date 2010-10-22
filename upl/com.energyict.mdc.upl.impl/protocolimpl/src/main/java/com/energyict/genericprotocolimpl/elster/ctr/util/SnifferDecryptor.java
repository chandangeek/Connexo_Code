package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.elster.ctr.encryption.CTREncryption;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyrights EnergyICT
 * Date: 21-okt-2010
 * Time: 15:45:48
 */
public class SnifferDecryptor {

    public static final String REGEX_1 = "(((\\s[A-F[0-9]]{2}){2,16})(.*)((\r*)(\n*))*){1,}";
    public static final String REGEX_2 = "(\\s[A-F[0-9]]{2}){4,16}";
    public static final Pattern regex1 = Pattern.compile(REGEX_1);
    public static final Pattern regex2 = Pattern.compile(REGEX_2);
    private static String inFile = "c:\\encrypted.htm";
    private static String outFile = "c:\\decrypted.htm";

    public static void main(String[] args) throws CTRParsingException, CtrCipheringException {

        String htmlContent = new String(ProtocolTools.readBytesFromFile(inFile));
        List<Frame> frames = new ArrayList<Frame>();
        List<String> items = getMatches(htmlContent, regex1);
        for (String item : items) {
            byte[] rawFrame = ProtocolTools.getBytesFromHexString(appendMatches(item, regex2).replace(" ", ""), "");
            GPRSFrame frame = new GPRSFrame().parse(rawFrame, 0);
            frames.add(decryptFrame(frame));
        }

        String newContent = replaceInContent(htmlContent, frames);
        ProtocolTools.writeBytesToFile(outFile, newContent.getBytes(), false);

    }

    private static String replaceInContent(String content, List<Frame> frames) {
        for (Frame frame : frames) {
            byte[] rawFrame = frame.getBytes();
            for (int i = 0; i < rawFrame.length; i += 16) {
                byte[] bytes = ProtocolTools.getSubArray(rawFrame, i, (i + 16) < rawFrame.length ? i + 16 : i + (rawFrame.length - i));
                try {
                    content = content.replaceFirst(REGEX_2, ProtocolTools.getHexStringFromBytes(bytes, "-"));
                } catch (Exception e) {
                }
            }
        }
        return content.replace("-", " ");
    }

    private static GPRSFrame decryptFrame(GPRSFrame frame) throws CtrCipheringException, CTRParsingException {
        CTREncryption encryption = new CTREncryption("32323232323232323232323232323232", "32323232323232323232323232323232", "32323232323232323232323232323232");
        GPRSFrame gprsFrame = ((GPRSFrame) encryption.decryptFrame(frame)).doParse();
        System.out.println(gprsFrame);
        return gprsFrame;
    }

    private static List<String> getMatches(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        List<String> strings = new ArrayList<String>();
        while (matcher.find()) {
            strings.add(matcher.group());
        }
        return strings;
    }

    private static String appendMatches(String text, Pattern pattern) {
        List<String> strings = getMatches(text, pattern);
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string);
        }
        return sb.toString();
    }


}
