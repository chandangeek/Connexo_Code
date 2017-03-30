/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.Ignore;
import org.junit.Test;

@Ignore // local testing only
public class InboundCommunicationLoggingTest {

    private static final int PREFIX_AND_HEX_LENGTH = 3;
    private static final int HEX = 16;
    private static final byte[] PLAIN_FRAME = getBytesFromHexString("000100010001007AC2004E2C000080000CFF030205090F3636302D3030353435442D31313235090C07DE080D030A2A11410000001200001200C209464733203A204E6F6465205B303232333A374546463A464546443A414145395D205B3078303030365D206861732072656769737465726564206F6E20746865206E6574776F726B","");

    @Test
    public void simulateInboundCommunicationAndCheckLogging() throws IOException{
            String serverName = "PasquienD.eict.local";
            int port = 1111;

            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.write(PLAIN_FRAME);
            client.close();

    }

    public static byte[] getBytesFromHexString(final String hexString, final String prefix) {
        int prefixLength = (prefix == null) ? 0 : prefix.length();
        int charsPerByte = prefixLength + 2;
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += charsPerByte) {
            bb.write(Integer.parseInt(hexString.substring(i + prefixLength, i + charsPerByte), HEX));
        }
        return bb.toByteArray();
    }

}
