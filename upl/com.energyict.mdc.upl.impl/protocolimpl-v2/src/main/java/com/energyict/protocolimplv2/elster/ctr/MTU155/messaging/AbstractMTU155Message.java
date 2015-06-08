package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.SmsRequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:28:57
 */
public abstract class AbstractMTU155Message {

    /**
     * An offlineDeviceMessageAttribute representing an empty attribute.
     * The name and value of this attribute are both returned as empty Strings (<code>""</code>).
     */
    private static final OfflineDeviceMessageAttribute emptyOfflineDeviceMessageAttribute = new OfflineDeviceMessageAttribute() {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getDeviceMessageAttributeValue() {
            return "";
        }

        @Override
        public int getDeviceMessageId() {
            return 0;
        }

        @Override
        public String getXmlType() {
            return this.getClass().getName();
        }

        @Override
        public void setXmlType(String ignore) {
        }
    };

    private final MTU155 protocol;
    private final RequestFactory factory;
    private final Logger logger;

    public AbstractMTU155Message(Messaging messaging) {
        this.protocol = messaging.getProtocol();
        this.factory = messaging.getProtocol().getRequestFactory();
        this.logger = messaging.getProtocol().getLogger();
    }

    public abstract boolean canExecuteThisMessage(OfflineDeviceMessage message);

    public CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    public CollectedMessage createCollectedMessageWithCollectedLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLoadProfile);
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
                    MdcManager.getIssueCollector().addWarning(message, "DeviceMessage.failed",   //Device message ({0}, {1} - {2})) failed: {3}
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
     *
     * @param message the OfflineDeviceMessage
     * @return null, in case no custom CollectedMessage object is needed
     * a custom CollectedMessage object, containing additional collected data (e.g. containing LoadProfile data)
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
            StringBuffer msg = new StringBuffer();
            msg.append("SMS identification numbers: ");
            Iterator<WriteDataBlock> it = writeDataBlockList.iterator();
            while (it.hasNext()) {
                WriteDataBlock next = it.next();
                msg.append(next.getWdb());
                if (it.hasNext()) {
                    msg.append(", ");
                }
            }
            collectedMessage.setDeviceProtocolInformation(msg.toString());
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
     * with the provided name. If no match is found, then the
     * {@link #emptyOfflineDeviceMessageAttribute}
     * attribute is returned
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to search in
     * @param attributeName        the name of the OfflineDeviceMessageAttribute to return
     * @return the requested OfflineDeviceMessageAttribute or {@link #emptyOfflineDeviceMessageAttribute}
     */
    protected OfflineDeviceMessageAttribute getDeviceMessageAttribute(OfflineDeviceMessage offlineDeviceMessage, String attributeName) {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute;
            }
        }
        return emptyOfflineDeviceMessageAttribute;
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
}
