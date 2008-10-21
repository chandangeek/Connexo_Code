package com.energyict.protocolimpl.edmi.mk10.eventsurvey;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.ProtocolUtils;

public class Event {

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

	private static String getDescriptionFromCode(int eventtype) throws IOException {
		
		switch (eventtype & 0xF000) {
			case 0x1000: 
				switch (eventtype & 0x0FF0) {
					case 0x0000: return "The meter was switched off"; 
					case 0x0010: return "The meter powered up. Reason: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
					case 0x0020: return "Recovered some parameters. Parameters: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
					case 0x0030: return "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
					case 0x0040: return "Meter runtime statistics changed. 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
				}
			case 0x2000: 
				switch (eventtype & 0x0FF0) {
					case 0x0000: return "User logged on: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2); 
					case 0x0040: return "User changed a setting: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
					case 0x0080: 
						switch (eventtype & 0x000F) {
							case 0x0000: return "User acces denied. Bad password."; 
							case 0x0001: return "User logged off. X command received."; 
							case 0x0002: return "User logged off. Inactivity timeout."; 
							case 0x0003: return "User logged off. Lost connection."; 
							case 0x0004: return "User logged off. Login under another name."; 
							case 0x0005: return "User logged off. Logoff via register write."; 
							case 0x0006: return "User logged off. Logoff via register write for firmware update."; 
						}
					case 0x00C0: 
						switch (eventtype & 0x000F) {
							case 0x000F: return "System time changed to this time.";
							default: return "System time changed from port 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
						}
				}
			case 0x3000:
				switch (eventtype & 0x003F) {
					case 0: return "EFA: User defined/magnetic tamper.";
					case 1: return "EFA: Battery failure.";
					case 2: return "EFA: Pulsing output overflow.";
					case 3: return "EFA: Data flash failure.";
					case 4: return "EFA: Program flash failure.";
					case 5: return "EFA: RAM or LCD failure.";
					case 6: return "EFA: Modem failure.";
					case 7: return "EFA: Calibration data loss.";
					case 8: return "EFA: Reverse power.";
					case 9: return "EFA: Clock failure.";
					case 10: return "EFA: Tamper.";
					case 11: return "EFA: Incorrect phase rotation.";
					case 12: return "EFA: VT failure.";
					case 13: return "EFA: Voltage tolerance error.";
					case 14: return "EFA: Asymetric power.";
					case 15: return "EFA: Reference failure.";
					case 63: return "EFA: All latched flags were cleared";
				}
			case 0x4000:
				switch (eventtype & 0x0F00) {
					case 0x0000: return "The meter firmware changed to revision 0x" + ProtocolUtils.buildStringHex(eventtype & 0x00FF, 2);
					case 0x0100: return "The meter boorloader was changed.";
				}
			case 0x5000:
				switch (eventtype & 0x0FFF) {
					case 0x0000: return "Automatic billing reset occured.";
					case 0x0001: return "Manual billing reset occured from the billing reset button.";
					default: return "Manual billing reset occured on port 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0030)>>4, 2);
				}
			case 0x6000: 
				switch (eventtype & 0x0800) {
				case 0x0080: return "Voltage surge change start.";
				case 0x0000: return "Voltage sag change start.";
				}
			case 0x7000: return "Voltage change end. The eventtime is the duration of the voltage change instead of date/time time !!!";
			case 0x8000: return "Reserved event.";
			case 0x9000: return "Reserved event.";
			case 0xA000:
				switch (eventtype & 0x00FF) {
				case 0: return "Setup change: Event logs cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 1: return "Setup change: Load survey 1. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 2: return "Setup change: Load survey 2. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 3: return "Setup change: TOU setup changed / cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 4: return "Setup change: Billing history cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 5: return "Setup change: Pulsing generator reset. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
				case 6: return "Setup change: TOU calendar changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);

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
				default: return "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00)>>8, 2);
			}
			default: return "Unknown event: 0x" + ProtocolUtils.buildStringHex(eventtype, 4) + " !!!";
		}
		
	}
	
}
