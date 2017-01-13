package com.energyict.protocols.mdc.adapter.cps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/01/2017 - 14:05
 */
public class DialectCustomPropertySetNameDetective {

    public static final String MAPPING_PROPERTIES_FILE_NAME = "dialect-custom-property-set-mapping.properties";
    private static final Logger LOGGER = Logger.getLogger(DialectCustomPropertySetNameDetective.class.getName());
    private final Map<String, String> customPropertySetClassNameMap = new ConcurrentHashMap<>();

    public DialectCustomPropertySetNameDetective() {
        super();
        this.loadCustomPropertySetNameMapping();
    }

    private void loadCustomPropertySetNameMapping() {
        Properties mappings = new Properties();
        try (InputStream inputStream = this.getClass().getResourceAsStream(MAPPING_PROPERTIES_FILE_NAME)) {
            if (inputStream == null) {
                LOGGER.severe("CustomPropertySetNameMapping properties file location is probably not correct " + MAPPING_PROPERTIES_FILE_NAME);
                throw new IllegalArgumentException("CustomPropertySetNameMapping - Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
            }
            mappings.load(inputStream);
            mappings.entrySet().forEach(entry -> this.customPropertySetClassNameMap.put((String) entry.getKey(), (String) entry.getValue()));
        } catch (IOException e) {
            LOGGER.severe("Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
            throw new IllegalArgumentException("CustomPropertySetNameMapping - Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
        }
    }

    /**
     * Get the CPS class from the mapping, for a given dialect class
     */
    public String customPropertySetClassNameFor(Class dialectClass) {
        return this.customPropertySetClassNameMap.get(dialectClass.getName());
    }
}