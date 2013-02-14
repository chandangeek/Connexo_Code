package test.com.energyict.protocolimplV2.ace4000;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.SerialNumberDeviceIdentifier;
import com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport;
import test.com.energyict.protocolimplV2.ace4000.objects.ObjectFactory;

import java.util.*;
import java.util.logging.Logger;

/**
 * Has common methods and fields for the inbound and the outbound ACE4000 protocol class
 * <p/>
 * Copyrights EnergyICT
 * Date: 6/11/12
 * Time: 16:47
 * Author: khe
 */
public abstract class ACE4000 extends NoOrPasswordSecuritySupport {

    private ACE4000Connection ace4000Connection;
    private ACE4000Properties properties;
    protected String serialNumber = null;

    private List<CollectedRegister> collectedBillingRegisters;
    private List<CollectedRegister> collectedMBusBillingRegisters;
    private List<CollectedRegister> collectedInstantRegisters;
    private List<CollectedRegister> collectedMaxDemandRegisters;
    private List<CollectedRegister> collectedCurrentRegisters;
    private List<CollectedRegister> collectedMBusCurrentRegisters;

    //Used by both inbound and outbound protocols
    protected ObjectFactory objectFactory;

    public ACE4000Connection getAce4000Connection() {
        return ace4000Connection;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return new SerialNumberDeviceIdentifier(serialNumber);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        if (this.serialNumber == null) {   //Only set it the first time, contains the master serial number
            this.serialNumber = serialNumber;
        }
    }

    public void setAce4000Connection(ACE4000Connection ace4000Connection) {
        this.ace4000Connection = ace4000Connection;
    }

    public ACE4000Properties getProperties() {
        if (properties == null) {
            properties = new ACE4000Properties();
        }
        return properties;
    }


    public void addProperties(TypedProperties properties) {
        this.properties = new ACE4000Properties(properties);
    }

    public List<PropertySpec> getRequiredProperties() {
        return getProperties().getRequiredKeys();
    }

    public List<PropertySpec> getOptionalProperties() {
        return getProperties().getOptionalKeys();
    }

    //Gather all collected registers from the ObjectFactory
    public List<CollectedRegister> getCollectedRegisters() {

        //Master registers
        List<CollectedRegister> collectedRegisters = new ArrayList<CollectedRegister>();
        collectedRegisters.addAll(getCollectedBillingRegisters());
        collectedRegisters.addAll(getCollectedCurrentRegisters());
        collectedRegisters.addAll(getCollectedInstantRegisters());
        collectedRegisters.addAll(getCollectedMaxDemandRegisters());

        //Slave registers
        collectedRegisters.addAll(getCollectedMBusCurrentRegisters());
        collectedRegisters.addAll(getCollectedMBusBillingRegisters());
        return collectedRegisters;
    }

    /**
     * After parsing received frames in the ObjectFactory, that data is added in these lists
     */
    public List<CollectedRegister> getCollectedBillingRegisters() {
        if (collectedBillingRegisters == null) {
            collectedBillingRegisters = new ArrayList<CollectedRegister>();
        }
        return collectedBillingRegisters;
    }

    public List<CollectedRegister> getCollectedMBusBillingRegisters() {
        if (collectedMBusBillingRegisters == null) {
            collectedMBusBillingRegisters = new ArrayList<CollectedRegister>();
        }
        return collectedMBusBillingRegisters;
    }

    public List<CollectedRegister> getCollectedInstantRegisters() {
        if (collectedInstantRegisters == null) {
            collectedInstantRegisters = new ArrayList<CollectedRegister>();
        }
        return collectedInstantRegisters;
    }

    public List<CollectedRegister> getCollectedMaxDemandRegisters() {
        if (collectedMaxDemandRegisters == null) {
            collectedMaxDemandRegisters = new ArrayList<CollectedRegister>();
        }
        return collectedMaxDemandRegisters;
    }

    public List<CollectedRegister> getCollectedCurrentRegisters() {
        if (collectedCurrentRegisters == null) {
            collectedCurrentRegisters = new ArrayList<CollectedRegister>();
        }
        return collectedCurrentRegisters;
    }

    public List<CollectedRegister> getCollectedMBusCurrentRegisters() {
        if (collectedMBusCurrentRegisters == null) {
            collectedMBusCurrentRegisters = new ArrayList<CollectedRegister>();
        }
        return collectedMBusCurrentRegisters;
    }

    /**
     * Sub classes (inbound and outbound protocol) provide a different implementation
     *
     * @return logger
     */
    public abstract Logger getLogger();

    public abstract ObjectFactory getObjectFactory();

    public abstract TimeZone getTimeZone();

    public boolean isDst() {
        return getTimeZone().inDaylightTime(getObjectFactory().getCurrentMeterTime(false, new Date()));
    }
}