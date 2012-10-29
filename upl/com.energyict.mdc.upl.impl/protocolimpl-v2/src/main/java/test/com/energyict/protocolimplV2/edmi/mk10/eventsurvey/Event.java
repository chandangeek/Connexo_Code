package test.com.energyict.protocolimplV2.edmi.mk10.eventsurvey;

import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.Date;

public class Event {

	private static final int FLASH_BACKUP_BAD = 0x0001;
	private static final int ENERGY_ACCUMULATION_RESTORED = 0x0004;
	private static final int CONTROL_DATA_RESTORED = 0x0008;
	
	private static final int PORT_MASK = 0x0030;
	private static final int PORT_OPTICAL = 0x0000;
	private static final int PORT_MODEM = 0x0010;
	private static final int PORT_RESERVED = 0x0020;
	
	private static final int USER_NUMBER_MASK = 0x000F;
	private static final int CHANGED_SETUP_MASK = 0x0003;
	private static final int TIME_CHANGE_MASK = 0x000F;
	
	private static final int EFA_CONDITION_LATCHED = 0x0000;
	private static final int EFA_CONDITION_LATCH_CLEARED = 0x0040;
	private static final int EFA_CONDITION_BECAME_ACTIVE = 0x0080;
	private static final int EFA_CONDITION_BECAME_INACTIVE = 0x00C0;

	private static final int RUNTIME_STATISTICS_ON_TIME = 0;
	private static final int RUNTIME_STATISTICS_OFF_TIME = 1;
	private static final int RUNTIME_STATISTICS_NUMBER_OF_POWERUPS = 2;
	private static final int RUNTIME_STATISTICS_RESERVED = 3;
	
	public static final int SYSTEM = 0;
	public static final int SETUP = 1;
	public static final int BILLING = 2;
	public static final int TRIG = 3;
	public static final int DIAG = 4;
	public static final String[] LOGNAMES = {"SYSTEM", "SETUP", "BILLING", "TRIG", "DIAG", "UNKNOWN"};
	
	private int eventCode;
	private Date eventDate;
	private int eventLogNr;
	private String eventLogName;
	private String eventDescription;
	
	public Event(Date date, int code, int lognr) throws IOException {
		if (lognr>4 || lognr<0) lognr = 5;
		this.eventLogNr = lognr;
		this.eventLogName = LOGNAMES[this.getEventLogNr()];
		this.eventDate = date;
		this.eventCode = code;
		this.eventDescription = this.eventLogName + " - " + getDescriptionFromCode(this.eventCode);
	}
	
	public String toString() {
		return this.eventDate.toString() + " " + getEventDescription();
	}

