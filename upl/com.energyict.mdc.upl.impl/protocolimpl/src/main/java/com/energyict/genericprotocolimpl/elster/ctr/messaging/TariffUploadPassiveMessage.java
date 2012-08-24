package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.ProcessingException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.NackStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.CodeObjectValidator;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.CodeTableBase64Parser;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.objects.CodeObject;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.rawobjects.RawTariffScheme;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

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

    private static final String MESSAGE_DESCRIPTION = "Upload a new passive tariff scheme";

    public static final String MESSAGE_TAG = "UploadPassiveTariff";
    public static final String ATTR_ACTIVATION_TIME = "ActivationTime";
    public static final String ATTR_cODE_TABLE_ID = "CodeTableId";

    public TariffUploadPassiveMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        try {
            String activationTimeAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ACTIVATION_TIME);
            String codeTableBase64Attr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_cODE_TABLE_ID);
            Date activationTime = validateAndGetActivationTime(activationTimeAttr);
            CodeObject codeObject = validateAndGetCodeObject(codeTableBase64Attr);
            writeCodeTable(codeObject, activationTime);
        } catch (IOException e) {
            throw new ProcessingException("Unable to write new tariff to device: " + e.getMessage(), e);
        }
    }

    private CodeObject validateAndGetCodeObject(String codeTableBase64) throws IOException, BusinessException {
        CodeObject codeObject = CodeTableBase64Parser.getCodeTableFromBase64(codeTableBase64);
        CodeObjectValidator.validateCodeObject(codeObject);
        return codeObject;
    }

    private Date validateAndGetActivationTime(String activationTimeAttr) throws BusinessException {
        if (activationTimeAttr == null) {
            throw new BusinessException("Activation time cannot be 'null'!");
        }
        Date activationDate = ProtocolTools.getEpochDateFromString(activationTimeAttr);
        if (activationDate == null) {
            throw new BusinessException("Unable to get Date object from activation time attribute [" + activationTimeAttr + "]. Maybe wrong format.");
        }
        Date after = ProtocolTools.getDateFromYYYYMMddhhmmss("2000-01-01 00:00:00");
        if (activationDate.before(after)) {
            throw new BusinessException("Invalid activation date [" + activationDate + "]. Date should be after [" + after + "]");
        }
        return activationDate;
    }

    private void writeCodeTable(CodeObject codeObject, Date activationDate) throws BusinessException, CTRException {
        RawTariffScheme rawTariffScheme = new RawTariffScheme(codeObject, activationDate);
        byte[] rawData = ProtocolTools.concatByteArrays(new CTRObjectID(OBJECT_ID_FUTURE).getBytes(), rawTariffScheme.getBytes());
        Data ackOrNack = getFactory().executeRequest(ReferenceDate.getReferenceDate(2), WriteDataBlock.getRandomWDB(), new CTRObjectID(OBJECT_ID), rawData);
        if ((ackOrNack != null) && ackOrNack instanceof NackStructure) {
            NackReason reason = ((NackStructure) ackOrNack).getReason();
            throw new CTRException("Unable to write the tariff! Received NACK: " + (reason != null ? reason.getDescription() : "NACK reason was 'null'."));
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_cODE_TABLE_ID, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_ACTIVATION_TIME, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
