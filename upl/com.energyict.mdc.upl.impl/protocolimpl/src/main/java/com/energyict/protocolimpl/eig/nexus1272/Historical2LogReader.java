package com.energyict.protocolimpl.eig.nexus1272;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.parse.LinePoint;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySetting;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySettingFactory;

public class Historical2LogReader extends AbstractLogReader {



	private List<LinePoint> meterlpMap;
	private List<LinePoint> masterlpMap;
	private ScaledEnergySettingFactory sesf;

	//	
	//	//most recent log entry
	//	protected byte[] readLastLogWindow() throws IOException {
	//		//	15. Read Window from the beginning up to (but not including) the ending offset.
	//		setWindowIndex(AbstractCommand.intToByteArray(endWindowIndex));
	//		//FIXME CHECK  -1 for not including
	//		int len = endWindowOffset/2;
	//		if (len != 0){
	//		Command command = NexusCommandFactory.getFactory().getHistorical2LogWindowCommand();
	//		byte[] numRegisters = new byte[] {0x00, (byte) len};
	//		((ReadCommand)command).setNumRegisters(numRegisters);
	//		outputStream.write(command.build());
	//		return connection.receiveWriteResponse(command).toByteArray();
	//		}
	//		return new byte[] {};
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
	//		Command command = NexusCommandFactory.getFactory().getHistorical2LogWindowCommand();
	//		byte[] numRegisters = new byte[] {0x00, (byte) len};
	//		((ReadCommand)command).setNumRegisters(numRegisters);
	//		outputStream.write(command.build());
	//		return connection.receiveWriteResponse(command).toByteArray();
	//
	//	}
	//
	//	protected byte[] readLogWindow() throws IOException {
	//		Command command = NexusCommandFactory.getFactory().getHistorical2LogWindowCommand();
	//		outputStream.write(command.build());
	//		return connection.receiveWriteResponse(command).toByteArray();
	//
	//	}
	//	
	//	
	//	
	//	
	//	protected byte[] readFirstLogWindow() throws IOException {
	//		//	9. Read the Window from starting offset to the end of the Window.
	//		setWindowIndex(AbstractCommand.intToByteArray(startWindowIndex));
	//		
	//		Command command = NexusCommandFactory.getFactory().getHistorical2LogWindowCommand();
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










	public Historical2LogReader(OutputStream os ,NexusProtocolConnection npc, List<LinePoint> mtrlpMap, List<LinePoint> mstrlpMap, ScaledEnergySettingFactory sesf) {
		this.sesf = sesf;
		meterlpMap = mtrlpMap;
		masterlpMap = mstrlpMap;
		outputStream = os;
		connection = npc;
		windowIndexAddress = new byte[] {(byte) 0x95, 0x01};;  
		windowModeAddress = new byte[] {(byte) 0x95, 0x41};
		windowEndAddress = 38400;
	}

	@Override
	protected Command getHeaderCommand() {
		return NexusCommandFactory.getFactory().getHistorical2LogHeaderCommand();
	}

	@Override
	protected Command getWindowCommand() {
		return NexusCommandFactory.getFactory().getHistorical2LogWindowCommand();
	}

	protected byte[] readLogHeader() throws IOException {
		Command command = NexusCommandFactory.getFactory().getHistorical2LogHeaderCommand();
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}

	//	public void readLog(byte[] byteArray) throws IOException {
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
	//			windowIndex = -255; //we're about to increment, yikes!
	//			baos2.write(readLargestLogWindow());
	//		}
	//		
	//		//not incrementing before this avoids the errors with starting and ending in index 0
	//		if (windowIndex !=0)
	//			windowIndex++;
	//		if (windowIndex == -255)
	//			windowIndex=1;
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
	//		parseHistorical2Log(baos2.toByteArray());
	//	}

	@Override
	public void parseLog(byte[] byteArray, ProfileData profileData) throws IOException {
//		ProfileData profileData = new ProfileData();
		List intervalDatas = new ArrayList();
		int offset = 0;
		int length = 4;
		int recNum = 0;
		//FIXME FIND THIS SIZE
		int recSize = 32*2;
//		get Scaled Energy Setting
//		Command getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
//		((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, 0x01});
//		outputStream.write(getSES.build());
//		byte[] resp = connection.receiveWriteResponse(getSES).toByteArray();
//		System.out.println(" ***************** " + ProtocolUtils.byte2int(resp[0]));
//		System.out.println(" ***************** " + ProtocolUtils.byte2int(resp[1]));
		
//		Command command = NexusCommandFactory.getFactory().getDataPointersCommand();
//		outputStream.write(command.build());
//		List <LinePoint> lpMap = processPointers(connection.receiveWriteResponse(command).toByteArray());

		//		int dataPointersStart = 45332;//B114
		//		int dataPointersEnd = 45460;//B194
		//
		//		byte[] test = AbstractCommand.intToByteArray(dataPointersStart);
		//		byte addrHigh = test[0];
		//		byte addrLow = test[1];
		//		int len = dataPointersEnd - dataPointersStart -1;
		//		byte[] send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
		//		//outputStream.write(send);
		//		byte[] byteArray = new byte []{};//connection.receiveResponse().toByteArray();
		//		byte[] ba2 = new byte[byteArray.length - 9]; 
		//		System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
		//
		//		List <LinePointMap> lpMap = processPointers(ba2);

		try{
			
			while (offset < byteArray.length) {
				length=8;
				Date recDate = parseF3(byteArray, offset);
				String event = recDate + "";
				offset+= length;
//				System.out.println(recDate);
				 IntervalData intervalData = new IntervalData(recDate,0,0);
				for (LinePoint lp : meterlpMap) {
//					System.out.println("Searching for " + lp);
					for (LinePoint lp2 : masterlpMap) {
						
						if (lp.getLine() == lp2.getLine() && lp.getPoint() == lp2.getPoint()) {
//							System.out.println("Found " +lp2 + "\n\n");
							BigDecimal val =new BigDecimal( parseF64(byteArray, offset));
							offset+=4;
							BigDecimal divisor = new BigDecimal(1);
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
								divisor = new BigDecimal(Math.pow(10, numDecimals));
							}
							val = val.divide(divisor);
							intervalData.addValue(val, 0, 0);
//							System.out.println("\t"+ lp.getLine() + "." + lp.getPoint() + " - " + lp.getDescription() + "\t" + val);
							break;
						}
						
					}
					
					
					
					
				}

				recNum++;
				offset = recNum * meterlpMap.size()*8;//recSize;
				intervalDatas.add(intervalData);
			}
		}catch (Exception e) {
			System.out.println(e);
		}
		
		profileData.setIntervalDatas(intervalDatas);
		
	}

	private int parseF64(byte[] bArray, int offset) throws IOException {
		return parseF64(bArray, offset, 4);
	}
	private int parseF64(byte[] bArray, int offset, int len) throws IOException {
		int val = ProtocolUtils.getInt(bArray, offset, len);
		return val;

	}

	private List<LinePoint> processPointers(byte[] ba) throws IOException {
		int offset = 0;
		List <LinePoint> lpMap = new ArrayList <LinePoint> ();
		//		for (int i = 0; i<4; i++){
		while (offset <= ba.length-4) {	
			if (ba[offset] == -1 && ba[offset+1] == -1) {
				offset += 4;
				continue;
			}
			int line = ProtocolUtils.getInt(ba, offset, 2);
			offset+=2;
			int point = ProtocolUtils.getInt(ba, offset, 1);
			offset+=2;
			lpMap.add(new LinePoint(line, point));
		}
		return lpMap;
	}


}
