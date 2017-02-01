/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum MeasurementKind {
	NOTAPPLICABLE (0, "Not applicable"),
	APPARENTPOWERFACTOR (2, "Apparent power factor"),
	CURRENCY (3, "Currency"),
	CURRENT (4, "Current"),
	CURRENTANGLE (5, "Current angle"),
	CURRENTIMBALANCE (6, "Current imbalance"),
	DATE (7, "Date"),
	DEMAND (8, "Demand"),
	DISTANCE (9, "Distance"),
	DISTORTIONVOLTAMPERES (10, "Distortion volt ampere"),
	ENERGIZATION (11, "Energization"),
	ENERGY (12, "Energy"),
	ENERGIZATIONLOADSIDE (13, "Energization load side"),
	FAN (14, "Fan"),
	FREQUENCY (15, "Frequency"),
	FUNDS (16, "Funds"),
	IEEE1366ASAI (17, "IEEE 1366 ASAI"),
	IEEE1366SIDI (18, "IEEE 1366 SIDI"),
	IEEE1366ASIFI (19, "IEEE 1366 ASIFI"),
	IEEE1366CAIDI (20, "IEEE 1366 CAIDI"),
	IEEE1366CAIFI (21, "IEEE 1366 CAIFI"),
	IEEE1366CEMIN (22, "IEEE 1366 CEMIn"),
	IEEE1366CEMSMIN (23, "IEEE 1366 CEMSMIn"),
	IEEE1366CTAIDI (24, "IEEE 1366 CTAIDI"),
	IEEE1366MAIFI (25, "IEEE 1366 MAIFI"),
	IEEE1366MAIFIE (26, "IEEE 1366 MAIFIE"),
	IEEE1366SAIDI (27, "IEEE 1366 SAIDI"),
	IEEE1366SAIFI (28, "IEEE 1366 SAIFI"),
	LINELOSSES (31, "Line losses"),
	LOSSES (32, "Losses"),
	NEGATIVESEQUENCE (33, "Negative sequence"),
	PHASORPOWERFACTOR (34, "Phasor power factor"),
	PHASORREACTIVEPOWER (35, "Phasor reactive power"),
	POSITIVESEQUENCE (36, "Positive sequence"),
	POWER (37, "Power"),
	POWERFACTOR (38, "Power factor"),
	QUANTITYPOWER (40, "Quantity power"),
	SAG (41, "Sag"),
	SWELL (42, "Swell"),
	SWITCHPOSITION (43, "Switch position"),
	TAPPOSITION (44, "Tap position"),
	TARIFFRATE (45, "Tariff rate"),
	TEMPERATURE (46, "Temperature"),
	TOTALHARMONICDISTORTION (47, "Total harmonic distortion"),
	TRANSFORMERLOSSES (48, "Transformer losses"),
	UNIPEDEVOLTAGEDIP10TO15 (49, "UNIPEDE voltage dip 10-15"),
	UNIPEDEVOLTAGEDIP15TO30 (50, "UNIPEDE voltage dip 15-30"),
	UNIPEDEVOLTAGEDIP30TO60 (51, "UNIPEDE voltage dip 30-60"),
	UNIPEDEVOLTAGEDIP60TO90 (52, "UNIPEDE voltage dip 60-90"),
	UNIPEDEVOLTAGEDIP90TO100 (53, "UNIPEDE voltage dip 90-100"),
	RMSVOLTAGE (54, "Voltage (rms)"),
	VOLTAGEANGLE (55,"Voltage angle"),
	VOLTAGEEXCURSION (56, "Voltage excursion"),
	VOLTAGEIMBALANCE (57, "Voltage imbalance"),
	VOLUME (58, "Volume"),
	ZEROFLOWDURATION (59, "Zero flow duration"),
	ZEROSEQUENCE (60, "Zero sequence"),
	DISTORTIONPOWERFACTOR(64,"Distortion power factor"),
	FREQUENCYEXCURSION(81,"Frequency excursion"),
	APPLICATIONCONTEXT (90, "Application context"),
	APTITLE (91, "Application title"),
	ASSETNUMBER (92, "Asset number"),
	BANDWIDTH (93, "Bandwidth"),
	BATTERYVOLTAGE (94, "Battery voltage"),
	BROADCASTADDRESS (95, "Broadcast address"),
	DEVICEADDRESSTYPE1 (96, "Device address type 1"),
	DEVICEADDRESSTYPE2 (97, "Device address type 2"),
	DEVICEADDRESSTYPE3 (98, "Device address type 3"),
	DEVICEADDRESSTYPE4 (99, "Device address type 4"),
	DEVICECLASS (100, "Device class"),
	ELECTRONICSERIALNUMBER (101, "Electronic serial number"),
	ENDDEVICEID (102, "End device ID"),
	GROUPADDRESSTYPE1 (103, "Group address type 1"),
	GROUPADDRESSTYPE2 (104, "Group address type 2"),
	GROUPADDRESSTYPE3 (105, "Group address type 3"),
	GROUPADDRESSTYPE4 (106, "Group address type 4"),
	IPADDRESS (107, "IP address"),
	MACADDRESS (108, "MAC address"),
	MFGASSIGNEDCONFIGURATIONID (109, "Manufacturing assigned configuration ID"),
	MFGASSIGNEDPHYSICALSERIALNUMBER (110, "Manufacturing assigned physical serial number"),
	MFGASSIGNEDPRODUCTNUMBER (111, "Manufacturing assigned product number"),
	MFGASSIGNEDUNIQUECOMMUNICATIONADDRESS (112, "Manufacturing assigned communication address"),
	MULITCASTADDRESS (113, "Multicast address"),
	ONEWAYADDRESS (114, "One-way address"),
	SIGNALSTRENGTH (115, "Signal strength"),
	TWOWAYADDRESS (116, "Two-way address") ,
	SIGNALNOISERATIO(117,"Signal noise ratio"),
	ALARM (118, "Alarm"),
	BATTERYCARRYOVER (119, "Battery carry-over"),
	DATAOVERFLOWALARM (120, "Data overflow alarm"),
	DEMANDLIMIT (121, "Demand limit"),
	DEMANDRESET (122, "Demand reset"),
	DIAGNOSTIC (123, "Diagnostic"),
	EMERGENCYLIMIT (124, "Emergency limit"),
	ENCODERTAMPER (125, "Encoder tamper"),
	IEEE1366MOMENTARYINTERRUPTION (126, "IEEE 1366 momentary interruption"),
	IEEE1366MOMENTARYINTERRUPTIONEVENT (127, "IEEE 1366 momentary interruption event"),
	IEEE1366SUSTAINEDINTERRUPTION (128, "IEEE 1366 sustained interruption"),
	INTERRUPTIONBEHAVIOUR (129, "Interruption behaviour"),
	INVERSIONTAMPER (130, "Inversion tamper"),
	LOADINTERRUPT (131, "Load interrupt"),
	LOADSHED (132, "Load shed"),
	MAINTENANCE (133, "Maintenance"),
	PHYSICALTAMPER (134, "Physical tamper"),
	POWERLOSSTAMPER (135, "Power loss tamper"),
	POWEROUTAGE (136, "Power outage"),
	POWERQUALITY (137, "Power quality"),
	POWERRESTORATION (138, "Power restoration"),
	PROGRAMMED (139, "Programmed"),
	PUSHBUTTON (140, "Pushbutton"),
	RELAYACTIVATION (141, "Relay activation"),
	RELAYCYCLE (142, "Relay cycle"),
	REMOVALTAMPER (143, "Removal tamper"),
	REPROGRAMMINGTAMPER (144, "Reprogramming tamper"),
	REVERSEROTATIONTAMPER (145, "Reverse rotation tamper"),
	SWITCHARMED (146, "Switch armed"),
	SWITCHDISABLED (147, "Switch disabled"),
	TAMPER (148, "Tamper"),
	WATCHDOGTIMEOUT (149, "Watchdog timeout"),
	BILLLASTPERIOD(150,"Bill last period"),
	BILLTODATE(151,"Bill to date"),
	BILLCARRYOVER(152,"Bill carry-over"),
	CONNECTIONFEE(153,"Connection fee"),
	AUDIBLEVOLUME(154,"Audible volume"),
	VOLUMETRICFLOW(155,"Volume metric flow"),
	RELATIVEHUMIDITY(156,"Relative humidity"),
	SKYCOVER(157,"Sky cover"),
	VOLTAGE(158,"Voltage"),
	DCVOLTAGE(159,"DC voltage"),
	ACVOLTAGEPEAK(160,"AC voltage peak"),
	ACVOLTAGEPEAKTOPEAK(161,"AC voltage peak to peak"),

    // custom measurementkinds have a value above 1000
    CTRATIO(1001,"CT ratio"),
    MULTIPLIER(1002,"Multiplier"),
    SECURITYSTATE(1003,"Security state"),
    BLACKLISTTABLE(1005,"BlackList table"),

    ;

	private final int id;
	private final String description;
	
	MeasurementKind(int id , String description) {
		this.id = id;
		this.description = description;
	}
	
	
	public static MeasurementKind get(int id) {
		for (MeasurementKind each : values()) {
			if (each.id == id) {
				return each;
			}
		}
        throw new IllegalEnumValueException(MeasurementKind.class, id);
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "MeasurementKind: " + id + " : " + description;
	}
	
	public boolean isApplicable() {
		return !(id == 0);
	}

    public int getId() {
        return id;
    }
}