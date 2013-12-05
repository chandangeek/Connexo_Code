package com.energyict.dlms.xmlparsing;

import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ObjectReference;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2011
 * Time: 15:31:20
 */
public class XmlToDlms {

    public static final String SetRequestNormalTag = "SetRequestNormal";
    public static final String AttributeDescriptorTag = "AttributeDescriptor";
    public static final String ClassIdTag = "ClassId";
    public static final String InstanceIdTag = "InstanceId";
    public static final String AttributeIdTag = "AttributeId";
    public static final String ValueTag = "Value";

    public static final String ArrayTag = "Array";
    public static final String BitString = "BitString";
    public static final String StructureTag = "Structure";
    public static final String Unsigned8Tag = "Unsigned8";
    public static final String Unsigned16Tag = "Unsigned16";
    public static final String Unsigned32Tag = "Unsigned32";
    public static final String Integer8Tag = "Integer8";
    public static final String Integer16Tag = "Integer16";
    public static final String Integer32Tag = "Integer32";
    public static final String Integer64Tag = "Integer64";
    public static final String OctetStringTag = "OctetString";
    public static final String TypeEnumTag = "TypeEnum";
    public static final String VisibleStringTag = "VisibleString";

    public static final String ValueAttribute = "Value";
    public static final String QuantityAttribute = "Qty";

    private static final int State_Idle = 0;
    private static final int State_SetRequestNormal = 1;
    private static final int State_AttributeDescriptor = 2;
    private static final int State_Value = 3;   // need to create rootElement

    private final ProtocolLink protocolLink;

    private List<GenericDataToWrite> gDataToWrite = new ArrayList<GenericDataToWrite>();
    private List<AbstractDataType> combinedDataTypes = new ArrayList<AbstractDataType>();
    private int combinedCounterIndex = 0;

    private int state = State_Idle;

    public XmlToDlms(final ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }

