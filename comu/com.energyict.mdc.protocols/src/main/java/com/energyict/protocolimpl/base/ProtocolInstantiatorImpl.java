/*
 * ProtocolInstantiator.java
 *
 * Created on 9 december 2002, 9:04
 */

package com.energyict.protocolimpl.base;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen
 */
public class ProtocolInstantiatorImpl implements ProtocolInstantiator {

    EventMapper eventMapper = null;
    MeterProtocol meterProtocol = null;
    SmartMeterProtocol smartMeterProtocol = null;
    BulkRegisterProtocol bulkRegisterProtocol = null;
    ConfigurationSupport configurationSupport;
    HHUEnabler hhuEnabler = null;
    HalfDuplexEnabler halfDuplexEnabler = null;
    RegisterProtocol registerProtocol = null;
    SerialNumber serialNumber = null;
    CacheMechanism cacheMechanism = null;
    DialinScheduleProtocol dialinScheduleProtocol = null;
    DemandResetProtocol demandResetProtocol = null;
    MultipleLoadProfileSupport multipleLoadProfileSupport = null;

    public ProtocolInstantiatorImpl() {
    }

    public void buildInstance(String className) throws IOException {
        Object protocolInstance = getInstance(className);

        try {
            eventMapper = (EventMapper) protocolInstance;
        } catch (ClassCastException e) {
            eventMapper = null;
        }

        try {
            meterProtocol = (MeterProtocol) protocolInstance;
        } catch (ClassCastException e) {
            meterProtocol = null;
        }
        try {
            smartMeterProtocol = (SmartMeterProtocol) protocolInstance;
        } catch (ClassCastException e) {
            smartMeterProtocol = null;
        }
        try {
            configurationSupport = (ConfigurationSupport) protocolInstance;
        } catch (ClassCastException e) {
            configurationSupport = null;
        }
        try {
            hhuEnabler = (HHUEnabler) protocolInstance;
        } catch (ClassCastException e) {
            hhuEnabler = null;
        }
        try {
            halfDuplexEnabler = (HalfDuplexEnabler) protocolInstance;
        } catch (ClassCastException e) {
            halfDuplexEnabler = null;
        }
        try {
            registerProtocol = (RegisterProtocol) protocolInstance;
        } catch (ClassCastException e) {
            registerProtocol = null;
        }
        try {
            dialinScheduleProtocol = (DialinScheduleProtocol) protocolInstance;
        } catch (ClassCastException e) {
            dialinScheduleProtocol = null;
        }
        try {
            demandResetProtocol = (DemandResetProtocol) protocolInstance;
        } catch (ClassCastException e) {
            demandResetProtocol = null;
        }
        try {
            serialNumber = (SerialNumber) protocolInstance;
        } catch (ClassCastException e) {
            serialNumber = null;
        }
        try {
            cacheMechanism = (CacheMechanism) protocolInstance;
        } catch (ClassCastException e) {
            cacheMechanism = null;
        }
        try {
            bulkRegisterProtocol = (BulkRegisterProtocol) protocolInstance;
        } catch (ClassCastException e) {
            bulkRegisterProtocol = null;
        }
        try {
            multipleLoadProfileSupport = (MultipleLoadProfileSupport) protocolInstance;
        } catch (ClassCastException e) {
            multipleLoadProfileSupport = null;
        }
    }

    public MeterProtocol getMeterProtocol() {
        return meterProtocol;
    }

    public SmartMeterProtocol getSmartMeterProtocol() {
        return this.smartMeterProtocol;
    }

    public HHUEnabler getHHUEnabler() {
        return hhuEnabler;
    }

    public HalfDuplexEnabler getHalfDuplexEnabler() {
        return halfDuplexEnabler;
    }

    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public CacheMechanism getCacheMechanism() {
        return cacheMechanism;
    }

    public RegisterProtocol getRegisterProtocol() {
        return registerProtocol;
    }

    public BulkRegisterProtocol getBulkRegisterProtocol() {
        return this.bulkRegisterProtocol;
    }

    public DialinScheduleProtocol getDialinScheduleProtocol() {
        return dialinScheduleProtocol;
    }

    public DemandResetProtocol getDemandResetProtocol() {
        return demandResetProtocol;
    }

    public MultipleLoadProfileSupport getMultipleLoadProfileSupport() {
        return this.multipleLoadProfileSupport;
    }

    public boolean isDialinScheduleProtocolEnabled() {
        return (getDialinScheduleProtocol() != null);
    }

    public boolean isDemandResetProtocolEnabled() {
        return (getDemandResetProtocol() != null);
    }

    public boolean isRegisterProtocolEnabled() {
        return (getRegisterProtocol() != null);
    }

    public boolean isBulkRegisterProtocolEnabled() {
        return getBulkRegisterProtocol() != null;
    }

    public boolean isHHUEnabled() {
        return (getHHUEnabler() != null);
    }

    public boolean isHalfDuplexEnabled() {
        return (getHalfDuplexEnabler() != null);
    }

    public boolean isSerialNumber() {
        return (getSerialNumber() != null);
    }

    public boolean isCacheMechanism() {
        return (getCacheMechanism() != null);
    }

    public boolean isMultipleLoadProfileSupported() {
        return getMultipleLoadProfileSupport() != null;
    }

    public List getOptionalKeys() {
        List<String> result = new ArrayList<String>();
        if (this.configurationSupport != null) {
            for (PropertySpec propertySpec : this.configurationSupport.getOptionalProperties()) {
                result.add(propertySpec.getKey());
            }
        }
        return result;
    }

    public List getRequiredKeys() {
        List<String> result = new ArrayList<String>();
        if (this.configurationSupport != null) {
            for (PropertySpec propertySpec : this.configurationSupport.getRequiredProperties()) {
                result.add(propertySpec.getKey());
            }
        }
        return result;
    }

    private Object getInstance(String className) throws IOException {
        try {
            return (Class.forName(className).newInstance());
        } catch (ClassNotFoundException e) {
            throw new IOException("instantiateProtocol(), ClassNotFoundException, " + e.getMessage());
        } catch (InstantiationException e) {
            throw new IOException("instantiateProtocol(), InstantiationException, " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IOException("instantiateProtocol(), IllegalAccessException, " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("instantiateProtocol(), Exception, " + e.getMessage());
        }

    } // private void instantiateProtocol(String className)

    public EventMapper getEventMapper() {
        return eventMapper;
    }
}
