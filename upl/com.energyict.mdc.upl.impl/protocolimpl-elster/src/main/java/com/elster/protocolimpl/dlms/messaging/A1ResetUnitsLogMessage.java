package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;

import java.io.IOException;

/**
 * Created by heuckeg on 24.04.2014.
 *
 */
public class A1ResetUnitsLogMessage extends AbstractDlmsMessage
{
    private static final String function = "ResetUNITSLog";
    public static final String MESSAGE_TAG = "ResetUNITSLog";
    public static final String MESSAGE_DESCRIPTION = "Reset UNI/TS log";

    public A1ResetUnitsLogMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry)
    {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        try
        {
            getLogger().severe("Received [" + MESSAGE_TAG + "] message");
            write();
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to reset UNI/TS log! " + e.getMessage());
        }

    }

    protected void write() throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }

        rwObject.write(layer, null);
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        msgSpec.add(tagSpec);
        return msgSpec;
    }


}
