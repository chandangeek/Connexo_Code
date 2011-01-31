/*
 * ManufacturerTableFactory.java
 *
 * Created on 18 oktober 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.elster.a1800.tables.PowerQualityMonitorLog;
import com.energyict.protocolimpl.elster.a1800.tables.PowerQualityMonitorTests;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ManufacturerTableFactory extends TableFactory {
    
    private C12ProtocolLink c12ProtocolLink;
    
    // cached tables
    private ElectricitySpecificProductSpec electricitySpecificProductSpec=null;
    private StatusTable statusTable=null;
    private PrimaryMeteringInformation primaryMeteringInformation=null;
    private FactoryDefaultMeteringInformation factoryDefaultMeteringInformation=null;
    private SourceDefinitionTable sourceDefinitionTable=null;
    private SummationSnapshotTable summationSnapshotTable=null;
    private PreviousIntervalDemand previousIntervalDemand=null;
    private CallStatusTableForRemotePorts callStatusTableForRemotePorts=null;  
    private OptionBoardScratchPad optionBoardScratchPad=null;
    private OutageModemConfiguration outageModemConfiguration=null;
    private OutageModemStatus outageModemStatus=null;
    private RemoteConfigurationConfiguration remoteConfigurationConfiguration=null;
    private RemoteCommunicationStatus remoteCommunicationStatus=null;
    private AnswerParametersTableForRemotePorts answerParametersTableForRemotePorts=null;
    private CallPurposeTableForRemotePorts callPurposeTableForRemotePorts=null;
    private GlobalParametersTablesForRemotePorts globalParametersTablesForRemotePorts=null;
    private OriginateParametersTableForRemotePorts originateParametersTableForRemotePorts=null;
    private OriginateSchedulingTablesforRemotePorts originateSchedulingTablesforRemotePorts=null;
    private PowerQualityMonitorLog powerQualityMonitorLog=null;
    private PowerQualityMonitorTests powerQualityMonitorTests=null;

    /** Creates a new instance of TableFactory */
    public ManufacturerTableFactory(C12ProtocolLink c12ProtocolLink) {
        this.c12ProtocolLink = c12ProtocolLink;
    }

    public C12ProtocolLink getC12ProtocolLink() {
        return c12ProtocolLink;
    }
    public OriginateSchedulingTablesforRemotePorts getOriginateSchedulingTablesforRemotePorts() throws IOException {  
        if (originateSchedulingTablesforRemotePorts==null) {
            originateSchedulingTablesforRemotePorts = new OriginateSchedulingTablesforRemotePorts(this);
            originateSchedulingTablesforRemotePorts.build();
        }
        return originateSchedulingTablesforRemotePorts;
    }
    
    public OriginateParametersTableForRemotePorts getOriginateParametersTableForRemotePorts() throws IOException {  
        if (originateParametersTableForRemotePorts==null) {
            originateParametersTableForRemotePorts = new OriginateParametersTableForRemotePorts(this);
            originateParametersTableForRemotePorts.build();
        }
        return originateParametersTableForRemotePorts;
    }
    
    public GlobalParametersTablesForRemotePorts getGlobalParametersTablesForRemotePorts() throws IOException {  
        if (globalParametersTablesForRemotePorts==null) {
            globalParametersTablesForRemotePorts = new GlobalParametersTablesForRemotePorts(this);
            globalParametersTablesForRemotePorts.build();
        }
        return globalParametersTablesForRemotePorts;
    }
    
    public CallPurposeTableForRemotePorts getCallPurposeTableForRemotePorts() throws IOException {  
        if (callPurposeTableForRemotePorts==null) {
            callPurposeTableForRemotePorts = new CallPurposeTableForRemotePorts(this);
            callPurposeTableForRemotePorts.build();
        }
        return callPurposeTableForRemotePorts;
    }
    
    public AnswerParametersTableForRemotePorts getAnswerParametersTableForRemotePorts() throws IOException {  
        if (answerParametersTableForRemotePorts==null) {
            answerParametersTableForRemotePorts = new AnswerParametersTableForRemotePorts(this);
            answerParametersTableForRemotePorts.build();
        }
        return answerParametersTableForRemotePorts;
    }
    
    public RemoteCommunicationStatus getRemoteCommunicationStatus() throws IOException {  
        if (remoteCommunicationStatus==null) {
            remoteCommunicationStatus = new RemoteCommunicationStatus(this);
            remoteCommunicationStatus.build();
        }
        return remoteCommunicationStatus;
    }
    
    public RemoteConfigurationConfiguration getRemoteConfigurationConfiguration() throws IOException {  
        if (remoteConfigurationConfiguration==null) {
            remoteConfigurationConfiguration = new RemoteConfigurationConfiguration(this);
            remoteConfigurationConfiguration.build();
        }
        return remoteConfigurationConfiguration;
    }
    
    public OutageModemStatus getOutageModemStatus() throws IOException {  
        if (outageModemStatus==null) {
            outageModemStatus = new OutageModemStatus(this);
            outageModemStatus.build();
        }
        return outageModemStatus;
    }
    
    public OutageModemConfiguration getOutageModemConfiguration() throws IOException {  
        if (outageModemConfiguration==null) {
            outageModemConfiguration = new OutageModemConfiguration(this);
            outageModemConfiguration.build();
        }
        return outageModemConfiguration;
    }
    
    public OptionBoardScratchPad getOptionBoardScratchPad() throws IOException {  
        if (optionBoardScratchPad==null) {
            optionBoardScratchPad = new OptionBoardScratchPad(this);
            optionBoardScratchPad.build();
        }
        return optionBoardScratchPad;
    }

    public CallStatusTableForRemotePorts getCallStatusTableForRemotePorts() throws IOException {  
        if (callStatusTableForRemotePorts==null) {
            callStatusTableForRemotePorts = new CallStatusTableForRemotePorts(this);
            callStatusTableForRemotePorts.build();
        }
        return callStatusTableForRemotePorts;
    }
    
    public ElectricitySpecificProductSpec getElectricitySpecificProductSpec() throws IOException {  
        if (electricitySpecificProductSpec==null) {
            electricitySpecificProductSpec = new ElectricitySpecificProductSpec(this);
            electricitySpecificProductSpec.build();
        }
        return electricitySpecificProductSpec;
    }
    
    public StatusTable getStatusTable() throws IOException {  
        if (statusTable==null) {
            statusTable = new StatusTable(this);
            statusTable.build();
        }
        return statusTable;
    }
    
    public PrimaryMeteringInformation getPrimaryMeteringInformation() throws IOException {  
        if (primaryMeteringInformation==null) {
            primaryMeteringInformation = new PrimaryMeteringInformation(this);
            primaryMeteringInformation.build();
        }
        return primaryMeteringInformation;
    }
    
    public FactoryDefaultMeteringInformation getFactoryDefaultMeteringInformation() throws IOException {  
        if (factoryDefaultMeteringInformation==null) {
            factoryDefaultMeteringInformation = new FactoryDefaultMeteringInformation(this);
            factoryDefaultMeteringInformation.build();
        }
        return factoryDefaultMeteringInformation;
    }
    
    public SourceDefinitionTable getSourceDefinitionTable() throws IOException {  
        if (sourceDefinitionTable==null) {
            sourceDefinitionTable = new SourceDefinitionTable(this);
            sourceDefinitionTable.build();
        }
        return sourceDefinitionTable;
    }
    
    public SummationSnapshotTable getSummationSnapshotTable() throws IOException {  
        if (summationSnapshotTable==null) {
            summationSnapshotTable = new SummationSnapshotTable(this);
            summationSnapshotTable.build();
        }
        return summationSnapshotTable;
    }
    
    public PreviousIntervalDemand getPreviousIntervalDemand() throws IOException {  
        if (previousIntervalDemand==null) {
            previousIntervalDemand = new PreviousIntervalDemand(this);
            previousIntervalDemand.build();
        }
        return previousIntervalDemand;
    }
    
    public PowerQualityMonitorLog getPowerQualityMonitorLog() throws IOException {
        if (powerQualityMonitorLog==null) {
            powerQualityMonitorLog = new PowerQualityMonitorLog(this, getPowerQualityMonitorTests().hasLogValue());
            powerQualityMonitorLog.build();
}
        return powerQualityMonitorLog;
    }

    public PowerQualityMonitorTests getPowerQualityMonitorTests() throws IOException {
        if (powerQualityMonitorTests==null) {
            powerQualityMonitorTests = new PowerQualityMonitorTests(this);
            powerQualityMonitorTests.build();
        }
        return powerQualityMonitorTests;
    }


}
