package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ppm.parser.HistoricalDataParser;
import com.energyict.protocolimpl.iec1107.ppm.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppm.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppm.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppm.register.ScalingFactor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/** @author Koen, fbo */

public class Register {

	final static int STRING = 0;
	final static int DATE = 1;
	final static int NUMBER = 2;
	final static int LONG = 3;
	final static int BYTEARRAY = 4;
	final static int QUANTITY = 5;
	final static int INTEGER = 6;
	final static int BITFIELD64 = 7;
	final static int HEX = 9;
	final static int HEX_LE = 10;
	final static int MD = 11;
	final static int REGISTER = 14;
	final static int SCALINGFACTOR = 21;
	final static int LOADPROFILEDEF = 22;
        final static int HISTORICAL = 23;
        final static int LAST_BILLING = 24;

	static protected final boolean CACHED = true;
	static protected final boolean NOT_CACHED = false;

	static protected final boolean WRITEABLE = true;
	static protected final boolean NOT_WRITEABLE = false;

	private String dataId;
	private String name;
	private int type;
	private int offset;
	private int length;
	private Unit unit;
	private Object value;
	private MetaRegister metaRegister;
	//TimeZone timeZone = null; // KV 22072005

	private boolean writeable;
	private boolean cached;

	private RegisterFactory registerFactory = null;

	/** Creates a new instance of Register */
	protected Register(String dataId, String name, int type, int offset,
			int length,boolean writeable,
			boolean cached) {
		this.dataId = dataId;
		this.name = name;
		this.type = type;
		this.offset = offset;
		this.length = length;
		this.writeable = writeable;
		this.cached = cached;
	}

	protected void setUnit(Unit unit) {
		this.unit = unit;
	}

	protected int getType() {
		return type;
	}

	protected int getOffset() {
		return offset;
	}

	protected int getLength() {
		return length;
	}

	protected boolean isWriteable() {
		return writeable;
	}

	protected boolean isCached() {
		return cached;
	}

	protected String getDataID() {
		return dataId;
	}

	protected String getName() {
		return name;
	}

	protected void setMetaRegister( MetaRegister metaRegister ){
		this.metaRegister = metaRegister;
	}



	/* _________ __________ */

	protected void setRegisterFactory(RegisterFactory abba1700RegisterFactory) {
		this.registerFactory = abba1700RegisterFactory;
	}


	protected DataIdentityFactory getDataIdentityFactory() {
		return registerFactory.getDataIdentityFactory();
	}
	protected FlagIEC1107Connection getFlagIEC1107Connection() {
		return registerFactory.getPpm().getFlagIEC1107Connection();
	}
	protected PPM getProtocolLink() {
		return registerFactory.getPpm();
	}

	/* ________ ______ */

	protected void writeRegister(String value)
			throws IOException {
		this.value = null;
		getDataIdentityFactory().setDataIdentity(getDataID(), value);
	}

	protected void writeRegister(Object object)
			throws IOException {
		this.value = null;
		getDataIdentityFactory()
				.setDataIdentity(getDataID(), buildData(object));
	}

	byte[] readRegister(boolean cached, int dataLength, int set)
            throws IOException {
		return getDataIdentityFactory().getDataIdentity(getDataID(), cached,
				dataLength, set);
	}

	protected Object getValue() throws IOException {
		if (value == null || !cached )
			value = parse(this.readRegister(cached, -1, 0));
		return value;
	}

	protected String buildData(Object object) throws IOException {
		switch (getType()) {
			case STRING :
				return (String) object;

			case DATE :
				return new String( PPMUtils.buildDate((Date) object, registerFactory
						.getPpm().getTimeZone()) );

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
				throw new IOException("Register, buildData , unknown type "
						+ getType());
		}
	}

	protected Object parse(byte[] data) throws IOException {

		try {
			switch (getType()) {
				case STRING :
					return new String(data);

				case DATE :
					return PPMUtils.parseDate(data, 0, registerFactory.getPpm()
							.getTimeZone());

				case LONG :
					return PPMUtils.parseLong(data, getOffset(), getLength());

				case INTEGER :
					return PPMUtils
							.parseInteger(data, getOffset(), getLength());

				case BITFIELD64 :
					return PPMUtils.parseBitfield(data, getOffset(),
							getLength());

				case BYTEARRAY :
					return data;

				case QUANTITY :
					MetaRegister metaRegister =
						registerFactory.getRegisterInformation().get( name );
					Unit unit = null;
					BigDecimal scaleFactor = null;
					if( metaRegister != null ){
						unit = metaRegister.getUnit();
						scaleFactor = metaRegister.getRegisterScaleFactor();
					}
					return PPMUtils.parseQuantity(data, getOffset(),
							getLength(), scaleFactor, unit );

				case HEX :
					return PPMUtils
							.parseLongHex(data, getOffset(), getLength());

				case HEX_LE :
					return PPMUtils.parseLongHexLE(data, getOffset(),
							getLength());

				case MD :
					metaRegister =
						registerFactory.getRegisterInformation().get( name );
					unit = null;
					scaleFactor = null;
					if( metaRegister != null ){
						unit = metaRegister.getUnit();
						scaleFactor = metaRegister.getRegisterScaleFactor();
					}
					byte[] d = ProtocolUtils.getSubArray2(data, getOffset(),
							getLength());
					return new MaximumDemand( unit, d, scaleFactor,
							getProtocolLink().getTimeZone() );

				case REGISTER :
					metaRegister =
						registerFactory.getRegisterInformation().get( name );
					unit = null;
					scaleFactor = null;
					if( metaRegister != null ){
						unit = metaRegister.getUnit();
						scaleFactor = metaRegister.getRegisterScaleFactor();
					}
					Quantity q = PPMUtils.parseQuantity(data, getOffset(),
							getLength(), scaleFactor, unit);
					return new MainRegister(metaRegister, q);

				case SCALINGFACTOR :
					return ScalingFactor.parse(data[0]);

				case LOADPROFILEDEF :
					return new LoadProfileDefinition(data);

                case HISTORICAL :
                    HistoricalDataParser hdp =
                            new HistoricalDataParser( registerFactory.getPpm(),
                                                      registerFactory );
                    hdp.setInput( data );
                    return hdp.match();

                case LAST_BILLING:
                    HistoricalDataParser hdpl =
                            new HistoricalDataParser( registerFactory.getPpm(),
                                                      registerFactory );
                    hdpl.setInput( data );
                    return hdpl.matchPeriod();

                default :
                    throw new ProtocolException("Register, parse , unknown type "
							+ getType());
			}
		} catch (NumberFormatException e) {
			throw new ProtocolException("Register, parse error:" + e.getMessage());
        }
    }

    public MetaRegister getMetaRegister() {
        return metaRegister;
    }

    public Unit getUnit() {
        return unit;
    }

}