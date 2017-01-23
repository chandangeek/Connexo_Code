package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.mbus.core.DataRecordTypeG_CP16;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MBusInternalLogs extends AbstractRadioCommand {

	private static final int MBUS_INTERNAL_LOGS_SIZE = 91;

	int portId;

	public class HistoricalValue {

		private final Calendar cal;
		private final Quantity value;

		HistoricalValue(final Calendar cal, final Quantity value) {
			super();
			this.cal = cal;
			this.value = value;
		}

		public final Calendar getCal() {
			return cal;
		}

		public final Quantity getValue() {
			return value;
		}

		public String toString() {
			return cal.getTime() + " value: " + value;
		}
	}

	/**
	 * 13 bhistorical last month values
	 */
	List<HistoricalValue> historicalValues = new ArrayList<>();


	public final List<HistoricalValue> getHistoricalValues() {
		return historicalValues;
	}

	MBusInternalLogs(WaveFlow100mW waveFlow100mW, int portId) {
		super(waveFlow100mW);
		this.portId = portId <= 0 ? 0 : 1;
	}

	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.MBusInternalLogs;
	}


	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("MBusInternalLogs for port").append(portId == 0 ? "A" : "B").append("\n");
		for (HistoricalValue o : historicalValues) {
			strBuilder.append(o).append("\n");
		}
		return strBuilder.toString();
	}

	private TimeZone getTimeZone() {
		if (getWaveFlow100mW() == null) {
			return TimeZone.getDefault();
		}
		else {
			return getWaveFlow100mW().getTimeZone();
		}

	}

	@Override
	void parse(byte[] data) throws IOException {

		if (data.length != (MBUS_INTERNAL_LOGS_SIZE + 1)) {
			throw new WaveFlow100mwEncoderException("Invalid datalength for the MBUS Internal Log data requested [" + (MBUS_INTERNAL_LOGS_SIZE + 1) + "], received [" + data.length + "]");
		}

		int offset = 0;
		int verifyPortId = WaveflowProtocolUtils.toInt(data[offset++]) - 1;
		if (verifyPortId != portId) {
			throw new WaveFlow100mwEncoderException("Invalid portId requested [" + (portId == 0 ? "A" : "B") + "], received [" + (verifyPortId == 0 ? "A" : "B") + "]");
		}

		// received 91 bytes of tdata

		while (offset <= MBUS_INTERNAL_LOGS_SIZE) {

			byte[] temp = WaveflowProtocolUtils.getSubArray(data, offset, 2);
			offset += 2;
			DataRecordTypeG_CP16 o = new DataRecordTypeG_CP16(getTimeZone());
			o.parse(temp);
			Calendar calendar = o.getCalendar();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			int scale = Math.abs((WaveflowProtocolUtils.toInt(data[offset++]) & 0x07) - 6);
			long value = ProtocolUtils.getBCD2IntLE(data, offset, 4);
			offset += 4;
			BigDecimal bd = new BigDecimal(value);
			bd = bd.movePointLeft(scale);

			historicalValues.add(new HistoricalValue(calendar, new Quantity(bd, Unit.get(BaseUnit.CUBICMETER))));
		}
	}


	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte) (portId + 1)};
	}

}