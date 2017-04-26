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
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The contents of securitymappings.properties has changed drastically between version 10.2 and 10.3 (due to the UPL feature).
 * This ugprader updates all entries in the database, according to the securitymappings.properties.
 * It also runs the PPC {@link Installer} once, to make sure that additional entries in securitymappings.properties are also inserted into the database.
 * <p>
 * It also updates the names and java_class_names of existing pluggable classes that have changed between 10.2 and 10.3.
 * <p>
 *
 * @author khe
 * @since 5/01/2017 - 14:52
 */
public class UpgraderV10_3 implements Upgrader {

    private static final String securityPropertyAdapterMappingLocation = "/securitymappings.properties";
    private static final Logger LOGGER = Logger.getLogger(UpgraderV10_3.class.getName());

    /**
     * Maps the old names (10.2) to the new names (10.3) for pluggable classes of type inbound protocols/connectiontypes/outbound protocols.
     */
    private static Map<String, String> PLUGGABLE_CLASS_NAMES_MAPPING = new HashMap<>();

    /**
     * Maps the old java_class_names (10.2) to the new java_class_names (10.3) for pluggable classes of type inbound protocols/connectiontypes/outbound protocols.
     */
    private static Map<String, String> PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING = new HashMap<>();

    static {
        PLUGGABLE_CLASS_NAMES_MAPPING.put("OutboundProximusSms", "OutboundProximusSmsConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("OutboundUdp", "OutboundUdpConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("OutboundTcpIp", "OutboundTcpIpConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("TcpIpPostDial", "TcpIpPostDialConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("RxTxSerial", "RxTxSerialConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("RxTxAtModem", "RxTxAtModemConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("RxTxOptical", "RxTxOpticalConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("SioSerial", "SioSerialConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("SioCaseModem", "SioCaseModemConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("SioAtModem", "SioAtModemConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("SioPEMPModem", "SioPEMPModemConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("SioOptical", "SioOpticalConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("SioPaknetModem", "SioPaknetModemConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("CTRInboundDialHomeId", "CTRInboundDialHomeIdConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("InboundProximusSms", "InboundProximusSmsConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("InboundIp", "InboundIpConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("EIWeb", "EIWebConnectionType");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("Empty", "EmptyConnectionType");

        PLUGGABLE_CLASS_NAMES_MAPPING.put("General Electric KV ANSI", "General Electric KVx ANSI");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("EICT SDK DeviceProtocol", "EnergyICT SDK DeviceProtocol");

        PLUGGABLE_CLASS_NAMES_MAPPING.put("IFrameDiscover", "IframeDiscover");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("DoubleIFrameDiscover", "DoubleIframeDiscover");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("RequestDiscover", "RequestDiscover");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("Ace4000", "ACE4000Inbound");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("MK10_INBOUND", "MK10InboundDeviceProtocol");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("BEACON_3100_PUSH", "Beacon3100PushEventNotification");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("EIWebBulk", "EIWebBulk");
        PLUGGABLE_CLASS_NAMES_MAPPING.put("DlmsSerialNumberDiscover", "DlmsSerialNumberDiscover");

        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.sms.OutboundProximusSmsConnectionType", "com.energyict.mdc.channels.sms.OutboundProximusSmsConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.ip.datagrams.OutboundUdpConnectionType", "com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType", "com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.ip.socket.TcpIpPostDialConnectionType", "com.energyict.mdc.channels.ip.socket.TcpIpPostDialConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.direct.rxtx.RxTxPlainSerialConnectionType", "com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.modem.rxtx.RxTxAtModemConnectionType", "com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType", "com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.direct.serialio.SioPlainSerialConnectionType", "com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.modem.serialio.SioCaseModemConnectionType", "com.energyict.mdc.channels.serial.modem.serialio.SioCaseModemConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.modem.serialio.SioAtModemConnectionType", "com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.modem.serialio.SioPEMPModemConnectionType", "com.energyict.mdc.channels.serial.modem.serialio.SioPEMPModemConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType", "com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.serial.modem.serialio.SioPaknetModemConnectionType", "com.energyict.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.inbound.CTRInboundDialHomeIdConnectionType", "com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.sms.InboundProximusSmsConnectionType", "com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.ip.InboundIpConnectionType", "com.energyict.mdc.channels.ip.InboundIpConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.inbound.EIWebConnectionType", "com.energyict.mdc.channels.inbound.EIWebConnectionType");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.impl.channels.EmptyConnectionType", "com.energyict.mdc.channels.EmptyConnectionType");

        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimplv2.sdksample.SDKDeviceProtocol", "test.com.energyict.protocolimplv2.sdksample.SDKDeviceProtocol");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithAllProperties", "test.com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithAllProperties");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimpl.dlms.SimpleDLMSProtocol", "test.com.energyict.protocolimpl.dlms.SimpleDLMSProtocol");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimpl.sdksample.SDKSampleProtocol", "test.com.energyict.protocolimpl.sdksample.SDKSampleProtocol");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol", "test.com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimpl.eicttest.EICTTestProtocol", "test.com.energyict.protocolimpl.eicttest.EICTTestProtocol");

        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.mdc.inbound.general.IframeDiscover", "com.energyict.mdc.protocol.inbound.general.IframeDiscover");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.mdc.inbound.general.DoubleIframeDiscover", "com.energyict.mdc.protocol.inbound.general.DoubleIframeDiscover");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.mdc.inbound.general.RequestDiscover", "com.energyict.mdc.protocol.inbound.general.RequestDiscover");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimplv2.ace4000.ACE4000Inbound", "com.energyict.protocolimplv2.ace4000.ACE4000Inbound");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol", "com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.mdc.inbound.g3.Beacon3100PushEventNotification", "com.energyict.mdc.protocol.inbound.g3.Beacon3100PushEventNotification");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocolimplv2.eict.eiweb.EIWebBulk", "com.energyict.protocolimplv2.eict.eiweb.EIWebBulk");
        PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.put("com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover", "com.energyict.mdc.protocol.inbound.dlms.DlmsSerialNumberDiscover");
    }

    private final DataModel dataModel;
    private final Installer installer;

    @Inject
    public UpgraderV10_3(DataModel dataModel, Installer installer) {
        this.dataModel = dataModel;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 3));
        installer.setInstallEventTypes(false);
        installer.install(dataModelUpgrader, Logger.getLogger(UpgraderV10_3.class.getName()));       //This will insert new mapping entries from the properties files into the database.

        updateAllSecurityAdapterMappings();
        updateAllPluggableClassNames();
        updateAllPluggableClassJavaClassNames();
    }

