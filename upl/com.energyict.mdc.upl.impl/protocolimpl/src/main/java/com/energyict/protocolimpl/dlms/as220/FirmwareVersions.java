/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author jme
 */
public class FirmwareVersions extends Data {

	private static final int		ATTRB_FW_VERSION	= 0x08;

	public FirmwareVersions(ObisCode obisCode, AS220 as220) throws IOException {
		super(as220.getCosemObjectFactory().getProtocolLink(), as220.getCosemObjectFactory().getObjectReference(obisCode));
	}

	/**
	 * @return
	 */
	public FirmwareVersionAttribute getFirmwareVersion() {
		try {
			return new FirmwareVersionAttribute(getResponseData(ATTRB_FW_VERSION));
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		FirmwareVersionAttribute fw = getFirmwareVersion();
		return (fw != null) ? fw.getVersion() : "Unknown";
	}

	/**
	 * A firmware version attribute is in fact an {@link Array}, containing one
	 * {@link Structure} with the following elements:
	 * <li>FIRMWARE_ID_APP (1 for AM500 and 2 for Vitilec)</li>
	 * <li>FIRMWARE_TYPE (Seems to be always 0x01)</li>
	 * <li>MAJOR_VERSION</li>
	 * <li>MINOR_VERSION</li>
	 * <li>CRC</li>
	 *
	 * @author jme
	 */
	private class FirmwareVersionAttribute extends Array {

		public FirmwareVersionAttribute(byte[] responseData) throws IOException {
			super(responseData, 0, 0);
		}

		public String getId() {
			return ProtocolTools.getHexStringFromBytes(getDataType(0).getOctetString().getContentBytes()).replace("$", "");
		}

		public int getType() {
			return getDataType(1).getTypeEnum().getValue();
		}

		public int getMajorVersion() {
			return getDataType(2).getUnsigned8().getValue();
		}

		public int getMinorVersion() {
			return getDataType(3).getUnsigned8().getValue();
		}

		public long getCRC() {
			return getDataType(4).getUnsigned32().getValue();
		}

		public String getVersion() {
			return getMajorVersion() + "." + getMinorVersion();
		}

		@Override
		public AbstractDataType getDataType(int index) {
			return super.getDataType(0).getStructure().getDataType(index);
		}

		@Override
		public String toString() {
			final String crlf = "\r\n";
			StringBuilder sb = new StringBuilder();
			sb.append("FirmwareVersion:").append(crlf );
			sb.append(" > ID = ").append(getId()).append(crlf );
			sb.append(" > TYPE = ").append(getType()).append(crlf );
			sb.append(" > MAJOR = ").append(getMajorVersion()).append(crlf );
			sb.append(" > MINOR = ").append(getMinorVersion()).append(crlf );
			sb.append(" > CRC = ").append(getCRC()).append(crlf );
			return sb.toString();
		}

	}

}
