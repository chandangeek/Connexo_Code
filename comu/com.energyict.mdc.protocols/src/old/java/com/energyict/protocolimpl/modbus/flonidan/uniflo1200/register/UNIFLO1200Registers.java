/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * UNIFLO_1200v28_Registers.java
 *
 * Created on 8-dec-2008, 13:37:23 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200Parsers;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author jme
 *
 */
public class UNIFLO1200Registers {
	private static final int MIN_INDEX 				= 0;
	private static final int MAX_INDEX 				= 276;
    private static final int DEBUG					= 0;
	public static final int UNIFLO1200_FW_25 		= 25;
    public static final int UNIFLO1200_FW_28 		= 28;
	public static final int OPTION_LOG_INTERVAL[] 	= {0, 60, 120, 300, 900, 1800, 3600, 7200, 14400};


	public static final String OPTION_GAS_CALC_FORMULA[] = {
		"AGA NX-19 MOD CORR",
		"AGA 8",
		"SGERG 88",
		"Z/Zb",
		"AGA NX-19 MOD HER/WOL"
	};

	private int fwVersion = 0;

	public UNIFLO1200Registers(int uniflo1200_fw_version) throws IOException {
		switch (uniflo1200_fw_version) {
			case UNIFLO1200_FW_25: break;
			case UNIFLO1200_FW_28: break;
			default: throw new IOException("Unknown firmwareversion: " + uniflo1200_fw_version);
		}
		this.fwVersion  = uniflo1200_fw_version;
	}

