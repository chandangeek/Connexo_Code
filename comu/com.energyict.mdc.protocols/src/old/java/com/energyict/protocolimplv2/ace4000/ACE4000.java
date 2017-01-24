package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.securitysupport.NoOrPasswordSecuritySupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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

    private final IdentificationService identificationService;
    private ACE4000Connection ace4000Connection;
    private ACE4000Properties properties;
    protected String serialNumber = null;

    private List<CollectedRegister> collectedBillingRegisters;
    private List<CollectedRegister> collectedMBusBillingRegisters;
    private List<CollectedRegister> collectedInstantRegisters;
    private List<CollectedRegister> collectedMaxDemandRegisters;
    private List<CollectedRegister> collectedCurrentRegisters;
    private List<CollectedRegister> collectedMBusCurrentRegisters;

    /**
     * Serves as a storage of all received RegisterValue ObisCodes.
     */
    private List<ObisCode> receivedRegisterObisCodeList = new ArrayList<>();

    //Used by both inbound and outbound protocols
    protected ObjectFactory objectFactory;

    public ACE4000(PropertySpecService propertySpecService, IdentificationService identificationService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
        this.identificationService = identificationService;
    }

    public ACE4000Connection getAce4000Connection() {
        return ace4000Connection;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return this.identificationService.createDeviceIdentifierByCallHomeId(this.serialNumber);
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
            properties = new ACE4000Properties(this.getPropertySpecService());
        }
        return properties;
    }

    public void copyProperties(TypedProperties properties) {
        this.properties = new ACE4000Properties(properties, this.getPropertySpecService());
    }

    public List<PropertySpec> getPropertySpecs () {
        return getProperties().getPropertySpecs(this.getThesaurus());
    }

    //Gather all collected registers from the ObjectFactory
    public List<CollectedRegister> getCollectedRegisters() {

        //Master registers
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
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
            collectedBillingRegisters = new ArrayList<>();
        }
        return collectedBillingRegisters;
    }

    public List<CollectedRegister> getCollectedMBusBillingRegisters() {
        if (collectedMBusBillingRegisters == null) {
            collectedMBusBillingRegisters = new ArrayList<>();
        }
        return collectedMBusBillingRegisters;
    }

    public List<CollectedRegister> getCollectedInstantRegisters() {
        if (collectedInstantRegisters == null) {
            collectedInstantRegisters = new ArrayList<>();
        }
        return collectedInstantRegisters;
    }

    public List<CollectedRegister> getCollectedMaxDemandRegisters() {
        if (collectedMaxDemandRegisters == null) {
            collectedMaxDemandRegisters = new ArrayList<>();
        }
        return collectedMaxDemandRegisters;
    }

    public List<CollectedRegister> getCollectedCurrentRegisters() {
        if (collectedCurrentRegisters == null) {
            collectedCurrentRegisters = new ArrayList<>();
        }
        return collectedCurrentRegisters;
    }

    public List<CollectedRegister> getCollectedMBusCurrentRegisters() {
        if (collectedMBusCurrentRegisters == null) {
            collectedMBusCurrentRegisters = new ArrayList<>();
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

    public void addReceivedRegisterObisCode(final ObisCode obisCode){
        this.receivedRegisterObisCodeList.add(obisCode);
    }

    public List<ObisCode> getReceivedRegisterObisCodeList() {
        return receivedRegisterObisCodeList;
    }
}