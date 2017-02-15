/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version  1.0
 * @author Koenraad Vanderschaeve
 * <P>
 *         <B>Description :</B><BR>
 *         Class that implements the HDLC datalink layer protocol.
 *         <B>Hardcoded :</B><BR>
 *         Destination and source address <BR>
 *         RX windowsize = 1 <BR>
 *         TX windowsize = 1 <BR>
 *         RX buffersize = 248 bytes <BR>
 *         TX buffersize = 248 bytes <BR>
 *         <B>Changes :</B><BR>
 *         KV 15052002 Initial version.<BR>
 *         KV 08102003 bugfix in getReasons (arrayoutofbounds)
 *         KV 14012004 bugfix 4 byte addressing
 *         KV 14012004 changed 4 byte addressing
 *         KV 17112004 made more robust...
 *         GN 14012008 made SNRMType for interoperability
 */


/*
 *  Siemens ZMD meter upperaddress (bij ons lower) 1=management unit, 17=communication unit
 *  loweraddress (bij ons upper) = physical device address! if not given, meter react always
 *
 *
 */
public class HDLCConnection extends Connection implements DLMSConnection {

    private static final Logger logger = Logger.getLogger(HDLCConnection.class.getName());

    private static final byte DEBUG = 0;

    private int[] crc_tab = {
            0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
            0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
            0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
            0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
            0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
            0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
            0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
            0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
            0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
            0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
            0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
            0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
            0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
            0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
            0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
            0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
            0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
            0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
            0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
            0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
            0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
            0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
            0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
            0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
            0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
            0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
            0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
            0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
            0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
            0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
            0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
            0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };

    public byte[] sendRawBytes(byte[] data) throws IOException {
        return new byte[0];
    }

    private boolean boolAbort = false;
    private long iProtocolTimeout;
    private int SNRMType = 0;

    // HDLC parameters
    // Only windowsize of 1 is accepted
    private long lTXWindowSize;
    private long lRXWindowSize;
    private short sMaxRXIFSize;
    private short sMaxTXIFSize;

    // Raw frames without HDLC_FLAG!
    private static final int MAX_BUFFER_SIZE = 512;
    private byte[] txFrame = new byte[MAX_BUFFER_SIZE];
    private byte[] rxFrame = new byte[MAX_BUFFER_SIZE];




    // HDLC specific
    // Sequence numbering
    private byte NR;
    private byte NS;
    private boolean boolHDLCConnected;
    private HDLCFrame lastHDLCFrame;

    // Return (bit) values for the waitForHDLCFrameStateMachine()
    private static final byte HDLC_RX_OK = 0x00;
    private static final byte HDLC_BADCRC = 0x01;
    private static final byte HDLC_TIMEOUT = 0x02;
    private static final byte HDLC_ABORT = 0x04;
    private static final byte HDLC_BADFRAME = 0x08;



    private static final String[] reasons = {"BADCRC", "TIMEOUT", "ABORT", "BADFRAME"};

    // State values for the waitForHDLCFrameStateMachine()
    private static final byte WAIT_FOR_START_FLAG = 0x00;
    private static final byte WAIT_FOR_FRAME_FORMAT = 0x01;
    private static final byte WAIT_FOR_DATA = 0x02;
    private static final byte WAIT_FOR_END_FLAG = 0x03;

    // Control byte
    private static final byte SNRM = (byte) 0x83;
    private static final byte DISC = 0x43;
    private static final byte UA = 0x63;
    private static final byte DM = 0x0F;
    private static final byte FRMR = (byte) 0x87;
    private static final byte UI = 0x03;
    private static final byte I_MASK = 0x01;
    private static final byte I = 0x00;
    private static final byte RR_MASK = 0x0F;
    private static final byte RR = 0x01;
    private static final byte RNR_MASK = 0x0F;
    private static final byte RNR = 0x05;

    // HDLC constants
    private static final byte HDLC_FLAG = 0x7E;
    private static final short HDLC_FRAME_TYPE3 = (short) 0xA000; // Requires the use of the S bit segmentation,
    // reducing the length to 11 bits max.
    private static final short HDLC_FRAME_S_BIT = 0x0800;
    private static final byte HDLC_FRAME_CONTROL_PF_BIT = 0x10;

    // see page 82/192 of Cosem Green book
    private int iMaxRetries;
    private int iClientMacAddress;
    private int iServerUpperMacAddress;
    private int iServerLowerMacAddress;

    /*
    // HDLC addresses, hardcoded
    private long HDLC_DESTINATION=0x03;
    private static final byte CLIENT_MAC_NO_SECURITY=0x21;         // 16 Siemens ZMD level 0
    private static final byte CLIENT_MAC_LOWLEVEL_SECURITY_1=0x41; // 32 Siemens ZMD level 1
    private static final byte CLIENT_MAC_LOWLEVEL_SECURITY_2=(byte)0xC9; // 100 my low level security test address with CTT in KEMA
    private static final byte CLIENT_MAC_LOWLEVEL_SECURITY_3=(byte)0x25; // MV90 use this level 6, remote data collection (low security level)
    private static final byte CLIENT_MAC_LOWLEVEL_SECURITY_4=(byte)0x05; //0x5; // SL7000
    */

    private static final byte CLIENT_NO_SECURITY = 0;
    private static final byte CLIENT_LOWLEVEL_SECURITY = 1;

    //private byte bClientMACAddress;

    private static final byte CRC_SIZE = 2;

    private byte HEADER_SIZE = 7;


    private byte FRAME_FORMAT_MSB = 0;
    private byte FRAME_FORMAT_LSB = 1;
    private byte FRAME_DESTINATION = 2;
    private byte FRAME_SOURCE = 3;
    private byte FRAME_HEADER_CRC_MSB = 5;
    private byte FRAME_HEADER_CRC_LSB = 6;
    private byte FRAME_CONTROL = 4;
    private byte FRAME_INFORMATION_FIELD = 7;

    // HDLC addressing modes
    public static final byte CLIENT_ADDRESSING_DEFAULT = -1;
    public static final byte CLIENT_ADDRESSING_1BYTE = 1;
    public static final byte CLIENT_ADDRESSING_2BYTE = 2;
    public static final byte CLIENT_ADDRESSING_4BYTE = 4;
    private byte bAddressingMode;

    private byte ISIZE = (byte) 0x80;

    private long lForceDelay;

