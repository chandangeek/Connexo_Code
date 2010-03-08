/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.Archives;
import com.energyict.protocolimpl.iec1107.instromet.dl220.commands.ReadArchiveCommand;

/**
 * Implementation of a generic archive (LoadProfile)
 * 
 * TODO TOCOMPLETE!
 * 
 * @author gna
 * @since 4-mrt-2010
 *
 */
public class GenericArchiveObject extends AbstractObject {
	
	/* The following attributes of an Archive can be read out */
	public static final int ATTRB_VALUE 		= 0;
	public static final int ATTRB_ACCESSRIGHTS 	= 1;
	public static final int ATTRB_DESCRIPTION 	= 2;
	public static final int ATTRB_UNITS_TEXT	= 3;
	public static final int ATTRB_SOURCE		= 4;
	public static final int ATTRB_UNITS_CODE	= 5;
	public static final int ATTRB_FORMAT		= 6;
	public static final int ATTRB_DATATYPE		= 7;
	public static final String ATTRB_NR_OF_ENTR = "A.0";
	
	/** The letter V */
	private static final String VIE = "V";
	
	/** A static string used for the request, it contains a colon ':', the capital 'V' and a dot '.' */
	private static final String COLLON_VIE_DOT = ":V.";

	/** A static string representing the format of an empty request (3 SEMI-COLONS) */
	private static final String EMPTY_REQUEST = ";;;";
	
	/** The startAddress of this object */
	private String startAddress = null;
	
	/** The instance of the object */
	private int instance = 0;
	
	/** The used {@link Archives}*/
	private final Archives archive;
	
	/**
	 * @param link
	 * @param archive 
	 */
	public GenericArchiveObject(ProtocolLink link, Archives archive) {
		super(link);
		this.archive = archive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getInitialAddress() {
		if(this.startAddress == null){
			throw new IllegalArgumentException("The initial address of the GenericArchive can't be NULL");
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
	 * Getter for the default value of this object
	 * 
	 * @return the value from the readCommand of this object
	 * @throws IOException
	 */
	@Override
	public String getValue() throws IOException {
		throw new UnsupportedException("The default getValue() is not supported by the archiveObjects");
	}
	
	/**
	 * Get the capturedObjects form the device
	 * 
	 * @return a string with the captured objects
	 * 
	 * @throws IOException if a read exception occurred
	 */
	public String getCapturedObjects() throws IOException {
		return getEmptyRequest(constructStartAddress(ATTRB_DESCRIPTION));
	}

	/**
	 * Get the Units from the device
	 * 
	 * @return a String of units
	 * @throws IOException 
	 */
	public String getUnits() throws IOException {
		return getEmptyRequest(constructStartAddress(ATTRB_UNITS_TEXT));
	}
	
	/**
	 * Default empty request
	 * 
	 * @param startAddress
	 * 			- the startAddress for the request
	 * 
	 * @return the response string from the device
	 * 
	 * @throws IOException if something happened during the read
	 */
	private String getEmptyRequest(String startAddress) throws IOException{
		this.startAddress = startAddress;
		this.instance = archive.getValue();
		ReadArchiveCommand rac = new ReadArchiveCommand(link);
		rac.setStartAddress(getStartAddress());
		return rac.invokeForOneTransaction(EMPTY_REQUEST);
	}
	
	/**
	 * Construct the startAddress
	 * 
	 * @param attrbDescription 
	 * 			- the attribute number to read
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
	 * 			- the attribute number to read
	 * 
	 * @return the constructed startAddress
	 */

	private String constructStartAddress(String attrbDescription){
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(VIE);
		strBuilder.append(DOT);
		strBuilder.append(attrbDescription);
		return strBuilder.toString();
		
	}
	

	/**
	 * Request the number of intervals 
	 * 
	 * TODO check the timeZone, it's possible that you need to get it from the Rtu
	 * 
	 * @param from 
	 * 			- the date from where to start reading
	 * 
	 * @return
	 * 			a number representing the available intervals starting from the from date
	 * 
	 * @throws IOException if something freaky happened during the read 
	 */
	public String getNumberOfIntervals(Date from) throws IOException {
		String rawFrom = getRawDate(from);
		String requestString = buildRequestString(3, rawFrom, null, -1);
		this.startAddress = constructStartAddress(ATTRB_NR_OF_ENTR);
		ReadArchiveCommand rac = new ReadArchiveCommand(link);
		rac.setStartAddress(getStartAddress());
		return ProtocolUtils.stripBrackets(rac.invokeForOneTransaction(requestString));
	}
	
	/**
	 * Request the raw intervals
	 * 
	 * @param from
	 * @param blockSize
	 * @return
	 * @throws IOException 
	 */
	public String getIntervals(Date from, int blockSize) throws IOException {
		String rawFrom = getRawDate(from);
		String requestString = buildRequestString(3, rawFrom, null, blockSize);
		this.startAddress = constructStartAddress(ATTRB_VALUE);
		ReadArchiveCommand rac = new ReadArchiveCommand(link);
		rac.setStartAddress(getStartAddress());
		if(blockSize > 1){
			return rac.invokeForMultiple(requestString);
		} else {
			return rac.invokeForOneTransaction(requestString);
		}
	}
	
	/**
	 * Construct a date in the request format
	 * 
	 * @param date
	 * 			- the date to convert
	 * 
	 * @return raw date
	 */
	private String getRawDate(Date date){
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTime(date);
		return ClockObject.getRawData(cal);
	}

	/**
	 * Build up the request string.<br>
	 * A profile request string has a specific format, it can contain on or more of the following elements:
	 * 
	 * @param columnIndex
	 * 			- the column index of the archive
	 * 
	 * @param rawFrom
	 * 			- the startDate to read from (null -> unused)
	 * 
	 * @param rawTo
	 * 			- the toDate to read to	(null -> unused)
	 * 
	 * @param numbOfBlocks
	 * 			- the number of blocks to read (-1 -> unused)
	 * 
	 * @return the requestData in form of [p]; [v]; [b] where [p] is position, [v] is fromDate, [b] is toDate, [t] is number of blocks
	 */
	private String buildRequestString(int columnIndex, String rawFrom, String rawTo, int numbOfBlocks) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(columnIndex);
		strBuilder.append(SEMI_COLON);
		strBuilder.append((rawFrom!=null)?rawFrom:"");
		strBuilder.append(SEMI_COLON);
		strBuilder.append((rawTo!=null)?rawTo:"");
		strBuilder.append(SEMI_COLON);
		strBuilder.append((numbOfBlocks!=-1)?numbOfBlocks:"");
		return strBuilder.toString();
	}


}
