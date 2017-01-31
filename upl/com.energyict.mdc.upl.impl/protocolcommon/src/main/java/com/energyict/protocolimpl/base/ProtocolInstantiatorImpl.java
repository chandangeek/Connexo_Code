/*
 * ProtocolInstantiator.java
 *
 * Created on 9 december 2002, 9:04
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MultipleLoadProfileSupport;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.SerialNumber;

import java.io.IOException;
import java.util.List;

/**
 * @author Koen
 */
public class ProtocolInstantiatorImpl implements ProtocolInstantiator {

    EventMapper eventMapper = null;
    MeterProtocol meterProtocol = null;
    SmartMeterProtocol smartMeterProtocol = null;
    BulkRegisterProtocol bulkRegisterProtocol = null;
    HasDynamicProperties configurationSupport;
    HHUEnabler hhuEnabler = null;
    HalfDuplexEnabler halfDuplexEnabler = null;
    RegisterProtocol registerProtocol = null;
    SerialNumber serialNumber = null;
    CacheMechanism cacheMechanism = null;
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
            configurationSupport = (HasDynamicProperties) protocolInstance;
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

    public MultipleLoadProfileSupport getMultipleLoadProfileSupport() {
        return this.multipleLoadProfileSupport;
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.configurationSupport.getUPLPropertySpecs();
    }

    private Object getInstance(String className) throws IOException {
        try {
            return (Class.forName(className).newInstance());
        } catch (ClassNotFoundException e) {
            throw new ProtocolException("instantiateProtocol(), ClassNotFoundException, " + e.getMessage());
        } catch (InstantiationException e) {
            throw new ProtocolException("instantiateProtocol(), InstantiationException, " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ProtocolException("instantiateProtocol(), IllegalAccessException, " + e.getMessage());
        } catch (Exception e) {
            throw new ProtocolException("instantiateProtocol(), Exception, " + e.getMessage());
        }

    }

    public EventMapper getEventMapper() {
        return eventMapper;
    }
}
