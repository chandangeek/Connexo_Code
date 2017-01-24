package com.energyict.protocolimpl.utils.communicationdump;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

	public static CommunicationDumpEntry createTxEntry(byte[] data) {
        return createTxEntry(data, -1);
    }

    public static CommunicationDumpEntry createTxEntry(byte[] data, int sequenceNumber) {
        return new CommunicationDumpEntry(data, new Date(), Direction.TX, sequenceNumber);
    }

    public static CommunicationDumpEntry createRxEntry(byte[] data) {
        return createRxEntry(data, -1);
    }

    public static CommunicationDumpEntry createRxEntry(byte[] data, int sequenceNumber) {
        return new CommunicationDumpEntry(data, new Date(), Direction.RX, sequenceNumber);
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
		return ProtocolTools.getAsciiFromBytes(getData());
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

	public String toStringAscii() {
		DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		StringBuilder builder = new StringBuilder();
		builder.append(sdf.format(getTimeStamp()));
		builder.append(" ");
		builder.append(getDirection());
		builder.append(" ");
		builder.append(getDataAsString());
		return builder.toString();
	}

	public int compareTo(CommunicationDumpEntry o) {
		return getTimeStamp().compareTo(o.getTimeStamp());
	}

}
