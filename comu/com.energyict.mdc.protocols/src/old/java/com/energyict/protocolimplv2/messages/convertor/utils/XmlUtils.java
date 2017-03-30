/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.utils;

import com.energyict.mdc.common.ApplicationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtils {

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
                throw new ApplicationException(e);
            }
        } catch (TransformerConfigurationException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Prints the document to a {@link String}, without the docType (this way we can put it in the OldDeviceMessage)
     *
     * @param doc the {@link org.w3c.dom.Document} to converted
     * @return the XML String from the Document
     */
    public static String getXmlWithoutDocType(Document doc) {
        String xmlFormat = documentToString(doc);
        int index = xmlFormat.indexOf("?>");
        return (index != -1) ? xmlFormat.substring(index + 2) : xmlFormat;
    }

    /**
     * Loads a {@link Document} from the given xmlString
     *
     * @param xml the xml string to load
     * @return the loaded Document
     * @throws Exception if the given string is not xml or an error occurs during parsing
     */
    public static Document loadXMLDocumentFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}