    /**
     * Some names of some pluggable classes (inbound protocols/connectiontypes/outbound protocols) have changed between 10.2 and 10.3.
     */
    private void updateAllPluggableClassNames() {
        dataModel.useConnectionRequiringTransaction(connection -> {
            for (String oldName : PLUGGABLE_CLASS_NAMES_MAPPING.keySet()) {
                String newName = PLUGGABLE_CLASS_NAMES_MAPPING.get(oldName);
                String sql = "UPDATE CPC_PLUGGABLECLASS SET NAME = '" + newName + "' WHERE NAME = '" + oldName + "'";

                try (Statement statement = connection.createStatement()) {
                    execute(statement, sql);
                }
            }
        });
    }

    /**
     * Some java_class_names of some pluggable classes (inbound protocols/connectiontypes/outbound protocols) have changed between 10.2 and 10.3.
     */
    private void updateAllPluggableClassJavaClassNames() {
        dataModel.useConnectionRequiringTransaction(connection -> {
            for (String oldJavaClassName : PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.keySet()) {
                String newJavaClassName = PLUGGABLE_CLASS_JAVA_CLASS_NAMES_MAPPING.get(oldJavaClassName);
                String sql = "UPDATE CPC_PLUGGABLECLASS SET JAVACLASSNAME = '" + newJavaClassName + "' WHERE JAVACLASSNAME = '" + oldJavaClassName + "'";

                try (Statement statement = connection.createStatement()) {
                    execute(statement, sql);
                }
            }
        });
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