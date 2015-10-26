package com.energyict.mdc.device.data;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a serializable search filter for Devices
 */
public class DevicesForConfigChangeSearch implements Serializable{

    public enum Operator {
        IN,
        LIKE,
        EQUALS;
    }
    public static class DeviceSearchItem implements Serializable {

        public String propertyName;
        public Operator operator;
        public Serializable data; // string data for serialization
        public DeviceSearchItem(String propertyName, Operator operator, Serializable data) {
            this.propertyName = propertyName;
            this.operator = operator;
            this.data = data;
        }

    }
    public List<DeviceSearchItem> searchItems;

}
