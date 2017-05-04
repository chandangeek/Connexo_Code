package com.energyict.protocols.messaging;

import com.energyict.obis.ObisCode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Message builder class responsible of generating and parsing Cosem attribute messages.
 *
 * @author isabelle
 */
public class CosemAttributeMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "CosemAttribute";
    private static final String ATTRIBUTE_CLASSID = "classId";
    private static final String ATTRIBUTE_LOGICALNAME = "logicalName";
    private static final String ATTRIBUTE_ATTRIBUTEID = "attributeId";
    private static final String ATTRIBUTE_ATTRIBUTEVALUE = "value";

    private int classId;
    private ObisCode logicalName;
    private int attributeId;
    private Object value;

    /**
     * Get the class id
     *
     * @return the class id
     */
    public int getClassId() {
        return this.classId;
    }

    /**
     * Set the class id
     *
     * @param classId The classId to be set
     */
    public void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * Get the logical name
     *
     * @return the logical name
     */
    public ObisCode getLogicalName() {
        return this.logicalName;
    }

    /**
     * Set the logicalName
     *
     * @param logicalName The logicalName to be set
     */
    public void setLogicalName(ObisCode logicalName) {
        this.logicalName = logicalName;
    }

    /**
     * Get the attributeId
     *
     * @return the attributeId
     */
    public int getAttributeId() {
        return this.attributeId;
    }

    /**
     * Set the attributeId
     *
     * @param attributeId The attributeId to be set
     */
    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * Get the value of the attribute
     *
     * @return the value of the attribute
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Set the value
     *
     * @param value The value to be set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    protected static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    /**
     * {@inheritDoc}
     */
    protected String getMessageContent() {
        if (classId == 0) {
            throw new IllegalArgumentException("<html>ClassId needed</html>");
        }
        if (logicalName == null) {
            throw new IllegalArgumentException("<html>LogicalName needed</html>");
        }
        if (attributeId == 0) {
            throw new IllegalArgumentException("<html>AttributeId needed</html>");
        }
        if (value == null) {
            throw new IllegalArgumentException("<html>Value needed</html>");
        }
        StringBuilder contentBuilder = new StringBuilder("<");
        contentBuilder.append(MESSAGETAG);
        addAttribute(contentBuilder, ATTRIBUTE_CLASSID, classId);
        addAttribute(contentBuilder, ATTRIBUTE_LOGICALNAME, logicalName);
        addAttribute(contentBuilder, ATTRIBUTE_ATTRIBUTEID, attributeId);
        addAttribute(contentBuilder, ATTRIBUTE_ATTRIBUTEVALUE, value);
        contentBuilder.append(">");
        contentBuilder.append(System.getProperty("line.separator")).append("</");
        contentBuilder.append(MESSAGETAG);
        contentBuilder.append(">");
        return contentBuilder.toString();
    }

    public String getDescription() {
        return MESSAGETAG + " " +
                "ClassId='" + classId + "', " +
                "LogicalName='" + logicalName + "', " +
                "AttributeId='" + attributeId + "', " +
                "Value='" + value + "', ";
    }

    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new CosemAttributeMessageHandler((CosemAttributeMessageBuilder) builder, getMessageNodeTag());
    }

    /**
     * Returns a fully initialized CosemMethodMessageBuilder starting from the given xml String
     *
     * @param xmlString string to initialize the messagebuilder
     * @return  a initialized CosemMethodMessageBuilder if the message is a correct 'CosemMethod' message
     * @throws SAXException, IOException if the given string is wrong xml
     *          or if the message is not a 'CosemMethod' message
     */
    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new CosemAttributeMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    public static class CosemAttributeMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        private CosemAttributeMessageBuilder msgBuilder;

        public CosemAttributeMessageHandler(CosemAttributeMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        protected AbstractMessageBuilder getMessageBuilder() {
            return msgBuilder;
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
            if (messageTagEncountered()) {
                if (getMessageTag().equals(localName)) {
                    if (atts != null) {
                        // classId attribute
                        String classId = atts.getValue(namespaceURI, ATTRIBUTE_CLASSID);
                        if (classId != null) {
                            msgBuilder.setClassId(Integer.parseInt(classId));
                        }
                        // logical name  attribute
                        String logicalName = atts.getValue(namespaceURI, ATTRIBUTE_LOGICALNAME);
                        if (logicalName != null) {
                            msgBuilder.setLogicalName(ObisCode.fromString(logicalName));
                        }
                        // attributeId attribute
                        String attributeId = atts.getValue(namespaceURI, ATTRIBUTE_ATTRIBUTEID);
                        if (attributeId != null) {
                            msgBuilder.setAttributeId(Integer.parseInt(attributeId));
                        }
                        // value attribute
                        String value = atts.getValue(namespaceURI, ATTRIBUTE_ATTRIBUTEVALUE);
                        if (value != null) {
                            msgBuilder.setValue(value);
                        }
                    }
                }

            }
        }

    }

}
