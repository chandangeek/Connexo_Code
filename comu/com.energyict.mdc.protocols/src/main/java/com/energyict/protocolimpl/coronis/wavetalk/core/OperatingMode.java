package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {
	
	/**
	 * Masks
	 */
	static private int DATALOGGING_MASK=0x000C; // bit 3..2 of operating mode byte
	static private int INPUTSELECTOR_MASK=0x0003; // bit 1..0 of operating mode byte

	/**
	 *  Datalogging control bits in the operation mode byte
	 */
	static private int DATALOGGING_DISABLED=0x0000;
	static private int DATALOGGING_PERIODIC=0x0004;
	static private int DATALOGGING_WEEKLY=0x0008;
	static private int DATALOGGING_MONTHLY=0x000C;

	void disableDataLogging() {
		setMask(DATALOGGING_MASK);
		setOperatingMode(DATALOGGING_DISABLED);
	}
	
	void enableDataLoggingPeriodic() {
		setMask(DATALOGGING_MASK);
		setOperatingMode(DATALOGGING_PERIODIC);
	}
	
	void enableDataLoggingWeekly() {
		setMask(DATALOGGING_MASK);
		setOperatingMode(DATALOGGING_WEEKLY);
	}
	
	void enableDataLoggingMonthly() {
		setMask(DATALOGGING_MASK);
		setOperatingMode(DATALOGGING_MONTHLY);
	}

	void manageInputs(int nrOfInputs2Enable) throws WaveFlowException {
		setMask(INPUTSELECTOR_MASK);
		if ((nrOfInputs2Enable > 4) || (nrOfInputs2Enable < 1)) {
			throw new WaveFlowException("Invalid nr of inputs to manage (1..4), requested ["+nrOfInputs2Enable+"]");
		}
		setOperatingMode(nrOfInputs2Enable-1);
	}

	
	OperatingMode(AbstractWaveTalk waveFlow) {
		super(waveFlow);
	}
	
	@Override
	ParameterId getParameterId() {
		return null;
	}

	@Override
	void parse(byte[] data) throws IOException {
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