	public int getEventCode() {
		return eventCode;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public int getEventLogNr() {
		return eventLogNr;
	}

	public String getEventLogName() {
		return eventLogName;
	}

	private static String getPortString(int eventtype) {
		if ((eventtype & PORT_MASK) == PORT_MODEM) return " (Port: MODEM)";
		if ((eventtype & PORT_MASK) == PORT_OPTICAL) return " (Port: OPTICAL)";
		if ((eventtype & PORT_MASK) >= PORT_RESERVED) return " (Port: RESERVED)";
		return " (Port: INVALID)";
	}
	
	private static String getTimeChangeReason(int eventtype) {
		if ((eventtype & TIME_CHANGE_MASK) == 0) return "from command on port.";
		if ((eventtype & TIME_CHANGE_MASK) == 1) return "from pulsing input.";
		if ((eventtype & TIME_CHANGE_MASK) == 2) return "from ripple count.";
		if ((eventtype & TIME_CHANGE_MASK) == 0x0F) return "to this time.";
		return "from RESERVED.";
	}
	
	public static String getDescriptionFromCode(int eventtype) throws IOException {
		String eventDescription = "";

		if ((eventtype & 0xFFF0) == 0x1000) return "The meter was switched off";
		if ((eventtype & 0xFFF0) == 0x1010) return "The meter powered up. Reason: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
		if ((eventtype & 0xFFF0) == 0x1020) {
			eventDescription = "Recovered some parameters. Parameters: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
			if ((eventtype & FLASH_BACKUP_BAD) == FLASH_BACKUP_BAD) eventDescription += " - Flash backup bad";
			if ((eventtype & ENERGY_ACCUMULATION_RESTORED) == ENERGY_ACCUMULATION_RESTORED) eventDescription += " - Energy accumulation restored";
			if ((eventtype & CONTROL_DATA_RESTORED) == CONTROL_DATA_RESTORED) eventDescription += " - Control data restored";
			return eventDescription;
		}
		if ((eventtype & 0xFFF0) == 0x1030) {
			eventDescription = "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
			if ((eventtype & FLASH_BACKUP_BAD) == FLASH_BACKUP_BAD) eventDescription += " - Flash backup bad";
			if ((eventtype & ENERGY_ACCUMULATION_RESTORED) == ENERGY_ACCUMULATION_RESTORED) eventDescription += " - Energy accumulation restored";
			if ((eventtype & CONTROL_DATA_RESTORED) == CONTROL_DATA_RESTORED) eventDescription += " - Control data restored";
			return eventDescription;
		}
		if ((eventtype & 0xFFF0) == 0x1040) {
			eventDescription = "Meter runtime statistics changed. " + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
			if ((eventtype & 0x000F) == RUNTIME_STATISTICS_ON_TIME) eventDescription += " - ON time changed.";
			if ((eventtype & 0x000F) == RUNTIME_STATISTICS_OFF_TIME) eventDescription += " - OFF time changed.";
			if ((eventtype & 0x000F) == RUNTIME_STATISTICS_NUMBER_OF_POWERUPS) eventDescription += " - Number of powerups changed.";
			if ((eventtype & 0x000F) >= RUNTIME_STATISTICS_RESERVED) eventDescription += " - Reserved.";
			return eventDescription;
		}
		if ((eventtype & 0xFFC0) == 0x2000) return "User" + (eventtype & USER_NUMBER_MASK) + " logged on." + getPortString(eventtype);
		if ((eventtype & 0xFFCC) == 0x2040) return "User changed setup" + ((eventtype & CHANGED_SETUP_MASK) + 1) + "." + getPortString(eventtype);
		
		if ((eventtype & 0xFFCF) == 0x2080) return "User acces denied. Bad password." + getPortString(eventtype);
		if ((eventtype & 0xFFCF) == 0x2081) return "User logged off. X command received." + getPortString(eventtype);
		if ((eventtype & 0xFFCF) == 0x2082) return "User logged off. Inactivity timeout." + getPortString(eventtype);
		if ((eventtype & 0xFFCF) == 0x2083) return "User logged off. Lost connection." + getPortString(eventtype);
		if ((eventtype & 0xFFCF) == 0x2084) return "User logged off. Login under another name." + getPortString(eventtype);
		if ((eventtype & 0xFFCF) == 0x2085) return "User logged off. Logoff via register write." + getPortString(eventtype);
		if ((eventtype & 0xFFCF) == 0x2086) return "User logged off. Logoff via register write for firmware update." + getPortString(eventtype);

		if ((eventtype & 0xFFC0) == 0x20C0) return "System time changed " + getTimeChangeReason(eventtype) + getPortString(eventtype);
		
		if ((eventtype & 0xFF00) == 0x3000) {
			eventDescription = "";
			switch (eventtype & 0x00C0) {
				case EFA_CONDITION_LATCHED: eventDescription = " (LATCHED)"; break;
				case EFA_CONDITION_LATCH_CLEARED: eventDescription = " (LATCH CLEARED)"; break;
				case EFA_CONDITION_BECAME_ACTIVE: eventDescription = " (BECAME ACTIVE)"; break;
				case EFA_CONDITION_BECAME_INACTIVE: eventDescription = " (BECAME INACTIVE)"; break;
			} 

			switch (eventtype & 0x003F) {
				case 0: return "EFA: User defined/magnetic tamper." + eventDescription;
				case 1: return "EFA: Battery failure." + eventDescription;
				case 2: return "EFA: Pulsing output overflow." + eventDescription;
				case 3: return "EFA: Data flash failure." + eventDescription;
				case 4: return "EFA: Program flash failure." + eventDescription;
				case 5: return "EFA: RAM or LCD failure." + eventDescription;
				case 6: return "EFA: Modem failure." + eventDescription;
				case 7: return "EFA: Calibration data loss." + eventDescription;
				case 8: return "EFA: Reverse power." + eventDescription;
				case 9: return "EFA: Clock failure." + eventDescription;
				case 10: return "EFA: Tamper." + eventDescription;
				case 11: return "EFA: Incorrect phase rotation." + eventDescription;
				case 12: return "EFA: VT failure." + eventDescription;
				case 13: return "EFA: Voltage tolerance error." + eventDescription;
				case 14: return "EFA: Asymetric power." + eventDescription;
				case 15: return "EFA: Reference failure." + eventDescription;
				case 63: return "EFA: All latched flags were cleared" + eventDescription;
			} 
		}
		
		if ((eventtype & 0xFF00) == 0x4000) return "The meter firmware changed to revision 0x" + ProtocolUtils.buildStringHex(eventtype & 0x00FF, 2);
		if (eventtype == 0x4100) return "The meter bootloader was upgraded.";

		if (eventtype == 0x5000) return "Automatic billing reset occured.";
		if (eventtype == 0x5001) return "Manual billing reset occured from the billing reset button.";
		if ((eventtype & 0xFFCF) == 0x5080) return "Manual billing reset occured." + getPortString(eventtype);
		
		if ((eventtype & 0xF800) == 0x6800) return "Voltage surge change start.";
		if ((eventtype & 0xF800) == 0x6000) return "Voltage sag change start.";
		if ((eventtype & 0xF000) == 0x7000) return "Voltage change end. The eventtime is the duration of the voltage change instead of date/time time !!!";

		if ((eventtype & 0xF000) == 0x8000) return "Reserved event.";
		if ((eventtype & 0xF000) == 0x9000) return "Reserved event.";

		if  ((eventtype & 0xF000) == 0xA000) {
			switch (eventtype & 0x00FF) {
				case 0: return "Setup change: Event logs cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 1: return "Setup change: Load survey 1. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 2: return "Setup change: Load survey 2. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 3: return "Setup change: TOU setup changed / cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 4: return "Setup change: Billing history cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 5: return "Setup change: Pulsing generator reset. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 6: return "Setup change: TOU calendar changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 7: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 8: return "Setup change: Hardware setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 9: return "Setup change: Calibration changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 10: return "Setup change: Scaling factors changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 11: return "Setup change: Transformer ratios changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 12: return "Setup change: Pulse factors changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 13: return "Setup change: Pulsing inputs setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 14: return "Setup change: Pulsing outputs setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 15: return "Setup change: Enunciators changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 16: return "Setup change: Optical port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 17: return "Setup change: Modem port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 18: return "Setup change: LCD screens changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 19: return "Setup change: Alarm setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 20: return "Setup change: Security setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 21: return "Setup change: Timer setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 22: return "Setup change: Timer setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 23: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 24: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 25: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 26: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 27: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 28: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 29: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 30: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 31: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 32: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
			}
		}

		if (eventtype == 0xB302) return "Stack corrupted when power up (Normal operation, as datahub has no battery)";

		return "Unknown event: 0x" + ProtocolUtils.buildStringHex(eventtype, 4) + " !!!";
		
	}
	
}