	public String getUnitString(int addressIndex) throws IOException {
		if ((addressIndex > MAX_INDEX) || (addressIndex < MIN_INDEX))
			throw new IOException("getUnitString() addressIndex wrong value: " + addressIndex + ". Valid value: " + MIN_INDEX + " to " + MAX_INDEX);

		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.UNITS[addressIndex];
			case UNIFLO1200_FW_28: return V28.UNITS[addressIndex];
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	public int getIntervalLogStartAddress() throws IOException {
		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.INTERVAL_LOG_STARTADDRESS;
			case UNIFLO1200_FW_28: return V28.INTERVAL_LOG_STARTADDRESS;
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	public int getEventLogStartAddress() throws IOException {
		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.EVENT_LOG_STARTADDRESS;
			case UNIFLO1200_FW_28: return V28.EVENT_LOG_STARTADDRESS;
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	public int getDailyLogStartAddress() throws IOException {
		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.DAILY_LOG_STARTADDRESS;
			case UNIFLO1200_FW_28: return V28.DAILY_LOG_STARTADDRESS;
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	public int getMonthLogStartAddress() throws IOException {
		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.MONTH_LOG_STARTADDRESS;
			case UNIFLO1200_FW_28: return V28.MONTH_LOG_STARTADDRESS;
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	public int getDataType(int addressIndex) throws IOException {
		int absoluteAddress = getAbsAddr(addressIndex);
		return (absoluteAddress & 0x00F00000) >> 20;
	}

	public int getDecimals(int addressIndex) throws IOException {
		int absoluteAddress = getAbsAddr(addressIndex);
		return (absoluteAddress & 0x70000000)>>28;
	}

	public int getSlaveID(int addressIndex) throws IOException {
		int absoluteAddress = getAbsAddr(addressIndex);
		int returnValue = (absoluteAddress & 0x000F0000)>>16;
		if (returnValue > 8)
			throw new ProtocolException(
					"Invalid slaveId: " + returnValue +
					" addressIndex: " + addressIndex +
					" absoluteAddress: 0x" + ProtocolUtils.buildStringHex(absoluteAddress, 8)
					);
		return returnValue;
	}

	public String getAddressName(int addressIndex) throws IOException {
		if ((addressIndex > MAX_INDEX) || (addressIndex < MIN_INDEX))
			throw new IOException("getAddressName() addressIndex wrong value: " + addressIndex + ". Valid value: " + MIN_INDEX + " to " + MAX_INDEX);

		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.REGISTER_NAME[addressIndex];
			case UNIFLO1200_FW_28: return V28.REGISTER_NAME[addressIndex];
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}


	public String getParser(int addressIndex) throws IOException {
		int dataType = getDataType(addressIndex);
		switch (dataType) {
			case 0x00: return UNIFLO1200Parsers.PARSER_UINT8;
			case 0x01: return UNIFLO1200Parsers.PARSER_UINT16;
			case 0x02: return UNIFLO1200Parsers.PARSER_UINT32;
			case 0x03: return UNIFLO1200Parsers.PARSER_REAL32;
			case 0x04: return UNIFLO1200Parsers.PARSER_INTREAL;
			case 0x05: return UNIFLO1200Parsers.PARSER_TIME;
			case 0x06: return UNIFLO1200Parsers.PARSER_STR22;
			case 0x07: return UNIFLO1200Parsers.PARSER_OPTION;
			case 0x08: return UNIFLO1200Parsers.PARSER_LOC_PTR;
			case 0x09: return UNIFLO1200Parsers.PARSER_STR29;
			case 0x0A: return UNIFLO1200Parsers.PARSER_STR1;
			case 0x0B: return UNIFLO1200Parsers.PARSER_UINT160;
			case 0x0C: return UNIFLO1200Parsers.PARSER_UINT320;
			case 0x0D: return UNIFLO1200Parsers.PARSER_REAL320;
			case 0x0E: return UNIFLO1200Parsers.PARSER_STR8;
			case 0x0F: return UNIFLO1200Parsers.PARSER_DATABLOCK;
			default: throw new IOException("Unknown datatype: " + dataType + " for firmware version " + fwVersion);
		}
	}

	public int getDataLength(int addressIndex) throws IOException {
		int dataType = getDataType(addressIndex);
		switch (dataType) {
			case 0x00: return UNIFLO1200Parsers.LENGTH_UINT8;
			case 0x01: return UNIFLO1200Parsers.LENGTH_UINT16;
			case 0x02: return UNIFLO1200Parsers.LENGTH_UINT32;
			case 0x03: return UNIFLO1200Parsers.LENGTH_REAL32;
			case 0x04: return UNIFLO1200Parsers.LENGTH_INTREAL;
			case 0x05: return UNIFLO1200Parsers.LENGTH_TIME;
			case 0x06: return UNIFLO1200Parsers.LENGTH_STR22;
			case 0x07: return UNIFLO1200Parsers.LENGTH_OPTION;
			case 0x08: return UNIFLO1200Parsers.LENGTH_LOC_PTR;
			case 0x09: return UNIFLO1200Parsers.LENGTH_STR29;
			case 0x0A: return UNIFLO1200Parsers.LENGTH_STR1;
			case 0x0B: return UNIFLO1200Parsers.LENGTH_UINT160;
			case 0x0C: return UNIFLO1200Parsers.LENGTH_UINT320;
			case 0x0D: return UNIFLO1200Parsers.LENGTH_REAL320;
			case 0x0E: return UNIFLO1200Parsers.LENGTH_STR8;
			case 0x0F:
				try {
					return Integer.parseInt(getUnitString(addressIndex));
				} catch (NumberFormatException e) {
					return 0;
				}
			default: throw new IOException("Unknown datatype length for dataType: " + dataType + " for firmware version " + fwVersion);
		}
	}

	public int getWordAddr(int addressIndex) throws IOException {
		int absoluteAddress = getAbsAddr(addressIndex) & 0x0000FFFF;
		return absoluteAddress / 0x02;
	}

	public boolean isOddAddr(int addressIndex) throws IOException {
		int absoluteAddress = getAbsAddr(addressIndex) & 0x0000FFFF;
		return ((absoluteAddress % 2) != 0);
	}

	public int getAbsAddr(int addressIndex) throws IOException {
		if ((addressIndex > MAX_INDEX) || (addressIndex < MIN_INDEX))
			throw new IOException("getWordAddr() addressIndex wrong value: " + addressIndex + ". Valid value: " + MIN_INDEX + " to " + MAX_INDEX);

		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.ABSOLUTE_ADDRESSES[addressIndex];
			case UNIFLO1200_FW_28: return V28.ABSOLUTE_ADDRESSES[addressIndex];
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	/**
	 * Checks if a given register is a cumulative register
	 * @param addressIndex - the number(index) of the register
	 * @return true if cumulative, false otherwise
	 * @throws IOException if addressIndex is out of range or firmwareVersion is unknown
	 */
	public boolean isCumulative(int addressIndex) throws IOException {
		if ((addressIndex > MAX_INDEX) || (addressIndex < MIN_INDEX))
			throw new IOException("isCumulative() addressIndex wrong value: " + addressIndex + ". Valid value: " + MIN_INDEX + " to " + MAX_INDEX);

		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.isCumulative(addressIndex);
			case UNIFLO1200_FW_28: return V28.isCumulative(addressIndex);
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	/**
	 * Returns the cumulative wrapValue
	 * @param addressIndex - the number(index) of the register
	 * @return the wrapValue
	 * @throws IOException if addressIndex is out of range or firmwareVersion is unknown
	 */
	public int getCumulativeWrapValue(int addressIndex) throws IOException{
		if ((addressIndex > MAX_INDEX) || (addressIndex < MIN_INDEX))
			throw new IOException("getCumulativeWrapValue() addressIndex wrong value: " + addressIndex + ". Valid value: " + MIN_INDEX + " to " + MAX_INDEX);

		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return V25.getCumulativeWrapValue(addressIndex);
			case UNIFLO1200_FW_28: return V28.getCumulativeWrapValue(addressIndex);
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	public static class V25 {
		private static final int ABSOLUTE_ADDRESSES[] 		= {0x00000000};
		private static final String UNITS[] 				= {""};
		private static final String[] REGISTER_NAME 		= {""};

		public static final int INTERVAL_LOG_STARTADDRESS 	= 0;
		public static final int DAILY_LOG_STARTADDRESS 		= 0;
		public static final int MONTH_LOG_STARTADDRESS 		= 0;
		public static final int EVENT_LOG_STARTADDRESS		= 0;

		public static boolean isCumulative(int addressIndex){ return false;}
		public static int getCumulativeWrapValue(int addressIndex){ return 99999999;}	// unnecessary because it's always false
	}

	public static class V28 {

		public static final int INTERVAL_LOG_STARTADDRESS 	= 0x0FFE0;
		public static final int DAILY_LOG_STARTADDRESS 		= 0x04000;
		public static final int MONTH_LOG_STARTADDRESS 		= 0x07000;
		public static final int EVENT_LOG_STARTADDRESS		= 0x011A0;

		public static final int ZA 						= 0;
		public static final int SLAVE_ADDRESS			= 1;
		public static final int TIME 					= 2;
		public static final int ZB	 					= 3;
		public static final int TEMP_CORR_TABEL 		= 4;
		public static final int BATTERY_REMAINING		= 5;
		public static final int INT_LOG_POWER_AVG		= 6;
		public static final int INT_LOG_TEMP_AVG		= 7;
		public static final int INT_LOG_PRESS_AVG		= 8;
		public static final int INT_LOG_CORR_FLOW_AVG	= 9;
		public static final int INT_LOG_CONV_FLOW_AVG	= 10;
		public static final int DAILY_LOG_POWER_AVG		= 11;
		public static final int DAILY_LOG_TEMP_AVG		= 12;
		public static final int DAILY_LOG_PRESS_AVG		= 13;
		public static final int DAILY_LOG_CORR_FLOW_AVG	= 14;
		public static final int DAILY_LOG_CONV_FLOW_AVG	= 15;
		public static final int CONFIG_CRC				= 16;
		public static final int LOG_LINE_CRC			= 17;
		public static final int PULSE_OUT_REG1			= 18;
		public static final int PULSE_OUT_REG2			= 19;
		public static final int PULSE_OUT_DIV_FACTOR1	= 20;
		public static final int PULSE_OUT_DIV_FACTOR2	= 21;
		public static final int NR_POWERUPS				= 22;
		public static final int PRESS_AD_COUNT			= 23;
		public static final int TEMP_AD_COUNT			= 24;
		public static final int PSENS_TEMP_AD_COUNT		= 25;
		public static final int PULSEOUT_PERIOD_TIME	= 26;
		public static final int HEAT_VALUE				= 27;
		public static final int DISPLAY_SETUP_TABLE		= 28;
		public static final int OPERATOR_ID				= 29;
		public static final int EVENT_LOG				= 30;
		public static final int ACTUAL_SECLEVEL			= 31;
		public static final int VOLUME_CONTROL			= 32;
		public static final int SYSTEM_DATA				= 33;
		public static final int PULSE_CHECK_EVERY		= 34;
		public static final int ALARM_LOG				= 35;
		public static final int PRESSURE				= 36;
		public static final int TEMPERATURE				= 37;
		public static final int CONVERSION_FACTOR		= 38;
		public static final int PULSE_VALUE				= 39;
		public static final int FLOW_CORRECTED			= 40;
		public static final int FLOW_CONVERTED			= 41;
		public static final int VOLUME_CORRECTED		= 42;
		public static final int VOLUME_CONVERTED		= 43;
		public static final int VOLUME_CORRECTED_I		= 44;
		public static final int VOLUME_CORRECTED_D		= 45;
		public static final int VOLUME_CONVERTED_I		= 46;
		public static final int VOLUME_CONVERTED_D		= 47;
		public static final int ALARM_SETUP_TABLE		= 48;
		public static final int FLOWSTOP_AFTER			= 49;
		public static final int PRESS_LOW_LIMIT			= 50;
		public static final int PRESS_HIGH_LIMIT		= 51;
		public static final int TEMP_LOW_LIMIT			= 52;
		public static final int TEMP_HIGH_LIMIT			= 53;
		public static final int FLOW_CORR_LOW_LIMIT		= 54;
		public static final int FLOW_CORR_HIGH_LIMIT	= 55;
		public static final int POWER_HIGH_LIMIT		= 56;
		public static final int FALLBACK_PRESS			= 57;
		public static final int FALLBACK_TEMP			= 58;
		public static final int TURN_OFF_DIAPLAY_AFTER	= 59;
		public static final int BASE_PRESS				= 60;
		public static final int BASE_TEMP				= 61;
		public static final int MAX_PRESS				= 62;
		public static final int MIN_PRESS				= 63;
		public static final int MAX_TEMP				= 64;
		public static final int MIN_TEMP				= 65;
		public static final int ALARM_STATUS_TABLE		= 66;
		public static final int ALARM_COUNT_TABLE		= 67;
		public static final int POWER					= 68;
		public static final int SET_ALARM_REG			= 69;
		public static final int PRESS_SENSOR			= 70;
		public static final int PRESS_SENS_STYLE		= 71;
		public static final int PRESS_SENS_RANGE		= 72;
		public static final int PRESS_SERIAL			= 73;
		public static final int PRESS_DAYOF_CALIB		= 74;
		public static final int PRESS_MONTHOF_CALIB		= 75;
		public static final int PRESS_YEAROF_CALIB		= 76;
		public static final int PRESS_SIGN_AT_1BAR		= 77;
		public static final int PRESS_SIGN_AT_FS		= 78;
		public static final int PRESS_TEMP_SIGN			= 79;
		public static final int RESET_ALARM				= 80;
		public static final int CLEAR_ALARM				= 81;
		public static final int AD_AMPLIFY				= 82;
		public static final int CLEAR_CONFIG_LOG		= 83;
		public static final int LOCK_GAS_TABLE			= 84;
		public static final int UNLOCK_GAS_TABLE		= 85;
		public static final int UPDATE_CORR_TABLE		= 86;
		public static final int UPDATE_GAS_DATA			= 87;
		public static final int MAKE_SNAPSHOT			= 88;
		public static final int PULSE_OUT1_ENABLE		= 89;
		public static final int PULSE_OUT2_ENABLE		= 90;
		public static final int MENU_FILE				= 91;
		public static final int INTERVAL_LOG_EEPROM		= 92;
		public static final int GRAPH_DISP_GAIN			= 93;
		public static final int GRAPH_DISP_CONTRAST		= 94;
		public static final int ENERGY					= 95;
		public static final int ENERGY_I				= 96;
		public static final int ENERGY_D				= 97;
		public static final int OPTION_CARD_CHANGED		= 98;
		public static final int TEMP_SENS_SOURCE		= 99;
		public static final int PRESS_SENS_SOURCE		= 100;
		public static final int ALARM_ACTIVE_1_32		= 101;
		public static final int ALARM_ACTIVE_33_64		= 102;
		public static final int ALARM_ACTIVE_65_96		= 103;
		public static final int ALARM_REG_1_32			= 104;
		public static final int ALARM_REG_33_64			= 105;
		public static final int ALARM_REG_65_96			= 106;
		public static final int IO_TABLE				= 107;
		public static final int VOLUME_MEASURED			= 108;
		public static final int PRESS_SENS_CRC			= 109;
		public static final int INTERVAL_LOG_REG		= 110;
		public static final int DAILY_LOG_REG			= 111;
		public static final int SNAPSHOT_LOG_REG		= 112;
		public static final int ALARM_TRIGGERED_LOG_REG	= 113;
		public static final int FW_VERSION_TYPE			= 114;
		public static final int INSTALLATION_NR			= 115;
		public static final int METER_NR				= 116;
		public static final int METER_SIZE				= 117;
		public static final int COSTUMER				= 118;
		public static final int DATE_OF_INSTALLATION	= 119;
		public static final int METER_INDEX				= 120;
		public static final int PROJECT_NO				= 121;
		public static final int SERIAL_NUMBER			= 122;
		public static final int FLOW_SENSOR				= 123;
		public static final int TEMP_SENSOR				= 124;
		public static final int INTERVAL_LOG_SIZE		= 125;
		public static final int DAILY_LOG_SIZE			= 126;
		public static final int SNAPSHOT_LOG_SIZE		= 127;
		public static final int ALARM_TRIG_LOG_SIZE		= 128;
		public static final int INTERVAL_LOG_WIDTH		= 129;
		public static final int DAILY_LOG_WIDTH			= 130;
		public static final int SNAPSHOT_LOG_WIDTH		= 131;
		public static final int ALARM_TRIG_LOG_WIDTH	= 132;
		public static final int FLOW_MEASURED			= 133;
		public static final int METHANE					= 134;
		public static final int NITROGEN				= 135;
		public static final int CO2						= 136;
		public static final int ETHANE					= 137;
		public static final int PROPANE					= 138;
		public static final int WATER					= 139;
		public static final int HYDRG_SUL				= 140;
		public static final int HYDROGEN				= 141;
		public static final int CARBON_MONOXIDE			= 142;
		public static final int OXYGEN					= 143;
		public static final int I_BUTANE				= 144;
		public static final int N_BUTANE				= 145;
		public static final int I_PETANE				= 146;
		public static final int N_PETANE				= 147;
		public static final int N_HEXANE				= 148;
		public static final int N_HEPTANE				= 149;
		public static final int N_OCTANE				= 150;
		public static final int N_NOCTANE				= 151;
		public static final int N_DECANE				= 152;
		public static final int HELIUM					= 153;
		public static final int ARGON					= 154;
		public static final int GAS_CALC_FORMULA		= 155;
		public static final int GAS_COMPOSITION			= 156;
		public static final int GAS_COMP_REV_TIME		= 157;
		public static final int DENSITY					= 158;
		public static final int GAS_CONV_TABLE			= 159;
		public static final int PASSWORD_LVL_3			= 160;
		public static final int PASSWORD_LVL_2			= 161;
		public static final int PASSWORD_LVL_1			= 162;
		public static final int LOG_REORGANIZE_BITS		= 163;
		public static final int CORRECTION_FACTOR		= 164;
		public static final int OPTION_BOARD_SW_VERSION	= 165;
		public static final int OPTION_BOARD_SERIAL		= 166;
		public static final int HF_CARD_TYPE			= 167;
		public static final int SN_TABLE				= 168;
		public static final int TEMPERATURE_CODE		= 169;
		public static final int INTERVAL_LOG_TEMP_MIN	= 170;
		public static final int INTERVAL_LOG_TEMP_MAX	= 171;
		public static final int INTERVAL_LOG_PRESS_MIN	= 172;
		public static final int INTERVAL_LOG_PRESS_MAX	= 173;
		public static final int INTER_LOG_FLOW_CORR_MIN	= 174;
		public static final int INTER_LOG_FLOW_CORR_MAX	= 175;
		public static final int INTER_LOG_FLOW_CONV_MIN	= 176;
		public static final int INTER_LOG_FLOW_CONV_MAX	= 177;
		public static final int DAILY_LOG_TEMP_MIN		= 178;
		public static final int DAILY_LOG_TEMP_MAX		= 179;
		public static final int DAILY_LOG_PRESS_MIN		= 180;
		public static final int DAILY_LOG_PRESS_MAX		= 181;
		public static final int DAILY_LOG_FLOW_CORR_MIN	= 182;
		public static final int DAILY_LOG_FLOW_CORR_MAX	= 183;
		public static final int DAILY_LOG_FLOW_CONV_MIN	= 184;
		public static final int DAILY_LOG_FLOW_CONV_MAX	= 185;
		public static final int INTERVAL_LOG_POWER_MIN	= 186;
		public static final int INTERVAL_LOG_POWER_MAX	= 187;
		public static final int DAILY_LOG_POWER_MIN		= 188;
		public static final int DAILY_LOG_POWER_MAX		= 189;
		public static final int VOL_MEASURED_I			= 190;
		public static final int VOL_MEASURED_D			= 191;
		public static final int VOLUME_AT_ERROR_VAL		= 192;
		public static final int VOLUME_AT_ERROR_I		= 193;
		public static final int VOLUME_AT_ERROR_D		= 194;
		public static final int SUPERIOR_HEAT_VALUE		= 195;
		public static final int C6PLUS_VALUE			= 196;
		public static final int C6_PLUS_ENABLED			= 197;
		public static final int AD_PRESS_OFFSET			= 198;
		public static final int AD_PRESS_TEMP_OFFSET	= 199;
		public static final int VOLUME_AT_ERROR			= 200;
		public static final int TZ_MODE					= 201;
		public static final int TZ_PRESSURE				= 202;
		public static final int MENU_AND_ALARM_TXT		= 203;
		public static final int NUMBER_OF_SLAVE_UNITS	= 204;
		public static final int UPDATE_EEPROM			= 205;
		public static final int NAME_OF_ALARM_FILE		= 206;
		public static final int NOT_USED				= 207;
		public static final int LOCK_GRAPH_DISPLAY		= 208;
		public static final int UNLOCK_GRAPH_DISPLAY	= 209;
		public static final int VOLUME_CONTROL_I		= 210;
		public static final int VOLUME_CONTROL_D		= 211;
		public static final int CONSUMP_ACTUAL_HOUR_I	= 212;
		public static final int CONSUMP_ACTUAL_HOUR_D	= 213;
		public static final int MAX_HOUR_CONSUMP_TIME	= 214;
		public static final int MAX_HOUR_CONSUMP_I		= 215;
		public static final int MAX_HOUR_CONSUMP_D		= 216;
		public static final int MAX2_HOUR_CONSUMP_TIME	= 217;
		public static final int MAX2_HOUR_CONSUMP_I		= 218;
		public static final int MAX2_HOUR_CONSUMP_D		= 219;
		public static final int MAX3_HOUR_CONSUMP_TIME	= 220;
		public static final int MAX3_HOUR_CONSUMP_I		= 221;
		public static final int MAX3_HOUR_CONSUMP_D		= 222;
		public static final int PRESS_CALIB_OPERATOR	= 223;
		public static final int TEMP_CALIB_OPERATOR		= 224;
		public static final int EVENT_LOG_INDEX			= 225;
		public static final int ALARM_LOG_INDEX			= 226;
		public static final int LOG_INTERVAL			= 227;
		public static final int INTERVAL_OF_MEASUREMENT	= 228;
		public static final int DAILY_LOG_TIME_HOUR		= 229;
		public static final int DAILY_LOG_TIME_MIN		= 230;
		public static final int INTERVAL_LOG_INDEX		= 231;
		public static final int DAILY_LOG_INDEX			= 232;
		public static final int SNAPSHOT_LOG_INDEX		= 233;
		public static final int ALARM_TRIG_LOG_INDEX	= 234;
		public static final int DISPLAY_TIME			= 235;
		public static final int MAX_PULSE_ERROR			= 236;
		public static final int PRESS_SENS_CHANGED		= 237;
		public static final int TEMP_SENS_CHANGED		= 238;
		public static final int MONTH_LOG_WIDTH			= 239;
		public static final int MONTH_LOG_SIZE			= 240;
		public static final int MONTH_LOG_REG			= 241;
		public static final int MONTH_LOG_INDEX			= 242;
		public static final int DISPLAY_TEST			= 243;
		public static final int FW_ADDR_RANGE_OK		= 244;
		public static final int FW_UPDATE_START			= 245;
		public static final int FW_UPDATE_END			= 246;
		public static final int FW_UPDATE_CANCEL		= 247;
		public static final int PROGRAM_CRC				= 248;
		public static final int TEMP_CALIB_TABLE		= 249;
		public static final int PRESS_CALIB_TABLE		= 250;
		public static final int FORCE_MEASUREMENT		= 251;
		public static final int PRESS_CALIB_TIME		= 252;
		public static final int TEMP_CALIB_TIME			= 253;
		public static final int CONVERSION_TABLE_CRC	= 254;
		public static final int CONV_TABLE_DLL_VERSION	= 255;

		public static final int WR_RESET_ALARM			= 256;
		public static final int WR_CLEAR_ALARM			= 257;
		public static final int WR_CLEAR_CONFIG_LOG		= 258;
		public static final int WR_LOCK_GAS_TABLE		= 259;
		public static final int WR_UNLOCK_GAS_TABLE		= 260;
		public static final int WR_UPDATE_CORR_TABLE	= 261;
		public static final int WR_UPDATE_GAS_DATA		= 262;
		public static final int WR_MAKE_SNAPSHOT		= 263;
		public static final int WR_UPDATE_EEPROM		= 264;
		public static final int WR_LOCK_GRAPH_DISP		= 265;
		public static final int WR_UNLOCK_GRAPH_DISP	= 266;
		public static final int WR_DISPLAY_TEST			= 267;
		public static final int WR_FW_UPDATE_START		= 268;
		public static final int WR_FW_UPDATE_END		= 269;
		public static final int WR_FW_UPDATE_CANCEL		= 270;
		public static final int WR_FORCE_MEASUREMENT	= 271;
		public static final int WR_PRESS_SENS_CHANGE 	= 272;
		public static final int WR_TEMP_SENS_CHANGE 	= 273;
		public static final int WR_SET_PASS				= 274;
		public static final int WR_SET_OPERATOR_ID		= 275;
		public static final int WR_ADJUST_TIME			= 276;

		private static final String REGISTER_NAME[] = {
			"Za",      								// 0    Za                              R       1R   Za                                                                          Za
			"Slave address",      					// 1    SlaveAdr                                1    Modbus slave address                                                        Modbus address
			"Time",     							// 2    Time                            T       1    Uniflo time                                                                 Time
			"Zb",      								// 3    Zb2                             R       1R   Zb                                                                          Zb
			"Temperature Correction table",   		// 4    TempCorrTabel        112                     Temperatur correction tabel
			"Battery remaining",     				// 5    Bat                  Days      U        1    Battery remaining                                                           Bat. remaining
			"Interval log power average",    		// 6    IEnergyFAvg          MJ        U0        R1  Average power.
			"Interval log temperature average",		// 7    ITempAvg             °C         0        R1  Average temp.
			"Interval log pressure average",   		// 8    ITrykAvg             BarA      U0        R1  Average Press.
			"Interval log corrected flow average",  // 9    IFlowUAvg            m3/h       0        R1  Avg. flow corr.
			"Interval log converted flow average", 	// 10   IFlowKAvg            Nm3/h     U0        R1  Avg. flow conv.
			"Daily log power average",    			// 11   DEnergyFAvg          MJ        U0        R2  Average power.
			"Daily log temperature average",    	// 12   DTempAvg             °C         0        R2  Average temp.
			"Daily log pressure average",   		// 13   DTrykAvg             BarA      U0        R2  Average Press.
			"Daily log corrected flow average",  	// 14   DFlowUAvg            m3/h       0        R2  Avg. flow corr.
			"Daily log converted flow average", 	// 15   DFlowKAvg            Nm3/h     U0        R2  Avg. flow conv.
			"Configuration checksum",      			// 16   ConfigChecksum                          1R   Configuration checksum                                                      Config. checksum
			"Log line checksum",      				// 17   LogChecksum                              R0  Log line checksum
			"Pulse out register 1",      			// 18   PulsoutReg1                     R            Pulse output 1
			"Pulse out register 2",      			// 19   PulsoutReg2                     R            Pulse output 2
			"Pulse out diversion factor 1",      	// 20   PulsDiv1                        R            Division faktor for puls 1
			"Pulse out diversion factor 2",      	// 21   PulsDiv2                        R            Division faktor for puls 2
			"Numbers of powerups",      			// 22   Powerup                                      Numbers of  powerup
			"Pressure AD-count",      				// 23   ADtryk                                   R3  Pressure AD-count
			"Temperature AD-count",      			// 24   ADtemp                                   R3  Temperature AD-count
			"Pressure sensortemperature AD-Count",	// 25   ADSensortemp                             R3  Pressure sensor A-count
			"Pulse output period cycle time", 		// 26   PulsOutTid                      R            Pulse output period length
			"Heat value",							// 27   BassisEnergi         MJ/nm3    UR       1    Heat value                                                                  Heat value
			"Display setup table",   				// 28   DisplaySetupTable    18         R            Display setup table
			"Operator ID",    						// 29   Operator                                     Operator ID 4 char
			"Event log",      						// 30   EventLog                                     Eventlog (200 logninger)
			"Actual security level",      			// 31   Password                                 R   Actual secure level
			"Volume control",    					// 32   VolCtrl              m3                      Vol. control                                        0           99999999
			"System data",      					// 33   SystemData                                   SystemData                                                                  Temp type
			"Pulse check every",      				// 34   PulsCheckPulser      pulses    UR            Pulse check every
			"Alarm log",     						// 35   AlarmLog                                     Alarmlog (100 logninger)
			"Pressure",   							// 36   Pressure             bar A     U        1R0 1Pressure                                                                    Pressure
			"Temperature",    						// 37   Temperature          °C                 1R0 1Temperature                                                                 Temperature
			"Conversion factor",      				// 38   Korr                                    1R0 1Conversion factor                                                           Conv. factor
			"Pulse value",    						// 39   Pulsvalue            m3/pulse  UR       1    Value of pulse                                      0           99999999    Pulse Value
			"Flow corrected",  						// 40   FlowCorr             m3/h               1R0 1Flow corrected                                                              Flow corr.
			"Flow converted", 						// 41   FlowConv             Nm3/h     U        1R0 1Flow conv.                                                                  Flow conv.
			"Volume corrected",    					// 42   VolCorr              m3                      Vol. corrected                                      0           99999999
			"Volume converted",   					// 43   VolConv              Nm3       U             Vol. converted                                      0           99999999
			"Volume corrected Integer part",    	// 44   VolCorrI             m3                 1 01 Vol. corrected                                                              Vol. corr.
			"Volume corrected decimal part",    	// 45   VolCorrF             m3                 1 0  Vol. corr. dec.                                                             Vol. corr. dec
			"Volume converted Integer part",   		// 46   VolConvI             Nm3       U        1 01 Vol. conv.                                                                  Vol. conv.
			"Volume converted decimal part",  		// 47   VolConvF             Nm3       U        1 0  Vol. conv. dec.                                                             Vol. conv. dec
			"Alarm setup table",   					// 48   AlarmSetupTable      192        R            Alarm setup table
			"Flowstop after",     					// 49   Flowstop             seconds   UR            Flowstop after                                      10          600
			"Pressure low limit",   				// 50   TrykMin              BarA      UR       1    Pressure low limit                                  0.6         80          Press. low limit
			"Pressure high limit",   				// 51   TrykMax              BarA      UR       1    Pressure high limit                                 0.6         80          Press. high limit
			"Temperature low limit",    			// 52   TempMin              °C         R       1    Temperature low limit                               -40         70          Temp. low limit
			"Temperature high limit",    			// 53   TempMax              °C         R       1    Temperature high limit                              -40         70          Temp. high limit
			"Flow corrected high limit",  			// 54   FlowCorrMax          m3/h       R       1    Flow high limit                                     0           99999999    Flow high limit
			"Flow converted high limit", 			// 55   FlowConvMax          Nm3/h     UR       1    Conv. flow high limit                               0           99999999    Conv. flow high limit
			"Power high limit",  					// 56   EnergiMax            MJ/h      UR       1    Power high limit                                    0           99999999    Power high limit
			"Fallback pressure",  					// 57   TrykVFejl            BarA      UR            Fallback press. used on error                       0.6         80
			"Fallback temperature",    				// 58   TempVFejl            °C         R            Fallback temp. used on error                        -40         70
			"Turn off display after",     			// 59   DispOffTime          Sec.      UR            Turn off display after                              4           240
			"Base pressure",  					 	// 60   Pb                   BarA      UR       1 1  Base pressure                                       0.6         80          Base press.
			"Base temperature",    					// 61   Tb                   °C         R       1 1  Base temperature                                    -40         70          Base temp.
			"Max pressure",   						// 62   MaxPress             BarA      UR            Pressure range                                      0.6         80
			"Min pressure",   						// 63   MinPress             BarA      UR            Pressure range                                      0.6         80
			"Max temperature",    					// 64   MaxTemp              °C         R            Temperature range                                   -40         70
			"Min temperature",    					// 65   MinTemp              °C         R            Temperature range                                   -40         70
			"Alarm status table",    				// 66   AlarmTable           12                      Alarm table
			"Alarm count table",    				// 67   AlarmCntTable        96         0            Alarm cnt table
			"Power",  								// 68   EnergiFlow           MJ/h      U        1R0 1Power                                                                       Power
			"Set alarm reg.",      					// 69   Alarmset                                     Set alarm reg in uniflo
			"Pressure Manufacture and type",		// 70   TDeviceID                                R   Manufacture and type                                                        P. Sens. type
			"Pressure sensor style",     			// 71   TPStyle                                  R   Tryksensor Pressure style
			"Pressure sensor range",      			// 72   TPressRange                             1R   Range [bar]                                                                 Range [bar]
			"Pressure serial number",      			// 73   TSerialno                               1R   Serial no.                                                                  P. sens no.
			"Pressure day of calibration",      	// 74   TCalibDay                                R   Date of calibration                                2
			"Pressure month of calibration",      	// 75   TCalibMonth                              R   Date of calibration                                2
			"Pressure day of calibration",      	// 76   TCalibYear                               R   Date of calibration                                2
			"Pressure signal at 1 bar",      		// 77   TPres1Bar                                R   Tryksensor Pressure signal at 1 bar at 20 deg. C
			"Pressure signal at FS",      			// 78   TPresFS                                  R   Tryksensor Pressure signal FS at 20 deg. C
			"Pressure temperaturesignal",      		// 79   TTemp                                    R   Tryksensor temperature signal at 20 deg. C
			"Reset Alarm",      					// 80   ResetAlarm                                   Reset alarm
			"Clear Alarm",      					// 81   ClrAlarm                                     Clear alarm
			"AD amplify",      						// 82   ADAmp                                        AD amplify
			"Clear configuration log",      		// 83   ClrEvent                                     Delete configuration log
			"Lock gas table",      					// 84   LockTable                                    Lock correction table
			"Unlock gas table",      				// 85   UnLockTable                                  UnLock correction table
			"Update correction table",      		// 86   OpdateCorrTable                              Update correction table
			"Update Gas Data",      				// 87   OpdateGasData                                Update Gas data
			"Make Snapshot",      					// 88   Snapshot                                     Make a snapshot in Uniflo
			"Pulse out 1 enable",      				// 89   PulsOut1En                      R            Puls out 1 enable
			"Pulse out 2 enable",      				// 90   PulsOut2En                      R            Puls out 2 enable
			"Menu File",      						// 91   Menufile                        R            Menu file
			"Interval log EEPROM",      			// 92   IntvAntal                                R   x
			"Graphic display gain",      			// 93   GSetup                                       Graphic display gain setup
			"Graphic display contrast",      		// 94   CSetup                                       Graphic display contrast setup
			"Energy",    							// 95   Energi               MJ        U             Energy                                              0           99999999
			"Energy integer part",    				// 96   EnergiI              MJ        U        1 01 Energy                                                                      Energy
			"Energy decimal part",    				// 97   EnergiF              MJ        U        1 0  Energy dec.                                                                 Energy dec.
			"Option Card changed",      			// 98   OptionCH                                     Options kort changed
			"Temperature sensorsource",      		// 99   TempSource                      R            Temperatur sensor sourse
			"Pressure sensor source",      			// 100  PresSource                      R            Pressure sensor source
			"Alarm active 1-32",      				// 101  Alarm active 1-32                        R0  Alarm active 1-32
			"Alarm active 33-64",      				// 102  Alarm active 33-64                       R0  Alarm active 33-64
			"Alarm active 64-96",      				// 103  Alarm active 64-96                       R0  Alarm active 64-96
			"Alarm reg. 1-32",      				// 104  Alarm reg. 1-32                          R0  Alarm reg. 1-32
			"Alarm reg. 33-64",      				// 105  Alarm reg. 33-64                         R0  Alarm reg. 33-64
			"Alarm reg. 64-96",      				// 106  Alarm reg. 64-96                         R0  Alarm reg. 64-96
			"I/O table",    						// 107  IOTabel              10                  R   I/O - tabel
			"Volume measured",    					// 108  VolMeasured          m3                      Volume measured                                     0           99999999
			"Pressure sensor checksum",      		// 109  TChecksum                                R   Pressure sensor check sum
			"Interval log register",    			// 110  IntvalLogreg         20         R            Interval log registre
			"24 hour log register",    				// 111  DagsLogreg           20         R            24 hour log registre
			"Snap shot log register",    			// 112  Snapshotreg          20         R            Snapshot log registre
			"Alarm triggered log register",    		// 113  AlarmAktLogreg       20         R            Alarm triggered log registre
			"SW Version and type",      			// 114  VerTyp                                  1R   Uniflo type and version                                                     Type/version
			"Installation number",      			// 115  Installation                    R       1    Installation no.                                                            Installation no.
			"Meter number",      					// 116  Maalernr                        R       1    Meter no.                                                                   Meter no.
			"Meter size",      						// 117  MaalerSize                      R       1    Meter size                                                                  Meter size
			"Customer",      						// 118  Kunde                           R       1    Customer                                                                    Customer
			"Date of installation",     	 		// 119  Installationsdato               R       1    Date of installation                                                        Installation date
			"Meter index",      					// 120  Counter                         R       1    Meter index                                                                 Meter index
			"Project no.",      					// 121  Sag                             R       1    Project no.                                                                 Project no.
			"Serial number",      					// 122  Serienr                         R       1    Uniflo serial no.                                                           Serial no.
			"Flow sensor",      					// 123  Flowmaaler                      R       1    Manufacture and type                                                        Flow type
			"Temperature sensor",     	 			// 124  Tempmaaler                      R       1    Manufacture and type                                                        Temp type
			"Interval log size",      				// 125  IntvalLogDyb                    R            No. of logs
			"24 hour log size",      				// 126  DagsLogDyb                      R            No. of logs
			"Snap shot log size",      				// 127  SnapshotDyb                     R            No. of logs
			"Alarm triggered log size",      		// 128  AlarmAktDyb                     R            No. of logs
			"Interval log width",      				// 129  IntvalLogBredde                 R            No. of log points
			"24 hour log width",      				// 130  DagslogBredde                   R            No. of log points
			"Snap shot log width",      			// 131  SnapshotBredde                  R            No. of log points
			"Alarm triggered log width",      		// 132  AlarmAktBredde                  R            No. of log points
			"Flow measured",  						// 133  FlowUnCorr           m3/h               1R0 1Flow measured                                                               Flow meas.
			"Methane",  							// 134  Methan                          R       1    Methane                                                                     Methane
			"Nitrogen",  							// 135  Nitrogen                        R       1    Nitrogen                                                                    Nitrogen
			"CO2",  								// 136  CarbonDioxide                   R       1    CO2                                                                         CO2
			"Ethane",  								// 137  Ethan                           R       1    Ethane                                                                      Ethane
			"Propane",  							// 138  Propan                          R       1    Propane                                                                     Propane
			"Water",  								// 139  Water                           R       1    Water                                                                       Water
			"Hydrg. Sul.",  						// 140  HydrogenSylfide                 R       1    Hydrg. Sul.                                                                 Hydrg. Sul.
			"Hydrogen",  							// 111  Hydrogen                        R       1    Hydrogen                                                                    Hydrogen
			"Carbon monoxide",  					// 142  CarbonMonoxide                  R       1    Carb. mo.                                                                   Carb. mo.
			"Oxygen",  								// 143  Oxygen                          R       1    Oxygen                                                                      Oxygen
			"i-Butane",  							// 144  iButan                          R       1    i-Butane                                                                    i-Butane
			"n-Butane",  							// 145  nButan                          R       1    n-Butane                                                                    n-Butane
			"i-Pentane",  							// 146  iPentan                         R       1    i-Pentane                                                                   i-Pentane
			"n-Pentane",  							// 147  nPentan                         R       1    n-Pentane                                                                   n-Pentane
			"n-Hexane",  							// 148  nHexan                          R       1    n-Hexane                                                                    n-Hexane
			"n-Heptane",  							// 149  nHeptan                         R       1    n-Heptane                                                                   n-Heptane
			"n-Octane",  							// 150  nOctan                          R       1    n-Octane                                                                    n-Octane
			"n-Nonane",  							// 151  nNontan                         R       1    n-Nonane                                                                    n-Nonane
			"n-Decane",  							// 152  nDecan                          R       1    n-Decane                                                                    n-Decane
			"Helium",  								// 153  Helium                          R       1    Helium                                                                      Helium
			"Argon",  								// 154  Argon                           R       1    Argon                                                                       Argon
			"Gas calculation formula",      		// 155  BeregningsMetode                R       1    Formular                                                                    Formular
			"Gas composition",      				// 156  GasComp                         R       1    Gas composition                                                             Gas comp.
			"Gas composition revisiontime",      	// 157  RevTime                         R       1R   Time of rev.                                                                Comp. rev.
			"Density",      						// 158  Density                         R       1    Density (rel.)                                      0.1         2           Density rel.
			"Gas conversion table",   				// 159  Gastabel             800        R            Gas correction tabel
			"Password level 3",      				// 160  Password3                       R            Password level 3
			"Password level 2",      				// 161  Password2                       R            Password level 2
			"Password level 1",      				// 162  Password1                       R            Password level 1
			"Log reorganize bits",      			// 163  LogReOrg                        R            Display mode
			"Correction factor",      				// 164  KorrF                                   1R0 1Corrections factor                                                          Corr. fact.
			"Option board SW version",    			// 165  IOVersion            14                  R   IO version
			"Option board serial number",    		// 166  IOSerienr            28                  R   IO serienr
			"HF Card type",      					// 167  HFCard                                   R   HF/Puls subtype
			"SN Table",    							// 168  SNTabel              40                  R   I/O - tabel
			"Temperature code",      				// 169  TempStregkode                           1    Temperture code                                                             Temperture code
			"Interval log temperature min.",    	// 170  ITempMin             °C         0        R1  Min. temp.
			"Interval log temperature max.",    	// 171  ITempMax             °C         0        R1  Max. temp.
			"Interval log pressure min.",   		// 172  ITrykMin             BarA      U0        R1  Min. Press.
			"Interval log pressure max.",   		// 173  ITrykMax             BarA      U0        R1  Max. Press.
			"Interval log Flow corrected min.",  	// 174  IFlowUMin            m3/h       0        R1  Min. flow corr.
			"Interval log Flow corrected max.",  	// 175  IFlowUMax            m3/h       0        R1  Max. flow corr.
			"Interval log Flow converted min.", 	// 176  IFlowKMin            Nm3/h     U0        R1  Min. flow conv.
			"Interval log Flow converted max.", 	// 177  IFlowKMax            Nm3/h     U0        R1  Max. flow conv.
			"Daily log temperature min.",    		// 178  DTempMin             °C         0        R2  Min. temp.
			"Daily log temperature max.",    		// 179  DTempMax             °C         0        R2  Max. temp.
			"Daily log pressure min.",   			// 180  DTrykMin             BarA      U0        R2  Min. Press.
			"Daily log pressure max.",   			// 181  DTrykMax             BarA      U0        R2  Max. Press.
			"Daily log Flow corrected max",  		// 182  DFlowUMin            m3/h       0        R2  Min. flow corr.
			"Daily log Flow corrected max.", 	 	// 183  DFlowUMax            m3/h       0        R2  Max. flow corr.
			"Daily log Flow converted min.",	 	// 184  DFlowKMin            Nm3/h     U0        R2  Min. flow conv.
			"Daily log Flow converted max.", 		// 185  DFlowKMax            Nm3/h     U0        R2  Max. flow conv.
			"Interval log Power min.",    			// 186  IEnergyFMin          MJ        U0        R1  Min. power
			"Interval log Power max.",    			// 187  IEnergyFMax          MJ        U0        R1  Max. power
			"Daily log Power min.",    				// 188  DEnergyFMin          MJ        U0        R2  Min. power
			"Daily log Power max.",   	 			// 189  DEnergyFMax          MJ        U0        R2  Max. power
			"Volume measured integer part",    		// 190  VolMeasuredI         m3                 1 01 Vol. measured                                                               Vol. meas.
			"Volume measured decimal part",    		// 191  VolMeasuredF         m3                 1 0  Vol. measured dec.                                                          Vol. meas. dec
			"Volume at error",    					// 192  VolErr               m3                      Vol. meas. at error                                 0           99999999
			"Volume at error integer part",    		// 193  VolErrI              m3                 1 01 Vol. meas. at error                                                         Vol. err.
			"Volume at error decimal part",    		// 194  VolErrF              m3                 1 0  Vol. meas. at err. dec.                                                     Vol. err. dec
			"Superior heat value",					// 195  Heatvalue            MJ/nm3    UR       1    Superior heat value                                 19          48          S. heat value
			"C6+ value",      						// 196  C6Value                         R       1    C6+ value                                                                   C6+ value
			"C6+ enabled",     	 					// 197  C6Enable                        R       1    C6+                                                                         C6+ enable
			"AD pressure offset",      				// 198  ADTrykOffset                                 AD pressure offset                                  -128        127
			"AD pressure temperature offset",      	// 199  ADTempTrykOffset                             AD pressure temperature offset                      -128        127
			"Volume at error",      				// 200 256  CountVmAtError                  R            Count Vm at Error
			"TZ Mode",      						// 201 257  TZMode                          R            Corrector Type
			"TZ pressure",   						// 202 258  TZTryk               bar A     UR            Pressure [bar A]                                    0.6         80
			"Menu and alarm text", 					// 203 259  Menu/Alarm           31768                   Menu og alarm text for grafisk display
			"Number of slave units",      			// 204 260  NoOfSlaves                              0    Number of slaves                                                            Number of slaves
			"Update EEPROM",      					// 205 261  OpdataerEEProm                               Opdater EEProm
			"Name of alarmfile",      				// 206 262  Alarmfile                       R            Alarm text file
			"Not used",      						// 207 263  No output                                   1No output
			"Lock graphic display",      			// 208 264  LockDisp                                     Lock graphic display
			"Unlock graphic display",      			// 209 265  UnLockDisp                                   UnLock graphic display
			"Volume control integer part",    		// 210 266  VolCtrlI             m3                 1 01 Vol. control                                                                Vol. ctrl.
			"Volume control decimal part",    		// 211 267  VolCtrlF             m3                 1 0  Vol. control dec.                                                           Vol. ctrl. dec
			"Consumption actual hour, int. part",	// 212  VolHourI             m3                 1R0  Current hour incr.                                                          Hour incr.
			"Consumption actual hour, dec. part",	// 213  VolHourF             m3                 1R0  Current incr. dec.                                                          Hour incr. dec
			"1. Max hour consumption time",      	// 214  MaxTime                                 1R0  1.Max hour incr. time                                                       1. Max incr. time
			"1. Max time consumption, int. part",  	// 215  MaxVolI              m3                 1R0  1.Max hour incr.                                                            1. Max incr.
			"1. Max time consumption, dec. part",  	// 216  MaxVolF              m3                 1R0  1.Max hour incr. dec.                                                       1. Max incr. dec
			"2. Max hour consumption time",      	// 217  MaxTime2                                1R0  2.Max hour incr. time                                                       2. Max time
			"2. Max time consumption, int. part",   // 218  MaxVolI2             m3                 1R0  2.max hour incr.                                                            2. Max incr.
			"2. Max time consumption, dec. part",   // 219  MaxVolF2             m3                 1R0  2.max hour incr. dec.                                                       2. Max incr. dec
			"3. Max hour consumption time",      	// 220  MaxTime3                                1R0  3.Max hour incr. time                                                       3. Max time
			"3. Max time consumption, int. part",   // 221  MaxVolI3             m3                 1R0  3.max hour incr.                                                            3. Max incr.
			"3. Max time consumption, dec. part",   // 222  MaxVolF3             m3                 1R0  3.max hour incr. dec.                                                       3. Max incr. dec
			"Pressure calibration operator",		// 223  PresureCalOP                            1    Pressure calibration operator                                               Press. cal. op.
			"Temperature calibration operator",		// 224  TempCalOP                               1    Temperature calibration operator                                            Temp. cal. op.
			"Event log index",      				// 225  EventlogIdx                              R   Indexpointer for Event log
			"Alarm log index",      				// 226  AlarmlogIdx                              R   Indexpointer for Alarmlog log
			"Log interval",     					// 227  IntervallogState                R            Log interval
			"Interval of measurement",     			// 228  MaalInterval         Sec.      UR       1    Interval of measurement                             0           254         Cycl. of meas.
			"Daily log time Hour",      			// 229  DagslogTime                     R            Time of Day                                         0           24
			"Daily log time Hour",      			// 230  DagslogMin                      R            Time of Day                                         0           60
			"Interval log index",      				// 231  IntervallogIdx                               Indexpointer for interval log
			"Daily log index",      				// 232  DagslogIdx                               R   Indexpointer for dagslog
			"Snapshot log index",      				// 233  SnapshotlogIdx                           R   Indexpointer for snapshot log
			"Alarm triggered log index",      		// 234  AlarmtriglogIdx                          R   Indexpointer for alarmaktiveret log
			"Display time",     					// 235  DispTime             Sec.      UR            Change to display 1 after
			"Max pulse error",      				// 236  MaxpulsError                    R            Max. pulse error
			"Pressure sensor is changed",      		// 237  ChangePressSensor                            Pressure sensor is change
			"Temperature sensor ischanged",      	// 238  ChangeTempSensor                             Temperature sensor is change
			"Month log width",      				// 239  MonthBredde                     R            No. of log points
			"Month log size",      					// 240  MonthDyb                        R            No. of logs
			"Month log register",    				// 241  MonthLogreg          20         R            Month log registre
			"Month log index",      				// 242  MonthlogIdx                              R   Indexpointer for Month log
			"Display test",      					// 243  Disptest                                     Display test
			"Firmware address range OK",      		// 244  FirmwareUpdate                               Firmware update
			"Firmware update start",      			// 245  FUPDATESTART                                 Firmware update start
			"Firmware update end",      			// 246  FUPDATEEND                                   Firmware update end
			"Firmware update cancel",      			// 247  FUPDATECANCEL                                Firmware update cancel
			"Program checksum",      				// 248  PRGChecksum                             1R   Program checksum                                                            Prg. chksum
			"Temperature calibrationtable",    		// 249  TempCorr             30                      Temperature calibretion tabel
			"Pressure calibration table",    		// 250  PressCorr            60                      Pressure calibretion tabel
			"Force measurement",      				// 251  ForceMesurement                              Force measurment
			"Pressure calibration time",      		// 252  PresureCalTime                          1    Pressure calibration time                                                   Press. cal. Time
			"Temperature calibrationtime",      	// 253  TempCalTime                             1    Temperature calibration time                                                Temp. cal. Time
			"Conversion tablechecksum",      		// 254  ConvTableChecksum                       1R   Conversion table checksum                                                   Conv. table chksum
			"Conversion table DLL version",      	// 255  ConvTableDLLChecksum                    1R   DLL checksum                                                                DLL checksum
			"",      								// 256
			"",      								// 257
			"",      								// 258
			"",      								// 259
			"",      								// 260
			"",      								// 261
			"",      								// 262
			"",     								// 263
			"",      								// 264
			"",      								// 265
			"",      								// 266
			"",      								// 267
			"",      								// 268
			"",      								// 269
			"",      								// 270
			"",      								// 271
			"",      								// 272
			"",      								// 273
			"",      								// 274
			"",      								// 275
			"",      								// 276
		};

		private static final int ABSOLUTE_ADDRESSES[] = {
			0x573000B0,   // 0    Za                              R       1R   Za                                                                          Za
			0x03000001,   // 1    SlaveAdr                                1    Modbus slave address                                                        Modbus address
			0x01500002,   // 2    Time                            T       1    Uniflo time                                                                 Time
			0x5A3000B4,   // 3    Zb2                             R       1R   Zb                                                                          Zb
			0x0BF01B0C,   // 4    TempCorrTabel        112                     Temperatur correction tabel
			0x011000F6,   // 5    Bat                  Days      U        1    Battery remaining                                                           Bat. remaining
			0x23300200,   // 6    IEnergyFAvg          MJ        U0        R1  Average power.
			0x23300204,   // 7    ITempAvg             °C         0        R1  Average temp.
			0x43300208,   // 8    ITrykAvg             BarA      U0        R1  Average Press.
			0x2330020C,   // 9    IFlowUAvg            m3/h       0        R1  Avg. flow corr.
			0x23300210,   // 10   IFlowKAvg            Nm3/h     U0        R1  Avg. flow conv.
			0x23300214,   // 11   DEnergyFAvg          MJ        U0        R2  Average power.
			0x23300218,   // 12   DTempAvg             °C         0        R2  Average temp.
			0x4330021C,   // 13   DTrykAvg             BarA      U0        R2  Average Press.
			0x23300220,   // 14   DFlowUAvg            m3/h       0        R2  Avg. flow corr.
			0x23300224,   // 15   DFlowKAvg            Nm3/h     U0        R2  Avg. flow conv.
			0x071001F0,   // 16   ConfigChecksum                          1R   Configuration checksum                                                      Config. checksum
			0x07100152,   // 17   LogChecksum                              R0  Log line checksum
			0x018000C4,   // 18   PulsoutReg1                     R            Pulse output 1
			0x018000C5,   // 19   PulsoutReg2                     R            Pulse output 2
			0x017000C6,   // 20   PulsDiv1                        R            Division faktor for puls 1
			0x017000C7,   // 21   PulsDiv2                        R            Division faktor for puls 2
			0x0300000F,   // 22   Powerup                                      Numbers of  powerup
			0x07100010,   // 23   ADtryk                                   R3  Pressure AD-count
			0x07100012,   // 24   ADtemp                                   R3  Temperature AD-count
			0x071000DA,   // 25   ADSensortemp                             R3  Pressure sensor A-count
			0x017000F4,   // 26   PulsOutTid                      R            Pulse output period length
			0xBA300064,   // 27   BassisEnergi         MJ/nm3    UR       1    Heat value                                                                  Heat value
			0x0AF01CC8,   // 28   DisplaySetupTable    18         R            Display setup table
			0x003000F0,   // 29   Operator                                     Operator ID 4 char
			0x07F0CEE8,   // 30   EventLog                                     Eventlog (200 logninger)
			0x070000D6,   // 31   Password                                 R   Actual secure level
			0xC34000B8,   // 32   VolCtrl              m3                      Vol. control                                        0           99999999
			0x0B601BC4,   // 33   SystemData                                   SystemData                                                                  Temp type
			0x09100124,   // 34   PulsCheckPulser      pulses    UR            Pulse check every
			0x07F011A0,   // 35   AlarmLog                                     Alarmlog (100 logninger)
			0x47300080,   // 36   Pressure             bar A     U        1R0 1Pressure                                                                    Pressure
			0x27300084,   // 37   Temperature          °C                 1R0 1Temperature                                                                 Temperature
			0x57300088,   // 38   Korr                                    1R0 1Conversion factor                                                           Conv. factor
			0xDB3000F8,   // 39   Pulsvalue            m3/pulse  UR       1    Value of pulse                                      0           99999999    Pulse Value
			0x27300078,   // 40   FlowCorr             m3/h               1R0 1Flow corrected                                                              Flow corr.
			0x2730007C,   // 41   FlowConv             Nm3/h     U        1R0 1Flow conv.                                                                  Flow conv.
			0xC3400068,   // 42   VolCorr              m3                      Vol. corrected                                      0           99999999
			0xC3400070,   // 43   VolConv              Nm3       U             Vol. converted                                      0           99999999
			0x83200068,   // 44   VolCorrI             m3                 1 01 Vol. corrected                                                              Vol. corr.
			0xC330006C,   // 45   VolCorrF             m3                 1 0  Vol. corr. dec.                                                             Vol. corr. dec
			0x83200070,   // 46   VolConvI             Nm3       U        1 01 Vol. conv.                                                                  Vol. conv.
			0xC3300074,   // 47   VolConvF             Nm3       U        1 0  Vol. conv. dec.                                                             Vol. conv. dec
			0x09F01A18,   // 48   AlarmSetupTable      192        R            Alarm setup table
			0x09100126,   // 49   Flowstop             seconds   UR            Flowstop after                                      10          600
			0x493000FC,   // 50   TrykMin              BarA      UR       1    Pressure low limit                                  0.6         80          Press. low limit
			0x49300100,   // 51   TrykMax              BarA      UR       1    Pressure high limit                                 0.6         80          Press. high limit
			0x29300104,   // 52   TempMin              °C         R       1    Temperature low limit                               -40         70          Temp. low limit
			0x29300108,   // 53   TempMax              °C         R       1    Temperature high limit                              -40         70          Temp. high limit
			0x2930010C,   // 54   FlowCorrMax          m3/h       R       1    Flow high limit                                     0           99999999    Flow high limit
			0x29300110,   // 55   FlowConvMax          Nm3/h     UR       1    Conv. flow high limit                               0           99999999    Conv. flow high limit
			0x29300114,   // 56   EnergiMax            MJ/h      UR       1    Power high limit                                    0           99999999    Power high limit
			0xDB300118,   // 57   TrykVFejl            BarA      UR            Fallback press. used on error                       0.6         80
			0xAB30011C,   // 58   TempVFejl            °C         R            Fallback temp. used on error                        -40         70
			0x0B0000C1,   // 59   DispOffTime          Sec.      UR            Turn off display after                              4           240
			0xDB300128,   // 60   Pb                   BarA      UR       1 1  Base pressure                                       0.6         80          Base press.
			0xAB30012C,   // 61   Tb                   °C         R       1 1  Base temperature                                    -40         70          Base temp.
			0x9B3000DC,   // 62   MaxPress             BarA      UR            Pressure range                                      0.6         80
			0x9B3000E0,   // 63   MinPress             BarA      UR            Pressure range                                      0.6         80
			0x9B3000E4,   // 64   MaxTemp              °C         R            Temperature range                                   -40         70
			0x9B300130,   // 65   MinTemp              °C         R            Temperature range                                   -40         70
			0x07F00234,   // 66   AlarmTable           12                      Alarm table
			0x03F01BE4,   // 67   AlarmCntTable        96         0            Alarm cnt table
			0x2730008C,   // 68   EnergiFlow           MJ/h      U        1R0 1Power                                                                       Power
			0x010000F5,   // 69   Alarmset                                     Set alarm reg in uniflo
			0x07901E02,   // 70   TDeviceID                                R   Manufacture and type                                                        P. Sens. type
			0x07A01E1F,   // 71   TPStyle                                  R   Tryksensor Pressure style
			0x07D01E20,   // 72   TPressRange                             1R   Range [bar]                                                                 Range [bar]
			0x07C01E24,   // 73   TSerialno                               1R   Serial no.                                                                  P. sens no.
			0x07001E28,   // 74   TCalibDay                                R   Date of calibration                                2
			0x07001E29,   // 75   TCalibMonth                              R   Date of calibration                                2
			0x07001E2A,   // 76   TCalibYear                               R   Date of calibration                                2
			0x57D01E2B,   // 77   TPres1Bar                                R   Tryksensor Pressure signal at 1 bar at 20 deg. C
			0x57D01E2F,   // 78   TPresFS                                  R   Tryksensor Pressure signal FS at 20 deg. C
			0x57D01E33,   // 79   TTemp                                    R   Tryksensor temperature signal at 20 deg. C
			0x01101E04,   // 80   ResetAlarm                                   Reset alarm
			0x03101E06,   // 81   ClrAlarm                                     Clear alarm
			0x897000EA,   // 82   ADAmp                                        AD amplify
			0x03101E0A,   // 83   ClrEvent                                     Delete configuration log
			0x02001E0C,   // 84   LockTable                                    Lock correction table
			0x02001E0E,   // 85   UnLockTable                                  UnLock correction table
			0x02001E10,   // 86   OpdateCorrTable                              Update correction table
			0x02001E12,   // 87   OpdateGasData                                Update Gas data
			0x01101E14,   // 88   Snapshot                                     Make a snapshot in Uniflo
			0x017000C8,   // 89   PulsOut1En                      R            Puls out 1 enable
			0x017000C9,   // 90   PulsOut2En                      R            Puls out 2 enable
			0x0B601B98,   // 91   Menufile                        R            Menu file
			0x070000D7,   // 92   IntvAntal                                R   x
			0x030000CA,   // 93   GSetup                                       Graphic display gain setup
			0x030000CB,   // 94   CSetup                                       Graphic display contrast setup
			0xC3400090,   // 95   Energi               MJ        U             Energy                                              0           99999999
			0x83200090,   // 96   EnergiI              MJ        U        1 01 Energy                                                                      Energy
			0xC3300094,   // 97   EnergiF              MJ        U        1 0  Energy dec.                                                                 Energy dec.
			0xF10000D8,   // 98   OptionCH                                     Options kort changed
			0x0B7000E8,   // 99   TempSource                      R            Temperatur sensor sourse
			0x0B7000E9,   // 100  PresSource                      R            Pressure sensor source
			0x07F00234,   // 101  Alarm active 1-32                        R0  Alarm active 1-32
			0x07F00238,   // 102  Alarm active 33-64                       R0  Alarm active 33-64
			0x07F0023C,   // 103  Alarm active 64-96                       R0  Alarm active 64-96
			0x07F01BD8,   // 104  Alarm reg. 1-32                          R0  Alarm reg. 1-32
			0x07F01BDC,   // 105  Alarm reg. 33-64                         R0  Alarm reg. 33-64
			0x07F01BE0,   // 106  Alarm reg. 64-96                         R0  Alarm reg. 64-96
			0x01F000CC,   // 107  IOTabel              10                  R   I/O - tabel
			0xC3400098,   // 108  VolMeasured          m3                      Volume measured                                     0           99999999
			0x07B01E8A,   // 109  TChecksum                                R   Pressure sensor check sum
			0x0BF0FFE0,   // 110  IntvalLogreg         20         R            Interval log registre
			0x09F04000,   // 111  DagsLogreg           20         R            24 hour log registre
			0x09F06000,   // 112  Snapshotreg          20         R            Snapshot log registre
			0x09F02000,   // 113  AlarmAktLogreg       20         R            Alarm triggered log registre
			0x07601DC0,   // 114  VerTyp                                  1R   Uniflo type and version                                                     Type/version
			0x01601588,   // 115  Installation                    R       1    Installation no.                                                            Installation no.
			0x0160159E,   // 116  Maalernr                        R       1    Meter no.                                                                   Meter no.
			0x016015B4,   // 117  MaalerSize                      R       1    Meter size                                                                  Meter size
			0x016015CA,   // 118  Kunde                           R       1    Customer                                                                    Customer
			0x016015E0,   // 119  Installationsdato               R       1    Date of installation                                                        Installation date
			0x016015F6,   // 120  Counter                         R       1    Meter index                                                                 Meter index
			0x0160160C,   // 121  Sag                             R       1    Project no.                                                                 Project no.
			0x09601622,   // 122  Serienr                         R       1    Uniflo serial no.                                                           Serial no.
			0x01601638,   // 123  Flowmaaler                      R       1    Manufacture and type                                                        Flow type
			0x0160164E,   // 124  Tempmaaler                      R       1    Manufacture and type                                                        Temp type
			0x0B10FFF4,   // 125  IntvalLogDyb                    R            No. of logs
			0x09104014,   // 126  DagsLogDyb                      R            No. of logs
			0x09106014,   // 127  SnapshotDyb                     R            No. of logs
			0x09102014,   // 128  AlarmAktDyb                     R            No. of logs
			0x0B00FFF6,   // 129  IntvalLogBredde                 R            No. of log points
			0x09004016,   // 130  DagslogBredde                   R            No. of log points
			0x09006016,   // 131  SnapshotBredde                  R            No. of log points
			0x09002016,   // 132  AlarmAktBredde                  R            No. of log points
			0x273000A0,   // 133  FlowUnCorr           m3/h               1R0 1Flow measured                                                               Flow meas.
			0xDA301668,   // 134  Methan                          R       1    Methane                                                                     Methane
			0xDA30166C,   // 135  Nitrogen                        R       1    Nitrogen                                                                    Nitrogen
			0xDA301670,   // 136  CarbonDioxide                   R       1    CO2                                                                         CO2
			0xDA301674,   // 137  Ethan                           R       1    Ethane                                                                      Ethane
			0xDA301678,   // 138  Propan                          R       1    Propane                                                                     Propane
			0xDA30167C,   // 139  Water                           R       1    Water                                                                       Water
			0xDA301680,   // 140  HydrogenSylfide                 R       1    Hydrg. Sul.                                                                 Hydrg. Sul.
			0xDA301684,   // 111  Hydrogen                        R       1    Hydrogen                                                                    Hydrogen
			0xDA301688,   // 142  CarbonMonoxide                  R       1    Carb. mo.                                                                   Carb. mo.
			0xDA30168C,   // 143  Oxygen                          R       1    Oxygen                                                                      Oxygen
			0xDA301690,   // 144  iButan                          R       1    i-Butane                                                                    i-Butane
			0xDA301694,   // 145  nButan                          R       1    n-Butane                                                                    n-Butane
			0xDA301698,   // 146  iPentan                         R       1    i-Pentane                                                                   i-Pentane
			0xDA30169C,   // 147  nPentan                         R       1    n-Pentane                                                                   n-Pentane
			0xDA3016A0,   // 148  nHexan                          R       1    n-Hexane                                                                    n-Hexane
			0xDA3016A4,   // 149  nHeptan                         R       1    n-Heptane                                                                   n-Heptane
			0xDA3016A8,   // 150  nOctan                          R       1    n-Octane                                                                    n-Octane
			0xDA3016AC,   // 151  nNontan                         R       1    n-Nonane                                                                    n-Nonane
			0xDA3016B0,   // 152  nDecan                          R       1    n-Decane                                                                    n-Decane
			0xDA3016B4,   // 153  Helium                          R       1    Helium                                                                      Helium
			0xDA3016B8,   // 154  Argon                           R       1    Argon                                                                       Argon
			0x8A7016BC,   // 155  BeregningsMetode                R       1    Formular                                                                    Formular
			0xFA6016BE,   // 156  GasComp                         R       1    Gas composition                                                             Gas comp.
			0x8A5016D6,   // 157  RevTime                         R       1R   Time of rev.                                                                Comp. rev.
			0xDA3016DC,   // 158  Density                         R       1    Density (rel.)                                      0.1         2           Density rel.
			0x0AF016E0,   // 159  Gastabel             800        R            Gas correction tabel
			0x0AE01A00,   // 160  Password3                       R            Password level 3
			0x0AE01A08,   // 161  Password2                       R            Password level 2
			0x0AE01A10,   // 162  Password1                       R            Password level 1
			0x007000C3,   // 163  LogReOrg                        R            Display mode
			0x573000A4,   // 164  KorrF                                   1R0 1Corrections factor                                                          Corr. fact.
			0x07F00200,   // 165  IOVersion            14                  R   IO version
			0x07F0020E,   // 166  IOSerienr            28                  R   IO serienr
			0x07130300,   // 167  HFCard                                   R   HF/Puls subtype
			0x07F00200,   // 168  SNTabel              40                  R   I/O - tabel
			0xFB601B7C,   // 169  TempStregkode                           1    Temperture code                                                             Temperture code
			0x23300020,   // 170  ITempMin             °C         0        R1  Min. temp.
			0x23300024,   // 171  ITempMax             °C         0        R1  Max. temp.
			0x43300028,   // 172  ITrykMin             BarA      U0        R1  Min. Press.
			0x4330002C,   // 173  ITrykMax             BarA      U0        R1  Max. Press.
			0x23300030,   // 174  IFlowUMin            m3/h       0        R1  Min. flow corr.
			0x23300034,   // 175  IFlowUMax            m3/h       0        R1  Max. flow corr.
			0x23300038,   // 176  IFlowKMin            Nm3/h     U0        R1  Min. flow conv.
			0x2330003C,   // 177  IFlowKMax            Nm3/h     U0        R1  Max. flow conv.
			0x23300040,   // 178  DTempMin             °C         0        R2  Min. temp.
			0x23300044,   // 179  DTempMax             °C         0        R2  Max. temp.
			0x43300048,   // 180  DTrykMin             BarA      U0        R2  Min. Press.
			0x4330004C,   // 181  DTrykMax             BarA      U0        R2  Max. Press.
			0x23300050,   // 182  DFlowUMin            m3/h       0        R2  Min. flow corr.
			0x23300054,   // 183  DFlowUMax            m3/h       0        R2  Max. flow corr.
			0x23300058,   // 184  DFlowKMin            Nm3/h     U0        R2  Min. flow conv.
			0x2330005C,   // 185  DFlowKMax            Nm3/h     U0        R2  Max. flow conv.
			0x23300018,   // 186  IEnergyFMin          MJ        U0        R1  Min. power
			0x2330001C,   // 187  IEnergyFMax          MJ        U0        R1  Max. power
			0x23300060,   // 188  DEnergyFMin          MJ        U0        R2  Min. power
			0x23300014,   // 189  DEnergyFMax          MJ        U0        R2  Max. power
			0x83200098,   // 190  VolMeasuredI         m3                 1 01 Vol. measured                                                               Vol. meas.
			0xC330009C,   // 191  VolMeasuredF         m3                 1 0  Vol. measured dec.                                                          Vol. meas. dec
			0xC34000A8,   // 192  VolErr               m3                      Vol. meas. at error                                 0           99999999
			0x832000A8,   // 193  VolErrI              m3                 1 01 Vol. meas. at error                                                         Vol. err.
			0xC33000AC,   // 194  VolErrF              m3                 1 0  Vol. meas. at err. dec.                                                     Vol. err. dec
			0xBA301B08,   // 195  Heatvalue            MJ/mn3    UR       1    Superior heat value                                 19          48          S. heat value
			0xCA301B92,   // 196  C6Value                         R       1    C6+ value                                                                   C6+ value
			0x8A701B96,   // 197  C6Enable                        R       1    C6+                                                                         C6+ enable
			0x8A000008,   // 198  ADTrykOffset                                 AD pressure offset                                  -128        127
			0x8A000009,   // 199  ADTempTrykOffset                             AD pressure temperature offset                      -128        127
			0x8A7000D4,//200 256  CountVmAtError                  R            Count Vm at Error
			0x8A7000D5,//201 257  TZMode                          R            Corrector Type
			0xDA300118,//202 258  TZTryk               bar A     UR            Pressure [bar A]                                    0.6         80
			0x0370C000,//203 259  Menu/Alarm           31768                   Menu og alarm text for grafisk display
			0x03000000,//204 260  NoOfSlaves                              0    Number of slaves                                                            Number of slaves
			0x00101E08,//205 261  OpdataerEEProm                               Opdater EEProm
			0x0B601BAE,//206 262  Alarmfile                       R            Alarm text file
			0x00000000,//207 263  No output                                   1No output
			0x02001E16,//208 264  LockDisp                                     Lock graphic display
			0x02001E18,//209 265  UnLockDisp                                   UnLock graphic display
			0x832000B8,//210 266  VolCtrlI             m3                 1 01 Vol. control                                                                Vol. ctrl.
			0xC33000BC,//211 267  VolCtrlF             m3                 1 0  Vol. control dec.                                                           Vol. ctrl. dec
			0x07200240,   // 212  VolHourI             m3                 1R0  Current hour incr.                                                          Hour incr.
			0x47300244,   // 213  VolHourF             m3                 1R0  Current incr. dec.                                                          Hour incr. dec
			0x07500248,   // 214  MaxTime                                 1R0  1.Max hour incr. time                                                       1. Max incr. time
			0x0720024C,   // 215  MaxVolI              m3                 1R0  1.Max hour incr.                                                            1. Max incr.
			0x47300250,   // 216  MaxVolF              m3                 1R0  1.Max hour incr. dec.                                                       1. Max incr. dec
			0x07500254,   // 217  MaxTime2                                1R0  2.Max hour incr. time                                                       2. Max time
			0x07200258,   // 218  MaxVolI2             m3                 1R0  2.max hour incr.                                                            2. Max incr.
			0x4730025C,   // 219  MaxVolF2             m3                 1R0  2.max hour incr. dec.                                                       2. Max incr. dec
			0x07500260,   // 220  MaxTime3                                1R0  3.Max hour incr. time                                                       3. Max time
			0x07200264,   // 221  MaxVolI3             m3                 1R0  3.max hour incr.                                                            3. Max incr.
			0x47300268,   // 222  MaxVolF3             m3                 1R0  3.max hour incr. dec.                                                       3. Max incr. dec
			0x0F201CE6,   // 223  PresureCalOP                            1    Pressure calibration operator                                               Press. cal. op.
			0x0F201CEA,   // 224  TempCalOP                               1    Temperature calibration operator                                            Temp. cal. op.
			0x071000EC,   // 225  EventlogIdx                              R   Indexpointer for Event log
			0x071000EE,   // 226  AlarmlogIdx                              R   Indexpointer for Alarmlog log
			0x0B70000D,   // 227  IntervallogState                R            Log interval
			0x0A00000A,   // 228  MaalInterval         Sec.      UR       1    Interval of measurement                             0           254         Cycl. of meas.
			0x0900000B,   // 229  DagslogTime                     R            Time of Day                                         0           24
			0x0900000C,   // 230  DagslogMin                      R            Time of Day                                         0           60
			0x07100148,   // 231  IntervallogIdx                               Indexpointer for interval log
			0x0710014A,   // 232  DagslogIdx                               R   Indexpointer for dagslog
			0x0710014C,   // 233  SnapshotlogIdx                           R   Indexpointer for snapshot log
			0x0710014E,   // 234  AlarmtriglogIdx                          R   Indexpointer for alarmaktiveret log
			0x090000C0,   // 235  DispTime             Sec.      UR            Change to display 1 after
			0x090000C2,   // 236  MaxpulsError                    R            Max. pulse error
			0xF3101F00,   // 237  ChangePressSensor                            Pressure sensor is change
			0xF3101F02,   // 238  ChangeTempSensor                             Temperature sensor is change
			0x0B007016,   // 239  MonthBredde                     R            No. of log points
			0x0B107014,   // 240  MonthDyb                        R            No. of logs
			0x0BF07000,   // 241  MonthLogreg          20         R            Month log registre
			0x071001F2,   // 242  MonthlogIdx                              R   Indexpointer for Month log
			0x02001E1A,   // 243  Disptest                                     Display test
			0x00FFFFFF,   // 244  FirmwareUpdate                               Firmware update
			0xF3101E1C,   // 245  FUPDATESTART                                 Firmware update start
			0xF3101E1E,   // 246  FUPDATEEND                                   Firmware update end
			0xF3101E20,   // 247  FUPDATECANCEL                                Firmware update cancel
			0x07100150,   // 248  PRGChecksum                             1R   Program checksum                                                            Prg. chksum
			0xFBF00194,   // 249  TempCorr             30                      Temperature calibretion tabel
			0xFBF001B2,   // 250  PressCorr            60                      Pressure calibretion tabel
			0x00101E22,   // 251  ForceMesurement                              Force measurment
			0x0F501CDA,   // 252  PresureCalTime                          1    Pressure calibration time                                                   Press. cal. Time
			0x0F501CE0,   // 253  TempCalTime                             1    Temperature calibration time                                                Temp. cal. Time
			0xFA101CEE,   // 254  ConvTableChecksum                       1R   Conversion table checksum                                                   Conv. table chksum
			0xFA101CF0,   // 255  ConvTableDLLChecksum                    1R                                                                               DLL checksum
			0x00101E04,   // 256
			0x00101E06,   // 257
			0x00101E0A,   // 258
			0x00101E0C,   // 259
			0x00101E0E,   // 260
			0x00101E10,   // 261
			0x00101E12,   // 262
			0x00101E14,   // 263
			0x00101E08,   // 264
			0x00001E16,   // 265
			0x00001E18,   // 266
			0x00001E1A,   // 267
			0x00101E1C,   // 268
			0x00101E1E,   // 269
			0x00101E20,   // 270
			0x00101E22,   // 271
			0x00101F00,   // 272
			0x00101F02,   // 273
			0x00E01E00,   // 274
			0x00F01E02,   // 275
			0x00101E24,   // 276
		};

		private static final String UNITS[] = {
			"",      // 0    Za                              R       1R   Za                                                                          Za
			"",      // 1    SlaveAdr                                1    Modbus slave address                                                        Modbus address
			"s",     // 2    Time                            T       1    Uniflo time                                                                 Time
			"",      // 3    Zb2                             R       1R   Zb                                                                          Zb
			"112",   // 4    TempCorrTabel        112                     Temperatur correction tabel
			"d",     // 5    Bat                  Days      U        1    Battery remaining                                                           Bat. remaining
			"MJ",    // 6    IEnergyFAvg          MJ        U0        R1  Average power.
			"°C",    // 7    ITempAvg             °C         0        R1  Average temp.
			"bar",   // 8    ITrykAvg             BarA      U0        R1  Average Press.
			"m3/h",  // 9    IFlowUAvg            m3/h       0        R1  Avg. flow corr.
			"Nm3/h", // 10   IFlowKAvg            Nm3/h     U0        R1  Avg. flow conv.
			"MJ",    // 11   DEnergyFAvg          MJ        U0        R2  Average power.
			"°C",    // 12   DTempAvg             °C         0        R2  Average temp.
			"bar",   // 13   DTrykAvg             BarA      U0        R2  Average Press.
			"m3/h",  // 14   DFlowUAvg            m3/h       0        R2  Avg. flow corr.
			"Nm3/h", // 15   DFlowKAvg            Nm3/h     U0        R2  Avg. flow conv.
			"",      // 16   ConfigChecksum                          1R   Configuration checksum                                                      Config. checksum
			"",      // 17   LogChecksum                              R0  Log line checksum
			"",      // 18   PulsoutReg1                     R            Pulse output 1
			"",      // 19   PulsoutReg2                     R            Pulse output 2
			"",      // 20   PulsDiv1                        R            Division faktor for puls 1
			"",      // 21   PulsDiv2                        R            Division faktor for puls 2
			"",      // 22   Powerup                                      Numbers of  powerup
			"",      // 23   ADtryk                                   R3  Pressure AD-count
			"",      // 24   ADtemp                                   R3  Temperature AD-count
			"",      // 25   ADSensortemp                             R3  Pressure sensor A-count
			"",      // 26   PulsOutTid                      R            Pulse output period length
			"MJ/Nm3",// 27   BassisEnergi         MJ/nm3    UR       1    Heat value                                                                  Heat value
			"18",    // 28   DisplaySetupTable    18         R            Display setup table
			"",      // 29   Operator                                     Operator ID 4 char
			"",      // 30   EventLog                                     Eventlog (200 logninger)
			"",      // 31   Password                                 R   Actual secure level
			"m3",    // 32   VolCtrl              m3                      Vol. control                                        0           99999999
			"",      // 33   SystemData                                   SystemData                                                                  Temp type
			"",      // 34   PulsCheckPulser      pulses    UR            Pulse check every
			"",      // 35   AlarmLog                                     Alarmlog (100 logninger)
			"bar",   // 36   Pressure             bar A     U        1R0 1Pressure                                                                    Pressure
			"°C",    // 37   Temperature          °C                 1R0 1Temperature                                                                 Temperature
			"",      // 38   Korr                                    1R0 1Conversion factor                                                           Conv. factor

			//FIXME: Onderstaande unit (m3/pulse) onbekend
			"m3",    // 39   Pulsvalue            m3/pulse  UR       1    Value of pulse                                      0           99999999    Pulse Value

			"m3/h",  // 40   FlowCorr             m3/h               1R0 1Flow corrected                                                              Flow corr.
			"Nm3/h", // 41   FlowConv             Nm3/h     U        1R0 1Flow conv.                                                                  Flow conv.
			"m3",    // 42   VolCorr              m3                      Vol. corrected                                      0           99999999
			"Nm3",   // 43   VolConv              Nm3       U             Vol. converted                                      0           99999999
			"m3",    // 44   VolCorrI             m3                 1 01 Vol. corrected                                                              Vol. corr.
			"m3",    // 45   VolCorrF             m3                 1 0  Vol. corr. dec.                                                             Vol. corr. dec
			"Nm3",   // 46   VolConvI             Nm3       U        1 01 Vol. conv.                                                                  Vol. conv.
			"Nm3",   // 47   VolConvF             Nm3       U        1 0  Vol. conv. dec.                                                             Vol. conv. dec
			"192",   // 48   AlarmSetupTable      192        R            Alarm setup table
			"s",     // 49   Flowstop             seconds   UR            Flowstop after                                      10          600
			"bar",   // 50   TrykMin              BarA      UR       1    Pressure low limit                                  0.6         80          Press. low limit
			"bar",   // 51   TrykMax              BarA      UR       1    Pressure high limit                                 0.6         80          Press. high limit
			"°C",    // 52   TempMin              °C         R       1    Temperature low limit                               -40         70          Temp. low limit
			"°C",    // 53   TempMax              °C         R       1    Temperature high limit                              -40         70          Temp. high limit
			"m3/h",  // 54   FlowCorrMax          m3/h       R       1    Flow high limit                                     0           99999999    Flow high limit
			"Nm3/h", // 55   FlowConvMax          Nm3/h     UR       1    Conv. flow high limit                               0           99999999    Conv. flow high limit
			"MJ/h",  // 56   EnergiMax            MJ/h      UR       1    Power high limit                                    0           99999999    Power high limit
			"bar",   // 57   TrykVFejl            BarA      UR            Fallback press. used on error                       0.6         80
			"°C",    // 58   TempVFejl            °C         R            Fallback temp. used on error                        -40         70
			"s",     // 59   DispOffTime          Sec.      UR            Turn off display after                              4           240
			"bar",   // 60   Pb                   BarA      UR       1 1  Base pressure                                       0.6         80          Base press.
			"°C",    // 61   Tb                   °C         R       1 1  Base temperature                                    -40         70          Base temp.
			"bar",   // 62   MaxPress             BarA      UR            Pressure range                                      0.6         80
			"bar",   // 63   MinPress             BarA      UR            Pressure range                                      0.6         80
			"°C",    // 64   MaxTemp              °C         R            Temperature range                                   -40         70
			"°C",    // 65   MinTemp              °C         R            Temperature range                                   -40         70
			"12",    // 66   AlarmTable           12                      Alarm table
			"96",    // 67   AlarmCntTable        96         0            Alarm cnt table
			"MJ/h",  // 68   EnergiFlow           MJ/h      U        1R0 1Power                                                                       Power
			"",      // 69   Alarmset                                     Set alarm reg in uniflo
			"",      // 70   TDeviceID                                R   Manufacture and type                                                        P. Sens. type
			"",      // 71   TPStyle                                  R   Tryksensor Pressure style
			"",      // 72   TPressRange                             1R   Range [bar]                                                                 Range [bar]
			"",      // 73   TSerialno                               1R   Serial no.                                                                  P. sens no.
			"",      // 74   TCalibDay                                R   Date of calibration                                2
			"",      // 75   TCalibMonth                              R   Date of calibration                                2
			"",      // 76   TCalibYear                               R   Date of calibration                                2
			"",      // 77   TPres1Bar                                R   Tryksensor Pressure signal at 1 bar at 20 deg. C
			"",      // 78   TPresFS                                  R   Tryksensor Pressure signal FS at 20 deg. C
			"",      // 79   TTemp                                    R   Tryksensor temperature signal at 20 deg. C
			"",      // 80   ResetAlarm                                   Reset alarm
			"",      // 81   ClrAlarm                                     Clear alarm
			"",      // 82   ADAmp                                        AD amplify
			"",      // 83   ClrEvent                                     Delete configuration log
			"",      // 84   LockTable                                    Lock correction table
			"",      // 85   UnLockTable                                  UnLock correction table
			"",      // 86   OpdateCorrTable                              Update correction table
			"",      // 87   OpdateGasData                                Update Gas data
			"",      // 88   Snapshot                                     Make a snapshot in Uniflo
			"",      // 89   PulsOut1En                      R            Puls out 1 enable
			"",      // 90   PulsOut2En                      R            Puls out 2 enable
			"",      // 91   Menufile                        R            Menu file
			"",      // 92   IntvAntal                                R   x
			"",      // 93   GSetup                                       Graphic display gain setup
			"",      // 94   CSetup                                       Graphic display contrast setup
			"MJ",    // 95   Energi               MJ        U             Energy                                              0           99999999
			"MJ",    // 96   EnergiI              MJ        U        1 01 Energy                                                                      Energy
			"MJ",    // 97   EnergiF              MJ        U        1 0  Energy dec.                                                                 Energy dec.
			"",      // 98   OptionCH                                     Options kort changed
			"",      // 99   TempSource                      R            Temperatur sensor sourse
			"",      // 100  PresSource                      R            Pressure sensor source
			"",      // 101  Alarm active 1-32                        R0  Alarm active 1-32
			"",      // 102  Alarm active 33-64                       R0  Alarm active 33-64
			"",      // 103  Alarm active 64-96                       R0  Alarm active 64-96
			"",      // 104  Alarm reg. 1-32                          R0  Alarm reg. 1-32
			"",      // 105  Alarm reg. 33-64                         R0  Alarm reg. 33-64
			"",      // 106  Alarm reg. 64-96                         R0  Alarm reg. 64-96
			"10",    // 107  IOTabel              10                  R   I/O - tabel
			"m3",    // 108  VolMeasured          m3                      Volume measured                                     0           99999999
			"",      // 109  TChecksum                                R   Pressure sensor check sum
			"20",    // 110  IntvalLogreg         20         R            Interval log registre
			"20",    // 111  DagsLogreg           20         R            24 hour log registre
			"20",    // 112  Snapshotreg          20         R            Snapshot log registre
			"20",    // 113  AlarmAktLogreg       20         R            Alarm triggered log registre
			"",      // 114  VerTyp                                  1R   Uniflo type and version                                                     Type/version
			"",      // 115  Installation                    R       1    Installation no.                                                            Installation no.
			"",      // 116  Maalernr                        R       1    Meter no.                                                                   Meter no.
			"",      // 117  MaalerSize                      R       1    Meter size                                                                  Meter size
			"",      // 118  Kunde                           R       1    Customer                                                                    Customer
			"",      // 119  Installationsdato               R       1    Date of installation                                                        Installation date
			"",      // 120  Counter                         R       1    Meter index                                                                 Meter index
			"",      // 121  Sag                             R       1    Project no.                                                                 Project no.
			"",      // 122  Serienr                         R       1    Uniflo serial no.                                                           Serial no.
			"",      // 123  Flowmaaler                      R       1    Manufacture and type                                                        Flow type
			"",      // 124  Tempmaaler                      R       1    Manufacture and type                                                        Temp type
			"",      // 125  IntvalLogDyb                    R            No. of logs
			"",      // 126  DagsLogDyb                      R            No. of logs
			"",      // 127  SnapshotDyb                     R            No. of logs
			"",      // 128  AlarmAktDyb                     R            No. of logs
			"",      // 129  IntvalLogBredde                 R            No. of log points
			"",      // 130  DagslogBredde                   R            No. of log points
			"",      // 131  SnapshotBredde                  R            No. of log points
			"",      // 132  AlarmAktBredde                  R            No. of log points
			"m3/h",  // 133  FlowUnCorr           m3/h               1R0 1Flow measured                                                               Flow meas.
			"mol%",  // 134  Methan                          R       1    Methane                                                                     Methane
			"mol%",  // 135  Nitrogen                        R       1    Nitrogen                                                                    Nitrogen
			"mol%",  // 136  CarbonDioxide                   R       1    CO2                                                                         CO2
			"mol%",  // 137  Ethan                           R       1    Ethane                                                                      Ethane
			"mol%",  // 138  Propan                          R       1    Propane                                                                     Propane
			"mol%",  // 139  Water                           R       1    Water                                                                       Water
			"mol%",  // 140  HydrogenSylfide                 R       1    Hydrg. Sul.                                                                 Hydrg. Sul.
			"mol%",  // 111  Hydrogen                        R       1    Hydrogen                                                                    Hydrogen
			"mol%",  // 142  CarbonMonoxide                  R       1    Carb. mo.                                                                   Carb. mo.
			"mol%",  // 143  Oxygen                          R       1    Oxygen                                                                      Oxygen
			"mol%",  // 144  iButan                          R       1    i-Butane                                                                    i-Butane
			"mol%",  // 145  nButan                          R       1    n-Butane                                                                    n-Butane
			"mol%",  // 146  iPentan                         R       1    i-Pentane                                                                   i-Pentane
			"mol%",  // 147  nPentan                         R       1    n-Pentane                                                                   n-Pentane
			"mol%",  // 148  nHexan                          R       1    n-Hexane                                                                    n-Hexane
			"mol%",  // 149  nHeptan                         R       1    n-Heptane                                                                   n-Heptane
			"mol%",  // 150  nOctan                          R       1    n-Octane                                                                    n-Octane
			"mol%",  // 151  nNontan                         R       1    n-Nonane                                                                    n-Nonane
			"mol%",  // 152  nDecan                          R       1    n-Decane                                                                    n-Decane
			"mol%",  // 153  Helium                          R       1    Helium                                                                      Helium
			"mol%",  // 154  Argon                           R       1    Argon                                                                       Argon
			"",      // 155  BeregningsMetode                R       1    Formular                                                                    Formular
			"",      // 156  GasComp                         R       1    Gas composition                                                             Gas comp.
			"",      // 157  RevTime                         R       1R   Time of rev.                                                                Comp. rev.
			"",      // 158  Density                         R       1    Density (rel.)                                      0.1         2           Density rel.
			"800",   // 159  Gastabel             800        R            Gas correction tabel
			"",      // 160  Password3                       R            Password level 3
			"",      // 161  Password2                       R            Password level 2
			"",      // 162  Password1                       R            Password level 1
			"",      // 163  LogReOrg                        R            Display mode
			"",      // 164  KorrF                                   1R0 1Corrections factor                                                          Corr. fact.
			"14",    // 165  IOVersion            14                  R   IO version
			"28",    // 166  IOSerienr            28                  R   IO serienr
			"",      // 167  HFCard                                   R   HF/Puls subtype
			"40",    // 168  SNTabel              40                  R   I/O - tabel
			"",      // 169  TempStregkode                           1    Temperture code                                                             Temperture code
			"°C",    // 170  ITempMin             °C         0        R1  Min. temp.
			"°C",    // 171  ITempMax             °C         0        R1  Max. temp.
			"bar",   // 172  ITrykMin             BarA      U0        R1  Min. Press.
			"bar",   // 173  ITrykMax             BarA      U0        R1  Max. Press.
			"m3/h",  // 174  IFlowUMin            m3/h       0        R1  Min. flow corr.
			"m3/h",  // 175  IFlowUMax            m3/h       0        R1  Max. flow corr.
			"Nm3/h", // 176  IFlowKMin            Nm3/h     U0        R1  Min. flow conv.
			"Nm3/h", // 177  IFlowKMax            Nm3/h     U0        R1  Max. flow conv.
			"°C",    // 178  DTempMin             °C         0        R2  Min. temp.
			"°C",    // 179  DTempMax             °C         0        R2  Max. temp.
			"bar",   // 180  DTrykMin             BarA      U0        R2  Min. Press.
			"bar",   // 181  DTrykMax             BarA      U0        R2  Max. Press.
			"m3/h",  // 182  DFlowUMin            m3/h       0        R2  Min. flow corr.
			"m3/h",  // 183  DFlowUMax            m3/h       0        R2  Max. flow corr.
			"Nm3/h", // 184  DFlowKMin            Nm3/h     U0        R2  Min. flow conv.
			"Nm3/h", // 185  DFlowKMax            Nm3/h     U0        R2  Max. flow conv.
			"MJ",    // 186  IEnergyFMin          MJ        U0        R1  Min. power
			"MJ",    // 187  IEnergyFMax          MJ        U0        R1  Max. power
			"MJ",    // 188  DEnergyFMin          MJ        U0        R2  Min. power
			"MJ",    // 189  DEnergyFMax          MJ        U0        R2  Max. power
			"m3",    // 190  VolMeasuredI         m3                 1 01 Vol. measured                                                               Vol. meas.
			"m3",    // 191  VolMeasuredF         m3                 1 0  Vol. measured dec.                                                          Vol. meas. dec
			"m3",    // 192  VolErr               m3                      Vol. meas. at error                                 0           99999999
			"m3",    // 193  VolErrI              m3                 1 01 Vol. meas. at error                                                         Vol. err.
			"m3",    // 194  VolErrF              m3                 1 0  Vol. meas. at err. dec.                                                     Vol. err. dec
			"MJ/Nm3",// 195  Heatvalue            MJ/nm3    UR       1    Superior heat value                                 19          48          S. heat value
			"",      // 196  C6Value                         R       1    C6+ value                                                                   C6+ value
			"",      // 197  C6Enable                        R       1    C6+                                                                         C6+ enable
			"",      // 198  ADTrykOffset                                 AD pressure offset                                  -128        127
			"",      // 199  ADTempTrykOffset                             AD pressure temperature offset                      -128        127
			"",      // 200 256  CountVmAtError                  R            Count Vm at Error
			"",      // 201 257  TZMode                          R            Corrector Type
			"bar",   // 202 258  TZTryk               bar A     UR            Pressure [bar A]                                    0.6         80
			"31768", // 203 259  Menu/Alarm           31768                   Menu og alarm text for grafisk display
			"",      // 204 260  NoOfSlaves                              0    Number of slaves                                                            Number of slaves
			"",      // 205 261  OpdataerEEProm                               Opdater EEProm
			"",      // 206 262  Alarmfile                       R            Alarm text file
			"",      // 207 263  No output                                   1No output
			"",      // 208 264  LockDisp                                     Lock graphic display
			"",      // 209 265  UnLockDisp                                   UnLock graphic display
			"m3",    // 210 266  VolCtrlI             m3                 1 01 Vol. control                                                                Vol. ctrl.
			"m3",    // 211 267  VolCtrlF             m3                 1 0  Vol. control dec.                                                           Vol. ctrl. dec
			"m3",    // 212  VolHourI             m3                 1R0  Current hour incr.                                                          Hour incr.
			"m3",    // 213  VolHourF             m3                 1R0  Current incr. dec.                                                          Hour incr. dec
			"",      // 214  MaxTime                                 1R0  1.Max hour incr. time                                                       1. Max incr. time
			"m3",    // 215  MaxVolI              m3                 1R0  1.Max hour incr.                                                            1. Max incr.
			"m3",    // 216  MaxVolF              m3                 1R0  1.Max hour incr. dec.                                                       1. Max incr. dec
			"",      // 217  MaxTime2                                1R0  2.Max hour incr. time                                                       2. Max time
			"m3",    // 218  MaxVolI2             m3                 1R0  2.max hour incr.                                                            2. Max incr.
			"m3",    // 219  MaxVolF2             m3                 1R0  2.max hour incr. dec.                                                       2. Max incr. dec
			"",      // 220  MaxTime3                                1R0  3.Max hour incr. time                                                       3. Max time
			"m3",    // 221  MaxVolI3             m3                 1R0  3.max hour incr.                                                            3. Max incr.
			"m3",    // 222  MaxVolF3             m3                 1R0  3.max hour incr. dec.                                                       3. Max incr. dec
			"",      // 223  PresureCalOP                            1    Pressure calibration operator                                               Press. cal. op.
			"",      // 224  TempCalOP                               1    Temperature calibration operator                                            Temp. cal. op.
			"",      // 225  EventlogIdx                              R   Indexpointer for Event log
			"",      // 226  AlarmlogIdx                              R   Indexpointer for Alarmlog log
			"s",     // 227  IntervallogState                R            Log interval
			"s",     // 228  MaalInterval         Sec.      UR       1    Interval of measurement                             0           254         Cycl. of meas.
			"",      // 229  DagslogTime                     R            Time of Day                                         0           24
			"",      // 230  DagslogMin                      R            Time of Day                                         0           60
			"",      // 231  IntervallogIdx                               Indexpointer for interval log
			"",      // 232  DagslogIdx                               R   Indexpointer for dagslog
			"",      // 233  SnapshotlogIdx                           R   Indexpointer for snapshot log
			"",      // 234  AlarmtriglogIdx                          R   Indexpointer for alarmaktiveret log
			"s",     // 235  DispTime             Sec.      UR            Change to display 1 after
			"",      // 236  MaxpulsError                    R            Max. pulse error
			"",      // 237  ChangePressSensor                            Pressure sensor is change
			"",      // 238  ChangeTempSensor                             Temperature sensor is change
			"",      // 239  MonthBredde                     R            No. of log points
			"",      // 240  MonthDyb                        R            No. of logs
			"20",    // 241  MonthLogreg          20         R            Month log registre
			"",      // 242  MonthlogIdx                              R   Indexpointer for Month log
			"",      // 243  Disptest                                     Display test
			"",      // 244  FirmwareUpdate                               Firmware update
			"",      // 245  FUPDATESTART                                 Firmware update start
			"",      // 246  FUPDATEEND                                   Firmware update end
			"",      // 247  FUPDATECANCEL                                Firmware update cancel
			"",      // 248  PRGChecksum                             1R   Program checksum                                                            Prg. chksum
			"30",    // 249  TempCorr             30                      Temperature calibretion tabel
			"60",    // 250  PressCorr            60                      Pressure calibretion tabel
			"",      // 251  ForceMesurement                              Force measurment
			"",      // 252  PresureCalTime                          1    Pressure calibration time                                                   Press. cal. Time
			"",      // 253  TempCalTime                             1    Temperature calibration time                                                Temp. cal. Time
			"",      // 254  ConvTableChecksum                       1R   Conversion table checksum                                                   Conv. table chksum
			"",      // 255  ConvTableDLLChecksum                    1R   DLL checksum                                                                DLL checksum
			"",      // 256
			"",      // 257
			"",      // 258
			"",      // 259
			"",      // 260
			"",      // 261
			"",      // 262
			"",      // 263
			"",      // 264
			"",      // 265
			"",      // 266
			"",      // 267
			"",      // 268
			"",      // 269
			"",      // 270
			"",      // 271
			"",      // 272
			"",      // 273
			"",      // 274
			"",      // 275
			"",      // 276

		};

		/* The HashMap will indicate if the register is cumulative and what the wrapValue is */
		public static HashMap CUMULATIVE_REGISTERS = new HashMap();
		static{
			buildCumulativeHashMap();
		}

		private static void buildCumulativeHashMap(){
			CUMULATIVE_REGISTERS.put(new Integer(32), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(42), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(43), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(44), "99999999");	// TODO, chech the wrapValue
			CUMULATIVE_REGISTERS.put(new Integer(45), "99999999");	// TODO, chech the wrapValue
			CUMULATIVE_REGISTERS.put(new Integer(46), "99999999");	// TODO, chech the wrapValue
			CUMULATIVE_REGISTERS.put(new Integer(47), "99999999");	// TODO, chech the wrapValue
			CUMULATIVE_REGISTERS.put(new Integer(95), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(96), "99999999");	// TODO, not sure, but in the manual you see a wrapValue
			CUMULATIVE_REGISTERS.put(new Integer(97), "99999999");	// TODO, not sure, but in the manual you see a wrapValue
			CUMULATIVE_REGISTERS.put(new Integer(108), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(190), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(191), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(192), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(193), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(194), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(210), "99999999");
			CUMULATIVE_REGISTERS.put(new Integer(211), "99999999");
			}

		/**
		 * Checks if current register is an accumulated value
		 * @param addressIndex - the number(index) of the register
		 * @return true if cumulative, false otherwise
		 */
		public static boolean isCumulative(int addressIndex){
			return CUMULATIVE_REGISTERS.containsKey(new Integer(addressIndex));
		}

		/**
		 * @param addressIndex - the number(index) of the register
		 * @return the wrapValue
		 */
		public static int getCumulativeWrapValue(int addressIndex){
			return Integer.parseInt((String)CUMULATIVE_REGISTERS.get(new Integer(addressIndex)));
		}
	}



}
