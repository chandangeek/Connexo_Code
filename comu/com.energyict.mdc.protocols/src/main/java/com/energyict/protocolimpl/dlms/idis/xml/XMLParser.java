package com.energyict.protocolimpl.dlms.idis.xml;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 21/12/12 - 16:35
 */

public class XMLParser {

    private static final String VALUE_ATTRIBUTE = "Value";
    private static final String DATA_INDEX_ATTRIBUTE = "DataIndex";

    private final CosemObjectFactory cosemObjectFactory;
    private final IDIS meterProtocol;

    private List<Object[]> parsedObjects = new ArrayList<Object[]>();

    public XMLParser(IDIS meterProtocol, CosemObjectFactory cosemObjectFactory) {
        this.meterProtocol = meterProtocol;
        this.cosemObjectFactory = cosemObjectFactory;
    }

    /**
     * Parse the XML to the corresponding object
     *
     * @param xml - the received MeterXML string
     * @throws javax.xml.parsers.ParserConfigurationException
     *
     * @throws org.xml.sax.SAXException when the xml parsing fails
     * @throws java.io.IOException      when the communication fails
     * @throws com.energyict.cbo.BusinessException
     *
     */
    public void parseXML(String xml) throws IOException {
        infoLog("Start parsing of the xml content.");
        Document document = createDocument(xml);
        parseNodes(document.getDocumentElement());
        infoLog("Completed the parsing of the xml content - " + parsedObjects.size() + (parsedObjects.size() != 1 ? " requests" : " request") + " found.");
    }

