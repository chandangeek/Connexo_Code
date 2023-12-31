package com.energyict.protocolimplv2.messages.convertor.utils;

import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 3/05/13
 * Time: 10:03
 */
public class LoadProfileMessageUtils {

    private static final String ROOT_TAG = "LoadProfile";

    private static final String ProfileObisCodeTag = "LPObisCode";
    private static final String MeterSerialNumberTag = "MSerial";
    private static final String StartReadingTimeTag = "StartTime";
    private static final String EndReadingTimeTag = "EndTime";
    private static final String LoadProfileIdTag = "LPId";
    private static final String RtuRegistersTag = "RtuRegs";
    private static final String RegisterTag = "Reg";
    private static final String RegisterObiscodeTag = "OC";
    private static final String RtuRegisterSerialNumber = "ID";
    private static final String RtuRegisterId = "RegID";
    private static final String ChannelInfosTag = "Channels";
    private static final String ChannelTag = "Ch";
    private static final String ChannelIdTag = "Id";
    private static final String ChannelNametag = "Name";
    private static final String ChannelMeterIdentifier = "ID";
    private static final String ChannelUnitTag = "Unit";
    private static final String ChannelReadingTypeMRID = "MRID";

    /**
     * Hide utility class constructor.
     */
    private LoadProfileMessageUtils() {
    }

