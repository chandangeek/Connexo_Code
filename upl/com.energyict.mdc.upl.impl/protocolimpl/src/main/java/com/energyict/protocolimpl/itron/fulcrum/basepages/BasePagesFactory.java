/*
 * BasePagesFactory.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocolimpl.itron.fulcrum.Fulcrum;
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
    
    private Fulcrum fulcrum;
    
    EnergyRegistersBasePage energyRegistersBasePage=null;
    EnergyConfigurationBasePage energyConfigurationBasePage=null;
    RegisterMultiplierBasePage registerMultiplierBasePage=null;
    DemandRegistersBasePage demandRegistersBasePage=null;
    PowerFactorBasePage powerFactorBasePage=null;
    TotalEnergyRegistersBasePage totalEnergyRegistersBasePage=null;
    RegisterAddressTable registerAddressTable=null;
    CoincidentDemandSetupTableBasePage coincidentDemandSetupTableBasePage=null;
    TimeAndDateOfProgramBasePage timeAndDateOfProgramBasePage=null;
    MassMemoryBasePages massMemoryBasePages=null;
    OperatingSetUpBasePage operatingSetUpBasePage=null;
    
    /** Creates a new instance of BasePagesFactory */
    public BasePagesFactory(Fulcrum fulcrum) {
        this.setFulcrum(fulcrum);
    }
    

    public OperatingSetUpBasePage getOperatingSetUpBasePage() throws IOException {
        if (operatingSetUpBasePage==null) {
            operatingSetUpBasePage = new OperatingSetUpBasePage(this);
            operatingSetUpBasePage.invoke();
        }
        return operatingSetUpBasePage;
    }

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
    
    public TimeAndDateOfProgramBasePage getTimeAndDateOfProgramBasePage() throws IOException {
        if (timeAndDateOfProgramBasePage==null) {
            timeAndDateOfProgramBasePage = new TimeAndDateOfProgramBasePage(this);
            timeAndDateOfProgramBasePage.invoke();
        }
        return timeAndDateOfProgramBasePage;
    }
    
    public CoincidentDemandSetupTableBasePage getCoincidentDemandSetupTableBasePage() throws IOException {
        if (coincidentDemandSetupTableBasePage==null) {
            coincidentDemandSetupTableBasePage = new CoincidentDemandSetupTableBasePage(this);
            coincidentDemandSetupTableBasePage.invoke();
        }
        return coincidentDemandSetupTableBasePage;
    }
    
    public RegisterAddressTable getRegisterAddressTable() throws IOException {
        if (registerAddressTable==null) {
            registerAddressTable = new RegisterAddressTable(this);
            registerAddressTable.invoke();
        }
        return registerAddressTable;
    }
    
    public TotalEnergyRegistersBasePage getTotalEnergyRegistersBasePage() throws IOException {
        if (totalEnergyRegistersBasePage==null) {
            totalEnergyRegistersBasePage = new TotalEnergyRegistersBasePage(this);
            totalEnergyRegistersBasePage.invoke();
        }
        return totalEnergyRegistersBasePage;
    }
    
    public PowerFactorBasePage getPowerFactorBasePage() throws IOException {
        if (powerFactorBasePage==null) {
            powerFactorBasePage = new PowerFactorBasePage(this);
            powerFactorBasePage.invoke();
        }
        return powerFactorBasePage;
    }
    
    public DemandRegistersBasePage getDemandRegistersBasePage() throws IOException {
        if (demandRegistersBasePage==null) {
            demandRegistersBasePage = new DemandRegistersBasePage(this);
            demandRegistersBasePage.invoke();
        }
        return demandRegistersBasePage;
    }
    
    public RegisterMultiplierBasePage getRegisterMultiplierBasePage() throws IOException {
        if (registerMultiplierBasePage==null) {
            registerMultiplierBasePage = new RegisterMultiplierBasePage(this);
            registerMultiplierBasePage.invoke();
        }
        return registerMultiplierBasePage;
    }
    
    public EnergyConfigurationBasePage getEnergyConfigurationBasePage() throws IOException {
        if (energyConfigurationBasePage==null) {
            energyConfigurationBasePage = new EnergyConfigurationBasePage(this);
            energyConfigurationBasePage.invoke();
        }
        return energyConfigurationBasePage;
    }
    
    public EnergyRegistersBasePage getEnergyRegistersBasePage() throws IOException {
        if (energyRegistersBasePage==null) {
            energyRegistersBasePage = new EnergyRegistersBasePage(this);
            energyRegistersBasePage.invoke();
        }
        return energyRegistersBasePage;
    }
    
    public ProtocolLink getProtocolLink() {
        return getFulcrum();
    }
    

    public RealTimeBasePage getRealTimeBasePage() throws IOException {
        RealTimeBasePage o = new RealTimeBasePage(this);
        o.invoke();
        return o;
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
    
    
    public MeterIdentificationBasePages getMeterIdentificationBasePages() throws IOException {
        MeterIdentificationBasePages o = new MeterIdentificationBasePages(this);
        o.invoke();
        return o;
    }
    
    
    
    public KYZInputTable getKYZInputTable() throws IOException {
        KYZInputTable o = new KYZInputTable(this);
        o.invoke();
        return o;
    }
    
    public RegisterBasePage getRegisterBasePage(Register register) throws IOException {
        RegisterBasePage o = new RegisterBasePage(this);
        o.setRegister(register);
        o.invoke();
        return o;
    }
    
    public SelfReadAreasBasePage getSelfReadAreasBasePage(int selfReadSet) throws IOException {
        SelfReadAreasBasePage o = new SelfReadAreasBasePage(this);
        o.setSelfReadSet(selfReadSet);
        o.invoke();
        return o;
    }
    
    public SelfreadIndexBasePage getSelfreadIndexBasePage() throws IOException {
        SelfreadIndexBasePage o = new SelfreadIndexBasePage(this);
        o.invoke();
        return o;
        
    }
    
    public MassMemoryRecordBasePage getMassMemoryRecordBasePageByRecordNr(int recordNr) throws IOException {
        MassMemoryRecordBasePage o = new MassMemoryRecordBasePage(this);
        o.setRecordNr(recordNr);
        o.invoke();
        return o;
        
    }
    
    public MassMemoryRecordBasePage getMassMemoryRecordBasePageByAddress(int recordAddress) throws IOException {
        MassMemoryRecordBasePage o = new MassMemoryRecordBasePage(this);
        o.setAddress(recordAddress);
        o.invoke();
        return o;
    }

    public void writeBasePage(int address, byte[] data) throws IOException {
        AddressWriteBasePage a = new AddressWriteBasePage(this);
        a.setAddress(address);
        a.setData(data);
        a.invoke();
    }

    public Fulcrum getFulcrum() {
        return fulcrum;
    }

    public void setFulcrum(Fulcrum fulcrum) {
        this.fulcrum = fulcrum;
    }
    
}