    private int currentTryCount;

    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler;
    /**
     * Class constructor.
     * @param inputStream  InputStream for the active connection
     * @param outputStream OutputStream for the active connection
     * @param iTimeout     Time in ms. for a request to wait for a response before returning an timeout error.
     * @param lForceDelay  Force delay (in ms) before each frame send (e.g. SL7000 meter needs at lease 100 ms.).
     * @exception DLMSConnectionException
     */
    public HDLCConnection(InputStream inputStream,
                          OutputStream outputStream,
                          int iTimeout,
                          long lForceDelay,
                          int iMaxRetries,
                          int iClientMacAddress,
                          int iServerLowerMacAddress,
                          int iServerUpperMacAddress,
                          int addressingMode) throws DLMSConnectionException, ConnectionException {
        super(inputStream, outputStream);
        this.iMaxRetries = iMaxRetries;
        boolAbort = false;
        iProtocolTimeout = iTimeout;
        NR = NS = 0;
        boolHDLCConnected = false;
        lastHDLCFrame = null;
        //bClientMACAddress=0;
        this.lForceDelay = lForceDelay;

        this.iClientMacAddress = iClientMacAddress;
        this.iServerUpperMacAddress = iServerUpperMacAddress;
        this.iServerLowerMacAddress = iServerLowerMacAddress;
        getAddressingMode(addressingMode);
        setProtocolParams();
        this.invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();

    } // public HDLCConnection(...)

    private void getAddressingMode(int addressingMode) throws DLMSConnectionException {



        if (addressingMode == CLIENT_ADDRESSING_DEFAULT) {
            if ((iServerLowerMacAddress == 0) && (iServerUpperMacAddress <= 0x7F)) {
                bAddressingMode = CLIENT_ADDRESSING_1BYTE;
            } else if (iServerLowerMacAddress != 0) {
                bAddressingMode = CLIENT_ADDRESSING_4BYTE;
            } else {
                throw new DLMSConnectionException("HDLCConnection, getAddressingMode, unknown addressing scheme, invalid server addresses");
            }
        } else {
            this.bAddressingMode = (byte) addressingMode;
        }

        /*
         if ((iServerLowerMacAddress  == 0) && (iServerUpperMacAddress  <= 0x7F)) bAddressingMode = CLIENT_ADDRESSING_1BYTE;
        else if ((iServerLowerMacAddress  == 0) && (iServerUpperMacAddress  > 0x7F)) throw new DLMSConnectionException("HDLCConnection, getAddressingMode, unknown addressing scheme, invalid server addresses");
        else if ((iServerLowerMacAddress  > 0) && (iServerUpperMacAddress  != 0)) bAddressingMode = CLIENT_ADDRESSING_4BYTE;
        else throw new DLMSConnectionException("HDLCConnection, getAddressingMode, unknown addressing scheme, invalid server addresses");
         */

        if (iClientMacAddress > 0x7F) {
            throw new DLMSConnectionException("HDLCConnection, getAddressingMode, unknown addressing scheme, invalid client addres");
        }
        if (iClientMacAddress == 0) {
            throw new DLMSConnectionException("HDLCConnection, getAddressingMode, unknown addressing scheme, client addres = 0");
        }
        if (iServerUpperMacAddress == 0) {
            throw new DLMSConnectionException("HDLCConnection, getAddressingMode, unknown addressing scheme, server upper addres = 0");
        }
    }

    private void setProtocolParams() {
        if (bAddressingMode == CLIENT_ADDRESSING_1BYTE) {
            FRAME_FORMAT_MSB = 0;
            FRAME_FORMAT_LSB = 1;
            FRAME_DESTINATION = 2;
            FRAME_SOURCE = 3;
            FRAME_CONTROL = 4;
            FRAME_HEADER_CRC_MSB = 5;
            FRAME_HEADER_CRC_LSB = 6;
            FRAME_INFORMATION_FIELD = 7;
            HEADER_SIZE = 7;
            ISIZE = (byte) 0xF8;
        } else if (bAddressingMode == CLIENT_ADDRESSING_2BYTE) {
            FRAME_FORMAT_MSB = 0;
            FRAME_FORMAT_LSB = 1;
            FRAME_DESTINATION = 2; // 2 3
            FRAME_SOURCE = 4;
            FRAME_CONTROL = 5;
            FRAME_HEADER_CRC_MSB = 6;
            FRAME_HEADER_CRC_LSB = 7;
            FRAME_INFORMATION_FIELD = 8;
            HEADER_SIZE = 8;
            ISIZE = (byte) 0x80; // Otherwise, error (DM) from SL7000
        } else if (bAddressingMode == CLIENT_ADDRESSING_4BYTE) {
            FRAME_FORMAT_MSB = 0;
            FRAME_FORMAT_LSB = 1;
            FRAME_DESTINATION = 2; // 2 3 4 5
            FRAME_SOURCE = 6;
            FRAME_CONTROL = 7;
            FRAME_HEADER_CRC_MSB = 8;
            FRAME_HEADER_CRC_LSB = 9;
            FRAME_INFORMATION_FIELD = 10;
            HEADER_SIZE = 10;
            ISIZE = (byte) 0x80; // Otherwise, error (DM) from SL7000
        }
    } // private void setProtocolParams()

    private byte[] macSNRMFrame = {(byte) SNRM | (byte) HDLC_FRAME_CONTROL_PF_BIT,
            0x00, 0x00, // Header CRC
            (byte) 0x81, (byte) 0x80, 0x12,
            // Changing the MAX information field size does not seems to
            // change the secondary device behaviour.
            0x05, 0x01, ISIZE,
            0x06, 0x01, ISIZE,
            0x07, 0x04, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x04, 0x00, 0x00, 0x00, 0x01,
            0x00, 0x00}; // Frame CRC

//    private byte[] flexSNRMFrame={(byte) 0x93, (byte) 0x00, (byte) 0x00};

    private byte[] flexSNRMFrame = {(byte) SNRM | (byte) HDLC_FRAME_CONTROL_PF_BIT,
            0x00, 0x00, // Header CRC
            (byte) 0x81, (byte) 0x80, 0x12,
            // Changing the MAX information field size does not seems to
            // change the secondary device behaviour.
            0x05, 0x01, (byte) 0xF8,
            0x06, 0x01, (byte) 0xF8,
            0x07, 0x04, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x04, 0x00, 0x00, 0x00, 0x01,
            0x00, 0x00}; // Frame CRC

