package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.protocolimpl.dlms.util.A1Defs;
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
 * User: heuckeg
 * Date: 02.07.13
 * Time: 15:48
 */
public class A1ConfigurePreferredDateMode extends AbstractDlmsMessage
{
    public static final String MESSAGE_TAG = "PreferredDateMode";
    public static final String MESSAGE_DESCRIPTION = "Set preferred date mode";
    public static final String ATTR_PREF_DATE = "PreferredDate";

    protected static final String DatePattern = "((([0-9]|[0-2][0-9]|3[0-1])|Mo|Tu|We|Th|Fr|Sa|Su) )?([01]?[0-9]|2[0-3]):([0-9]|[0-5][0-9]):([0-5][0-9]|[0-9])";

    public A1ConfigurePreferredDateMode(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry)
    {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    /**
     * Send the message to the meter.
     *
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        String prefDate = MessagingTools.getContentOfAttribute(messageEntry, ATTR_PREF_DATE);
        validateMessageData(prefDate);
        try
        {
            write(prefDate);
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to set cyclic mode data: " + e.getMessage());
        }
    }

    private void write(String prefDate) throws IOException
    {

        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();

        CosemAttributeDescriptor attributeDescriptor;

        long distance = computeDate(prefDate);
        attributeDescriptor = new CosemAttributeDescriptor(A1Defs.GPRS_MODEM_SETUP, 9138, 10);
        layer.setAttribute(attributeDescriptor, new DlmsDataDoubleLongUnsigned(distance));

        attributeDescriptor = new CosemAttributeDescriptor(A1Defs.GPRS_MODEM_SETUP, 9138, 17);
        DlmsDataDoubleLongUnsigned controlFlags = (DlmsDataDoubleLongUnsigned) layer.getAttributeAndCheckResult(attributeDescriptor);
        long flags = (controlFlags.getValue() & (0xFF ^ 0x3)) | 0x02;
        controlFlags = new DlmsDataDoubleLongUnsigned(flags);
        layer.setAttributeAndCheckResult(attributeDescriptor, controlFlags);
    }

    private final String[] weekdays = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};

    protected long computeDate(final String distance)
    {
        long result = 0;
        Matcher match;
        Pattern pattern = Pattern.compile(DatePattern);
        match = pattern.matcher(distance);
        if (match.matches())
        {
            //for (int i = 0; i <= match.groupCount(); i++)
            //{
            //    System.out.println("Group[" + i + "]=<" + match.group(i) + ">");
            //}

            if (match.group(2) != null)
            {
                for (int i = 0; i < weekdays.length; i++)
                {
                    if (weekdays[i].equals(match.group(2)))
                    {
                        result = i + 1;
                        break;
                    }
                }
                if (result == 0)
                {
                    result = Integer.parseInt(match.group(2)) << 4;
                }
            }
            for (int i = 4; i <= 6; i++)
            {
                int j = (match.group(i) != null) ? Integer.parseInt(match.group(i)) : 0;
                result = (result << 8) | j;
            }
        }
        return result;
    }

    protected void validateMessageData(String prefDate) throws BusinessException
    {
        Pattern pattern = Pattern.compile(DatePattern);
        if (!pattern.matcher(prefDate).matches())
        {
            throw new BusinessException(ATTR_PREF_DATE + ": error in definition");
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_PREF_DATE, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
