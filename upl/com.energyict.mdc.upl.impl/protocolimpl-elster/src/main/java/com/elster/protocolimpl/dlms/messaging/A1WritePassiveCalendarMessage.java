package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class20.DayProfile;
import com.elster.dlms.cosem.classes.class20.WeekProfile;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.messaging.utils.SaxUtils;
import com.elster.protocolimpl.dlms.messaging.utils.TariffCalendar;
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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Calendar.getInstance;

/**
 * Created by heuckeg on 26.05.2014.
 *
 */
public class A1WritePassiveCalendarMessage extends AbstractDlmsMessage
{
    private static String DATE_PATTERN = "(20\\d\\d)?-(0?[1-9]|1[012])-([12][0-9]|3[01]|0?[1-9])";
    private static String TIME_PATTERN = "([01]?\\d|[2][0-3]):([0-5]\\d)(:[0-5]\\d)?";
    private static String DATE_TIME_PATTERN = DATE_PATTERN + "( " + TIME_PATTERN + ")?";
    private static Pattern datePattern = Pattern.compile(DATE_TIME_PATTERN);

    private static final String function = "UploadPassiveTariff";
    public static final String MESSAGE_TAG = "SetPassiveCalendar";
    public static final String MESSAGE_DESCRIPTION = "Set Passive Calendar";
    public static final String ATTR_TC_FILE = "TARIFF_CALENDAR_FILE";
    public static final String ATTR_ACTIVATION_DATE = "TARIFF_ACTIVATION_DATE";

    public A1WritePassiveCalendarMessage(DlmsMessageExecutor messageExecutor)
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
            String data1 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_TC_FILE);
            data1 = data1.replaceAll("''", "\"");
            TariffCalendar tc = validateTariffCalendar(data1);

            String data2 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ACTIVATION_DATE);
            Date actDate = validateActivationDate(data2);
            write(tc, actDate);
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to set new tariff calendar: " + e.getMessage());
        }

    }

    private void write(TariffCalendar tc, Date activationDate) throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }

        Object[] data = new Object[] {tc.getName(), activationDate,
                tc.getDayProfiles().toArray(new DayProfile[0]),
                tc.getWeekProfiles().toArray(new WeekProfile[0])};
        rwObject.write(layer, data);
    }

    protected static TariffCalendar validateTariffCalendar(String data) throws IOException
    {
        Document doc = SaxUtils.createDocument(data);

        try
        {
            return TariffCalendar.parseTariffCalendar(doc.getDocumentElement(), new ObisCode(0, 0, 10, 0, 100, 255));
        }
        catch (SAXException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    protected static Date validateActivationDate(String dateString) throws BusinessException
    {
        int[] time = new int[] {0, 0, 0, 0, 0, 0};
        Matcher match = datePattern.matcher(dateString);
        if (!match.matches())
        {
            throw new BusinessException("invalid date/time string: " + dateString);
        }

        String s;
        for (int i = 1; i < 4; i++)
        {
            s = match.group(i);
            if (s != null)
            {
                time[i - 1] = Integer.parseInt(s);
            }
        }

        s = match.group(4);
        if ((s != null) && (s.length() > 0))
        {
            for (int i = 5; i < 7; i++)
            {
                s = match.group(i);
                if (s != null)
                {
                    if (s.startsWith(":"))
                        s = s.substring(1);
                    time[i - 2] = Integer.parseInt(s);
                }
            }
        }

        Calendar c = getInstance(TimeZone.getTimeZone("GMT+0"));
        c.set(time[0], time[1] - 1, time[2], time[3], time[4], time[5]);
        return c.getTime();
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);

        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_ACTIVATION_DATE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_TC_FILE, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
