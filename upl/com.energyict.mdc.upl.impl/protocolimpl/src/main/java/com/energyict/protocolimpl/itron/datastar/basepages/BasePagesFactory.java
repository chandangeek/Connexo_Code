/*
 * BasePagesFactory.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocolimpl.itron.datastar.Datastar;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePageFactory;
import com.energyict.protocolimpl.itron.protocol.ProtocolLink;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class BasePagesFactory extends AbstractBasePageFactory {
    
    private Datastar datastar;
    

    MassMemoryBasePages massMemoryBasePages=null;

//      FrontEndFirmwareVersionBasePage frontEndFirmwareVersionBasePage=null;
      OperatingSetUpBasePage operatingSetUpBasePage=null;
//      FirmwareOptionsBasePage firmwareOptionsBasePage=null;
//      ModelTypeBasePage modelTypeBasePage=null;
//      MeterKhBasePage meterKhBasePage=null;
//      RegisterConfigurationBasePage registerConfigurationBasePage=null;
      FirmwareAndSoftwareRevision firmwareAndSoftwareRevision=null;
//      RegisterMultiplierBasePage registerMultiplierBasePage=null;
      MassMemoryRecordBasePage currentMassMemoryRecordBasePage=null;
      PulseMultiplierAndDisplayUnits pulseMultiplierAndDisplayUnits=null;        
      
    /** Creates a new instance of BasePagesFactory */
    public BasePagesFactory(Datastar datastar) {
        this.setDatastar(datastar);
    }
    
    public PulseMultiplierAndDisplayUnits getPulseMultiplierAndDisplayUnits() throws IOException {
        if (pulseMultiplierAndDisplayUnits==null) {
            pulseMultiplierAndDisplayUnits = new PulseMultiplierAndDisplayUnits(this);
            pulseMultiplierAndDisplayUnits.invoke();
        }
        return pulseMultiplierAndDisplayUnits;
    }            
    
    public void writeBasePage(int address, byte[] data) throws IOException {
        AddressWriteBasePage a = new AddressWriteBasePage(this);
        a.setAddress(address);
        a.setData(data);
        a.invoke();
    }
    
    public void setRealTimeBasePage() throws IOException {
        RealTimeBasePage o = new RealTimeBasePage(this);
        TimeZone tz = getProtocolLink().getTimeZone();
        if (!getOperatingSetUpBasePage().isDstEnabled()) {
            tz = ProtocolUtils.getWinterTimeZone(tz);
        }
        
        o.setCalendar(ProtocolUtils.getCalendar(tz));
        o.invoke();
    }
    
    public MassMemoryRecordBasePage getCurrentMassMemoryRecordBasePage() throws IOException {
        if (currentMassMemoryRecordBasePage==null) {
            currentMassMemoryRecordBasePage = new MassMemoryRecordBasePage(this);
            currentMassMemoryRecordBasePage.setOffset(getMassMemoryBasePages().getStartOffsetOfCurrentRecord());
            currentMassMemoryRecordBasePage.setOnlyRegisters(true);
            currentMassMemoryRecordBasePage.invoke();
        }
        return currentMassMemoryRecordBasePage;
    }        
    
    public FirmwareAndSoftwareRevision getFirmwareAndSoftwareRevision() throws IOException {
        if (firmwareAndSoftwareRevision==null) {
            firmwareAndSoftwareRevision = new FirmwareAndSoftwareRevision(this);
            firmwareAndSoftwareRevision.invoke();
        }
        return firmwareAndSoftwareRevision;
    }    
    
