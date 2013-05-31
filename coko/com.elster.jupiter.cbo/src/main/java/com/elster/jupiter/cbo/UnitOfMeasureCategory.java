package com.elster.jupiter.cbo;

public enum UnitOfMeasureCategory {
	NOTAPPLICABLE (0,0, "NotApplicable"),
	AIR (0,1, "Air"),
	APPARENTPOWERFACTOR (0,2, "ApparentPowerFactor"),
	CURRENCY (0,3, "Currency"),
	CURRENT (0,4, "Current"),
	CURRENTANGLE (0,5, "CurrentAngle"),
	CURRENTIMBALANCE (0,6, "CurrentImbalance"),
	DATE (0,7, "Date"),
	DEMAND (0,8, "Demand"),
	DISTANCE (0,9, "Distance"),
	DISTORTIONVOLTAMPERES (0,10, "DistortionVoltAmpere"),
	ENERGIZATION (0,11, "Energization"),
	ENERGY (0,12, "Energy"),
	ENERGIZATIONLOADSIDE (0,13, "EnergizationLoadSide"),
	FAN (0,14, "Fan"),
	FREQUENCY (0,15, "Frequency"),
	FUNDS (0,16, "Funds"),
	IEEE1366ASAI (0,17, "IEEE1366ASAI"),
	IEEE1366SIDI (0,18, "IEEE1366SIDI"),
	IEEE1366ASIFI (0,19, "IEEE1366ASIFI"),
	IEEE1366CAIDI (0,20, "IEEE1366CAIDI"),
	IEEE1366CAIFI (0,21, "IEEE1366CAIFI"),
	IEEE1366CEMIN (0,22, "IEEE1366CEMIN"),
	IEEE1366CEMSMIN (0,23, "IEEE1366CEMSMIN"),
	IEEE1366CTAIDI (0,24, "IEEE1366CTAIDI"),
	IEEE1366MAIFI (0,25, "IEEE1366MAIFI"),
	IEEE1366MAIFIE (0,26, "IEEE1366MAIFIE"),
	IEEE1366SAIDI (0,27, "IEEE1366SAIDI"),
	IEEE1366SAIFI (0,28, "IEEE1366SAIFI"),
	INSULATIVEGAS (0,29, "InsulativeGas"),
	INSULATIVEOIL (0,30, "InsualtiveOil"),
	LINELOSSES (0,31, "LineLosses"),
	LOSSES (0,32, "Losses"),
	NEGATIVESEQUENCE (0,33, "NegativeSequence"),
	PHASORPOWERFACTOR (0,34, "PhasorPowerFactor"),
	PHASORREACTIVEPOWER (0,35, "PhasorReactivePower"),
	POSITIVESEQUENCE (0,36, "PositiveSequence"),
	POWER (0,37, "Power"),
	POWERFACTOR (0,38, "PowerFactor"),
	PRICE (0,39, "Price"),
	QUANTITYPOWER (0,40, "QuantityPower"),
	SAG (0,41, "Sag"),
	SWELL (0,42, "Swell"),
	SWITCHPOSITION (0,43, "SwitchPosition"),
	TAPPOSITION (0,44, "TapPosition"),
	TARIFFRATE (0,45, "TariffRate"),
	TEMPERATURE (0,46, "Temperature"),
	TOTALHARMONICDISTORTION (0,47, "TotalHarmonicDistortion"),
	TRANSFORMERLOSSES (0,48, "TransformerLosses"),
	UNIPEDEVOLTAGEDIP10TO15 (0,49, "UNIPEDEVOLTAGEDIP10TO15"),
	UNIPEDEVOLTAGEDIP15TO30 (0,50, "UNIPEDEVOLTAGEDIP15TO30"),
	UNIPEDEVOLTAGEDIP30TO60 (0,51, "UNIPEDEVOLTAGEDIP30TO60"),
	UNIPEDEVOLTAGEDIP60TO90 (0,52, "UNIPEDEVOLTAGEDIP60TO90"),
	UNIPEDEVOLTAGEDIP90TO100 (0,53, "UNIPEDEVOLTAGEDIP90TO100"),
	VOLTAGE (0,54, "Voltage"),
	VOLTAGEANGLE (0,55,"VoltageAngle"),
	VOLTAGEEXCURSION (0,56, "VoltageExcursion"),
	VOLTAGEIMBALANCE5 (0,57, "VoltageImbalance"),
	VOLUME (0,58, "Volume"),
	ZEROFLOWDURATION (0,59, "ZeroFlowDuration"),
	ZEROSEQUENCE (0,60, "ZeroSequence"), 
	APPLICATIONCONTEXT (1,0, "ApplicationContext"),
	APTITLE (1,1, "ApplicationTitle"),
	ASSETNUMBER (1,2, "AssetNumber"),
	BANDWIDTH (1,3, "Bandwidth"),
	BATTERYVOLTAGE (1,4, "BatteryVoltage"),
	BROADCASTADDRESS (1,5, "BroadcastAddress"),
	DEVICEADDRESSTYPE1 (1,6, "DeviceAddressType1"),
	DEVICEADDRESSTYPE2 (1,7, "DeviceAddressType2"),
	DEVICEADDRESSTYPE3 (1,8, "DeviceAddressType3"),
	DEVICEADDRESSTYPE4 (1,9, "DeviceAddressType4"),
	DEVICECLASS (1,10, "DeviceClass"),
	ELECTRONICSERIALNUMBER (1,11, "ElectronicSerialNumber"),
	ENDDEVICEID (1,12, "EndDeviceID"),
	GROUPADDRESSTYPE1 (1,13, "GroupAddressType1"),
	GROUPADDRESSTYPE2 (1,14, "GroupAddressType2"),
	GROUPADDRESSTYPE3 (1,15, "GroupAddressType3"),
	GROUPADDRESSTYPE4 (1,16, "GroupAddressType4"),
	IPADDRESS (1,17, "IPAddress"),
	MACADDRESS (1,18, "MACAddress"),
	MFGASSIGNEDCONFIGURATIONID (1,19, "ManufacturingAssignedConfigurationID"),
	MFGASSIGNEDPHYSICALSERIALNUMBER (1,20, "ManufacturingAssignedPhysicalSerialNumber"),
	MFGASSIGNEDPRODUCTNUMBER (1,21, "ManufacturingAssignedProductNumber"),
	MFGASSIGNEDUNIQUECOMMUNICATIONADDRESS (1,22, "ManufacturingAssignedCommunicationAddress"),
	MULITCASTADDRESS (1,23, "MulticastAddress"),
	ONEWAYADDRESS (1,24, "OneWayAddress"),
	SIGNALSTRENGTH (1,25, "SignalStrength"),
	TWOWAYADDRESS (1,26, "TwoWayAddress") ,
	ALARM (2,0, "Alarm"),
	BATTERYCARRYOVER (2,1, "BatteryCarryOver"),
	DATAOVERFLOWALARM (2,2, "DataOverflowAlarm"),
	DEMANDLIMIT (2,3, "DemandLimit"),
	DEMANDRESET (2,4, "DemandReset"),
	DIAGNOSTIC (2,5, "Diagnostic"),
	EMERGENCYLIMIT (2,6, "EmergencyLimit"),
	ENCODERTAMPER (2,7, "EncoderTamper"),
	IEEE1366MOMENTARYINTERRUPTION (2,8, "IEEE1366MomentaryInterruption"),
	IEEE1366MOMENTARYINTERRUPTIONEVENT (2,9, "IEEE1366MomentaryInterruptionEvent"),
	IEEE1366SUSTAINEDINTERRUPTION (2,10, "IEEE1366SustainedInterruption"),
	INTERRUPTIONBEHAVIOUR (2,11, "InterruptionBehaviour"),
	INVERSIONTAMPER (2,12, "InversionTamper"),
	LOADINTERRUPT (2,13, "LoadInterrupt"),
	LOADSHED (2,14, "LoadShed"),
	MAINTENANCE (2,15, "Maintenance"),
	PHYSICALTAMPER (2,16, "PhysicalTamper"),
	POWERLOSSTAMPER (2,17, "PowerLossTamper"),
	POWEROUTAGE (2,18, "PowerOutage"),
	POWERQUALITY (2,19, "PowerQuality"),
	POWERRESTORATION (2,20, "PowerRestoration"),
	PROGRAMMED (2,21, "Programmed"),
	PUSHBUTTON (2,22, "Pushbutton"),
	RELAYACTIVATION (2,23, "RelayActivation"),
	RELAYCYCLE (2,24, "RelayCycle"),
	REMOVALTAMPER (2,25, "RemovalTamper"),
	REPROGRAMMINGTAMPER (2,26, "ReprogrammingTamper"),
	REVERSEROTATIONTAMPER (2,27, "ReverseRotationTamper"),
	SWITCHARMED (2,30, "SwitchArmed"),
	SWITCHDISABLED (2,31, "SwitchDisabled"),
	TAMPER (2,32, "Tamper"),
	WATCHDOGTIMEOUT (2,33, "WatchdogTimeout");

	private final int subClassId;
	private final int id;
	private final String description;
	
	private UnitOfMeasureCategory(int subClassId, int id , String description) {
		this.subClassId = subClassId;
		this.id = id;
		this.description = description;
	}
	
	
	public static UnitOfMeasureCategory get(int subClassId, int id) {
		for (UnitOfMeasureCategory each : values()) {
			if (each.subClassId == subClassId && each.id == id) {
				return each;
			}
		}
		throw new IllegalArgumentException("" + subClassId + "." + id);
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "Unit of Measure Category " + subClassId + "." + id + " : " + description;
	}
	
	public boolean isApplicable() {
		return !(subClassId == 0 && id == 0);
	}

}