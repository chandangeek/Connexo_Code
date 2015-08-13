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
 *
 * Created by heuckeg on 24.04.2014.
 *
 */
public class A1SetBillingPeriodStartMessage extends AbstractDlmsMessage
{
    private static final String PATTERN = "([\\d]{4})?-([\\d]{1,2})?-([\\d]{1,2})?( (MO|TU|WE|TH|FR|SA|SU))?";
    //
    private static final String function = "BillingPeriodStart";
    public static final String MESSAGE_TAG = "BillingPeriodStart";
    public static final String MESSAGE_DESCRIPTION = "Set Billing period start";
    public static final String ATTR_BILLING_PERIOD_START_DATE = "BILLING_PERIOD_START_DATE";

    public A1SetBillingPeriodStartMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }
    /**
     * Send the message to the meter.
     *
     * @param messageEntry: the message containing the data
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        String data = MessagingTools.getContentOfAttribute(messageEntry, ATTR_BILLING_PERIOD_START_DATE);
        validateMessage(data);
        Integer[] values = processDateString(data);
        try {
            write(values);
        } catch (IOException e) {
            throw new BusinessException("Unable to set billing period start: " + e.getMessage());
        }
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


    protected void validateMessage(String data) throws BusinessException
    {
        if ((data == null) || ("".equals(data)))
        {
            throw new BusinessException("Parameter billing period start was 'null' or empty.");
        }
        processDateString(data);
    }


    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);

        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_BILLING_PERIOD_START_DATE, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public static Integer[] processDateString(String data) throws BusinessException
    {
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher match = pattern.matcher(data.toUpperCase());
        if (!match.matches())
        {
            throw new BusinessException("Parameter billing period start: error in definition");
        }

        Integer[] result = new Integer[] { 0xFFFF, 0xFF, 0xFF, 0xFF};
        String[] weekdays = new String[] {"--", " MO", " TU", " WE", " TH", " FR", " SA", " SU"};

        for (int i = 0; i < match.groupCount() - 1; i++)
        {
            String groupValue = match.group(i + 1);
            if ((groupValue != null) && (groupValue.trim().length() > 0))
            {
                switch (i)
                {
                    case 0:
                    case 1:
                    case 2:
                        result[i] = Integer.parseInt(groupValue);
                        break;
                    case 3:
                        for (int j = 1; j < weekdays.length; j++)
                        {
                            if (groupValue.equals(weekdays[j]))
                            {
                                result[3] = j;
                                break;
                            }
                        }
                }
            }
        }

        return result;
    }
}
