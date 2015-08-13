package com.elster.protocolimpl.dlms.messaging.utils;

import com.elster.dlms.cosem.classes.class11.SpecialDayEntry;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by heuckeg on 23.05.2014.
 *
 */
public class TestSpecialDayList extends SpecialDayList
{
    @Test
    public void testParseDlmsDate1() throws SAXException
    {
        int[] date = SpecialDayList.parseDateString("2014-5-23");
        assertEquals(2014, date[0]);
        assertEquals(5, date[1]);
        assertEquals(23, date[2]);
    }

    @Test
    public void testParseDlmsDate2() throws SAXException
    {
        int[] date = SpecialDayList.parseDateString("-5-23");
        assertEquals(0xFFFF, date[0]);
        assertEquals(5, date[1]);
        assertEquals(23, date[2]);
    }

    @Test(expected = SAXException.class)
    public void testParseDlmsDateErrYear() throws SAXException
    {
        SpecialDayList.parseDateString("1999-5-23");
    }

    @Test(expected = SAXException.class)
    public void testParseDlmsDateErrMonth() throws SAXException
    {
        SpecialDayList.parseDateString("2014-13-23");
    }

    @Test(expected = SAXException.class)
    public void testParseDlmsDateErrDay() throws SAXException
    {
        SpecialDayList.parseDateString("2014-12-32");
    }

    private static String sample = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
            "<special_days_table>\n" +
            "\t<entries>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>-1-1</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-4-18</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-4-20</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-4-21</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-5-1</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-5-29</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-6-8</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-6-9</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-6-19</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-10-3</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-11-1</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>-12-25</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>-12-26</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t</entries>\n" +
            "</special_days_table>\n";

    private static String sampleErr1 = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
            "<special_days_table>\n" +
            "\t<entries>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>214-1-1</date>\n" +
            "\t\t\t<day_id>2</day_id>\n" +
            "\t\t</entry>\n" +
            "\t</entries>\n" +
            "</special_days_table>\n";

    private static String sampleErr2 = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
            "<special_days_table>\n" +
            "\t<entries>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-1-1</date>\n" +
            "\t\t</entry>\n" +
            "\t</entries>\n" +
            "</special_days_table>\n";

    private static String sampleErr3 = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
            "<special_days_table>\n" +
            "\t\t<entry>\n" +
            "\t\t\t<date>2014-1-1</date>\n" +
            "\t\t</entry>\n" +
            "</special_days_table>\n";

    @Test
    public void testBuildList() throws IOException, SAXException
    {
        Document xmldoc = createDocument(sample);

        Element rootElement = xmldoc.getDocumentElement();

        ArrayList<SpecialDayEntry> list = SpecialDayList.parseSpecialDayList(rootElement);

        assertEquals(13, list.size());
    }

    @Test(expected = SAXException.class)
    public void testBuildListErr1() throws IOException, SAXException
    {
        Document xmldoc = createDocument(sampleErr1);

        Element rootElement = xmldoc.getDocumentElement();

        SpecialDayList.parseSpecialDayList(rootElement);
    }

    @Test(expected = SAXException.class)
    public void testBuildListErr2() throws IOException, SAXException
    {
        Document xmldoc = createDocument(sampleErr2);

        Element rootElement = xmldoc.getDocumentElement();

        SpecialDayList.parseSpecialDayList(rootElement);
    }

    @Test(expected = SAXException.class)
    public void testBuildListErr3() throws IOException, SAXException
    {
        Document xmldoc = createDocument(sampleErr3);

        Element rootElement = xmldoc.getDocumentElement();

        SpecialDayList.parseSpecialDayList(rootElement);
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
