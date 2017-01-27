package com.energyict.protocolimpl.dlms.elster.ek2xx;

import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.utils.ProtocolUtils;

public class EK2xxEvents {

	private static final int RESTART_FLAG			= 0x00000001;	// 1
	// private static final int UNKNOWN1_FLAG		= 0x00000002;	// 2
	private static final int DATA_RESTORED_FLAG		= 0x00000004;	// 3
	private static final int POWERSUPPLY_FLAG		= 0x00000008;	// 4
	private static final int DATA_ERROR_FLAG		= 0x00000010;	// 5
	// private static final int UNKNOWN2_FLAG		= 0x00000020;	// 6
	// private static final int UNKNOWN3_FLAG		= 0x00000040;	// 7
	private static final int SETTINGS_ERROR_FLAG	= 0x00000080;	// 8
	private static final int BATTERY_LIVE_FLAG		= 0x00000100;	// 9
	private static final int REPAIR_MODE_FLAG		= 0x00000200;	// 10
	private static final int CLOCK_FLAG				= 0x00000400;	// 11
	// private static final int UNKNOWN4_FLAG		= 0x00000800;	// 12
	private static final int DATA_TRANSMISSION_FLAG	= 0x00001000;	// 13
	private static final int REMOTE_CLOCK_FLAG		= 0x00002000;	// 14
	private static final int BATTERY_MODE_FLAG		= 0x00004000;	// 15
	private static final int DIAPLAY_DST_FLAG		= 0x00008000;	// 16

	//	16=The displayed time is daylight saving time

	private EK2xxEvents() {}

