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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * Created by heuckeg on 29.04.2014.
 */
public class A1SetOnDemandSnapshotTimeMessage extends AbstractDlmsMessage
{
    private static final String function = "OnDemandSnapshotTime";
    public static final String MESSAGE_TAG = "OnDemandSnapshotTime";
    public static final String MESSAGE_DESCRIPTION = "Set on demand snapshot time";
    public static final String ATTR_ODST_DATE = "DATE";
    public static final String ATTR_ODST_REASON = "REASON";
    //
    private final DateFormat df;

    public A1SetOnDemandSnapshotTimeMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));

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
        validateMessage(messageEntry);
        Date date = getDateOfData(messageEntry);
        Integer reason = getReasonOfData(messageEntry);
        try {
            write(new Object[] {date, reason});
        } catch (IOException e) {
            throw new BusinessException("Unable to set billing period start: " + e.getMessage());
        }
    }

    protected void write(Object[] data) throws IOException
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


    @SuppressWarnings({"unused"})
    protected void validateMessage(MessageEntry messageEntry) throws BusinessException
    {
        Date date = getDateOfData(messageEntry);
        Integer reason = getReasonOfData(messageEntry);
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);

        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);

        tagSpec.add(new MessageAttributeSpec(ATTR_ODST_DATE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_ODST_REASON, true));

        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private Date getDateOfData(MessageEntry messageEntry) throws BusinessException
    {
        String dateString = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ODST_DATE);
        try
        {
            return df.parse(dateString);
        }
        catch (ParseException e)
        {
            throw new BusinessException(MESSAGE_DESCRIPTION + "- error in date parameter (" + dateString + "): " + e.getMessage());
        }
    }

    private Integer getReasonOfData(MessageEntry messageEntry) throws BusinessException
    {
        String reasonString = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ODST_REASON);
        try
        {
            int reason = Integer.parseInt(reasonString);
            if ((reason < 0) || (reason > 7))
                throw new NumberFormatException("value of of range");
            return reason;
        }
        catch (NumberFormatException e)
        {
            throw new BusinessException(MESSAGE_DESCRIPTION + "- error in reason parameter (" + reasonString + "): " + e.getMessage());
        }
    }


}
