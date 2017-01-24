package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.MacAddressSetupAttributes;

import java.io.IOException;


public class MacAddressSetup extends AbstractCosemObject {

	private OctetString logicalName = null;

	private OctetString macAddress = null;


    /** Constructor for the object with a given ObjectReference(including Logical Name)*/
	public MacAddressSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

    /** @return the classId of the MacAddressSetup object */
	protected int getClassId() {
		return DLMSClassId.MAC_ADDRESS_SETUP.getClassId();
	}

    /** @return the "Mac Address setup" object instance identifier **/
	public OctetString readLogicalName() throws IOException {
        this.logicalName = OctetString.fromByteArray(getResponseData(MacAddressSetupAttributes.LOGICAL_NAME));
		return this.logicalName;
    }

    /** @return the MAC address **/
    public OctetString readMacAddress() throws IOException {
        this.macAddress = OctetString.fromByteArray(getResponseData(MacAddressSetupAttributes.MAC_ADDRESS));
		return this.macAddress;
    }
}
