package com.energyict.mdc.sap.soap.webservices.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractSapMessage {

    protected static final String REQUEST_ID_XML_NAME = "MessageHeader.ID";
    protected static final String UUID_XML_NAME = "MessageHeader.UUID";
    protected static final String UTILITIES_DEVICE_ID_XML_NAME = "UtilitiesDevice.UtilitiesDeviceID";

    private static final String AT_LEAST_ONE_OF = "at least one of";
    private Set<String> missingXmlNames = new HashSet<>();

    protected void addMissingField(String xmlName) {
        missingXmlNames.add(xmlName);
    }

    protected void addAtLeastOneMissingField(String... xmlName) {
        missingXmlNames.add(AT_LEAST_ONE_OF + '[' + Arrays.stream(xmlName).collect(Collectors.joining(", ", "'", "'")) + ']');
    }

    public String getMissingFields() {
        return missingXmlNames.stream().collect(Collectors.joining(", ", "'", "'"));
    }

    public boolean isValid() {
        return missingXmlNames.isEmpty();
    }
}
