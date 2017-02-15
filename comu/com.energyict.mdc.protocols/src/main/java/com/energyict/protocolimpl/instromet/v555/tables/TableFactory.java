/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.instromet.v555.tables;

import com.energyict.protocolimpl.instromet.v555.CommandFactory;
import com.energyict.protocolimpl.instromet.v555.Instromet555;

import java.io.IOException;
import java.util.Date;

public class TableFactory {

	private CorrectorInformationTable correctorInformationTable = null;
	private GasCompositionDataTable gasCompositionDataTable = null;
	private PulseInputSetupTable pulseInputSetupTable = null;
	private ErrorAlarmMasksTable errorAlarmMasksTable = null;
	private CountersTable countersTable = null;
	private LoggingConfigurationTable loggingConfigurationTable = null;
	private LoggedDataTable loggedDataTable = null;
	private ActiveDataTable activeDataTable = null;
	private PeakHourPeakDayTable peakHourPeakDayDable = null;

	private Instromet555 instromet555 = null;

	public TableFactory(Instromet555 instromet555) {
        this.instromet555=instromet555;
    }

    public CommandFactory getCommandFactory() {
        return instromet555.getCommandFactory();
    }

    public Instromet555 getInstromet555() {
        return instromet555;
    }

	public CorrectorInformationTable getCorrectorInformationTable() throws IOException {
        //if (correctorInformationTable==null) {
        	correctorInformationTable = new CorrectorInformationTable(this);
        	correctorInformationTable.build();
        //}
        return correctorInformationTable;
    }

	public GasCompositionDataTable getGasCompositionDataTable() throws IOException {
        if (gasCompositionDataTable==null) {
        	gasCompositionDataTable = new GasCompositionDataTable(this);
        	gasCompositionDataTable.build();
        }
        return gasCompositionDataTable;
    }

	public PulseInputSetupTable getPulseInputSetupTable() throws IOException {
        if (pulseInputSetupTable==null) {
        	pulseInputSetupTable = new PulseInputSetupTable(this);
        	pulseInputSetupTable.build();
        }
        return pulseInputSetupTable;
    }

	public ErrorAlarmMasksTable getErrorAlarmMasksTable() throws IOException {
        if (errorAlarmMasksTable==null) {
        	errorAlarmMasksTable = new ErrorAlarmMasksTable(this);
        	errorAlarmMasksTable.build();
        }
        return errorAlarmMasksTable;
    }

	public CountersTable getCountersTable() throws IOException {
        if (countersTable==null) {
        	countersTable = new CountersTable(this);
        	countersTable.build();
        }
        return countersTable;
    }

	public LoggingConfigurationTable getLoggingConfigurationTable() throws IOException {
        if (loggingConfigurationTable==null) {
        	loggingConfigurationTable = new LoggingConfigurationTable(this);
        	loggingConfigurationTable.build();
        }
        return loggingConfigurationTable;
    }

	public LoggedDataTable getLoggedDataTable(Date lastReading) throws IOException {
        if (loggedDataTable==null) {
        	loggedDataTable = new LoggedDataTable(this, lastReading);
        	loggedDataTable.build();
        }
        return loggedDataTable;
    }

	public ActiveDataTable getActiveDataTable() throws IOException {
        if (activeDataTable==null) {
        	activeDataTable = new ActiveDataTable(this);
        	activeDataTable.build();
        }
        return activeDataTable;
    }

	public PeakHourPeakDayTable getPeakHourPeakDayTable() throws IOException {
        if (peakHourPeakDayDable==null) {
        	peakHourPeakDayDable = new PeakHourPeakDayTable(this);
        	peakHourPeakDayDable.build();
        }
        return peakHourPeakDayDable;
    }

}