    private byte[] buildFrame(byte[] macFrame) {
        short sSize = (short) (macFrame.length + HEADER_SIZE - 3);
        short sFrameFormat = (short) (sSize | HDLC_FRAME_TYPE3);
        byte[] frame = new byte[sSize];

        frame[FRAME_FORMAT_MSB] = (byte) ((sFrameFormat >> 8) & 0x00FF);
        frame[FRAME_FORMAT_LSB] = (byte) (sFrameFormat & 0x00FF);
        /*
       if (bAddressingMode == CLIENT_ADDRESSING_4BYTE) {
         frame[FRAME_DESTINATION+3] = (byte)HDLC_DESTINATION;
         frame[FRAME_DESTINATION+2] = (byte)(HDLC_DESTINATION>>8);
         frame[FRAME_DESTINATION+1] = (byte)(HDLC_DESTINATION>>16);
         frame[FRAME_DESTINATION] = (byte)(HDLC_DESTINATION>>24);
       }
       else if (bAddressingMode == CLIENT_ADDRESSING_1BYTE) {
         frame[FRAME_DESTINATION] = (byte)HDLC_DESTINATION;
       }
       frame[FRAME_SOURCE] = (byte)iClientMACAddress;
        */

        frame = buildAddressingScheme(frame);

        for (int i = 0; i < macFrame.length; i++) {
            frame[FRAME_SOURCE + 1 + i] = macFrame[i];
        }

        return frame;

    } // private byte[] buildFrame(byte[] macFrame)

    /**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     * @exception DLMSConnectionException
     */
    public void connectMAC() throws IOException, DLMSConnectionException {

        byte[] SNRMFrame;
        if (boolHDLCConnected == false) {
            // KV 18092003
            try {
                if (hhuSignOn != null) {
                    hhuSignOn.signOn("", meterId);
                }
            } catch (ConnectionException e) {
                throw new DLMSConnectionException("connectMAC, HHU signOn failed, ConnectionException, " + e.getMessage());
            }

            if (SNRMType == 1) {
                SNRMFrame = flexSNRMFrame;
            } else {
                SNRMFrame = macSNRMFrame;
            }

            byte[] macFrame = buildFrame(SNRMFrame);

            doConnectMAC(macFrame);
        } // if (boolHDLCConnected==false)
    } // public void connectMAC() throws NestedIOException,DLMSConnectionException

    public void setSNRMType(int type) {
        SNRMType = type;
    }

    public String getReason(int reason) {
        StringBuffer strbuff = new StringBuffer();
        for (int i = 0; i < reasons.length; i++) {
            if ((reason & (0x1 << i)) != 0) {
                strbuff.append(reasons[i] + " ");
            }
        }
        return strbuff.toString();
    }

    private void doConnectMAC(byte[] macFrame) throws NestedIOException, DLMSConnectionException {
        HDLCFrame hdlcFrame;
        byte bResult;
        int i;
        bResult = 0;
        // Requested HDLC parameters
        lTXWindowSize = 1;
        lRXWindowSize = 1;
        sMaxRXIFSize = 0x00F8;
        sMaxTXIFSize = 0x00F8;
        /// The mac frame is hardcoded.

        calcCRC(macFrame);
        for (i = 0; i < iMaxRetries; i++) {
            sendFrame(macFrame);
            bResult = waitForHDLCFrameStateMachine(iProtocolTimeout, rxFrame);
            if (bResult == HDLC_RX_OK) {
                hdlcFrame = decodeFrame(rxFrame);
                if (hdlcFrame.bControl != UA) {
                    throw new DLMSConnectionException("ERROR connecting MAC, should receive an UA frame.");
                }
                getHDLCParameters(hdlcFrame.InformationBuffer);
                boolHDLCConnected = true;
                break;
            } // if (waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame) == HDLC_RX_OK)
        } // for (i=0;i<iMaxRetries;i++)
        if (i == iMaxRetries) {
            // KV_19012004
            throw new DLMSConnectionException("ERROR connecting MAC, reason " + getReason(bResult), (short) ((short) bResult & 0x00FF));
        }
    } // public void doConnectMAC() throws DLMSConnectionException

    private byte[] macDISCFrame = {(byte) DISC | (byte) HDLC_FRAME_CONTROL_PF_BIT, 0x00, 0x00}; // Header CRC

    /**
     * Method that requests a MAC disconnect for the HDLC layer.
     * @exception DLMSConnectionException
     */
    public void disconnectMAC() throws IOException, DLMSConnectionException {
        HDLCFrame hdlcFrame;
        byte bResult;
        int i;

        bResult = 0;

        if (boolHDLCConnected == true) {
            byte[] macFrame = buildFrame(macDISCFrame);
            calcCRC(macFrame);

            for (i = 0; i < iMaxRetries; i++) {
                sendFrame(macFrame);
                bResult = waitForHDLCFrameStateMachine(iProtocolTimeout, rxFrame);
                if (bResult == HDLC_RX_OK) {
                    hdlcFrame = decodeFrame(rxFrame);
                    if (hdlcFrame.bControl != UA) {
                        throw new DLMSConnectionException("ERROR disConnecting MAC, should receive an UA frame.");
                    }

                    // Should not be done here KV 05062002
                    //getHDLCParameters(hdlcFrame.InformationBuffer);

                    boolHDLCConnected = false;
                    break;
                } // if (waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame) == HDLC_RX_OK)

            } // for (i=0;i<iMaxRetries;i++)
            if (i == iMaxRetries) {
                // KV_19012004
                throw new DLMSConnectionException("ERROR disConnecting MAC, reason " + getReason(bResult), (short) ((short) bResult & 0x00FF));
            }
        } // if (boolHDLCConnected==true)
    } // public void disconnectMAC() throws DLMSConnectionException

