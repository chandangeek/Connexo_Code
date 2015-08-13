package com.elster.protocolimpl.dsfg.telegram;

import com.elster.protocolimpl.dsfg.util.DsfgBlockInputStream;
import com.energyict.dialer.connection.Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * @version  1.0 (5/10/2010)
 * @author   Gunter Heuckeroth
 *
 * <P>
 * <B>Description :</B><BR>
 *  a class containing the data of a dsfg data block
 * 
 * <B>Changes :</B><BR>
 */
/**
 * This defines the class for a dsfg data block
 * 
 * @author heuckeg
 * 
 */
public class DataBlock {

	private static byte[] US = { Connection.US };
	private static byte[] FS = { Connection.FS };
	public static byte[] USFS = { Connection.US, Connection.FS };
	public static byte[] GSFS = { Connection.GS, Connection.FS };

	public static String SFS = new String(FS);
	public static String SUS = new String(US);

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
	private static SimpleDateFormat stf = new SimpleDateFormat("HHmmss");

	/** receiver address */
	private char TRN;
	/** header data flag */
	private int DID;
	/** data exchange reference */
	private int TID;
	/** amount of blocks */
	private int BLO;
	/** block number */
	private int BNR;
	/** sender address */
	private char DNO;
	/** block type (A - request, R - answer ) */
	private char NTY;
	/** Answer requested */
	private char DFO;
	/** type of data elements */
	private char DEB;
	/** no of data elements */
	private int ZAE;
	/** date/Time of creation of block */
	private Date TST;
	/** Data elements */
	private Vector<DataElement> data = new Vector<DataElement>();

	public DataBlock(String receiver, char typeOfBlock, char answerRequested,
			char typeOfElements) {
		this(receiver, typeOfBlock, answerRequested, typeOfElements, "");
	}

	public DataBlock(String receiver, char typeOfBlock, char answerRequested,
			char typeOfElements, String data) {
		this(receiver, typeOfBlock, answerRequested, typeOfElements,
				new DataElement[] { new DataElement(data) });
	}

	public DataBlock(String receiver, char typeOfBlock, char answerRequested,
			char typeOfElements, DataElement[] data) {
		TRN = receiver.charAt(0);
		DID = 255;
		TID = 1;
		BLO = 1;
		BNR = 1;
		DNO = 0;
		NTY = typeOfBlock;
		DFO = answerRequested;
		DEB = typeOfElements;
		ZAE = 0;

		setData(data);
	}

	public DataBlock(byte[] data) throws IOException {
		DsfgBlockInputStream indata = new DsfgBlockInputStream(data);

		// skip first STX
		indata.read();

		TRN = indata.readChar(US);
		DID = indata.readInt(US);
		TID = indata.readInt(US);
		BLO = indata.readInt(US);
		BNR = indata.readInt(US);
		DNO = indata.readChar(US);
		NTY = indata.readChar(US);
		DFO = indata.readChar(US);
		DEB = indata.readChar(US);
		ZAE = indata.readInt(USFS);

		TST = new Date(0);
		if ((DID & 0x1000) != 0) {
			String d = indata.readString(USFS);
			try {
				Date tda = sdf.parse(d);
				TST.setTime(tda.getTime());
			} catch (ParseException ignored) {

			}
		}
		if ((DID & 0x2000) != 0) {
			String d = indata.readString(USFS);
			try {
				Date tti = stf.parse(d);
				TST.setTime(tti.getTime() + TST.getTime());
			} catch (ParseException ignored) {

			}
		}

		this.data.clear();

		for (int i = 0; i < ZAE; i++) {
			String s = indata.readString(GSFS);
			this.data.add(new DataElement(s));
		}
	}

	public char getReceiver() {
		return TRN;
	}

	public void setReceiver(char trn) {
		TRN = trn;
	}

	public int getHeaderDataFlags() {
		return DID;
	}

	public void setHeaderDataFlags(int did) {
		DID = did;
	}

	public int getDataExchangeReference() {
		return TID;
	}

	public void setDataExchangeReference(int tid) {
		TID = tid;
	}

	public int getBlockCount() {
		return BLO;
	}

