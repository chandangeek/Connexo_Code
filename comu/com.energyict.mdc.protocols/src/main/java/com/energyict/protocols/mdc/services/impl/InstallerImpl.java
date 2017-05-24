/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolUpgradeService;
import com.energyict.mdc.upl.TypedProperties;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installer for the {@link DeviceProtocolUpgradeService}, which will
 * migrate all former security attributes to proper KeyAccessors on the
 * devices. On the device types, corresponding KeyAccessorsTypes will be
 * created along the way. These will be used in the SecurityPropertySets of
 * the device configurations.
 *
 * @author stijn
 * @since 23.05.17 - 09:00
 */
public class InstallerImpl implements FullInstaller {

    private final DataModel dataModel;
    private final DeviceService deviceService;
    private final PkiService pkiService;
    private final DataVaultService dataVaultService;

    private KeyAccessorValuePersister keyAccessorValuePersister;

    @Inject
    InstallerImpl(DataModel dataModel, DeviceService deviceService, PkiService pkiService, DataVaultService dataVaultService) {
        super();
        this.dataModel = dataModel;
        this.deviceService = deviceService;
        this.pkiService = pkiService;
        this.dataVaultService = dataVaultService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        logger.log(Level.INFO, "Upgrading all security settings - this may take some time depending on the number of devices in the system.");
        moveSecurityAttributesToKeyAccessors(logger);
    }

    private void moveSecurityAttributesToKeyAccessors(Logger logger) {
        doTry(
                "Upgrading security settings of DLMS security",
                () -> moveSecurityAttrbituteToKeyAccessorsForDlmsSecuritySupport(logger),
                logger
        );
        doTry(
                "Upgrading security settings of No security or password protected security",
                () -> moveSecurityAttributesToKeyAccessorsForNoOrPasswordSecuritySupport(logger),
                logger
        );
        doTry(
                "Upgrading security settings of Password with user identification security",
                () -> moveSecurityAttributesToKeyAccessorsForPasswordWithUserIdentificationSecuritySupport(logger),
                logger
        );
        doTry(
                "Upgrading security settings of IEC1107 security",
                () -> moveSecurityAttributesToKeyAccessorsForIEC1107SecuritySupport(logger),
                logger
        );
    }

    private void moveSecurityAttrbituteToKeyAccessorsForDlmsSecuritySupport(Logger logger) {
        String sql = "SELECT DEVICE, PROPERTYSPECPROVIDER, CLIENTMACADDRESS, PASSWORD, AUTHKEY, ENCRYPTIONKEY FROM PR1_DLMS_SECURITY"; // CLIENTMACADDRESS maps to client
        List<String> propertyNames = Arrays.asList("Password", "AuthenticationKey", "EncryptionKey");
        doMoveSecurityAttributesToKeyAccessors(sql, propertyNames, logger);
    }

    private void moveSecurityAttributesToKeyAccessorsForNoOrPasswordSecuritySupport(Logger logger) {
        String sql;
        List<String> propertyNames;
        sql = "SELECT DEVICE, PROPERTYSPECPROVIDER, null, PASSWORD FROM PR1_NO_OR_PWD_SECURITY"; // Doesn't have support for client
        propertyNames = Collections.singletonList("Password");
        doMoveSecurityAttributesToKeyAccessors(sql, propertyNames, logger);
    }

    private void moveSecurityAttributesToKeyAccessorsForPasswordWithUserIdentificationSecuritySupport(Logger logger) {
        String sql;
        List<String> propertyNames;
        sql = "SELECT DEVICE, PROPERTYSPECPROVIDER, USER_NME, PASSWORD FROM PR1_BASIC_AUTHENTICATION"; // USER_NME maps to client
        propertyNames = Collections.singletonList("Password");
        doMoveSecurityAttributesToKeyAccessors(sql, propertyNames, logger);
    }

    private void moveSecurityAttributesToKeyAccessorsForIEC1107SecuritySupport(Logger logger) {
        String sql;
        List<String> propertyNames;
        sql = "SELECT DEVICE, PROPERTYSPECPROVIDER, null, PASSWORD FROM PR1_IEC1107_SECURITY"; // Doesn't have support for client
        propertyNames = Collections.singletonList("Password");
        doMoveSecurityAttributesToKeyAccessors(sql, propertyNames, logger);
    }

