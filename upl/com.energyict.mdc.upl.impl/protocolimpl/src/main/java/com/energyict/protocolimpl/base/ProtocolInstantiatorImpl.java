/*
 * ProtocolInstantiator.java
 *
 * Created on 9 december 2002, 9:04
 */

package com.energyict.protocolimpl.base;

import java.io.IOException;
import java.util.List;

import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.DemandResetProtocol;
import com.energyict.protocol.DialinScheduleProtocol;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.SerialNumber;

/**
 *
 * @author  Koen
 */
public class ProtocolInstantiatorImpl implements ProtocolInstantiator {
    
	EventMapper eventMapper=null;
    MeterProtocol meterProtocol=null;
    HHUEnabler hhuEnabler=null;
    HalfDuplexEnabler halfDuplexEnabler=null;
    RegisterProtocol registerProtocol=null;
    SerialNumber serialNumber=null;
    CacheMechanism cacheMechanism=null;
    DialinScheduleProtocol dialinScheduleProtocol=null;
    DemandResetProtocol demandResetProtocol=null;
    
    public ProtocolInstantiatorImpl() {
    }
    
    public void buildInstance(String className) throws IOException {
        Object protocolInstance = getInstance(className);
        
        try {
        	eventMapper = (EventMapper)protocolInstance;
        }
        catch(ClassCastException e) {
        	eventMapper=null;   
        }
        
        try {
            meterProtocol = (MeterProtocol)protocolInstance;
         }
         catch(ClassCastException e) {
            meterProtocol=null;   
         }
        try {
           hhuEnabler = (HHUEnabler)protocolInstance;
        }
        catch(ClassCastException e) {
           hhuEnabler=null;   
        }
        try {
           halfDuplexEnabler = (HalfDuplexEnabler)protocolInstance;
        }
        catch(ClassCastException e) {
           halfDuplexEnabler=null;   
        }
        try {
           registerProtocol = (RegisterProtocol)protocolInstance;
        }
        catch(ClassCastException e) {
           registerProtocol=null;   
        }
        try {
           dialinScheduleProtocol = (DialinScheduleProtocol)protocolInstance;
        }
        catch(ClassCastException e) {
           dialinScheduleProtocol=null;   
        }
        try {
           demandResetProtocol = (DemandResetProtocol)protocolInstance;
        }
        catch(ClassCastException e) {
           demandResetProtocol=null;   
        }
        try {
           serialNumber = (SerialNumber)protocolInstance;
        }
        catch(ClassCastException e) {
           serialNumber=null;   
        }
        try {
           cacheMechanism = (CacheMechanism)protocolInstance;
        }
        catch(ClassCastException e) {
           cacheMechanism=null;   
        }
    }
    
    public MeterProtocol getMeterProtocol() {
        return meterProtocol;
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
    public DialinScheduleProtocol getDialinScheduleProtocol() {  
        return dialinScheduleProtocol;
    }
    public DemandResetProtocol getDemandResetProtocol() {
        return demandResetProtocol;
    }
    public boolean isDialinScheduleProtocolEnabled() {
        return (getDialinScheduleProtocol()!=null);
    }
    
    public boolean isDemandResetProtocolEnabled() {
        return (getDemandResetProtocol()!=null);
    }
    
    public boolean isRegisterProtocolEnabled() {
        return (getRegisterProtocol()!=null);
    }
    public boolean isHHUEnabled() {
        return (getHHUEnabler()!=null);
    }
    public boolean isHalfDuplexEnabled() {
        return (getHalfDuplexEnabler()!=null);
    }
    public boolean isSerialNumber() {
        return (getSerialNumber()!=null);
    }
    public boolean isCacheMechanism() {
        return (getCacheMechanism()!=null);
    }
    public List getOptionalKeys() {
        return getMeterProtocol().getOptionalKeys();
    }
    public List getRequiredKeys()  {
        return getMeterProtocol().getRequiredKeys();
   }
    private Object getInstance(String className) throws IOException {
        try {
           return(Class.forName(className).newInstance());
        }
        catch(ClassNotFoundException e) {
            throw new IOException("instantiateProtocol(), ClassNotFoundException, "+e.getMessage());
        }
        catch(InstantiationException e) {
            throw new IOException("instantiateProtocol(), InstantiationException, "+e.getMessage());
        }
        catch(IllegalAccessException e) {
            throw new IOException("instantiateProtocol(), IllegalAccessException, "+e.getMessage());
        }
        catch(Exception e) {
            throw new IOException("instantiateProtocol(), Exception, "+e.getMessage());
        }
        
    } // private void instantiateProtocol(String className)

	public EventMapper getEventMapper() {
		return eventMapper;
	}
}
