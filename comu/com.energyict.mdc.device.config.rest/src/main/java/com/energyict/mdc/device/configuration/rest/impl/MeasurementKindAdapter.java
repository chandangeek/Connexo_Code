package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.MeasurementKind;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class MeasurementKindAdapter extends MapBasedXmlAdapter<MeasurementKind> {

    public MeasurementKindAdapter() {
        register("",MeasurementKind.NOTAPPLICABLE );
        register("Not applicable",MeasurementKind.NOTAPPLICABLE );
        register("Apparent power factor",MeasurementKind.APPARENTPOWERFACTOR );
        register("Currency",MeasurementKind.CURRENCY );
        register("Current",MeasurementKind.CURRENT );
        register("Current angle",MeasurementKind.CURRENTANGLE );
        register("Current imbalance",MeasurementKind.CURRENTIMBALANCE );
        register("Date",MeasurementKind.DATE );
        register("Demand",MeasurementKind.DEMAND );
        register("Distance",MeasurementKind.DISTANCE );
        register("Distortion volt/ampere",MeasurementKind.DISTORTIONVOLTAMPERES );
        register("Energization",MeasurementKind.ENERGIZATION );
        register("Energy",MeasurementKind.ENERGY );
        register("Energization load side",MeasurementKind.ENERGIZATIONLOADSIDE );
        register("Fan",MeasurementKind.FAN );
        register("Frequency",MeasurementKind.FREQUENCY );
        register("Funds",MeasurementKind.FUNDS );
        register("IEEE1366 ASAI",MeasurementKind.IEEE1366ASAI );
        register("IEEE1366 SIDI",MeasurementKind.IEEE1366SIDI );
        register("IEEE1366 ASIFI",MeasurementKind.IEEE1366ASIFI );
        register("IEEE1366 CAIDI",MeasurementKind.IEEE1366CAIDI );
        register("IEEE1366 CAIFI",MeasurementKind.IEEE1366CAIFI );
        register("IEEE1366 CEMIN",MeasurementKind.IEEE1366CEMIN );
        register("IEEE1366 CEMSMIN",MeasurementKind.IEEE1366CEMSMIN );
        register("IEEE1366 CTAIDI",MeasurementKind.IEEE1366CTAIDI );
        register("IEEE1366 MAIFI",MeasurementKind.IEEE1366MAIFI );
        register("IEEE1366 MAIFIE",MeasurementKind.IEEE1366MAIFIE );
        register("IEEE1366 SAIDI",MeasurementKind.IEEE1366SAIDI );
        register("IEEE1366 SAIFI",MeasurementKind.IEEE1366SAIFI );
        register("Line losses",MeasurementKind.LINELOSSES );
        register("Losses",MeasurementKind.LOSSES );
        register("Negative sequence",MeasurementKind.NEGATIVESEQUENCE );
        register("Phasor power factor",MeasurementKind.PHASORPOWERFACTOR );
        register("Phasor reactive power",MeasurementKind.PHASORREACTIVEPOWER );
        register("Positive sequence",MeasurementKind.POSITIVESEQUENCE );
        register("Power",MeasurementKind.POWER );
        register("Power factor",MeasurementKind.POWERFACTOR );
        register("Quantity power",MeasurementKind.QUANTITYPOWER );
        register("Sag",MeasurementKind.SAG );
        register("Swell",MeasurementKind.SWELL );
        register("Switch position",MeasurementKind.SWITCHPOSITION );
        register("Tap position",MeasurementKind.TAPPOSITION );
        register("Tariff rate",MeasurementKind.TARIFFRATE );
        register("Temperature",MeasurementKind.TEMPERATURE );
        register("Total harmonic distortion",MeasurementKind.TOTALHARMONICDISTORTION );
        register("Transformer losses",MeasurementKind.TRANSFORMERLOSSES );
        register("Unipede voltage DIP10TO15",MeasurementKind.UNIPEDEVOLTAGEDIP10TO15 );
        register("Unipede voltage DIP15TO30",MeasurementKind.UNIPEDEVOLTAGEDIP15TO30 );
        register("Unipede voltage DIP30TO60",MeasurementKind.UNIPEDEVOLTAGEDIP30TO60 );
        register("Unipede voltage DIP60TO90",MeasurementKind.UNIPEDEVOLTAGEDIP60TO90 );
        register("Unipede voltage DIP90TO100",MeasurementKind.UNIPEDEVOLTAGEDIP90TO100 );
        register("Voltage (rms)",MeasurementKind.RMSVOLTAGE );
        register("Voltage angle",MeasurementKind.VOLTAGEANGLE );
        register("Voltage excursion",MeasurementKind.VOLTAGEEXCURSION );
        register("Voltage imbalance",MeasurementKind.VOLTAGEIMBALANCE );
        register("Volume",MeasurementKind.VOLUME );
        register("Zero flow duration",MeasurementKind.ZEROFLOWDURATION );
        register("Zero sequence",MeasurementKind.ZEROSEQUENCE );
        register("Distortion power factor",MeasurementKind.DISTORTIONPOWERFACTOR);
        register("Frequency excursion",MeasurementKind.FREQUENCYEXCURSION);
        register("Application context",MeasurementKind.APPLICATIONCONTEXT );
        register("Application title",MeasurementKind.APTITLE );
        register("Asset number",MeasurementKind.ASSETNUMBER );
        register("Bandwidth",MeasurementKind.BANDWIDTH );
        register("Battery voltage",MeasurementKind.BATTERYVOLTAGE );
        register("Broadcast address",MeasurementKind.BROADCASTADDRESS );
        register("Device address type 1",MeasurementKind.DEVICEADDRESSTYPE1 );
        register("Device address type 2",MeasurementKind.DEVICEADDRESSTYPE2 );
        register("Device address type 3",MeasurementKind.DEVICEADDRESSTYPE3 );
        register("Device address type 4",MeasurementKind.DEVICEADDRESSTYPE4 );
        register("Device class",MeasurementKind.DEVICECLASS );
        register("Electronic serial number",MeasurementKind.ELECTRONICSERIALNUMBER );
        register("End device ID",MeasurementKind.ENDDEVICEID );
        register("Group address type 1",MeasurementKind.GROUPADDRESSTYPE1 );
        register("Group address type 2",MeasurementKind.GROUPADDRESSTYPE2 );
        register("Group address type 3",MeasurementKind.GROUPADDRESSTYPE3 );
        register("Group address type 4",MeasurementKind.GROUPADDRESSTYPE4 );
        register("IP address",MeasurementKind.IPADDRESS );
        register("MAC address",MeasurementKind.MACADDRESS );
        register("Manufacturing assigned configuration ID",MeasurementKind.MFGASSIGNEDCONFIGURATIONID );
        register("Manufacturing assigned physical serial number",MeasurementKind.MFGASSIGNEDPHYSICALSERIALNUMBER );
        register("Manufacturing assigned product number",MeasurementKind.MFGASSIGNEDPRODUCTNUMBER );
        register("Manufacturing assigned communication address",MeasurementKind.MFGASSIGNEDUNIQUECOMMUNICATIONADDRESS );
        register("Multicast address",MeasurementKind.MULITCASTADDRESS );
        register("One way address",MeasurementKind.ONEWAYADDRESS );
        register("Signal strength",MeasurementKind.SIGNALSTRENGTH );
        register("Two way address",MeasurementKind.TWOWAYADDRESS );
        register("Alarm",MeasurementKind.ALARM );
        register("Battery carry over",MeasurementKind.BATTERYCARRYOVER );
        register("Data overflow alarm",MeasurementKind.DATAOVERFLOWALARM );
        register("Demand limit",MeasurementKind.DEMANDLIMIT );
        register("Demand reset",MeasurementKind.DEMANDRESET );
        register("Diagnostic",MeasurementKind.DIAGNOSTIC );
        register("Emergency limit",MeasurementKind.EMERGENCYLIMIT );
        register("Encoder tamper",MeasurementKind.ENCODERTAMPER );
        register("IEEE1366 momentary interruption",MeasurementKind.IEEE1366MOMENTARYINTERRUPTION );
        register("IEEE1366 momentary interruption event",MeasurementKind.IEEE1366MOMENTARYINTERRUPTIONEVENT );
        register("IEEE1366 sustained interruption",MeasurementKind.IEEE1366SUSTAINEDINTERRUPTION );
        register("Interruption behaviour",MeasurementKind.INTERRUPTIONBEHAVIOUR );
        register("Inversion tamper",MeasurementKind.INVERSIONTAMPER );
        register("Load interrupt",MeasurementKind.LOADINTERRUPT );
        register("Load shed",MeasurementKind.LOADSHED );
        register("Maintenance",MeasurementKind.MAINTENANCE );
        register("Physical tamper",MeasurementKind.PHYSICALTAMPER );
        register("PowerLoss tamper",MeasurementKind.POWERLOSSTAMPER );
        register("Power outage",MeasurementKind.POWEROUTAGE );
        register("Power quality",MeasurementKind.POWERQUALITY );
        register("Power restoration",MeasurementKind.POWERRESTORATION );
        register("Programmed",MeasurementKind.PROGRAMMED );
        register("Push-button",MeasurementKind.PUSHBUTTON );
        register("Relay activation",MeasurementKind.RELAYACTIVATION );
        register("Relay cycle",MeasurementKind.RELAYCYCLE );
        register("Removal tamper",MeasurementKind.REMOVALTAMPER );
        register("Reprogramming tamper",MeasurementKind.REPROGRAMMINGTAMPER );
        register("Reverse rotation tamper",MeasurementKind.REVERSEROTATIONTAMPER );
        register("Switch armed",MeasurementKind.SWITCHARMED );
        register("Switch disabled",MeasurementKind.SWITCHDISABLED );
        register("Tamper",MeasurementKind.TAMPER );
        register("Watchdog timeout",MeasurementKind.WATCHDOGTIMEOUT );
        register("Bill last period",MeasurementKind.BILLLASTPERIOD);
        register("Bill to date",MeasurementKind.BILLTODATE);
        register("Bill carry over",MeasurementKind.BILLCARRYOVER);
        register("Connection fee",MeasurementKind.CONNECTIONFEE);
        register("Audible volume",MeasurementKind.AUDIBLEVOLUME);
        register("Volume metric flow",MeasurementKind.VOLUMETRICFLOW);
        register("Relative humidity",MeasurementKind.RELATIVEHUMIDITY);
        register("sky cover",MeasurementKind.SKYCOVER);
        register("voltage",MeasurementKind.VOLTAGE);
        register("dc voltage",MeasurementKind.DCVOLTAGE);
        register("ac voltage peak",MeasurementKind.ACVOLTAGEPEAK);
        register("ac voltage peak to peak",MeasurementKind.ACVOLTAGEPEAKTOPEAK);    }
}
