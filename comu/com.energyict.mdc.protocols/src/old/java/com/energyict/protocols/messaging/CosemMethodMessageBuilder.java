package com.energyict.protocols.messaging;

import com.energyict.obis.ObisCode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Message builder class responsible of generating and parsing custom cosem messages.
 *
 * @author isabelle
 */
public class CosemMethodMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "CosemMethod";
    private static final String ATTRIBUTE_CLASSID = "classId";
    private static final String ATTRIBUTE_LOGICALNAME = "logicalName";
    private static final String ATTRIBUTE_METHODID = "messageId";
    private static final String TAG_PARAMETER = "Parameter";
    private static final String ATTRIBUTE_PARAMETERVALUE = "value";

    private int classId;
    private ObisCode logicalName;
    private int methodId;
    private List<Object> parameterValues = new ArrayList<>();


    /**
     * Get the classId
     *
     * @return the classId for this Cosem method
     */
    public int getClassId() {
        return classId;
    }

    /**
     * Set the classId
     *
     * @param classId the classId for this Cosem method
     */
    public void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * Get the logicalName
     *
     * @return the logicalName for this Cosem method
     */
    public ObisCode getLogicalName() {
        return logicalName;
    }

    /**
     * Set the logicalName
     *
     * @param logicalName the logicalName for this Cosem method
     */
    public void setLogicalName(ObisCode logicalName) {
        this.logicalName = logicalName;
    }

    /**
     * Get the methodId
     *
     * @return the methodId for this Cosem method
     */
    public int getMethodId() {
        return methodId;
    }

    /**
     * Set the methodId
     *
     * @param methodId the methodId for this Cosem method
     */
    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    /**
     * Get the parameter values for this Cosem method
     *
     * @return the parameter values for this Cosem method
     */
    public List<Object> getParameterValues() {
        return parameterValues;
    }

    /**
     * Set the parameter values needed for this Cosem method
     *
     * @param parameterValues the parameter values needed for this Cosem method
     */
    public void setParameterValues(List<Object> parameterValues) {
        this.parameterValues = parameterValues;
    }

    protected static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    protected Object getParameterValue(int index) {
        return parameterValues.get(index);
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
        if (methodId == 0) {
            throw new IllegalArgumentException("<html>MessageId needed</html>");
        }
        StringBuilder contentBuilder = new StringBuilder("<");
        contentBuilder.append(MESSAGETAG);
        addAttribute(contentBuilder, ATTRIBUTE_CLASSID, classId);
        addAttribute(contentBuilder, ATTRIBUTE_LOGICALNAME, logicalName);
        addAttribute(contentBuilder, ATTRIBUTE_METHODID, methodId);
        contentBuilder.append(">");
        int size = parameterValues.size();
        for (int i = 0; i < size; i++) {
            contentBuilder.append(System.getProperty("line.separator"));
            contentBuilder.append("<");
            contentBuilder.append(TAG_PARAMETER);
            addAttribute(contentBuilder, ATTRIBUTE_PARAMETERVALUE, getParameterValue(i));
            contentBuilder.append("/>");
        }
        contentBuilder.append(System.getProperty("line.separator")).append("</");
        contentBuilder.append(MESSAGETAG);
        contentBuilder.append(">");
        return contentBuilder.toString();
    }

    public String getDescription() {
        return MESSAGETAG + " " +
                "ClassId='" + classId + "', " +
                "LogicalName='" + logicalName + "', " +
                "MethodId='" + methodId + "', ";
    }

    // Parsing the message use SAX

    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new CosemMethodMessageHandler((CosemMethodMessageBuilder) builder, getMessageNodeTag());
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
        MessageBuilder builder = new CosemMethodMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    public static class CosemMethodMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        private CosemMethodMessageBuilder msgBuilder;
        private List<Object> parameterValues;

        public CosemMethodMessageHandler(CosemMethodMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
            this.parameterValues = new ArrayList<>();
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
                        // methodId attribute
                        String methodId = atts.getValue(namespaceURI, ATTRIBUTE_METHODID);
                        if (methodId != null) {
                            msgBuilder.setMethodId(Integer.parseInt(methodId));
                        }
                    }
                }
                if (TAG_PARAMETER.equals(localName)) {
                    String parameterValue = atts.getValue(namespaceURI, ATTRIBUTE_PARAMETERVALUE);
                    if (parameterValue != null) {
                        this.parameterValues.add(parameterValue);
                    }
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (getMessageTag().equals(localName)) {
                msgBuilder.setParameterValues(this.parameterValues);
            }
        }

    }

}
