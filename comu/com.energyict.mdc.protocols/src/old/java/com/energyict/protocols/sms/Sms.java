/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.sms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 16-sep-2010
 * Time: 8:51:18
 * To change this template use File | Settings | File Templates.
 */
public class Sms implements Serializable {

    private static final Log logger = LogFactory.getLog(Sms.class);

    public static int LATIN9_7_BITS = 7;
    public static int HEX_8_BITS = 8;
    public static int UCS2_16_BITS = 16;

    private String from;
    private String to;
    private Date date;
    private String network;
    private String id;
    private int bits;
    private byte[] message;

    //Constructor

    public Sms(String from, String to, Date date, String network, String id, int bits, byte[] message) {
        this.from = from;
        this.to = to;
        this.date = date;
        this.network = network;
        this.id = id;
        this.bits = bits;
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getNetwork() {
        return network;
    }

    public String getId() {
        return id;
    }

    public int getBits() {
        return bits;
    }

    @Override
    public String toString() {
        String s = null;
        try {
            s = "From: " + this.from + "\n" + "To: " + this.to + "\n" + "Date: " + this.date + "\n"
                    + "Network: " + this.network + "\n" + "Id: " + this.id + "\n" + "bits: " + this.bits
                    + "\n" + "Message: " + this.getText();

        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return s;
    }

    public byte[] getMessage() {
        return message;
    }

    public String getText() throws UnsupportedEncodingException {
        String text = null;
        if (bits == 8) {
            text = new String(message);
        }
        if (bits == 16) {
            text = new String(message, "UTF-16");
        }
        //The 7bits format needs decoding
        if (bits == 7) {
            text = decode7Bit(message);
        }
        return text;
    }

    private String decode7Bit(byte[] message) {
        int extraBytes = (int) (message.length / 7);
        byte[] result = new byte[message.length + extraBytes];
        int lengthPart1 = 0;
        int currentByte;
        int part1 = 0;
        int part2;
        int j = 0;

        for (int i = 0; i < message.length; i++) {

            lengthPart1++;

            //special case, every 8th byte
            if (lengthPart1 == 8) {
                lengthPart1 = 0;
                result[j] = (byte) part1;
                part1 = 0;
                i--;

                //normal case
            } else {
                currentByte = (int) message[i] & 0xFF;

                //get the signifant part
                part2 = (currentByte << lengthPart1) & 0xFF;
                part2 = (part2 >> lengthPart1) & 0xFF;
                part2 = (part2 << (lengthPart1 - 1)) & 0xFF;

                //make the result
                result[j] = (byte) (part2 | part1);

                //to use with the next byte
                part1 = (currentByte >> (8 - lengthPart1));
            }
            j++;
        }
        return new String(result);
    }

}




