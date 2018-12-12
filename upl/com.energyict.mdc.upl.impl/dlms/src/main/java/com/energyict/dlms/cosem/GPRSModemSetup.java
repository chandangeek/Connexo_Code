/**
 *
 */
package com.energyict.dlms.cosem;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Float32;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributeobjects.QualityOfService;
import com.energyict.dlms.cosem.attributeobjects.QualityOfServiceElement;
import com.energyict.dlms.cosem.attributes.GprsModemSetupAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * @author gna
 */
public class GPRSModemSetup extends AbstractCosemObject {

    /**
     * Attributes
     */
    private OctetString apn = null;    // Defines the accessPoint name of the network
    private Unsigned32 pincode = null;    // Holds the personal identification number
    private QualityOfService qualityOfService = null;
    private TypeEnum networkSelectionMode = null;
    private Array preferredOperatorList = null;
    private BooleanObject intlRoamingAllowed = null;
    private Unsigned32 minimumRSSI = null;
    private Float32 maximumBer = null;
    private Array networkTechnology = null;
    private BooleanObject isGprsPreferred = null;

    /**
     * Method invoke
     */
    // none

    private static final int LOGICAL_NAME = 1;
    private static final int APN = 2;
    private static final int PIN_CODE =3;
    private static final int QUALITY_OF_SERVICE = 4;
    private static final int NETWORK_SELECTION_MODE = -1;
    private static final int PREFERRED_OPERATOR_LIST = -2;
    private static final int INTL_ROAMING_ALLOWED = -4;
    private static final int MINIMUM_RSSI = -5;
    private static final int MAXIMUM_BER = -6;
    private static final int NETWORK_TECHNOLOGY = -7;
    private static final int IS_GPRS_PREFERRED = -8;

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
     *
     * @return
     * @throws java.io.IOException
     */
    public OctetString readAPN() throws IOException {
        try {
            this.apn = new OctetString(getLNResponseData(GprsModemSetupAttributes.APN.getAttributeNumber()), 0);
            return this.apn;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not retrieve the Access Point Name(apn)." + e.getMessage());
        }
    }

    /**
     * Return the latest retrieved apn
     *
     * @return
     * @throws java.io.IOException
     */
    public OctetString getAPN() throws IOException {
        if (this.apn == null) {
            readAPN();    // do a dummy read
        }
        return this.apn;
    }

    /**
     * Write the given apn octetString to the device
     *
     * @param apn
     * @throws java.io.IOException
     */
    public void writeAPN(OctetString apn) throws IOException {
        try {
            write(GprsModemSetupAttributes.APN.getAttributeNumber(), apn.getBEREncodedByteArray());
            this.apn = apn;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not write the Access Point Name(apn)." + e.getMessage());
        }
    }

    /**
     * Write the given apn string to the device
     *
     * @param apn
     * @throws java.io.IOException
     */
    public void writeAPN(String apn) throws IOException {
        this.writeAPN(OctetString.fromString(apn));
    }

    /**
     * Read the current pincode from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned32 readPinCode() throws IOException {
        try {
            this.pincode = new Unsigned32(getLNResponseData(GprsModemSetupAttributes.PIN_CODE.getAttributeNumber()), 0);
            return this.pincode;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not retrieve the pincode." + e.getMessage());
        }
    }

    /**
     * Return the latest retrieved pincode
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned32 getPinCod() throws IOException {
        if (this.pincode == null) {
            readPinCode();    // do a dummy read
        }
        return this.pincode;
    }

    /**
     * Write the given unsigned16 pincode to the device
     *
     * @param pincode
     * @throws java.io.IOException
     */
    public void writePinCode(Unsigned32 pincode) throws IOException {
        try {
            write(GprsModemSetupAttributes.PIN_CODE.getAttributeNumber(), pincode.getBEREncodedByteArray());
            this.pincode = pincode;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not write the pincode." + e.getMessage());
        }
    }

    /**
     * Write the given pincode to the device
     *
     * @param pincode
     * @throws java.io.IOException
     */
    public void writePinCode(long pincode) throws IOException {
        this.writePinCode(new Unsigned32((int) pincode));
    }

