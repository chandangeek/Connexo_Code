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

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePageFactory;
import com.energyict.protocolimpl.itron.protocol.ProtocolLink;
import com.energyict.protocolimpl.itron.quantum.Quantum;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class BasePagesFactory extends AbstractBasePageFactory {

    private Quantum quantum;

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
    GeneralSetUpBasePage generalSetUpBasePage=null;
    ProgramTableBasePage programTableBasePage=null;
    ProgramTableBasePage alternateProgramTableBasePage=null;
    MultipliersBasePage multipliersBasePage=null;
    InstantaneousRegMultipliers instantaneousRegMultipliers=null;

    RegisterDataBasePage registerDataBasePagePresent=null;
    RegisterDataBasePage registerDataBasePageLastSeason=null;
    RegisterDataBasePage registerDataBasePageBilling=null;

    VoltageAndCurrentBasePage voltageAndCurrentBasePage=null;

    /** Creates a new instance of BasePagesFactory */
    public BasePagesFactory(Quantum quantum) {
        this.setQuantum(quantum);
    }

    public VoltageAndCurrentBasePage getVoltageAndCurrentBasePage() throws IOException {
        if (voltageAndCurrentBasePage==null) {
            voltageAndCurrentBasePage = new VoltageAndCurrentBasePage(this);
            voltageAndCurrentBasePage.invoke();
        }
        return voltageAndCurrentBasePage;
    }

    public InstantaneousRegMultipliers getInstantaneousRegMultipliers() throws IOException {
        if (instantaneousRegMultipliers==null) {
            instantaneousRegMultipliers = new InstantaneousRegMultipliers(this);
            instantaneousRegMultipliers.invoke();
        }
        return instantaneousRegMultipliers;
    }

    public MultipliersBasePage getMultipliersBasePage() throws IOException {
        if (multipliersBasePage==null) {
            multipliersBasePage = new MultipliersBasePage(this);
            multipliersBasePage.invoke();
        }
        return multipliersBasePage;
    }

    public ProgramTableBasePage getProgramTableBasePage(boolean alternate) throws IOException {
        if (alternate) {
            if (alternateProgramTableBasePage==null) {
                alternateProgramTableBasePage = new ProgramTableBasePage(this);
                alternateProgramTableBasePage.setAlternate(alternate);
                alternateProgramTableBasePage.invoke();
            }
            return alternateProgramTableBasePage;
        }
        else
        {
            if (programTableBasePage==null) {
                programTableBasePage = new ProgramTableBasePage(this);
                programTableBasePage.setAlternate(alternate);
                programTableBasePage.invoke();
            }
            return programTableBasePage;
        }
    }

    public GeneralSetUpBasePage getGeneralSetUpBasePage() throws IOException {
        if (generalSetUpBasePage==null) {
            generalSetUpBasePage = new GeneralSetUpBasePage(this);
            generalSetUpBasePage.invoke();
        }
        return generalSetUpBasePage;
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

    public Quantum getQuantum() {
        return quantum;
    }

    public void setQuantum(Quantum quantum) {
        this.quantum = quantum;
    }

    public ProtocolLink getProtocolLink() {
        return (ProtocolLink)quantum;
    }

    public RealTimeBasePage getRealTimeBasePage() throws IOException {
        RealTimeBasePage o = new RealTimeBasePage(this);
        o.invoke();
        return o;
    }

    public RegisterBasePage getRegisterBasePage(Register register) throws IOException {
        RegisterBasePage o = new RegisterBasePage(this);
        o.setRegister(register);
        o.init();
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

    public FirmwareRevisionBasePage getFirmwareRevisionBasePage() throws IOException {
        FirmwareRevisionBasePage o = new FirmwareRevisionBasePage(this);
        o.invoke();
        return o;

    }

    public RegisterDataBasePage getRegisterDataSelfReadBasePage() throws IOException {
        if (registerDataBasePageBilling == null) {
            registerDataBasePageBilling = getRegisterDataBasePage(getPointerTimeDateRegisterReadingBasePage().getRegisterReadOffset(), 0);
        }
        return registerDataBasePageBilling;
    }

//    public RegisterDataBasePage getRegisterDataSelfReadBasePage(int selfReadSet) throws IOException {
//        return getRegisterDataBasePage(getPointerTimeDateRegisterReadingBasePage().getRegisterReadOffset(), selfReadSet);
//    }

    public RegisterDataBasePage getRegisterDataLastSeasonBasePage() throws IOException {
        if (registerDataBasePageLastSeason == null) {
            registerDataBasePageLastSeason = getRegisterDataBasePage(306,-1);
        }
        return registerDataBasePageLastSeason;
    }

    public RegisterDataBasePage getRegisterDataBasePage() throws IOException {
        if (registerDataBasePageBilling == null) {
            registerDataBasePageBilling = getRegisterDataBasePage(831,-1);
        }
        return registerDataBasePageBilling;    }

    private RegisterDataBasePage getRegisterDataBasePage(int offset, int selfReadSet) throws IOException {
        RegisterDataBasePage o = new RegisterDataBasePage(this);
        o.setSelfReadSet(selfReadSet);
        o.setOffset(offset);
        o.invoke();
        return o;
    }

    public PointerTimeDateRegisterReadingBasePage getPointerTimeDateRegisterReadingBasePage() throws IOException {
        PointerTimeDateRegisterReadingBasePage o = new PointerTimeDateRegisterReadingBasePage(this);
        o.invoke();
        return o;
    }

}
