package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.mbus.core.DataRecordTypeG_CP16;
import com.energyict.protocolimpl.utils.ProtocolUtils;

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

		final public Calendar getCal() {
			return cal;
		}
		final public Quantity getValue() {
			return value;
		}

		public String toString() {
			return cal.getTime()+" value: "+value;
		}
	}

	/**
	 * 13 bhistorical last month values
	 */
	List<HistoricalValue> historicalValues = new ArrayList<HistoricalValue>();


	final public List<HistoricalValue> getHistoricalValues() {
		return historicalValues;
	}

	MBusInternalLogs(WaveFlow100mW waveFlow100mW,int portId) {
		super(waveFlow100mW);
		this.portId=portId<=0?0:1;
	}

	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.MBusInternalLogs;
	}



	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("MBusInternalLogs for port"+(portId==0?"A":"B")+"\n");
		for(HistoricalValue o : historicalValues) {
			strBuilder.append(o+"\n");
		}
		return strBuilder.toString();
	}

	private TimeZone getTimeZone() {
		if (getWaveFlow100mW() == null) return TimeZone.getDefault();
		else return getWaveFlow100mW().getTimeZone();

	}

	@Override
	void parse(byte[] data) throws IOException {

		if (data.length != (MBUS_INTERNAL_LOGS_SIZE +1)) {
			throw new WaveFlow100mwEncoderException("Invalid datalength for the MBUS Internal Log data requested ["+(MBUS_INTERNAL_LOGS_SIZE +1)+"], received ["+data.length+"]");
		}

		int offset=0;
		int verifyPortId = WaveflowProtocolUtils.toInt(data[offset++]) - 1;
		if (verifyPortId != portId) {
			throw new WaveFlow100mwEncoderException("Invalid portId requested ["+(portId==0?"A":"B")+"], received ["+(verifyPortId==0?"A":"B")+"]");
		}

		// received 91 bytes of tdata

		while(offset <= MBUS_INTERNAL_LOGS_SIZE) {

			byte[] temp = WaveflowProtocolUtils.getSubArray(data, offset, 2);offset+=2;
			DataRecordTypeG_CP16 o = new DataRecordTypeG_CP16(getTimeZone());
			o.parse(temp);
			Calendar calendar = o.getCalendar();
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			calendar.set(Calendar.MILLISECOND,0);

		    int scale = Math.abs((WaveflowProtocolUtils.toInt(data[offset++])&0x07) - 6);
		    long value = ProtocolUtils.getBCD2IntLE(data, offset, 4); offset+=4;
		    BigDecimal bd = new BigDecimal(value);
		    bd = bd.movePointLeft(scale);

		    historicalValues.add(new HistoricalValue(calendar,new Quantity(bd,Unit.get(BaseUnit.CUBICMETER))));
		}
	}


	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)(portId+1)};
	}


	static public void main(String[] args) {
		byte[] data = new byte[]{0x02,(byte)0x5F,(byte)0x13,(byte)0x14,(byte)0x74,(byte)0x65,(byte)0x06,(byte)0x00,(byte)0x5C,(byte)0x12,(byte)0x14,(byte)0x74,(byte)0x65,(byte)0x06,(byte)0x00,(byte)0x5F,(byte)0x11,(byte)0x14,(byte)0x74,(byte)0x65,(byte)0x06,(byte)0x00,(byte)0x3F,(byte)0x1C,(byte)0x14,(byte)0x96,(byte)0x57,(byte)0x06,(byte)0x00,(byte)0x3E,(byte)0x1B,(byte)0x14,(byte)0x13,(byte)0x36,(byte)0x06,(byte)0x00,(byte)0x3F,(byte)0x1A,(byte)0x14,(byte)0x11,(byte)0x13,(byte)0x06,(byte)0x00,(byte)0x3E,(byte)0x19,(byte)0x14,(byte)0x69,(byte)0x88,(byte)0x05,(byte)0x00,(byte)0x3F,(byte)0x18,(byte)0x14,(byte)0x62,(byte)0x66,(byte)0x05,(byte)0x00,(byte)0x3F,(byte)0x17,(byte)0x14,(byte)0x90,(byte)0x42,(byte)0x05,(byte)0x00,(byte)0x3E,(byte)0x16,(byte)0x14,(byte)0x13,(byte)0x12,(byte)0x05,(byte)0x00,(byte)0x3F,(byte)0x15,(byte)0x14,(byte)0x01,(byte)0x88,(byte)0x04,(byte)0x00,(byte)0x3E,(byte)0x14,(byte)0x14,(byte)0x46,(byte)0x65,(byte)0x04,(byte)0x00,(byte)0x3F,(byte)0x13,(byte)0x14,(byte)0x98,(byte)0x44,(byte)0x04,(byte)0x00};
		MBusInternalLogs o = new MBusInternalLogs(null, 1);
		try {
			o.parse(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(o);
	}

}