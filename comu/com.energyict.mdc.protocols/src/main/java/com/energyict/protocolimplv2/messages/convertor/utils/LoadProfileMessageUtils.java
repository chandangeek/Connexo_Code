package com.energyict.protocolimplv2.messages.convertor.utils;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.BaseChannel;

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
    private static final String ChannelInfosTag = "Channels";
    private static final String ChannelTag = "Ch";
    private static final String ChannelIdTag = "Id";
    private static final String ChannelNametag = "Name";
    private static final String ChannelMeterIdentifier = "ID";
    private static final String ChannelUnitTag = "Unit";

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
            throw new ApplicationException(e);
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
            throw new ApplicationException(e);
        }
    }

    public static String formatLoadProfile(final LoadProfile loadProfile, TopologyService topologyService) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(ROOT_TAG);

            root.setAttribute(ProfileObisCodeTag, loadProfile.getDeviceObisCode().toString());
            root.setAttribute(MeterSerialNumberTag, loadProfile.getDevice().getSerialNumber());
            root.setAttribute(LoadProfileIdTag, String.valueOf(loadProfile.getId()));

            // append the channels
            List<Channel> allChannels = topologyService.getAllChannels(loadProfile);
            root.appendChild(convertToChannelsElement(allChannels, document));

            // append the registers
            root.appendChild(convertToRegisterElements(allChannels, document));

            document.appendChild(root);
            return XmlUtils.documentToString(document);
        } catch (ParserConfigurationException e) {
            throw new ApplicationException(e);
        }
    }

    private static Node convertToRegisterElements(List<Channel> allChannels, Document document) {
        Element registers = document.createElement(RtuRegistersTag);
        for (BaseChannel channel : allChannels) {
            Element registerElement = document.createElement(RegisterTag);
            registerElement.setAttribute(RegisterObiscodeTag, channel.getRegisterTypeObisCode().toString());
            registerElement.setAttribute(RtuRegisterSerialNumber, channel.getDevice().getSerialNumber());
            registers.appendChild(registerElement);
        }
        return registers;
    }

    private static Node convertToChannelsElement(List<Channel> allChannels, Document document) {
        Element channels = document.createElement(ChannelInfosTag);
        int counter = 0;
        for (BaseChannel channel : allChannels) {
            Element channelElement = document.createElement(ChannelTag);
            channelElement.setAttribute(ChannelIdTag, String.valueOf(counter++));
            channelElement.setAttribute(ChannelNametag, channel.getRegisterTypeObisCode().toString());
            channelElement.setAttribute(ChannelUnitTag, channel.getUnit().toString());
            channelElement.setAttribute(ChannelMeterIdentifier, channel.getDevice().getSerialNumber());
            channels.appendChild(channelElement);
        }
        return channels;
    }

    /**
     * Privately hidden constructor, please leave me alone
     */
    private LoadProfileMessageUtils() {
    }
}