	public static String getEventDescription(int protocolCode) {

		switch (protocolCode) {
		case 0x00001: return "Message 1 disappears from status 1 (Alarm)";
		case 0x00002: return "Message 1 disappears from status 2 (Alarm)";
		case 0x00003: return "Message 1 disappears from status 3 (Alarm)";
		case 0x00004: return "Message 1 disappears from status 4 (Alarm)";
		case 0x00005: return "Conversion factor (C) cannot be computed - disappears (Alarm)";
		case 0x00006: return "Alarm limit violated for temperature - disappears (Alarm)";
		case 0x00007: return "Alarm limit violated for pressure - disappears (Alarm)";
		case 0x00008: return "Compressibility cannot be computed - disappears (Alarm)";
		case 0x00009: return "Compressibility factor cannot be computed - disappears (Alarm)";
		case 0x00101: return "Encoder alarm disappears";
		case 0x00102: return "Message 2 disappears from status 2 (Alarm)";
		case 0x00103: return "Message 2 disappears from status 3 (Alarm)";
		case 0x00104: return "Message 2 disappears from status 4 (Alarm)";
		case 0x00105: return "No usable input value for temperature - disappears (Alarm)";
		case 0x00106: return "No usable input value for pressure - disappears (Alarm)";
		case 0x00107: return "Message 2 disappears from status 7 (Alarm)";
		case 0x00108: return "Message 2 disappears from status 8 (Alarm)";
		case 0x00109: return "Message 2 disappears from status 9 (Alarm)";
		case 0x00201: return "Message 3 disappears from status 1 (Warning)";
		case 0x00202: return "Message 3 disappears from status 2 (Warning)";
		case 0x00203: return "Message 3 disappears from status 3 (Warning)";
		case 0x00204: return "Message 3 disappears from status 4 (Warning)";
		case 0x00205: return "Message 3 disappears from status 5 (Warning)";
		case 0x00206: return "Message 3 disappears from status 6 (Warning)";
		case 0x00207: return "Message 3 disappears from status 7 (Warning)";
		case 0x00208: return "Message 3 disappears from status 8 (Warning)";
		case 0x00209: return "Message 3 disappears from status 9 (Warning)";
		case 0x00301: return "Output1: pulse buffer overloaded - disappears (Warning)";
		case 0x00302: return "Output2: pulse buffer overloaded - disappears (Warning)";
		case 0x00303: return "Output3: pulse buffer overloaded - disappears (Warning)";
		case 0x00304: return "Output4: pulse buffer overloaded - disappears (Warning)";
		case 0x00305: return "Message 4 disappears from status 5 (Warning)";
		case 0x00306: return "Message 4 disappears from status 6 (Warning)";
		case 0x00307: return "Message 4 disappears from status 7 (Warning)";
		case 0x00308: return "Message 4 disappears from status 8 (Warning)";
		case 0x00309: return "Message 4 disappears from status 9 (Warning)";
		case 0x00401: return "Message 5 disappears from status 1 (Warning)";
		case 0x00402: return "Deviation comparing pulses Input 2 - disappears (Warning)";
		case 0x00403: return "Message 5 disappears from status 3 (Warning)";
		case 0x00404: return "Message 5 disappears from status 4 (Warning)";
		case 0x00405: return "Message 5 disappears from status 5 (Warning)";
		case 0x00406: return "Message 5 disappears from status 6 (Warning)";
		case 0x00407: return "Message 5 disappears from status 7 (Warning)";
		case 0x00408: return "Message 5 disappears from status 8 (Warning)";
		case 0x00409: return "Message 5 disappears from status 9 (Warning)";
		case 0x00501: return "Warning limit power violated - disappears (Warning)";
		case 0x00502: return "Warning limit standard flow violated - disappears (Warning)";
		case 0x00503: return "Message 6 disappears from status 3 (Warning)";
		case 0x00504: return "Warning limit violated for  actaual flow - disappears";
		case 0x00505: return "Message 6 disappears from status 5 (Warning)";
		case 0x00506: return "Warning limit violated for temperature - disappears";
		case 0x00507: return "Warning limit violated for pressure - disappears";
		case 0x00508: return "Message 6 disappears from status 8 (Warning)";
		case 0x00509: return "Message 6 disappears from status 9 (Warning)";
		case 0x00601: return "Message 7 disappears from status 1 (Warning)";
		case 0x00602: return "Message 7 disappears from status 2 (Warning)";
		case 0x00603: return "Message 7 disappears from status 3 (Warning)";
		case 0x00604: return "Message 7 disappears from status 4 (Warning)";
		case 0x00605: return "Message 7 disappears from status 5 (Warning)";
		case 0x00606: return "Message 7 disappears from status 6 (Warning)";
		case 0x00607: return "Message 7 disappears from status 7 (Warning)";
		case 0x00608: return "Message 7 disappears from status 8 (Warning)";
		case 0x00609: return "Message 7 disappears from status 9 (Warning)";
		case 0x00701: return "Message 8 disappears from status 1 (Warning)";
		case 0x00702: return "Warning signal at input 2 disappears (Warning)";
		case 0x00703: return "Warning signal at input 3 disappears (Warning)";
		case 0x00704: return "Message 8 disappears from status 4 (Warning)";
		case 0x00705: return "Message 8 disappears from status 5 (Warning)";
		case 0x00706: return "Message 8 disappears from status 6 (Warning)";
		case 0x00707: return "Warning limit violated for pressure 2 - disappears";
		case 0x00708: return "Message 8 disappears from status 8 (Warning)";
		case 0x00709: return "Message 8 disappears from status 9 (Warning)";
		case 0x00801: return "Message 9 disappears from status 1 (Message)";
		case 0x00802: return "Message 9 disappears from status 2 (Message)";
		case 0x00803: return "Message 9 disappears from status 3 (Message)";
		case 0x00804: return "Message 9 disappears from status 4 (Message)";
		case 0x00805: return "Message 9 disappears from status 5 (Message)";
		case 0x00806: return "Message 9 disappears from status 6 (Message)";
		case 0x00807: return "Message 9 disappears from status 7 (Message)";
		case 0x00808: return "Message 9 disappears from status 8 (Message)";
		case 0x00809: return "Message 9 disappears from status 9 (Message)";
		case 0x00901: return "Message 10 disappears from status 1 (Message)";
		case 0x00902: return "Message 10 disappears from status 2 (Message)";
		case 0x00903: return "Message 10 disappears from status 3 (Message)";
		case 0x00904: return "Message 10 disappears from status 4 (Message)";
		case 0x00905: return "Temperature input not calibrated - disappears(Message)";
		case 0x00906: return "Pressure input not calibrated - disappears(Message)";
		case 0x00907: return "Message 10 disappears from status 7 (Message)";
		case 0x00908: return "Message 10 disappears from status 8 (Message)";
		case 0x00909: return "Message 10 disappears from status 9 (Message)";
		case 0x00A01: return "Encoder error disappears";
		case 0x00A02: return "Message 11 disappears from status 2 (Message)";
		case 0x00A03: return "Message 11 disappears from status 3 (Message)";
		case 0x00A04: return "Message 11 disappears from status 4 (Message)";
		case 0x00A05: return "Message 11 disappears from status 5 (Message)";
		case 0x00A06: return "Message 11 disappears from status 6 (Message)";
		case 0x00A07: return "Message 11 disappears from status 7 (Message)";
		case 0x00A08: return "Message 11 disappears from status 8 (Message)";
		case 0x00A09: return "Message 11 disappears from status 9 (Message)";
		case 0x00B01: return "Message 12 disappears from status 1 (Message)";
		case 0x00B02: return "Message 12 disappears from status 2 (Message)";
		case 0x00B03: return "Message 12 disappears from status 3 (Message)";
		case 0x00B04: return "Message 12 disappears from status 4 (Message)";
		case 0x00B05: return "Message 12 disappears from status 5 (Message)";
		case 0x00B06: return "Message 12 disappears from status 6 (Message)";
		case 0x00B07: return "Message 12 disappears from status 7 (Message)";
		case 0x00B08: return "Message 12 disappears from status 8 (Message)";
		case 0x00B09: return "Message 12 disappears from status 9 (Message)";
		case 0x00C01: return "Message 13 disappears from status 1 (Message)";
		case 0x00C02: return "Report signal on input 2 - disappears (Message)";
		case 0x00C03: return "Report signal on Input 3 - disappears (Message)";
		case 0x00C04: return "Message 13 disappears from status 4 (Message)";
		case 0x00C05: return "Message 13 disappears from status 5 (Message)";
		case 0x00C06: return "Message 13 disappears from status 6 (Message)";
		case 0x00C07: return "Message 13 disappears from status 7 (Message)";
		case 0x00C08: return "Message 13 disappears from status 8 (Message)";
		case 0x00C09: return "Message 13 disappears from status 9 (Message)";
		case 0x00D01: return "Calibration lock open - disappears (Message)";
		case 0x00D02: return "Manufacturer lock open - disappears (Message)";
		case 0x00D03: return "Supplier lock open - disappears (Message)";
		case 0x00D04: return "Customer lock open - disappears (Message)";
		case 0x00D05: return "Message 14 disappears from status 5 (Message)";
		case 0x00D06: return "Message 14 disappears from status 6 (Message)";
		case 0x00D07: return "Message 14 disappears from status 7 (Message)";
		case 0x00D08: return "Message 14 disappears from status 8 (Message)";
		case 0x00D09: return "Message 14 disappears from status 9 (Message)";
		case 0x00E01: return "Advanced call time window - disappears (Message)";
		case 0x00E02: return "Message 15 disappears from status 2 (Message)";
		case 0x00E03: return "Message 15 disappears from status 3 (Message)";
		case 0x00E04: return "Message 15 disappears from status 4 (Message)";
		case 0x00E05: return "Message 15 disappears from status 5 (Message)";
		case 0x00E06: return "Message 15 disappears from status 6 (Message)";
		case 0x00E07: return "Message 15 disappears from status 7 (Message)";
		case 0x00E08: return "Message 15 disappears from status 8 (Message)";
		case 0x00E09: return "Message 15 disappears from status 9 (Message)";
		case 0x00F01: return "Call time window 1 - end (Message)";
		case 0x00F02: return "Call time window 2 - end (Message)";
		case 0x00F03: return "Call time window 3 - end (Message)";
		case 0x00F04: return "Call time window 4 - end (Message)";
		case 0x00F05: return "Message 16 disappears from status 5 (Message)";
		case 0x00F06: return "Message 16 disappears from status 6 (Message)";
		case 0x00F07: return "Message 16 disappears from status 7 (Message)";
		case 0x00F08: return "Message 16 disappears from status 8 (Message)";
		case 0x00F09: return "Message 16 disappears from status 9 (Message)";
		case 0x01001: return "One message 1 disappears from overall status (Alarm)";
		case 0x01101: return "One message 2 disappears from overall status (Alarm)";
		case 0x01201: return "One message 3 disappears from overall status (Warning)";
		case 0x01301: return "One message 4 disappears from overall status (Warning)";
		case 0x01401: return "One message 5 disappears from overall status (Warning)";
		case 0x01501: return "One message 6 disappears from overall status (Warning)";
		case 0x01601: return "One message 7 disappears from overall status (Warning)";
		case 0x01701: return "One message 8 disappears from overall status (Warning)";
		case 0x01801: return "One message 9 disappears from overall status (Message)";
		case 0x01901: return "One message 10 disappears from overall status (Message)";
		case 0x01A01: return "One message 11 disappears from overall status (Message)";
		case 0x01B01: return "One message 12 disappears from overall status (Message)";
		case 0x01C01: return "One message 13 disappears from overall status (Message)";
		case 0x01D01: return "One message 14 disappears from overall status (Message)";
		case 0x01E01: return "One message 15 disappears from overall status (Message)";
		case 0x01F01: return "One message 16 disappears from overall status (Message)";
		case 0x01002: return "Restart of the device - disappears (Alarm)";
		case 0x01102: return "Message 2 in system status - disappears (Alarm)";
		case 0x01202: return "Data (clock, counter reading) has been restored - disappears (Warning)";
		case 0x01302: return "Internal power supply too less - disappears (Warning)";
		case 0x01402: return "Fatal data error - disappears (Warning)";
		case 0x01502: return "Message 6 in system status - disappears (Warning)";
		case 0x01602: return "Message 7 in system status - disappears (Warning)";
		case 0x01702: return "Setting error - disappears (Warning)";
		case 0x01802: return "Battery service life below limit - disappears (Message)";
		case 0x01902: return "Repair mode switched off (Message)";
		case 0x01A02: return "Clock not justified - disappears (Message)";
		case 0x01B02: return "Message 12 in system status - disappears (Message)";
		case 0x01C02: return "End of data transmission running (Message)";
		case 0x01D02: return "Remote clock setting - disappears (Message)";
		case 0x01E02: return "External power supply active (Message)";
		case 0x01F02: return "The displayed time is winter time (Message)";
		case 0x01003: return "Message 1 in system status - disappears";
		case 0x01103: return "Message 2 in system status - disappears";
		case 0x01203: return "Message 3 in system status - disappears";
		case 0x01303: return "Message 4 in system status - disappears";
		case 0x01403: return "Message 5 in system status - disappears";
		case 0x01503: return "Message 6 in system status - disappears";
		case 0x01603: return "Message 7 in system status - disappears";
		case 0x01703: return "Message 8 in system status - disappears";
		case 0x01803: return "Message 9 in system status - disappears";
		case 0x01903: return "Message 10 in system status - disappears";
		case 0x01A03: return "Message 11 in system status - disappears";
		case 0x01B03: return "Message 12 in system status - disappears";
		case 0x01C03: return "Message 13 in system status - disappears";
		case 0x01D03: return "Message 14 in system status - disappears";
		case 0x01E03: return "Time window for battery depassivating - end";
		case 0x01F03: return "Request Switch on Modem for SMS transmission - end";
		case 0x01004: return "Message 1 in programmable status 1 - disappears";
		case 0x01104: return "Message 2 in programmable status 1 - disappears";
		case 0x01204: return "Message 3 in programmable status 1 - disappears";
		case 0x01304: return "Message 4 in programmable status 1 - disappears";
		case 0x01404: return "Message 5 in programmable status 1 - disappears";
		case 0x01504: return "Message 6 in programmable status 1 - disappears";
		case 0x01604: return "Message 7 in programmable status 1 - disappears";
		case 0x01704: return "Message 8 in programmable status 1 - disappears";
		case 0x01804: return "Message 9 in programmable status 1 - disappears";
		case 0x01904: return "Message 10 in programmable status 1 - disappears";
		case 0x01A04: return "Message 11 in programmable status 1 - disappears";
		case 0x01B04: return "Message 12 in programmable status 1 - disappears";
		case 0x01C04: return "Message 13 in programmable status 1 - disappears";
		case 0x01D04: return "Message 14 in programmable status 1 - disappears";
		case 0x01E04: return "Message 15 in programmable status 1 - disappears";
		case 0x01F04: return "Message 16 in programmable status 1 - disappears";
		case 0x01005: return "Message 1 in programmable status 2 - disappears";
		case 0x01105: return "Message 2 in programmable status 2 - disappears";
		case 0x01205: return "Message 3 in programmable status 2 - disappears";
		case 0x01305: return "Message 4 in programmable status 2 - disappears";
		case 0x01405: return "Message 5 in programmable status 2 - disappears";
		case 0x01505: return "Message 6 in programmable status 2 - disappears";
		case 0x01605: return "Message 7 in programmable status 2 - disappears";
		case 0x01705: return "Message 8 in programmable status 2 - disappears";
		case 0x01805: return "Message 9 in programmable status 2 - disappears";
		case 0x01905: return "Message 10 in programmable status 2 - disappears";
		case 0x01A05: return "Message 11 in programmable status 2 - disappears";
		case 0x01B05: return "Message 12 in programmable status 2 - disappears";
		case 0x01C05: return "Message 13 in programmable status 2 - disappears";
		case 0x01D05: return "Message 14 in programmable status 2 - disappears";
		case 0x01E05: return "Message 15 in programmable status 2 - disappears";
		case 0x01F05: return "Message 16 in programmable status 2 - disappears";
		case 0x02001: return "Message 1 arises in status 1 (Alarm)";
		case 0x02002: return "Message 1 arises in status 2 (Alarm)";
		case 0x02003: return "Message 1 arises in status 3 (Alarm)";
		case 0x02004: return "Message 1 arises in status 4 (Alarm)";
		case 0x02005: return "Conversion factor (C) cannot be calculated - arises (Alarm)";
		case 0x02006: return "Alarm limit violated for temperature - arises (Alarm)";
		case 0x02007: return "Alarm limit violated for pressure - arises (Alarm)";
		case 0x02008: return "Compressibility cannot be computed - arises (Alarm)";
		case 0x02009: return "Compressibility factor cannot be computed - arises (Alarm)";
		case 0x02101: return "Encoder alarm arises";
		case 0x02102: return "Message 2 arises in status 2 (Alarm)";
		case 0x02103: return "Message 2 arises in status 3 (Alarm)";
		case 0x02104: return "Message 2 arises in status 4 (Alarm)";
		case 0x02105: return "No usable input value for temperature - arises (Alarm)";
		case 0x02106: return "No usable input value for pressure - arises (Alarm)";
		case 0x02107: return "Message 2 arises in status 7 (Alarm)";
		case 0x02108: return "Message 2 arises in status 8 (Alarm)";
		case 0x02109: return "Message 2 arises in status 9 (Alarm)";
		case 0x02201: return "Message 3 arises in status 1 (Warning)";
		case 0x02202: return "Message 3 arises in status 2 (Warning)";
		case 0x02203: return "Message 3 arises in status 3 (Warning)";
		case 0x02204: return "Message 3 arises in status 4 (Warning)";
		case 0x02205: return "Message 3 arises in status 5 (Warning)";
		case 0x02206: return "Message 3 arises in status 6 (Warning)";
		case 0x02207: return "Message 3 arises in status 7 (Warning)";
		case 0x02208: return "Message 3 arises in status 8 (Warning)";
		case 0x02209: return "Message 3 arises in status 9 (Warning)";
		case 0x02301: return "Output1: pulse buffer overloaded - arises(Warning)";
		case 0x02302: return "Output2: pulse buffer overloaded - arises(Warning)";
		case 0x02303: return "Output3: pulse buffer overloaded - arises(Warning)";
		case 0x02304: return "Output4: pulse buffer overloaded - arises(Warning)";
		case 0x02305: return "Message 4 arises in status 5 (Warning)";
		case 0x02306: return "Message 4 arises in status 6 (Warning)";
		case 0x02307: return "Message 4 arises in status 7 (Warning)";
		case 0x02308: return "Message 4 arises in status 8 (Warning)";
		case 0x02309: return "Message 4 arises in status 9 (Warning)";
		case 0x02401: return "Message 5 arises in status 1 (Warning)";
		case 0x02402: return "Deviation comparing pulses Input 2 - arises (Warning)";
		case 0x02403: return "Message 5 arises in status 3 (Warning)";
		case 0x02404: return "Message 5 arises in status 4 (Warning)";
		case 0x02405: return "Message 5 arises in status 5 (Warning)";
		case 0x02406: return "Message 5 arises in status 6 (Warning)";
		case 0x02407: return "Message 5 arises in status 7 (Warning)";
		case 0x02408: return "Message 5 arises in status 8 (Warning)";
		case 0x02409: return "Message 5 arises in status 9 (Warning)";
		case 0x02501: return "Warning limit power violated - arises (Warning)";
		case 0x02502: return "Warning limit standard flow violated - arises (Warning)";
		case 0x02503: return "Message 6 arises in status 3 (Warning)";
		case 0x02504: return "Warning limit violated for  actaual flow - arises (Warning)";
		case 0x02505: return "Message 6 arises in status 5 (Warning)";
		case 0x02506: return "Warning limit violated for temperature - arises (Warning)";
		case 0x02507: return "Warning limit violated for pressure - arises (Warning)";
		case 0x02508: return "Message 6 arises in status 8 (Warning)";
		case 0x02509: return "Message 6 arises in status 9 (Warning)";
		case 0x02601: return "Message 7 arises in status 1 (Warning)";
		case 0x02602: return "Message 7 arises in status 2 (Warning)";
		case 0x02603: return "Message 7 arises in status 3 (Warning)";
		case 0x02604: return "Message 7 arises in status 4 (Warning)";
		case 0x02605: return "Message 7 arises in status 5 (Warning)";
		case 0x02606: return "Message 7 arises in status 6 (Warning)";
		case 0x02607: return "Message 7 arises in status 7 (Warning)";
		case 0x02608: return "Message 7 arises in status 8 (Warning)";
		case 0x02609: return "Message 7 arises in status 9 (Warning)";
		case 0x02701: return "Message 8 arises in status 1 (Warning)";
		case 0x02702: return "Warning signal on input 2 arises (Warning)";
		case 0x02703: return "Warning signal on input 3 arises (Warning)";
		case 0x02704: return "Message 8 arises in status 4 (Warning)";
		case 0x02705: return "Message 8 arises in status 5 (Warning)";
		case 0x02706: return "Message 8 arises in status 6 (Warning)";
		case 0x02707: return "Warning limit violated for pressure 2 - arises";
		case 0x02708: return "Message 8 arises in status 8 (Warning)";
		case 0x02709: return "Message 8 arises in status 9 (Warning)";
		case 0x02801: return "Message 9 arises in status 1 (Message)";
		case 0x02802: return "Message 9 arises in status 2 (Message)";
		case 0x02803: return "Message 9 arises in status 3 (Message)";
		case 0x02804: return "Message 9 arises in status 4 (Message)";
		case 0x02805: return "Message 9 arises in status 5 (Message)";
		case 0x02806: return "Message 9 arises in status 6 (Message)";
		case 0x02807: return "Message 9 arises in status 7 (Message)";
		case 0x02808: return "Message 9 arises in status 8 (Message)";
		case 0x02809: return "Message 9 arises in status 9 (Message)";
		case 0x02901: return "Message 10 arises in status 1 (Message)";
		case 0x02902: return "Message 10 arises in status 2 (Message)";
		case 0x02903: return "Message 10 arises in status 3 (Message)";
		case 0x02904: return "Message 10 arises in status 4 (Message)";
		case 0x02905: return "Temperature input not calibrated - arises (Message)";
		case 0x02906: return "Pressure input not calibrated - arises (Message)";
		case 0x02907: return "Message 10 arises in status 7 (Message)";
		case 0x02908: return "Message 10 arises in status 8 (Message)";
		case 0x02909: return "Message 10 arises in status 9 (Message)";
		case 0x02A01: return "Encoder error arises";
		case 0x02A02: return "Message 11 arises in status 2 (Message)";
		case 0x02A03: return "Message 11 arises in status 3 (Message)";
		case 0x02A04: return "Message 11 arises in status 4 (Message)";
		case 0x02A05: return "Message 11 arises in status 5 (Message)";
		case 0x02A06: return "Message 11 arises in status 6 (Message)";
		case 0x02A07: return "Message 11 arises in status 7 (Message)";
		case 0x02A08: return "Message 11 arises in status 8 (Message)";
		case 0x02A09: return "Message 11 arises in status 9 (Message)";
		case 0x02B01: return "Message 12 arises in status 1 (Message)";
		case 0x02B02: return "Message 12 arises in status 2 (Message)";
		case 0x02B03: return "Message 12 arises in status 3 (Message)";
		case 0x02B04: return "Message 12 arises in status 4 (Message)";
		case 0x02B05: return "Message 12 arises in status 5 (Message)";
		case 0x02B06: return "Message 12 arises in status 6 (Message)";
		case 0x02B07: return "Message 12 arises in status 7 (Message)";
		case 0x02B08: return "Message 12 arises in status 8 (Message)";
		case 0x02B09: return "Message 12 arises in status 9 (Message)";
		case 0x02C01: return "Message 13 arises in status 1 (Message)";
		case 0x02C02: return "Report signal on Input 2 - arises (Message)";
		case 0x02C03: return "Report signal on Input 3 - arises (Message)";
		case 0x02C04: return "Message 13 arises in status 4 (Message)";
		case 0x02C05: return "Message 13 arises in status 5 (Message)";
		case 0x02C06: return "Message 13 arises in status 6 (Message)";
		case 0x02C07: return "Message 13 arises in status 7 (Message)";
		case 0x02C08: return "Message 13 arises in status 8 (Message)";
		case 0x02C09: return "Message 13 arises in status 9 (Message)";
		case 0x02D01: return "Calibration lock open - arises (Message)";
		case 0x02D02: return "Manufacturer lock open - arises (Message)";
		case 0x02D03: return "Supplier lock open - arises (Message)";
		case 0x02D04: return "Customer lock open - arises (Message)";
		case 0x02D05: return "Message 14 arises in status 5 (Message)";
		case 0x02D06: return "Message 14 arises in status 6 (Message)";
		case 0x02D07: return "Message 14 arises in status 7 (Message)";
		case 0x02D08: return "Message 14 arises in status 8 (Message)";
		case 0x02D09: return "Message 14 arises in status 9 (Message)";
		case 0x02E01: return "Advanced call time window - arises (Message)";
		case 0x02E02: return "Message 15 arises in status 2 (Message)";
		case 0x02E03: return "Message 15 arises in status 3 (Message)";
		case 0x02E04: return "Message 15 arises in status 4 (Message)";
		case 0x02E05: return "Message 15 arises in status 5 (Message)";
		case 0x02E06: return "Message 15 arises in status 6 (Message)";
		case 0x02E07: return "Message 15 arises in status 7 (Message)";
		case 0x02E08: return "Message 15 arises in status 8 (Message)";
		case 0x02E09: return "Message 15 arises in status 9 (Message)";
		case 0x02F01: return "Call time window 1 - start (Message)";
		case 0x02F02: return "Call time window 2 - start (Message)";
		case 0x02F03: return "Call time window 3 - start (Message)";
		case 0x02F04: return "Call time window 4 - start (Message)";
		case 0x02F05: return "Message 16 arises in status 5 (Message)";
		case 0x02F06: return "Message 16 arises in status 6 (Message)";
		case 0x02F07: return "Message 16 arises in status 7 (Message)";
		case 0x02F08: return "Message 16 arises in status 8 (Message)";
		case 0x02F09: return "Message 16 arises in status 9 (Message)";
		case 0x03001: return "Any message 1 arises in overall status (Alarm)";
		case 0x03101: return "Any message 2 arises in overall status (Alarm)";
		case 0x03201: return "Any message 3 arises in overall status (Warning)";
		case 0x03301: return "Any message 4 arises in overall status (Warning)";
		case 0x03401: return "Any message 5 arises in overall status (Warning)";
		case 0x03501: return "Any message 6 arises in overall status (Warning)";
		case 0x03601: return "Any message 7 arises in overall status (Warning)";
		case 0x03701: return "Any message 8 arises in overall status (Warning)";
		case 0x03801: return "Any message 9 arises in overall status (Message)";
		case 0x03901: return "Any message 10 arises in overall status (Message)";
		case 0x03A01: return "Any message 11 arises in overall status (Message)";
		case 0x03B01: return "Any message 12 arises in overall status (Message)";
		case 0x03C01: return "Any message 13 arises in overall status (Message)";
		case 0x03D01: return "Any message 14 arises in overall status (Message)";
		case 0x03E01: return "Any message 15 arises in overall status (Info)";
		case 0x03F01: return "Any message 16 arises in overall status (Info)";
		case 0x03002: return "Restart of the device - arises (Alarm)";
		case 0x03102: return "Message 2 in system status - arises (Alarm)";
		case 0x03202: return "Data (clock, counter reading) has been restored - arises (Warning)";
		case 0x03302: return "Internal power supply too less - arises (Warning)";
		case 0x03402: return "Fatal data error - arises (Warning)";
		case 0x03502: return "Message 6 in system status - arises (Warning)";
		case 0x03602: return "Message 7 in system status - arises (Warning)";
		case 0x03702: return "Setting error - arises (Warning)";
		case 0x03802: return "Battery service life below limit - arises (Message)";
		case 0x03902: return "Repair mode switched on (Message)";
		case 0x03A02: return "Clock not justified - arises (Message)";
		case 0x03B02: return "Message 12 in system status - arises (Message)";
		case 0x03C02: return "Start of data transmission running (Message)";
		case 0x03D02: return "Remote clock setting - arises (Message)";
		case 0x03E02: return "Device under battery power - arises (Message)";
		case 0x03F02: return "The displayed time is summer time (Message)";
		case 0x03003: return "Message 1 in system status - arises";
		case 0x03103: return "Message 2 in system status - arises";
		case 0x03203: return "Message 3 in system status - arises";
		case 0x03303: return "Message 4 in system status - arises";
		case 0x03403: return "Message 5 in system status - arises";
		case 0x03503: return "Message 6 in system status - arises";
		case 0x03603: return "Message 7 in system status - arises";
		case 0x03703: return "Message 8 in system status - arises";
		case 0x03803: return "Message 9 in system status - arises";
		case 0x03903: return "Message 10 in system status - arises";
		case 0x03A03: return "Message 11 in system status - arises";
		case 0x03B03: return "Message 12 in system status - arises";
		case 0x03C03: return "Message 13 in system status - arises";
		case 0x03D03: return "Message 14 in system status - arises";
		case 0x03E03: return "Time window for battery depassivating - start";
		case 0x03F03: return "Request Switch on Modem for SMS transmission - start";
		case 0x03004: return "Message 1 in programmable status 1 - arises";
		case 0x03104: return "Message 2 in programmable status 1 - arises";
		case 0x03204: return "Message 3 in programmable status 1 - arises";
		case 0x03304: return "Message 4 in programmable status 1 - arises";
		case 0x03404: return "Message 5 in programmable status 1 - arises";
		case 0x03504: return "Message 6 in programmable status 1 - arises";
		case 0x03604: return "Message 7 in programmable status 1 - arises";
		case 0x03704: return "Message 8 in programmable status 1 - arises";
		case 0x03804: return "Message 9 in programmable status 1 - arises";
		case 0x03904: return "Message 10 in programmable status 1 - arises";
		case 0x03A04: return "Message 11 in programmable status 1 - arises";
		case 0x03B04: return "Message 12 in programmable status 1 - arises";
		case 0x03C04: return "Message 13 in programmable status 1 - arises";
		case 0x03D04: return "Message 14 in programmable status 1 - arises";
		case 0x03E04: return "Message 15 in programmable status 1 - arises";
		case 0x03F04: return "Message 16 in programmable status 1 - arises";
		case 0x03005: return "Message 1 in programmable status 2 - arises";
		case 0x03105: return "Message 2 in programmable status 2 - arises";
		case 0x03205: return "Message 3 in programmable status 2 - arises";
		case 0x03305: return "Message 4 in programmable status 2 - arises";
		case 0x03405: return "Message 5 in programmable status 2 - arises";
		case 0x03505: return "Message 6 in programmable status 2 - arises";
		case 0x03605: return "Message 7 in programmable status 2 - arises";
		case 0x03705: return "Message 8 in programmable status 2 - arises";
		case 0x03805: return "Message 9 in programmable status 2 - arises";
		case 0x03905: return "Message 10 in programmable status 2 - arises";
		case 0x03A05: return "Message 11 in programmable status 2 - arises";
		case 0x03B05: return "Message 12 in programmable status 2 - arises";
		case 0x03C05: return "Message 13 in programmable status 2 - arises";
		case 0x03D05: return "Message 14 in programmable status 2 - arises";
		case 0x03E05: return "Message 15 in programmable status 2 - arises";
		case 0x03F05: return "Message 16 in programmable status 2 - arises";
		case 0x04101: return "Message 1 or 2 disappears from status 1";
		case 0x04201: return "Any message of 1 till 3 disappears from status 1";
		case 0x04301: return "Any message of 1 till 4 disappears from status 1";
		case 0x04401: return "Any message of 1 till 5 disappears from status 1";
		case 0x04501: return "Any message of 1 till 6 disappears from status 1";
		case 0x04601: return "Any message of 1 till 7 disappears from status 1";
		case 0x04701: return "Any message of 1 till 8 disappears from status 1";
		case 0x04801: return "Any message of 1 till 9 disappears from status 1";
		case 0x04901: return "Any message of 1 till 10 disappears from status 1";
		case 0x04A01: return "Any message of 1 till 11 disappears from status 1";
		case 0x04B01: return "Any message of 1 till 12 disappears from status 1";
		case 0x04C01: return "Any message of 1 till 13 disappears from status 1";
		case 0x04D01: return "Any message of 1 till 14 disappears from status 1";
		case 0x04E01: return "Any message of 1 till 15 disappears from status 1";
		case 0x04F01: return "Any message of 1 till 16 disappears from status 1";
		case 0x04102: return "Any message of 1 till 2 disappears from status 2";
		case 0x04202: return "Any message of 1 till 3 disappears from status 2";
		case 0x04302: return "Any message of 1 till 4 disappears from status 2";
		case 0x04402: return "Any message of 1 till 5 disappears from status 2";
		case 0x04502: return "Any message of 1 till 6 disappears from status 2";
		case 0x04602: return "Any message of 1 till 7 disappears from status 2";
		case 0x04702: return "Any message of 1 till 8 disappears from status 2";
		case 0x04802: return "Any message of 1 till 9 disappears from status 2";
		case 0x04902: return "Any message of 1 till 10 disappears from status 2";
		case 0x04A02: return "Any message of 1 till 11 disappears from status 2";
		case 0x04B02: return "Any message of 1 till 12 disappears from status 2";
		case 0x04C02: return "Any message of 1 till 13 disappears from status 2";
		case 0x04D02: return "Any message of 1 till 14 disappears from status 2";
		case 0x04E02: return "Any message of 1 till 15 disappears from status 2";
		case 0x04F02: return "Any message of 1 till 16 disappears from status 2";
		case 0x04103: return "Any message of 1 till 2 disappears from status 3";
		case 0x04203: return "Any message of 1 till 3 disappears from status 3";
		case 0x04303: return "Any message of 1 till 4 disappears from status 3";
		case 0x04403: return "Any message of 1 till 5 disappears from status 3";
		case 0x04503: return "Any message of 1 till 6 disappears from status 3";
		case 0x04603: return "Any message of 1 till 7 disappears from status 3";
		case 0x04703: return "Any message of 1 till 8 disappears from status 3";
		case 0x04803: return "Any message of 1 till 9 disappears from status 3";
		case 0x04903: return "Any message of 1 till 10 disappears from status 3";
		case 0x04A03: return "Any message of 1 till 11 disappears from status 3";
		case 0x04B03: return "Any message of 1 till 12 disappears from status 3";
		case 0x04C03: return "Any message of 1 till 13 disappears from status 3";
		case 0x04D03: return "Any message of 1 till 14 disappears from status 3";
		case 0x04E03: return "Any message of 1 till 15 disappears from status 3";
		case 0x04F03: return "Any message of 1 till 16 disappears from status 3";
		case 0x04104: return "Any message of 1 till 2 disappears from status 4";
		case 0x04204: return "Any message of 1 till 3 disappears from status 4";
		case 0x04304: return "Any message of 1 till 4 disappears from status 4";
		case 0x04404: return "Any message of 1 till 5 disappears from status 4";
		case 0x04504: return "Any message of 1 till 6 disappears from status 4";
		case 0x04604: return "Any message of 1 till 7 disappears from status 4";
		case 0x04704: return "Any message of 1 till 8 disappears from status 4";
		case 0x04804: return "Any message of 1 till 9 disappears from status 4";
		case 0x04904: return "Any message of 1 till 10 disappears from status 4";
		case 0x04A04: return "Any message of 1 till 11 disappears from status 4";
		case 0x04B04: return "Any message of 1 till 12 disappears from status 4";
		case 0x04C04: return "Any message of 1 till 13 disappears from status 4";
		case 0x04D04: return "Any message of 1 till 14 disappears from status 4";
		case 0x04E04: return "Any message of 1 till 15 disappears from status 4";
		case 0x04F04: return "Any message of 1 till 16 disappears from status 4";
		case 0x04105: return "Any message of 1 till 2 in status 5 disappears ";
		case 0x04205: return "Any message of 1 till 3 in status 5 disappears ";
		case 0x04305: return "Any message of 1 till 4 in status 5 disappears ";
		case 0x04405: return "Any message of 1 till 5 in status 5 disappears ";
		case 0x04505: return "Any message of 1 till 6 in status 5 disappears ";
		case 0x04605: return "Any message of 1 till 7 in status 5 disappears ";
		case 0x04705: return "Any message of 1 till 8 in status 5 disappears ";
		case 0x04805: return "Any message of 1 till 9 in status 5 disappears ";
		case 0x04905: return "Any message of 1 till 10 in status 5 disappears ";
		case 0x04A05: return "Any message of 1 till 11 in status 5 disappears";
		case 0x04B05: return "Any message of 1 till 12 in status 5 disappears";
		case 0x04C05: return "Any message of 1 till 13 in status 5 disappears";
		case 0x04D05: return "Any message of 1 till 14 in status 5 disappears";
		case 0x04E05: return "Any message of 1 till 15 in status 5 disappears";
		case 0x04F05: return "Any message of 1 till 16 in status 5 disappears";
		case 0x04106: return "Any message of 1 till 2 in status 6 disappears";
		case 0x04206: return "Any message of 1 till 3 in status 6 disappears";
		case 0x04306: return "Any message of 1 till 4 in status 6 disappears";
		case 0x04406: return "Any message of 1 till 5 in status 6 disappears";
		case 0x04506: return "Any message of 1 till 6 in status 6 disappears";
		case 0x04606: return "Any message of 1 till 7 in status 6 disappears";
		case 0x04706: return "Any message of 1 till 8 in status 6 disappears";
		case 0x04806: return "Any message of 1 till 9 in status 6 disappears";
		case 0x04906: return "Any message of 1 till 10 in status 6 disappears";
		case 0x04A06: return "Any message of 1 till 11 in status 6 disappears";
		case 0x04B06: return "Any message of 1 till 12 in status 6 disappears";
		case 0x04C06: return "Any message of 1 till 13 in status 6 disappears";
		case 0x04D06: return "Any message of 1 till 14 in status 6 disappears";
		case 0x04E06: return "Any message of 1 till 15 in status 6 disappears";
		case 0x04F06: return "Any message of 1 till 16 in status 6 disappears";
		case 0x04107: return "Any message of 1 till 2 in status 7 disappears";
		case 0x04207: return "Any message of 1 till 3 in status 7 disappears";
		case 0x04307: return "Any message of 1 till 4 in status 7 disappears";
		case 0x04407: return "Any message of 1 till 5 in status 7 disappears";
		case 0x04507: return "Any message of 1 till 6 in status 7 disappears";
		case 0x04607: return "Any message of 1 till 7 in status 7 disappears";
		case 0x04707: return "Any message of 1 till 8 in status 7 disappears";
		case 0x04807: return "Any message of 1 till 9 in status 7 disappears";
		case 0x04907: return "Any message of 1 till 10 in status 7 disappears";
		case 0x04A07: return "Any message of 1 till 11 in status 7 disappears";
		case 0x04B07: return "Any message of 1 till 12 in status 7 disappears";
		case 0x04C07: return "Any message of 1 till 13 in status 7 disappears";
		case 0x04D07: return "Any message of 1 till 14 in status 7 disappears";
		case 0x04E07: return "Any message of 1 till 15 in status 7 disappears";
		case 0x04F07: return "Any message of 1 till 16 in status 7 disappears";
		case 0x04108: return "Any message of 1 till 2 in status 8 disappears";
		case 0x04208: return "Any message of 1 till 3 in status 8 disappears";
		case 0x04308: return "Any message of 1 till 4 in status 8 disappears";
		case 0x04408: return "Any message of 1 till 5 in status 8 disappears";
		case 0x04508: return "Any message of 1 till 6 in status 8 disappears";
		case 0x04608: return "Any message of 1 till 7 in status 8 disappears";
		case 0x04708: return "Any message of 1 till 8 in status 8 disappears";
		case 0x04808: return "Any message of 1 till 9 in status 8 disappears";
		case 0x04908: return "Any message of 1 till 10 in status 8 disappears";
		case 0x04A08: return "Any message of 1 till 11 in status 8 disappears";
		case 0x04B08: return "Any message of 1 till 12 in status 8 disappears";
		case 0x04C08: return "Any message of 1 till 13 in status 8 disappears";
		case 0x04D08: return "Any message of 1 till 14 in status 8 disappears";
		case 0x04E08: return "Any message of 1 till 15 in status 8 disappears";
		case 0x04F08: return "Any message of 1 till 16 in status 8 disappears";
		case 0x04109: return "Any message of 1 till 2 in status 9 disappears";
		case 0x04209: return "Any message of 1 till 3 in status 9 disappears";
		case 0x04309: return "Any message of 1 till 4 in status 9 disappears";
		case 0x04409: return "Any message of 1 till 5 in status 9 disappears";
		case 0x04509: return "Any message of 1 till 6 in status 9 disappears";
		case 0x04609: return "Any message of 1 till 7 in status 9 disappears";
		case 0x04709: return "Any message of 1 till 8 in status 9 disappears";
		case 0x04809: return "Any message of 1 till 9 in status 9 disappears";
		case 0x04909: return "Any message of 1 till 10 in status 9 disappears";
		case 0x04A09: return "Any message of 1 till 11 in status 9 disappears";
		case 0x04B09: return "Any message of 1 till 12 in status 9 disappears";
		case 0x04C09: return "Any message of 1 till 13 in status 9 disappears";
		case 0x04D09: return "Any message of 1 till 14 in status 9 disappears";
		case 0x04E09: return "Any message of 1 till 15 in status 9 disappears";
		case 0x04F09: return "Any message of 1 till 16 in status 9 disappears";
		case 0x05002: return "Any message of 1 till 1 disappears from system status";
		case 0x05102: return "Any message of 1 till 2 disappears from system status";
		case 0x05202: return "Any message of 1 till 3 disappears from system status";
		case 0x05302: return "Any message of 1 till 4 disappears from system status";
		case 0x05402: return "Any message of 1 till 5 disappears from system status";
		case 0x05502: return "Any message of 1 till 6 disappears from system status";
		case 0x05602: return "Any message of 1 till 7 disappears from system status";
		case 0x05702: return "Any message of 1 till 8 disappears from system status";
		case 0x05802: return "Any message of 1 till 9 disappears from system status";
		case 0x05902: return "Any message of 1 till 10 disappears from system status";
		case 0x05A02: return "Any message of 1 till 11 disappears from system status";
		case 0x05B02: return "Any message of 1 till 12 disappears from system status";
		case 0x05C02: return "Any message of 1 till 13 disappears from system status";
		case 0x05D02: return "Any message of 1 till 14 disappears from system status";
		case 0x05E02: return "Any message of 1 till 15 disappears from system status";
		case 0x05F02: return "Any message of 1 till 16 disappears from system status";
		case 0x05001: return "Any message of 1 till 1 disappears from overall status";
		case 0x05101: return "Any message of 1 till 2 disappears from overall status";
		case 0x05201: return "Any message of 1 till 3 disappears from overall status";
		case 0x05301: return "Any message of 1 till 4 disappears from overall status";
		case 0x05401: return "Any message of 1 till 5 disappears from overall status";
		case 0x05501: return "Any message of 1 till 6 disappears from overall status";
		case 0x05601: return "Any message of 1 till 7 disappears from overall status";
		case 0x05701: return "Any message of 1 till 8 disappears from overall status";
		case 0x05801: return "Any message of 1 till 9 disappears from overall status";
		case 0x05901: return "Any message of 1 till 10 disappears from overall status";
		case 0x05A01: return "Any message of 1 till 11 disappears from overall status";
		case 0x05B01: return "Any message of 1 till 12 disappears from overall status";
		case 0x05C01: return "Any message of 1 till 13 disappears from overall status";
		case 0x05D01: return "Any message of 1 till 14 disappears from overall status";
		case 0x05E01: return "Any message of 1 till 15 disappears from overall status";
		case 0x05F01: return "Any message of 1 till 16 disappears from overall status";
		case 0x06101: return "Message 1 or 2 arises in status 1";
		case 0x06201: return "Any message of 1 till 3 arises in status 1";
		case 0x06301: return "Any message of 1 till 4 arises in status 1";
		case 0x06401: return "Any message of 1 till 5 arises in status 1";
		case 0x06501: return "Any message of 1 till 6 arises in status 1";
		case 0x06601: return "Any message of 1 till 7 arises in status 1";
		case 0x06701: return "Any message of 1 till 8 arises in status 1";
		case 0x06801: return "Any message of 1 till 9 arises in status 1";
		case 0x06901: return "Any message of 1 till 10 arises in status 1";
		case 0x06A01: return "Any message of 1 till 11 arises in status 1";
		case 0x06B01: return "Any message of 1 till 12 arises in status 1";
		case 0x06C01: return "Any message of 1 till 13 arises in status 1";
		case 0x06D01: return "Any message of 1 till 14 arises in status 1";
		case 0x06E01: return "Any message of 1 till 15 arises in status 1";
		case 0x06F01: return "Any message of 1 till 16 arises in status 1";
		case 0x06102: return "Message 1 or 2 arises in status 2";
		case 0x06202: return "Any message of 1 till 3 arises in status 2";
		case 0x06302: return "Any message of 1 till 4 arises in status 2";
		case 0x06402: return "Any message of 1 till 5 arises in status 2";
		case 0x06502: return "Any message of 1 till 6 arises in status 2";
		case 0x06602: return "Any message of 1 till 7 arises in status 2";
		case 0x06702: return "Any message of 1 till 8 arises in status 2";
		case 0x06802: return "Any message of 1 till 9 arises in status 2";
		case 0x06902: return "Any message of 1 till 10 arises in status 2";
		case 0x06A02: return "Any message of 1 till 11 arises in status 2";
		case 0x06B02: return "Any message of 1 till 12 arises in status 2";
		case 0x06C02: return "Any message of 1 till 13 arises in status 2";
		case 0x06D02: return "Any message of 1 till 14 arises in status 2";
		case 0x06E02: return "Any message of 1 till 15 arises in status 2";
		case 0x06F02: return "Any message of 1 till 16 arises in status 2";
		case 0x06103: return "Message of 1 or 2 arises in status 3";
		case 0x06203: return "Any message of 1 till 3 arises in status 3";
		case 0x06303: return "Any message of 1 till 4 arises in status 3";
		case 0x06403: return "Any message of 1 till 5 arises in status 3";
		case 0x06503: return "Any message of 1 till 6 arises in status 3";
		case 0x06603: return "Any message of 1 till 7 arises in status 3";
		case 0x06703: return "Any message of 1 till 8 arises in status 3";
		case 0x06803: return "Any message of 1 till 9 arises in status 3";
		case 0x06903: return "Any message of 1 till 10 arises in status 3";
		case 0x06A03: return "Any message of 1 till 11 arises in status 3";
		case 0x06B03: return "Any message of 1 till 12 arises in status 3";
		case 0x06C03: return "Any message of 1 till 13 arises in status 3";
		case 0x06D03: return "Any message of 1 till 14 arises in status 3";
		case 0x06E03: return "Any message of 1 till 15 arises in status 3";
		case 0x06F03: return "Any message of 1 till 16 arises in status 3";
		case 0x06104: return "Message 1 or 2 arises in status 4";
		case 0x06204: return "Any message of 1 till 3 arises in status 4";
		case 0x06304: return "Any message of 1 till 4 arises in status 4";
		case 0x06404: return "Any message of 1 till 5 arises in status 4";
		case 0x06504: return "Any message of 1 till 6 arises in status 4";
		case 0x06604: return "Any message of 1 till 7 arises in status 4";
		case 0x06704: return "Any message of 1 till 8 arises in status 4";
		case 0x06804: return "Any message of 1 till 9 arises in status 4";
		case 0x06904: return "Any message of 1 till 10 arises in status 4";
		case 0x06A04: return "Any message of 1 till 11 arises in status 4";
		case 0x06B04: return "Any message of 1 till 12 arises in status 4";
		case 0x06C04: return "Any message of 1 till 13 arises in status 4";
		case 0x06D04: return "Any message of 1 till 14 arises in status 4";
		case 0x06E04: return "Any message of 1 till 15 arises in status 4";
		case 0x06F04: return "Any message of 1 till 16 arises in status 4";
		case 0x06105: return "Message 1 or 2 in status 5 arises";
		case 0x06205: return "Any message of 1 till 3 in status 5 arises";
		case 0x06305: return "Any message of 1 till 4 in status 5 arises";
		case 0x06405: return "Any message of 1 till 5 in status 5 arises";
		case 0x06505: return "Any message of 1 till 6 in status 5 arises";
		case 0x06605: return "Any message of 1 till 7 in status 5 arises";
		case 0x06705: return "Any message of 1 till 8 in status 5 arises";
		case 0x06805: return "Any message of 1 till 9 in status 5 arises";
		case 0x06905: return "Any message of 1 till 10 in status 5 arises";
		case 0x06A05: return "Any message of 1 till 11 in status 5 arises";
		case 0x06B05: return "Any message of 1 till 12 in status 5 arises";
		case 0x06C05: return "Any message of 1 till 13 in status 5 arises";
		case 0x06D05: return "Any message of 1 till 14 in status 5 arises";
		case 0x06E05: return "Any message of 1 till 15 in status 5 arises";
		case 0x06F05: return "Any message of 1 till 16 in status 5 arises";
		case 0x06106: return "Message 1 or 2 in status 6 arises";
		case 0x06206: return "Any message of 1 till 3 in status 6 arises";
		case 0x06306: return "Any message of 1 till 4 in status 6 arises";
		case 0x06406: return "Any message of 1 till 5 in status 6 arises";
		case 0x06506: return "Any message of 1 till 6 in status 6 arises";
		case 0x06606: return "Any message of 1 till 7 in status 6 arises";
		case 0x06706: return "Any message of 1 till 8 in status 6 arises";
		case 0x06806: return "Any message of 1 till 9 in status 6 arises";
		case 0x06906: return "Any message of 1 till 10 in status 6 arises";
		case 0x06A06: return "Any message of 1 till 11 in status 6 arises";
		case 0x06B06: return "Any message of 1 till 12 in status 6 arises";
		case 0x06C06: return "Any message of 1 till 13 in status 6 arises";
		case 0x06D06: return "Any message of 1 till 14 in status 6 arises";
		case 0x06E06: return "Any message of 1 till 15 in status 6 arises";
		case 0x06F06: return "Any message of 1 till 16 in status 6 arises";
		case 0x06107: return "Message 1 or 2 in status 7 arises";
		case 0x06207: return "Any message of 1 till 3 in status 7 arises";
		case 0x06307: return "Any message of 1 till 4 in status 7 arises";
		case 0x06407: return "Any message of 1 till 5 in status 7 arises";
		case 0x06507: return "Any message of 1 till 6 in status 7 arises";
		case 0x06607: return "Any message of 1 till 7 in status 7 arises";
		case 0x06707: return "Any message of 1 till 8 in status 7 arises";
		case 0x06807: return "Any message of 1 till 9 in status 7 arises";
		case 0x06907: return "Any message of 1 till 10 in status 7 arises";
		case 0x06A07: return "Any message of 1 till 11 in status 7 arises";
		case 0x06B07: return "Any message of 1 till 12 in status 7 arises";
		case 0x06C07: return "Any message of 1 till 13 in status 7 arises";
		case 0x06D07: return "Any message of 1 till 14 in status 7 arises";
		case 0x06E07: return "Any message of 1 till 15 in status 7 arises";
		case 0x06F07: return "Any message of 1 till 16 in status 7 arises";
		case 0x06108: return "Message 1 or 2 in status 8 arises";
		case 0x06208: return "Any message of 1 till 3 in status 8 arises";
		case 0x06308: return "Any message of 1 till 4 in status 8 arises";
		case 0x06408: return "Any message of 1 till 5 in status 8 arises";
		case 0x06508: return "Any message of 1 till 6 in status 8 arises";
		case 0x06608: return "Any message of 1 till 7 in status 8 arises";
		case 0x06708: return "Any message of 1 till 8 in status 8 arises";
		case 0x06808: return "Any message of 1 till 9 in status 8 arises";
		case 0x06908: return "Any message of 1 till 10 in status 8 arises";
		case 0x06A08: return "Any message of 1 till 11 in status 8 arises";
		case 0x06B08: return "Any message of 1 till 12 in status 8 arises";
		case 0x06C08: return "Any message of 1 till 13 in status 8 arises";
		case 0x06D08: return "Any message of 1 till 14 in status 8 arises";
		case 0x06E08: return "Any message of 1 till 15 in status 8 arises";
		case 0x06F08: return "Any message of 1 till 16 in status 8 arises";
		case 0x06109: return "Message 1 or 2 in status 9 arises";
		case 0x06209: return "Any message of 1 till 3 in status 9 arises";
		case 0x06309: return "Any message of 1 till 4 in status 9 arises";
		case 0x06409: return "Any message of 1 till 5 in status 9 arises";
		case 0x06509: return "Any message of 1 till 6 in status 9 arises";
		case 0x06609: return "Any message of 1 till 7 in status 9 arises";
		case 0x06709: return "Any message of 1 till 8 in status 9 arises";
		case 0x06809: return "Any message of 1 till 9 in status 9 arises";
		case 0x06909: return "Any message of 1 till 10 in status 9 arises";
		case 0x06A09: return "Any message of 1 till 11 in status 9 arises";
		case 0x06B09: return "Any message of 1 till 12 in status 9 arises";
		case 0x06C09: return "Any message of 1 till 13 in status 9 arises";
		case 0x06D09: return "Any message of 1 till 14 in status 9 arises";
		case 0x06E09: return "Any message of 1 till 15 in status 9 arises";
		case 0x06F09: return "Any message of 1 till 16 in status 9 arises";
		case 0x07002: return "Message 1 arises in system status";
		case 0x07102: return "Any message of 1 till 2 arises in system status";
		case 0x07202: return "Any message of 1 till 3 arises in system status";
		case 0x07302: return "Any message of 1 till 4 arises in system status";
		case 0x07402: return "Any message of 1 till 5 arises in system status";
		case 0x07502: return "Any message of 1 till 6 arises in system status";
		case 0x07602: return "Any message of 1 till 7 arises in system status";
		case 0x07702: return "Any message of 1 till 8 arises in system status";
		case 0x07802: return "Any message of 1 till 9 arises in system status";
		case 0x07902: return "Any message of 1 till 10 arises in system status";
		case 0x07A02: return "Any message of 1 till 11 arises in system status";
		case 0x07B02: return "Any message of 1 till 12 arises in system status";
		case 0x07C02: return "Any message of 1 till 13 arises in system status";
		case 0x07D02: return "Any message of 1 till 14 arises in system status";
		case 0x07E02: return "Any message of 1 till 15 arises in system status";
		case 0x07F02: return "Any message of 1 till 16 arises in system status";
		case 0x07001: return "Message 1 arises in overall status";
		case 0x07101: return "Any message of 1 till 2 arises in overall status";
		case 0x07201: return "Any message of 1 till 3 arises in overall status";
		case 0x07301: return "Any message of 1 till 4 arises in overall status";
		case 0x07401: return "Any message of 1 till 5 arises in overall status";
		case 0x07501: return "Any message of 1 till 6 arises in overall status";
		case 0x07601: return "Any message of 1 till 7 arises in overall status";
		case 0x07701: return "Any message of 1 till 8 arises in overall status";
		case 0x07801: return "Any message of 1 till 9 arises in overall status";
		case 0x07901: return "Any message of 1 till 10 arises in overall status";
		case 0x07A01: return "Any message of 1 till 11 arises in overall status";
		case 0x07B01: return "Any message of 1 till 12 arises in overall status";
		case 0x07C01: return "Any message of 1 till 13 arises in overall status";
		case 0x07D01: return "Any message of 1 till 14 arises in overall status";
		case 0x07E01: return "Any message of 1 till 15 arises in overall status";
		case 0x07F01: return "Any message of 1 till 16 arises in overall status";
		case 0x08101: return "Reached Backup time";
		case 0x08002: return "Change of monthly period backwards (set clock backwards)";
		case 0x08102: return "Month expired";
		case 0x08003: return "Change of daly period backwards (set clock backwards)";
		case 0x08103: return "Day expired";
		case 0x08004: return "Change of measuring period backwards (set clock backwards)";
		case 0x08104: return "End of measuring period";
		case 0x08005: return "Event counter 5 decreases";
		case 0x08105: return "Event counter 5 increases";
		case 0x08006: return "Event counter 6 decreases";
		case 0x08106: return "Event counter 6 increases";
		case 0x08007: return "Event counter 7 decreases";
		case 0x08107: return "Event counter 7 increases";
		case 0x08008: return "Event counter 8 decreases";
		case 0x08108: return "Event counter 8 increases";
		case 0x08009: return "Event counter 9 decreases";
		case 0x08109: return "Event counter 9 increases";
		case 0x0800A: return "Event counter 10 decreases";
		case 0x0810A: return "Event counter 10 increases";
		case 0x0800B: return "Event counter 11 decreases";
		case 0x0810B: return "Event counter 11 increases";
		case 0x0800C: return "Event counter 12 decreases";
		case 0x0810C: return "Event counter 12 increases";
		case 0x0800D: return "Repeated data protocol cycle (set clock backwards) ";
		case 0x0810D: return "New data protocol cycle ";
		case 0x0800E: return "Repeated data protocol day (set clock backwards)";
		case 0x0810E: return "New data protocol day";
		case 0x0800F: return "Event counter 15 decreases";
		case 0x0810F: return "Event counter 15 increases";
		case 0x08201: return "Parameter (e.g. time, counter reading) for monthly archive counter readings changed - after change";
		case 0x08202: return "Parameter (e.g. time) for monthly archive measuring values changed - after change";
		case 0x08203: return "Parameter (e.g. time, counter reading) for measuring archive changed - after change";
		case 0x08204: return "Parameter for logbook changed - after change";
		case 0x08205: return "Parameter for audit trail changed - after change";
		case 0x08206: return "Parameter for calibr. archive changed - after change";
		case 0x08207: return "Parameter (e.g. time, counter reading) for daily archive changed - after change";
		case 0x08208: return "Parameter for Selma archive changed - after change";
		case 0x08209: return "Parameter for PTB logbook changed - after change";
		case 0x0820A: return "Parameter for standard output archive changed - after change";
		case 0x0820B: return "Parameter (e.g. time, counter reading) for measuring archive 2 changed - after change";
		case 0x0820C: return "Parameter for flexible archive 2 changed - after change";
		case 0x0820D: return "Parameter for flexible archive 3 changed - after change";
		case 0x0820E: return "Parameter for flexible archive 4 changed - after change";
		case 0x0820F: return "Parameter for flexible archive 5 changed - after change";
		case 0x08301: return "Parameter (e.g. time, counter reading) for monthly archive counter readings changed - before change";
		case 0x08302: return "Parameter (e.g. time) for monthly archive measuring values changed - before change";
		case 0x08303: return "Parameter (e.g. time, counter reading) for measuring archive changed - before change";
		case 0x08304: return "Parameter for logbook changed - before change";
		case 0x08305: return "Parameter for audit trail changed - before change";
		case 0x08306: return "Parameter for calibr. archive changed - before change";
		case 0x08307: return "Parameter (e.g. time, counter reading) for daily archive changed - before change";
		case 0x08308: return "Parameter for Selma archive changed - before change";
		case 0x08309: return "Parameter for PTB logbook changed - before change";
		case 0x0830A: return "Parameter for standard output archive changed - before change";
		case 0x0830B: return "Parameter (e.g. time, counter reading) for measuring archive 2 changed - before change";
		case 0x0830C: return "Parameter for flexible archive 2 changed - before change";
		case 0x0830D: return "Parameter for flexible archive 3 changed - before change";
		case 0x0830E: return "Parameter for flexible archive 4 changed - before change";
		case 0x0830F: return "Parameter for flexible archive 5 changed - before change";
		case 0x08501: return "Freeze command for monthly archive counter readings ";
		case 0x08502: return "Freeze command for monthly archive measuring values";
		case 0x08503: return "Freeze command for measurement period archive";
		case 0x08504: return "Freeze command for logboook freezing";
		case 0x08505: return "Freeze command for audit trail (modifications logbook)";
		case 0x08506: return "Freeze command for calibration archive";
		case 0x08507: return "Freeze command for daily archive";
		default:
			return "Unknown event: 0x" + ProtocolUtils.buildStringHex(protocolCode, 8);
		}


	}

