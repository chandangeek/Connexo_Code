package com.energyict.protocolimpl.eig.nexus1272;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.eig.nexus1272.command.AbstractCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.command.ReadCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.WriteSingleRegisterCommand;
import com.energyict.protocolimpl.eig.nexus1272.parse.NexusDataParser;

public abstract class AbstractLogReader implements LogReader{

	protected OutputStream outputStream;
	protected NexusProtocolConnection connection;

	byte[] windowIndexAddress;  
	byte[] windowModeAddress;
	protected int windowEndAddress;

	protected long memsize;
	protected int recordSize;
	protected int firstIndex;
	protected int lastIndex;
	protected Date firstTimeStamp;
	protected Date lastTimeStamp;
	protected long validBitmap;
	protected int maxRecords;
	protected int startWindowIndex;
	protected int startWindowOffset;
	protected int largestWindowIndex;
	protected int largestWindowOffset;
	protected int endWindowIndex;
	protected int endWindowOffset;

	protected void setWindowIndex(byte[] data) throws IOException {
		Command command = NexusCommandFactory.getFactory().getWriteSingleRegisterCommand();
		((WriteSingleRegisterCommand)command).setAddress(windowIndexAddress);
		((WriteSingleRegisterCommand)command).setData(data);
		outputStream.write(command.build());
		connection.receiveWriteResponse(command).toByteArray();
		//		}
	}

	protected void setWindowModeToDownload() throws IOException {
		Command command = NexusCommandFactory.getFactory().getWriteSingleRegisterCommand();
		((WriteSingleRegisterCommand)command).setAddress(windowModeAddress);
		((WriteSingleRegisterCommand)command).setData(new byte[]{0,0});
		outputStream.write(command.build());
		connection.receiveWriteResponse(command).toByteArray();
	}

	protected void parseLogHeader(NexusDataParser ndp) throws IOException {
		//		int offset = 0;
		//		int length = 4;
		memsize = ndp.parseF18();//parseF18(byteArray, offset, length);
		//		offset += length;
		//		length = 2;
		recordSize = ndp.parseF51();//parseF51(byteArray, offset, length);
		//TODO fix this global needed for historical parsing
		//recSize = recordSize;
		//		offset += length;
		firstIndex = ndp.parseF51();//parseF51(byteArray, offset, length);
		//		offset += length;
		lastIndex = ndp.parseF51();//parseF51(byteArray, offset, length);
		//		offset += length;
		//		length = 8;
		firstTimeStamp = ndp.parseF3();//parseF3(byteArray, offset);
		//		offset += length;
		lastTimeStamp = ndp.parseF3();//parseF3(byteArray, offset);
		//		offset += length;
		//		length = 8;
		validBitmap = ndp.parseF18();ndp.parseF18();//parseF18(byteArray, offset, length);
		//		offset += length;
		//		length = 2;
		maxRecords = ndp.parseF51();//parseF51(byteArray, offset, length);

		//	4. Determine the starting Window Index and Window offset.
		startWindowIndex = (recordSize * firstIndex) / 128;
		startWindowOffset = (recordSize * firstIndex) % 128;

		//	5. Determine the largest Window Index and Window offset.
		largestWindowIndex = (recordSize * maxRecords) / 128;
		largestWindowOffset = (recordSize * maxRecords) % 128;

		//	6. Determine the ending Window Index and Window offset.
		endWindowIndex = ( recordSize * (lastIndex + 1) ) / 128;
		endWindowOffset = ( recordSize * (lastIndex + 1) ) % 128;

//		System.out.println("Reading from start window index " + startWindowIndex + " offset " + startWindowOffset + " to end window index" +
//				endWindowIndex + " offset " + endWindowOffset);

	}

	protected byte[] readLogHeader() throws IOException {
		Command command = getHeaderCommand();
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}

