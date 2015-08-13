package com.elster.protocolimpl.dlms.messaging.utils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
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

/**
 * Created by heuckeg on 22.05.2014.
 * Some utility functions for parsing xml files
 */
@SuppressWarnings({"unused"})
public class SaxUtils
{
    private static final String XML_PARSE_ERR_MSG = "Failed to parse the xml document - the user file does not contain a valid XML document: ";

    public static Document createDocument(String xml) throws IOException
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
            throw new IOException(XML_PARSE_ERR_MSG + e.getMessage());
        }
        catch (SAXException e)
        {
            throw new IOException(XML_PARSE_ERR_MSG + e.getMessage());
        }
        catch (IOException e)
        {
            throw new IOException(XML_PARSE_ERR_MSG + e.getMessage());
        }
        catch (DOMException e)
        {
            throw new IOException(XML_PARSE_ERR_MSG + e.getMessage());
        }
        catch (NullPointerException e)
        {
            throw new IOException(XML_PARSE_ERR_MSG + "NPE");
        }
    }

    public static String getValue(Node node)
    {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node subNode = nodes.item(i);
            if (subNode instanceof Text)
            {
                return ((Text) subNode).getWholeText();
            }
        }
        return null;
    }


}