    public static String createPartialLoadProfileMessage(String messageTag, String fromDate, String toDate, final String loadProfileXml) {
        try {
            final Document loadProfileDocument = XmlUtils.loadXMLDocumentFromString(loadProfileXml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            final Element loadProfileDocumentElement = loadProfileDocument.getDocumentElement();
            Element root = createRootElementWithAttributesFromOtherDocument(messageTag, fromDate, toDate, document, loadProfileDocumentElement);
            final Node channelInfos = loadProfileDocumentElement.getElementsByTagName(ChannelInfosTag).item(0);
            Element channels = createChannelElementFromOtherDocument(document, channelInfos);
            root.appendChild(channels);

            document.appendChild(root);
            return XmlUtils.getXmlWithoutDocType(document);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Element createRootElementWithAttributesFromOtherDocument(String messageTag, String fromDate, String toDate, Document document, Element loadProfileDocumentElement) {
        Element root = document.createElement(messageTag);
        root.setAttribute(ProfileObisCodeTag, loadProfileDocumentElement.getAttributes().getNamedItem(ProfileObisCodeTag).getNodeValue());
        root.setAttribute(MeterSerialNumberTag, loadProfileDocumentElement.getAttributes().getNamedItem(MeterSerialNumberTag).getNodeValue());
        root.setAttribute(StartReadingTimeTag, fromDate);
        if (toDate != null) {
            root.setAttribute(EndReadingTimeTag, toDate);
        }
        root.setAttribute(LoadProfileIdTag, loadProfileDocumentElement.getAttributes().getNamedItem(LoadProfileIdTag).getNodeValue());
        return root;
    }

    private static Element createChannelElementFromOtherDocument(Document document, Node channelInfos) {
        Element channels = document.createElement(ChannelInfosTag);
        for (int i = 0; i < channelInfos.getChildNodes().getLength(); i++) {
            final Node item = channelInfos.getChildNodes().item(i);
            Element channelElement = document.createElement(ChannelTag);
            channelElement.setAttribute(ChannelIdTag, String.valueOf(item.getAttributes().getNamedItem(ChannelIdTag).getNodeValue()));
            channelElement.setAttribute(ChannelNametag, String.valueOf(item.getAttributes().getNamedItem(ChannelNametag).getNodeValue()));
            channelElement.setAttribute(ChannelUnitTag, String.valueOf(item.getAttributes().getNamedItem(ChannelUnitTag).getNodeValue()));
            channelElement.setAttribute(ChannelMeterIdentifier, String.valueOf(item.getAttributes().getNamedItem(ChannelMeterIdentifier).getNodeValue()));
            channelElement.setAttribute(ChannelReadingTypeMRID, String.valueOf(item.getAttributes().getNamedItem(ChannelReadingTypeMRID).getNodeValue()));
            channels.appendChild(channelElement);
        }
        return channels;
    }

    private static Element createRegisterElementFromOtherDocument(Document document, Node registersList) {
        Element registers = document.createElement(RtuRegistersTag);
        for (int i = 0; i < registersList.getChildNodes().getLength(); i++) {
            final Node item = registersList.getChildNodes().item(i);
            Element registerElement = document.createElement(RegisterTag);
            registerElement.setAttribute(RegisterObiscodeTag, String.valueOf(item.getAttributes().getNamedItem(RegisterObiscodeTag).getNodeValue()));
            registerElement.setAttribute(RtuRegisterSerialNumber, String.valueOf(item.getAttributes().getNamedItem(RtuRegisterSerialNumber).getNodeValue()));
            registerElement.setAttribute(RtuRegisterId, String.valueOf(item.getAttributes().getNamedItem(RtuRegisterId).getNodeValue()));
            registers.appendChild(registerElement);
        }
        return registers;
    }

    public static String createLoadProfileRegisterMessage(String messageTag, String fromDate, String loadProfileXml) {
        try {
            final Document loadProfileDocument = XmlUtils.loadXMLDocumentFromString(loadProfileXml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            final Element loadProfileDocumentElement = loadProfileDocument.getDocumentElement();
            Element root = createRootElementWithAttributesFromOtherDocument(messageTag, fromDate, null, document, loadProfileDocumentElement);
            final Node registerList = loadProfileDocumentElement.getElementsByTagName(RtuRegistersTag).item(0);
            Element registers = createRegisterElementFromOtherDocument(document, registerList);
            root.appendChild(registers);

            document.appendChild(root);
            return XmlUtils.getXmlWithoutDocType(document);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * We format the LoadProfile is such a way it is usable in the
     * {@link com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder} and
     * {@link com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder}
     *
     * @param loadProfile the LoadProfile to format
     * @return the formatted loadProfile
     */
    public static String formatLoadProfile(final LoadProfile loadProfile, final LoadProfileExtractor extractor) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(ROOT_TAG);

            root.setAttribute(ProfileObisCodeTag, extractor.specDeviceObisCode(loadProfile));
            root.setAttribute(MeterSerialNumberTag, extractor.deviceSerialNumber(loadProfile));
            root.setAttribute(LoadProfileIdTag, extractor.id(loadProfile));

            // append the channels
            root.appendChild(convertToChannelsElement(extractor.channels(loadProfile), document));

            // append the registers
            root.appendChild(convertToRegisterElements(extractor.registers(loadProfile), document));

            document.appendChild(root);
            return XmlUtils.documentToString(document);
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Node convertToRegisterElements(List<LoadProfileExtractor.Register> allRegisters, Document document) {
        Element registers = document.createElement(RtuRegistersTag);
        for (LoadProfileExtractor.Register register : allRegisters) {
            Element registerElement = document.createElement(RegisterTag);
            registerElement.setAttribute(RegisterObiscodeTag, register.obisCode());
            registerElement.setAttribute(RtuRegisterSerialNumber, register.deviceSerialNumber());
            registerElement.setAttribute(RtuRegisterId, String.valueOf(register.getRegisterId()));
            registers.appendChild(registerElement);
        }
        return registers;
    }

    private static Node convertToChannelsElement(List<LoadProfileExtractor.Channel> allChannels, Document document) {
        Element channels = document.createElement(ChannelInfosTag);
        int counter = 0;
        for (LoadProfileExtractor.Channel channel : allChannels) {
            Element channelElement = document.createElement(ChannelTag);
            channelElement.setAttribute(ChannelIdTag, String.valueOf(counter++));
            channelElement.setAttribute(ChannelNametag, channel.obisCode());
            channelElement.setAttribute(ChannelUnitTag, channel.unit());
            channelElement.setAttribute(ChannelMeterIdentifier, channel.deviceSerialNumber());
            channelElement.setAttribute(ChannelReadingTypeMRID, channel.MRID());
            channels.appendChild(channelElement);
        }
        return channels;
    }
}