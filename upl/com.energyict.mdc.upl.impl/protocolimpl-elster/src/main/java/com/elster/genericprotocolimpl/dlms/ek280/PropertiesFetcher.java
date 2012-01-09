package com.elster.genericprotocolimpl.dlms.ek280;

import com.energyict.mdw.core.*;
import com.energyict.protocol.MeterProtocol;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 8/06/11
 * Time: 16:45
 */
public class PropertiesFetcher {

    private Logger logger;

    public PropertiesFetcher(Logger logger) {
        this.logger = logger;
    }

    public Properties getPropertiesForRtu(Rtu rtu) {
        Properties properties = new Properties();
        properties.putAll(getPropertiesFromProtocolClass());
        if (rtu != null) {
            properties.putAll(getPropertiesFromRtu(rtu));
        }
        return properties;
    }

    private Properties getPropertiesFromRtu(Rtu rtu) {
        Properties properties = rtu.getProperties();

        properties.setProperty(MeterProtocol.PROFILEINTERVAL, replaceNullValue("" + rtu.getIntervalInSeconds()));
        properties.setProperty(MeterProtocol.NODEID, replaceNullValue(rtu.getNodeAddress()));
        properties.setProperty(MeterProtocol.PASSWORD, replaceNullValue(rtu.getPassword()));
        properties.setProperty("PhoneNumber", replaceNullValue(rtu.getPhoneNumber()));
        properties.setProperty("CallHomeId", replaceNullValue(rtu.getDialHomeId()));
        properties.setProperty("NetworkId", replaceNullValue(rtu.getNetworkId()));

        // Some strange things to do here :P The EIServer model for italgas uses the gas serial number as rtu serial number
        // The real EK280 serial number can be found in the device id. However, the non generic EK280 expects the EK280 serial in this field,
        // so to make both parties happy we have to switch them here.
        properties.setProperty(MeterProtocol.ADDRESS, replaceNullValue(rtu.getSerialNumber()));
        properties.setProperty(MeterProtocol.SERIALNUMBER, replaceNullValue(rtu.getDeviceId()));

        return properties;
    }

    private String replaceNullValue(String valueThatCanBeNull) {
        return valueThatCanBeNull == null ? "" : valueThatCanBeNull;
    }

    private Properties getPropertiesFromProtocolClass() {
        CommunicationProtocol protocol = findCommunicationProtocol();
        if (protocol != null) {
            return protocol.getProperties();
        }
        getLogger().warning("No protocol properties found. Device type has no protocol. Using defaults!");
        return new Properties();
    }

    private CommunicationProtocol findCommunicationProtocol() {
        String className = EK280.class.getName();
        List<CommunicationProtocol> protocols = MeteringWarehouse.getCurrent().getCommunicationProtocolFactory().findAll();
        for (CommunicationProtocol protocol : protocols) {
            if (protocol.getJavaClassName().equalsIgnoreCase(className)) {
                return protocol;
            }
        }
        getLogger().severe("No protocol found with class [" + className + "]! Unable to fetch protocol properties.");
        return null;
    }

    private Logger getLogger() {
        if (logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

}
