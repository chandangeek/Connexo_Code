package com.energyict.protocolimpl.eig.nexus1272;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;

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

//	public void readLog() throws IOException {
//		
//		//	2. Pause the log by writing an initial, non-FFFFH value (0000H) to the Log Window Index Register.
//		setWindowIndex(new byte[]{0x00,0x00});
//		//	3. Read and store the Log Header information.
//		NexusDataParser headerParser = new NexusDataParser(readLogHeader());
//		//	4. Determine the starting Window Index and Window offset.
//		//	5. Determine the largest Window Index and Window offset.
//		//	6. Determine the ending Window Index and Window offset.
//		parseLogHeader(headerParser);
//		//	7. Set the Window Mode to Download Mode.
//		setWindowModeToDownload();
//		
//		int windowIndex = startWindowIndex;
////		//	8. Set the Log Window Index to the starting Window Index.
//		setWindowIndex(AbstractCommand.intToByteArray(startWindowIndex));
//		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//		baos2.write(readFirstLogWindow());
//		if (windowIndex == largestWindowIndex) { //first was also max
//			windowIndex = 0; //we're about to increment, yikes!
//			baos2.write(readLargestLogWindow());
//		}
//		
//		//not incrementing before this avoids the errors with starting and ending in index 0
//		if (windowIndex !=0)
//			windowIndex++;
//		while (windowIndex != endWindowIndex) {
//			if (windowIndex == largestWindowIndex) {
//				baos2.write(readLargestLogWindow());
//				windowIndex = 0; //we're about to increment, yikes!
//				continue;
//			}
//			
//			setWindowIndex(AbstractCommand.intToByteArray(windowIndex));
//			baos2.write(readLogWindow());
//			windowIndex++;
//		}
////		//		10. Increment the Window Index.
////		//	12. Repeat steps 10 and 11 until the largest or ending Window Index is reached.
////		//	     �If the largest is reached, go to step 13.
////		//	     �If the ending is reached, go to step 15.
////		//	13. Read window from beginning up to (but not including) the largest offset.
////		//	11. Read the Window from beginning to end.
//
//		
//		baos2.write(readLastLogWindow());
////		readLogWindow();
////		readFirstLogWindow();
//		//	16. Un-pause the log by writing FFFFH to the Log Window Index Register.
//		setWindowIndex(new byte[]{(byte) 0xFF,(byte) 0xFF});
//
//		parseLimitLog(baos2.toByteArray());
//	}
//
//	
//
//	//most recent log entry
//	protected byte[] readLastLogWindow() throws IOException {
//		//	15. Read Window from the beginning up to (but not including) the ending offset.
//		setWindowIndex(AbstractCommand.intToByteArray(endWindowIndex));
//		//FIXME CHECK  -1 for not including
//		int len = endWindowOffset/2;
//		if (len == 0) {
//			return new byte[]{};
//		}
//		Command command = NexusCommandFactory.getFactory().getLimitTriggerLogWindowCommand();
//		byte[] numRegisters = new byte[] {0x00, (byte) len};
//		((ReadCommand)command).setNumRegisters(numRegisters);
//		outputStream.write(command.build());
//		return connection.receiveWriteResponse(command).toByteArray();
//
//	}
//	
//	protected byte[] readLargestLogWindow() throws IOException {
//		//FIXME CHECK  -1 for not including
//		setWindowIndex(AbstractCommand.intToByteArray(largestWindowIndex));
//		int len = largestWindowOffset/2;
//		if (len == 0) {
//			return new byte[]{};
//		}
//		Command command = NexusCommandFactory.getFactory().getLimitTriggerLogWindowCommand();
//		byte[] numRegisters = new byte[] {0x00, (byte) len};
//		((ReadCommand)command).setNumRegisters(numRegisters);
//		outputStream.write(command.build());
//		return connection.receiveWriteResponse(command).toByteArray();
//
//	}
//
//	protected byte[] readLogWindow() throws IOException {
//		Command command = NexusCommandFactory.getFactory().getLimitTriggerLogWindowCommand();
//		outputStream.write(command.build());
//		return connection.receiveWriteResponse(command).toByteArray();
//
//	}
//	
//	protected byte[] readFirstLogWindow() throws IOException {
//		//	9. Read the Window from starting offset to the end of the Window.
//		setWindowIndex(AbstractCommand.intToByteArray(startWindowIndex));
//		
//		Command command = NexusCommandFactory.getFactory().getLimitTriggerLogWindowCommand();
//		int address = ProtocolUtils.getShort(((ReadCommand)command).getStartAddress(), 0) +  + startWindowOffset/2;
//		//FIXME CHECK  -1 for not including
//		int len = windowEndAddress - address;
//		if (len == 0) {
//			return new byte[]{};
//		}
//		byte[] partialStartAddress = AbstractCommand.intToByteArray(address);
//		((ReadCommand)command).setStartAddress(partialStartAddress);
//		byte[] numRegisters = new byte[] {0x00, (byte) len};
//		((ReadCommand)command).setNumRegisters(numRegisters);
//		outputStream.write(command.build());
//		return connection.receiveWriteResponse(command).toByteArray();
//
//	}



	private int recSize = 32;
