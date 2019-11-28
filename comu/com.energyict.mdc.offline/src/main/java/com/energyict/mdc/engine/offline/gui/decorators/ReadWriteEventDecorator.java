package com.energyict.mdc.engine.offline.gui.decorators;

import com.energyict.mdc.engine.offline.gui.util.ProtocolTools;

import java.util.HashMap;

/**
 * @author sva
 * @since 11/03/14 - 8:28
 */
public class ReadWriteEventDecorator extends DefaultEventDecorator implements EventDecorator {

    public ReadWriteEventDecorator(HashMap<String, Object> event) {
        super(event);
    }

    public String asLogString() {
        String logString = this.getOccurenceDateAsString() + " - " + this.getEvent().get("clazz") + ": ";
        if (this.getEvent().containsKey("bytes-read")) {
            logString += " bytes read:" + this.getEvent().get("bytes-read");
        }
        if (this.getEvent().containsKey("bytes-written")) {
            logString += " bytes written:" + this.getEvent().get("bytes-written");
        }
        return logString;
    }

    public String logHexPartReadWriteEvent() {
        String formattedHexPart = getDateAndTypePart() + System.lineSeparator();
        String hexPart = getHex();

        int x = 0;
        int y = 0;
        int z = 0;
        for (int i = 0; i < hexPart.length(); i++) {
            formattedHexPart += hexPart.charAt(i);
            if (++x == 2) {
                x = 0;
                if (++y == 4) {
                    y = 0;
                    if (++z == 4) {
                        z = 0;
                        formattedHexPart += System.lineSeparator();
                    }   else {
                        formattedHexPart += "  ";
                    }
                } else{
                    formattedHexPart += " ";
                }
            }
        }
        formattedHexPart += System.lineSeparator();
        return formattedHexPart;
    }

    public String logAsciiPartReadWriteEvent() {
        String formattedAsciiPart = getDateAndTypePart() + System.lineSeparator();
        String asciiPart = getASCII();

        int t = 0;
        for (int i = 0; i < asciiPart.length(); i++) {
            formattedAsciiPart += asciiPart.charAt(i);
            if (++t == 16) {
                formattedAsciiPart += System.lineSeparator();
                t = 0;
            }
        }

        formattedAsciiPart += System.lineSeparator();
        return formattedAsciiPart;
    }

    public String getDateAndTypePart() {
        return this.getOccurenceDateAsString() + " - " + this.getType((String) this.getEvent().get("clazz"));
    }

    private String getType(String eventClass) {
        if (eventClass.equals("ReadEvent")) {
            return "RX";
        }
        if (eventClass.equals("WriteEvent")) {
            return "TX";
        }
        return "";
    }

    public String getHex() {
        return ProtocolTools.getHexStringFromBytes(getBytes(), "");
    }

    public String getASCII() {
        return ProtocolTools.getAsciiFromBytes(getBytes());
    }

    private byte[] getBytes() {
        byte[] bytes = new byte[0];

        if (this.getEvent().containsKey("bytes-read")) {
            Object[] objects = (Object[]) this.getEvent().get("bytes-read");
            bytes = ReadWriteEventDecorator.toByteArray(objects);

        }
        if (this.getEvent().containsKey("bytes-written")) {
            Object[] objects = (Object[]) this.getEvent().get("bytes-written");
            bytes = ReadWriteEventDecorator.toByteArray(objects);
        }
        return bytes;
    }

    private static byte[] toByteArray(Object[] in) {
        final int n = in.length;
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = ((Long) in[i]).byteValue();
        }
        return ret;
    }
}