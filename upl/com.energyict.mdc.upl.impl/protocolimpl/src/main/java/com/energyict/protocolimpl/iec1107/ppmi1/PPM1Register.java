package com.energyict.protocolimpl.iec1107.ppmi1;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppmi1.parser.HistoricalDataParser;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

/** @author Koen, fbo */

public class PPM1Register {

	/* Data Types used in the meter */
	protected final static int STRING = 0;
	protected final static int DATE = 1;
	protected final static int NUMBER = 2;
	protected final static int LONG = 3;
	protected final static int BYTEARRAY = 4;
	protected final static int QUANTITY = 5;
	protected final static int INTEGER = 6;
	protected final static int BITFIELD64 = 7;
	protected final static int HEX = 9;
	protected final static int HEX_LE = 10;
	protected final static int MD = 11;
	protected final static int REGISTER = 14;
	protected final static int SCALINGFACTOR = 21;
	protected final static int LOADPROFILEDEF = 22;
	protected final static int HISTORICAL = 23;

	/* obvious stuff, but I like code that way */
	protected static final boolean CACHED = true;
	protected static final boolean NOT_CACHED = false;

	protected static final boolean WRITEABLE = true;
	protected static final boolean NOT_WRITEABLE = false;

	protected static final boolean READABLE = true;
	protected static final boolean NOT_READABLE = false;

	private String dataId;
	private String name;
	private int type;
	private int offset;
	private int length;
	private Object value;

	private boolean writeable;
	private boolean cached;
	private boolean readable;

	private RegisterFactory registerFactory = null;

	/**
	 * Creates a new instance of Register
	 * 
	 * @param dataId
	 * @param name
	 * @param type
	 * @param offset
	 * @param length
	 * @param writeable
	 * @param cached
	 */
	protected PPM1Register(String dataId, String name, int type, int offset, int length, boolean writeable, boolean cached) {
		init(dataId, name, type, offset, length, writeable, cached, true);
	}

	/**
	 * Creates a new instance of Register
	 * 
	 * @param dataId
	 * @param name
	 * @param type
	 * @param offset
	 * @param length
	 * @param writeable
	 * @param cached
	 * @param readable
	 */
	protected PPM1Register(String dataId, String name, int type, int offset, int length, boolean writeable, boolean cached, boolean readable) {
		init(dataId, name, type, offset, length, writeable, cached, readable);
	}

	/**
	 * Creates a new instance of Register
	 * 
	 * @param dataId
	 * @param name
	 * @param type
	 * @param offset
	 * @param length
	 * @param writeable
	 * @param cached
	 * @param readable
	 */
	protected void init(String dataId, String name, int type, int offset, int length, boolean writeable, boolean cached, boolean readable) {
		this.dataId = dataId;
		this.name = name;
		this.type = type;
		this.offset = offset;
		this.length = length;
		this.writeable = writeable;
		this.cached = cached;
		this.readable = readable;
	}

	/**
	 * @return
	 */
	protected int getType() {
		return type;
	}

	/**
	 * @return
	 */
	protected int getOffset() {
		return offset;
	}

	/**
	 * @return
	 */
	protected int getLength() {
		return length;
	}

	/**
	 * @return
	 */
	protected boolean isWriteable() {
		return writeable;
	}

	/**
	 * @return
	 */
	protected boolean isReadable() {
		return readable;
	}

	/**
	 * @return
	 */
	protected boolean isCached() {
		return cached;
	}

	/**
	 * @return
	 */
	protected String getDataID() {
		return dataId;
	}

	/**
	 * @return
	 */
	protected String getName() {
		return name;
	}

	/**
	 * @param abba1700RegisterFactory
	 */
	protected void setRegisterFactory(RegisterFactory abba1700RegisterFactory) {
		this.registerFactory = abba1700RegisterFactory;
	}

	/**
	 * @return
	 */
	protected DataIdentityFactory getDataIdentityFactory() {
		return registerFactory.getDataIdentityFactory();
	}

	/**
	 * @return
	 */
	protected FlagIEC1107Connection getFlagIEC1107Connection() {
		return registerFactory.getPpm().getFlagIEC1107Connection();
	}

	/**
	 * @return
	 */
	protected PPM getProtocolLink() {
		return registerFactory.getPpm();
	}

	/**
	 * @param value
	 * @throws FlagIEC1107ConnectionException
	 * @throws IOException
	 */
	protected void writeRegister(String value) throws FlagIEC1107ConnectionException, IOException {
		this.value = null;
		getDataIdentityFactory().setDataIdentity(getDataID(), value);
	}

