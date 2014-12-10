package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.dialer.core.LinkException;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.inbound.LoadProfileIdentifierType;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocolimpl.debug.AbstractSmartDebuggingMain;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.Dsmr40Protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/07/11
 * Time: 13:45
 */
public class Dsmr40EictMain extends AbstractSmartDebuggingMain<Dsmr40Protocol> {

    private static Dsmr40Protocol dsmr40Eict = null;
    public static final String MASTER_SERIAL_NUMBER = "1000827";
    public static final String MBUS_SERIAL_NUMBER = "SIM1000827006301";

    public Dsmr40Protocol getMeterProtocol() {
        if (dsmr40Eict == null) {
            dsmr40Eict = new Dsmr40Protocol();
            log("Created new instance of " + dsmr40Eict.getClass().getCanonicalName() + " [" + dsmr40Eict.getVersion() + "]");
        }
        return dsmr40Eict;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty(MeterProtocol.MAXTIMEDIFF, "300");
        properties.setProperty(MeterProtocol.MINTIMEDIFF, "1");
        properties.setProperty(MeterProtocol.CORRECTTIME, "0");
        properties.setProperty(MeterProtocol.PROFILEINTERVAL, "900");
        properties.setProperty(MeterProtocol.PASSWORD, "ntaSim");
        properties.setProperty(MeterProtocol.SERIALNUMBER, MASTER_SERIAL_NUMBER);
        properties.setProperty("NTASimulationTool", "1");
        properties.setProperty("SecurityLevel", "1:0");
        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "10000");

        return properties;
    }

    public static void main(String[] args) {
        Dsmr40EictMain main = new Dsmr40EictMain();
        main.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        main.setPhoneNumber("jme.eict.local:4059");
        main.setShowCommunication(false);
        main.run();
    }

    public void doDebug() throws LinkException, IOException {
        CosemObjectFactory cof = getMeterProtocol().getDlmsSession().getCosemObjectFactory();

        objectList();
        List<LoadProfileReader> loadProfileReaders = new ArrayList<LoadProfileReader>();

        LoadProfileReader profileReader = getMbusProfileReader();

        loadProfileReaders.add(profileReader);
        List<LoadProfileConfiguration> loadProfileConfigurations = getMeterProtocol().fetchLoadProfileConfiguration(loadProfileReaders);
        for (LoadProfileConfiguration config : loadProfileConfigurations) {
            List<ChannelInfo> channelInfos = config.getChannelInfos();
            for (ChannelInfo channelInfo : channelInfos) {
                System.out.println(channelInfo);
            }
        }

    }

    private LoadProfileReader getMbusProfileReader() {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        channelInfos.add(new ChannelInfo(0, "1.x.1.8.1.255", Unit.getUndefined(), MASTER_SERIAL_NUMBER, null));
        channelInfos.add(new ChannelInfo(1, "1.x.2.8.1.255", Unit.getUndefined(), MASTER_SERIAL_NUMBER, null));
        channelInfos.add(new ChannelInfo(2, "0.x.24.2.1.255", Unit.get("m3"), MBUS_SERIAL_NUMBER, null));
        channelInfos.add(new ChannelInfo(3, "0.x.24.2.1.255", Unit.get("ms"), MBUS_SERIAL_NUMBER, null));

        return new LoadProfileReader(
                ObisCode.fromString("0.x.98.1.0.255"),
                new Date(System.currentTimeMillis() - (24 * 3600 * 1000)),
                new Date(),
                0,
                new DeviceIdentifier<BaseDevice<?, ?, ?>>() {
                    @Override
                    public String getIdentifier() {
                        return MASTER_SERIAL_NUMBER;
                    }

                    @Override
                    public DeviceIdentifierType getDeviceIdentifierType() {
                        return null;
                    }

                    @Override
                    public String getXmlType() {
                        return null;
                    }

                    @Override
                    public void setXmlType(String ignore) {

                    }

                    @Override
                    public BaseDevice<?, ?, ?> findDevice() {
                        throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper Device ...");
                    }
                },
                channelInfos,
                MASTER_SERIAL_NUMBER, new LoadProfileIdentifier() {
            @Override
            public BaseLoadProfile findLoadProfile() {
                throw new IllegalArgumentException("This placeholder identifier can not provide you with a proper LoadProfile ...");
            }

            @Override
            public LoadProfileIdentifierType getLoadProfileIdentifierType() {
                return null;
            }

            @Override
            public List<Object> getIdentifier() {
                return null;
            }

            @Override
            public String getXmlType() {
                return null;
            }

            @Override
            public void setXmlType(String ignore) {

            }
        });
    }

    private void objectList() {
        UniversalObject[] objects = getMeterProtocol().getDlmsSession().getMeterConfig().getInstantiatedObjectList();
        for (UniversalObject uo : objects) {
            if (uo.getObisCode().equalsIgnoreBChannel(ObisCode.fromString("0.x.24.2.1.255"))) {
                System.out.println(uo.getDescription());
            }
        }
    }

}