	public void setBlockCount(int blo) {
		BLO = blo;
	}

	public int getBlockNumber() {
		return BNR;
	}

	public void setBlockNumber(int bnr) {
		BNR = bnr;
	}

	public char getSender() {
		return DNO;
	}

	public void setSender(char dno) {
		DNO = dno;
	}

	public char getTypeOfBlock() {
		return NTY;
	}

	public void setTypeOfBlock(char nty) {
		NTY = nty;
	}

	public char getAnswerRequested() {
		return DFO;
	}

	public void setAnswerRequested(char dfo) {
		DFO = dfo;
	}

	public char getTypeOfElements() {
		return DEB;
	}

	public void setTypeOfElements(char deb) {
		DEB = deb;
	}

	/**
	 * gets the number of data elements
	 * 
	 * @return int
	 */
	public int getNumberOfElements() {
		return ZAE;
	}

	/**
	 * gets the data of the block
	 * 
	 * @return array of Strings
	 */
	public DataElement[] getData() {
		return data.toArray(new DataElement[data.size()]);
	}

	/**
	 * set the data of a block
	 * 
	 * @param data
	 *            is an array of Strings with the data
	 */
	public void setData(DataElement[] data) {
		if (data == null) {
			ZAE = 0;
		}
		else {
			ZAE = data.length;
		}
		this.data.clear();

        if (data != null)
        {
            this.data.addAll(Arrays.asList(data));
        }
	}

	/**
	 * converting all data into an array of bytes
	 * 
	 * @return array of bytes containing a "ready to send" data block
	 * 
	 * @throws IOException
	 */
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		DID = 0xFF | 0x3000;
		buffer.write(TRN);
		buffer.write(Connection.US);
		buffer.write(Integer.toString(DID).getBytes());
		buffer.write(Connection.US);
		buffer.write(Integer.toString(TID).getBytes());
		buffer.write(Connection.US);
		buffer.write(Integer.toString(BLO).getBytes());
		buffer.write(Connection.US);
		buffer.write(Integer.toString(BNR).getBytes());
		buffer.write(Connection.US);
		buffer.write(DNO);
		buffer.write(Connection.US);
		buffer.write(NTY);
		buffer.write(Connection.US);
		buffer.write(DFO);
		buffer.write(Connection.US);
		buffer.write(DEB);
		buffer.write(Connection.US);
		buffer.write(Integer.toString(ZAE).getBytes());
		buffer.write(Connection.US);

		// insert current date and time
		TST = new Date();
		buffer.write(sdf.format(TST).getBytes());
		buffer.write(Connection.US);
		buffer.write(stf.format(TST).getBytes());
		buffer.write(Connection.US);

		// add data elements
		for (int i = 0; i < data.size();) {
			String s = data.get(i++).toString();
			buffer.write(s.getBytes());
			if (i < data.size()) {
				buffer.write(Connection.GS);
			}
			else {
				buffer.write(Connection.FS);
			}
		}

		return buffer.toByteArray();
	}

	/**
	 * the elements of an answer data block contains address and data
	 * 
	 * @return list of addresses
	 */
	public List<String> getDataKeys() {
		ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i < data.size(); i++) {
			result.add(data.get(i).getAddress());
		}

		return result;
	}

	/**
	 * gets a single value of an answer. The value is identified by key and the
	 * position of the value (a data element consists of the key, the separator
	 * US and a single value. More values can follow, separated by US.
	 * 
	 * @param key
	 *            - of the value
	 *
	 * @return value
	 */
	public DataElement getElementOf(String key) {
		for (int i = 0; i < data.size(); i++) {
			if (key.equalsIgnoreCase(data.get(i).getAddress())) {
				return data.get(i);
			}
		}
		return null;
	}

	/**
	 * gets a single value of an answer. The value is identified it's index and
	 * the position of the value (a data element consists of the key, the
	 * separator US and a single value. More values can follow, separated by US.
	 * 
	 * @param index
	 *            - of data element in the answer
	 *
	 * @return value
	 */
	public DataElement getElementAt(int index) {
		if (index >= data.size())
			return null;
		return data.get(index);
		
	}
}
