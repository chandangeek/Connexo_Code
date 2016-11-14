package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.cbo.BusinessException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeObjectValidator;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeTableBase64Parser;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects.RawTariffScheme;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 28/03/11
 * Time: 16:43
 */
public class TariffUploadPassiveMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "11.0.B";
    private static final String OBJECT_ID_CURRENT = "17.0.0";
    private static final String OBJECT_ID_FUTURE = "17.0.1";

    public TariffUploadPassiveMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND.getPrimaryKey().getValue()) ||
                message.getDeviceMessageSpecPrimaryKey().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATE.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String tariffName = getDeviceMessageAttribute(message, DeviceMessageConstants.activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String codeTableBase64 = getDeviceMessageAttribute(message, DeviceMessageConstants.activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        Date activationDate = new Date(Long.parseLong(getDeviceMessageAttribute(message, DeviceMessageConstants.activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue()));

        try {
            CodeObject codeObject = validateAndGetCodeObject(codeTableBase64);
            writeCodeTable(codeObject, tariffName, activationDate);
            return null;
        } catch (IOException | BusinessException e) {
            throw new CTRException(e.getMessage());
        }
    }

    private CodeObject validateAndGetCodeObject(String codeTableBase64) throws IOException {
        try {
            CodeObject codeObject = CodeTableBase64Parser.getCodeTableFromBase64(codeTableBase64);
            CodeObjectValidator.validateCodeObject(codeObject);
            return codeObject;
        } catch (BusinessException e) {
            throw new CTRException(e.getMessage());
        }
    }

    private void writeCodeTable(CodeObject codeObject, String tariffName, Date activationDate) throws CTRException, BusinessException {
        RawTariffScheme rawTariffScheme = new RawTariffScheme(codeObject,tariffName, activationDate);
        byte[] rawData = ProtocolTools.concatByteArrays(new CTRObjectID(OBJECT_ID_FUTURE).getBytes(), rawTariffScheme.getBytes());
        getFactory().executeRequest(ReferenceDate.getReferenceDate(2), WriteDataBlock.getRandomWDB(), new CTRObjectID(OBJECT_ID), rawData);
    }
}
