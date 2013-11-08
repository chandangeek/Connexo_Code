package com.energyict.protocolimplv2.messages.enums;

/**
 * Copyrights EnergyICT
 * Date: 25/10/13
 * Time: 17:27
 * Author: khe
 */
public enum MonitoredValue {

    TotalInstantCurrent(1, "Total instantaneous current"),
    AverageActiveEnergyImport(2, "Average active energy+ (sliding demand)"),
    AverageTotalActiveEnergyImport(3, "Average total active energy+ (sliding demand)");

    private final String description;
    private final int id;

    MonitoredValue(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public static int fromDescription(String description) {
        for (MonitoredValue monitoredValue : values()) {
            if (monitoredValue.getDescription().equals(description)) {
                return monitoredValue.getId();
            }
        }
        return -1;
    }

    public static String[] getAllDescriptions() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getDescription();
        }
        return result;
    }
}