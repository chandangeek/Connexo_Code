package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class11.SpecialDayEntry;
import com.elster.protocolimpl.dlms.messaging.utils.SaxUtils;
import com.elster.protocolimpl.dlms.messaging.utils.SpecialDayList;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * Created by heuckeg on 22.05.2014.
 *
 */
public class A1WriteSpecialDaysTableMessage extends AbstractDlmsMessage
{
    private static final String function = "SetSpecialDaysTable";
    public static final String MESSAGE_TAG = "SetSpecialDaysTable";
    public static final String MESSAGE_DESCRIPTION = "Set special days table";
    public static final String ATTR_SPT_FILE = "SPECIAL_DAYS_TABLE_FILE";

    public A1WriteSpecialDaysTableMessage(DlmsMessageExecutor messageExecutor)
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
        String data = MessagingTools.getContentOfAttribute(messageEntry, ATTR_SPT_FILE);
        try
        {
            data = data.replaceAll("''", "\"");
            List<SpecialDayEntry> spt = validateMessage(data);

            write(spt);
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to set billing period length: " + e.getMessage());
        }

    }

    private void write(List<SpecialDayEntry> data) throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }

        rwObject.write(layer, data.toArray());
    }

    protected static List<SpecialDayEntry> validateMessage(String data) throws IOException
    {
        Document doc = SaxUtils.createDocument(data);

        try
        {
            return SpecialDayList.parseSpecialDayList(doc.getDocumentElement());
        }
        catch (SAXException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);

        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_SPT_FILE, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
