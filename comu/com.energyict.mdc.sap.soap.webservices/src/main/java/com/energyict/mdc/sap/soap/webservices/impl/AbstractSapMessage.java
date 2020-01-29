package com.energyict.mdc.sap.soap.webservices.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractSapMessage {

    protected static final String REQUEST_ID_XML_NAME = "MessageHeader.ID";
    protected static final String UUID_XML_NAME = "MessageHeader.UUID";
    protected static final String UTILITIES_DEVICE_ID_XML_NAME = "UtilitiesDevice.UtilitiesDeviceID";
    protected static final String POD_ID_XML_NAME = "UtilitiesMeasurementTask.UtilitiesPointOfDeliveryAssignment.UtilitiesPointOfDeliveryPartyID";

    protected static final String AT_LEAST_ONE_OF = " (at least one of)";
    private Set<String> notValidXmlNames = new HashSet<>();

    protected void addNotValidField(String xmlName) {
        notValidXmlNames.add(xmlName);
    }

    protected void addAtLeastOneNotValid(String ...xmlName) {
        notValidXmlNames.add(Arrays.stream(xmlName).collect(Collectors.joining("/")) + AT_LEAST_ONE_OF);
    }

    public String getNotValidFields() {
        return notValidXmlNames.stream().collect(Collectors.joining(", ", "'", "'"));
    }

    public boolean isValid() {
        return notValidXmlNames.isEmpty();
    }
}
