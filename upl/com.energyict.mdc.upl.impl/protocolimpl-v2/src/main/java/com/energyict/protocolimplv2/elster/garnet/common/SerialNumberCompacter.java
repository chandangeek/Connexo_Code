package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Utility class that can be used to compact an - up to 16 characters long - serialNumber into 8 bytes BCD/ASCII.
 * This class can also be used to decompact a serialNumber from its compacted form.
 *
 * @author sva
 * @since 4/06/2014 - 15:30
 */
public class SerialNumberCompacter {

    /**
     * Pack the serialNumber (16 ASCII characters) into 8 packed bytes
     *
     * @param serialNumber the serial number who should be compacted
     * @return the compacted serial number as byte array
     */
    public static byte[] packSerialNumber(String serialNumber) {
        byte[] serialNumberBytes = serialNumber.getBytes(Charset.forName("US-ASCII"));
        byte[] packedSerial = new byte[8];

        byte currentByte;
        byte nextByte = (byte) 0;
        int nextBytePosition;
        int packedSerialPos = 0;
        boolean lastByte;

        //For all bytes of the serial number
        for (int i = 0; i < serialNumberBytes.length; i++) {
            //takes the current byte
            currentByte = serialNumberBytes[i];
            //Verifies if it’s a numerical character
            if (currentByte >= 0x30 && currentByte <= 0x39) {
                //If it’s a numerical character
                //Store the position of the next byte in my nextBytePosition variable
                nextBytePosition = i + 1;
                //Verify if it’s the last byte
                if (nextBytePosition >= serialNumberBytes.length) {
                    lastByte = true;
                } else {
                    //If it’s not the last byte
                    lastByte = false;
                    //Takes the next byte
                    nextByte = serialNumberBytes[nextBytePosition];
                }
                //If the next byte is not the last one, verify if he is numerical
                if (!lastByte && nextByte >= 0x30 && nextByte <= 0x39) {
                    //If  the next byte is not the last, and is numerical,
                    //store the current byte on the more significant nibble and the next byte
                    //On the less significant nibble, packing the currentByte and the nextByte
                    // in only 1 byte
                    int packedByte = (currentByte - 0x30) << 4;
                    packedByte += nextByte - 0x30;
                    packedSerial[packedSerialPos++] = (byte) (packedByte & 0xFF);
                    i = i + 1;
                } else {
                    //If it’s the last byte OR the next byte, even if it’s not the last, it is not numerical,
                    //It cannot be packed, thus set the mor significant bit of the byte, to sinalise that is ASCII and store the
                    // byte
                    currentByte += 0x80;
                    packedSerial[packedSerialPos++] = currentByte;
                }
            } else {
                //If the current character is not numerical, has to store it as ASCII
                //if it’s a blank space (0x20), goes to trash, ignoring
                if (currentByte != 0x20) {
                    //If it’s not a blank space, set the more significant bit to signaling that is ASCII and store the byte
                    currentByte += 0x80;
                    packedSerial[packedSerialPos++] = currentByte;
                }
            }
        }

        //After packing of the bytes,
        //there may be remained space(s), because the serial number packed vector
        //must be composed by 8 bytes; to indicate the bytes
        //that are remaining, fill the less significant bytes of the command with characters 0xFF
        while (packedSerialPos < 8) {
            packedSerial[packedSerialPos++] = (byte) 0xFF;
        }
        //Returns the packed serial number
        return packedSerial;
    }

    /**
     * Unpack the compacted serial number(given as byte array) back into its up to 16 ASCII characters serial number
     *
     * @param packedSerialNumberBytes the 8 bytes compacted form of the serial number
     * @return the full-length serial number
     * @throws ParsingException
     */

    public static String unPackSerialNumber(byte[] packedSerialNumberBytes) throws ParsingException {
        String serialNumber = "";
        for (byte packedSerialNumberByte : packedSerialNumberBytes) {
            int b = packedSerialNumberByte & 0xFF;
            if (b <= 0x99) {    // The byte can be considered as plain BCD
                serialNumber += String.format("%02d", getIntFromBCD(new byte[]{(byte) b}, 0, 1));
            } else {            // Subtract 0x80 and consider as ASCII
                b -= 0x80;
                if (b != 0x7F) { // 0x7F can be ignored
                    serialNumber += ProtocolTools.getAsciiFromBytes(new byte[]{(byte) b}, ' ');
                }
            }
        }
        return serialNumber;
    }

    private static int getIntFromBCD(byte[] rawData, int offset, int length) throws ParsingException {
        try {
            return ProtocolUtils.getBCD2Int(rawData, offset, length);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    // Utility class, hide constructor
    private SerialNumberCompacter() {
    }
}
