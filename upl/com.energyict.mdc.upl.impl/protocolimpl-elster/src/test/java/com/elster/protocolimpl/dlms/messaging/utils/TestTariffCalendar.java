package com.elster.protocolimpl.dlms.messaging.utils;

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

import static org.junit.Assert.assertEquals;

/**
 * Created by heuckeg on 26.05.2014.
 *
 */
public class TestTariffCalendar
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


    @Test
    public void testParseXML_cd() throws IOException
    {
        Document xmldoc = createDocument(testData);

        Element rootElement = xmldoc.getDocumentElement();

        Integer ins = 0;
        printNodes(rootElement, ins);
    }

    @Test(expected = SAXException.class)
    public void testParseSDL_Err1() throws IOException, SAXException
    {
        Document xmldoc = createDocument(testDataErr1);

        Element rootElement = xmldoc.getDocumentElement();

        TariffCalendar.parseTariffCalendar(rootElement, null);
    }

    @Test
    public void testParseSDL() throws IOException, SAXException
    {
        Document xmldoc = createDocument(testData);

        Element rootElement = xmldoc.getDocumentElement();

        TariffCalendar cal = TariffCalendar.parseTariffCalendar(rootElement, null);
        assertEquals("ccc", cal.getName());
        assertEquals(1, cal.getWeekProfiles().size());
        assertEquals(3, cal.getDayProfiles().size());

        System.out.println(cal.toString());
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
