package com.energyict.genericprotocolimpl.actarisace4000.objects;

import org.w3c.dom.Element;

/**
 * @author khe
 */
public class Reject extends AbstractActarisObject {

    private int reason;

    public Reject(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected void parse(Element element) {
        this.reason = Integer.parseInt(element.getTextContent(), 16);
    }

    public String getDescription() {
        String description = "";

        switch (reason) {
            case 0:  description += getCommaSeparator(description) + "Cannot synchronise the time as drift too much";
            case 1:  description += getCommaSeparator(description) + "Over the air firmware upgrade failed";
            case 2:  description += getCommaSeparator(description) + "Invalid output setting";
            case 3:  description += getCommaSeparator(description) + "Invalid daily send schedule setting";
            case 4:  description += getCommaSeparator(description) + "Self test failed";
            case 5:  description += getCommaSeparator(description) + "Cannot return requested BD data";
            case 6:  description += getCommaSeparator(description) + "Cannot return requested LP data";
            case 7:  description += getCommaSeparator(description) + "Cannot understand tag";
            case 8:  description += getCommaSeparator(description) + "Invalid DINSO configuration data";
            case 9:  description += getCommaSeparator(description) + "Invalid tariff configuration data";
            case 10: description += getCommaSeparator(description) + "Configuration not applied";
            case 11: description += getCommaSeparator(description) + "Configuration partially applied";
            case 12: description += getCommaSeparator(description) + "Contactor command not allowed in current mode";
            case 13: description += getCommaSeparator(description) + "Password error";
            case 14: description += getCommaSeparator(description) + "Consumption limitation configuration error";
        }

        if ("".equals(description)) {
            return "Unknown reason";
        } else {
            return description;
        }
    }

    private String getCommaSeparator(String reason) {
        if ("".equals(reason)) {
            return "";
        } else {
            return ", ";
        }
    }

    @Override
    protected String prepareXML() {
        return "";        //A Reject is never sent as a request.
    }
}