	public static String getStatusDescription(int statusCode) {
		String returnValue = "";

		if ((statusCode & RESTART_FLAG) != 0) {
			returnValue += "Restart of the device. ";
		}
		if ((statusCode & DATA_RESTORED_FLAG) != 0) {
			returnValue += "Data (clock, counter reading) has been restored. ";
		}
		if ((statusCode & POWERSUPPLY_FLAG) != 0) {
			returnValue += "Internal power supply too less. ";
		}
		if ((statusCode & DATA_ERROR_FLAG) != 0) {
			returnValue += "Fatal data error. ";
		}
		if ((statusCode & SETTINGS_ERROR_FLAG) != 0) {
			returnValue += "Settings error. ";
		}
		if ((statusCode & BATTERY_LIVE_FLAG) != 0) {
			returnValue += "Battery life time end. ";
		}
		if ((statusCode & REPAIR_MODE_FLAG) != 0) {
			returnValue += "Repair mode switched on. ";
		}
		if ((statusCode & CLOCK_FLAG) != 0) {
			returnValue += "Clock not justified. ";
		}
		if ((statusCode & DATA_TRANSMISSION_FLAG) != 0) {
			returnValue += "Data transmission running. ";
		}
		if ((statusCode & REMOTE_CLOCK_FLAG) != 0) {
			returnValue += "Remote clock setting active. ";
		}
		if ((statusCode & BATTERY_MODE_FLAG) != 0) {
			returnValue += "Device in battery powered mode. ";
		}
		if ((statusCode & DIAPLAY_DST_FLAG) != 0) {
			returnValue += "The displayed time is daylight saving time. ";
		}

		returnValue += " Ast3 = ";
		if (statusCode == 0) {
			returnValue += "0";
		}
		for (int j = 0; j < 32; j++) {
			if ((statusCode & (1<<j)) == (1<<j)) {
				returnValue += (j+1) + ";";
			}
		}

		returnValue += " [0x" + ProtocolUtils.buildStringHex(statusCode, 8) + "]";
		return returnValue;
	}

