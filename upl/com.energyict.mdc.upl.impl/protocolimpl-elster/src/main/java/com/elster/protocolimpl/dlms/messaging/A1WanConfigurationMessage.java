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
import java.util.regex.Pattern;

/**
 * User: heuckeg
 * Date: 02.07.13
 * Time: 13:25
 */
public class A1WanConfigurationMessage extends AbstractDlmsMessage
{
    private static final String FUNCTION = "SetWanConfiguration";
    //
    private static final String ValidIpAddressRegex = "((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))";
    private static final String OptionalPort = "(\\:[0-9]+)";
    //
    public static final String MESSAGE_TAG = "WanConfiguration";
    public static final String MESSAGE_DESCRIPTION = "Set WAN configuration data";
    public static final String ATTR_DESTINATION1 = "Destination1";
    public static final String ATTR_DESTINATION2 = "Destination2";

    public A1WanConfigurationMessage(DlmsMessageExecutor messageExecutor)
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
     * @param messageEntry: the message containing the new keys
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        String dest1 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_DESTINATION1);
        String dest2 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_DESTINATION2);
        validateMessageData(dest1, dest2);
        try
        {
            write(dest1, dest2);
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to set wan configuration data: " + e.getMessage());
        }

    }

    private void write(String dest1, String dest2) throws IOException
    {

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
                        dest1, dest2
                });
    }


    private void validateMessageData(String dest1, String dest2) throws BusinessException
    {

        checkDestination(dest1, "WAN configuration destination 1");
        if ((dest2 != null) && (dest2.length() > 0))
        {
            checkDestination(dest2, "WAN configuration  destination 2");
        }
    }

    public void checkDestination(String dest, String name) throws BusinessException
    {

        Pattern pattern = Pattern.compile(ValidIpAddressRegex + OptionalPort + "$");
        if (!pattern.matcher(dest).matches())
        {
            throw new BusinessException(name + ": error in definition");
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_DESTINATION1, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_DESTINATION2, false));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @SuppressWarnings("unused")
    private class DestinationData
    {
        private final String ip;
        private final int port;

        public DestinationData(final String data)
        {
            if ((data != null) && (data.length() > 0))
            {
                String[] s = data.split(":");
                ip = s[0];
                port = Integer.parseInt(s[1]);
            } else
            {
                ip = "";
                port = 0;
            }
        }

        public boolean isEmpty()
        {
            return ip.length() == 0;
        }

        public String getIp()
        {
            return ip;
        }

        public int getPort()
        {
            return port;
        }
    }
}
