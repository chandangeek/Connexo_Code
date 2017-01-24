/**
 *
 */
package com.energyict.dlms.cosem;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributeobjects.QualityOfService;
import com.energyict.dlms.cosem.attributeobjects.QualityOfServiceElement;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * @author gna
 *
 */
public class GPRSModemSetup extends AbstractCosemObject {

	/** Attributes */
	private OctetString apn = null;	// Defines the accessPoint name of the network
	private Unsigned16 pincode = null;	// Holds the personal identification number
	private QualityOfService qualityOfService = null;

	/** Attribute numbers */
	private static final int ATTRB_APN = 2;
	private static final int ATTRB_PIN_CODE = 3;
	private static final int ATTRB_QUALITY_OF_SERVICE = 4;

	/** Method invoke */
	// none

	private static final int QOS_DEFAULT = 0;
	private static final int QOS_REQUESTED = 1;

    private static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.25.4.0.255");

	/**
	 * @param protocolLink
	 * @param objectReference
	 */
	public GPRSModemSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

    public final static ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    protected int getClassId() {
		return DLMSClassId.GPRS_SETUP.getClassId();
	}

	/**
	 * Reads the current APN from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public OctetString readAPN() throws IOException {
		try{
			this.apn = new OctetString(getLNResponseData(ATTRB_APN), 0);
			return this.apn;
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the Access Point Name(apn)." + e.getMessage());
		}
	}

	/**
	 * Return the latest retrieved apn
	 * @return
	 * @throws java.io.IOException
	 */
	public OctetString getAPN() throws IOException {
		if(this.apn == null){
			readAPN();	// do a dummy read
		}
		return this.apn;
	}

	/**
	 * Write the given apn octetString to the device
	 * @param apn
	 * @throws java.io.IOException
	 */
	public void writeAPN(OctetString apn) throws IOException {
		try{
			write(ATTRB_APN, apn.getBEREncodedByteArray());
			this.apn = apn;
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the Access Point Name(apn)." + e.getMessage());
		}
	}

	/**
	 * Write the given apn string to the device
	 * @param apn
	 * @throws java.io.IOException
	 */
	public void writeAPN(String apn) throws IOException {
		this.writeAPN(OctetString.fromString(apn));
	}

	/**
	 * Read the current pincode from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 readPinCode() throws IOException {
		try{
			this.pincode = new Unsigned16(getLNResponseData(ATTRB_PIN_CODE), 0);
			return this.pincode;
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the pincode." + e.getMessage());
		}
	}

	/**
	 * Return the latest retrieved pincode
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 getPinCod() throws IOException {
		if(this.pincode == null){
			readPinCode();	// do a dummy read
		}
		return this.pincode;
	}

	/**
	 * Write the given unsigned16 pincode to the device
	 * @param pincode
	 * @throws java.io.IOException
	 */
	public void writePinCode(Unsigned16 pincode) throws IOException {
		try{
			write(ATTRB_PIN_CODE, pincode.getBEREncodedByteArray());
			this.pincode = pincode;
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the pincode." + e.getMessage());
		}
	}

	/**
	 * Write the given pincode to the device
	 * @param pincode
	 * @throws java.io.IOException
	 */
	public void writePinCode(long pincode) throws IOException {
		this.writePinCode(new Unsigned16((int)pincode));
	}

	/**
	 * Read the current quality of Service from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public QualityOfService readQualityOfService() throws IOException {
		try{
			this.qualityOfService = QualityOfService.fromStructure(new Structure(getLNResponseData(ATTRB_QUALITY_OF_SERVICE), 0, 0));
			return this.qualityOfService;
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not retrieve the quality of service." + e.getMessage());
		}
	}

	/**
	 * Get the latest retrieved quality of service structure
	 * @return
	 * @throws java.io.IOException
	 */
	public Structure getQualityOfService() throws IOException {
		if(this.qualityOfService == null){
			readQualityOfService();		// do a dummy read
		}
		return this.qualityOfService;
	}

	/**
	 * Return the default QOS structure
	 * @return
	 * @throws java.io.IOException
	 */
	public Structure getTheDefaultQualityOfService() throws IOException {
		if(getQualityOfService().getDataType(QOS_DEFAULT).isStructure()){
			return (Structure)getQualityOfService().getDataType(QOS_DEFAULT);
		} else {
			throw new ProtocolException("The QOS structure does not contain a default QOS structure ...");
		}
	}

	/**
	 * Return the requested QOS structure
	 * @return
	 * @throws java.io.IOException
	 */
	public Structure getRequestedQualityOfService() throws IOException {
		if(getQualityOfService().getDataType(QOS_REQUESTED).isStructure()){
			return (Structure)getQualityOfService().getDataType(QOS_REQUESTED);
		} else {
			throw new ProtocolException("The QOS structure does not contain a requested QOS structure ...");
		}
	}

	/**
	 * Write the given quality of service structure to the device
	 * @param qos
	 * @throws java.io.IOException
	 */
	public void writeQualityOfService(final QualityOfService qos) throws IOException {
		try{
			write(ATTRB_QUALITY_OF_SERVICE, qos.getBEREncodedByteArray());
			this.qualityOfService = qos;
		} catch (IOException e){
			e.printStackTrace();
			throw new NestedIOException(e, "Could not write the pincode." + e.getMessage());
		}
	}

	/**
	 * Write the given default and requested qos structures to the device
	 * @param defaultQOS
	 * @param requestedQOS
	 * @throws java.io.IOException
	 */
	public void writeQualityOfService(QualityOfServiceElement defaultQOS, QualityOfServiceElement requestedQOS) throws IOException {
		Structure qos = new Structure();
		qos.addDataType(defaultQOS);
		qos.addDataType(requestedQOS);
		writeQualityOfService(QualityOfService.fromStructure(qos));
	}
}
