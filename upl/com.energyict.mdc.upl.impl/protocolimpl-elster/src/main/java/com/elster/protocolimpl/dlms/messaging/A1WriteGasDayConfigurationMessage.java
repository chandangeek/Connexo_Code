package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

/**
 * Created by heuckeg on 02.06.2014.
 *
 */
public class A1WriteGasDayConfigurationMessage extends AbstractDlmsMessage
{
    private static final String function = "FlagGasDayConfig";
    public static final String MESSAGE_TAG = "GasDayConfiguration";
    public static final String MESSAGE_DESCRIPTION = "Set gas day configuration flag on/off";
    public static final String ATTR_GDCF_ACTION = "GDC_FLAG";

    public A1WriteGasDayConfigurationMessage(DlmsMessageExecutor messageExecutor)
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
            String data = MessagingTools.getContentOfAttribute(messageEntry, ATTR_GDCF_ACTION);
            boolean mode = validateData(data);
            write(mode);
        }
        catch (IOException e)
        {
            String msg = "Unable to set gas day configuration flag. " + e.getClass().getName();
            if (e.getMessage() != null)
            {
                msg += " (" + e.getMessage() + ")";
            }
            throw new BusinessException(msg);
        }
    }


    private boolean validateData(String data) throws IOException
    {
        if ((data != null) && (data.length() > 0))
        {
            if ("0".equals(data))
            {
                return false;
            }
            if ("1".equals(data))
            {
                return true;
            }
        }
        throw new IOException("Wrong parameter:" + data);
    }

    protected void write(boolean mode) throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }

        Object[] data = mode ? new Object[]{1} : new Object[]{0};
        rwObject.write(layer, data);
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_GDCF_ACTION, true));

        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