    private void doMoveSecurityAttributesToKeyAccessors(String sql, List<String> propertyNames, Logger logger) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                String sqlQuery = sql.concat(" WHERE ENDTIME = 1000000000000000000"); // As we only want to migrate active entries (~ the ones having end time set as eternity)
                ResultSet rs = statement.executeQuery(sqlQuery);
                List<Long> securityPropertySetsAlreadyDone = new ArrayList<>();
                while (rs.next()) {
                    handleSecurityAttributeChangesFor(rs, propertyNames, securityPropertySetsAlreadyDone, logger);
                }
            }
        });
    }

    private void handleSecurityAttributeChangesFor(ResultSet rs, List<String> propertyNames, List<Long> securityPropertySetsAlreadyDone, Logger logger) throws SQLException {
        int i = 1;
        long deviceId = rs.getLong(i++);
        long securityPropertySetId = rs.getLong(i++);
        String clientMacAddress = rs.getString(i++);
        TypedProperties oldProperties = TypedProperties.empty();
        for (String propertyName : propertyNames) {
            addPropertyIfNotNull(oldProperties, propertyName, rs.getString(i++));
        }

        Optional<Device> deviceOptional = deviceService.findDeviceById(deviceId);
        if (deviceOptional.isPresent()) {
            try {
                Optional<SecurityPropertySet> securityPropertySetOptional = deviceOptional.get().getDeviceConfiguration().getSecurityPropertySets()
                        .stream()
                        .filter(securityPropertySet -> securityPropertySet.getId() == securityPropertySetId)
                        .findFirst();
                if (securityPropertySetOptional.isPresent()) {
                    SecurityPropertySet securityPropertySet = securityPropertySetOptional.get();

                    // 1. Configuration level - update the SecurityPropertySet on DeviceConfig and add corresponding KeyAccessorTypes to the DeviceType
                    if (!securityPropertySetsAlreadyDone.contains(securityPropertySetId)) {
                        updateExistingSecurityPropertySet(securityPropertySet, deviceOptional.get(), clientMacAddress);
                        securityPropertySetsAlreadyDone.add(securityPropertySetId);
                    }

                    // 2. Device level - create the corresponding KeyAccessors and fill in the actual value of the keys/passwords
                    createKeyAccessorsOnDeviceAndFillInActualValue(deviceOptional.get(), securityPropertySet, oldProperties);
                }
            } catch (IllegalStateException e) {
                logger.log(Level.SEVERE, "Migration of security properties for device with MRID " + deviceOptional.get().getmRID() + " failed: " + e.getMessage());
            }
        }
    }

    private void addPropertyIfNotNull(TypedProperties oldProperties, String name, String value) throws SQLException {
        if (value != null) {
            oldProperties.setProperty(name, new String(dataVaultService.decrypt(value)));
        }
    }

    private void updateExistingSecurityPropertySet(SecurityPropertySet securityPropertySet, Device device, String clientMacAddress) {
        securityPropertySet.setClient(clientMacAddress);
        securityPropertySet.getPropertySpecs().forEach(
                propertySpec ->
                        securityPropertySet.addConfigurationSecurityProperty(
                                propertySpec.getName(),
                                createNewKeyAccessorType(device.getDeviceType(), securityPropertySet, propertySpec)
                        )
        );
        securityPropertySet.update();
    }

    private KeyAccessorType createNewKeyAccessorType(DeviceType deviceType, SecurityPropertySet securityPropertySet, PropertySpec propertySpec) {
        String keyAccessorTypeName = securityPropertySet.getName().concat(" - ").concat(propertySpec.getName());
        return deviceType.getKeyAccessorTypes()
                .stream()
                .filter(keyAccessorType -> keyAccessorType.getName().equals(keyAccessorTypeName))
                .findFirst()
                .orElseGet(() -> deviceType.addKeyAccessorType(keyAccessorTypeName, createOrGetKeyType(propertySpec))
                        .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                        .duration(TimeDuration.years(1))
                        .add());
    }

    private KeyType createOrGetKeyType(PropertySpec propertySpec) {
        if (propertySpec.getName().equals("Password")) {
            return pkiService.getKeyType("Password").orElseGet(() -> pkiService.newPassphraseType("Password").withSpecialCharacters().length(30).add());
        } else {
            return pkiService.getKeyType("AES 128").orElseGet(() -> pkiService.newSymmetricKeyType("AES 128", "AES", 128).add());
        }
    }

    private void createKeyAccessorsOnDeviceAndFillInActualValue(Device device, SecurityPropertySet securityPropertySet, TypedProperties oldProperties) {
        securityPropertySet.getConfigurationSecurityProperties().forEach(
                property -> {
                    if (oldProperties.hasValueFor(property.getName())) {
                        getKeyAccessorValuePersister().persistKeyAccessorValue(device, property.getKeyAccessorType(), (String) oldProperties.getProperty(property.getName()));
                    }
                }
        );
    }

    private KeyAccessorValuePersister getKeyAccessorValuePersister() {
        if (keyAccessorValuePersister == null) {
            keyAccessorValuePersister = new KeyAccessorValuePersister(pkiService);
        }
        return keyAccessorValuePersister;
    }
}