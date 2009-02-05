/**
 * 
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

/**
 * @author gna
 *
 */
public class GPRSModemSetup extends AbstractCosemObject {
	
	/** Attributes */
	private OctetString apn = null;	// Defines the accessPoint name of the network
	private Unsigned16 pincode = null;	// Holds the personal identification number
	private Structure qualityOfService = null;
	
	/** Attribute numbers */
	static private final int ATTRB_APN = 2;
	static private final int ATTRB_PIN_CODE = 3;
	static private final int ATTRB_QUALITY_OF_SERVICE = 4;
	
	/** Method invoke */
	// none
	
	private int CLASSID = 45;
	
	static private final int QOS_DEFAULT = 0;
	static private final int QOS_REQUESTED = 1;

	/**
	 * @param protocolLink
	 * @param objectReference
	 */
	public GPRSModemSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return this.CLASSID;
	}
	
	/**
	 * Reads the current APN from the device
	 * @return
	 * @throws IOException
	 */
	public OctetString readAPN() throws IOException{
		try{
			this.apn = new OctetString(getLNResponseData(ATTRB_APN));
			return this.apn;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the Access Point Name(apn)." + e.getMessage());
		}
	}
	
	/**
	 * Return the latest retrieved apn
	 * @return
	 * @throws IOException
	 */
	public OctetString getAPN() throws IOException{
		if(this.apn == null){
			readAPN();	// do a dummy read
		}
		return this.apn;
	}
	
	/**
	 * Write the given apn octetString to the device
	 * @param apn
	 * @throws IOException
	 */
	public void writeAPN(OctetString apn) throws IOException{
		try{
			write(ATTRB_APN, apn.getBEREncodedByteArray());
			this.apn = apn;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the Access Point Name(apn)." + e.getMessage());
		}
	}

	/**
	 * Write the given apn string to the device
	 * @param apn
	 * @throws IOException
	 */
	public void writeAPN(String apn) throws IOException{
		this.writeAPN(OctetString.fromString(apn));
	}
	
	/**
	 * Read the current pincode from the device
	 * @return
	 * @throws IOException
	 */
	public Unsigned16 readPinCode() throws IOException{
		try{
			this.pincode = new Unsigned16(getLNResponseData(ATTRB_PIN_CODE), 0);
			return this.pincode;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the pincode." + e.getMessage());
		}
	}
	
	/**
	 * Return the latest retrieved pincode
	 * @return
	 * @throws IOException
	 */
	public Unsigned16 getPinCod() throws IOException{
		if(this.pincode == null){
			readPinCode();	// do a dummy read
		}
		return this.pincode;
	}
	
	/**
	 * Write the given unsigned16 pincode to the device
	 * @param pincode
	 * @throws IOException
	 */
	public void writePinCode(Unsigned16 pincode) throws IOException{
		try{
			write(ATTRB_PIN_CODE, pincode.getBEREncodedByteArray());
			this.pincode = pincode;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the pincode." + e.getMessage());
		}
	}
	
	/**
	 * Write the given pincode to the device
	 * @param pincode
	 * @throws IOException
	 */
	public void writePinCode(long pincode) throws IOException{
		this.writePinCode(new Unsigned16((int)pincode));
	}
	
	/**
	 * Read the current quality of Service from the device
	 * @return
	 * @throws IOException
	 */
	public Structure readQualityOfService() throws IOException{
		try{
			this.qualityOfService = new Structure(getLNResponseData(ATTRB_QUALITY_OF_SERVICE), 0, 0);
			return this.qualityOfService;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the quality of service." + e.getMessage());
		}
	}
	
	/**
	 * Get the latest retrieved quality of service structure
	 * @return
	 * @throws IOException
	 */
	public Structure getQualityOfService() throws IOException{
		if(this.qualityOfService == null){
			readQualityOfService();		// do a dummy read
		}
		return this.qualityOfService;
	}
	
	/**
	 * Return the default QOS structure
	 * @return
	 * @throws IOException
	 */
	public Structure getTheDefaultQualityOfService() throws IOException{
		if(getQualityOfService().getDataType(QOS_DEFAULT).isStructure()){
			return (Structure)getQualityOfService().getDataType(QOS_DEFAULT);
		} else {
			throw new IOException("The QOS structure does not contain a default QOS structure ...");
		}
	}
	
	/**
	 * Return the requested QOS structure
	 * @return
	 * @throws IOException
	 */
	public Structure getRequestedQualityOfService() throws IOException{
		if(getQualityOfService().getDataType(QOS_REQUESTED).isStructure()){
			return (Structure)getQualityOfService().getDataType(QOS_REQUESTED);
		} else {
			throw new IOException("The QOS structure does not contain a requested QOS structure ...");
		}
	}
	
	/**
	 * Write the given quality of service structure to the device
	 * @param qos
	 * @throws IOException
	 */
	public void writeQualityOfService(Structure qos) throws IOException{
		try{
			write(ATTRB_QUALITY_OF_SERVICE, qos.getBEREncodedByteArray());
			this.qualityOfService = qos;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the pincode." + e.getMessage());
		}
	}
	
	/**
	 * Write the given default and requested qos structures to the device
	 * @param defaultQOS
	 * @param requestedQOS
	 * @throws IOException
	 */
	public void writeQualityOfService(Structure defaultQOS, Structure requestedQOS) throws IOException{
		Structure qos = new Structure();
		qos.addDataType(defaultQOS);
		qos.addDataType(requestedQOS);
		writeQualityOfService(qos);
	}
}
