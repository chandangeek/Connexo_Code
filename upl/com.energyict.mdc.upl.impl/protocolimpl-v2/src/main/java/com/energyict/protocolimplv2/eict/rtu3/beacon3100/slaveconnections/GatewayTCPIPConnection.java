package com.energyict.protocolimplv2.eict.rtu3.beacon3100.slaveconnections;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.TCPIPConnection;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.LowerLayerCommunicationException;

/**
 * Serves as a TCPIPConnection when the slave devices communication in 'gateway-mode' with the Beacon.
 * All connection-related exceptions are actually communication related exceptions due to lower-layer errors
 * (read: Timeout, incorrect logicalId, ...) These exceptions should not terminate the connection itself.
 * This means that all connection-related exceptions should be wrapped as a lower-layer Communication related exception
 * <p>
 * Except when it is actually a connection-related exception (read: connection-closed, ...)
 * <p>
 * Copyrights EnergyICT
 * Date: 07.04.16
 * Time: 15:14
 */
public class GatewayTCPIPConnection extends TCPIPConnection {

    public GatewayTCPIPConnection(ComChannel comChannel, CommunicationSessionProperties properties) {
        super(comChannel, properties);
    }

    @Override
    public void prepareComChannelForReceiveOfNextPacket() {
        try {
            super.prepareComChannelForReceiveOfNextPacket();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public long getTimeout() {
        try {
            return super.getTimeout();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void setTimeout(long timeout) {
        try {
            super.setTimeout(timeout);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void setRetries(int retries) {
        try {
            super.setRetries(retries);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public byte[] sendRequest(byte[] encryptedRequest, boolean isAlreadyEncrypted) {
        try {
            return super.sendRequest(encryptedRequest, isAlreadyEncrypted);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void sendUnconfirmedRequest(byte[] request) {
        try {
            super.sendUnconfirmedRequest(request);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        try {
            super.setHHUSignOn(hhuSignOn, meterId);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int hhuSignonBaudRateCode) {
        try {
            super.setHHUSignOn(hhuSignOn, meterId, hhuSignonBaudRateCode);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public HHUSignOn getHhuSignOn() {
        try {
            return super.getHhuSignOn();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        try {
            return super.getInvokeIdAndPriorityHandler();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        try {
            super.setInvokeIdAndPriorityHandler(iiapHandler);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public int getMaxRetries() {
        try {
            return super.getMaxRetries();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public int getMaxTries() {
        try {
            return super.getMaxTries();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public boolean useGeneralBlockTransfer() {
        try {
            return super.useGeneralBlockTransfer();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        try {
            return super.getGeneralBlockTransferWindowSize();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public long getForceDelay() {
        try {
            return super.getForceDelay();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void connectMAC() {
        try {
            super.connectMAC();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void disconnectMAC() {
        try {
            super.disconnectMAC();
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    protected void delay(long lDelay) {
        try {
            super.delay(lDelay);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public void setSwitchAddresses(boolean type) {
        try {
            super.setSwitchAddresses(type);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public byte[] readResponseWithRetries(byte[] retryRequest) {
        try {
            return super.readResponseWithRetries(retryRequest);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) {
        try {
            return super.readResponseWithRetries(retryRequest, isAlreadyEncrypted);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public byte[] sendRawBytes(byte[] data) {
        try {
            return super.sendRawBytes(data);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }

    @Override
    public byte[] sendRequest(byte[] data) {
        try {
            return super.sendRequest(data);
        } catch (ConnectionCommunicationException e) {
            throw LowerLayerCommunicationException.downgradeFromConnectionExceptionIfNecessary(e);
        }
    }
}
