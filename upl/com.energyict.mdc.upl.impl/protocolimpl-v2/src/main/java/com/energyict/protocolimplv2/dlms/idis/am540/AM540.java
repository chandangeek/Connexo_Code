package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;

import java.util.Arrays;
import java.util.List;

/**
 * The AM540 is a PLC E-meter designed according to IDIS package 2 specifications <br/>
 * The protocol is an extension of the AM130 protocol (which is the GPRS variant designed according to IDIS P2)
 *
 * @author sva
 * @since 11/08/2015 - 14:04
 */
public class AM540 extends AM130 {

    @Override
    public String getProtocolDescription() {
        return "AM540 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getEquipmentIdentifier();
        } else {
            return getMeterInfo().getSerialNr();
        }
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList((DeviceProtocolDialect) new SerialDeviceProtocolDialect());
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList((ConnectionType) new SioOpticalConnectionType(), new RxTxOpticalConnectionType());
    }

    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new AM540ConfigurationSupport();
    }

    @Override
    public AM540Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (AM540Properties) dlmsProperties;
    }

    @Override
    protected AM540Properties getNewInstanceOfProperties() {
        return new AM540Properties();
    }

    @Override
    protected AM130RegisterFactory getAM130RegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AM540RegisterFactory(this);
        }
        return registerFactory;
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this);
        }
        return meterTopology;
    }

    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM540Messaging(this);
        }
        return idisMessaging;
    }
}