	public static int getEiIntervalStatus(int statusCode) {
		int returnCode = IntervalData.OK;

		if ((statusCode & RESTART_FLAG) != 0) {
			returnCode |= IntervalData.POWERUP | IntervalData.POWERDOWN;
		}
		if ((statusCode & DATA_RESTORED_FLAG) != 0) {
			returnCode |= IntervalData.DEVICE_ERROR;
		}
		if ((statusCode & POWERSUPPLY_FLAG) != 0) {
			returnCode |= IntervalData.BATTERY_LOW;
		}
		if ((statusCode & DATA_ERROR_FLAG) != 0) {
			returnCode |= IntervalData.CORRUPTED;
		}
		if ((statusCode & SETTINGS_ERROR_FLAG) != 0) {
			returnCode |= IntervalData.DEVICE_ERROR;
		}
		if ((statusCode & BATTERY_LIVE_FLAG) != 0) {
			returnCode |= IntervalData.BATTERY_LOW;
		}
		if ((statusCode & REPAIR_MODE_FLAG) != 0) {
			returnCode |= IntervalData.OTHER;
		}
		if ((statusCode & CLOCK_FLAG) != 0) {
			returnCode |= IntervalData.BADTIME;
		}
		if ((statusCode & DATA_TRANSMISSION_FLAG) != 0) {
			returnCode |= IntervalData.OTHER;
		}
		if ((statusCode & REMOTE_CLOCK_FLAG) != 0) {
			returnCode |= IntervalData.SHORTLONG;
		}
		if ((statusCode & BATTERY_MODE_FLAG) != 0) {
			returnCode |= IntervalData.PHASEFAILURE;
		}
		if ((statusCode & DIAPLAY_DST_FLAG) != 0) {
			returnCode |= IntervalData.OTHER;
		}
		return returnCode;
	}

}
