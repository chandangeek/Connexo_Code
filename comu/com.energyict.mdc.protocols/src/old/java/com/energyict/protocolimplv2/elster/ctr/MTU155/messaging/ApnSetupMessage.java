package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:27:15
 */
public class ApnSetupMessage extends AbstractMTU155Message {

    public static final int VALUE_LENGTH = 114;
    public static final int APN_MAX_LENGTH = 40;
    public static final int PASS_MAX_LENGTH = 30;
    public static final int USER_MAX_LENGTH = 30;

    public ApnSetupMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String apn = getDeviceMessageAttribute(message, DeviceMessageConstants.apnAttributeName).getDeviceMessageAttributeValue();
        String user = getDeviceMessageAttribute(message, DeviceMessageConstants.usernameAttributeName).getDeviceMessageAttributeValue();
        String pssw = getDeviceMessageAttribute(message, DeviceMessageConstants.passwordAttributeName).getDeviceMessageAttributeValue();

        validateApnSetupParameters(apn, user, pssw);
        writeApnSetup(apn, user, pssw);
        return null;
    }

    private void validateApnSetupParameters(String apn, String user, String pssw) throws CTRException {
        if (apn.length() > APN_MAX_LENGTH) {
            String msg = "Parameter APN exceeded the maximum length (40 characters).";
            throw new CTRException(msg);
        } else if (user.length() > USER_MAX_LENGTH) {
            String msg = "Parameter username exceeded the maximum length (30 characters).";
            throw new CTRException(msg);
        } else if (pssw.length() > PASS_MAX_LENGTH) {
            String msg = "Parameter password exceeded the maximum length (30 characters).";
            throw new CTRException(msg);
        }
    }

    private void writeApnSetup(String apn, String user, String pssw) throws CTRException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        int frequency = 0;
        byte[] rawData = getObjectBytes(frequency, apn, user, pssw);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, attributeType);
        getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
    }

    private byte[] getObjectBytes(int frequency, String apn, String user, String pssw) {
        byte[] rawData = new CTRObjectID("E.E.1").getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{(byte) frequency});
        rawData = ProtocolTools.concatByteArrays(rawData, apn.getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[1]);
        rawData = ProtocolTools.concatByteArrays(rawData, user.getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[1]);
        rawData = ProtocolTools.concatByteArrays(rawData, pssw.getBytes());
        rawData = padData(rawData, VALUE_LENGTH);
        return rawData;
    }
}
