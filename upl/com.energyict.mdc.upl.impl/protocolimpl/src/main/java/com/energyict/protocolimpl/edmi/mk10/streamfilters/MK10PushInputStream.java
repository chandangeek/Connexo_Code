package com.energyict.protocolimpl.edmi.mk10.streamfilters;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.CircularByteBuffer;
import com.energyict.protocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MK10PushInputStream extends InputStream {

    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;
    private static final byte DLE = 0x10;
    private static final byte XON = 0x11;
    private static final byte XOFF = 0x13;

    private final InputStream udpInputStream;
    private final CircularByteBuffer circularByteBuffer = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE, false);
    private final Logger logger;

    public MK10PushInputStream(InputStream udpInputStream, Logger logger) {
        this.udpInputStream = udpInputStream;
        this.logger = logger;
    }

    @Override
    public int read() throws IOException {
        movePacketFromUDPStream();
        return circularByteBuffer.getInputStream().read();
    }

    @Override
    public int available() throws IOException {
        movePacketFromUDPStream();
        return circularByteBuffer.getInputStream().available();
    }

    @Override
    public void reset() throws IOException {
        movePacketFromUDPStream();
        circularByteBuffer.getInputStream().reset();
    }

    @Override
    public long skip(long n) throws IOException {
        movePacketFromUDPStream();
        return circularByteBuffer.getInputStream().skip(n);
    }

    @Override
    public void close() throws IOException {
        movePacketFromUDPStream();
        circularByteBuffer.getInputStream().close();
    }

    @Override
    public void mark(int readlimit) {
        circularByteBuffer.getInputStream().mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return circularByteBuffer.getInputStream().markSupported();
    }

    /**
     * Check if there is data, read a full packet, convert it to a basic EDMI commandLine packet
     * and place it in the circular buffer for the MK10 protocol
     */
    private synchronized void movePacketFromUDPStream() throws IOException {
        byte[] rawPacket = getDataFromUDPStream();
        if ((rawPacket != null) && (rawPacket.length > 0)) {
            byte[] packet = rawPacket.clone();
            if (packet[0] == (byte) 'E') {
                packet = checkAndRemoveCRC(packet);
                packet = addStxAndCrc(packet);
                packet = addBitStuffing(packet);
                packet = addEtx(packet);
                circularByteBuffer.getOutputStream().write(packet);
                logRX(rawPacket, packet);
            } else if (packet[0] == (byte) 0x8F) {
                PushPacket pushPacket = PushPacket.getPushPacket(packet);
                logRX(rawPacket, null);
                throw new IOException("Received unexpected push packet in a communication session: " + pushPacket);
            } else {
                logRX(rawPacket, null);
                throw new IOException("Received invalid packet in a communication session: " + ProtocolTools.getHexStringFromBytes(packet));
            }
        }
    }

    private byte[] checkAndRemoveCRC(byte[] rawPacket) {
        byte[] noCrc = new byte[rawPacket.length - 2];
        System.arraycopy(rawPacket, 0, noCrc, 0, noCrc.length);
        return noCrc;
    }

    private byte[] addStxAndCrc(byte[] rawPacket) {
        byte[] crcPacket = new byte[rawPacket.length + 3];
        crcPacket[0] = STX;
        System.arraycopy(rawPacket, 0, crcPacket, 1, rawPacket.length);
        int crc = CRCGenerator.ccittCRC(crcPacket, crcPacket.length - 2);
        crcPacket[crcPacket.length - 2] = (byte) (crc >> 8);
        crcPacket[crcPacket.length - 1] = (byte) (crc);
        return crcPacket;
    }

    private byte[] addBitStuffing(byte[] rawPacket) {
        ByteArrayOutputStream stuffedBuffer = new ByteArrayOutputStream();
        stuffedBuffer.write(rawPacket[0]);
        for (int i = 1; i < rawPacket.length; i++) {
            byte b = rawPacket[i];
            switch(b) {
            case STX:
            case ETX:
            case DLE:
            case XON:
            case XOFF:
                stuffedBuffer.write(DLE);
                stuffedBuffer.write((byte)(b|0x40));
                break;
            default:
                stuffedBuffer.write(b);
            }
        }
        return stuffedBuffer.toByteArray();
    }

    private byte[] addEtx(byte[] rawPacket) {
        byte[] returnValue = new byte[rawPacket.length + 1];
        System.arraycopy(rawPacket, 0, returnValue, 0, rawPacket.length);
        returnValue[returnValue.length - 1] = ETX;
        return returnValue;
    }

    private synchronized byte[] getDataFromUDPStream() throws IOException {
        ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
        int bytesRead;
        while (udpInputStream.available() > 0) {
            delay(5);
            byte[] udpBuffer = new byte[1024];
            bytesRead = udpInputStream.read(udpBuffer);
            packetBuffer.write(udpBuffer, 0, bytesRead);
        }
        return packetBuffer.toByteArray();
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    private void logRX(byte[] rawPacket, byte[] edmiPacket) {
        if (this.logger != null) {
            String raw = rawPacket != null ? ProtocolTools.getHexStringFromBytes(rawPacket) : "null";
            String edmi = edmiPacket != null ? ProtocolTools.getHexStringFromBytes(edmiPacket) : "null";
            String currentMillis = "[" + System.currentTimeMillis() + "]  ";
            this.logger.log(Level.INFO, currentMillis + "RX RAW = " + raw + ", EDMI = " + edmi);
        }
    }

}
