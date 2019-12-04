/*
 * ConfigEntry.java
 *
 * Created on 5 november 2003, 15:29
 */

package com.energyict.mdc.engine.offline.core.registry;

/**
 * @author Koen
 */
public class ConfigEntry {

    public static final int TYPE_FILESTRING = 3;

    String key;
    String description;
    int type;
    String value;
    String defaultValue;

    public ConfigEntry(String key, String description, int type, String defaultValue) {
        this(key, description, type, defaultValue, null);
    }

    public ConfigEntry(String key, String description, int type, String defaultValue, String value) {
        this.key = key;
        this.description = description;
        this.type = type;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}