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
 * Created by heuckeg on 27.05.2014.
 *
 */
public class A1WriteClockConfigurationMessage extends AbstractDlmsMessage
{
    private static final String function = "DstEnabled";
    public static final String MESSAGE_TAG = "SetClockConfiguration";
    public static final String MESSAGE_DESCRIPTION = "Set clock configuration";
    public static final String ATTR_TZ_OFFSET = "TIMEZONE_OFFSET";
    public static final String ATTR_DST_ENABLED = "DST_ENABLED";
    public static final String ATTR_DST_DEVIATION = "DST_DEVIATION";

    public A1WriteClockConfigurationMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    public boolean canExecuteThisMessage(MessageEntry messageEntry)
    {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        try
        {
            String data1 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_TZ_OFFSET);
            int tzOffset = validateTimezoneOffset(data1);

            String data2 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_DST_ENABLED);
            int dstEnabled = validateDstEnabled(data2);

            String data3 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_DST_DEVIATION);
            int dstDeviation = validateDstDeviation(data3);
            write(new Object[] {tzOffset, dstEnabled, dstDeviation});
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to set clock configuration: " + e.getMessage());
        }

    }

    private void write(Object[] data) throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }

        rwObject.write(layer, data);
    }

    protected static int validateTimezoneOffset(String data) throws IOException
    {
        try
        {
            return Integer.parseInt(data);
        }
        catch (NumberFormatException ex)
        {
            throw new IOException("Timezone offset: " + ex.getMessage());
        }
    }

    protected static int validateDstEnabled(String data) throws IOException
    {
        try
        {
            return Integer.parseInt(data) > 0 ? 1 : 0;
        }
        catch (NumberFormatException ex)
        {
            if ((data != null) && (data.length() > 0))
            {
                if (data.toUpperCase().startsWith("T") || data.toUpperCase().startsWith("Y"))
                {
                    return 1;
                }
                if (data.toUpperCase().startsWith("F") || data.toUpperCase().startsWith("N"))
                {
                    return 0;
                }
            }
            throw new IOException("Dst enabled: " + ex.getMessage());
        }
    }

    protected static int validateDstDeviation(String data) throws IOException
    {
        try
        {
            return Integer.parseInt(data);
        }
        catch (NumberFormatException ex)
        {
            throw new IOException("Dst deviation: " + ex.getMessage());
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);

        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_TZ_OFFSET, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_DST_ENABLED, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_DST_DEVIATION, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
