package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.mdc.protocol.api.UnsupportedException;

import java.io.IOException;

public class BatteryLifeDurationCounter extends AbstractParameter {

	static final int INITIAL_BATTERY_LIFE_COUNT=0xF7F490;


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
	 * The factory initial battery life count value
	 * @return the factory initial battery life count
	 */
	final int initialBatteryLifeCount() {
		return INITIAL_BATTERY_LIFE_COUNT;
	}

	/**
	 * The remaining battery life in 0..100 % knowing that the initial battery life count is 100 % and the getBatteryLifeCounter() is the remaining
	 * @return the remaining battery life in percentage
	 */
	final public int remainingBatteryLife() {
		return 100-(((INITIAL_BATTERY_LIFE_COUNT*100)-(getBatteryLifeCounter()*100))/INITIAL_BATTERY_LIFE_COUNT);
	}

	BatteryLifeDurationCounter(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.BatteryLifeDurationCounter;
	}

	@Override
	void parse(byte[] data) throws IOException {
		batteryLifeCounter = ProtocolUtils.getInt(data, 0, 3);
	}


	byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}


}