    private void getHDLCParameters(byte[] byteReceiveBuffer) throws DLMSConnectionException {
        long negolTXWindowSize = 1;
        long negolRXWindowSize = 1;
        short negosMaxRXIFSize = 128;
        short negosMaxTXIFSize = 128;

        final int OFFSET_TO_FORMAT_ID = 0;
        final int OFFSET_TO_GROUP_ID = 1;
        final int OFFSET_TO_GROUP_LENGTH = 2;
        final int OFFSET_TO_DATA = 3;

        final int RX_FRAME_SIZE = 5;
        final int TX_FRAME_SIZE = 6;
        final int RX_WINDOW_SIZE = 7;
        final int TX_WINDOW_SIZE = 8;

        if ((byteReceiveBuffer[OFFSET_TO_FORMAT_ID] != (byte) 0x81) ||
                (byteReceiveBuffer[OFFSET_TO_GROUP_ID] != (byte) 0x80)) {
            throw new DLMSConnectionException("HDLCConnection, getHDLCParameters, format (0x" + ((int) byteReceiveBuffer[OFFSET_TO_FORMAT_ID] & 0xFF) + ") and/or group identifier (0x" + ((int) byteReceiveBuffer[OFFSET_TO_GROUP_ID] & 0xFF) + ") wrong!");
        }

        //int length = (int)byteReceiveBuffer[OFFSET_TO_GROUP_LENGTH]&0xFF;
        int valLen;
        int data;
        int i = 0;
        while (i < (byteReceiveBuffer.length - OFFSET_TO_DATA)) {
            data = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
            try {
                switch (data) {
                    case RX_FRAME_SIZE:
                        i++;
                        valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                        i++;
                        negosMaxRXIFSize = (short) (ProtocolUtils.getLongFromBytes(byteReceiveBuffer, OFFSET_TO_DATA + i, valLen).intValue());
                        break;

                    case TX_FRAME_SIZE:
                        i++;
                        valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                        i++;
                        negosMaxTXIFSize = (short) (ProtocolUtils.getLongFromBytes(byteReceiveBuffer, OFFSET_TO_DATA + i, valLen).intValue());
                        break;

                    case RX_WINDOW_SIZE:
                        i++;
                        valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                        i++;
                        negolRXWindowSize = (long) (ProtocolUtils.getLongFromBytes(byteReceiveBuffer, OFFSET_TO_DATA + i, valLen).intValue());
                        break;

                    case TX_WINDOW_SIZE:
                        i++;
                        valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                        i++;
                        negolTXWindowSize = (long) (ProtocolUtils.getLongFromBytes(byteReceiveBuffer, OFFSET_TO_DATA + i, valLen).intValue());
                        break;

                    default:
                        i++;
                        break;

                } // switch(data)
            } catch (IOException e) {
                throw new DLMSConnectionException("HDLCConnection, getHDLCParameters, IOException, " + e.getMessage());
            }
        } // while(i<length)

        if (DEBUG >= 1) {
            System.out.println(negosMaxRXIFSize + " " + negosMaxTXIFSize + " " + negolRXWindowSize + " " + negolTXWindowSize);
        }

        // Use smalles parameters negotiated.
        if (negolRXWindowSize < lRXWindowSize) {
            lRXWindowSize = negolRXWindowSize;
        }
        if (negolTXWindowSize < lTXWindowSize) {
            lTXWindowSize = negolTXWindowSize;
        }
        if (negosMaxRXIFSize < sMaxRXIFSize) {
            sMaxRXIFSize = negosMaxRXIFSize;
        }
        if (negosMaxTXIFSize < sMaxTXIFSize) {
            sMaxTXIFSize = negosMaxTXIFSize;
        }
    }

    private HDLCFrame decodeFrame(byte[] byteReceiveBuffer) throws DLMSConnectionException {
        return (new HDLCFrame(byteReceiveBuffer));
    } // private decodeFrame(byte[] byteReceiveBuffer)


    private byte waitForHDLCFrameStateMachine(long iTimeout, byte[] byteReceiveBuffer) throws DLMSConnectionException {
        long lMSTimeout;
        int inewKar;
        int[] CRC; //=new int[2];
        int[] calcCRC; //=new int[2];

        short sLength;
        short sRXCount = 0;
        byte bCurrentState; // Statevariable

        lMSTimeout = System.currentTimeMillis() + iTimeout;
        sRXCount = 0;
        sLength = 0;
        bCurrentState = WAIT_FOR_START_FLAG;

        copyEchoBuffer();

        try {
            while (boolAbort == false) {
                if ((inewKar = readIn()) != -1) {
                    if (DEBUG >= 1) {
                        ProtocolUtils.outputHex(inewKar);
                    }
                    if (sRXCount >= MAX_BUFFER_SIZE) {
                        return HDLC_BADFRAME;
                    }

                    switch (bCurrentState) {
                        case WAIT_FOR_START_FLAG: {
                            switch ((byte) inewKar) {
                                case HDLC_FLAG: {
                                    sRXCount = 0;
                                    byteReceiveBuffer[0] = (byte) inewKar;
                                    bCurrentState = WAIT_FOR_FRAME_FORMAT;
                                }
                                break;

                                default:
                                    break;
                            } // switch((byte)inewKar)

                        }
                        break; // case WAIT_FOR_START_FLAG

                        case WAIT_FOR_FRAME_FORMAT: {
                            // KV_DEBUG skip flag when triggered on previous frame!
                            if ((sRXCount == 0) && ((byte) inewKar == HDLC_FLAG)) {
                                break;
                            }

                            byteReceiveBuffer[sRXCount++] = (byte) inewKar;
                            if (sRXCount >= 2) {
                                sLength = (short) ((((short) byteReceiveBuffer[FRAME_FORMAT_MSB] & 0x0007) << 8) |
                                        ((short) byteReceiveBuffer[FRAME_FORMAT_LSB] & 0x00FF));
                                bCurrentState = WAIT_FOR_DATA;
                            }
                        }
                        break; // WAIT_FOR_FRAME_FORMAT

                        case WAIT_FOR_DATA: {
                            byteReceiveBuffer[sRXCount++] = (byte) inewKar;
                            if (sRXCount >= sLength) {
                                bCurrentState = WAIT_FOR_END_FLAG;
                            }
                        }
                        break; // WAIT_FOR_DATA

                        case WAIT_FOR_END_FLAG: {
                            switch ((byte) inewKar) {
                                case HDLC_FLAG: {
                                    // Check CRC
                                    CRC = getCRC(byteReceiveBuffer);
                                    calcCRC = calcCRC(byteReceiveBuffer);
                                    if ((CRC[0] == calcCRC[0]) &&
                                            (CRC[1] == calcCRC[1])) {
                                        return HDLC_RX_OK;
                                    } else {
                                        return HDLC_BADCRC;
                                    }

                                } // case HDLC_FLAG:

                                default:
                                    return HDLC_BADFRAME;

                            } // switch((byte)inewKar)

                        } // case WAIT_FOR_END_FLAG:

                    } // switch (bCurrentState)

                } // if ((iNewKar = readIn()) != -1)

                if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                    return HDLC_TIMEOUT;
                }

            } // while(boolAbort==false)

            return HDLC_ABORT;
        } catch (IOException e) {
            throw new DLMSConnectionException(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DLMSConnectionException(e.getMessage());
        }
    } // private byte waitForHDLCFrameStateMachine()

    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        byte[] receivedArray;
        int tooManyRetries = 0;
        int bufferLength;
        boolean firstRead = true;
        // this.currentTryCount contains the current try number - we should not start again from 0, but continue from current try number

