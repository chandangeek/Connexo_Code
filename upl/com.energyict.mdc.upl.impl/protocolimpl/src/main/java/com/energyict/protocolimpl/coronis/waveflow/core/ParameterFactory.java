package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;
import java.util.*;

public interface ParameterFactory {

	int readApplicationStatus() throws IOException;
	void writeApplicationStatus(final int status) throws IOException;
	int readOperatingMode() throws IOException;
	void disableDataLogging() throws IOException;
	void enableDataLoggingPeriodic() throws IOException;
	void manageDataloggingInputs(int nrOfInputs2Enable) throws IOException;
	void writeOperatingMode(final int operatingModeVal, final int mask) throws IOException;
	void writeOperatingMode(final int operatingModeVal) throws IOException;
	Date readTimeDateRTC() throws IOException;
	void writeTimeDateRTC(final Date date) throws IOException;
	int readSamplingPeriod() throws IOException;
	void writeSamplingPeriod(final int samplingPeriodInSeconds) throws IOException;
	void writeSamplingActivationNextHour() throws IOException;
	void writeSamplingActivationType(final int startHour) throws IOException;
	int getProfileIntervalInSeconds() throws IOException;
	BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException;
	Date readBatteryLifeDateEnd() throws IOException;
}
