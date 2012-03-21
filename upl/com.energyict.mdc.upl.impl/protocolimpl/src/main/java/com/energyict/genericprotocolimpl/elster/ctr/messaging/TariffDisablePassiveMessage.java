package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.NackStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.rawobjects.RawTariffScheme;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 14/04/11
 * Time: 14:09
 */
public class TariffDisablePassiveMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "11.0.B";
    private static final String OBJECT_ID_FUTURE = "17.0.1";

    private static String MESSAGE_TAG = "ClearPassiveTariff";
    private static final String MESSAGE_DESCRIPTION = "Clear and disable the passive tariff";

    public TariffDisablePassiveMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        try {
            disableTariff();
        } catch (CTRException e) {
            throw new BusinessException("Unable to disable the tariff: " + e.getMessage());
        }
    }

    private void disableTariff() throws BusinessException, CTRException {
        RawTariffScheme rawTariffScheme = new RawTariffScheme();
        byte[] rawData = ProtocolTools.concatByteArrays(new CTRObjectID(OBJECT_ID_FUTURE).getBytes(), rawTariffScheme.getBytes());
        Data ackOrNack = getFactory().executeRequest(ReferenceDate.getReferenceDate(2), WriteDataBlock.getRandomWDB(), new CTRObjectID(OBJECT_ID), rawData);
        if ((ackOrNack != null) && ackOrNack instanceof NackStructure) {
            NackReason reason = ((NackStructure) ackOrNack).getReason();
            throw new CTRException("Unable to disable the tariff! Received NACK: " + (reason != null ? reason.getDescription() : "NACK reason was 'null'."));
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
