package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.classes.class20.DayProfile;
import com.elster.dlms.cosem.classes.class20.WeekProfile;
import com.elster.protocolimpl.dlms.messaging.utils.TariffCalendar;
import com.energyict.cbo.BusinessException;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by heuckeg on 21.05.2014.
 *
 */
@SuppressWarnings("unused")
public class TestWritePassiveCalendarMessage extends A1WritePassiveCalendarMessage
{
    private final static String testData = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
            "<calendar>\n" +
            "   <name>ccc</name>\n" +
            " \n" +
            "   <week_profile name=\"www\">\n" +
            "     <Monday>0</Monday>\n" +
            "     <Tuesday>0</Tuesday>\n" +
            "      <Wednesday>0</Wednesday>\n" +
            "     <Thursday>0</Thursday>\n" +
            "     <Friday>0</Friday>\n" +
            "      <Saturday>1</Saturday>\n" +
            "      <Sunday>2</Sunday>\n" +
            "   </week_profile>\n" +
            " \n" +
            "   <daily_schedule>\n" +
            "       <day_id>0</day_id>\n" +
            "       <schedules>\n" +
            "          <schedule>\n" +
            "             <time>06:00</time>\n" +
            "             <tariff>1</tariff>\n" +
            "          </schedule>\n" +
            "          <schedule>\n" +
            "             <time>22:00</time>\n" +
            "             <tariff>0</tariff>\n" +
            "          </schedule>\n" +
            "       </schedules>\n" +
            "    </daily_schedule>\n" +
            "   <daily_schedule>\n" +
            "       <day_id>1</day_id>\n" +
            "       <schedules>\n" +
            "          <schedule>\n" +
            "             <time>10:00</time>\n" +
            "             <tariff>1</tariff>\n" +
            "          </schedule>\n" +
            "          <schedule>\n" +
            "             <time>18:00</time>\n" +
            "             <tariff>0</tariff>\n" +
            "          </schedule>\n" +
            "       </schedules>\n" +
            "    </daily_schedule>\n" +
            "   <daily_schedule>\n" +
            "       <day_id>2</day_id>\n" +
            "       <schedules>\n" +
            "          <schedule>\n" +
            "             <time>06:00</time>\n" +
            "             <tariff>2</tariff>\n" +
            "          </schedule>\n" +
            "       </schedules>\n" +
            "    </daily_schedule>\n" +
            "</calendar>\n";


    private final static String testDataErr1 = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
            "<calendar>\n" +
            "  <name>ccc</name>\n" +
            " \n" +
            "  <week_profile name=\"www\">\n" +
            "    <Monday>0</Monday>\n" +
            "    <Tuesday>0</Tuesday>\n" +
            "    <Wednesday>0</Wednesday>\n" +
            "    <Thursday>0</Thursday>\n" +
            "    <Friday>0</Friday>\n" +
            "    <Saturday>1</Saturday>\n" +
            "  </week_profile>\n" +
            " \n" +
            "  <daily_schedule>\n" +
            "    <day_id>0</day_id>\n" +
            "    <schedules>\n" +
            "      <schedule>\n" +
            "        <time>06:00</time>\n" +
            "        <tariff>1</tariff>\n" +
            "      </schedule>\n" +
            "    </schedules>\n" +
            "  </daily_schedule>\n" +
            "</calendar>\n";

    public TestWritePassiveCalendarMessage()
    {
        super(null);
    }

    @Test
    public void validateActivationDate1() throws BusinessException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT0"));

        String dateString = "2014-1-1 10:11:12";

        Date date = validateActivationDate(dateString);

