package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.Date;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class LeakageEventTable extends AbstractRadioCommand {

	public class LeakageEvent {
		

		/**
		 * indicates the event type (occurrence or disappearance) and the corresponding Port.
		 * Bit 7 Bit 6 Bit 5 Bit 4 Bit 3 Bit 2 Bit 1 Bit 0
		 * Corresponding Port
		 * bit7..6: 00 : Port A 01 : Port B
		 * bit5..2: reserved
		 * bit1: Leak type 0 : Extreme leak	1 : Residual leak
		 * bit0: Event Type	0 : disappearance 1 : occurence
		 */
		int status;

		/**
		 * 
		 */
		int consumptionRate;
		
		/**
		 * leakage event timestamp
		 */
		Date date=null;
		
		/**
		 * valid or not
		 */
		boolean valid=false;
		
		
		final public boolean isValid() {
			return valid;
		}

		private LeakageEvent(int status, int consumptionRate, byte[] timestamp) throws IOException {
			this.status = status;
			this.consumptionRate = consumptionRate;
			
			for (byte b : timestamp) {
				if (b!=0) {
					valid=true;
					this.date = TimeDateRTCParser.parse(timestamp, getWaveFlow100mW().getTimeZone()).getTime();
					break;
				}
			}
		}

		final public int getStatus() {
			return status;
		}


		final public int getConsumptionRate() {
			return consumptionRate;
		}


		final public Date getDate() {
			return date;
		}		
		
		
	}
	
	LeakageEvent[] leakageEvents = new LeakageEvent[5];
	
	final public LeakageEvent[] getLeakageEvents() {
		return leakageEvents;
	}

	LeakageEventTable(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
		// TODO Auto-generated constructor stub
	}

	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.LeakageEventTable;
	}

	@Override
	void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));
			for (int i=0;i<leakageEvents.length;i++) {
				int status = WaveflowProtocolUtils.toInt(dais.readByte());
				int consumptionRate = WaveflowProtocolUtils.toInt(dais.readShort());
				byte[] timestamp = new byte[7];
				dais.read(timestamp);
				leakageEvents[i] = new LeakageEvent(status, consumptionRate, timestamp);
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}				
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
