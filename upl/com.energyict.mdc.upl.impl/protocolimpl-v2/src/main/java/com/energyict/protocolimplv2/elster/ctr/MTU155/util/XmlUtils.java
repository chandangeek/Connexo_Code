package com.energyict.protocolimplv2.elster.ctr.MTU155.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Copyrights EnergyICT
 * Date: 29/03/11
 * Time: 15:44
 */
public class XmlUtils {

    public static final String XML_DOC_TYPE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
     * @param document
     * @param name
     * @param booleanValue
     * @return
     */
    public static Element createValueChild(Document document, String name, boolean booleanValue) {
        return createValueChild(document, name, String.valueOf(booleanValue));
    }

    /**
     * @param document
     * @param name
     * @param intValue
     * @return
     */
    public static Element createValueChild(Document document, String name, int intValue) {
        return createValueChild(document, name, String.valueOf(intValue));
    }

    /**
     * @param document
     * @param name
     * @param value
     * @return
     */
    public static Element createValueChild(Document document, String name, String value) {
        Element child = document.createElement(name);
        child.setTextContent(value);
        return child;
    }

    /**
     * Prints the document to a {@link String}, without the docType (this way we can put it in the OldDeviceMessage)
     *
     * @param doc the {@link org.w3c.dom.Document} to converted
     * @return the XML String from the Document
     */
    public static String getXmlWithoutDocType(Document doc) {
        String codeTableXml = documentToString(doc);
        return codeTableXml.substring(codeTableXml.indexOf(XML_DOC_TYPE) + XML_DOC_TYPE.length());
    }

    /**
     * Prints the document to a {@link String}
     *
     * @param doc the {@link org.w3c.dom.Document} to converted
     * @return the XML String from the Document
     */
    public static String documentToString(Document doc) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            try {
                StreamResult result = new StreamResult(new StringWriter());
                DOMSource source = new DOMSource(doc);
                transformer.transform(source, result);
                return result.getWriter().toString();
            } catch (TransformerException e) {
                throw new IllegalArgumentException(e);
            }
        } catch (TransformerConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Document getDocumentFromContent(String content) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(XML_DOC_TYPE.concat(content))));
    }

    public static Element getElementByUniqueTagName(Document doc, String tagName) throws IOException {
        NodeList elementsByTagName = doc.getElementsByTagName(tagName);
        int nodeCount = elementsByTagName.getLength();
        if (nodeCount == 1) {
            Node node = elementsByTagName.item(0);
            if (node instanceof Element) {
                return (Element) node;
            } else {
                throw new IOException("tagName is not an Element but was [" + node.getClass().getName() + "]");
            }
        } else if (nodeCount == 0) {
            throw new IOException("Tag [" + tagName + "] not found in document!");
        } else {
            throw new IOException("Tag [" + tagName + "] has multiple instances in document!");
        }
    }

    /**
     * Add an openingTag to the Builder
     *
     * @param builder the builder to complete
     * @param tagName the opening TagName
     */
    public static void addOpeningTag(StringBuilder builder, String tagName) {
        builder.append("<");
        builder.append(tagName);
        builder.append(">");
    }

    /**
     * Add a closingTag to the Builder
     *
     * @param builder the builder to complete
     * @param tagName the closing TagName
     */
    public static void addClosingTag(StringBuilder builder, String tagName) {
        builder.append("</");
        builder.append(tagName);
        builder.append(">");
    }

}
