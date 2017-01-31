/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataDefinitionFactory.java
 *
 * Created on 8 december 2006, 15:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.AbstractRemoteProcedure;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DataDefinitionFactory {

    private ProtocolLink protocolLink;

    EnergyRegisterConfiguration energyRegisterConfiguration=null;
    DemandRegisterConfiguration demandRegisterConfiguration=null;
    SelfReadRegisterConfiguration selfReadRegisterConfiguration=null;
    MassMemoryConfiguration massMemoryConfiguration0=null;
    MassMemoryConfiguration massMemoryConfiguration1=null;
    SelfReadGeneralConfiguration selfReadGeneralConfiguration=null;
    GeneralDiagnosticInfo generalDiagnosticInfo=null;
    DefaultViewIdConfiguration defaultViewIdConfiguration=null;
    GeneralEnergyConfiguration generalEnergyConfiguration=null;
    GeneralDemandConfiguration generalDemandConfiguration=null;
    MultiTariffScheduleGeneralParameters multiTariffScheduleGeneralParameters=null;
    MultiTariffRateScheduleGeneralParameters multiTariffRateScheduleGeneralParameters=null;
    MeterSetup meterSetup=null;

    /** Creates a new instance of DataDefinitionFactory */
    public DataDefinitionFactory(ProtocolLink protocolLink) {
        this.setProtocolLink(protocolLink);
    }


    public MeterSetup getMeterSetup() throws IOException {
        if (meterSetup==null) {
            meterSetup = new MeterSetup(this);
            meterSetup.invokeRead();
        }
        return meterSetup;
    }

    public MultiTariffRateScheduleGeneralParameters getMultiTariffRateScheduleGeneralParameters() throws IOException {
        if (multiTariffRateScheduleGeneralParameters==null) {
            multiTariffRateScheduleGeneralParameters = new MultiTariffRateScheduleGeneralParameters(this);
            multiTariffRateScheduleGeneralParameters.invokeRead();
        }
        return multiTariffRateScheduleGeneralParameters;
    }

    public boolean isTOUMeter() throws IOException {
        return getMultiTariffScheduleGeneralParameters()!=null;
    }

    public MultiTariffScheduleGeneralParameters getMultiTariffScheduleGeneralParameters() throws IOException {
        if (multiTariffScheduleGeneralParameters==null) {
            multiTariffScheduleGeneralParameters = new MultiTariffScheduleGeneralParameters(this);
            try {
                multiTariffScheduleGeneralParameters.invokeRead();
            }
            catch(ReplyException e) {
                if (e.getAbstractReplyDataError().isInvalidObject()) {
                    multiTariffScheduleGeneralParameters=null;
                    return null;
                }
                throw e;
            }
        }
        return multiTariffScheduleGeneralParameters;
    }

    public GeneralDemandConfiguration getGeneralDemandConfiguration() throws IOException {
        if (generalDemandConfiguration==null) {
            generalDemandConfiguration = new GeneralDemandConfiguration(this);
            generalDemandConfiguration.invokeRead();
        }
        return generalDemandConfiguration;
    }

    public GeneralEnergyConfiguration getGeneralEnergyConfiguration() throws IOException {
        if (generalEnergyConfiguration==null) {
            generalEnergyConfiguration = new GeneralEnergyConfiguration(this);
            generalEnergyConfiguration.invokeRead();
        }
        return generalEnergyConfiguration;
    }


    public DefaultViewIdConfiguration getDefaultViewIdConfiguration() throws IOException {
        if (defaultViewIdConfiguration==null) {
            defaultViewIdConfiguration = new DefaultViewIdConfiguration(this);
            defaultViewIdConfiguration.invokeRead();
        }
        return defaultViewIdConfiguration;
    }

    public GeneralDiagnosticInfo getGeneralDiagnosticInfo() throws IOException {
        if (generalDiagnosticInfo==null) {
            generalDiagnosticInfo = new GeneralDiagnosticInfo(this);
            generalDiagnosticInfo.invokeRead();
        }
        return generalDiagnosticInfo;
    }

    public SelfReadGeneralConfiguration getSelfReadGeneralConfiguration() throws IOException {
        if (selfReadGeneralConfiguration == null) {
            selfReadGeneralConfiguration = new SelfReadGeneralConfiguration(this);
            selfReadGeneralConfiguration.invokeRead();
        }
        return selfReadGeneralConfiguration;
    }

    public MassMemoryConfiguration getMassMemoryConfiguration(int index) throws IOException {

        if (index==0) {
            try {
                if (massMemoryConfiguration0 == null) {
                    massMemoryConfiguration0 = new MassMemoryConfiguration(this);
                    massMemoryConfiguration0.setIndex(0);
                    massMemoryConfiguration0.invokeRead();
                }
                return massMemoryConfiguration0;
            }
            catch(IOException e) {
               massMemoryConfiguration0=null;
               throw e;
            }
        }
        if (index==1) {
            try {
                if (massMemoryConfiguration1 == null) {
                    massMemoryConfiguration1 = new MassMemoryConfiguration(this);
                    massMemoryConfiguration1.setIndex(1);
                    massMemoryConfiguration1.invokeRead();
                }
                return massMemoryConfiguration1;
            }
            catch(IOException e) {
               massMemoryConfiguration1=null;
               throw e;
            }
        }

        throw new IOException("DataDefinitionFactory, getMassMemoryConfiguration, invalid index"+index);
    }


    public SelfReadRegisterConfiguration getSelfReadRegisterConfiguration() throws IOException {
        if (selfReadRegisterConfiguration == null) {
            selfReadRegisterConfiguration = new SelfReadRegisterConfiguration(this);
            selfReadRegisterConfiguration.setIndex(0);
            selfReadRegisterConfiguration.setRange(30);
            selfReadRegisterConfiguration.invokeRead();
            SelfReadRegisterConfigurationType[] selfReadRegisterConfigurationTypes = new SelfReadRegisterConfigurationType[60];
            System.arraycopy(selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes(), 0, selfReadRegisterConfigurationTypes, 0, 30);
            selfReadRegisterConfiguration.setIndex(30);
            selfReadRegisterConfiguration.setRange(30);
            selfReadRegisterConfiguration.invokeRead();
            System.arraycopy(selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes(), 0, selfReadRegisterConfigurationTypes, 30, 30);
            selfReadRegisterConfiguration.setSelfReadRegisterConfigurationTypes(selfReadRegisterConfigurationTypes);
        }
        return selfReadRegisterConfiguration;
    }

    public DemandRegisterConfiguration getDemandRegisterConfiguration() throws IOException {
        if (demandRegisterConfiguration == null) {
            demandRegisterConfiguration = new DemandRegisterConfiguration(this);
            demandRegisterConfiguration.setIndex(0);
            demandRegisterConfiguration.setRange(30);
            demandRegisterConfiguration.invokeRead();
            DemandRegister[] demandRegisters = new DemandRegister[60];
            System.arraycopy(demandRegisterConfiguration.getDemandRegisters(), 0, demandRegisters, 0, 30);
            demandRegisterConfiguration.setIndex(30);
            demandRegisterConfiguration.setRange(30);
            demandRegisterConfiguration.invokeRead();
            System.arraycopy(demandRegisterConfiguration.getDemandRegisters(), 0, demandRegisters, 30, 30);
            demandRegisterConfiguration.setDemandRegisters(demandRegisters);
        }
        return demandRegisterConfiguration;
    }

    public EnergyRegisterConfiguration getEnergyRegisterConfiguration() throws IOException {
        if (energyRegisterConfiguration == null) {
            energyRegisterConfiguration = new EnergyRegisterConfiguration(this);
            energyRegisterConfiguration.invokeRead();
        }
        return energyRegisterConfiguration;
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    private void setProtocolLink(ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }




    public void setDLMLSecurity(String password) throws IOException {
        DLMSSecurity dlmsSecurity = new DLMSSecurity(this);
        dlmsSecurity.setPassword(password);
        dlmsSecurity.invokeWrite();
    }

    public void setRemoteProcedureCall(AbstractRemoteProcedure remoteProcedure) throws IOException {
        RemoteProcedureCall rpc = new RemoteProcedureCall(this);
        rpc.setRemoteProcedure(remoteProcedure);
        rpc.invokeWrite();
    }

    public SelfReadGeneralInformation getSelfReadGeneralInformation() throws IOException {
        SelfReadGeneralInformation obj = new SelfReadGeneralInformation(this);
        obj.invokeRead();
        return obj;
    }

    public MeterIDS getMeterIDS() throws IOException {
        MeterIDS obj = new MeterIDS(this);
        obj.invokeRead();
        return obj;
    }

    public RealTime getRealTime() throws IOException {
        RealTime obj = new RealTime(this);
        obj.invokeRead();
        return obj;
    }


    public EnergyRegistersReading getEnergyRegistersReadingTotal() throws IOException {
        return getEnergyRegistersReading(0);
    }

    public EnergyRegistersReading getEnergyRegistersReading(int index) throws IOException {
        EnergyRegistersReading obj = new EnergyRegistersReading(this);
        obj.setIndex(index);
        obj.invokeRead();
        return obj;
    }


    public DemandRegisterReadings getDemandRegisterReadings(int index,int range) throws IOException {
        DemandRegisterReadings obj = new DemandRegisterReadings(this);
        obj.setIndex(index);
        obj.setRange(range);
        obj.invokeRead();
        return obj;
    }

    public MultiplePeaksOrMinimums getMultiplePeaksOrMinimums() throws IOException {
        MultiplePeaksOrMinimums obj = new MultiplePeaksOrMinimums(this);
        obj.invokeRead();
        return obj;
    }

    public MassMemoryInformation getMassMemoryInformation(int index) throws IOException {
        MassMemoryInformation obj = new MassMemoryInformation(this);
        obj.setIndex(index);
        obj.invokeRead();
        return obj;
    }

    public ViewInformation getViewInformation() throws IOException {
        ViewInformation obj = new ViewInformation(this);
        obj.invokeRead();
        return obj;
    }

    public ViewObjectConfig getViewObjectConfig() throws IOException {
        ViewObjectConfig obj = new ViewObjectConfig(this);
        obj.invokeRead();
        return obj;
    }

    public MassMemory getMassMemory() throws IOException {
        MassMemory obj = new MassMemory(this);
        obj.invokeRead();
        return obj;
    }

    public EventLogSummary getEventLogSummary() throws IOException {
        EventLogSummary obj = new EventLogSummary(this);
        obj.invokeRead();
        return obj;
    }


    public SelfReadDataUpload getSelfReadDataUpload() throws IOException {
        SelfReadDataUpload srdu = new SelfReadDataUpload(protocolLink);
        srdu.invoke();
        return srdu;
    }

    public EventLogUpload getEventLogUpload() throws IOException {
        EventLogUpload elu = new EventLogUpload(protocolLink);
        elu.invoke();
        return elu;
    }

} // public class DataDefinitionFactory
