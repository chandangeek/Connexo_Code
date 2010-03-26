package com.energyict.protocolimpl.utils.communicationdump;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author jme
 *
 */
public class CommunicationDumpEntry implements Comparable<CommunicationDumpEntry> {

	private static final String	DATE_FORMAT			= "dd/MM/yy HH:mm:ss.SSS";
	private static final int	DATE_STRING_LENGTH	= 21;
	private static final int	DATA_OFFSET			= 25;

	private final Direction direction;
	private final Date timeStamp;
	private final byte[] data;
	private final int sequenceNumber;

	public enum Direction {
		TX, RX
	}

	public CommunicationDumpEntry(byte[] data, Date timeStamp, Direction direction, int sequenceNumber) {
		this.data = data.clone();
		this.direction = direction;
		this.timeStamp = timeStamp;
		this.sequenceNumber = sequenceNumber;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public byte[] getData() {
		return data;
	}

	public String getDataAsString() {
		return new String(getData());
	}

	public String getDataAsHexString() {
		return ProtocolTools.getHexStringFromBytes(getData());
	}

	public Direction getDirection() {
		return direction;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Get the length of the data
	 * @return
	 */
	public int getLength() {
		return getData().length;
	}

	public static CommunicationDumpEntry getEntryFromString(String entryLine, int sequenceNumber) {
		byte[] data;
		Date timeStamp;
		Direction direction;

		if ((entryLine == null) || (entryLine.length() <= DATA_OFFSET))  {
			return null;
		}

		try {
			DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			timeStamp = sdf.parse(entryLine.substring(0, DATE_STRING_LENGTH));
		} catch (ParseException e) {
			return null;
		}

		if (entryLine.contains(Direction.TX.name())) {
			direction = Direction.TX;
		} else if (entryLine.contains(Direction.RX.name())) {
			direction = Direction.RX;
		} else {
			return null;
		}

		data = ProtocolTools.getBytesFromHexString(entryLine.substring(DATA_OFFSET).trim());

		return new CommunicationDumpEntry(data, timeStamp, direction, sequenceNumber);
	}

	public static CommunicationDumpEntry getEntryFromString(String entryLine) {
		return getEntryFromString(entryLine, -1);
	}

	public boolean isTx() {
		return getDirection().equals(Direction.TX);
	}

	public boolean isRx() {
		return getDirection().equals(Direction.RX);
	}

	public String toString() {
		DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		StringBuilder builder = new StringBuilder();
		builder.append(sdf.format(getTimeStamp()));
		builder.append(" ");
		builder.append(getDirection());
		builder.append(" ");
		builder.append(getDataAsHexString());
		return builder.toString();
	}

	public static void main(String[] args) {
		String stringIn = "08/12/09 14:52:16.757 TX $04$0D$01$30$30$31";
		System.out.println(stringIn);
		System.out.println(CommunicationDumpEntry.getEntryFromString(stringIn));
	}

	public int compareTo(CommunicationDumpEntry o) {
		return getTimeStamp().compareTo(o.getTimeStamp());
	}

}
