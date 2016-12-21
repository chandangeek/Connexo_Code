package com.energyict.messaging;

import com.energyict.cbo.BusinessException;
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
import java.util.Date;

/**
 * Message builder class responsible of generating and parsing messages.
 *
 * @return The {@link MessageBuilder} capable of generating and parsing messages.
 */
public abstract class AbstractMessageBuilder implements MessageBuilder {

    private String trackingId;
    private Date releaseDate = new Date();

    /**
     * Returns the tracking id, each message can be given a tracking id (optional) for further tracking possibilies.
     *
     * @return the tracking id.
     */
    public String getTrackingId() {
        return this.trackingId;
    }

    /**
     * Sets the tracking id
     *
     * @param trackingId the tracking id.
     */
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    /**
     * Returns the releaseDate, each message can be given a releaseDate,
     * the message won't be sent until this releaseDate has passes.
     * Until then the message still can be edited.
     *
     * @return the tracking id.
     */
    public Date getReleaseDate() {
        return this.releaseDate;
    }

    /**
     * Sets the releaseDate.
     *
     * @param releaseDate the releaseDate.
     */
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * Returns the message content, this a the xml String containing the content for the message.
     *
     * @return the xml String containing the content for the message.
     */
    protected abstract String getMessageContent() throws BusinessException;

    /**
     * Adds a child tag to the given {@link StringBuffer}.
     *
     * @param buf The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected void addChildTag(StringBuffer buf, String tagName, Object value) {
        buf.append(System.getProperty("line.separator"));
        buf.append("<");
        buf.append(tagName);
        buf.append(">");
        buf.append(value);
        buf.append("</");
        buf.append(tagName);
        buf.append(">");
    }

    /**
     * Adds a child tag to the given {@link StringBuilder}. As a StringBuilder is not synchronized as the {@link StringBuffer} is, this is a faster
     * alternative.
     *
     * @param builder The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected final void addChildTag(final StringBuilder builder, final String tagName, final Object value) {
        if (builder == null) {
            throw new NullPointerException("Cannot pass an null StringBuilder reference here.");
        }

        builder.append(System.getProperty("line.separator"));
        builder.append("<");
        builder.append(tagName);
        builder.append(">");
        builder.append(value);
        builder.append("</");
        builder.append(tagName);
        builder.append(">");
    }


    /**
     * Adds a an attribute tag to the given {@link StringBuffer}.
     *
     * @param buf       The string buffer to whose contents the attribute tag needs to be added.
     * @param attribute The attribute to be added
     * @param value     The contents (value) of the attribute.
     */
    protected void addAttribute(StringBuffer buf, String attribute, Object value) {
        buf.append(" ");
        buf.append(attribute);
        buf.append("=\"");
        buf.append(value);
        buf.append("\"");
    }

    /**
     * Adds a an attribute tag to the given {@link StringBuilder}.
     *
     * @param builder   The string builder to whose contents the attribute tag needs to be added.
     * @param attribute The attribute to be added
     * @param value     The contents (value) of the attribute.
     */
    protected void addAttribute(StringBuilder builder, String attribute, Object value) {
        builder.append(" ");
        builder.append(attribute);
        builder.append("=\"");
        builder.append(value);
        builder.append("\"");
    }

    // Parsing the message
    protected static InputSource getInputSource(String xmlString) {
        Reader reader = new CharArrayReader(xmlString.toCharArray());
        return new InputSource(reader);
    }

    // parsing using SAX
    public SAXParser getSaxParser() throws SAXException, IOException {
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

    // Parsing using SAX parser

    /**
     * Returns a fully initialized MessageBuilder starting from the given xml String
     *
     * @param xmlString string to initialize the messagebuilder
     * @throws ParserConfigurationException,SAXException,
     *          IOException if the given string is wrong xml
     *          or is not the correct message
     * @Return a initialized MessageBuilder if the message is a correct message
     */
    public void initFromXml(String xmlString) throws SAXException, IOException {
        SAXParser saxParser = getSaxParser();
        XMLReader reader = saxParser.getXMLReader();
        AdvancedMessageHandler handler = this.getMessageHandler(this);
        reader.setContentHandler(handler);
        reader.parse(AbstractMessageBuilder.getInputSource(xmlString));
    }

    protected abstract AdvancedMessageHandler getMessageHandler(MessageBuilder builder);

    public static abstract class AdvancedMessageHandler extends DefaultHandler {

        private Locator locator;
        private String messageTag;
        private boolean messageTagEncountered;
        //private Object currentValue = null;

        /**
         * Strign builder used to build content in the current tag.
         */
        private final StringBuilder contentBuilder = new StringBuilder();

        public AdvancedMessageHandler(String messageTag) {
            this.messageTag = messageTag;
            if (this.messageTag == null) {
                throw new IllegalArgumentException("Parameter messageTaf cannot be null");
            }
        }

        public void startDocument() {
            messageTagEncountered = false;
        }

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
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

            if (characters != null && !characters.trim().equals("")) {
                this.contentBuilder.append(characters.trim());
            }
        }

        protected String getMessageTag() {
            return messageTag;
        }

        protected boolean messageTagEncountered() {
            return messageTagEncountered;
        }

        protected Locator getDocumentLocator() {
            return locator;
        }

        protected Object getCurrentValue() {
            return this.contentBuilder.toString();
        }

        protected abstract AbstractMessageBuilder getMessageBuilder();

    }

}
