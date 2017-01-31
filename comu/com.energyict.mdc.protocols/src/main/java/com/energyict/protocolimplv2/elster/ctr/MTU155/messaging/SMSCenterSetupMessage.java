/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

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

public class SMSCenterSetupMessage extends AbstractMTU155Message {

    protected static final int VALUE_LENGTH = 24;
    protected static final int SMSC_NUMBER_MAX_LENGTH = 14;
    protected static final String ALLOWED_CHARS = "1234567890+";

    protected CTRObjectID smsc_Object_ID = new CTRObjectID("E.3.1");

    public SMSCenterSetupMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
    }


    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_SMS_CENTER_NUMBER);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String smscNumber = getDeviceMessageAttribute(message, DeviceMessageConstants.smsCenterPhoneNumberAttributeName).getDeviceMessageAttributeValue();

        validateParameters(smscNumber);
        writeSMSCNumber(smscNumber);
        return null;
    }

    private void validateParameters(String smscNumber) throws CTRException {
        if (smscNumber.length() > SMSC_NUMBER_MAX_LENGTH) {
            String msg = "SMS center number is too long. Max length is " + SMSC_NUMBER_MAX_LENGTH + ". [smscNumber='" + smscNumber + "']";
            throw new CTRException(msg);
        } else if (containsInvalidChars(smscNumber)) {
            String msg = "SMS center number contains invalid characters. The number can only contain [" + ALLOWED_CHARS + "]. [smscNumber='" + smscNumber + "']";
            throw new CTRException(msg);
        }
    }

    /**
     * Checks if a given string contains a invalid characters
     *
     * @param smscNumber
     * @return
     */
    private boolean containsInvalidChars(String smscNumber) {
        for (int i = 0; i < smscNumber.length(); i++) {
            if (!ALLOWED_CHARS.contains(smscNumber.substring(i, i + 1))) {
                return true;
            }
        }
        return false;
    }

    protected void writeSMSCNumber(String smscNumber) throws CTRException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = getObjectBytes(smscNumber);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = null;
        object = objectFactory.parse(rawData, 0, attributeType);
        getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
    }

    protected byte[] getObjectBytes(String smscNumber) {
        byte[] rawData = smsc_Object_ID.getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{(byte) 0x02});
        rawData = ProtocolTools.concatByteArrays(rawData, smscNumber.getBytes());
        rawData = padData(rawData, VALUE_LENGTH);
        return rawData;
    }
}
