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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by heuckeg on 19.05.2014.
 *
 */
public class A1WriteStartOfGasDayMessage extends AbstractDlmsMessage
{
    private static final String TIME_PATTERN = "^([0-1]?[0-9]|[2][0-3])(:[0-5][0-9])?((:[0-5][0-9])?)";

    private static final String function = "StartOfGasDay";
    public static final String MESSAGE_TAG = "StartOfGasDay";
    public static final String MESSAGE_DESCRIPTION = "Set start of gas day";
    public static final String ATTR_SGD_TIME = "SGD_TIME";

    public A1WriteStartOfGasDayMessage(DlmsMessageExecutor messageExecutor)
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
            String data = MessagingTools.getContentOfAttribute(messageEntry, ATTR_SGD_TIME);
            Integer[] hms = validateData(data);
            write(hms);
        }
        catch (IOException e)
        {
            String msg = "Unable to set start of gas day. " + e.getClass().getName();
            if (e.getMessage() != null)
            {
                msg += " (" + e.getMessage() + ")";
            }
            throw new BusinessException(msg);
        }
    }


    private Integer[] validateData(String data) throws BusinessException
    {
        if ((data != null) && (data.length() > 0))
        {
            return processTimeString(data);
        }
        throw new BusinessException("Wrong parameter:" + data);
    }

    protected void write(Integer[] data) throws IOException
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

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_SGD_TIME, true));

        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public static Integer[] processTimeString(String data) throws BusinessException
    {
        Pattern pattern = Pattern.compile(TIME_PATTERN);
        Matcher match = pattern.matcher(data.toUpperCase());
        if (!match.matches())
        {
            throw new BusinessException(MESSAGE_DESCRIPTION + ": error in definition");
        }

        Integer[] result = new Integer[] { 0x0, 0x0, 0x0, 0xFF};

        for (int i = 0; i < match.groupCount() - 1; i++)
        {
            String groupValue = match.group(i + 1);
            if ((groupValue != null) && (groupValue.trim().length() > 0))
            {
                switch (i)
                {
                    case 1:
                    case 2:
                        groupValue = groupValue.substring(1);
                    case 0:
                        result[i] = Integer.parseInt(groupValue);
                        break;
                }
            }
        }

        return result;
    }

}
