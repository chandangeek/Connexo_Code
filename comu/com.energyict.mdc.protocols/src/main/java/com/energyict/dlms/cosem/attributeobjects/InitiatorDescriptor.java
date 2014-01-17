package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class InitiatorDescriptor extends Structure {

	private static final int	SYSTEM_TITLE_ID		= 0;
	private static final int	MAC_ADDRESS_ID		= 1;
	private static final int	LSAP_SELECTOR_ID	= 2;

	/**
	 * @param berEncodeData
	 * @param offset
	 * @param level
	 * @throws java.io.IOException
	 */
	public InitiatorDescriptor(byte[] berEncodeData, int offset, int level) throws IOException {
		super(berEncodeData, offset, level);
	}

	/**
	 * @return
	 */
	public OctetString getSystemTitle() {
		return getDataType(SYSTEM_TITLE_ID).getOctetString();
	}

	/**
	 * @return
	 * @throws java.io.IOException
	 */
	public MacAddress getMacAddress() {
		try {
			return new MacAddress(getDataType(MAC_ADDRESS_ID).getBEREncodedByteArray(), 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @return
	 */
	public Unsigned8 getLSAPSelector() {
		return getDataType(LSAP_SELECTOR_ID).getUnsigned8();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("systemTitle=").append(getSystemTitle() != null ? ProtocolUtils.getResponseData(getSystemTitle().getOctetStr()) : null).append(", ");
		sb.append("macAddress=").append(getMacAddress() != null ? getMacAddress() : null).append(", ");
		sb.append("LSAPSelector=").append(getLSAPSelector() != null ? getLSAPSelector().getValue() : null);
		sb.append("]");
		return sb.toString();
	}

}
