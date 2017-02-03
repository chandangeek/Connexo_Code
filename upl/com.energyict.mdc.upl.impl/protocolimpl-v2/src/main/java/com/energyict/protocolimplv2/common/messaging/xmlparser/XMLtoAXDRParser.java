package com.energyict.protocolimplv2.common.messaging.xmlparser;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Created by cisac on 3/02/2017.
 */
public class XMLtoAXDRParser {

    private AbstractDataType rootElement = null;

    public AbstractDataType parseXml(byte[] fileInBytes){
        try{
            DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(fileInBytes));
            doc.normalizeDocument();
            Element rootEl = doc.getDocumentElement();
            rootElement = getDlmsDataType(rootEl, null);
            NodeList childNodes = rootEl.getChildNodes();
            parseChildNodes(childNodes, rootElement);
            return rootElement;
        } catch (Exception e) {
            new ProtocolException(e, "Unable to convert given xml file to an AbstractDataType dlms object");
        }
        return null;
    }

    private AbstractDataType parseChildNodes(NodeList childNodes, AbstractDataType parentObject){
        AbstractDataType childNode = null;
        for(int i = 0; i < childNodes.getLength(); i++) {

            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                childNode = getDlmsDataType(child, null);
                if(child.getChildNodes().getLength() != 0) {
                    parseChildNodes(child.getChildNodes(), childNode);
                }
                addDataTypeToParent(parentObject, childNode);
            }
        }

        return childNode;
    }

    private AbstractDataType getDlmsDataType(Node node, AbstractDataType parentObject){
        DLMSDataTypes dataTypeName = DLMSDataTypes.getFromDescription(node.getNodeName());
        switch (dataTypeName){
            case ARRAY:
                return addDataTypeToParent(parentObject, new Array());
            case STRUCTURE:
                return addDataTypeToParent(parentObject, new Structure());
            case OCTET_STRING:
                byte[] octetStringValue = ProtocolTools.getBytesFromHexString(node.getAttributes().getNamedItem("value").getNodeValue(), "");
                return addDataTypeToParent(parentObject, new OctetString(octetStringValue));
            case UNSIGNED:
                int unsignedValue = Integer.parseInt(node.getAttributes().getNamedItem("value").getNodeValue());
                return addDataTypeToParent(parentObject, new Unsigned8(unsignedValue));
            case LONG_UNSIGNED:
                int longUnsignedValue = Integer.parseInt(node.getAttributes().getNamedItem("value").getNodeValue());
                return addDataTypeToParent(parentObject, new Unsigned16(longUnsignedValue));
            //TODO: extend it by parsing more data types
            default:
                return parentObject;
        }

    }

    private AbstractDataType addDataTypeToParent(AbstractDataType parentObject, AbstractDataType child){
        if(parentObject == null){
            return child;
        } else if(parentObject.isArray()) {
            parentObject.getArray().addDataType(child);
        } else if(parentObject.isStructure()) {
            parentObject.getStructure().addDataType(child);
        }
        return parentObject;
    }

}
