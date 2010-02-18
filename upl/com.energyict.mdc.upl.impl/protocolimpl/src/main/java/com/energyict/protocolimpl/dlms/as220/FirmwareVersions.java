/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
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
		StringBuilder sb = new StringBuilder();
		FirmwareVersionAttribute fw = getFirmwareVersion();
		return (fw != null) ? fw.getVersion() : "Unknown";
	}

	private class FirmwareVersionAttribute extends Array {

		/*
		 * FIRMWARE_ID_APP:
		 * 	0x00 0x00 0x00 0x00 0x00 0x01 for meter (AM500 board)
		 *  0x00 0x00 0x00 0x00 0x00 0x02 for vitilec board
		 *
		 * FIRMWARE_TYPE
		 * 	always 0x01
		 * MAJOR_VERSION
		 * MINOR_VERSION
		 * CRC
		 *
		 */

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
