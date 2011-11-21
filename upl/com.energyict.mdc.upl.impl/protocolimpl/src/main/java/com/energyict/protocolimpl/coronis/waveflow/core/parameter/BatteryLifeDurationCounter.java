package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class BatteryLifeDurationCounter extends AbstractParameter {

	final double INITIAL_BATTERY_LIFE_COUNT= 0xC15C;
	
	/**
	 * This is the remaining battery life. Use the default value to calculate the remaining life time of the battery
	 */
	private int batteryLifeCounter; 
	
	/**
	 * The remaining battery life count
	 * @return
	 */
	final int getBatteryLifeCounter() {
		return batteryLifeCounter;
	}

	/** 
	 * The remaining battery life in 0..100 % knowing that the initial battery life count is 100 % and the getBatteryLifeCounter() is the remaining
	 * @return the remaining battery life in percentage
	 */
	final public double remainingBatteryLife() {
        double value = 100 - (((INITIAL_BATTERY_LIFE_COUNT * 100) - (getBatteryLifeCounter() * 100)) / INITIAL_BATTERY_LIFE_COUNT);
        return Math.round(value * 100.0) / 100.0;
	}
	
	BatteryLifeDurationCounter(WaveFlow waveFlow) {
		super(waveFlow);
	}

	BatteryLifeDurationCounter(WaveFlow waveFlow, int batteryLifeCounter) {
		super(waveFlow);
        this.batteryLifeCounter = batteryLifeCounter;
	}
	
	@Override
	ParameterId getParameterId() {
		return ParameterId.BatteryLifeDurationCounter;
	}

	@Override
    protected void parse(byte[] data) throws IOException {
		batteryLifeCounter = ProtocolUtils.getInt(data, 0, 2);
	}
	
	protected byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}
}
