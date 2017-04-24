/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class IF2Packet {

    public static int ST = 0x0F0;

    private static final int APPLICATION_ADDRESS = 0;
    private static final int LAN_WAN_ADDRESS = 1;

    public static final int PING_REQUEST = 0x21;
    public static final int PING_RESPONSE = 0x22;
    public static final int REPORT_POWER_UP = 0x23;
    public static final int ACK_POWER_UP = 0x24;

    public static final int CONNECT_INDICATION = 0x41;
    public static final int DISCONNECT_INDICATION = 0x42;

    public static final int DATA_INDICATION = 0x43;
    public static final int REQUEST_INDICATION = 0x44;

    public static final int IF2_VERSION = 0x01;

    private int sourceAddress = 0;

    private int destinationAddress = 0;
    private int length = 1;
    private int headerCrc = 0;
    private byte[] data = new byte[length];
    private int dataCrc = 0;

    private int dataIndex = -1;

    public IF2Packet(byte[] data) {
        this(APPLICATION_ADDRESS, LAN_WAN_ADDRESS, data);
    }

    public IF2Packet(int sourceAddress, int destinationAddress, byte[] data) {
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.data = data;
        this.length = data.length;
        this.headerCrc = calcCrc8CCITT(toByteArray(), 0, 4);
        this.dataCrc = calcCrc16CCITT(data);
    }

    public IF2Packet() {

    }

    public int getDataCrc() {
        return this.dataCrc;
    }

    public void setDataCrc(int dataCrc) {
        this.dataCrc = dataCrc & 0x0FFFF;
    }

    public int getDestinationAddress() {
        return this.destinationAddress;
    }

    public void setDestinationAddress(int destinationAddress) {
        this.destinationAddress = destinationAddress & 0x0F;
    }

    public void setAddress(int address) {
        this.sourceAddress = (address >> 4) & 0x0F;
        this.destinationAddress = address & 0x0F;
    }

    public int getHeaderCrc() {
        return this.headerCrc;
    }

    public void setHeaderCrc(int headerCrc) {
        this.headerCrc = headerCrc & 0x0FF;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length & 0x0FFFF;
    }

    public int getSourceAddress() {
        return this.sourceAddress;
    }

    public void setSourceAddress(int sourceAddress) {
        this.sourceAddress = sourceAddress & 0x0F;
    }

    public final byte[] getData() {
        return this.data;
    }

    public final void setData(final byte[] data) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, this.data.length);
    }

    public final byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(ST);
            out.write(((this.sourceAddress << 4) & 0x0F0) + (this.destinationAddress & 0x0F));
            out.write(this.length & 0x0FF);
            out.write((this.length >> 8) & 0x0FF);
            out.write(this.headerCrc);
            out.write(this.data);
            out.write(this.dataCrc & 0x0FF);
            out.write((this.dataCrc >> 8) & 0x0FF);
        } catch (IOException e) {
            // should not happen with a ByteArrayOutputStream
        }
        final byte[] bytes = out.toByteArray();

        return bytes;
    }

    /**
     * CRC 8 CCITT lookup-table (poly = 0x07)
     */
    private static final byte[] CRC8_TABLE = new byte[]{
            (byte) 0x00, (byte) 0x07, (byte) 0x0E, (byte) 0x09, (byte) 0x1C, (byte) 0x1B, (byte) 0x12, (byte) 0x15,
            (byte) 0x38, (byte) 0x3F, (byte) 0x36, (byte) 0x31, (byte) 0x24, (byte) 0x23, (byte) 0x2A, (byte) 0x2D,
            (byte) 0x70, (byte) 0x77, (byte) 0x7E, (byte) 0x79, (byte) 0x6C, (byte) 0x6B, (byte) 0x62, (byte) 0x65,
            (byte) 0x48, (byte) 0x4F, (byte) 0x46, (byte) 0x41, (byte) 0x54, (byte) 0x53, (byte) 0x5A, (byte) 0x5D,
            (byte) 0xE0, (byte) 0xE7, (byte) 0xEE, (byte) 0xE9, (byte) 0xFC, (byte) 0xFB, (byte) 0xF2, (byte) 0xF5,
            (byte) 0xD8, (byte) 0xDF, (byte) 0xD6, (byte) 0xD1, (byte) 0xC4, (byte) 0xC3, (byte) 0xCA, (byte) 0xCD,
            (byte) 0x90, (byte) 0x97, (byte) 0x9E, (byte) 0x99, (byte) 0x8C, (byte) 0x8B, (byte) 0x82, (byte) 0x85,
            (byte) 0xA8, (byte) 0xAF, (byte) 0xA6, (byte) 0xA1, (byte) 0xB4, (byte) 0xB3, (byte) 0xBA, (byte) 0xBD,
            (byte) 0xC7, (byte) 0xC0, (byte) 0xC9, (byte) 0xCE, (byte) 0xDB, (byte) 0xDC, (byte) 0xD5, (byte) 0xD2,
            (byte) 0xFF, (byte) 0xF8, (byte) 0xF1, (byte) 0xF6, (byte) 0xE3, (byte) 0xE4, (byte) 0xED, (byte) 0xEA,
            (byte) 0xB7, (byte) 0xB0, (byte) 0xB9, (byte) 0xBE, (byte) 0xAB, (byte) 0xAC, (byte) 0xA5, (byte) 0xA2,
            (byte) 0x8F, (byte) 0x88, (byte) 0x81, (byte) 0x86, (byte) 0x93, (byte) 0x94, (byte) 0x9D, (byte) 0x9A,
            (byte) 0x27, (byte) 0x20, (byte) 0x29, (byte) 0x2E, (byte) 0x3B, (byte) 0x3C, (byte) 0x35, (byte) 0x32,
            (byte) 0x1F, (byte) 0x18, (byte) 0x11, (byte) 0x16, (byte) 0x03, (byte) 0x04, (byte) 0x0D, (byte) 0x0A,
            (byte) 0x57, (byte) 0x50, (byte) 0x59, (byte) 0x5E, (byte) 0x4B, (byte) 0x4C, (byte) 0x45, (byte) 0x42,
            (byte) 0x6F, (byte) 0x68, (byte) 0x61, (byte) 0x66, (byte) 0x73, (byte) 0x74, (byte) 0x7D, (byte) 0x7A,
            (byte) 0x89, (byte) 0x8E, (byte) 0x87, (byte) 0x80, (byte) 0x95, (byte) 0x92, (byte) 0x9B, (byte) 0x9C,
            (byte) 0xB1, (byte) 0xB6, (byte) 0xBF, (byte) 0xB8, (byte) 0xAD, (byte) 0xAA, (byte) 0xA3, (byte) 0xA4,
            (byte) 0xF9, (byte) 0xFE, (byte) 0xF7, (byte) 0xF0, (byte) 0xE5, (byte) 0xE2, (byte) 0xEB, (byte) 0xEC,
            (byte) 0xC1, (byte) 0xC6, (byte) 0xCF, (byte) 0xC8, (byte) 0xDD, (byte) 0xDA, (byte) 0xD3, (byte) 0xD4,
            (byte) 0x69, (byte) 0x6E, (byte) 0x67, (byte) 0x60, (byte) 0x75, (byte) 0x72, (byte) 0x7B, (byte) 0x7C,
            (byte) 0x51, (byte) 0x56, (byte) 0x5F, (byte) 0x58, (byte) 0x4D, (byte) 0x4A, (byte) 0x43, (byte) 0x44,
            (byte) 0x19, (byte) 0x1E, (byte) 0x17, (byte) 0x10, (byte) 0x05, (byte) 0x02, (byte) 0x0B, (byte) 0x0C,
            (byte) 0x21, (byte) 0x26, (byte) 0x2F, (byte) 0x28, (byte) 0x3D, (byte) 0x3A, (byte) 0x33, (byte) 0x34,
            (byte) 0x4E, (byte) 0x49, (byte) 0x40, (byte) 0x47, (byte) 0x52, (byte) 0x55, (byte) 0x5C, (byte) 0x5B,
            (byte) 0x76, (byte) 0x71, (byte) 0x78, (byte) 0x7F, (byte) 0x6A, (byte) 0x6D, (byte) 0x64, (byte) 0x63,
            (byte) 0x3E, (byte) 0x39, (byte) 0x30, (byte) 0x37, (byte) 0x22, (byte) 0x25, (byte) 0x2C, (byte) 0x2B,
            (byte) 0x06, (byte) 0x01, (byte) 0x08, (byte) 0x0F, (byte) 0x1A, (byte) 0x1D, (byte) 0x14, (byte) 0x13,
            (byte) 0xAE, (byte) 0xA9, (byte) 0xA0, (byte) 0xA7, (byte) 0xB2, (byte) 0xB5, (byte) 0xBC, (byte) 0xBB,
            (byte) 0x96, (byte) 0x91, (byte) 0x98, (byte) 0x9F, (byte) 0x8A, (byte) 0x8D, (byte) 0x84, (byte) 0x83,
            (byte) 0xDE, (byte) 0xD9, (byte) 0xD0, (byte) 0xD7, (byte) 0xC2, (byte) 0xC5, (byte) 0xCC, (byte) 0xCB,
            (byte) 0xE6, (byte) 0xE1, (byte) 0xE8, (byte) 0xEF, (byte) 0xFA, (byte) 0xFD, (byte) 0xF4, (byte) 0xF3
    };

    private final int calcCrc8CCITT(byte[] bytes, int offset, int length) {
        byte crc = (byte) 255;
        final byte[] subArray = DLMSUtils.getSubArray(bytes, offset, offset + length);
        for (byte b : subArray) {
            crc = CRC8_TABLE[(crc ^ b) & 0xFF];
        }
        return crc & 0x0FF;
    }

    private final int calcCrc16CCITT(final byte[] data) {
        final int poly = 0x1021;
        int crc = 0x0FFFF;
        for (int t = 0; t < data.length; t++) {
            int val = ((int) data[t] & 0xff);
            crc ^= ((int) (val << 8) & 0xFFFF);
            for (int i = 0; i < 8; i++) {
                int old = crc & 0x8000;
                crc = (crc << 1) & 0xffff;
                if (old != 0) {
                    crc ^= poly;
                }
            }
        }
        return crc & 0x0FFFF;
    }

    /**
     * Recalculate the data crc and check if it matches the crc in the packet.
     *
     * @return true if the data crc in the packet is valid
     */
    public final boolean isDataCrcValid() {
        return calcCrc16CCITT(this.data) == this.dataCrc;
    }

    /**
     * Recalculate the header crc and check if it matches the crc in the packet.
     *
     * @return true if the header crc in the packet is valid
     */
    public final boolean isHeaderCrcValid() {
        final int expected = calcCrc8CCITT(toByteArray(), 0, 4);
        return expected == this.headerCrc;
    }

    public boolean isReportPowerUp() {
        return (data != null) && (data.length == 2) && (data[0] == REPORT_POWER_UP) && (data[1] == IF2_VERSION);
    }

    public boolean isPingRequest() {
        return (data != null) && (data.length == 1) && (data[0] == PING_REQUEST);
    }

    public boolean isRequestIndication() {
        return (data != null) && (data.length >= 6) && (data[0] == REQUEST_INDICATION);
    }

    /**
     * @return Ack power up IF2 packet
     */
    public static IF2Packet createAckPowerUp() {
        return new IF2Packet(
                new byte[]{
                        ACK_POWER_UP,
                        IF2_VERSION
                }
        );
    }

    /**
     * @return Ping response IF2 packet
     */
    public static IF2Packet createPingResponse() {
        return new IF2Packet(
                new byte[]{
                        PING_RESPONSE
                }
        );
    }

    public static IF2Packet createConnectRequest(int cid) {
        return new IF2Packet(
                new byte[]{
                        CONNECT_INDICATION,
                        1, // CID length
                        (byte) (cid & 0x0FF)
                }
        );
    }

    public static IF2Packet createDisconnectRequest(int cid) {
        return new IF2Packet(
                new byte[]{
                        DISCONNECT_INDICATION,
                        1, // CID length
                        (byte) (cid & 0x0FF)
                }
        );
    }

    public static IF2Packet createDataIndication(int cid, int clientAddress, int serverAddress, byte[] requestBuffer) {
        final byte[] dataIndication = {
                DATA_INDICATION,
                1, // CID length
                (byte) (cid & 0x0FF),
                (byte) (clientAddress & 0x0FF),
                (byte) (serverAddress & 0x0FF),
                1 // Confirmed message
        };
        return new IF2Packet(DLMSUtils.concatByteArrays(dataIndication, requestBuffer));
    }

    public void initData() {
        this.data = new byte[this.length];
        this.dataIndex = 0;
    }

    public boolean fillNextDataByte(int value) {
        if (dataIndex == -1) {
            initData();
        }
        if (dataIndex < data.length) {
            data[dataIndex++] = (byte) (value & 0x0FF);
        }
        return dataIndex == data.length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("IF2Packet={");
        sb.append(DLMSUtils.getHexStringFromBytes(toByteArray(), " "));
        sb.append("}");
        return sb.toString();
    }
}
