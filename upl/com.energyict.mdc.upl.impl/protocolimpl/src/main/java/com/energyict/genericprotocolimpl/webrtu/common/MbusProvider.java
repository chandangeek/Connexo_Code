package com.energyict.genericprotocolimpl.webrtu.common;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.obis.ObisCode;

/**
 * Provider for the Mbus serialNumber
 * 
 * @author gna
 *
 */
public class MbusProvider {

	private CosemObjectFactory cosemObjectFactory;
	
	public MbusProvider(CosemObjectFactory cof){
		this.cosemObjectFactory = cof;
	}
	
	/**
	 * Construct the serialNumber for the given Mbus channel
	 * The serialNumber is constructed according to:
	 * "RFC13 of Enexis’ NTA 2009 meter project and RFC037 of Enexis’ Gridfield II project"
	 * @param mbusChannel - the given Mbus channel
	 * @return - the constructed serialNumber of the Mbus device
	 * @throws IOException
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
	 * Construct the shortId from the four given fields
	 * @param manufacturer - the manufacturer ID of the meter
	 * @param identification - the identification number(serialnumber) of the meter
	 * @param version - the version of the device type
	 * @param deviceType - the device type
	 * @return a string which is a concatenation of the manipulated given fields
	 */
	protected String constructShortId(Unsigned16 manufacturer, Unsigned32 identification, Unsigned8 version, Unsigned8 deviceType){
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append((char)(((manufacturer.getValue()&0x7D00)/32/32)+64));
		strBuilder.append((char)(((manufacturer.getValue()&0x03E0)/32)+64));
		strBuilder.append((char)((manufacturer.getValue()&0x001F)+64));
		
		strBuilder.append(String.format("%08x", identification.getValue()));	// 8 Hex digits with leading zeros
		strBuilder.append(String.format("%03d", version.getValue()));			// 3 Dec digits with leading zeros
		strBuilder.append(String.format("%02d", deviceType.getValue()));		// 2 Dec digits with leading zeros
		
		return strBuilder.toString();
	}
	
}
