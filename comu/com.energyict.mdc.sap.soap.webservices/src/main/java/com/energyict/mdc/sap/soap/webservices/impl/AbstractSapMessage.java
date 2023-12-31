package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractSapMessage {

    protected static final String REQUEST_ID_XML_NAME = "MessageHeader.ID";
    protected static final String UUID_XML_NAME = "MessageHeader.UUID";
    protected static final String UTILITIES_DEVICE_ID_XML_NAME = "UtilitiesDeviceID";

    private Set<String> missingXmlNames = new HashSet<>();

    protected void addMissingField(String xmlName) {
        missingXmlNames.add(quotation(xmlName));
    }

    public void addMissingFields(Collection<String> xmlName) {
        missingXmlNames.addAll(xmlName);
    }

    protected void addAtLeastOneMissingField(Thesaurus thesaurus, String... xmlName) {
        missingXmlNames.add(thesaurus.getFormat(TranslationKeys.AT_LEAST_ONE_OF).format() + Arrays.stream(xmlName).map(this::quotation).collect(Collectors.joining(", ", " [", "]")));
    }

    public final Set<String> getMissingFieldsSet() {
        return missingXmlNames;
    }

    public String getMissingFields() {
        return missingXmlNames.stream().collect(Collectors.joining(", "));
    }

    public boolean isValid() {
        return missingXmlNames.isEmpty();
    }

    private String quotation(String value) {
        return '\'' + value + '\'';
    }
}
