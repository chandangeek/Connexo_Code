/**
 * 
 */
package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.commands.ReadArchiveCommand;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of a generic archive (LoadProfile)
 * 
 * @author gna
 * @since 4-mrt-2010
 * 
 */
public class GenericArchiveObject extends AbstractObject {

	/* The following attributes of an Archive can be read out */
    @SuppressWarnings({"unused"})
	public static final int ATTRB_VALUE = 0;
    @SuppressWarnings({"unused"})
	public static final int ATTRB_ACCESSRIGHTS = 1;
    @SuppressWarnings({"unused"})
	public static final int ATTRB_DESCRIPTION = 2;
    @SuppressWarnings({"unused"})
	public static final int ATTRB_UNITS_TEXT = 3;
    @SuppressWarnings({"unused"})
	public static final int ATTRB_SOURCE = 4;
    @SuppressWarnings({"unused"})
	public static final int ATTRB_UNITS_CODE = 5;
    @SuppressWarnings({"unused"})
	public static final int ATTRB_FORMAT = 6;
    @SuppressWarnings({"unused"})
	public static final int ATTRB_DATATYPE = 7;
    @SuppressWarnings({"unused"})
	public static final String ATTRB_NR_OF_ENTR = "A.0";

	/** The letter V */
	private static final String VIE = "V";

	/**
	 * A static string representing the format of an empty request (3
	 * SEMI-COLONS)
	 */
	private static final String EMPTY_REQUEST = ";;;";

	/** The startAddress of this object */
	private String startAddress = null;

	/** The instance of the object */
	private int instance = 0;

