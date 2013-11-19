package com.elster.jupiter.cbo;

public enum MeasurementKind {
	NOTAPPLICABLE (0, "NotApplicable"),
	APPARENTPOWERFACTOR (2, "ApparentPowerFactor"),
	CURRENCY (3, "Currency"),
	CURRENT (4, "Current"),
	CURRENTANGLE (5, "CurrentAngle"),
	CURRENTIMBALANCE (6, "CurrentImbalance"),
	DATE (7, "Date"),
	DEMAND (8, "Demand"),
	DISTANCE (9, "Distance"),
	DISTORTIONVOLTAMPERES (10, "DistortionVoltAmpere"),
	ENERGIZATION (11, "Energization"),
	ENERGY (12, "Energy"),
	ENERGIZATIONLOADSIDE (13, "EnergizationLoadSide"),
	FAN (14, "Fan"),
	FREQUENCY (15, "Frequency"),
	FUNDS (16, "Funds"),
	IEEE1366ASAI (17, "IEEE1366ASAI"),
	IEEE1366SIDI (18, "IEEE1366SIDI"),
	IEEE1366ASIFI (19, "IEEE1366ASIFI"),
	IEEE1366CAIDI (20, "IEEE1366CAIDI"),
	IEEE1366CAIFI (21, "IEEE1366CAIFI"),
	IEEE1366CEMIN (22, "IEEE1366CEMIN"),
	IEEE1366CEMSMIN (23, "IEEE1366CEMSMIN"),
	IEEE1366CTAIDI (24, "IEEE1366CTAIDI"),
	IEEE1366MAIFI (25, "IEEE1366MAIFI"),
	IEEE1366MAIFIE (26, "IEEE1366MAIFIE"),
	IEEE1366SAIDI (27, "IEEE1366SAIDI"),
	IEEE1366SAIFI (28, "IEEE1366SAIFI"),
	LINELOSSES (31, "LineLosses"),
	LOSSES (32, "Losses"),
	NEGATIVESEQUENCE (33, "NegativeSequence"),
	PHASORPOWERFACTOR (34, "PhasorPowerFactor"),
	PHASORREACTIVEPOWER (35, "PhasorReactivePower"),
	POSITIVESEQUENCE (36, "PositiveSequence"),
	POWER (37, "Power"),
	POWERFACTOR (38, "PowerFactor"),	
	QUANTITYPOWER (40, "QuantityPower"),
	SAG (41, "Sag"),
	SWELL (42, "Swell"),
	SWITCHPOSITION (43, "SwitchPosition"),
	TAPPOSITION (44, "TapPosition"),
	TARIFFRATE (45, "TariffRate"),
	TEMPERATURE (46, "Temperature"),
	TOTALHARMONICDISTORTION (47, "TotalHarmonicDistortion"),
	TRANSFORMERLOSSES (48, "TransformerLosses"),
	UNIPEDEVOLTAGEDIP10TO15 (49, "UNIPEDEVOLTAGEDIP10TO15"),
	UNIPEDEVOLTAGEDIP15TO30 (50, "UNIPEDEVOLTAGEDIP15TO30"),
	UNIPEDEVOLTAGEDIP30TO60 (51, "UNIPEDEVOLTAGEDIP30TO60"),
	UNIPEDEVOLTAGEDIP60TO90 (52, "UNIPEDEVOLTAGEDIP60TO90"),
	UNIPEDEVOLTAGEDIP90TO100 (53, "UNIPEDEVOLTAGEDIP90TO100"),
	VOLTAGE (54, "Voltage"),
	VOLTAGEANGLE (55,"VoltageAngle"),
	VOLTAGEEXCURSION (56, "VoltageExcursion"),
	VOLTAGEIMBALANCE5 (57, "VoltageImbalance"),
	VOLUME (58, "Volume"),
	ZEROFLOWDURATION (59, "ZeroFlowDuration"),
	ZEROSEQUENCE (60, "ZeroSequence"),
	DISTORTIONPOWERFACTOR(64,"DistortionPowerFactor"),
	FREQUENCYEXCURSION(81,"FrequencyExcursion"),
	APPLICATIONCONTEXT (90, "ApplicationContext"),
	APTITLE (91, "ApplicationTitle"),
	ASSETNUMBER (92, "AssetNumber"),
	BANDWIDTH (93, "Bandwidth"),
	BATTERYVOLTAGE (94, "BatteryVoltage"),
	BROADCASTADDRESS (95, "BroadcastAddress"),
	DEVICEADDRESSTYPE1 (96, "DeviceAddressType1"),
	DEVICEADDRESSTYPE2 (97, "DeviceAddressType2"),
	DEVICEADDRESSTYPE3 (98, "DeviceAddressType3"),
	DEVICEADDRESSTYPE4 (99, "DeviceAddressType4"),
	DEVICECLASS (100, "DeviceClass"),
	ELECTRONICSERIALNUMBER (101, "ElectronicSerialNumber"),
	ENDDEVICEID (102, "EndDeviceID"),
	GROUPADDRESSTYPE1 (103, "GroupAddressType1"),
	GROUPADDRESSTYPE2 (104, "GroupAddressType2"),
	GROUPADDRESSTYPE3 (105, "GroupAddressType3"),
	GROUPADDRESSTYPE4 (106, "GroupAddressType4"),
	IPADDRESS (107, "IPAddress"),
	MACADDRESS (108, "MACAddress"),
	MFGASSIGNEDCONFIGURATIONID (109, "ManufacturingAssignedConfigurationID"),
	MFGASSIGNEDPHYSICALSERIALNUMBER (110, "ManufacturingAssignedPhysicalSerialNumber"),
	MFGASSIGNEDPRODUCTNUMBER (111, "ManufacturingAssignedProductNumber"),
	MFGASSIGNEDUNIQUECOMMUNICATIONADDRESS (112, "ManufacturingAssignedCommunicationAddress"),
	MULITCASTADDRESS (113, "MulticastAddress"),
	ONEWAYADDRESS (114, "OneWayAddress"),
	SIGNALSTRENGTH (115, "SignalStrength"),
	TWOWAYADDRESS (116, "TwoWayAddress") ,
	SIGNALNOISERATION(117,"SignalNoiseRatio"),
	ALARM (118, "Alarm"),
	BATTERYCARRYOVER (119, "BatteryCarryOver"),
	DATAOVERFLOWALARM (120, "DataOverflowAlarm"),
	DEMANDLIMIT (121, "DemandLimit"),
	DEMANDRESET (122, "DemandReset"),
	DIAGNOSTIC (123, "Diagnostic"),
	EMERGENCYLIMIT (124, "EmergencyLimit"),
	ENCODERTAMPER (125, "EncoderTamper"),
	IEEE1366MOMENTARYINTERRUPTION (126, "IEEE1366MomentaryInterruption"),
	IEEE1366MOMENTARYINTERRUPTIONEVENT (127, "IEEE1366MomentaryInterruptionEvent"),
	IEEE1366SUSTAINEDINTERRUPTION (128, "IEEE1366SustainedInterruption"),
	INTERRUPTIONBEHAVIOUR (129, "InterruptionBehaviour"),
	INVERSIONTAMPER (130, "InversionTamper"),
	LOADINTERRUPT (131, "LoadInterrupt"),
	LOADSHED (132, "LoadShed"),
	MAINTENANCE (133, "Maintenance"),
	PHYSICALTAMPER (134, "PhysicalTamper"),
	POWERLOSSTAMPER (135, "PowerLossTamper"),
	POWEROUTAGE (136, "PowerOutage"),
	POWERQUALITY (137, "PowerQuality"),
	POWERRESTORATION (138, "PowerRestoration"),
	PROGRAMMED (139, "Programmed"),
	PUSHBUTTON (140, "Pushbutton"),
	RELAYACTIVATION (141, "RelayActivation"),
	RELAYCYCLE (142, "RelayCycle"),
	REMOVALTAMPER (143, "RemovalTamper"),
	REPROGRAMMINGTAMPER (144, "ReprogrammingTamper"),
	REVERSEROTATIONTAMPER (145, "ReverseRotationTamper"),
	SWITCHARMED (146, "SwitchArmed"),
	SWITCHDISABLED (147, "SwitchDisabled"),
	TAMPER (148, "Tamper"),
	WATCHDOGTIMEOUT (149, "WatchdogTimeout"),
	BILLLASTPERIOD(150,"BillLastPeriod"),
	BILLTODATE(151,"BillToDate"),
	BILLCARRYOVER(152,"BillCarryOver"),
	CONNECTIONFEE(153,"ConnectionFee"),
	AUDIBLEVOLUME(154,"AudibleVolume"),
	VOLUMETRICFLOW(155,"VolumeMetricFlow");

	private final int id;
	private final String description;
	
	private MeasurementKind(int id , String description) {
		this.id = id;
		this.description = description;
	}
	
	
	public static MeasurementKind get(int id) {
		for (MeasurementKind each : values()) {
			if (each.id == id) {
				return each;
			}
		}
		throw new IllegalArgumentException("" + id);
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

}