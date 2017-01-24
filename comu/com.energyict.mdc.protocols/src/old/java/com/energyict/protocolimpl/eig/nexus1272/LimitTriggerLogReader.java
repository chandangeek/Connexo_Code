package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LimitTriggerLogReader extends AbstractLogReader {


	List <MeterEvent> meterEvents = new ArrayList <MeterEvent>();


	public LimitTriggerLogReader(OutputStream os ,NexusProtocolConnection npc) {
		outputStream = os;
		connection = npc;
		windowIndexAddress = new byte[] {(byte) 0x95, 0x02};;
		windowModeAddress = new byte[] {(byte) 0x95, 0x42};
		windowEndAddress = 38464;
	}

	@Override
	protected Command getHeaderCommand() {
		return NexusCommandFactory.getFactory().getLimitTriggerLogHeaderCommand();
	}

	@Override
	protected Command getWindowCommand() {
		return NexusCommandFactory.getFactory().getLimitTriggerLogWindowCommand();
	}

	protected byte[] readLogHeader() throws IOException {
		Command command = NexusCommandFactory.getFactory().getLimitTriggerLogHeaderCommand();
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}

	private int recSize = 32;
	private Date recDate;
	@Override
	public void parseLog(byte[] limitTriggerLogData, ProfileData profileData, Date from, int intervalSeconds) throws IOException {
		int offset = 0;
		int length = 8;
		int recNum = 0;

		try {
			while (offset < limitTriggerLogData.length) {
				recDate = parseF3(limitTriggerLogData, offset);
				if (recDate.before(from)) {
					recNum++;
					offset = recNum * recSize;
					continue;
				}
				offset+= length;

				processComparisonBitmap(limitTriggerLogData, offset);
				//				processDeltaBitmap(limitTriggerLogData, offset);

				recNum++;
				offset = recNum * recSize;
			}
		}catch (Exception e) {
			System.out.println(e);
		}
	}

//	private void processDeltaBitmap(byte[] limitTriggerLogData, int offset) {
//		//read bakcwards for easier parsing
//		int limitNumber = 32;
//		for (int i=offset+3; i>=offset; i--) {
//			byte value1 = limitTriggerLogData[i];
//			byte value2 = limitTriggerLogData[i+4];
//			byte combination = limitTriggerLogData[i+16];
//			for (int j = 0; j<8; j++) {
//				if ((combination & 0x01) == 1) {
//					System.out.println("comparison state changed since last record for limit " + limitNumber );
//				}
//				combination = (byte) (combination >> 1);
//				limitNumber--;
//			}
//		}
//	}

	private void processComparisonBitmap(byte[] limitTriggerLogData, int offset) {
		//read bakcwards for easier parsing

		int limitNumber = 32;
		for (int i=offset+3; i>=offset; i--) {
			//TODO clean up the offset jumping
			byte value1 = limitTriggerLogData[i];
			byte value2 = limitTriggerLogData[i+4];
			byte combination = limitTriggerLogData[i+16];
			for (int j = 0; j<8; j++) {
				if ((combination & 0x01) == 1) {
					//here the combination is triggered, so we see which values (1 or 2) are violated

					String val1Str = "";
					String val2Str = "";
					if ((value1 & 0x01) == 1) {
						val1Str = "[value 1 limit violated]";
					}
					if ((value2 & 0x01) == 1) {
						val2Str = "[value 2 limit violated]";
					}

					String detail = "";
					if (!val1Str.isEmpty() && !val2Str.isEmpty()) {
						detail = val1Str + ", " + val2Str;
					}
					else if (!val1Str.isEmpty()) {
						detail = val1Str;
					}
					else if (!val2Str.isEmpty()) {
						detail = val2Str;
					}
					meterEvents.add(new MeterEvent(recDate, MeterEvent.METER_ALARM, "Limit threshold exceeded for limit " + limitNumber + " : " + detail));
				}
				combination = (byte) (combination >> 1);
				value1 = (byte) (value1 >> 1);
				value2 = (byte) (value2 >> 1);
				limitNumber--;
			}
		}
	}

	public List<MeterEvent> getMeterEvents() {
		return meterEvents;
	}

}
