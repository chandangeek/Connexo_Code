package com.energyict.protocolimplv2.edp.logbooks;

/**
 * Copyrights EnergyICT
 * Date: 11/02/14
 * Time: 10:15
 * Author: khe
 */
public class EventInfo {

    private final int eisEventCode;
    private final String description;

    public EventInfo(int eisEventCode, String description) {
        this.eisEventCode = eisEventCode;
        this.description = description;
    }

    public int getEisEventCode() {
        return eisEventCode;
    }

    public String getDescription() {
        return description;
    }
}