//    public RegisterConfigurationBasePage getRegisterConfigurationBasePage() throws IOException {
//        if (registerConfigurationBasePage==null) {
//            registerConfigurationBasePage = new RegisterConfigurationBasePage(this);
//            registerConfigurationBasePage.invoke();
//        }
//        return registerConfigurationBasePage;
//    }    
//    
//    public MeterKhBasePage getMeterKhBasePage() throws IOException {
//        if (meterKhBasePage==null) {
//            meterKhBasePage = new MeterKhBasePage(this);
//            meterKhBasePage.invoke();
//        }
//        return meterKhBasePage;
//    }    
//    
//    public ModelTypeBasePage getModelTypeBasePage() throws IOException {
//        if (modelTypeBasePage==null) {
//            modelTypeBasePage = new ModelTypeBasePage(this);
//            modelTypeBasePage.invoke();
//        }
//        return modelTypeBasePage;
//    }    
//    
//    public FirmwareOptionsBasePage getFirmwareOptionsBasePage() throws IOException {
//        if (firmwareOptionsBasePage==null) {
//            firmwareOptionsBasePage = new FirmwareOptionsBasePage(this);
//            firmwareOptionsBasePage.invoke();
//        }
//        return firmwareOptionsBasePage;
//    }    
//    
    public OperatingSetUpBasePage getOperatingSetUpBasePage() throws IOException {
        if (operatingSetUpBasePage==null) {
            operatingSetUpBasePage = new OperatingSetUpBasePage(this);
            operatingSetUpBasePage.invoke();
        }
        return operatingSetUpBasePage;
    }    
    
//    public FrontEndFirmwareVersionBasePage getFrontEndFirmwareVersionBasePage() throws IOException {
//        if (frontEndFirmwareVersionBasePage==null) {
//            frontEndFirmwareVersionBasePage = new FrontEndFirmwareVersionBasePage(this);
//            frontEndFirmwareVersionBasePage.invoke();
//        }
//        return frontEndFirmwareVersionBasePage;
//    }    
    

    public MassMemoryBasePages getMassMemoryBasePages() throws IOException {
        return getMassMemoryBasePages(false);
    }
    
    public MassMemoryBasePages getMassMemoryBasePages(boolean refresh) throws IOException {
        if ((massMemoryBasePages==null) || (refresh)){
            massMemoryBasePages = new MassMemoryBasePages(this);
            massMemoryBasePages.invoke();
        }
        return massMemoryBasePages;
    }

    public Datastar getDatastar() {
        return datastar;
    }
    
    public void setDatastar(Datastar datastar) {
        this.datastar = datastar;
    }
    
    public ProtocolLink getProtocolLink() {
        return datastar;
    }    
    
    public RealTimeBasePage getRealTimeBasePage() throws IOException {
        RealTimeBasePage o = new RealTimeBasePage(this);
        o.invoke();
        return o;
    }
    
//    public SelfreadIndexBasePage getSelfreadIndexBasePage() throws IOException {
//        SelfreadIndexBasePage o = new SelfreadIndexBasePage(this);
//        o.invoke();
//        return o;
//        
//    }    
//    
//    public SelfreadTimestampBasePage getSelfreadTimestampBasePage(int index) throws IOException {
//        SelfreadTimestampBasePage o = new SelfreadTimestampBasePage(this);
//        o.setIndex(index);
//        o.invoke();
//        return o;
//        
//    }   
//    
//    public RegisterBasePage getRegisterBasePage(Register register) throws IOException {
//        RegisterBasePage o = new RegisterBasePage(this);
//        o.setRegister(register);
//        o.invoke();
//        if (register.getAddress2() != -1)
//            o.invoke();
//        return o;
//        
//    }    


    public MassMemoryRecordBasePage getMassMemoryRecordBasePageByRecordNr(int recordNr) throws IOException {
        MassMemoryRecordBasePage o = new MassMemoryRecordBasePage(this);
        o.setRecordNr(recordNr);
        o.invoke();
        return o;
        
    }
    
    public KYZDividersBasePage getKYZDividersBasePage() throws IOException {
        KYZDividersBasePage o = new KYZDividersBasePage(this);
        o.invoke();
        return o;
        
    }

    public DataBuffersBasePage getDataBuffersBasePage() throws IOException {
        DataBuffersBasePage o = new DataBuffersBasePage(this);
        o.invoke();
        return o;
        
    }            
}
