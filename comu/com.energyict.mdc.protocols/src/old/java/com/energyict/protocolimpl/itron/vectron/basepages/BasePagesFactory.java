/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * BasePagesFactory.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePageFactory;
import com.energyict.protocolimpl.itron.protocol.ProtocolLink;
import com.energyict.protocolimpl.itron.vectron.Vectron;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class BasePagesFactory extends AbstractBasePageFactory {

    private Vectron vectron;

//    EnergyRegistersBasePage energyRegistersBasePage=null;
//    EnergyConfigurationBasePage energyConfigurationBasePage=null;
//    RegisterMultiplierBasePage registerMultiplierBasePage=null;
//    DemandRegistersBasePage demandRegistersBasePage=null;
//    PowerFactorBasePage powerFactorBasePage=null;
//    TotalEnergyRegistersBasePage totalEnergyRegistersBasePage=null;
//    RegisterAddressTable registerAddressTable=null;
//    CoincidentDemandSetupTableBasePage coincidentDemandSetupTableBasePage=null;
//    TimeAndDateOfProgramBasePage timeAndDateOfProgramBasePage=null;
    MassMemoryBasePages massMemoryBasePages=null;
//    GeneralSetUpBasePage generalSetUpBasePage=null;
//    ProgramTableBasePage programTableBasePage=null;
//    ProgramTableBasePage alternateProgramTableBasePage=null;
//    MultipliersBasePage multipliersBasePage=null;
//    InstantaneousRegMultipliers instantaneousRegMultipliers=null;
//
//    RegisterDataBasePage registerDataBasePagePresent=null;
//    RegisterDataBasePage registerDataBasePageLastSeason=null;
//    RegisterDataBasePage registerDataBasePageBilling=null;
//
//    VoltageAndCurrentBasePage voltageAndCurrentBasePage=null;
      FrontEndFirmwareVersionBasePage frontEndFirmwareVersionBasePage=null;
      OperatingSetUpBasePage operatingSetUpBasePage=null;
      FirmwareOptionsBasePage firmwareOptionsBasePage=null;
      ModelTypeBasePage modelTypeBasePage=null;
      MeterKhBasePage meterKhBasePage=null;
      RegisterConfigurationBasePage registerConfigurationBasePage=null;
      FirmwareAndSoftwareRevision firmwareAndSoftwareRevision=null;
      RegisterMultiplierBasePage registerMultiplierBasePage=null;

    /** Creates a new instance of BasePagesFactory */
    public BasePagesFactory(Vectron vectron) {
        this.setVectron(vectron);
    }

    public RegisterMultiplierBasePage getRegisterMultiplierBasePage() throws IOException {
        if (registerMultiplierBasePage==null) {
            registerMultiplierBasePage = new RegisterMultiplierBasePage(this);
            registerMultiplierBasePage.invoke();
        }
        return registerMultiplierBasePage;
    }

    public FirmwareAndSoftwareRevision getFirmwareAndSoftwareRevision() throws IOException {
        if (firmwareAndSoftwareRevision==null) {
            firmwareAndSoftwareRevision = new FirmwareAndSoftwareRevision(this);
            firmwareAndSoftwareRevision.invoke();
        }
        return firmwareAndSoftwareRevision;
    }

    public RegisterConfigurationBasePage getRegisterConfigurationBasePage() throws IOException {
        if (registerConfigurationBasePage==null) {
            registerConfigurationBasePage = new RegisterConfigurationBasePage(this);
            registerConfigurationBasePage.invoke();
        }
        return registerConfigurationBasePage;
    }

    public MeterKhBasePage getMeterKhBasePage() throws IOException {
        if (meterKhBasePage==null) {
            meterKhBasePage = new MeterKhBasePage(this);
            meterKhBasePage.invoke();
        }
        return meterKhBasePage;
    }

    public ModelTypeBasePage getModelTypeBasePage() throws IOException {
        if (modelTypeBasePage==null) {
            modelTypeBasePage = new ModelTypeBasePage(this);
            modelTypeBasePage.invoke();
        }
        return modelTypeBasePage;
    }

    public FirmwareOptionsBasePage getFirmwareOptionsBasePage() throws IOException {
        if (firmwareOptionsBasePage==null) {
            firmwareOptionsBasePage = new FirmwareOptionsBasePage(this);
            firmwareOptionsBasePage.invoke();
        }
        return firmwareOptionsBasePage;
    }

    public OperatingSetUpBasePage getOperatingSetUpBasePage() throws IOException {
        if (operatingSetUpBasePage==null) {
            operatingSetUpBasePage = new OperatingSetUpBasePage(this);
            operatingSetUpBasePage.invoke();
        }
        return operatingSetUpBasePage;
    }

    public FrontEndFirmwareVersionBasePage getFrontEndFirmwareVersionBasePage() throws IOException {
        if (frontEndFirmwareVersionBasePage==null) {
            frontEndFirmwareVersionBasePage = new FrontEndFirmwareVersionBasePage(this);
            frontEndFirmwareVersionBasePage.invoke();
        }
        return frontEndFirmwareVersionBasePage;
    }

//    public VoltageAndCurrentBasePage getVoltageAndCurrentBasePage() throws IOException {
//        if (voltageAndCurrentBasePage==null) {
//            voltageAndCurrentBasePage = new VoltageAndCurrentBasePage(this);
//            voltageAndCurrentBasePage.invoke();
//        }
//        return voltageAndCurrentBasePage;
//    }
//
//    public InstantaneousRegMultipliers getInstantaneousRegMultipliers() throws IOException {
//        if (instantaneousRegMultipliers==null) {
//            instantaneousRegMultipliers = new InstantaneousRegMultipliers(this);
//            instantaneousRegMultipliers.invoke();
//        }
//        return instantaneousRegMultipliers;
//    }
//
//    public MultipliersBasePage getMultipliersBasePage() throws IOException {
//        if (multipliersBasePage==null) {
//            multipliersBasePage = new MultipliersBasePage(this);
//            multipliersBasePage.invoke();
//        }
//        return multipliersBasePage;
//    }
//
//    public ProgramTableBasePage getProgramTableBasePage(boolean alternate) throws IOException {
//        if (alternate) {
//            if (alternateProgramTableBasePage==null) {
//                alternateProgramTableBasePage = new ProgramTableBasePage(this);
//                alternateProgramTableBasePage.setAlternate(alternate);
//                alternateProgramTableBasePage.invoke();
//            }
//            return alternateProgramTableBasePage;
//        }
//        else
//        {
//            if (programTableBasePage==null) {
//                programTableBasePage = new ProgramTableBasePage(this);
//                programTableBasePage.setAlternate(alternate);
//                programTableBasePage.invoke();
//            }
//            return programTableBasePage;
//        }
//    }
//
//    public GeneralSetUpBasePage getGeneralSetUpBasePage() throws IOException {
//        if (generalSetUpBasePage==null) {
//            generalSetUpBasePage = new GeneralSetUpBasePage(this);
//            generalSetUpBasePage.invoke();
//        }
//        return generalSetUpBasePage;
//    }
//
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

    public Vectron getVectron() {
        return vectron;
    }

    public void setVectron(Vectron vectron) {
        this.vectron = vectron;
    }

    public ProtocolLink getProtocolLink() {
        return (ProtocolLink)vectron;
    }

    public RealTimeBasePage getRealTimeBasePage() throws IOException {
        RealTimeBasePage o = new RealTimeBasePage(this);
        o.invoke();
        return o;
    }

    public SelfreadIndexBasePage getSelfreadIndexBasePage() throws IOException {
        SelfreadIndexBasePage o = new SelfreadIndexBasePage(this);
        o.invoke();
        return o;

    }

    public SelfreadTimestampBasePage getSelfreadTimestampBasePage(int index) throws IOException {
        SelfreadTimestampBasePage o = new SelfreadTimestampBasePage(this);
        o.setIndex(index);
        o.invoke();
        return o;

    }

    public RegisterBasePage getRegisterBasePage(Register register) throws IOException {
        RegisterBasePage o = new RegisterBasePage(this);
        o.setRegister(register);
        o.invoke();
        if (register.getAddress2() != -1)
            o.invoke();
        return o;

    }

//
//    public RegisterBasePage getRegisterBasePage(Register register) throws IOException {
//        RegisterBasePage o = new RegisterBasePage(this);
//        o.setRegister(register);
//        o.init();
//        return o;
//    }
//
    public MassMemoryRecordBasePage getMassMemoryRecordBasePageByRecordNr(int recordNr) throws IOException {
        MassMemoryRecordBasePage o = new MassMemoryRecordBasePage(this);
        o.setRecordNr(recordNr);
        o.invoke();
        return o;

    }

//    public MassMemoryRecordBasePage getMassMemoryRecordBasePageByAddress(int recordAddress) throws IOException {
//        MassMemoryRecordBasePage o = new MassMemoryRecordBasePage(this);
//        o.setAddress(recordAddress);
//        o.invoke();
//        return o;
//    }
//
//    public FirmwareRevisionBasePage getFirmwareRevisionBasePage() throws IOException {
//        FirmwareRevisionBasePage o = new FirmwareRevisionBasePage(this);
//        o.invoke();
//        return o;
//
//    }
//
//    public RegisterDataBasePage getRegisterDataSelfReadBasePage() throws IOException {
//        if (registerDataBasePageBilling == null) {
//            registerDataBasePageBilling = getRegisterDataBasePage(getPointerTimeDateRegisterReadingBasePage().getRegisterReadOffset(), 0);
//        }
//        return registerDataBasePageBilling;
//    }
//
////    public RegisterDataBasePage getRegisterDataSelfReadBasePage(int selfReadSet) throws IOException {
////        return getRegisterDataBasePage(getPointerTimeDateRegisterReadingBasePage().getRegisterReadOffset(), selfReadSet);
////    }
//
//    public RegisterDataBasePage getRegisterDataLastSeasonBasePage() throws IOException {
//        if (registerDataBasePageLastSeason == null) {
//            registerDataBasePageLastSeason = getRegisterDataBasePage(306,-1);
//        }
//        return registerDataBasePageLastSeason;
//    }
//
//    public RegisterDataBasePage getRegisterDataBasePage() throws IOException {
//        if (registerDataBasePageBilling == null) {
//            registerDataBasePageBilling = getRegisterDataBasePage(831,-1);
//        }
//        return registerDataBasePageBilling;    }
//
//    private RegisterDataBasePage getRegisterDataBasePage(int offset, int selfReadSet) throws IOException {
//        RegisterDataBasePage o = new RegisterDataBasePage(this);
//        o.setSelfReadSet(selfReadSet);
//        o.setOffset(offset);
//        o.invoke();
//        return o;
//    }
//
//    public PointerTimeDateRegisterReadingBasePage getPointerTimeDateRegisterReadingBasePage() throws IOException {
//        PointerTimeDateRegisterReadingBasePage o = new PointerTimeDateRegisterReadingBasePage(this);
//        o.invoke();
//        return o;
//    }

}
