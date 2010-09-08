package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class MeasurementPeriod extends AbstractParameter {

	int measurementPeriod;
	
	final int getMeasurementPeriod() {
		return measurementPeriod;
	}

	final void setMeasurementPeriod(int measurementPeriod) {
		this.measurementPeriod = measurementPeriod;
	}

	MeasurementPeriod(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
		// TODO Auto-generated constructor stub
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.MeasurementPeriod;
	}

	@Override
	void parse(byte[] data) throws IOException {
		measurementPeriod = Utils.toInt(data[0]);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)measurementPeriod};
	}

}
