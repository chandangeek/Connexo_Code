package com.energyict.protocolimpl.eig.nexus1272;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;

public class LimitSnapshotLogReader extends AbstractLogReader {

	 

	

	public LimitSnapshotLogReader(OutputStream os ,NexusProtocolConnection npc) {
		outputStream = os;
		connection = npc;
		windowIndexAddress = new byte[] {(byte) 0x95, 0x03};;  
		windowModeAddress = new byte[] {(byte) 0x95, 0x43};
		windowEndAddress = 38528;
	}
	
	@Override
	protected Command getHeaderCommand() {
		return NexusCommandFactory.getFactory().getLimitSnapshotLogHeaderCommand();
	}

	@Override
	protected Command getWindowCommand() {
		return NexusCommandFactory.getFactory().getLimitSnapshotLogWindowCommand();
	}
	
	protected byte[] readLogHeader() throws IOException {
		Command command = NexusCommandFactory.getFactory().getLimitSnapshotLogHeaderCommand();
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
//		Command command = NexusCommandFactory.getFactory().getLimitSnapshotLogWindowCommand();
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
//		Command command = NexusCommandFactory.getFactory().getLimitSnapshotLogWindowCommand();
//		byte[] numRegisters = new byte[] {0x00, (byte) len};
//		((ReadCommand)command).setNumRegisters(numRegisters);
//		outputStream.write(command.build());
//		return connection.receiveWriteResponse(command).toByteArray();
//
//	}
//
//	protected byte[] readLogWindow() throws IOException {
//		Command command = NexusCommandFactory.getFactory().getLimitSnapshotLogWindowCommand();
//		outputStream.write(command.build());
//		return connection.receiveWriteResponse(command).toByteArray();
//
//	}
//	
//	protected byte[] readFirstLogWindow() throws IOException {
//		//	9. Read the Window from starting offset to the end of the Window.
//		setWindowIndex(AbstractCommand.intToByteArray(startWindowIndex));
//		
//		Command command = NexusCommandFactory.getFactory().getLimitSnapshotLogWindowCommand();
//		int address = ProtocolUtils.getShort(((ReadCommand)command).getStartAddress(), 0) +  + startWindowOffset/2;
//		//FIXME CHECK  -1 for not including
//		int len = windowEndAddress - address;
//		byte[] partialStartAddress = AbstractCommand.intToByteArray(address);
//		((ReadCommand)command).setStartAddress(partialStartAddress);
//		byte[] numRegisters = new byte[] {0x00, (byte) len};
//		((ReadCommand)command).setNumRegisters(numRegisters);
//		outputStream.write(command.build());
//		return connection.receiveWriteResponse(command).toByteArray();
//
//	}



	private int recSize = 128;
	@Override
	public void parseLog(byte[] LimitSnapshotLogData, ProfileData profileData) throws IOException {
		int offset = 0;
		int length = 8;
		int recNum = 0;
//		int offset1 = 0;
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
			while (offset < LimitSnapshotLogData.length) {
				Date recDate = parseF3(LimitSnapshotLogData, offset);
//				Date recDate2 = parseF3(limitSnapshotLogData, offset1);
				String event = recDate + "";// + "\t" + recDate2;
				offset+= length;
//				System.out.println(event);
				//			for (LinePointMap lp : lpMap) {
				//				int val = parseF64(ba, offset);
				//				offset+=4;
				//				System.out.println("\t"+ lp.line + "." + lp.point + " - " + lp.getDescription() + "\t" + val);
				//			}

				recNum++;
//				offset1 = recNum * recSize;
				offset = recNum * recSize;
			}
		}catch (Exception e) {
			System.out.println(e);
		}

	}

	
	
}
