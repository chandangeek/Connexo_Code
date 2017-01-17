package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingImpl;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The contents of securitymappings.properties has changed drastically between version 10.2 and 10.3.
 * This ugprader updates all entries in the database, according to the securitymappings.properties
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/01/2017 - 14:52
 */
public class UpgraderV10_3 implements Upgrader {

    private static final String securityPropertyAdapterMappingLocation = "/securitymappings.properties";
    private static final Logger LOGGER = Logger.getLogger(UpgraderV10_3.class.getName());

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 3));
        updateAllSecurityAdapterMappings();
    }

    /**
     * Every existing mapping in the DB must be updated with the values from the properties file.
     * If a mapping is in the properties file but not yet in the DB, it will be inserted by the installer (not by this upgrader).
     */
    private void updateAllSecurityAdapterMappings() {
        Properties allProperties = loadProperties(securityPropertyAdapterMappingLocation);
        List<SecuritySupportAdapterMapping> existingDBMappings = dataModel.mapper(SecuritySupportAdapterMapping.class).find();

        List<SecuritySupportAdapterMapping> dbMappingsToUpdate = allProperties
                .stringPropertyNames()
                .stream()
                //Check if this key is already in the DB
                .filter(key -> existingDBMappings.stream().filter(mapping -> mapping.getDeviceProtocolJavaClassName().equals(key)).findAny().isPresent())
                .map(key -> new SecuritySupportAdapterMappingImpl(key, allProperties.getProperty(key)))
                .collect(Collectors.toList());

        dataModel.mapper(SecuritySupportAdapterMapping.class).update(dbMappingsToUpdate);
    }

    private Properties loadProperties(String propertiesLocation) {
        Properties mappings = new Properties();
        try (InputStream inputStream = UpgraderV10_3.class.getResourceAsStream(propertiesLocation)) {
            if (inputStream == null) {
                LOGGER.severe("PropertiesFile location is probably not correct :" + propertiesLocation);
            } else {
                mappings.load(inputStream);
            }
        } catch (IOException e) {
            LOGGER.severe("Could not load the properties from " + propertiesLocation);
        }
        return mappings;
    }
}