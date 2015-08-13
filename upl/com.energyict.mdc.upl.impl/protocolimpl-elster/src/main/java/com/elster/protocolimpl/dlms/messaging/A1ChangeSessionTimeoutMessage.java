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
import java.text.ParseException;

/**
 * User: heuckeg
 * Date: 02.07.13
 * Time: 15:48
 */
public class A1ChangeSessionTimeoutMessage extends AbstractDlmsMessage
{
    //
    private static final String FUNCTION = "SetSessionTimeout";
    //
    public static final String MESSAGE_TAG = "SessionTimeout";
    public static final String MESSAGE_DESCRIPTION = "Change session timeout";
    public static final String ATTR_SESSION_TIMEOUT_MS = "SessionTimeout[ms]";


    public A1ChangeSessionTimeoutMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

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
    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        String timeoutValue = MessagingTools.getContentOfAttribute(messageEntry, ATTR_SESSION_TIMEOUT_MS);
        validateMessageData(timeoutValue);
        try
        {
            write(timeoutValue);
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to set session timeout: " + e.getMessage());
        }
    }

    private void write(String sessionTimeout) throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, FUNCTION);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support FUNCTION '" + FUNCTION + "'");
        }

        long timeout = Long.parseLong(sessionTimeout);

        rwObject.write(layer, new Object[]
                {
                        timeout
                });
    }

    protected void validateMessageData(final String sessionTimeout) throws BusinessException
    {
        try
        {
            int i = Integer.parseInt(sessionTimeout);
            if ((i < 30000) || (i > 1000000))
            {
                throw new ParseException("value out of range (30000-1000000). Is " + i, 0);
            }
        }
        catch (ParseException e)
        {
            throw new BusinessException(ATTR_SESSION_TIMEOUT_MS + ": error in definition - " + e.getMessage());
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_SESSION_TIMEOUT_MS, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
