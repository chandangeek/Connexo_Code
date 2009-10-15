package com.energyict.genericprotocolimpl.edmi.mk10.packets;

/**
 * @author jme
 *
 */
public class CommissioningPacket extends PushPacket {

	private static final int	OFFSET				= 8;
	private static final int	LENGTH_GSM_IMEI		= 16;
	private static final int	LENGTH_METER_ID		= 5;
	private static final int	LENGTH_FW_VERSION	= 6;
	private static final int	LENGTH_FW_EDITION	= 4;

	private String gsmImei;
	private String plantNumber;
	private String deviceConfiguration;
	private String meterId;
	private String firmwareVersion;
	private String firmwareEdition;
	private String gsmCellTowerInfo;
	private String gsmSimCardInfo;

	private int pointer = 0;

	public CommissioningPacket(byte[] packetData) {
		super(packetData);
	}

	protected void doParse() {
		if (checkValidLength()) {
			parsePlantNumber();
			parseGsmImei();
			parseDeviceConfiguration();
			parseMeterId();
			parseFirmwarVersion();
			parseFirmwareEdition();
			parseGsmCellTowerInfo();
			parseGsmSimCardInfo();
		}
	}

	public String getPlantNumber() {
		return plantNumber;
	}

	public String getGsmImei() {
		return gsmImei;
	}

	public String getDeviceConfiguration() {
		return deviceConfiguration;
	}

	public String getMeterId() {
		return meterId;
	}

	public String getFirmwareEdition() {
		return firmwareEdition;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public String getGsmCellTowerInfo() {
		return gsmCellTowerInfo;
	}

	public String getGsmSimCardInfo() {
		return gsmSimCardInfo;
	}

	private void parsePlantNumber() {
		addPointer(OFFSET);
		plantNumber = readString(getPointer());
	}

	private void parseGsmImei() {
		addPointer(getPlantNumber().length() + 1);
		gsmImei = readString(getPointer());
	}

	private void parseDeviceConfiguration() {
		addPointer(LENGTH_GSM_IMEI);
		deviceConfiguration = readString(getPointer());
	}

	private void parseMeterId() {
		addPointer(getDeviceConfiguration().length() + 1);
		meterId = readString(getPointer());
	}

	private void parseFirmwarVersion() {
		addPointer(LENGTH_METER_ID);
		firmwareVersion = readString(getPointer());
	}

	private void parseFirmwareEdition() {
		addPointer(LENGTH_FW_VERSION);
		int fwEdition = readInt(getPointer(), LENGTH_FW_EDITION);
		firmwareEdition = String.valueOf(fwEdition);
	}

	private void parseGsmCellTowerInfo() {
		addPointer(LENGTH_FW_EDITION);
		gsmCellTowerInfo = readString(getPointer());
	}

	private void parseGsmSimCardInfo() {
		addPointer(getGsmCellTowerInfo().length() + 1);
		gsmSimCardInfo = readString(getPointer());
	}

	private boolean checkValidLength() {
		if (getPacketLength() < getMinimumPacketLength()) {
			makeInvalid();
			return false;
		}
		return true;
	}

	private int getMinimumPacketLength() {
		return super.getBasicLength() + LENGTH_FW_EDITION + LENGTH_FW_VERSION + LENGTH_GSM_IMEI + LENGTH_METER_ID + 4;
	}

	private void addPointer(int value) {
		pointer += value;
	}

	private int getPointer() {
		return pointer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\nCommissioningPacket [");
		builder.append(getClass().getName());
		builder.append(super.toString());
		builder.append("\n > deviceConfiguration = ");
		builder.append(deviceConfiguration);
		builder.append("\n > firmwareEdition = ");
		builder.append(firmwareEdition);
		builder.append("\n > firmwareVersion = ");
		builder.append(firmwareVersion);
		builder.append("\n > gsmCellTowerInfo = ");
		builder.append(gsmCellTowerInfo);
		builder.append("\n > gsmImei = ");
		builder.append(gsmImei);
		builder.append("\n > gsmSimCardInfo = ");
		builder.append(gsmSimCardInfo);
		builder.append("\n > meterId = ");
		builder.append(meterId);
		builder.append("\n > plantNumber = ");
		builder.append(plantNumber);
		return builder.toString();
	}



}