    public List<GenericDataToWrite> parseSetRequests(String xmlString) throws IOException, SAXException {

        SAXParser saxParser = getSaxParser();
        XMLReader reader = saxParser.getXMLReader();
        XmlDlmsHandler handler = new XmlDlmsHandler();
        reader.setContentHandler(handler);
        reader.parse(getInputSource(xmlString));

        return this.gDataToWrite;
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

    // Parsing the message

    protected static InputSource getInputSource(String xmlString) {
        Reader reader = new CharArrayReader(xmlString.toCharArray());
        return new InputSource(reader);
    }

    private void addGenericDataToWrite(byte[] logicalName, int classId, int attributeId, AbstractDataType objectToWrite) {
        gDataToWrite.add(new GenericDataToWrite(new GenericWrite(protocolLink, new ObjectReference(logicalName, classId), attributeId), objectToWrite.getBEREncodedByteArray()));
    }

    private final class XmlDlmsHandler extends DefaultHandler {

        private AbstractDataType adt;
        private int classId = 0;
        private byte[] longName;
        private int attributeId = 0;

        /**
         * Receive notification of the start of an element.
         * <p/>
         * <p>By default, do nothing.  Application writers may override this
         * method in a subclass to take specific actions at the start of
         * each element (such as allocating a new tree node or writing
         * output to a file).</p>
         *
         * @param uri        The Namespace URI, or the empty string if the
         *                   element has no Namespace URI or if Namespace
         *                   processing is not being performed.
         * @param localName  The local name (without prefix), or the
         *                   empty string if Namespace processing is not being
         *                   performed.
         * @param qName      The qualified name (with prefix), or the
         *                   empty string if qualified names are not available.
         * @param attributes The attributes attached to the element.  If
         *                   there are no attributes, it shall be an empty
         *                   Attributes object.
         * @throws org.xml.sax.SAXException Any SAX exception, possibly
         *                                  wrapping another exception.
         * @see org.xml.sax.ContentHandler#startElement
         */
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase(SetRequestNormalTag)) {
                state = State_SetRequestNormal;
            } else if (qName.equalsIgnoreCase(AttributeDescriptorTag)) {
                state = State_AttributeDescriptor;
            } else if (qName.equalsIgnoreCase(ClassIdTag)) {
                classId = Integer.valueOf(attributes.getValue(ValueAttribute));
            } else if (qName.equalsIgnoreCase(InstanceIdTag)) {
                longName = ParseUtils.hexStringToByteArray(attributes.getValue(ValueAttribute));
            } else if (qName.equalsIgnoreCase(AttributeIdTag)) {
                attributeId = Integer.valueOf(attributes.getValue(ValueAttribute));
            } else if (qName.equalsIgnoreCase(ValueTag)) {
                state = State_Value;
            } else if (qName.equalsIgnoreCase(ArrayTag)) {
                combinedDataTypes.add(new Array());
                combinedCounterIndex++;
            } else if (qName.equalsIgnoreCase(StructureTag)) {
                combinedDataTypes.add(new Structure());
                combinedCounterIndex++;
            } else if (qName.equalsIgnoreCase(BitString)) {
                adt = new BitString(Long.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(Unsigned8Tag)) {
                adt = new Unsigned8(Integer.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(Unsigned16Tag)) {
                adt = new Unsigned16(Integer.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(Unsigned32Tag)) {
                adt = new Unsigned32(Long.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(Integer8Tag)) {
                adt = new Integer8(Integer.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(Integer16Tag)) {
                adt = new Integer16(Integer.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(Integer32Tag)) {
                adt = new Integer32(Integer.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(Integer64Tag)) {
                adt = new Integer64(Long.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(OctetStringTag)) {
                adt = OctetString.fromByteArray(ParseUtils.hexStringToByteArray(attributes.getValue(ValueAttribute)), ParseUtils.hexStringToByteArray(attributes.getValue(ValueAttribute)).length);
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(TypeEnumTag)) {
                adt = new TypeEnum(Integer.valueOf(attributes.getValue(ValueAttribute)));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            } else if (qName.equalsIgnoreCase(VisibleStringTag)) {
                adt = new VisibleString(attributes.getValue(ValueAttribute));
                if (combinedCounterIndex > 0) {
                    addAbstractObjectToCombinedObject(adt, combinedDataTypes.get(combinedCounterIndex - 1));
                }
            }

        }

        private void addAbstractObjectToCombinedObject(final AbstractDataType adt, final AbstractDataType combinedObject) {
            if (combinedObject instanceof Array) {
                ((Array) combinedObject).addDataType(adt);
            } else if (combinedObject instanceof Structure) {
                ((Structure) combinedObject).addDataType(adt);
            }
        }

        private void endSimpleDataTag(AbstractDataType dataType) {
            if (combinedCounterIndex == 0) {
                addGenericDataToWrite(longName, classId, attributeId, dataType);
            }
        }

        /**
         * Receive notification of the end of an element.
         * <p/>
         * <p>By default, do nothing.  Application writers may override this
         * method in a subclass to take specific actions at the end of
         * each element (such as finalising a tree node or writing
         * output to a file).</p>
         *
         * @param uri       The Namespace URI, or the empty string if the
         *                  element has no Namespace URI or if Namespace
         *                  processing is not being performed.
         * @param localName The local name (without prefix), or the
         *                  empty string if Namespace processing is not being
         *                  performed.
         * @param qName     The qualified name (with prefix), or the
         *                  empty string if qualified names are not available.
         * @throws org.xml.sax.SAXException Any SAX exception, possibly
         *                                  wrapping another exception.
         * @see org.xml.sax.ContentHandler#endElement
         */
        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (qName.equalsIgnoreCase(SetRequestNormalTag)) {
                state = State_Idle;
            } else if (qName.equalsIgnoreCase(ValueTag)) {
                state = State_Idle;
            } else if (qName.equalsIgnoreCase(ArrayTag)) {
                combinedCounterIndex--;
                if (combinedCounterIndex == 0) {
                    endSimpleDataTag(combinedDataTypes.get(0));
                } else {
                    addAbstractObjectToCombinedObject(combinedDataTypes.get(combinedCounterIndex), combinedDataTypes.get(combinedCounterIndex - 1));
                }
                combinedDataTypes.remove(combinedCounterIndex);
            } else if (qName.equalsIgnoreCase(StructureTag)) {
                combinedCounterIndex--;
                if (combinedCounterIndex == 0) {
                    endSimpleDataTag(combinedDataTypes.get(0));
                } else {
                    addAbstractObjectToCombinedObject(combinedDataTypes.get(combinedCounterIndex), combinedDataTypes.get(combinedCounterIndex - 1));
                }
                combinedDataTypes.remove(combinedCounterIndex);
            } else if (qName.equalsIgnoreCase(BitString)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(Unsigned8Tag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(Unsigned16Tag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(Unsigned32Tag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(Integer8Tag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(Integer16Tag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(Integer32Tag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(Integer64Tag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(OctetStringTag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(TypeEnumTag)) {
                endSimpleDataTag(adt);
            } else if (qName.equalsIgnoreCase(VisibleStringTag)) {
                endSimpleDataTag(adt);
            }
        }
    }
}