private Date recDate;
	@Override
	public void parseLog(byte[] limitTriggerLogData, ProfileData profileData) throws IOException {
		int offset = 0;
		int length = 8;
		int recNum = 0;
		//		int recSize = 16;

		int dataPointersStart = 45332;
		int dataPointersEnd = 45460;

		//		byte[] test = intToByteArray(dataPointersStart);
		//		byte addrHigh = test[0];
		//		byte addrLow = test[1];
		//		int len = dataPointersEnd - dataPointersStart -1;
		//		byte[] send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
		//		outputStream.write(send);
		//		byte[] byteArray = connection.receiveResponse().toByteArray();
		//		byte[] ba2 = new byte[byteArray.length - 9]; 
		//		System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
		//
		//		List <LinePointMap> lpMap = processPointers(ba2);
		try {
			while (offset < limitTriggerLogData.length) {
				//FIXME make sure MSB flag on seconds isn't set
				recDate = parseF3(limitTriggerLogData, offset);
//				Date recDate2 = parseF3(limitSnapshotLogData, offset1);
//				System.out.println(recDate);
				String event = recDate + "";// + "\t" + recDate2;
				offset+= length;
				
//				System.out.println("Value 1");
				//Value 1 bitmap
				processComparisonBitmap(limitTriggerLogData, offset);
				offset +=4;
//				System.out.println("Value 2");
				//value 2 bitmap
//				processComparisonBitmap(limitTriggerLogData, offset);
				offset +=4;
				
//				System.out.println("Value 1 delta");
				//value 1
//				processDeltaBitmap(limitTriggerLogData, offset);
				offset +=4;
//				System.out.println("Value 2 delta");
				//value 2
//				processDeltaBitmap(limitTriggerLogData, offset);
				offset +=4;
				
//				System.out.println("combination");
				//combination
//				processComparisonBitmap(limitTriggerLogData, offset);
				offset +=4;
				
//				System.out.println("combination delta");
				//combination
//				processDeltaBitmap(limitTriggerLogData, offset);
				offset +=4;
				
				recNum++;
				offset = recNum * recSize;
			}
		}catch (Exception e) {
			System.out.println(e);
		}
	}

	private void processDeltaBitmap(byte[] limitTriggerLogData, int offset) {
		//read bakcwards for easier parsing
	
		int limitNumber = 32;
		for (int i=offset+3; i>=offset; i--) {
			byte value1 = limitTriggerLogData[i];
			byte value2 = limitTriggerLogData[i+4];
			byte combination = limitTriggerLogData[i+16];
			for (int j = 0; j<8; j++) {
//				System.out.println("BM: " + ProtocolUtils.buildStringHex(ProtocolUtils.byte2int(b), 2));
				if ((combination & 0x01) == 1) {
					System.out.println("comparison state changed since last record for limit " + limitNumber );
				}
				combination = (byte) (combination >> 1);
				limitNumber--;
			}
		}
		
	}

	private void processComparisonBitmap(byte[] limitTriggerLogData, int offset) {
		//read bakcwards for easier parsing
		
		int limitNumber = 32;
		for (int i=offset+3; i>=offset; i--) {
			byte value1 = limitTriggerLogData[i];
			byte value2 = limitTriggerLogData[i+4];
			byte combination = limitTriggerLogData[i+16];
			for (int j = 0; j<8; j++) {
//				System.out.println("BM: " + ProtocolUtils.buildStringHex(ProtocolUtils.byte2int(b), 2));
				if ((combination & 0x01) == 1) {
					//here the combination is triggered, so we see which values (1 or 2) are violated
					//TODO clean up the offset jumping
					String val1Str = "";
					String val2Str = "";
					if ((value1 & 0x01) == 1) {
						//val 1 triggered
						val1Str = "[value 1 limit violated]";
					}
					if ((value2 & 0x01) == 1) {
						//val 2 triggered
						val2Str = "[value 2 limit violated]";
					}
					
					String detail = "";
					if (!val1Str.equals("") && !val2Str.equals("")) {
						detail = val1Str + ", " + val2Str;
					}
					else if (!val1Str.equals("")) {
						detail = val1Str;
					}
					else if (!val2Str.equals("")) {
						detail = val2Str;
					}
					meterEvents.add(new MeterEvent(recDate, MeterEvent.METER_ALARM, "Limit threshold exceeded for limit " + limitNumber + " : " + detail));
//					System.out.println("limit " + limitNumber + " triggered");
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
