package com.energyict.mdc.device.data;

import java.util.List;

/**
 * Defines a serializable search filter for Devices
 */
public class DevicesForConfigChangeSearch {

    public enum Operator {
        IN,
        LIKE,
        EQUALS
    }

    public static class DeviceSearchItem {

        public String propertyName;
        public Operator operator;
        public String singleData;
        public List<String> multipleData;

        public DeviceSearchItem(String propertyName, Operator operator, String data) {
            this.propertyName = propertyName;
            this.operator = operator;
            this.singleData = data;
        }

        public DeviceSearchItem(String propertyName, Operator operator, List<String> data) {
            this.propertyName = propertyName;
            this.operator = operator;
            this.multipleData = data;
        }

    }

    public List<DeviceSearchItem> searchItems;

}
