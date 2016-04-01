package com.energyict.genericprotocolimpl.webrtu.common;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;

import java.io.IOException;

/**
 * Provider for the Mbus serialNumber
 *
 * @author gna
 *
 */
public class MbusProvider {

	private CosemObjectFactory cosemObjectFactory;
	private final boolean fixMbusHexShortId;


	/**
	 * Constructor
	 *
	 * @param cof
	 *            - the {@link CosemObjectFactory} to use
	 *
	 * @param fixMbusHexShortId
	 *            - boolean indicating we need to convert the Identification number from hex or from BCD (true is
	 *            converting from hex)
	 */
	public MbusProvider(CosemObjectFactory cof, boolean fixMbusHexShortId){
		this.cosemObjectFactory = cof;
		this.fixMbusHexShortId = fixMbusHexShortId;
	}

	/**
	 * Construct the serialNumber for the given Mbus channel The serialNumber is constructed according to:
	 * "RFC13 of Enexis� NTA 2009 meter project and RFC037 of Enexis� Gridfield II project"
	 *
	 * @param obisCode
	 *            - the given Mbus channel
	 * @return - the constructed serialNumber of the Mbus device
	 * @throws IOException
	 *             if some MBus attributes could not be retrieved
	 */
	public String getMbusSerialNumber(ObisCode obisCode) throws IOException{
		MBusClient mClient = this.cosemObjectFactory.getMbusClient(obisCode);
		Unsigned16 manId = mClient.getManufacturerID();
		Unsigned32 idNum = mClient.getIdentificationNumber();
		Unsigned8 version =	mClient.getVersion();
		Unsigned8 devicet = mClient.getDeviceType();
		return constructShortId(manId, idNum, version, devicet);
	}

	/**
	 * Extracts the manufacturer id from the short id
	 * @param shortId
	 * @return manufacturer id
	 * @throws ProtocolException
	 */
	public Unsigned16 getManufacturerID(String shortId) throws ProtocolException {
		if(shortId == null || shortId.length() < 3){
			throw new ProtocolException("Invalid short id length.");
		}
		char[] chars = shortId.substring(0, 3).toCharArray();
		try {
			int id = Integer.parseInt("" + ((chars[2] - 64) + (chars[1] - 64) * 32 + (chars[0] - 64) * 32 * 32));
			return new Unsigned16(id);
		}catch (NumberFormatException e){
			throw new ProtocolException("Invalid short id value." );
		}
	}

	/**
	 * Extracts the identification number from the short id
	 * @param shortId
	 * @return identification number
	 * @throws IOException
	 */
	public Unsigned32 getIdentificationNumber(String shortId) throws IOException {
		if(shortId == null || shortId.length() < 11){
			throw new ProtocolException("Invalid short id length.");
		}
		try {
			if(fixMbusHexShortId)
				return new Unsigned32(Integer.parseInt(shortId.substring(3, 11)));
			else
				return new Unsigned32(Integer.parseInt(shortId.substring(3, 11), 16));
		}catch (NumberFormatException e){
			throw new ProtocolException("Invalid short id value." );
		}
	}

	/**
	 * Extracts the version from the short id
	 *
	 * @param shortId
	 * @return version
	 * @throws IOException
	 */
	public Unsigned8 getVersion(String shortId) throws IOException {
		if(shortId == null || shortId.length() < 14){
			throw new ProtocolException("Invalid short id length.");
		}
		try {
			return new Unsigned8(Integer.parseInt(shortId.substring(11,14)));
		}catch (NumberFormatException e){
			throw new ProtocolException("Invalid short id value." );
		}
	}

	/**
	 * Extracts the device type from the short id
	 * @param shortId
	 * @return device type
	 * @throws IOException
	 */
	public Unsigned8 getDeviceType(String shortId) throws IOException {
		if(shortId == null || shortId.length() < 16){
			throw new ProtocolException("Invalid short id length.");
		}
		try{
			return new Unsigned8(Integer.parseInt(shortId.substring(14, 16), 16));
		}catch (NumberFormatException e){
			throw new ProtocolException("Invalid short id value." );
		}
	}

	/**
	 * Construct the shortId from the four given fields
	 * @param manufacturer - the manufacturer ID of the meter
	 * @param identification - the identification number(serialnumber) of the meter
	 * @param version - the version of the device type
	 * @param deviceType - the device type
	 * @return a string which is a concatenation of the manipulated given fields
	 */
	protected String constructShortId(Unsigned16 manufacturer, Unsigned32 identification, Unsigned8 version, Unsigned8 deviceType){
		StringBuilder strBuilder = new StringBuilder();
		if(manufacturer.getValue() != 0) {
			strBuilder.append((char) (((manufacturer.getValue() & 0x7D00) / 32 / 32) + 64));
			strBuilder.append((char) (((manufacturer.getValue() & 0x03E0) / 32) + 64));
			strBuilder.append((char) ((manufacturer.getValue() & 0x001F) + 64));
		}
		strBuilder.append(String.format((this.fixMbusHexShortId)?"%08d":"%08x", identification.getValue()));	// 8 Hex digits with leading zeros
		strBuilder.append(String.format("%03d", version.getValue()));			// 3 Dec digits with leading zeros
		strBuilder.append(String.format("%02d", deviceType.getValue()));		// 2 Dec digits with leading zeros

		return strBuilder.toString();
	}

}