	/**
	 * Initial constructor
	 * 
	 * @param link
	 *            - the {@link ProtocolLink}
	 * 
	 * @param archiveInstance
	 *            - instance letter of the archive
	 */
	public GenericArchiveObject(ProtocolLink link, int archiveInstance) {

		super(link);
		this.instance = archiveInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getInitialAddress() {
		if (this.startAddress == null) {
			throw new IllegalArgumentException(
					"The initial address of the GenericArchive can't be NULL");
		}
		return this.startAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getObjectInstance() {
		return instance;
	}

	/**
	 * Unsupported getter for the default value
	 * 
	 * @throws IOException
	 *             because it is not supported
	 */
	@Override
	public String getValue() throws IOException {
		throw new UnsupportedException(
				"The default getValue() is not supported by the archiveObjects");
	}

	/**
	 * Get the capturedObjects form the device
	 * 
	 * @return a string with the captured objects
	 * 
	 * @throws IOException
	 *             if a read exception occurred
	 */
	public String getCapturedObjects() throws IOException {
		return getEmptyRequest(constructStartAddress(ATTRB_DESCRIPTION));
	}

	/**
	 * Get the Units from the device
	 * 
	 * @return a String of units
	 * @throws IOException
	 *             if something happened during the read
	 */
	public String getUnits() throws IOException {
		return getEmptyRequest(constructStartAddress(ATTRB_UNITS_TEXT));
	}

	/**
	 * Default empty request
	 * 
	 * @param startAddress
	 *            - the startAddress for the request
	 * 
	 * @return the response string from the device
	 * 
	 * @throws IOException
	 *             if something happened during the read
	 */
	private String getEmptyRequest(String startAddress) throws IOException {
		this.startAddress = startAddress;
		ReadArchiveCommand rac = new ReadArchiveCommand(link);
		rac.setStartAddress(getStartAddress());
		return rac.invokeForOneTransaction(EMPTY_REQUEST);
	}

	/**
	 * Construct the startAddress
	 * 
	 * @param attrbDescription
	 *            - the attribute number to read
	 * 
	 * @return the constructed startAddress
	 */
	private String constructStartAddress(int attrbDescription) {
		return constructStartAddress(Integer.toString(attrbDescription));
	}

	/**
	 * Construct the startAddress
	 * 
	 * @param attrbDescription
	 *            - the attribute number to read
	 * 
	 * @return the constructed startAddress
	 */

	private String constructStartAddress(String attrbDescription) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(VIE);
		strBuilder.append(DOT);
		strBuilder.append(attrbDescription);
		return strBuilder.toString();
	}

	/**
	 * Request the number of intervals
	 * 
	 * @param from
	 *            - the date from where to start reading
	 * 
	 * @return a number representing the available intervals starting from the
	 *         from date
	 * 
	 * @throws IOException
	 *             if something freaky happened during the read
	 */
	public String getNumberOfIntervals(Date from) throws IOException {
		String rawFrom = getDLFormatDate(from);
		String requestString = buildRequestString(3, rawFrom, null, -1);
		this.startAddress = constructStartAddress(ATTRB_NR_OF_ENTR);
		ReadArchiveCommand rac = new ReadArchiveCommand(link);
		rac.setStartAddress(getStartAddress());
		return ProtocolUtils.stripBrackets(rac
				.invokeForOneTransaction(requestString));
	}

	/**
	 * Request the raw intervals from the device
	 * 
	 * @param from
	 *            - the date to start reading from
	 * 
	 * @param blockSize
	 *            - the size of the blocks to read
	 * 
	 * @return a string containing one or multiple interval records
	 * 
	 * @throws IOException
	 *             when an error occurred during the read
	 */
	public String getIntervals(Date from, int blockSize) throws IOException {
		return getIntervals(from, null, blockSize);
	}

	/**
	 * Request the raw intervals from the device
	 * 
	 * @param from
	 *            - the date to start reading from
	 * 
	 * @param to
	 *            - the date to read to
	 * 
	 * @param blockSize
	 *            - the size of the blocks to read
	 * 
	 * @return a string containing one or multiple interval records
	 * 
	 * @throws IOException
	 *             when an error occurred during the read
	 */
	public String getIntervals(Date from, Date to, int blockSize)
			throws IOException {
		String requestString = buildRequestString(3, getDLFormatDate(from),
				(to == null) ? null : getDLFormatDate(to), blockSize);
		this.startAddress = constructStartAddress(ATTRB_VALUE);
		ReadArchiveCommand rac = new ReadArchiveCommand(link);
		rac.setStartAddress(getStartAddress());
		if (blockSize > 1) {
			return rac.invokeForMultiple(requestString);
		} else {
			return rac.invokeForOneTransaction(requestString);
		}
	}

	/**
	 * Construct a date in the request format
	 * 
	 * @param date
	 *            - the date to convert
	 * 
	 * @return raw date
	 */
	private String getDLFormatDate(Date date) {
		Calendar cal = Calendar.getInstance(link.getTimeZone());
		cal.setTime(date);
		return ClockObject.getRawData(cal);
	}

	/**
	 * Build up the request string.<br>
	 * A profile request string has a specific format, it can contain on or more
	 * of the following elements:
	 * 
	 * @param columnIndex
	 *            - the column index of the archive
	 * 
	 * @param rawFrom
	 *            - the startDate to read from (null -> unused)
	 * 
	 * @param rawTo
	 *            - the toDate to read to (null -> unused)
	 * 
	 * @param numbOfBlocks
	 *            - the number of blocks to read (-1 -> unused)
	 * 
	 * @return the requestData in form of [p]; [v]; [b] where [p] is position,
	 *         [v] is fromDate, [b] is toDate, [t] is number of blocks
	 */
	private String buildRequestString(int columnIndex, String rawFrom,
			String rawTo, int numbOfBlocks) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(columnIndex);
		strBuilder.append(SEMI_COLON);
		strBuilder.append((rawFrom != null) ? rawFrom : "");
		strBuilder.append(SEMI_COLON);
		strBuilder.append((rawTo != null) ? rawTo : "");
		strBuilder.append(SEMI_COLON);
		strBuilder.append((numbOfBlocks != -1) ? numbOfBlocks : "");
		return strBuilder.toString();
	}

}
