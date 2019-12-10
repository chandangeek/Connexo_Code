/*
 * RegistryConfiguration.java
 *
 * Created on 5 november 2003, 13:57
 */

package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.registry.ComServerMobileGuiJConfigEntries;
import com.energyict.mdc.engine.offline.core.registry.ConfigEntry;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;

import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Koen
 */
public class RegistryConfiguration {

    private static RegistryConfiguration config = null;
    private Map<String, ConfigEntry> map = null;
    private Preferences prefs;

    /**
     * Creates a new instance of PropertiesLoader
     */
    private RegistryConfiguration(Preferences prefs, Map map) {
        this.prefs = prefs;
        this.map = map;
        loadMap();
    }

    static public void createDefault(Class cls, Map map) {
        config = new RegistryConfiguration(Preferences.userNodeForPackage(cls), map);
    }

    static public RegistryConfiguration getDefault() {
        if (config == null) {
            createDefault(OfflineFrame.class, ComServerMobileGuiJConfigEntries.getMap());
        }
        return config;
    }

    public void editAll() {
        // build editable table with config values 
        // via tools van de gui application, display table dialog and let the user change the values
        // evt. JOptionPane in get method vervangen door die editAll...
        // f(configEntry.type()) extra configuratie mogelijkheden per table toevoegen, zoals een filechooser, etc...
    }

    public void loadMap() {
        for (ConfigEntry ce : map.values()) {
            ce.setValue(get(ce.getKey()));
        }
    }

    public String get(String key) {
        // search map
        ConfigEntry val = map.get(key);
        if (val == null) {
            throw new ApplicationException("RegistryConfiguration, key " + key + " does not exist! Correct first!");
        }
        if (val.getValue() == null) {
            // search registry  
            val.setValue(prefs.get(key, null));
            if (val.getValue() == null) {
                //initialize vith default value
                val.setValue(val.getDefaultValue());
                prefs.put(key, val.getValue());
            }
            map.put(key, val);
        }
        return val.getValue();
    }

    public void set(String key, String value) {
        ConfigEntry val = map.get(key);
        val.setValue(value);
        prefs.put(key, value);
    }

    public void copyKeyValues(String key, String tempKey) {
        ConfigEntry val = map.get(key);
        ConfigEntry tempVal = map.get(tempKey);
        val.setValue(tempVal.getValue());
        prefs.put(key, val.getValue());
    }
}