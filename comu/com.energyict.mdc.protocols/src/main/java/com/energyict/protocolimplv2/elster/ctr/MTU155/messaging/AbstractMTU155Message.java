package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.SmsRequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:28:57
 */
public abstract class AbstractMTU155Message {

    private final MTU155 protocol;
    private final RequestFactory factory;
    private final Logger logger;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;

    public abstract boolean canExecuteThisMessage(OfflineDeviceMessage message);

    public AbstractMTU155Message(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.protocol = messaging.getProtocol();
        this.factory = messaging.getProtocol().getRequestFactory();
        this.logger = messaging.getProtocol().getLogger();
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    public CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(message.getIdentifier());
    }

    public CollectedMessage createCollectedMessageWithCollectedLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return this.collectedDataFactory.createCollectedMessageWithLoadProfileData(message.getIdentifier(), collectedLoadProfile);
    }

    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        resetSMSWriteDataBlockList();

        try {
            CollectedMessage customCollectedMessage = doExecuteMessage(message);
            if (customCollectedMessage != null) {   // During execution of the message, a custom CollectedMessage is build up (e.g. containing LoadProfile data)
                collectedMessage = customCollectedMessage;
            }
            setSuccessfulDeviceMessageStatus(collectedMessage, getListOfSMSWriteDataBlocks());
        } catch (CTRException e) {
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
             collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other,
                    this.issueService.newWarning(
                            message,
                            MessageSeeds.DEVICEMESSAGE_FAILED,
                            message.getDeviceMessageId(),
                            message.getSpecification().getCategory().getName(),
                            message.getSpecification().getName(),
                            e.getMessage())
            );
        }
        return collectedMessage;
    }

    /**
     * Method in which the pending message will be executed
     * @param message the OfflineDeviceMessage
     * @return  null, in case no custom CollectedMessage object is needed
     *          a custom CollectedMessage object, containing additional collected data (e.g. containing LoadProfile data)
     * @throws CTRException
     */
    protected abstract CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException;

    private void resetSMSWriteDataBlockList() {
        if (factory instanceof SmsRequestFactory) {
            ((SmsRequestFactory) factory).resetWriteDataBlockList();
        }
    }

    private List<WriteDataBlock> getListOfSMSWriteDataBlocks() {
        if (factory instanceof SmsRequestFactory) {
            return ((SmsRequestFactory) factory).getListOfWriteDataBlocks();
        }
        return null;
    }

    protected void setSuccessfulDeviceMessageStatus(CollectedMessage collectedMessage, List<WriteDataBlock> writeDataBlockList) {
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        if (this.factory instanceof SmsRequestFactory) {
            StringBuilder builder = new StringBuilder();
            builder.append("SMS identification numbers: ");
            Iterator<WriteDataBlock> it = writeDataBlockList.iterator();
            while (it.hasNext()) {
                WriteDataBlock next = it.next();
                builder.append(next.getWdb());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            collectedMessage.setDeviceProtocolInformation(builder.toString());
        }
    }

    protected byte[] padData(byte[] fieldData, int valueLength) {
        int paddingLength = valueLength - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, valueLength);
        }
        return fieldData;
    }

    /**
     * Searches for the {@link OfflineDeviceMessageAttribute}
     * in the given {@link OfflineDeviceMessage} which corresponds
     * with the provided name. If no match is found, then a new
     * {@link EmptyOfflineDeviceMessageAttribute} is created and returned.
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to search in
     * @param attributeName        the name of the OfflineDeviceMessageAttribute to return
     * @return the OfflineDeviceMessageAttribute
     */
    protected OfflineDeviceMessageAttribute getDeviceMessageAttribute(OfflineDeviceMessage offlineDeviceMessage, String attributeName) {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute;
            }
        }
        return new EmptyOfflineDeviceMessageAttribute();
    }

    public MTU155 getProtocol() {
        return protocol;
    }

    public RequestFactory getFactory() {
        return factory;
    }

    public Logger getLogger() {
        return logger;
    }

    private static class EmptyOfflineDeviceMessageAttribute implements OfflineDeviceMessageAttribute {

        @Override
        public PropertySpec getPropertySpec() {
            return null;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getDeviceMessageAttributeValue() {
            return "";
        }
    }

}