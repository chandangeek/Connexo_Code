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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: heuckeg
 * Date: 02.07.13
 * Time: 15:48
 */
public class A1ConfigureCyclicMode extends AbstractDlmsMessage
{
    private static final String FUNCTION = "SetCyclicMode";
    //
    public static final String MESSAGE_TAG = "CyclicMode";
    public static final String MESSAGE_DESCRIPTION = "Set cyclic mode";
    public static final String ATTR_CALLDISTANCE = "CallDistance";

    protected static final String DistancePattern = "^(([0-9]*) )?([01]?[0-9]|2[0-3]):([0-9]|[0-5][0-9]):([0-5][0-9]|[0-9])";

    public A1ConfigureCyclicMode(DlmsMessageExecutor messageExecutor)
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
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws IOException
    {
        String callDistance = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CALLDISTANCE);
        validateMessageData(callDistance);
        try
        {
            write(callDistance);
        }
        catch (IOException e)
        {
            throw new IOException("Unable to set cyclic mode data: " + e.getMessage(), e);
        }
    }

    private void write(String callDistance) throws IOException
    {

        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, FUNCTION);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + FUNCTION + "'");
        }

        long distance = computeDistance(callDistance);
        rwObject.write(layer, new Object[]
                {
                        distance
                });
    }

    protected long computeDistance(final String distance)
    {
        long result = 0;
        Matcher match;
        Pattern pattern = Pattern.compile(DistancePattern);
        match = pattern.matcher(distance);
        if (match.matches())
        {
            for (int i = 2; i < 6; i++)
            {
                int j = (match.group(i) != null) ? Integer.parseInt(match.group(i)) : 0;
                result = (result << 8) | j;
            }
        }
        return result;
    }

    protected void validateMessageData(String callDistance)
    {
        Pattern pattern = Pattern.compile(DistancePattern);
        if (!pattern.matcher(callDistance).matches())
        {
            throw new IllegalArgumentException(ATTR_CALLDISTANCE + ": error in definition");
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        tagSpec.add(new MessageAttributeSpec(ATTR_CALLDISTANCE, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
