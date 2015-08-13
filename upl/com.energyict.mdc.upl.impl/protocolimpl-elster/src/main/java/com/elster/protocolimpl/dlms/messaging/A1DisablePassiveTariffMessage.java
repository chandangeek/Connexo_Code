package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 14/04/11
 * Time: 14:09
 */
public class A1DisablePassiveTariffMessage extends AbstractDlmsMessage {

    private static final String FUNCTION = "DisablePassiveTariff";

    private static final String MESSAGE_TAG = "ClearPassiveTariff";
    private static final String MESSAGE_DESCRIPTION = "Clear and disable the passive tariff";

    public A1DisablePassiveTariffMessage(DlmsMessageExecutor messageExecutor) {
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
        } catch (IOException e) {
            throw new BusinessException("Unable to disable the tariff: " + e.getMessage());
        }
    }

    private void disableTariff() throws IOException {

        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, FUNCTION );
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + FUNCTION + "'");
        }

        rwObject.write(layer, new Object[]
                {
                });
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
