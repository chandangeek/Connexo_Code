package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.SmsRequestFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:28:57
 */
public abstract class AbstractMTU155Message {

    private final DeviceProtocol protocol;
    private final RequestFactory factory;
    private final Logger logger;

    private List<Integer> wdbList = new ArrayList();

    public abstract boolean canExecuteThisMessage(OfflineDeviceMessage message);

    public abstract CollectedMessage executeMessage(OfflineDeviceMessage message);

    public AbstractMTU155Message(Messaging messaging) {
        this.protocol = messaging.getProtocol();
        this. factory = messaging.getProtocol().getRequestFactory();
        this.logger = messaging.getProtocol().getLogger();
    }

    public CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    public CollectedMessage createCollectedMessageWithCollectedLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLoadProfile);
    }

    protected void addWriteDataBlockToWDBList(int wdb) {
        wdbList.add(new Integer(wdb));
    }

    protected void setSuccessfulDeviceMessageStatus(CollectedMessage collectedMessage) {
        if (this.factory instanceof SmsRequestFactory) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.SENT);
            if (wdbList.isEmpty()) {
                collectedMessage.setDeviceProtocolInformation("SMS identification number: " + Integer.toString((this.factory).getWriteDataBlockID()));
            } else {
                StringBuffer msg = new StringBuffer();
                msg.append("SMS identification numbers: ");
                Iterator<Integer> it = wdbList.iterator();
                while (it.hasNext()){
                    Integer next = it.next();
                    msg.append(next);
                    if (it.hasNext()) {
                        msg.append(", ");
                    }
                }

                collectedMessage.setDeviceProtocolInformation(msg.toString());
            }
        } else {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
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

    public DeviceProtocol getProtocol() {
        return protocol;
    }

    public RequestFactory getFactory() {
        return factory;
    }

    public Logger getLogger() {
        return logger;
    }
}