        // KV 19092003 bugfix, after retry, data was truncated cause receiveBuffer was constructed in receiveInformationField()
        ReceiveBuffer receiveBuffer = new ReceiveBuffer();
        bufferLength = 0;
        while (true) {
            try {
                if (firstRead) {
                    firstRead = false;
                    sendInformationField(retryRequest, false);
                } else {
                    sendInformationField(retryRequest);
                }
                receivedArray = receiveInformationField(receiveBuffer); // KV 19092003 bugfix, after retry, data was truncated cause receiveBuffer was constructed in receiveInformationField()
                // If successfull send , adjust S
                if (NS++ > 6) {
                    NS = 0; // Sequence counter
                }

                if (receivedArray.length != 0) {
                    return receivedArray;
                } else {
                    // if retryCount grows over the configuration's max retries parameter, throw exception
                    if (currentTryCount++ >= (iMaxRetries - 1)) {
                        throw new DLMSConnectionException("readResponseWithRetries, max retries!");
                    }
                }
            } catch (DLMSConnectionException e) {

                // Test if length of receivebuffer has changed since previous error. If so,
                // reset retrycount and save length for!
                if (bufferLength < receiveBuffer.bytesReceived()) {
                    currentTryCount = 0;
                    bufferLength = receiveBuffer.bytesReceived();
                }
                // if retryCount grows over the configuration's max retries parameter, throw exception
                if (currentTryCount++ >= (iMaxRetries - 1)) {
                    throw new ConnectionException("readResponseWithRetries, max retries exceeded, " + e.getMessage() + ", reason=" + getReason(e.getReason()), e.getReason());
                }
                // for safety purposes. I line is so bad that tooManyRetries exceeds 500 retries, throw exception
                if (tooManyRetries++ >= (TOO_MANY_RETRIES - 1)) {
                    throw new ConnectionException("readResponseWithRetries, too many retries exceeded, " + e.getMessage() + ", reason=" + getReason(e.getReason()), e.getReason());
                }
                // KV 17112004
                if (NS++ > 6) {
                    NS = 0; // Sequence counter
                }
            } catch (IOException e) {
                throw new ConnectionException("readResponseWithRetries, IOException, " + e.getMessage());
            }
        } // while(true)
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        return this.readResponseWithRetries(retryRequest);
    }

    private void sendFrame(byte[] byteBuffer) throws NestedIOException, DLMSConnectionException {
        int iLength;
        byte[] flag = new byte[1];
        flag[0] = HDLC_FLAG;
        DLMSUtils.delay(lForceDelay);
        iLength = (((int) byteBuffer[FRAME_FORMAT_MSB] & 0x00000007) << 8) |
                ((int) byteBuffer[FRAME_FORMAT_LSB] & 0x000000FF);
        try {
//           sendOut(flag);
//           sendOut(byteBuffer,0,iLength);
//           sendOut(flag);


            byte[] data = new byte[iLength + 2];
            data[0] = HDLC_FLAG;
            System.arraycopy(byteBuffer, 0, data, 1, iLength);
            data[data.length - 1] = HDLC_FLAG;

            sendOut(data);

            if (DEBUG == 1) {
                int i;
                for (i = 0; i < iLength; i++) {
                    ProtocolUtils.outputHex(((int) byteBuffer[i]) & 0x000000FF);
                }
                System.out.println();
            }
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    } // private void sendFrame(byte[] byteBuffer) throws DLMSConnectionException

    /**
     * Method that sends an information data field and receives an information field.
     * @param Data with the information field.
     * @return Response data with the information field.
     * @exception DLMSConnectionException
     */
    private static final int TOO_MANY_RETRIES = 500;

    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
        byte[] receivedArray;
        int tooManyRetries = 0;
        int bufferLength;
        // KV 19092003 bugfix, after retry, data was truncated cause receiveBuffer was constructed in receiveInformationField()
        ReceiveBuffer receiveBuffer = new ReceiveBuffer();
        resetCurrentTryCount();
        bufferLength = 0;
        while (true) {
            try {
                sendInformationField(byteRequestBuffer);
                receivedArray = receiveInformationField(receiveBuffer); // KV 19092003 bugfix, after retry, data was truncated cause receiveBuffer was constructed in receiveInformationField()
                // If successfull send , adjust S
                if (NS++ > 6) {
                    NS = 0; // Sequence counter
                }

                if (receivedArray.length != 0) {
                    return receivedArray;
                } else {
                    // if retryCount grows over the configuration's max retries parameter, throw exception
                    if (currentTryCount++ >= (iMaxRetries - 1)) {
                        throw new DLMSConnectionException("sendRequest, max retries!");
                    }
                }
            } catch (DLMSConnectionException e) {

                // Test if length of receivebuffer has changed since previous error. If so,
                // reset retrycount and save length for!
                if (bufferLength < receiveBuffer.bytesReceived()) {
                    currentTryCount = 0;
                    bufferLength = receiveBuffer.bytesReceived();
                }
                // if retryCount grows over the configuration's max retries parameter, throw exception
                if (currentTryCount++ >= (iMaxRetries - 1)) {
                    throw new ConnectionException("sendRequest, max retries exceeded, " + e.getMessage() + ", reason=" + getReason(e.getReason()), e.getReason());
                }
                // for safety purposes. I line is so bad that tooManyRetries exceeds 500 retries, throw exception
                if (tooManyRetries++ >= (TOO_MANY_RETRIES - 1)) {
                    throw new ConnectionException("sendRequest, too many retries exceeded, " + e.getMessage() + ", reason=" + getReason(e.getReason()), e.getReason());
                }
                // KV 17112004
                if (NS++ > 6) {
                    NS = 0; // Sequence counter
                }
            } catch (IOException e) {
                throw new ConnectionException("sendRequest, IOException, " + e.getMessage());
            }
        } // while(true)

    } // public byte[] sendRequest(byte[] byteBuffer) throws IOException

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
    }

    public void setTimeout(long timeout) {
        this.iProtocolTimeout = timeout;
    }

    public long getTimeout() {
        return iProtocolTimeout;
    }

    public void setRetries(int retries) {
        this.iMaxRetries = retries;
    }

    public void sendUnconfirmedRequest(final byte[] request) throws IOException {
        int tooManyRetries = 0;
        resetCurrentTryCount();

        while (true) {

            try {
                sendInformationField(request);

                // Increase and reset sequence counter if required
                if (NS++ > 6) {
                    NS = 0; // Sequence counter
                }

                return;
            } catch (DLMSConnectionException e) {

                // if retryCount grows over the configuration's max retries parameter, throw exception
                if (currentTryCount++ >= (iMaxRetries - 1)) {
                    throw new ConnectionException("sendRequest, max retries exceeded, " + e.getMessage() + ", reason=" + getReason(e.getReason()), e.getReason());
                }
                // for safety purposes. I line is so bad that tooManyRetries exceeds 500 retries, throw exception
                if (tooManyRetries++ >= (TOO_MANY_RETRIES - 1)) {
                    throw new ConnectionException("sendRequest, too many retries exceeded, " + e.getMessage() + ", reason=" + getReason(e.getReason()), e.getReason());
                }

            } catch (IOException e) {
                throw new ConnectionException("sendRequest, IOException, " + e.getMessage());
            }

            logger.log(Level.WARNING, "Sleeping for [" + iProtocolTimeout + " ms] until next try ...");
            DLMSUtils.delay(iProtocolTimeout);

            // Increase and reset sequence counter if required
            if (NS++ > 6) {
                NS = 0; // Sequence counter
            }

        }
    }

    private void sendInformationField(byte[] byteBuffer) throws NestedIOException, DLMSConnectionException {
        sendInformationField(byteBuffer, true);

            }

    /**
     * Build a proper information frame and optionally send it out to the device.
     * Send information field segmented if size > sMaxTXIFSize.
     * Wait for RR frame each I frame send (windowsize=1, hardcoded).
     *
     * @param byteBuffer   the request
     * @param sendOutFrame indicates if we should send the frame
     * @throws IOException
     * @throws DLMSConnectionException
*/
    private void sendInformationField(byte[] byteBuffer,boolean sendOutFrame) throws NestedIOException, DLMSConnectionException {
        long lLength;
        short sFrameFormat;
        int i;
        int iIndex;
        byte bResult;
        HDLCFrame hdlcFrame;

        if (!boolHDLCConnected) {
            throw new DLMSConnectionException("HDLC connection not established!");
        }

        lLength = byteBuffer.length; // length of information byteBuffer
        iIndex = 0; // index in information byteBuffer

        while (lLength > 0) {
            if (lLength > sMaxTXIFSize) {
                sFrameFormat = (short) ((sMaxTXIFSize + HEADER_SIZE + CRC_SIZE) | HDLC_FRAME_TYPE3 | HDLC_FRAME_S_BIT);
                for (i = 0; i < sMaxTXIFSize; i++) {
                    txFrame[FRAME_INFORMATION_FIELD + i] = byteBuffer[iIndex++];
                }
                lLength -= sMaxTXIFSize;
            } else {
                sFrameFormat = (short) ((lLength + HEADER_SIZE + CRC_SIZE) | HDLC_FRAME_TYPE3);
                for (i = 0; i < lLength; i++) {
                    txFrame[FRAME_INFORMATION_FIELD + i] = byteBuffer[iIndex++];
                }
                lLength = 0;
            }

            txFrame[FRAME_FORMAT_MSB] = (byte) ((sFrameFormat >> 8) & 0x00FF);
            txFrame[FRAME_FORMAT_LSB] = (byte) (sFrameFormat & 0x00FF);
            /*
            if (bAddressingMode == CLIENT_ADDRESSING_4BYTE) {
                txFrame[FRAME_DESTINATION+3] = (byte)HDLC_DESTINATION;
                txFrame[FRAME_DESTINATION+2] = (byte)(HDLC_DESTINATION>>8);
                txFrame[FRAME_DESTINATION+1] = (byte)(HDLC_DESTINATION>>16);
                txFrame[FRAME_DESTINATION] = (byte)(HDLC_DESTINATION>>24);
            }
            else if (bAddressingMode == CLIENT_ADDRESSING_1BYTE) {
                txFrame[FRAME_DESTINATION] = (byte)HDLC_DESTINATION;
            }
            txFrame[FRAME_SOURCE] = bClientMACAddress;
*/
            txFrame = buildAddressingScheme(txFrame);


            if (sendOutFrame) { // Do not modify the NR field when just building up the txFrame (and do not send it out)
            // If we received already an I / RR / RNR frame, use the
            // last receive N(S) field to construct N(R).
            if (lastHDLCFrame != null) {
                NR = (byte) (lastHDLCFrame.NS + 1);
                if (NR > 7) {
                    NR = 0;
                }
            }
            }

            txFrame[FRAME_CONTROL] = (byte) (I | (NR << 5) | (NS << 1) | HDLC_FRAME_CONTROL_PF_BIT);
            calcCRC(txFrame);
            if (sendOutFrame) {
            sendFrame(txFrame);

            if ((sFrameFormat & HDLC_FRAME_S_BIT) != 0) {
                do {
                    bResult = waitForHDLCFrameStateMachine(iProtocolTimeout, rxFrame);
                    if (bResult == HDLC_RX_OK) {
                        hdlcFrame = decodeFrame(rxFrame);
                        if (hdlcFrame.bControl != RR) {
                            throw new DLMSConnectionException("ERROR sending data, should receive an RR frame.");
                        }


                        // Should not be done here KV 05062002
                        //getHDLCParameters(hdlcFrame.InformationBuffer);

                    } // if (waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame) == HDLC_RX_OK)
                    else {

                        throw new DLMSConnectionException("ERROR sending data, reason " + getReason(bResult), (short) ((short) bResult & 0x00FF));
                    }

                } while (!hdlcFrame.boolControlPFBit);

            } // if (sFrameFormat & HDLC_FRAME_S_BIT)
            }
        } // while(lLength>0)
    }

    private byte[] buildAddressingScheme(byte[] buffer) {

        if (bAddressingMode == CLIENT_ADDRESSING_4BYTE) {

            // KV 19012004 zo zou het moeten zijn...
            buffer[FRAME_DESTINATION + 3] = (byte) (((iServerLowerMacAddress << 1) | 0x0001) & 0x00FF);
            buffer[FRAME_DESTINATION + 2] = (byte) ((iServerLowerMacAddress >> 6) & 0x00FE);
            buffer[FRAME_DESTINATION + 1] = (byte) ((iServerUpperMacAddress << 1) & 0x00FF);
            buffer[FRAME_DESTINATION] = (byte) ((iServerUpperMacAddress >> 6) & 0x00FE);

            /*
            // KV 14012004 changed
           buffer[FRAME_DESTINATION+3] = (byte)(((iServerUpperMacAddress<<1)|0x0001)&0x00FF);
           buffer[FRAME_DESTINATION+2] = (byte)((iServerUpperMacAddress>>6)&0x00FE);
           buffer[FRAME_DESTINATION+1] = (byte)((iServerLowerMacAddress<<1)&0x00FF);
           buffer[FRAME_DESTINATION] = (byte)((iServerLowerMacAddress>>6)&0x00FE);
            */
        } else if (bAddressingMode == CLIENT_ADDRESSING_2BYTE) {

            // KV 19012004 zo zou het moeten zijn...
            buffer[FRAME_DESTINATION + 1] = (byte) (((iServerLowerMacAddress << 1) | 0x01) & 0xFF);
            buffer[FRAME_DESTINATION] = (byte) ((iServerUpperMacAddress << 1) & 0xFF);
            /*
           buffer[FRAME_DESTINATION+1] = (byte)(((iServerUpperMacAddress<<1)|0x01)&0xFF);
           buffer[FRAME_DESTINATION] = (byte)((iServerLowerMacAddress<<1)&0xFF);
            */
        } else if (bAddressingMode == CLIENT_ADDRESSING_1BYTE) {
            buffer[FRAME_DESTINATION] = (byte) (((iServerUpperMacAddress << 1) | 0x01) & 0x00FF);
        }
        buffer[FRAME_SOURCE] = (byte) (((iClientMacAddress << 1) | 0x0001) & 0x00FF);

        return buffer;
    }

    private static final byte STATE_WAIT_FOR_I_FRAME = 0;
    private static final byte STATE_WAIT_FOR_RR_FRAME = 1;

    private byte[] receiveInformationField(ReceiveBuffer receiveBuffer) throws DLMSConnectionException, IOException {
        byte bResult;
        HDLCFrame hdlcFrame = null;
        short sFrameFormat;
        byte bCountWindowSize = 0;
        byte bState = STATE_WAIT_FOR_I_FRAME;
        int retryCount = 0;

        if (!boolHDLCConnected) {
            throw new DLMSConnectionException("HDLC connection not established!");
        }
        do {
            bResult = waitForHDLCFrameStateMachine(iProtocolTimeout, rxFrame);
            if (bResult == HDLC_RX_OK) {
                retryCount = 0;
                hdlcFrame = decodeFrame(rxFrame);
                if (hdlcFrame.bControl == I) {
                    lastHDLCFrame = hdlcFrame;

                    // Send RR frame
                    if (NR == hdlcFrame.NS) {
                        if (hdlcFrame.InformationBuffer != null) {
                            receiveBuffer.addArray(hdlcFrame.InformationBuffer);
                        }
                    } else {
                        if (DEBUG >= 1) {
                            System.out.println("KV_DEBUG> NR != hdlcFrame.NS, received frame not added to receivebuffer...");
                        }
                    }

                    NR = (byte) (hdlcFrame.NS + 1);
                    if (NR > 7) {
                        NR = 0;
                    }
                    sFrameFormat = (short) (HEADER_SIZE | HDLC_FRAME_TYPE3);
                    txFrame[FRAME_FORMAT_MSB] = (byte) ((sFrameFormat >> 8) & 0x00FF);
                    txFrame[FRAME_FORMAT_LSB] = (byte) (sFrameFormat & 0x00FF);
                    txFrame = buildAddressingScheme(txFrame);
                    txFrame[FRAME_CONTROL] = (byte) (RR | (NR << 5) | HDLC_FRAME_CONTROL_PF_BIT);

                    calcCRC(txFrame);
                    sendFrame(txFrame);

                    bState = STATE_WAIT_FOR_RR_FRAME;
                } else if (hdlcFrame.bControl == RR) {
                    bState = STATE_WAIT_FOR_I_FRAME;
                } else if (hdlcFrame.bControl == DM) {
                    // Device is in Disconnected Mode, thus logically disconnected from the data link
                    throw new DLMSConnectionException("ERROR receiving data, received an DM frame - device logically disconnected from the data link.");
                } else {
                    bState = STATE_WAIT_FOR_I_FRAME;
                    throw new DLMSConnectionException("ERROR receiving data, should receive an I or RR frame.");
                }

            } // if (waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame) == HDLC_RX_OK)
            // KV 17112004 changed
            else {
                // if initial frame was I frame (hdlsFrame == null) or retries reached max, bubble up to sendRequest method and resend I frame
                // else send txframe again
                if ((hdlcFrame == null) || (retryCount++ >= (iMaxRetries - 1))) {
                    throw new DLMSConnectionException("receiveInformationField> ERROR receiving data.", (short) ((short) bResult & 0x00FF));
                }
                sendFrame(txFrame);
                bState = STATE_WAIT_FOR_RR_FRAME;
            }

        } while (((hdlcFrame.sFrameFormat & HDLC_FRAME_S_BIT) != 0) ||
                (bState == STATE_WAIT_FOR_RR_FRAME) ||
                (hdlcFrame.bControl == DM));

        return (receiveBuffer.getArray());

    } // public byte[] receiveInformationField() throws DLMSConnectionException


    private int[] getCRC(byte[] byteBuffer) {
        int iLength;
        int iCRCHeader, iCRCFrame, iCharVal, i;
        int[] iCRC = new int[2];

        iLength = (((int) byteBuffer[FRAME_FORMAT_MSB] & 0x00000007) << 8) |
                ((int) byteBuffer[FRAME_FORMAT_LSB] & 0x000000FF);

        if (HEADER_SIZE <= iLength) {
            iCRCHeader = (((int) byteBuffer[FRAME_HEADER_CRC_MSB] & 0x000000FF) << 8) |
                    ((int) byteBuffer[FRAME_HEADER_CRC_LSB] & 0x000000FF);
        } else {
            iCRCHeader = -1;
        }

        iCRCFrame = (((int) byteBuffer[iLength - 2] & 0x000000FF) << 8) |
                ((int) byteBuffer[iLength - 1] & 0x000000FF);

        iCRC[0] = iCRCHeader;
        iCRC[1] = iCRCFrame;

        return (iCRC);
    }

    /**
     * <p> Input: frame without the 0x7E flag. </p>
     * <p> The CRC values will be filled in. </p>
     */
    private int[] calcCRC(byte[] byteBuffer) {
        int iLength;
        int iCRCHeader, iCRCFrame, iCharVal, i;
        int[] iCRC = new int[2];

        iLength = (((int) byteBuffer[FRAME_FORMAT_MSB] & 0x00000007) << 8) |
                ((int) byteBuffer[FRAME_FORMAT_LSB] & 0x000000FF);

        if (HEADER_SIZE <= iLength) {
            iCRCHeader = 0x0000FFFF;
            for (i = 0; i < (HEADER_SIZE - 2); i++) {
                iCharVal = (int) byteBuffer[i] & 0x000000FF;
                iCRCHeader = (iCRCHeader >> 8) ^ crc_tab[(iCRCHeader ^ iCharVal) & 0x000000FF];
                iCRCHeader &= 0x0000FFFF;
            }
            iCRCHeader ^= 0x0000FFFF;
            i = iCRCHeader;
            iCRCHeader = (iCRCHeader >> 8) & 0x000000FF;
            iCRCHeader = iCRCHeader | ((i << 8) & 0x0000FF00);
            byteBuffer[FRAME_HEADER_CRC_MSB] = (byte) ((iCRCHeader >> 8) & 0x000000FF);
            byteBuffer[FRAME_HEADER_CRC_LSB] = (byte) ((iCRCHeader) & 0x000000FF);
        } else {
            iCRCHeader = -1;
        }

        iCRCFrame = 0x0000FFFF;
        for (i = 0; i < iLength - 2; i++) {
            iCharVal = (int) byteBuffer[i] & 0x000000FF;
            iCRCFrame = (iCRCFrame >> 8) ^ crc_tab[(iCRCFrame ^ iCharVal) & 0x000000FF];
            iCRCFrame &= 0x0000FFFF;
        }

        iCRCFrame ^= 0x0000FFFF;
        i = iCRCFrame;
        iCRCFrame = (iCRCFrame >> 8) & 0x000000FF;
        iCRCFrame = iCRCFrame | ((i << 8) & 0x0000FF00);

        byteBuffer[iLength - 2] = (byte) ((iCRCFrame >> 8) & 0x000000FF);
        byteBuffer[iLength - 1] = (byte) ((iCRCFrame) & 0x000000FF);

        iCRC[0] = iCRCHeader;
        iCRC[1] = iCRCFrame;

        return (iCRC);

    } // private unsigned short CalcCRC()

    // KV 18092003
    HHUSignOn hhuSignOn = null;
    String meterId = "";
    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        this.hhuSignOn = hhuSignOn;
        this.meterId = meterId;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn,String meterId, int hhuSignonBaudRateCode) {
        setHHUSignOn(hhuSignOn, meterId);
    }

    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }

    class HDLCFrame {

        public HDLCFrame(byte[] byteReceiveBuffer) throws DLMSConnectionException {
            int i;

            sLength = (short) ((((short) byteReceiveBuffer[FRAME_FORMAT_MSB] & 0x0007) << 8) |
                    ((short) byteReceiveBuffer[FRAME_FORMAT_LSB] & 0x00FF));


            sFrameFormat = (short) ((((short) byteReceiveBuffer[FRAME_FORMAT_MSB] & 0x00FF) << 8) |
                    ((short) byteReceiveBuffer[FRAME_FORMAT_LSB] & 0x00FF));

            if (sLength >= (HEADER_SIZE + 2)) {
                InformationBuffer = new byte[sLength - (HEADER_SIZE + 2)];

                for (i = 0; i < sLength - (HEADER_SIZE + 2); i++) {
                    InformationBuffer[i] = byteReceiveBuffer[HEADER_SIZE + i];
                }
            } else {
                InformationBuffer = null;
            }

            bFrameType = (byte) ((byteReceiveBuffer[FRAME_FORMAT_MSB] >> 4) & 0x0F);

            boolControlPFBit = ((byteReceiveBuffer[FRAME_CONTROL] & HDLC_FRAME_CONTROL_PF_BIT) != 0);

            // Mask out the PF bit
            byteReceiveBuffer[FRAME_CONTROL] &= (HDLC_FRAME_CONTROL_PF_BIT ^ 0xFF);

            if ((byteReceiveBuffer[FRAME_CONTROL] & I_MASK) == I) {
                bControl = I;
            } else if ((byteReceiveBuffer[FRAME_CONTROL] & RR_MASK) == RR) {
                bControl = RR;
            } else if ((byteReceiveBuffer[FRAME_CONTROL] & RNR_MASK) == RNR) {
                bControl = RNR;
            } else {
                bControl = byteReceiveBuffer[FRAME_CONTROL];
            }


            NR = 0;
            NS = 0;
            if (bControl == I) {
                NR = (byte) ((byteReceiveBuffer[FRAME_CONTROL] & 0xE0) >> 5);
                NS = (byte) ((byteReceiveBuffer[FRAME_CONTROL] & 0x0E) >> 1);
            } else if ((bControl == RR) || (bControl == RNR)) {
                NR = (byte) ((byteReceiveBuffer[FRAME_CONTROL] & 0xE0) >> 5);
            }

            bDestination = 0;
            bSource = 0;

            // Only 1 byte address fields are supported
            if (bAddressingMode == CLIENT_ADDRESSING_1BYTE) {
                if ((byteReceiveBuffer[FRAME_DESTINATION] & 0x01) == 0) {
                    throw new DLMSConnectionException("HDLCFrame> Frame destination address error");
                } else if (bAddressingMode == CLIENT_ADDRESSING_2BYTE) {
                    if ((byteReceiveBuffer[FRAME_DESTINATION + 1] & 0x01) == 0) {
                        throw new DLMSConnectionException("HDLCFrame> Frame destination address error");
                    } else if (bAddressingMode == CLIENT_ADDRESSING_4BYTE) {
                        if ((byteReceiveBuffer[FRAME_DESTINATION + 3] & 0x01) == 0) {
                            throw new DLMSConnectionException("HDLCFrame> Frame destination address error");
                        }
                    }
                }
            }
            bDestination = (byte) (byteReceiveBuffer[FRAME_DESTINATION] >> 1);
            if ((byteReceiveBuffer[FRAME_SOURCE] & 0x01) == 0) {
                throw new DLMSConnectionException("HDLCFrame> Frame source address error");
            }
            bSource = (byte) (byteReceiveBuffer[FRAME_SOURCE] >> 1);

        }

        public short sLength;
        public byte bFrameType;
        public byte bControl;
        public boolean boolControlPFBit;
        public byte NR;
        public byte NS;
        public byte bDestination;
        public byte bSource;
        public byte[] InformationBuffer;
        public short sFrameFormat;

    } // class HDLCFrame

    public void setIskraWrapper(int type) {
        // absorb ...

    }

    private void resetCurrentTryCount() {
        this.currentTryCount = 0;
    }

    /**
     * *****************************************************************************************************
     * Invoke-Id-And-Priority byte setting
     * ******************************************************************************************************
     */

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return this.invokeIdAndPriorityHandler;
    }

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        this.invokeIdAndPriorityHandler = iiapHandler;
    }

    public int getMaxRetries() {
        return iMaxRetries;
    }

    @Override
    public int getMaxTries() {
        return getMaxRetries() + 1;
    }
}