package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/09/2015 - 12:26
 */
public class Beacon3100PLCConfigurationDeviceMessageExecutor extends PLCConfigurationDeviceMessageExecutor {

    private static final String SEPARATOR = ";";
    private final CollectedDataFactory collectedDataFactory;
    boolean readOldObisCodes = false;

    public Beacon3100PLCConfigurationDeviceMessageExecutor(DlmsSession session, OfflineDevice offlineDevice, boolean readOldObisCodes, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(session, offlineDevice, readOldObisCodes, collectedDataFactory, issueFactory);
        this.readOldObisCodes = readOldObisCodes;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    // Beacon3100 uses IC version 1 now, doesn't have the adp_routing_tuple_TTL
    protected boolean isICVersion0(OfflineDeviceMessage pendingMessage) {
        return false;
    }

    @Override
    protected CollectedMessage doPathRequest(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        final long normalTimeout = session.getProperties().getTimeout();
        StringBuilder pingFailed = new StringBuilder();
        StringBuilder pathFailed = new StringBuilder();

        try {
            final long timeoutInMillis = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.timeout).getValue());

            List<String> macAddresses = new ArrayList<>();
            for (String macAddress : MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceGroupAttributeName).getValue().split(SEPARATOR)) {
                final String errorMsg = "MAC addresses should be a list of 16 hex characters, separated by a semicolon.";
                try {
                    final byte[] macAddressBytes = ProtocolTools.getBytesFromHexString(macAddress, "");
                    if (macAddressBytes.length != 8) {
                        collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(pendingMessage, errorMsg));
                        collectedMessage.setDeviceProtocolInformation(errorMsg);
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        return collectedMessage;
                    }
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    collectedMessage.setFailureInformation(ResultType.ConfigurationError, createMessageFailedIssue(pendingMessage, errorMsg));
                    collectedMessage.setDeviceProtocolInformation(errorMsg);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    return collectedMessage;
                }
                macAddresses.add(macAddress);
            }

            final long fullRoundTripTimeout = timeoutInMillis + normalTimeout;
            session.getDLMSConnection().setTimeout(fullRoundTripTimeout);


            final G3NetworkManagement g3NetworkManagement = getG3NetworkManagement();
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            for (String macAddress : macAddresses) {

                Integer pingTime = null;
                try {
                    pingTime = g3NetworkManagement.pingNode(macAddress, ((int) timeoutInMillis) / 1000);
                } catch (DataAccessResultException e) {
                    pingTime = null;
                }

                if (pingTime == null || pingTime <= 0) {
                    logFailedPingRequest(pingFailed, macAddress);
                } else {
                    try {
                        String fullPath = g3NetworkManagement.requestPath(macAddress, false);   //If successful, it will be added in the topology of the RTU+Server

                        final DialHomeIdDeviceIdentifier deviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);
                        final RegisterDataIdentifierByObisCodeAndDevice registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(G3NetworkManagement.getDefaultObisCode(), deviceIdentifier);
                        final CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(registerIdentifier);
                        collectedRegister.setCollectedData(fullPath);
                        collectedRegister.setReadTime(new Date());
                        collectedRegisters.add(collectedRegister);
                    } catch (DataAccessResultException e) {
                        logFailedPathRequest(pathFailed, macAddress);
                    }
                }
            }

            String allInfo = (pingFailed.length() == 0 ? "" : (pingFailed + ". ")) + (pathFailed.length() == 0 ? "" : (pathFailed + "."));

            collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
            collectedMessage.setDeviceProtocolInformation(allInfo);
            collectedMessage.setNewDeviceMessageStatus(allInfo.isEmpty() ? DeviceMessageStatus.CONFIRMED : DeviceMessageStatus.FAILED);
        } finally {
            session.getDLMSConnection().setTimeout(normalTimeout);
        }
        return collectedMessage;
    }

    protected G3NetworkManagement getG3NetworkManagement() throws NotInObjectListException {
        if(readOldObisCodes) {
            return this.session.getCosemObjectFactory().getG3NetworkManagement();
        }else{
            return this.session.getCosemObjectFactory().getG3NetworkManagement(Beacon3100Messaging.G3_NETWORK_MANAGEMENT_NEW_OBISCODE);
        }
    }


    private void logFailedPingRequest(StringBuilder pingFailed, String macAddress) {
        if (pingFailed.toString().length() == 0) {
            pingFailed.append("Ping failed for: ");
            pingFailed.append(macAddress);
        } else {
            pingFailed.append(", ");
            pingFailed.append(macAddress);
        }
    }

    private void logFailedPathRequest(StringBuilder pathFailed, String macAddress) {
        if (pathFailed.toString().length() == 0) {
            pathFailed.append("Path request failed for: ");
            pathFailed.append(macAddress);
        } else {
            pathFailed.append(", ");
            pathFailed.append(macAddress);
        }
    }
}