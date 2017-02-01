package com.energyict.messaging;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Message builder class responsible of generating and parsing messages.
 *
 * @return The {@link MessageBuilder} capable of generating and parsing messages.
 */
public abstract class AbstractMessageBuilder implements MessageBuilder {

    // Parsing the message
    protected static InputSource getInputSource(String xmlString) {
        Reader reader = new CharArrayReader(xmlString.toCharArray());
        return new InputSource(reader);
    }

    /**
     * Returns a fully initialized MessageBuilder starting from the given xml String
     *
     * @param xmlString string to initialize the messagebuilder
     * @throws SAXException, IOException if the given string is wrong xml
     *                       or is not the correct message
     * @Return a initialized MessageBuilder if the message is a correct message
     */
    public void initFromXml(String xmlString) throws SAXException, IOException {
        SAXParser saxParser = getSaxParser();
        XMLReader reader = saxParser.getXMLReader();
        AdvancedMessageHandler handler = this.getMessageHandler(this);
        reader.setContentHandler(handler);
        reader.parse(AbstractMessageBuilder.getInputSource(xmlString));
    }

    // parsing using SAX
    private SAXParser getSaxParser() throws SAXException, IOException {
        try {
            SAXParser saxParser;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            //factory.setValidating(true);
            saxParser = factory.newSAXParser();
            saxParser.setProperty(
                    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            return saxParser;
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }

    protected abstract AdvancedMessageHandler getMessageHandler(MessageBuilder builder);

    public static abstract class AdvancedMessageHandler extends DefaultHandler {

        /**
         * String builder used to build content in the current tag.
         */
        private final StringBuilder contentBuilder = new StringBuilder();
        private Locator locator;
        private String messageTag;
        //private Object currentValue = null;
        private boolean messageTagEncountered;

        public AdvancedMessageHandler(String messageTag) {
            this.messageTag = messageTag;
            if (this.messageTag == null) {
                throw new IllegalArgumentException("Parameter messageTaf cannot be null");
            }
        }

        public void startDocument() {
            messageTagEncountered = false;
        }

        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) throws SAXException {
            if (!messageTagEncountered) {
                if (!messageTag.equals(localName)) {
                    throw new SAXParseException(localName + " not recognized", this.locator);
                }
                messageTagEncountered = true;
            }

            // Reset the string builder.
            this.contentBuilder.setLength(0);
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            final String characters = String.valueOf(ch, start, length);

            if (!characters.trim().equals("")) {
                this.contentBuilder.append(characters.trim());
            }
        }

        protected String getMessageTag() {
            return messageTag;
        }

        protected boolean messageTagEncountered() {
            return messageTagEncountered;
        }

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        protected Object getCurrentValue() {
            return this.contentBuilder.toString();
        }
    }
}