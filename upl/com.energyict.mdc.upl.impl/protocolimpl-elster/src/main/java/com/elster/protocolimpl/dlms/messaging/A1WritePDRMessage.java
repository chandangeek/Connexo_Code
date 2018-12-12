package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class A1WritePDRMessage extends AbstractDlmsMessage {

    private static final String FUNCTION = "SetMeteringPointId";
    //
    private static final String MESSAGE_TAG = "WritePDR";
    private static final String MESSAGE_DESCRIPTION = "Write new PDR to device";
    private static final String ATTR_PDR = "PdrToWrite";

    public A1WritePDRMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws IOException {
        String pdr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_PDR);
        validatePdr(pdr);
        try {
            writePdr(pdr);
        } catch (IOException e) {
            String msg = "Unable to write PDR. " + e.getClass().getName();
            if (e.getMessage() != null)
            {
                msg += " (" + e.getMessage() + ")";
            }
            throw new IOException(msg, e);
        }
    }

    private void writePdr(String pdr) throws IOException {

        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, FUNCTION);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + FUNCTION + "'");
        }
        rwObject.write(layer, new Object[]
                {
                        pdr
                });
    }

    private void validatePdr(String pdr) {
        if (pdr == null) {
            throw new IllegalArgumentException("Unable to write pdr. PDR value was 'null'");
        }
        if (pdr.length() != 14) {
            throw new IllegalArgumentException("Unable to write pdr. PDR should be 14 digits but was [" + pdr.length() + "] [" + pdr + "]");
        }
        if (!ProtocolTools.isNumber(pdr)) {
            throw new IllegalArgumentException("Unable to write pdr. PDR should only contain numbers but was [" + pdr + "]");
        }
        if ("00000000000000".equalsIgnoreCase(pdr)) {
            throw new IllegalArgumentException("Unable to write pdr. PDR with a value of '00000000000000' is not allowed!]");
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        tagSpec.add(new MessageAttributeSpec(ATTR_PDR, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
