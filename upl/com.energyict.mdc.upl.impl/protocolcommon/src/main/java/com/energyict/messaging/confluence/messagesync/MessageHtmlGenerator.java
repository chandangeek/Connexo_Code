package com.energyict.messaging.confluence.messagesync;

import com.energyict.comserver.adapters.common.DefaultMessageAdapterMappingFactoryProvider;
import com.energyict.comserver.adapters.common.DefaultProtocolDescriptionAdapterMappingFactoryProvider;
import com.energyict.comserver.adapters.common.MessageAdapterMappingFactoryProvider;
import com.energyict.comserver.adapters.common.ProtocolDescriptionAdapterMappingFactory;
import com.energyict.comserver.adapters.meterprotocol.MeterProtocolMessageAdapter;
import com.energyict.comserver.adapters.smartmeterprotocol.SmartMeterProtocolMessageAdapter;
import com.energyict.cpo.Environment;
import com.energyict.cpo.IdBusinessObject;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecPossibleValues;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdw.core.ClassTypes;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.ObjectType;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.SmartMeterProtocol;
import com.energyict.protocolimplv2.messages.DeviceMessageCategories;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyrights EnergyICT
 * Date: 14/03/14
 * Time: 16:06
 * Author: khe
 */
public class MessageHtmlGenerator {

    public Document createAllMessages() throws ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        doBefore();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element table = doc.createElement("table");
        doc.appendChild(table);

        Element tableBody = doc.createElement("tbody");
        table.appendChild(tableBody);

        Element tableRow1 = doc.createElement("tr");
        tableBody.appendChild(tableRow1);

        Element idHeader = doc.createElement("th");
        idHeader.setTextContent("ID");
        tableRow1.appendChild(idHeader);

        Element nameHeader = doc.createElement("th");
        nameHeader.setTextContent("Name");
        tableRow1.appendChild(nameHeader);

        Element attributesHeader = doc.createElement("th");
        attributesHeader.setTextContent("Attributes");
        tableRow1.appendChild(attributesHeader);