	public byte[] readLog(Date lastReadDate) throws IOException {

		Date checkDate = null;

		//	2. Pause the log by writing an initial, non-FFFFH value (0000H) to the Log Window Index Register.
		setWindowIndex(new byte[]{0x00,0x00});
		//	3. Read and store the Log Header information.
		NexusDataParser headerParser = new NexusDataParser(readLogHeader());
		//	4. Determine the starting Window Index and Window offset.
		//	5. Determine the largest Window Index and Window offset.
		//	6. Determine the ending Window Index and Window offset.
		parseLogHeader(headerParser);
		//	7. Set the Window Mode to Download Mode.
		setWindowModeToDownload();

		int windowIndex = startWindowIndex;


		//		//		//	8. Set the Log Window Index to the starting Window Index.
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


		//set to the end address as we are going to read backwards...

//		System.out.println("SI: " + startWindowIndex + " SO: " + startWindowOffset);
//		System.out.println("EI: " + endWindowIndex + " EO: " + endWindowOffset);
//		System.out.println("LI: " + largestWindowIndex + " LO: " + largestWindowOffset);


		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		windowIndex = endWindowIndex;
		setWindowIndex(AbstractCommand.intToByteArray(endWindowIndex));


		//		//	8. Set the Log Window Index to the starting Window Index.
		//		setWindowIndex(AbstractCommand.intToByteArray(startWindowIndex));

		boolean doneReading = false;
		byte[] ba = readLastLogWindow();
		System.out.print("ba: " + ba.length + " ");
		if (ba.length >7){
			checkDate = parseF3(ba, 0);
//			System.out.println(checkDate + "\tWI: " + windowIndex);
		}
//		else	System.out.println();
		baos2.write(ba);
		if (checkDate!=null && checkDate.before(lastReadDate)) {
			doneReading  = true;
		}
		//			baos2.write(readLastLogWindow());
		//if (windowIndex == largestWindowIndex) {  //last was also max
		//	windowIndex = 0; //we're about to increment, yikes!
		//}

		//			baos2.write(readFirstLogWindow());

		//not incrementing before this avoids the errors with starting and ending in index 0
		//if (windowIndex !=0 )
		windowIndex--;
		
		while (windowIndex != startWindowIndex && ! doneReading) {
			if (windowIndex <= 0) { //wrap awound
				windowIndex = largestWindowIndex;
				ba = readLargestLogWindow();
				System.out.print("ba: " + ba.length + " ");
				if (ba.length >7){
					checkDate = parseF3(ba, 0);
//					System.out.println(checkDate + "\tWI: " + windowIndex);
				}
//				else	System.out.println();
				baos2.write(ba);
				//				baos2.write(readLargestLogWindow());
				//				windowIndex = 0; //we're about to increment, yikes!
				windowIndex--; //we just read the largestWindow
				if (checkDate!=null && checkDate.before(lastReadDate)) {
					doneReading  = true;
				}
				continue;
			}

			setWindowIndex(AbstractCommand.intToByteArray(windowIndex));

			ba = readLogWindow();
			System.out.print("ba: " + ba.length + " ");
			if (ba.length >7){
				checkDate = parseF3(ba, 0);
//				System.out.println(checkDate + "\tWI: " + windowIndex);
			}
//			else	System.out.println();
			baos2.write(ba);
			if (checkDate!=null && checkDate.before(lastReadDate)) {
				doneReading  = true;
			}
			windowIndex--;
		}
		//		//		10. Increment the Window Index.
		//		//	12. Repeat steps 10 and 11 until the largest or ending Window Index is reached.
		//		//	     �If the largest is reached, go to step 13.
		//		//	     �If the ending is reached, go to step 15.
		//		//	13. Read window from beginning up to (but not including) the largest offset.
		//		//	11. Read the Window from beginning to end.


		if (!doneReading) {
			ba = readFirstLogWindow();
			System.out.print("ba: " + ba.length + " ");
//			if (ba.length >7)				
//				System.out.println(parseF3(ba, 0) + "\tWI: " + windowIndex);
//			else	System.out.println();
			baos2.write(ba);
		}
		//		baos2.write(ba);
		//		readLogWindow();
		//		readFirstLogWindow();




		//	16. Un-pause the log by writing FFFFH to the Log Window Index Register.
		setWindowIndex(new byte[]{(byte) 0xFF,(byte) 0xFF});

		return baos2.toByteArray();
		
//		parseLog();
	}



//	public abstract Object parseLog(byte[] byteArray) throws IOException;

	//most recent log entry
	protected byte[] readLastLogWindow() throws IOException {
//		System.out.println("READ LAST WINDOW");
		//	15. Read Window from the beginning up to (but not including) the ending offset.
		setWindowIndex(AbstractCommand.intToByteArray(endWindowIndex));
		//FIXME CHECK  -1 for not including
		int len = endWindowOffset/2;
		if (len == 0) {
			return new byte[]{};
		}
		Command command = getWindowCommand(); 
		byte[] numRegisters = new byte[] {0x00, (byte) len};
		((ReadCommand)command).setNumRegisters(numRegisters);
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}

	protected abstract Command getWindowCommand();
	protected abstract Command getHeaderCommand();

	protected byte[] readLargestLogWindow() throws IOException {
//		System.out.println("ReadLargest WINDOW");
		//FIXME CHECK  -1 for not including
		setWindowIndex(AbstractCommand.intToByteArray(largestWindowIndex));
		int len = largestWindowOffset/2;
		if (len == 0) {
			return new byte[]{};
		}
		Command command = getWindowCommand();
		byte[] numRegisters = new byte[] {0x00, (byte) len};
		((ReadCommand)command).setNumRegisters(numRegisters);
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}

	protected byte[] readLogWindow() throws IOException {
		Command command = getWindowCommand();
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}

	protected byte[] readFirstLogWindow() throws IOException {

//		System.out.println("READ FIRST WINDOW");
		//	9. Read the Window from starting offset to the end of the Window.
		setWindowIndex(AbstractCommand.intToByteArray(startWindowIndex));

		Command command = getWindowCommand(); 
		int address = ProtocolUtils.getShort(((ReadCommand)command).getStartAddress(), 0) +  + startWindowOffset/2;
		//FIXME CHECK  -1 for not including
		int len = windowEndAddress - address;
		if (len == 0) {
			return new byte[]{};
		}
		byte[] partialStartAddress = AbstractCommand.intToByteArray(address);
		((ReadCommand)command).setStartAddress(partialStartAddress);
		byte[] numRegisters = new byte[] {0x00, (byte) len};
		((ReadCommand)command).setNumRegisters(numRegisters);
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}






	protected Date parseF3(byte[] byteArray, int offset) throws IOException {
		int century = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int year = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int month = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int day = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int hour = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int minute = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int second = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int tenMilli = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();

		//TODO Use TZ from RMR tab?
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, century*100+year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, tenMilli*10);

		return cal.getTime();
	}

	public abstract void parseLog(byte[] byteArray, ProfileData profileData) throws IOException;
}
