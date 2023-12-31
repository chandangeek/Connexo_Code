package com.energyict.dlms.protocolimplv2.connection;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.ProtocolException;

import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.ReceiveBuffer;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;

import java.io.IOException;

/**
 * HDLC connection layer for transport, using a ComChannel.
 * This should be used for all V2 protocols.
 * It extends from the old (V1) HDLC2Connection to reuse some common stuff.
 */
public class HDLCConnection extends HDLC2Connection implements DlmsV2Connection {

    private final ComChannel comChannel;
    private boolean useGeneralBlockTransfer;
    private int generalBlockTransferWindowSize;

    public HDLCConnection(ComChannel comChannel, CommunicationSessionProperties properties) {
        this(comChannel, properties, true);
    }

    public HDLCConnection(ComChannel comChannel, CommunicationSessionProperties properties, boolean infoFieldRequired) {
        super(properties);
        this.comChannel = comChannel;
        this.iMaxRetries = properties.getRetries();
        this.iProtocolTimeout = properties.getTimeout();
        this.NR = 0;
        this.NS = 0;
        this.boolHDLCConnected = false;
        this.hhuSignonBaudRateCode = properties.getHHUSignonBaudRateCode();
        this.invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
        this.useGeneralBlockTransfer = properties.useGeneralBlockTransfer();
        this.generalBlockTransferWindowSize = properties.getGeneralBlockTransferWindowSize();
        parseAddressingMode(properties.getAddressingMode());
        setProtocolParams();
        setSNRMType(properties.getSNRMType());
        this.infoFieldRequired = infoFieldRequired;
        generateSNRMFrames();
    }

    /**
     * Send a complete HDLC frame to the device
     */
    public void sendOut(byte[] dataToSendOut) {
        comChannel.write(dataToSendOut);
    }

    @Override
    protected void startWriting() {
        comChannel.startWriting();
    }

    @Override
    protected void startReading() {
        comChannel.startReading();
    }

    /**
     * Read in a single byte, or wait 100 ms and return -1 if nothing is available.
     */
    @Override
    protected int readIn() {
        if (comChannel.available() != 0) {
            return comChannel.read();
        } else {
            delay(100);
            return -1;
        }
    }

    @Override
    protected void copyEchoBuffer() {
        //No longer used
    }

    @Override
    public void sendUnconfirmedRequest(byte[] request) {
        try {
            super.sendUnconfirmedRequest(request);
        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMaxTries());
        }
    }

    public byte[] sendRawBytes(byte[] data) {
        startWriting();
        sendOut(data);
        try {
            return receiveInformationField(new ReceiveBuffer());
        } catch (DLMSConnectionException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new ProtocolException(e));
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMaxTries());
        }
    }

    @Override
    public byte[] sendRequest(byte[] byteRequestBuffer) {
        try {
            return super.sendRequest(byteRequestBuffer);
        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMaxTries());
        }
    }

    @Override
    public void connectMAC() {
        try {
            super.connectMAC();
        } catch (DataAccessResultException | DLMSConnectionException e) {
            throw CommunicationException.protocolConnectFailed(e);
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMaxTries());
        }
    }

    @Override
    public HHUSignOnV2 getHhuSignOn() {
        return (HHUSignOnV2) hhuSignOn;
    }

    @Override
    public void disconnectMAC() {
        try {
            super.disconnectMAC();
        } catch (AssociationControlServiceElement.ACSEParsingException | DataAccessResultException | DLMSConnectionException e) {
            throw CommunicationException.protocolDisconnectFailed(e);
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMaxTries());
        }
    }

    @Override
    public byte[] sendRequest(byte[] byteRequestBuffer, boolean alreadyEncrypted) {
        return this.sendRequest(byteRequestBuffer);
    }

    @Override
    public byte[] readResponseWithRetries(byte[] retryRequest) {
        try {
            return super.readResponseWithRetries(retryRequest);
        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMaxTries());
        }
    }

    @Override
    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) {
        return this.readResponseWithRetries(retryRequest);
    }

    /**
     * Parse the addressing mode property, it should be 1, 2 or 4.
     * It defines the number of bytes that is used to represent the server address.
     */
    private void parseAddressingMode(int addressingMode) {
        if (addressingMode == CLIENT_ADDRESSING_DEFAULT) {
            if ((iServerLowerMacAddress == 0) && (iServerUpperMacAddress <= 0x7F)) {
                bAddressingMode = CLIENT_ADDRESSING_1BYTE;
            } else if (iServerLowerMacAddress != 0) {
                bAddressingMode = CLIENT_ADDRESSING_4BYTE;
            }
        } else {
            this.bAddressingMode = (byte) addressingMode;
        }
    }

    @Override
    public boolean useGeneralBlockTransfer() {
        return useGeneralBlockTransfer;
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        return generalBlockTransferWindowSize;
    }

    @Override
    public void prepareComChannelForReceiveOfNextPacket() {
        comChannel.startWriting();
    }

}