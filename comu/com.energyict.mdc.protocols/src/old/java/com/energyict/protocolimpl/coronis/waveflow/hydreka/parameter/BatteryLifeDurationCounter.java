package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.coronis.waveflow.core.ParameterType;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;

import java.io.IOException;

public class BatteryLifeDurationCounter extends AbstractParameter {

	public static final double INITIAL_BATTERY_LIFE_COUNT= 0xF7F490;

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

	public BatteryLifeDurationCounter(WaveFlow waveFlow) {
		super(waveFlow);
        parameterType = ParameterType.Hydreka;
    }

	BatteryLifeDurationCounter(WaveFlow waveFlow, int batteryLifeCounter) {
		super(waveFlow);
        this.batteryLifeCounter = batteryLifeCounter;
	}

	@Override
    protected AbstractParameter.ParameterId getParameterId() {
		return AbstractParameter.ParameterId.BatteryLifeDurationCounterHydreka;
	}

	@Override
    protected void parse(byte[] data) throws IOException {
		batteryLifeCounter = ProtocolUtils.getInt(data, 0, 3);
	}

	protected byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}
}