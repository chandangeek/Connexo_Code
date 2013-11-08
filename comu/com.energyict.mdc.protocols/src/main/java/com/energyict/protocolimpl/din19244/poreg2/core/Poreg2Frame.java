package com.energyict.protocolimpl.din19244.poreg2.core;

import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Class with static methods generating standard Poreg2 frames.
 *
 * Copyrights EnergyICT
 * Date: 26-apr-2011
 * Time: 11:29:43
 */
public class Poreg2Frame {

    private static final int DISCONNECT_LENGTH = 9;
    private static final int END = 0x16;
    private static final int START_VARIABLE = 0x68;

    private static final byte[] FILL = new byte[4];
    private static final byte[] CONNECT_START = new byte[]{(byte) 0x63, (byte) 0x6F};
    private static final byte[] CONNECT_END = new byte[]{(byte) 0x0D};
    private static final byte[] SPACE = new byte[]{(byte) 0x20};

    public static byte[] getDisconnectFrame(Poreg poreg) {
        byte[] disconnect = new byte[0];
        disconnect = ProtocolTools.concatByteArrays(disconnect, getControlByte());
        disconnect = ProtocolTools.concatByteArrays(disconnect, getAddressBytes(poreg.getNodeId()));
        disconnect = ProtocolTools.concatByteArrays(disconnect, ASDU.Disconnect.getIdBytes());
        disconnect = ProtocolTools.concatByteArrays(disconnect, new byte[]{(byte) DISCONNECT_LENGTH - 3});
        disconnect = ProtocolTools.concatByteArrays(disconnect, FILL);
        disconnect = ProtocolTools.concatByteArrays(disconnect, getEnd(disconnect));

        //Add the header
        disconnect = ProtocolTools.concatByteArrays(getVariableHeader(DISCONNECT_LENGTH), disconnect);
        return disconnect;
    }

    public static byte[] getContinueFrame(Poreg poreg) {
        byte[] request = new byte[0];
        request = ProtocolTools.concatByteArrays(request, getControlByte());                        //Control byte
        request = ProtocolTools.concatByteArrays(request, getAddressBytes(poreg.getNodeId()));     //Address bytes
        request = ProtocolTools.concatByteArrays(request, ASDU.Continue.getIdBytes());              //Continue command = 0x45 = 69
        request = ProtocolTools.concatByteArrays(request, new byte[1]);                             //The remaining length, left blank atm
        request = ProtocolTools.concatByteArrays(request, FILL);                                    //4 zeroes !?

        int length = request.length;
        request[4] = (byte) (length - 3);                                                           //Fill in the remaining length
        request = ProtocolTools.concatByteArrays(request, getEnd(request));                         //Add crc and end byte
        request = ProtocolTools.concatByteArrays(getVariableHeader(length), request);               //Insert the header
        return request;

    }

    public static byte[] getRequestFrame(Poreg poreg, byte[] requestType, byte[] extraInfo) {
        byte[] request = new byte[0];
        request = ProtocolTools.concatByteArrays(request, getControlByte());                        //Control byte
        request = ProtocolTools.concatByteArrays(request, getAddressBytes(poreg.getNodeId()));     //Address bytes
        request = ProtocolTools.concatByteArrays(request, requestType);                             //Read register command = 0x8C = 140
        request = ProtocolTools.concatByteArrays(request, new byte[1]);                             //The remaining length, left blank atm
        request = ProtocolTools.concatByteArrays(request, FILL);                                    //4 zeroes !?
        request = ProtocolTools.concatByteArrays(request, extraInfo);                               //E.g. for registers this is where the number of fields etc comes

        int length = request.length;
        request[4] = (byte) (length - 3);                                                           //Fill in the remaining length
        request = ProtocolTools.concatByteArrays(request, getEnd(request));                         //Add crc and end byte
        request = ProtocolTools.concatByteArrays(getVariableHeader(length), request);               //Insert the header
        return request;
    }

    public static byte[] getConnectFrame(String strID, String strPassword, String nodeId) {
        return ProtocolTools.concatByteArrays(CONNECT_START,
                SPACE,
                nodeId.getBytes(),
                SPACE,
                strID.getBytes(),
                SPACE,
                strPassword.getBytes(),
                CONNECT_END);
    }

    private static byte[] getVariableHeader(int length) {
        return new byte[]{(byte) START_VARIABLE, (byte) length, (byte) length, (byte) START_VARIABLE};
    }

    private static byte[] getControlByte() {
        return new byte[]{0x5B};
    }

    private static byte[] getAddressBytes(String address) {
        byte[] hex = ProtocolTools.getBytesFromInt(Integer.parseInt(address), 3);
        return new byte[]{hex[2], hex[1]};
    }

    private static byte[] getEnd(byte[] request) {
        int crc = calcCRC(request);
        return new byte[]{(byte) crc, (byte) END};
    }

    private static int calcCRC(byte[] userData) {
        return CRCGenerator.getModulo256(userData);
    }
}