        for (DeviceMessageCategory category : DeviceMessageCategories.values()) {

            Element tableRow2 = doc.createElement("tr");
            tableBody.appendChild(tableRow2);

            Element categoryHeader = doc.createElement("th");
            tableRow2.appendChild(categoryHeader);
            categoryHeader.setTextContent(category.getName() + ": " + category.getDescription());
            categoryHeader.setAttribute("colspan", "3");

            for (DeviceMessageSpec message : category.getMessageSpecifications()) {

                Element tableRow3 = doc.createElement("tr");
                tableBody.appendChild(tableRow3);

                Element id = doc.createElement("td");
                id.setTextContent(category.getId() + "." + message.getMessageId());
                tableRow3.appendChild(id);

                Element name = doc.createElement("td");
                name.setTextContent(message.getName());
                tableRow3.appendChild(name);

                Element attributes = doc.createElement("td");
                tableRow3.appendChild(attributes);

                if (!message.getPropertySpecs().isEmpty()) {
                    Element table2 = doc.createElement("table");
                    table2.setAttribute("style", "table-layout:fixed;");
                    attributes.appendChild(table2);

                    Element tbody2 = doc.createElement("tbody");
                    table2.appendChild(tbody2);

                    Element attributesHeader2 = doc.createElement("tr");
                    tbody2.appendChild(attributesHeader2);

                    Element attributeNameHeader = doc.createElement("th");
                    attributeNameHeader.setTextContent("Name");
                    attributesHeader2.appendChild(attributeNameHeader);

                    Element attributeType = doc.createElement("th");
                    attributeType.setTextContent("Type");
                    attributesHeader2.appendChild(attributeType);

                    Element attributeDefaultValue = doc.createElement("th");
                    attributeDefaultValue.setTextContent("Default value");
                    attributesHeader2.appendChild(attributeDefaultValue);

                    List<PropertySpec> propertySpecs = message.getPropertySpecs();
                    for (PropertySpec propertySpec : propertySpecs) {
                        Element attributeValues = doc.createElement("tr");
                        tbody2.appendChild(attributeValues);

                        Element attributeName = doc.createElement("td");
                        String translatedAttributeName = translate(propertySpec.getName());
                        attributeName.setTextContent(translatedAttributeName);
                        attributeValues.appendChild(attributeName);

                        Element type = doc.createElement("td");
                        String attributeTypeString = getAttributeType(propertySpec);
                        type.setTextContent(attributeTypeString);
                        attributeValues.appendChild(type);

                        Element defaultValue = doc.createElement("td");
                        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
                        String propertyDefault = "(none)";
                        if (possibleValues != null && possibleValues.getDefault() != null) {
                            propertyDefault = possibleValues.getDefault().toString();
                        }
                        defaultValue.setTextContent(propertyDefault);
                        attributeValues.appendChild(defaultValue);
                    }
                }
            }
        }
        return doc;
    }


    /**
     * Create a full HTML description of the given protocol java class (= args).
     * If the old HTML table is provided, the old message descriptions are reused for the new table result.
     */
    public Document createMessagesForProtocol(String oldTable, String[] args) throws ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        doBefore();

        oldTable = oldTable.replace("<p>", "");
        oldTable = oldTable.replace("</p>", "");
        oldTable = StringEscapeUtils.unescapeHtml4(oldTable);  //Replace HTML entities with the proper characters
        while (oldTable.contains("> ")) {   //Replace spaces between the tags
            oldTable = oldTable.replace("> ", ">");
        }

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element table = doc.createElement("table");
        doc.appendChild(table);

        Element tableBody = doc.createElement("tbody");
        table.appendChild(tableBody);

        Element tableRow1 = doc.createElement("tr");
        tableBody.appendChild(tableRow1);

        Element idHeader = doc.createElement("th");
        idHeader.setTextContent("ID");
        tableRow1.appendChild(idHeader);

        Element nameHeader = doc.createElement("th");
        nameHeader.setTextContent("Name");
        tableRow1.appendChild(nameHeader);

        Element attributesHeader = doc.createElement("th");
        attributesHeader.setTextContent("Description & attributes");
        tableRow1.appendChild(attributesHeader);

        List<DeviceMessageSpec> allSupportedMessages = getSupportedMessages(args);
        List<DeviceMessageCategory> categories = getSupportedCategories(allSupportedMessages);

        for (DeviceMessageCategory category : categories) {

            Element tableRow2 = doc.createElement("tr");
            tableBody.appendChild(tableRow2);

            Element categoryHeader = doc.createElement("th");
            tableRow2.appendChild(categoryHeader);

            categoryHeader.setTextContent(category.getName());
            categoryHeader.setAttribute("colspan", "3");

            for (DeviceMessageSpec message : getMessagesForCategory(allSupportedMessages, category)) {

                Element tableRow3 = doc.createElement("tr");
                tableBody.appendChild(tableRow3);

                Element id = doc.createElement("td");
                id.setTextContent(category.getId() + "." + message.getMessageId());
                tableRow3.appendChild(id);

                Element name = doc.createElement("td");
                name.setTextContent(message.getName());
                tableRow3.appendChild(name);

                Element attributes = doc.createElement("td");
                tableRow3.appendChild(attributes);

                Element messageDescriptionText = doc.createElement("p");
                messageDescriptionText.setTextContent(getCurrentMessageDescription(oldTable, message));
                attributes.appendChild(messageDescriptionText);

                if (!message.getPropertySpecs().isEmpty()) {
                    Element table2 = doc.createElement("table");
                    attributes.appendChild(table2);

                    Element tbody2 = doc.createElement("tbody");
                    table2.appendChild(tbody2);

                    Element attributesHeader2 = doc.createElement("tr");
                    tbody2.appendChild(attributesHeader2);

                    Element attributeNameHeader = doc.createElement("th");
                    attributeNameHeader.setTextContent("Name");
                    attributesHeader2.appendChild(attributeNameHeader);

                    Element attributeType = doc.createElement("th");
                    attributeType.setTextContent("Type");
                    attributesHeader2.appendChild(attributeType);

                    Element attributeDefaultValue = doc.createElement("th");
                    attributeDefaultValue.setTextContent("Default value");
                    attributesHeader2.appendChild(attributeDefaultValue);

                    Element attributeDescription = doc.createElement("th");
                    attributeDescription.setTextContent("Description");
                    attributesHeader2.appendChild(attributeDescription);


                    List<PropertySpec> propertySpecs = message.getPropertySpecs();
                    for (PropertySpec propertySpec : propertySpecs) {
                        Element attributeValues = doc.createElement("tr");
                        tbody2.appendChild(attributeValues);

                        Element attributeName = doc.createElement("td");
                        String translatedAttributeName = translate(propertySpec.getName());
                        attributeName.setTextContent(translatedAttributeName);
                        attributeValues.appendChild(attributeName);

                        Element type = doc.createElement("td");
                        String attributeTypeString = getAttributeType(propertySpec);
                        type.setTextContent(attributeTypeString);
                        attributeValues.appendChild(type);

                        Element defaultValue = doc.createElement("td");
                        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
                        String propertyDefault = "(none)";
                        if (possibleValues != null && possibleValues.getDefault() != null) {
                            propertyDefault = possibleValues.getDefault().toString();
                        }
                        defaultValue.setTextContent(propertyDefault);
                        attributeValues.appendChild(defaultValue);

                        Element description = doc.createElement("td");

                        String attrDescription = getOldAttributeDescription(oldTable, message, translatedAttributeName, attributeTypeString, propertyDefault);

                        description.setTextContent(attrDescription);
                        attributeValues.appendChild(description);
                    }
                }
            }
        }
        return doc;
    }

    /**
     * Find the current description in the HTML table for a given message
     */
    private String getCurrentMessageDescription(String oldTable, DeviceMessageSpec message) {
        String xmlId = "<td>" + message.getCategory().getId() + "." + message.getMessageId() + "</td>";
        String xmlName = "<td>" + message.getName() + "</td>";
        String descriptionPattern = "<td>(.*?)<";
        Pattern messageDescriptionPattern = Pattern.compile(xmlId + xmlName + descriptionPattern);
        Matcher matcher = messageDescriptionPattern.matcher(oldTable);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private String getOldAttributeDescription(String oldTable, DeviceMessageSpec message, String name, String type, String defaultValue) {
        String xmlId = "<td>" + message.getCategory().getId() + "." + message.getMessageId() + "</td>";
        String xmlName = "<td>" + message.getName() + "</td>";
        String descriptionPattern = "<td>(.*?)</table>";
        Pattern attributesTablePattern = Pattern.compile(xmlId + xmlName + descriptionPattern);
        Matcher matcher = attributesTablePattern.matcher(oldTable);

        String attributesTable;
        if (matcher.find()) {
            attributesTable = matcher.group(1);
        } else {
            return "";
        }

        String attributeNamePattern = "<td>" + name + "</td>";
        String attributeTypePattern = "<td>" + type + "</td>";
        String attributeDefaultValuePattern = "<td>" + defaultValue.replace("(", "\\(").replace(")", "\\)") + "</td>";
        Pattern attributesDescriptionPattern = Pattern.compile(attributeNamePattern + attributeTypePattern + attributeDefaultValuePattern + "<td>" + "(.*?)" + "</td></tr>");
        matcher = attributesDescriptionPattern.matcher(attributesTable);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private String getAttributeType(PropertySpec propertySpec) {
        if (IdBusinessObject.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            int factoryId = propertySpec.getDomain().getFactoryId();
            String text = ObjectType.getText(new ObjectType(factoryId, ObjectType.NO_SUBTYPE));
            if (text != null) {
                return text;
            } else {
                String[] split = ClassTypes.getType(factoryId).split("\\.");
                return split[split.length - 1];
            }
        } else {
            return propertySpec.getValueFactory().getValueType().getSimpleName();
        }
    }

    private String translate(String name1) {
        return Environment.getDefault().getTranslation(name1);
    }

    private List<DeviceMessageCategory> getSupportedCategories(List<DeviceMessageSpec> supportedMessages) {
        List<DeviceMessageCategory> result = new ArrayList<>();
        for (DeviceMessageSpec supportedMessage : supportedMessages) {
            if (!result.contains(supportedMessage.getCategory())) {
                result.add(supportedMessage.getCategory());
            }
        }
        return result;
    }

    private List<DeviceMessageSpec> getMessagesForCategory(List<DeviceMessageSpec> supportedMessages, DeviceMessageCategory category) {
        List<DeviceMessageSpec> result = new ArrayList<>();
        for (DeviceMessageSpec supportedMessage : supportedMessages) {
            if (supportedMessage.getCategory().getId() == category.getId()) {
                result.add(supportedMessage);
            }
        }
        return result;
    }

    private List<DeviceMessageSpec> getSupportedMessages(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object protocolObject = getProtocolObject(args);
        List<DeviceMessageSpec> supportedMessages;
        MessageAdapterMappingFactoryProvider.INSTANCE.set(new DefaultMessageAdapterMappingFactoryProvider());

        if (protocolObject instanceof MeterProtocol) {
            MeterProtocol meterProtocol = (MeterProtocol) protocolObject;
            MeterProtocolMessageAdapter meterProtocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol);
            supportedMessages = meterProtocolMessageAdapter.getSupportedMessages();
        } else if (protocolObject instanceof SmartMeterProtocol) {
            SmartMeterProtocol smartMeterProtocol = (SmartMeterProtocol) protocolObject;
            SmartMeterProtocolMessageAdapter meterProtocolMessageAdapter = new SmartMeterProtocolMessageAdapter(smartMeterProtocol);
            supportedMessages = meterProtocolMessageAdapter.getSupportedMessages();
        } else if (protocolObject instanceof DeviceProtocol) {
            supportedMessages = ((DeviceProtocol) protocolObject).getSupportedMessages();
        } else {
            throw new IllegalArgumentException("Unsupported protocol class type: " + protocolObject.getClass().getSimpleName() + ". Expected MeterProtocol, SmartMeterProtocol or DeviceProtocol");
        }
        return supportedMessages;
    }

    private Object getProtocolObject(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Excepted one argument (protocol java class name), received " + args.length + " arguments!");
        }
        String javaClassName = args[0];
        Class<?> clazz = Class.forName(javaClassName);
        return clazz.newInstance();
    }

    public String getProtocolDescription(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Object protocolObject = getProtocolObject(args);
        if (protocolObject instanceof MeterProtocol || protocolObject instanceof  SmartMeterProtocol) {
            return getProtocolDescriptionAdapterMappingFactory().getUniqueProtocolDescriptionForDeviceProtocol(protocolObject.getClass().getCanonicalName());
        } else if (protocolObject instanceof DeviceProtocol) {
            return ((DeviceProtocol) protocolObject).getProtocolDescription();
        } else {
            throw new IllegalArgumentException("Unsupported protocol class type: " + protocolObject.getClass().getSimpleName() + ". Expected MeterProtocol, SmartMeterProtocol or DeviceProtocol");
        }
    }

    private ProtocolDescriptionAdapterMappingFactory getProtocolDescriptionAdapterMappingFactory() {
        return new DefaultProtocolDescriptionAdapterMappingFactoryProvider().getProtocolDescriptionAdapterMappingFactory();
    }

    private void doBefore() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("./eiserver.properties"));
        Environment.setDefault(properties);
        MeteringWarehouse.createBatchContext(false);
        DataVaultProvider.instance.set(new KeyStoreDataVaultProvider());
        RandomProvider.instance.set(new SecureRandomProvider());
    }
}