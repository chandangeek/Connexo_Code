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

import static java.lang.Integer.parseInt;

/**
 *
 * Created by heuckeg on 29.04.2014.
 */
public class A1SetPeriodLengthMessage extends AbstractDlmsMessage
{
    private static final String function = "BillingPeriod";
    public static final String MESSAGE_TAG = "BillingPeriod";
    public static final String MESSAGE_DESCRIPTION = "Set Billing period length";
    public static final String ATTR_BILLING_PERIOD_LENGTH = "BILLING_PERIOD_LENGTH";

    public A1SetPeriodLengthMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }


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
    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        String data = MessagingTools.getContentOfAttribute(messageEntry, ATTR_BILLING_PERIOD_LENGTH);
        validateMessage(data);
        Integer length = parseInt(data);
        try {
            write(length);
        } catch (IOException e) {
            throw new BusinessException("Unable to set billing period length: " + e.getMessage());
        }
    }

    protected void write(Integer length) throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }
        rwObject.write(layer, new Integer[] {length});
    }


    @SuppressWarnings({"unused"})
    protected void validateMessage(String data) throws BusinessException
    {
        if ((data == null) || ("".equals(data)))
        {
            throw new BusinessException("Parameter billing period length was 'null' or empty.");
        }
        try
        {
            final int i = parseInt(data);
        } catch (NumberFormatException ex)
        {
            throw new BusinessException("Parameter billing period length: " + ex.getMessage());
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);

        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_BILLING_PERIOD_LENGTH, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