	/**
	 * @param object
	 * @throws FlagIEC1107ConnectionException
	 * @throws IOException
	 */
	protected void writeRegister(Object object) throws FlagIEC1107ConnectionException, IOException {
		this.value = null;
		getDataIdentityFactory().setDataIdentity(getDataID(), buildData(object));
	}

	/**
	 * @param cached
	 * @param dataLength
	 * @param set
	 * @return
	 * @throws FlagIEC1107ConnectionException
	 * @throws IOException
	 */
	byte[] readRegister(boolean cached, int dataLength, int set) throws FlagIEC1107ConnectionException, IOException {
		return getDataIdentityFactory().getDataIdentity(getDataID(), cached, dataLength, set);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	protected Object getValue() throws IOException {
		if (value == null || !cached) {
			value = parse(this.readRegister(cached, -1, 0));
		}
		return value;
	}

	/**
	 * @param object
	 * @return
	 * @throws IOException
	 */
	protected String buildData(Object object) throws IOException {
		switch (getType()) {
		case STRING :
			return (String) object;
		case DATE :
			return new String(PPMUtils.buildDate((Date) object, registerFactory.getPpm().getTimeZone()));
		case NUMBER :
			return null;
		case LONG :
			return null;
		case INTEGER :
			return null;
		case BITFIELD64 :
			return null;
		case BYTEARRAY :
			return null;
		case QUANTITY :
			return null;
		case HEX :
			return null;
		case HEX_LE :
			return PPMUtils.buildHexLE((Long) object);
		default :
			throw new IOException("Register, buildData , unknown type " + getType());
		}
	}

	/**
	 * Parse byte[] to object
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	protected Object parse(byte[] data) throws IOException {

		try {
			switch (getType()) {
			case STRING:
				return new String(ProtocolUtils.getSubArray2(data, getOffset(), getLength()));
			case DATE:
				return PPMUtils.parseDate(data, 0, registerFactory.getPpm().getTimeZone());
			case LONG:
				return PPMUtils.parseLong(data, getOffset(), getLength());
			case INTEGER :
				return PPMUtils.parseInteger(data, getOffset(), getLength());
			case BITFIELD64 :
				return PPMUtils.parseBitfield(data, getOffset(), getLength());
			case BYTEARRAY :
				return ProtocolUtils.getSubArray2(data, getOffset(), getLength());
			case QUANTITY :
				MetaRegister metaRegister = registerFactory.getRegisterInformation().get(name);
				Unit unit = null;
				BigDecimal scaleFactor = null;
				if (metaRegister != null) {
					unit = metaRegister.getUnit();
					scaleFactor = metaRegister.getRegisterScaleFactor();
				}
				return PPMUtils.parseQuantity(data, getOffset(), getLength(), scaleFactor, unit);
			case HEX :
				return PPMUtils.parseLongHex(data, getOffset(), getLength());
			case HEX_LE:
				return PPMUtils.parseLongHexLE(data, getOffset(), getLength());
			case MD:
				metaRegister = registerFactory.getRegisterInformation().get(name);
				unit = null;
				scaleFactor = null;
				if (metaRegister != null) {
					unit = metaRegister.getUnit();
					scaleFactor = metaRegister.getRegisterScaleFactor();
				}
				byte[] d = ProtocolUtils.getSubArray2(data, getOffset(), getLength());
				return new MaximumDemand(unit, d, scaleFactor, getProtocolLink().getTimeZone());
			case REGISTER:
				metaRegister = registerFactory.getRegisterInformation().get(name);
				unit = null;
				scaleFactor = null;
				if (metaRegister != null) {
					unit = metaRegister.getUnit();
					scaleFactor = metaRegister.getRegisterScaleFactor();
				}
				Quantity q = PPMUtils.parseQuantity(data, getOffset(), getLength(), scaleFactor, unit);
				return new MainRegister(metaRegister, q);
			case SCALINGFACTOR :
				return ScalingFactor.parse(data[getOffset()]);
			case LOADPROFILEDEF :
				return new LoadProfileDefinition(ProtocolUtils.getSubArray2(data, getOffset(), getLength()));
			case HISTORICAL :
				HistoricalDataParser hdp = new HistoricalDataParser(registerFactory.getPpm(), registerFactory);
				hdp.setInput(data);
				return hdp.match();
			default :
				throw new IOException("Register, parse , unknown type " + getType());
			}
		} catch (NumberFormatException e) {
			throw new IOException("Register, parse error");
		}
	}

}