        dateString = "2014-1-1 10:11:00";
        assertEquals(sdf.parse(dateString).toString(), date.toString());
    }

    @Test
    public void validateActivationDate2() throws BusinessException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT0"));

        String dateString = "2014-1-1 10:11";

        Date date = validateActivationDate(dateString);

        dateString = "2014-1-1 10:11:00";
        assertEquals(sdf.parse(dateString).toString(), date.toString());
    }

    @Test
    public void validateActivationDate3() throws BusinessException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        String dateString = "2014-1-1";

        Date date = validateActivationDate(dateString);

        dateString = "2014-1-1 00:00:00";
        assertEquals(sdf.parse(dateString).toString(), date.toString());
    }

    @Test(expected = BusinessException.class)
    public void validateActivationDateErr1() throws BusinessException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT0"));

        String dateString = "2014-1-1 10:11:60";

        Date date = validateActivationDate(dateString);
    }

    @Test(expected = BusinessException.class)
    public void validateActivationDateErr2() throws BusinessException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT0"));

        String dateString = "2014-1-1 10:69";

        Date date = validateActivationDate(dateString);
    }

    @Test(expected = BusinessException.class)
    public void validateActivationDateErr3() throws BusinessException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT0"));

        String dateString = "2014-1-32";

        Date date = validateActivationDate(dateString);
    }

    @Test(expected = BusinessException.class)
    public void validateActivationDateErr4() throws BusinessException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT0"));

        String dateString = "2014-13-31";

        Date date = validateActivationDate(dateString);
    }

    @Test
    public void testTariffCalendarOk() throws IOException
    {
        TariffCalendar cal = validateTariffCalendar(testData);

        String c1 = cal.toString();

        String c2 = "ccc-WeekProfile{weekProfileName=www, monday=0, tuesday=0, wednesday=0, thursday=0, friday=0, saturday=1, sunday=2}"
                + "DayProfile{dayId=0, dayProfileActions={DayProfileAction{startTime=DlmsTime{hour=6, minute=0, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=1}, DayProfileAction{startTime=DlmsTime{hour=22, minute=0, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}}}"
                + "DayProfile{dayId=1, dayProfileActions={DayProfileAction{startTime=DlmsTime{hour=10, minute=0, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=1}, DayProfileAction{startTime=DlmsTime{hour=18, minute=0, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}}}"
                + "DayProfile{dayId=2, dayProfileActions={DayProfileAction{startTime=DlmsTime{hour=6, minute=0, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=2}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}, DayProfileAction{startTime=DlmsTime{hour=NOT_SPECIFIED, minute=NOT_SPECIFIED, second=NOT_SPECIFIED, hundredths=NOT_SPECIFIED}, scriptLogicalName=0.0.10.0.100.255, scriptSelector=0}}}";

        assertEquals(c2, c1);
    }

    @Test(expected = IOException.class)
    public void testTariffCalendarErr() throws IOException
    {
        TariffCalendar cal = validateTariffCalendar(testDataErr1);
    }

    @Test
    public void testTariffCalendarOkTypeTest() throws IOException
    {
        TariffCalendar cal = validateTariffCalendar(testData);

        Object[] data = new Object[] {cal.getName(), new Date(), cal.getDayProfiles().toArray(new DayProfile[0]), cal.getWeekProfiles().toArray(new WeekProfile[0])};

        assertTrue(data[0] instanceof String);
        assertTrue(data[1] instanceof Date);
        assertTrue(data[2] instanceof DayProfile[]);
        assertTrue(data[3] instanceof WeekProfile[]);
    }


    private void printNodes(Element element, Integer ins)
    {
        for (int i = 0; i < ins; i++)
            System.out.print(" ");

        System.out.print(element.getNodeName());
        ins += 2;

        NodeList nodes = element.getChildNodes();

        if (element.getAttributes().getLength() > 0)
        {
            NamedNodeMap a = element.getAttributes();
            for (int i = 0; i < a.getLength(); i++)
            {
                Node n = a.item(i);
                System.out.print(" [" + n.getNodeName() + ":" + n.getNodeValue() + "]");
            }
        }
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (node instanceof Element)
            {
                continue;
            }
            if (node instanceof Attr)
            {
                System.out.print(" (Attr=" + node.toString() + ")");
                continue;
            }
            if (node instanceof Text)
            {
                System.out.print(" (Text=" + ((Text) node).getWholeText() + ")");
                continue;
            }
            System.out.println(node.toString());
        }
        System.out.println();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (node instanceof Element)
            {
                //a child element to process
                printNodes((Element) node, ins);
            }
        }
    }

    private Document createDocument(String xml) throws IOException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            xml = xml.replaceAll("\n", "").replaceAll("\r", "").replaceAll(">\\s*<", "><");

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        }
        catch (ParserConfigurationException e)
        {
            throw new IOException("Failed to parse the xml document - the user file does not contain a valid XML document: " + e.getMessage());
        }
        catch (SAXException e)
        {
            throw new IOException("Failed to parse the xml document - the user file does not contain a valid XML document: " + e.getMessage());
        }
        catch (IOException e)
        {
            throw new IOException("Failed to parse the xml document - the user file does not contain a valid XML document: " + e.getMessage());
        }
        catch (DOMException e)
        {
            throw new IOException("Failed to parse the xml document - the user file does not contain a valid XML document: " + e.getMessage());
        }
    }
}
