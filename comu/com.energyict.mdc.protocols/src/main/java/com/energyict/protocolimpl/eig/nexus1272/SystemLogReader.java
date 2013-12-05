package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SystemLogReader extends AbstractLogReader {

	List <MeterEvent> meterEvents = new ArrayList <MeterEvent>();

	public SystemLogReader(OutputStream os ,NexusProtocolConnection npc) {
		outputStream = os;
		connection = npc;
		windowIndexAddress = new byte[] {(byte) 0x95, 0x0a};;
		windowModeAddress = new byte[] {(byte) 0x95, 0x4a};
		windowEndAddress = 38976;
	}

	@Override
	protected Command getHeaderCommand() {
		return NexusCommandFactory.getFactory().getSystemLogHeaderCommand();
	}

	@Override
	protected Command getWindowCommand() {
		return NexusCommandFactory.getFactory().getSystemLogWindowCommand();
	}

	@Override
	public void parseLog(byte[] byteArray, ProfileData profileData, Date from, int intervalSeconds) throws IOException {
		parseSystemLog(byteArray);
	}

	public static final byte POWER = 0x000;
	public static final byte PASSWORD = 0x001;
	public static final byte CHANGE_PROGRAMMABLE_SETTINGS = 0x002;
	public static final byte CHANGE_FIRMWARE= 0x003;
	public static final byte CHANGE_TIME = 0x004;
	public static final byte TEST_MODE = 0x005;
	public static final byte LOG_DOWNLOAD = 0x006;
	public static final byte FEATURE_RESET = 0x007;
	private void parseSystemLog(byte[] ba) throws IOException {

//		NexusDataParser ndp = new NexusDataParser(ba);
		int offset = 0;
		int length = 8;
		int recNum = 0;
		int recSize = 16;

		while (offset < ba.length) {
			Date recDate = parseF3(ba, offset);
			String event = recDate + "";
			offset+= length;
			byte code = ba[offset++];
			byte subcode1;
			byte subcode2;
			byte subcode3;
			switch (code) {
			case POWER:
				event += " => POWER";
				subcode1 = ba[offset++];
				switch (subcode1) {
				case 0x00:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.POWERDOWN, subcode1, "Power was lost"));
					event += " : Power was lost";
					break;
				case 0x01:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.POWERUP, subcode1,"Run Time has started"));
					event += " : Run Time has started";
					break;
				case 0x02:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.POWERUP, subcode1, "Run Time is active (features were initialized and enabled)"));
					event += " : Run Time is active (features were initialized and enabled)";
					break;
				default:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Undefined event"));
					event += " : Undefined" + " " + subcode1;
					break;
				}
				break;

			case PASSWORD:
				event += " => PASSWORD";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				String port = "";
				switch (subcode2) {
				case 0x000:
					port = "Port 4";
					break;
				case 0x001:
					port = "Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					port = "Port 2";
					break;
				case 0x003:
					port = "Port 1";
					break;
				default:
					port = "Port Undefined";
					break;
				}
				switch (subcode1) {
				case 0x00:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, subcode1, "Password Protection was Enabled " + port));
					event += " : Password Protection was Enabled";
					break;
				case 0x01:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, subcode1, "Password Protection was Disabled " + port));
					event += " : Password Protection was Disabled";
					break;
				case 0x002:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, subcode1, "The Level 1 Password was changed " + port));
					event += " : The Level 1 Password was changed";
					break;
				case 0x003:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, subcode1, "The Level 2 Password was changed " + port));
					event += " : The Level 2 Password was changed";
					break;
				case 0x004:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Level 1 access was granted " + port));
					event += " : Level 1 access was granted";
					break;
				case 0x005:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Level 2 access was granted " + port));
					event += " : Level 2 access was granted";
					break;
				case 0x006:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "An invalid password was supplied " + port));
					event += " : An invalid password was supplied";
					break;
				default:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Undefined event " + port));
					event += " : Undefined";
					break;
				}

				break;
			case CHANGE_PROGRAMMABLE_SETTINGS:
				meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, "Programmable Setting have been changed"));
				event += " => CHANGE PROGRAMMABLE SETTINGS";
				break;
			case CHANGE_FIRMWARE:
				event += " => CHANGE FIRMWARE";
				subcode1 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, subcode1, "Change Firmware : Comm Run Time"));
					event += " : Comm Run Time";
					break;
				case 0x001:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, subcode1, "Change Firmware : DSP Run Time"));
					event += " : DSP Run Time";
					break;
				default:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CONFIGURATIONCHANGE, subcode1, "Change Firmware : Undefined"));
					event += " : Undefined";
					break;
				}
				//String version = parseF2(ba, offset, 4);
				event += " : Old version - ";// + version;
				break;
			case CHANGE_TIME:
				event += " => CHANGE TIME";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode2) {
				case 0x000:
					port = "Port 4";
					break;
				case 0x001:
					port = "Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					port = "Port 2";
					break;
				case 0x003:
					port = "Port 1";
					break;
				default:
					port = "Port Undefined";
					break;
				}

				switch (subcode1) {
				case 0x000:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.SETCLOCK_BEFORE, subcode1, "Old Time - The time stamp is the old time of the meter " + port));
					event += " : Old Time - The time stamp is the old time of the meter";
					break;
				case 0x001:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.SETCLOCK_AFTER, subcode1, "New Time - The time stamp is the new time of the meter " + port));
					event += " : New Time - The time stamp is the new time of the meter";
					break;
				default:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.SETCLOCK, subcode1, "Clock set - Undefined " + port));
					event += " : Undefined";
					break;
				}
				break;
			case TEST_MODE:
				event += " => TEST MODE";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x001:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)  Test Mode = TLC"));
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC";
					break;
				case 0x002:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q1 & Q2)  Test Mode = TLC"));
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = TLC";
					break;
				case 0x003:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q3 & Q4)  Test Mode = TLC"));
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = TLC";
					break;
				case 0x004:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VAh Test (Q1 & Q4)  Test Mode = TLC"));
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = TLC";
					break;
				case 0x005:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VAh Test (Q2 & Q3)  Test Mode = TL"));
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = TLC";
					break;
				case 0x006:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Block Average Test  Test Mode = TLC"));
					event += " : Action = Block Average Test  Test Mode = TLC";
					break;
				case 0x007:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Rolling Average Test  Test Mode = TLC"));
					event += " : Action = Rolling Average Test  Test Mode = TLC";
					break;
				case 0x008:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)  Test Mode = TLC"));
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC";
					break;
				case 0x009:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT"));
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT";
					break;
				case 0x00A:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q1 & Q2)  Test Mode = TLC & CTPT"));
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = TLC & CTPT";
					break;
				case 0x00B:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q3 & Q4)  Test Mode = TLC & CTPT"));
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = TLC & CTPT";
					break;
				case 0x00C:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VAh Test (Q1 & Q4)  Test Mode = TLC & CTPT"));
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = TLC & CTPT";
					break;
				case 0x00D:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, " Action = VAh Test (Q2 & Q3)  Test Mode = TLC & CTPT"));
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = TLC & CTPT";
					break;
				case 0x00E:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Block Average Test  Test Mode = TLC & CTPT"));
					event += " : Action = Block Average Test  Test Mode = TLC & CTPT";
					break;
				case 0x00F:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Rolling Average Test  Test Mode = TLC & CTPT"));
					event += " : Action = Rolling Average Test  Test Mode = TLC & CTPT";
					break;
				case 0x010:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT"));
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT";
					break;
				case 0x011:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated"));
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated";
					break;
				case 0x012:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q1 & Q2)  Test Mode = Uncompensated"));
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = Uncompensated";
					break;
				case 0x013:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q3 & Q4)  Test Mode = Uncompensated"));
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = Uncompensated";
					break;
				case 0x014:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VAh Test (Q1 & Q4)  Test Mode = Uncompensated"));
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = Uncompensated";
					break;
				case 0x015:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VAh Test (Q2 & Q3)  Test Mode = Uncompensated"));
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = Uncompensated";
					break;
				case 0x016:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Block Average Test  Test Mode = Uncompensated"));
					event += " : Action = Block Average Test  Test Mode = Uncompensated";
					break;
				case 0x017:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Rolling Average Test  Test Mode = Uncompensated"));
					event += " : Action = Rolling Average Test  Test Mode = Uncompensated";
					break;
				case 0x018:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated"));
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated";
					break;
				case 0x019:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)   Test Mode = CTPT"));
					event += " : Action = Wh Test (Del & Rcv)   Test Mode = CTPT";
					break;
				case 0x01A:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q1 & Q2)   Test Mode = CTPT"));
					event += " : Action = VARh Test (Q1 & Q2)   Test Mode = CTPT";
					break;
				case 0x01B:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VARh Test (Q3 & Q4)   Test Mode = CTPT"));
					event += " : Action = VARh Test (Q3 & Q4)   Test Mode = CTPT";
					break;
				case 0x01C:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VAh Test (Q1 & Q4)   Test Mode = CTPT"));
					event += " : Action = VAh Test (Q1 & Q4)   Test Mode = CTPT";
					break;
				case 0x01D:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = VAh Test (Q2 & Q3)   Test Mode = CTPT"));
					event += " : Action = VAh Test (Q2 & Q3)   Test Mode = CTPT";
					break;
				case 0x01E:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Block Average Test   Test Mode = CTPT"));
					event += " : Action = Block Average Test   Test Mode = CTPT";
					break;
				case 0x01F:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Rolling Average Test   Test Mode = CTPT"));
					event += " : Action = Rolling Average Test   Test Mode = CTPT";
					break;
				case 0x020:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Action = Wh Test (Del & Rcv)   Test Mode = CTPT"));
					event += " : Action = Wh Test (Del & Rcv)   Test Mode = CTPT";
					break;
				default:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Test Mode - Undefined"));
					event += " : Undefined";
					break;
				}

			case LOG_DOWNLOAD:
				event += " => LOG DOWNLOAD";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				subcode3 = ba[offset++];
				switch (subcode3) {
				case 0x000:
					port = "Port 4";
					break;
				case 0x001:
					port = "Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					port = "Port 2";
					break;
				case 0x003:
					port = "Port 1";
					break;
				default:
					port = "Port Undefined";
					break;
				}
				String log;
				switch (subcode2) {
				case 0x000:
					log = "Historical Log 1";
					break;
				case 0x001:
					log = "Historical Log 2";
					break;
				case 0x002:
					log = "Sequence of Events State Log";
					break;
				case 0x003:
					log = "Sequence of Events Snapshot Log";
					break;
				case 0x004:
					log = "Digital Input State Log";
					break;
				case 0x005:
					log = "Digital Input Snapshot Log";
					break;
				case 0x006:
					log = "Digital Output State Log";
					break;
				case 0x007:
					log = "Digital Output Snapshot Log";
					break;
				case 0x008:
					log = "Flicker Log";
					break;
				case 0x009:
					log = "Waveform Trigger Log";
					break;
				case 0x00A:
					log = "System Event Log";
					break;
				case 0x00B:
					log = "Waveform Sample Log";
					break;
				case 0x00C:
					log = "PQ Log";
					break;
				case 0x00D:
					log = "Reset Log";
					break;
				default:
					log = "Undefined";
					break;
				}
				switch (subcode1) {
				case 0x000:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Download Started, Log records while downloading [" + log + "] " + port));
					event += " : Download Started, Log records while downloading";
					break;
				case 0x001:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Download Started, Log paused while downloading [" + log + "] " + port));
					event += " : Download Started, Log Paused while downloading";
					break;
				case 0x002:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Download Ended [" + log + "] " + port));
					event += " : Download Ended";
					break;
				default:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Download Undefinded [" + log + "] " + port));
					event += " : Undefined";
					break;
				}


				break;
			case FEATURE_RESET:
				event += " => FEATURE RESET";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode2) {
				case 0x000:
					port = "Port 4";
					break;
				case 0x001:
					port = "Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					port = "Port 2";
					break;
				case 0x003:
					port = "Port 1";
					break;
				default:
					port = "Port Undefined";
					break;
				}
				switch (subcode1) {
				case 0x000:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.LOADPROFILE_CLEARED, subcode1, "All Logs Reset " + port));
					event += " : All Logs Reset";
					break;
				case 0x001:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "Maximum Reset " + port));
					event += " : Maximum Reset";
					break;
				case 0x002:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "Minimum Reset " + port));
					event += " : Minimum Reset";
					break;
				case 0x003:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "Energy Reset " + port));
					event += " : Energy Reset";
					break;
				case 0x004:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "Time of Use Current Month Reset " + port));
					event += " : Time of Use Current Month";
					break;
				case 0x005:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "Internal Input Accumulations and Aggregations Reset " + port));
					event += " : Internal Input Accumulations and Aggregations";
					break;
				case 0x006:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "KYZ Output Accumulation Reset " + port));
					event += " : KYZ Output Accumulations";
					break;
				case 0x007:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "Cumulative Demand Reset " + port));
					event += " : Cumulative Demand";
					break;
				case 0x008:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.LOADPROFILE_CLEARED, subcode1, "Historical Log 1 Reset " + port));
					event += " : Historical Log 1 Reset";
					break;
				case 0x009:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.LOADPROFILE_CLEARED, subcode1, "Historical Log 2 Reset " + port));
					event += " : Historical Log 2 Reset";
					break;
				case 0x00A:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CLEAR_DATA, subcode1, "Sequence of Events Log Reset " + port));
					event += " : Sequence of Events Log Reset";
					break;
				case 0x00B:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Digital Input Log Reset " + port));
					event += " : Digital Input Log Reset";
					break;
				case 0x00C:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Digital Output Log Reset " + port));
					event += " : Digital Output Log Reset";
					break;
				case 0x00D:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Flicker Log Reset " + port));
					event += " : Flicker Log Reset";
					break;
				case 0x00E:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Waveform Log Reset " + port));
					event += " : Waveform Log Reset";
					break;
				case 0x00F:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "PQ Log Reset " + port));
					event += " : PQ Log Reset";
					break;
				case 0x010:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.CLEAR_DATA, subcode1, "System Event Log Reset " + port));
					event += " : System Event Log Reset";
					break;
				case 0x011:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Total Average Power Factor Reset " + port));
					event += " : Total Average Power Factor Reset";
					break;
				case 0x012:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.BILLING_ACTION, subcode1, "Time of Use Active Registers Reset " + port));
					event += " : Time of Use Active Registers";
					break;
				default:
					meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, subcode1, "Feature Reset Undefinded " + port));
					event += " : Undefined";
					break;
				}
//				switch (subcode2) {
//				case 0x000:
//					event += " : Port 4";
//					break;
//				case 0x001:
//					event += " : Port 3 (10/100 Base T Ethernet)";
//					break;
//				case 0x002:
//					event += " : Port 2";
//					break;
//				case 0x003:
//					event += " : Port 1";
//					break;
//				default:
//					event += " : Port Undefined";
//					break;
//				}
				break;
			default:
				meterEvents.add(new MeterEvent(recDate, MeterEvent.OTHER, code, "Event Undefinded"));
				event += " => UNDEFINED";
				break;
			}

//			System.out.println(event);
			recNum++;
			offset = recNum * recSize;
		}
	}

	public List<MeterEvent> getMeterEvents() {
		return meterEvents;
	}




}