    /**
     * Read the current quality of Service from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public QualityOfService readQualityOfService() throws IOException {
        try {
            this.qualityOfService = QualityOfService.fromStructure(new Structure(getLNResponseData(GprsModemSetupAttributes.QUALITY_OF_SERVICE.getAttributeNumber()), 0, 0));
            return this.qualityOfService;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not retrieve the quality of service." + e.getMessage());
        }
    }

    /**
     * Get the latest retrieved quality of service structure
     *
     * @return
     * @throws java.io.IOException
     */
    public Structure getQualityOfService() throws IOException {
        if (this.qualityOfService == null) {
            readQualityOfService();        // do a dummy read
        }
        return this.qualityOfService;
    }

    /**
     * Return the default QOS structure
     *
     * @return
     * @throws java.io.IOException
     */
    public Structure getTheDefaultQualityOfService() throws IOException {
        if (getQualityOfService().getDataType(QOS_DEFAULT).isStructure()) {
            return (Structure) getQualityOfService().getDataType(QOS_DEFAULT);
        } else {
            throw new ProtocolException("The QOS structure does not contain a default QOS structure ...");
        }
    }

    /**
     * Return the requested QOS structure
     *
     * @return
     * @throws java.io.IOException
     */
    public Structure getRequestedQualityOfService() throws IOException {
        if (getQualityOfService().getDataType(QOS_REQUESTED).isStructure()) {
            return (Structure) getQualityOfService().getDataType(QOS_REQUESTED);
        } else {
            throw new ProtocolException("The QOS structure does not contain a requested QOS structure ...");
        }
    }

    /**
     * Write the given quality of service structure to the device
     *
     * @param qos
     * @throws java.io.IOException
     */
    public void writeQualityOfService(final QualityOfService qos) throws IOException {
        try {
            write(GprsModemSetupAttributes.QUALITY_OF_SERVICE.getAttributeNumber(), qos.getBEREncodedByteArray());
            this.qualityOfService = qos;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NestedIOException(e, "Could not write the pincode." + e.getMessage());
        }
    }

    /**
     * Write the given default and requested qos structures to the device
     *
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

    /**
     * Read Network Selection Mode from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public TypeEnum readNetworkSelectionMode() throws IOException {
        System.out.println("NETWORK_SELECTION_MODE: "+ GprsModemSetupAttributes.NETWORK_SELECTION_MODE.getAttributeNumber());
            this.networkSelectionMode = new TypeEnum(getResponseData(NETWORK_SELECTION_MODE), 0);
            return this.networkSelectionMode;
    }

    /**
     * Read Preferred Operator List from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public Array readPreferredOperatorList() throws IOException {
        System.out.println("NETWORK_SELECTION_MODE: "+ GprsModemSetupAttributes.PREFERRED_OPERATOR_LIST.getAttributeNumber());
        this.preferredOperatorList = new Array(getResponseData(PREFERRED_OPERATOR_LIST), 0, 0);
        return this.preferredOperatorList;
    }

    /**
     * Read Preferred Operator List from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public BooleanObject readIntlRoamingAllowed() throws IOException {
        this.intlRoamingAllowed = new BooleanObject(getResponseData(INTL_ROAMING_ALLOWED), 0);
        return this.intlRoamingAllowed;
    }

    /**
     * Read Minimum RSSI from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned32 readMinimumRssi() throws IOException {
        this.minimumRSSI = new Unsigned32(getResponseData(MINIMUM_RSSI), 0);
        return this.minimumRSSI;
    }

    /**
     * Read Maximum BER from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public Float32 readMaximumBer() throws IOException {
        this.maximumBer = new Float32(getResponseData(MAXIMUM_BER), 0);
        return this.maximumBer;
    }

    /**
     * Read Network Technology from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public Array readNetworkTechnology() throws IOException {
        this.networkTechnology = new Array(getResponseData(NETWORK_TECHNOLOGY), 0, 0);
        return this.networkTechnology;
    }

    /**
     * Read IsGprsPreferred from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public BooleanObject readIsGprsPreferred() throws IOException {
        this.isGprsPreferred = new BooleanObject(getResponseData(IS_GPRS_PREFERRED), 0);
        return this.isGprsPreferred;
    }
}
