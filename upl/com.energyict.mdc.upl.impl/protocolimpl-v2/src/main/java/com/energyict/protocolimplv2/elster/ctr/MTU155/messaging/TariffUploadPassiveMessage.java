package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeObjectValidator;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeTableBase64Parser;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects.RawTariffScheme;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;

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
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND.getPrimaryKey().getValue()) ||
                message.getDeviceMessageSpecPrimaryKey().getValue().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATE.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);    //TODO: should be in format "name_x", with x progressive tariff identifier number - maybe adapt to BigDecimal field?
        String tariffName = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        String codeTableBase64 = message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue().trim();
        Date activationDate = new Date(Long.parseLong(message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue()));

        try {
            CodeObject codeObject = validateAndGetCodeObject(collectedMessage, codeTableBase64);
            writeCodeTable(codeObject, tariffName, activationDate);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (IOException | BusinessException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private CodeObject validateAndGetCodeObject(CollectedMessage collectedMessage,String codeTableBase64) throws IOException {
        try {
        CodeObject codeObject = CodeTableBase64Parser.getCodeTableFromBase64(codeTableBase64);
        CodeObjectValidator.validateCodeObject(codeObject);
        return codeObject;
        } catch (BusinessException e) {
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            throw new CTRException(e.getMessage());
        }
    }

    private void writeCodeTable(CodeObject codeObject, String tariffName, Date activationDate) throws CTRException, BusinessException {
        RawTariffScheme rawTariffScheme = new RawTariffScheme(codeObject,tariffName, activationDate);
        byte[] rawData = ProtocolTools.concatByteArrays(new CTRObjectID(OBJECT_ID_FUTURE).getBytes(), rawTariffScheme.getBytes());
        getFactory().executeRequest(ReferenceDate.getReferenceDate(2), WriteDataBlock.getRandomWDB(), new CTRObjectID(OBJECT_ID), rawData);
    }
}