    private Document createDocument(String xml) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            xml = xml.replaceAll("\r\n", "").replaceAll(">\\s*<", "><");    // Make sure the xml string is well-formed, so replacing all "\r\n" and spaces between xml nodes.

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            throw new IOException("Failed to parse the xml document - the user file does not contain a valid XML document: " + e.getMessage());
        }
    }

    private void parseNodes(Node node) throws IOException {
        String nodeName = node.getNodeName();
        XMLTags xmlTag = XMLTags.forTag(nodeName);

        if (xmlTag == XMLTags.UNKNOWN) {
            throw new IOException("Unexpected XML node '" + node.getNodeName() + "'.");
        }

        switch (xmlTag.getType()) {
            case PARSE:
                if (nodeName.equals(XMLTags.SET_REQUEST_NORMAL.getTagName())) {
                    parseSetRequestNode(node);
                } else if ((nodeName.equals(XMLTags.ACTION_REQUEST_NORMAL.getTagName()))) {
                    parseActionRequestNode(node);
                } else {
                    throw new IOException("Unexpected XML node " + nodeName + " - We should never come here!");
                }
                break;

            case PARSE_CHILD_NODES:
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    parseNodes(childNodes.item(i));
                }
                break;

            case SKIP_NODE:
                break;
        }
    }

    private void parseSetRequestNode(Node node) throws IOException {
        NodeList childNodes = node.getChildNodes();

        Node attributeDescriptorNode = getNodeWithTag(childNodes, XMLTags.ATTRIBUTE_DESCRIPTOR);
        NodeList attributeDescriptorChildNodes = attributeDescriptorNode.getChildNodes();

        Node classIdNode = getNodeWithTag(attributeDescriptorChildNodes, XMLTags.CLASS_ID);
        Node instanceIdNode = getNodeWithTag(attributeDescriptorChildNodes, XMLTags.INSTANCE_ID);
        Node attributeIdNode = getNodeWithTag(attributeDescriptorChildNodes, XMLTags.ATTRIBUTE_ID);
        Node valueNode = getNodeWithTag(childNodes, XMLTags.VALUE);

        int classId = (int) getNumericValueAttributeFromNode(classIdNode);
        ObisCode instanceId = getObisCodeValueFromNode(instanceIdNode);
        int attributeId = (int) getNumericValueAttributeFromNode(attributeIdNode);

        GenericWrite genericWrite = cosemObjectFactory.getGenericWrite(instanceId, attributeId, classId);
        AbstractDataType value = getValueFromNode(valueNode.getFirstChild());

        Object[] parsedObject = {genericWrite, value};
        infoLog("Detected SetRequest for object " + instanceId + " (class " + classId + ") for attribute " + attributeId + ".");
        parsedObjects.add(parsedObject);
    }

    private void parseActionRequestNode(Node node) throws IOException {
        NodeList childNodes = node.getChildNodes();

        Node attributeDescriptorNode = getNodeWithTag(childNodes, XMLTags.METHOD_DESCRIPTOR);
        NodeList attributeDescriptorChildNodes = attributeDescriptorNode.getChildNodes();

        Node classIdNode = getNodeWithTag(attributeDescriptorChildNodes, XMLTags.CLASS_ID);
        Node instanceIdNode = getNodeWithTag(attributeDescriptorChildNodes, XMLTags.INSTANCE_ID);
        Node methodIdNode = getNodeWithTag(attributeDescriptorChildNodes, XMLTags.METHOD_ID);
        Node valueNode = getNodeWithTag(childNodes, XMLTags.METHOD_INV_PARAMETERS);

        int classId = (int) getNumericValueAttributeFromNode(classIdNode);
        ObisCode instanceId = getObisCodeValueFromNode(instanceIdNode);
        int methodId = (int) getNumericValueAttributeFromNode(methodIdNode);

        GenericInvoke genericInvoke = cosemObjectFactory.getGenericInvoke(instanceId, classId, methodId);
        AbstractDataType value = getValueFromNode(valueNode.getFirstChild());

        Object[] parsedObject = {genericInvoke, value};
        infoLog("Detected ActionRequest for object " + instanceId + " (class " + classId + ") for method " + methodId + ".");
        parsedObjects.add(parsedObject);
    }

    private Node getNodeWithTag(NodeList nodeList, XMLTags xmlTag) throws IOException {
        String nodeName = xmlTag.getTagName();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeName().equals(nodeName)) {
                return node;
            }
        }

        int iPos = parsedObjects.size() + 1;
        String pos = iPos > 1 ? iPos + "th" : (iPos + "st");
        throw new IOException("Expected a node with name '" + nodeName + "' while parsing the " + pos + " Set- or ActionRequest, but was not found. ");
    }

    private long getNumericValueAttributeFromNode(Node node) throws IOException {
        try {
            return Long.parseLong(getValueAttributeFromNode(node), 16);
        } catch (NumberFormatException e) {
            int iPos = parsedObjects.size() + 1;
            String pos = iPos > 1 ? iPos + "th" : (iPos + "st");
            throw new IOException("NumberFormatException while parsing the value of node '" + node.getNodeName() + "' (parsing the " + pos + " Set- or ActionRequest).");
        }
    }

    private String getValueAttributeFromNode(Node node) throws IOException {
        Node namedItem = node.getAttributes().getNamedItem(VALUE_ATTRIBUTE);
        if (namedItem != null) {
            return namedItem.getNodeValue();
        } else {
            int iPos = parsedObjects.size() + 1;
            String pos = iPos > 1 ? iPos + "th" : (iPos + "st");
            throw new IOException("Node '" + node.getNodeName() + "' does not have a value attribute (parsing the " + pos + " Set- or ActionRequest).");
        }
    }

    private ObisCode getObisCodeValueFromNode(Node node) throws IOException {
        String hexEncodedObisCode = getValueAttributeFromNode(node);
        try {
            byte[] obisCodeBytes = ProtocolTools.getBytesFromHexString(hexEncodedObisCode, "");
            return ObisCode.fromByteArray(obisCodeBytes);
        } catch (Exception e) {
            int iPos = parsedObjects.size() + 1;
            String pos = iPos > 1 ? iPos + "th" : (iPos + "st");
            throw new IOException("The value attribute of node '" + node.getNodeName() + "' does not contain a valid obiscode (parsing the " + pos + " Set- or ActionRequest).");
        }
    }

    private AbstractDataType getValueFromNode(Node node) throws IOException {
        switch (XMLTags.forTag(node.getNodeName())) {
            case BOOLEAN:
                return new BooleanObject(getValueAttributeFromNode(node).equalsIgnoreCase("01"));
            case OCTET_STRING:
                return new OctetString(ProtocolTools.getBytesFromHexString(getValueAttributeFromNode(node), ""));
            case ENUMERATED:
                return new TypeEnum((int) getNumericValueAttributeFromNode(node));
            case INTEGER8:
                return new Integer8((int) getNumericValueAttributeFromNode(node));
            case INTEGER16:
                return new Integer16((int) getNumericValueAttributeFromNode(node));
            case INTEGER32:
                return new Integer32((int) getNumericValueAttributeFromNode(node));
            case UNSIGNED8:
                return new Unsigned8((int) getNumericValueAttributeFromNode(node));
            case UNSIGNED16:
                return new Unsigned16((int) getNumericValueAttributeFromNode(node));
            case UNSIGNED32:
                return new Unsigned32(getNumericValueAttributeFromNode(node));
            case STRUCTURE:
                NodeList childNodes = node.getChildNodes();
                Structure structure = new Structure();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    AbstractDataType structElement = getValueFromNode(childNodes.item(i));
                    structure.addDataType(structElement);
                }
                return structure;
            case ARRAY:
                childNodes = node.getChildNodes();
                Array array = new Array();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    AbstractDataType arrayElement = getValueFromNode(childNodes.item(i));
                    array.addDataType(arrayElement);
                }
                return array;
            default:
                throw new IOException("Unexpected XML node " + node.getNodeName() + " - We should never come here!");
        }
    }

    private void infoLog(String message) {
        if (meterProtocol != null) {
            meterProtocol.getLogger().info(message);
        } else {
            System.out.println(message);
        }
    }

    public List<Object[]> getParsedObjects() {
        return parsedObjects;
    }
}