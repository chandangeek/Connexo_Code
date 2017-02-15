/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TableFactory.java
 *
 * Created on 18 oktober 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class StandardTableFactory extends TableFactory {

    public static final int CURRENT_REGISTER_DATA_TABLE=23;
    public static final int PREVIOUS_SEASON_DATA_TABLE=24;
    public static final int PREVIOUS_DEMAND_RESET_DATA_TABLE=25;
    public static final int SELF_READ_DATA_TABLE=26;

    public static final int CLOCK_TABLE=52;
    public static final int CLOCK_STATE_TABLE=55;


    private C12ProtocolLink c12ProtocolLink;

    // cached tables
    private ManufacturerIdentificationTable manufacturerIdentificationTable=null;
    private ConfigurationTable configurationTable=null;
    private EndDeviceModeAndStatusTable endDeviceModeAndStatusTable=null;
    private PendingStatusTable pendingStatusTable=null;
    private DeviceIdentificationTable deviceIdentificationTable=null;
    private ActualSourcesLimitingTable actualSourcesLimitingTable=null;
    private DemandControlTable demandControlTable=null;
    private DataControlTable dataControlTable=null;
    private ConstantsTable constantsTable=null;
    private SourceDefinitionTable sourceDefinitionTable=null;
    private DimensionRegisterTable dimensionRegisterTable=null;
    private ActualRegisterTable actualRegisterTable=null;
    private DataSelectionTable dataSelectionTable=null;
    private DimensionTimeAndTOUTable dimensionTimeAndTOUTable=null;
    private TimeOffsetTable timeOffsetTable=null;
    private CalendarTable calendarTable=null;
    private DimensionLoadProfileTable dimensionLoadProfileTable=null;
    private ActualLoadProfileTable actualLoadProfileTable=null;
    private LoadProfileControlTable loadProfileControlTable=null;
    private ActualTimeAndTOUTable actualTimeAndTOUTable=null;
    private DimensionLogTable dimensionLogTable=null;
    private ActualLogTable actualLogTable=null;
    private EventsIdentificationTable eventsIdentificationTable=null;
    private HistoryLogControlTable historyLogControlTable=null;
    private EventLogControlTable eventLogControlTable=null;
    private UnitOfMeasureEntryTable unitOfMeasureEntryTable=null;
    private UtilityInformationTable utilityInformationTable=null;
    private LoadProfileStatusTable loadProfileStatusTableCached=null;

    // DECADE 90
    private ActualTelephoneTable actualTelephoneTable=null;
    private DimensionTelephoneTable dimensionTelephoneTable=null;
    private GlobalParametersTable globalParametersTable=null;
    private OriginateParametersTable originateParametersTable=null;
    private OriginateScheduleTable originateScheduleTable=null;
    private AnswerParameters answerParameters=null;
    private CallPurpose callPurpose=null;
    private CallStatus callStatus=null;

    /** Creates a new instance of TableFactory */
    public StandardTableFactory(C12ProtocolLink c12ProtocolLink) {
        this.c12ProtocolLink = c12ProtocolLink;
    }

    public C12ProtocolLink getC12ProtocolLink() {
        return c12ProtocolLink;
    }

    public StandardTableFactory getStandardTableFactory() {
        return this;
    }

    public CallStatus getCallStatus() throws IOException {
        if (callStatus==null) {
            callStatus = new CallStatus(this);
            callStatus.build();
        }
        return callStatus;
    }

    public CallPurpose getCallPurpose() throws IOException {
        if (callPurpose==null) {
            callPurpose = new CallPurpose(this);
            callPurpose.build();
        }
        return callPurpose;
    }

    public AnswerParameters getAnswerParameters() throws IOException {
        if (answerParameters==null) {
            answerParameters = new AnswerParameters(this);
            answerParameters.build();
        }
        return answerParameters;
    }

    public OriginateScheduleTable getOriginateScheduleTable() throws IOException {
        if (originateScheduleTable==null) {
            originateScheduleTable = new OriginateScheduleTable(this);
            originateScheduleTable.build();
        }
        return originateScheduleTable;
    }

    public OriginateParametersTable getOriginateParametersTable() throws IOException {
        if (originateParametersTable==null) {
            originateParametersTable = new OriginateParametersTable(this);
            originateParametersTable.build();
        }
        return originateParametersTable;
    }

    public GlobalParametersTable getGlobalParametersTable() throws IOException {
        if (globalParametersTable==null) {
            globalParametersTable = new GlobalParametersTable(this);
            globalParametersTable.build();
        }
        return globalParametersTable;
    }

    public DimensionTelephoneTable getDimensionTelephoneTable() throws IOException {
        if (dimensionTelephoneTable==null) {
            dimensionTelephoneTable = new DimensionTelephoneTable(this);
            dimensionTelephoneTable.build();
        }
        return dimensionTelephoneTable;
    }

    public ActualTelephoneTable getActualTelephoneTable() throws IOException {
        if (actualTelephoneTable==null) {
            actualTelephoneTable = new ActualTelephoneTable(this);
            actualTelephoneTable.build();
        }
        return actualTelephoneTable;
    }

    public UtilityInformationTable getUtilityInformationTable() throws IOException {
        if (utilityInformationTable==null) {
            utilityInformationTable = new UtilityInformationTable(this);
            utilityInformationTable.build();
        }
        return utilityInformationTable;
    }

    public ManufacturerIdentificationTable getManufacturerIdentificationTable() throws IOException {
        if (manufacturerIdentificationTable==null) {
            manufacturerIdentificationTable = new ManufacturerIdentificationTable(this);
            manufacturerIdentificationTable.build();
        }
        return manufacturerIdentificationTable;
    }

    public ConfigurationTable getConfigurationTable() throws IOException {
        if (configurationTable==null) {
            configurationTable = new ConfigurationTable(this);
            configurationTable.build();
        }
        return configurationTable;
    }

    public EndDeviceModeAndStatusTable getEndDeviceModeAndStatusTable() throws IOException {
        if (endDeviceModeAndStatusTable==null) {
            EndDeviceModeAndStatusTable edmast = new EndDeviceModeAndStatusTable(this);
            edmast.build();
            endDeviceModeAndStatusTable = edmast;
        }
        return endDeviceModeAndStatusTable;
    }

    public PendingStatusTable getPendingStatusTable() throws IOException {
        if (pendingStatusTable==null) {
            PendingStatusTable pst = new PendingStatusTable(this);
            pst.build();
            pendingStatusTable = pst;
        }
        return pendingStatusTable;
    }

    public DeviceIdentificationTable getDeviceIdentificationTable() throws IOException {
        if (deviceIdentificationTable==null) {
            DeviceIdentificationTable dit = new DeviceIdentificationTable(this);
            dit.build();
            deviceIdentificationTable = dit;
        }
        return deviceIdentificationTable;
    }

    public ActualSourcesLimitingTable getActualSourcesLimitingTable() throws IOException {
        if (actualSourcesLimitingTable==null) {
            ActualSourcesLimitingTable aslt = new ActualSourcesLimitingTable(this);
            aslt.build();
            actualSourcesLimitingTable = aslt;
        }
        return actualSourcesLimitingTable;
    }

    public DemandControlTable getDemandControlTable() throws IOException {
        if (demandControlTable==null) {
            DemandControlTable dct = new DemandControlTable(this);
            dct.build();
            demandControlTable = dct;
        }
        return demandControlTable;
    }

    public DataControlTable getDataControlTable() throws IOException {
        if (dataControlTable==null) {
            DataControlTable dct = new DataControlTable(this);
            dct.build();
            dataControlTable = dct;
        }
        return dataControlTable;
    }

    public ConstantsTable getConstantsTable() throws IOException {
        return getConstantsTable(false);
    }

    public ConstantsTable getConstantsTable(boolean forceFullRead) throws IOException {
        if (constantsTable==null) {
            ConstantsTable ct = new ConstantsTable(this);
            ct.setForceFullRead(forceFullRead);
            ct.build();
            constantsTable = ct;
        }
        return constantsTable;
    }

    public SourceDefinitionTable getSourceDefinitionTable() throws IOException {
        return getSourceDefinitionTable(false);
    }

    public SourceDefinitionTable getSourceDefinitionTable(boolean forceFullRead) throws IOException {
        if (sourceDefinitionTable==null) {
            SourceDefinitionTable sdt = new SourceDefinitionTable(this);
            sdt.setForceFullRead(forceFullRead);
            sdt.build();
            sourceDefinitionTable = sdt;
        }
        return sourceDefinitionTable;
    }

    public DimensionRegisterTable getDimensionRegisterTable() throws IOException {
        if (dimensionRegisterTable==null) {
            DimensionRegisterTable drt = new DimensionRegisterTable(this);
            drt.build();
            dimensionRegisterTable = drt;
        }
        return dimensionRegisterTable;
    }

    public ActualRegisterTable getActualRegisterTable() throws IOException {
        if (actualRegisterTable==null) {
            ActualRegisterTable art = new ActualRegisterTable(this);
            art.build();
            actualRegisterTable = art;
        }
        return actualRegisterTable;
    }

    public DataSelectionTable getDataSelectionTable() throws IOException {
        if (dataSelectionTable==null) {
            DataSelectionTable dst = new DataSelectionTable(this);
            dst.build();
            dataSelectionTable = dst;
        }
        return dataSelectionTable;
    }

    public DimensionTimeAndTOUTable getDimensionTimeAndTOUTable() throws IOException {
        if (dimensionTimeAndTOUTable==null) {
            DimensionTimeAndTOUTable dttt = new DimensionTimeAndTOUTable(this);
            dttt.build();
            dimensionTimeAndTOUTable = dttt;
        }
        return dimensionTimeAndTOUTable;
    }


    public TimeOffsetTable getTimeOffsetTable() throws IOException {
        if (timeOffsetTable==null) {
            TimeOffsetTable tot = new TimeOffsetTable(this);
            tot.build();
            timeOffsetTable = tot;
        }
        return timeOffsetTable;
    }

    public CalendarTable getCalendarTable() throws IOException {
        if (calendarTable==null) {
            CalendarTable ct = new CalendarTable(this);
            ct.build();
            calendarTable = ct;
        }
        return calendarTable;
    }

    public DimensionLoadProfileTable getDimensionLoadProfileTable() throws IOException {
        if (dimensionLoadProfileTable==null) {
            DimensionLoadProfileTable dlpt = new DimensionLoadProfileTable(this);
            dlpt.build();
            dimensionLoadProfileTable = dlpt;
        }
        return dimensionLoadProfileTable;
    }

    public ActualLoadProfileTable getActualLoadProfileTable() throws IOException {
        if (actualLoadProfileTable==null) {
            ActualLoadProfileTable alpt = new ActualLoadProfileTable(this);
            alpt.build();
            actualLoadProfileTable = alpt;
        }
        return actualLoadProfileTable;
    }

    public LoadProfileControlTable getLoadProfileControlTable() throws IOException {
        if (loadProfileControlTable==null) {
            LoadProfileControlTable lpct = new LoadProfileControlTable(this);
            lpct.build();
            loadProfileControlTable = lpct;
        }
        return loadProfileControlTable;
    }

    public ActualTimeAndTOUTable getActualTimeAndTOUTable() throws IOException {
        if (actualTimeAndTOUTable==null) {
           ActualTimeAndTOUTable atat = new ActualTimeAndTOUTable(this);
           atat.build();
           actualTimeAndTOUTable = atat;
        }
        return actualTimeAndTOUTable;
    }

    public DimensionLogTable getDimensionLogTable() throws IOException {
        if (dimensionLogTable==null) {
           DimensionLogTable dlt = new DimensionLogTable(this);
           dlt.build();
           dimensionLogTable = dlt;
        }
        return dimensionLogTable;
    }

    public ActualLogTable getActualLogTable() throws IOException {
        if (actualLogTable==null) {
           ActualLogTable alt = new ActualLogTable(this);
           alt.build();
           actualLogTable = alt;
        }
        return actualLogTable;
    }

    public EventsIdentificationTable getEventsIdentificationTable() throws IOException {
        if (eventsIdentificationTable==null) {
           EventsIdentificationTable eit = new EventsIdentificationTable(this);
           eit.build();
           eventsIdentificationTable = eit;
        }
        return eventsIdentificationTable;
    }

    public HistoryLogControlTable getHistoryLogControlTable() throws IOException {
        if (historyLogControlTable==null) {
           HistoryLogControlTable hlct = new HistoryLogControlTable(this);
           hlct.build();
           historyLogControlTable = hlct;
        }
        return historyLogControlTable;
    }

    public EventLogControlTable getEventLogControlTable() throws IOException {
        if (eventLogControlTable==null) {
           eventLogControlTable = new EventLogControlTable(this);
           eventLogControlTable.build();
        }
        return eventLogControlTable;
    }

    public UnitOfMeasureEntryTable getUnitOfMeasureEntryTable() throws IOException {
        return getUnitOfMeasureEntryTable(false);
    }

    public UnitOfMeasureEntryTable getUnitOfMeasureEntryTable(boolean forceFullRead) throws IOException {
        if (unitOfMeasureEntryTable==null) {
           unitOfMeasureEntryTable = new UnitOfMeasureEntryTable(this);
           unitOfMeasureEntryTable.setForceFullRead(forceFullRead);
           unitOfMeasureEntryTable.build();
        }
        return unitOfMeasureEntryTable;
    }

    public CurrentRegisterDataTable getCurrentRegisterDataTable() throws IOException {
        return getCurrentRegisterDataTable(false);
    }
    public CurrentRegisterDataTable getCurrentRegisterDataTable(boolean forceFullRead) throws IOException {
        CurrentRegisterDataTable currentRegisterDataTable = new CurrentRegisterDataTable(this);
        currentRegisterDataTable.setForceFullRead(forceFullRead);
        currentRegisterDataTable.build();
        return currentRegisterDataTable;
    }

    public PreviousSeasonDataTable getPreviousSeasonDataTable() throws IOException {
        return getPreviousSeasonDataTable(false);
    }
    public PreviousSeasonDataTable getPreviousSeasonDataTable(boolean forceFullRead) throws IOException {
        PreviousSeasonDataTable previousSeasonDataTable = new PreviousSeasonDataTable(this);
        previousSeasonDataTable.setForceFullRead(forceFullRead);
        previousSeasonDataTable.build();
        return previousSeasonDataTable;
    }

    public PreviousDemandResetDataTable getPreviousDemandResetDataTable() throws IOException {
        return getPreviousDemandResetDataTable(false);
    }
    public PreviousDemandResetDataTable getPreviousDemandResetDataTable(boolean forceFullRead) throws IOException {
        PreviousDemandResetDataTable previousDemandResetDataTable = new PreviousDemandResetDataTable(this);
        previousDemandResetDataTable.setForceFullRead(forceFullRead);
        previousDemandResetDataTable.build();
        return previousDemandResetDataTable;
    }

    public SelfReadDataTable getSelfReadDataTable() throws IOException {
        return getSelfReadDataTable(false);
    }
    public SelfReadDataTable getSelfReadDataTable(boolean forceFullRead) throws IOException {
        SelfReadDataTable selfReadDataTable = new SelfReadDataTable(this);
        selfReadDataTable.setForceFullRead(forceFullRead);
        selfReadDataTable.build();
        return selfReadDataTable;
    }

    public PresentRegisterSelectionTable getPresentRegisterSelectionTable() throws IOException {
        return getPresentRegisterSelectionTable(false);
    }
    public PresentRegisterSelectionTable getPresentRegisterSelectionTable(boolean forceFullRead) throws IOException {
        PresentRegisterSelectionTable presentRegisterSelectionTable = new PresentRegisterSelectionTable(this);
        presentRegisterSelectionTable.setForceFullRead(forceFullRead);
        presentRegisterSelectionTable.build();
        return presentRegisterSelectionTable;
    }

    public PresentRegisterDataTable getPresentRegisterDataTable() throws IOException {
        return getPresentRegisterDataTable(false);
    }
    public PresentRegisterDataTable getPresentRegisterDataTable(boolean forceFullRead) throws IOException {
        PresentRegisterDataTable presentRegisterDataTable = new PresentRegisterDataTable(this);
        presentRegisterDataTable.setForceFullRead(forceFullRead);
        presentRegisterDataTable.build();
        return presentRegisterDataTable;
    }

    public LoadProfileStatusTable getLoadProfileStatusTable() throws IOException {
        LoadProfileStatusTable loadProfileStatusTable = new LoadProfileStatusTable(this);
        loadProfileStatusTable.build();
        if (loadProfileStatusTableCached==null)
            loadProfileStatusTableCached = loadProfileStatusTable;
        return loadProfileStatusTable;
    }

    public LoadProfileStatusTable getLoadProfileStatusTableCached() throws IOException {
        if (loadProfileStatusTableCached==null) {
            loadProfileStatusTableCached = new LoadProfileStatusTable(this);
            loadProfileStatusTableCached.build();
        }
        return loadProfileStatusTableCached;
    }

    public EventLogDataTable getEventLogDataTable() throws IOException {
        EventLogDataTable eventLogDataTable = new EventLogDataTable(this);
        eventLogDataTable.build();
        return eventLogDataTable;
    }

    public EventLogDataTable getEventLogDataTableEventEntryHeader(int eventEntryNr) throws IOException {
        EventLogDataTable eventLogDataTable = new EventLogDataTable(this);
        eventLogDataTable.prepareGetEventEntryHeader(eventEntryNr);
        eventLogDataTable.build();
        return eventLogDataTable;
    }
    public EventLogDataTable getEventLogDataTableEventEntries(int eventEntryNrOffset, int nrOfEventEntriesToRequest) throws IOException {
        EventLogDataTable eventLogDataTable = new EventLogDataTable(this);
        eventLogDataTable.prepareGetEventEntries(eventEntryNrOffset, nrOfEventEntriesToRequest);
        eventLogDataTable.build();
        return eventLogDataTable;
    }
    public EventLogDataTable getEventLogDataTableHeader() throws IOException {
        EventLogDataTable eventLogDataTable = new EventLogDataTable(this);
        eventLogDataTable.prepareGetHeader();
        eventLogDataTable.build();
        return eventLogDataTable;
    }

    public HistoryLogDataTable getHistoryLogDataTable() throws IOException {
        HistoryLogDataTable historyLogDataTable = new HistoryLogDataTable(this);
        historyLogDataTable.build();
        return historyLogDataTable;
    }
    public HistoryLogDataTable getHistoryLogDataTableHeader() throws IOException {
        HistoryLogDataTable historyLogDataTable = new HistoryLogDataTable(this);
        historyLogDataTable.prepareGetHeader();
        historyLogDataTable.build();
        return historyLogDataTable;
    }
    public HistoryLogDataTable getHistoryLogDataTableHistoryEntryHeader(int historyEntryNr) throws IOException {
        HistoryLogDataTable historyLogDataTable = new HistoryLogDataTable(this);
        historyLogDataTable.prepareGetHistoryEntryHeader(historyEntryNr);
        historyLogDataTable.build();
        return historyLogDataTable;
    }
    public HistoryLogDataTable getHistoryLogDataTableHistoryEntries(int historyEntryNrOffset, int nrOfHistoryEntriesToRequest) throws IOException {
        HistoryLogDataTable historyLogDataTable = new HistoryLogDataTable(this);
        historyLogDataTable.prepareGetHistoryEntries(historyEntryNrOffset, nrOfHistoryEntriesToRequest);
        historyLogDataTable.build();
        return historyLogDataTable;
    }

    // set0..3
    public AbstractLoadProfileDataSetTable getLoadProfileDataSetTable(int set) throws IOException {

        if (set==0)
            return getLoadProfileDataSetTable(set, getLoadProfileStatusTable().getLoadProfileSet1Status().getLastBlockElement(), 1);
        if (set==1)
            return getLoadProfileDataSetTable(set, getLoadProfileStatusTable().getLoadProfileSet2Status().getLastBlockElement(), 1);
        if (set==2)
            return getLoadProfileDataSetTable(set, getLoadProfileStatusTable().getLoadProfileSet3Status().getLastBlockElement(), 1);
        if (set==3)
            return getLoadProfileDataSetTable(set, getLoadProfileStatusTable().getLoadProfileSet4Status().getLastBlockElement(), 1);
        throw new IOException("StandardTableFactory, getLoadProfileDataSetTable("+set+"), set "+set+" is invalid!");
    }

    public AbstractLoadProfileDataSetTable getLoadProfileDataSetTable(int set, int blockNr) throws IOException {
        return getLoadProfileDataSetTable(set,blockNr,1);
    }
    public AbstractLoadProfileDataSetTable getLoadProfileDataSetTableIntervalsets(int set, int blockNr, int intervalsets) throws IOException {
        return getLoadProfileDataSetTable(set, blockNr, 1, intervalsets);
    }

    public AbstractLoadProfileDataSetTable getLoadProfileDataSetTableBlockHeader(int set, int blockNr) throws IOException {
        return getLoadProfileDataSetTable(set,blockNr,0);
    }

    public AbstractLoadProfileDataSetTable getLoadProfileDataSetTableNormalRead(int set) throws IOException {
        return getLoadProfileDataSetTable(set,0,-1);
    }

    public AbstractLoadProfileDataSetTable getLoadProfileDataSetTable(int set, int blockNrOffset, int nrOfBlocksToRequest) throws IOException {
        return getLoadProfileDataSetTable(set,blockNrOffset,nrOfBlocksToRequest,-1);
    }
    public AbstractLoadProfileDataSetTable getLoadProfileDataSetTable(int set, int blockNrOffset, int nrOfBlocksToRequest, int intervalsets) throws IOException {
        if (set==0) {
            LoadProfileDataSet1Table loadProfileDataSet1Table = new LoadProfileDataSet1Table(this);
            loadProfileDataSet1Table.setBlockNrOffset(blockNrOffset);
            loadProfileDataSet1Table.setNrOfBlocksToRequest(nrOfBlocksToRequest);
            loadProfileDataSet1Table.setIntervalsets(intervalsets);
            loadProfileDataSet1Table.build();
            return loadProfileDataSet1Table;
        }
        if (set==1) {
            LoadProfileDataSet2Table loadProfileDataSet2Table = new LoadProfileDataSet2Table(this);
            loadProfileDataSet2Table.setBlockNrOffset(blockNrOffset);
            loadProfileDataSet2Table.setNrOfBlocksToRequest(nrOfBlocksToRequest);
            loadProfileDataSet2Table.setIntervalsets(intervalsets);
            loadProfileDataSet2Table.build();
            return loadProfileDataSet2Table;
        }
        if (set==2) {
            LoadProfileDataSet3Table loadProfileDataSet3Table = new LoadProfileDataSet3Table(this);
            loadProfileDataSet3Table.setBlockNrOffset(blockNrOffset);
            loadProfileDataSet3Table.setNrOfBlocksToRequest(nrOfBlocksToRequest);
            loadProfileDataSet3Table.setIntervalsets(intervalsets);
            loadProfileDataSet3Table.build();
            return loadProfileDataSet3Table;
        }
        if (set==3) {
            LoadProfileDataSet4Table loadProfileDataSet4Table = new LoadProfileDataSet4Table(this);
            loadProfileDataSet4Table.setBlockNrOffset(blockNrOffset);
            loadProfileDataSet4Table.setNrOfBlocksToRequest(nrOfBlocksToRequest);
            loadProfileDataSet4Table.setIntervalsets(intervalsets);
            loadProfileDataSet4Table.build();
            return loadProfileDataSet4Table;
        }

        throw new IOException("StandardTableFactory, getLoadProfileDataSetTable("+set+"), set "+set+" is invalid!");


    }


    public Date getTime() throws IOException {
        if (getConfigurationTable().isStdTableUsed(CLOCK_TABLE))
           return getClockTable().getDate();
        else if (getConfigurationTable().isStdTableUsed(CLOCK_STATE_TABLE))
           return getClockStateTable().getClockCalendar();
        else throw new IOException("StandardTableFactory, no Clock table present in the meter!");
    }

    public Date getTime2() throws IOException {
        return getClockStateTable().getClockCalendar();
    }

    public int getTimeDataQualBitfield() throws IOException {
        if (getConfigurationTable().isStdTableUsed(CLOCK_TABLE))
             return getClockTable().getTimeDateQualifier().getTimeDataQualBitfield();
        else if (getConfigurationTable().isStdTableUsed(CLOCK_STATE_TABLE))
             return getClockStateTable().getTimeDateQualifier().getTimeDataQualBitfield();

        throw new IOException("StandardTableFactory, getTimeDataQualBitfield, no clock table supported!");
    }


    public ClockTable getClockTable() throws IOException {
        ClockTable ct = new ClockTable(this);
        ct.build();
        return ct;
    }

    public ClockStateTable getClockStateTable() throws IOException {
        ClockStateTable cst = new ClockStateTable(this);
        cst.build();
        return cst;
    }

    public TimeRemainingTable getTimeRemainingTable() throws IOException {
        TimeRemainingTable trt = new TimeRemainingTable(this);
        trt.build();
        return trt;
    }
}
