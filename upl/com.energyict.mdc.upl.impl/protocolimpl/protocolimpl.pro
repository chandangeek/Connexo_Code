-dontshrink
-dontoptimize
-verbose

# Keep - Applications. Keep all application classes that have a main method.
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Also keep - Enumerations. Keep a method that is required in enumeration
# classes.
-keepclassmembers class * extends java.lang.Enum {
    public **[] values();
}

-keep interface *
-keep class * implements com.energyict.protocol.SmartMeterProtocol
-keep class * implements com.energyict.protocol.MeterProtocol

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepnames class test.com.*

-keepnames class com.energyict.protocolimpl.base.*

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.*

-keepnames class com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController  {
    public *;
    protected *;
}

-keepnames class com.energyict.protocolimpl.generic.*

-keepnames class com.energyict.protocolimpl.meteridentification.*

-keepnames class com.energyict.protocolimpl.iec1107identification.*

-keepnames class com.energyict.protocolimpl.metcom.Metcom3

-keepnames class com.energyict.protocolimpl.dlms.g3.registers.G3Mapping
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.*  {
	public *;
}
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper {
	public *;
	protected *;
}
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals
-keepnames class com.energyict.protocolimpl.dlms.idis.AM540ObjectList {
	public *;
}

-keepnames class com.energyict.protocolimpl.metcom.Metcom2

-keepnames class com.energyict.protocolimpl.rtuplusbus.rtuplusbus

# For usage in protocolimpl-v2
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMeterInfo {
    public *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMbusSerialNumber {
    public *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits {
    public *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject {
    public *;
}

-keepnames class com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping {
	public *;
	protected *;
}
-keepnames class com.energyict.protocolimpl.dlms.g3.registers.mapping.*
-keepnames class com.energyict.protocolimpl.dlms.idis.xml.XMLParser {
	public *;
}

# For usage in the cryptoserver project
-keepnames class com.energyict.protocolimpl.dlms.common.NTASecurityProvider
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40SecurityProvider
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties {
    public *;
}
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.Dsmr50Properties
-keepnames class com.energyict.protocolimpl.dlms.g3.G3SecurityProvider
-keepnames class com.energyict.protocol.MessageProtocol
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging
-keepnames class com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MbusMessageExecutor
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RespondingFrameCounterHandler
-keep public class com.energyict.protocolimpl.dlms.common.NTASecurityProvider {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.dlms.g3.G3Properties {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.dlms.g3.profile.G3Profile {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.dlms.g3.G3DeviceInfo {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.dlms.g3.profile.G3Profile {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.dlms.g3.events.* {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50MessageExecutor {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages.SagemComDsmr50MessageExecutor {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372 {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology {
	public *;
}
-keep public class com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.MbusDevice {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372 {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology {
	public *;
}
-keep public class com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.MbusDevice {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350 {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540 {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.AM540Messaging {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.CX20009 {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages.SagemComMessaging {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382 {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.generic.messages.MessageHandler {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor {
	public *;
	protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor {
	public *;
	protected *;
}
-keep public class com.energyict.protocolimpl.dlms.idis.events.* {
    public *;
    protected *;
}
-keep public class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology.DeviceMappingRange {
    public *;
}
-keep public class com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister {
    public *;
}
-keep public class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.events.EventsLog {
    public *;
}

-keepnames class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.events.EventsLog
-keepnames class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology.DeviceMappingRange
-keepnames class com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister

-keepnames class com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco

-keepnames class com.energyict.protocolimpl.siemens7ED62.Siemens7ED62

-keepnames class com.energyict.protocolimpl.dlms.DLMSEICT

-keepnames class com.energyict.protocolimpl.iec1107.abba1700.ABBA1700

-keepnames class com.energyict.protocolimpl.dlms.DLMSEMO

-keepnames class com.energyict.protocolimpl.dukepower.DukePower

-keepnames class com.energyict.protocolimpl.dlms.DLMSZMD

-keepnames class com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD

-keepnames class com.energyict.protocolimpl.actarissevc.SEVC

-keepnames class com.energyict.protocolimpl.dlms.DLMSLNSL7000

-keepnames class com.energyict.protocolimpl.pact.pripact.PRIPact

-keepnames class com.energyict.protocolimpl.iec870.datawatt.DataWatt

-keepnames class com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup

-keepnames class com.energyict.protocolimpl.dlms.edp.CX20009

-keepnames class com.energyict.protocolimpl.dlms.edp.JanzB280

-keepnames class com.energyict.protocolimpl.iec1107.abba1500.ABBA1500

-keepnames class com.energyict.protocolimpl.iec1107.ferranti.Ferranti

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.CX20009

-keepnames class com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62

-keepnames class com.energyict.protocolimpl.iec1107.indigo.IndigoPlus

-keepnames class com.energyict.protocolimpl.iec1107.ppm.PPM

-keepnames class com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x

-keepnames class com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X

-keepnames class com.energyict.protocolimpl.iec1107.ppmi1.PPM

-keepnames class com.energyict.protocolimpl.gmc.u1600.U1600

-keepnames class com.energyict.protocolimpl.sctm.ekm.EKM

-keepnames class com.energyict.protocolimpl.sctm.mtt3a.MTT3A

-keepnames class com.energyict.protocolimpl.sctm.fag.FAG

-keepnames class com.energyict.protocolimpl.sctm.faf.FAF20

-keepnames class com.energyict.protocolimpl.sctm.fbc.FBC

-keepnames class com.energyict.protocolimpl.sctm.fcl.FCL

-keepnames class com.energyict.protocolimpl.sctm.faf.FAF10

-keepnames class com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X

-keepnames class com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew

-keepnames class com.energyict.protocolimpl.iec1107.unilog.Unilog

-keepnames class com.energyict.protocolimpl.emon.ez7.EZ7

-keepnames class com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus

-keepnames class com.energyict.protocolimpl.transdata.markv.MarkV

-keepnames class com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu

-keepnames class com.energyict.protocolimpl.modbus.northerndesign.eimeterflex.EIMeterFlexSM352

-keepnames class com.energyict.protocolimpl.modbus.energyict.EIMeterFlexSlaveModule

-keepnames class com.energyict.protocolimpl.modbus.northerndesign.cube350.Cube350

-keepnames class com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic

-keepnames class com.energyict.protocolimpl.ge.kv.GEKV

-keepnames class com.energyict.protocolimpl.iec1107.a140.A140

-keepnames class com.energyict.protocolimpl.ge.kv2.GEKV2

-keepnames class com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd

-keepnames class com.energyict.protocolimpl.iec1107.abba1140.ABBA1140

-keepnames class com.energyict.protocolimpl.elster.a3.AlphaA3

-keepnames class com.energyict.protocolimpl.elster.a1800.A1800

-keepnames class com.energyict.protocolimpl.edmi.mk6.MK6

-keepnames class com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4

-keepnames class com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4

-keepnames class com.energyict.protocolimpl.edf.trimaran.Trimaran

-keepnames class com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys

-keepnames class com.energyict.protocolimpl.powermeasurement.ion.Ion

-keepnames class com.energyict.protocolimpl.landisgyr.sentry.s200.S200

-keepnames class com.energyict.protocolimpl.itron.fulcrum.Fulcrum

-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.AbstractEvent {
    public *;
}

-keepnames class com.energyict.protocolimpl.itron.quantum.Quantum

-keepnames class com.energyict.protocolimpl.itron.vectron.Vectron

-keepnames class com.energyict.protocolimpl.itron.datastar.Datastar

-keepnames class com.energyict.protocolimpl.itron.sentinel.Sentinel

-keepnames class com.energyict.protocolimpl.itron.quantum1000.Quantum1000

-keepnames class com.energyict.protocolimpl.enermet.e120.E120

-keepnames class com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer

-keepnames class com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer

-keepnames class com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus

-keepnames class com.energyict.protocolimpl.iec1107.abba1350.ABBA1350

-keepnames class com.energyict.protocolimpl.iec1107.zmd.Zmd

-keepnames class com.energyict.protocolimpl.modbus.squared.pm800.PM800

-keepnames class com.energyict.protocolimpl.modbus.ge.pqm2.PQM2

-keepnames class com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X

-keepnames class com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE

-keepnames class com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr

-keepnames class com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800

-keepnames class com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct

-keepnames class com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower

-keepnames class com.energyict.protocolimpl.mbus.generic.Generic

-keepnames class com.energyict.protocolimpl.mbus.nzr.pn16.PN16

-keep public class com.energyict.protocolimpl.mbus.core.* {
	public *;
}

-keep public class com.energyict.protocolimpl.dlms.* {
	public *;
}

-keepnames class com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770

-keepnames class com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ

-keepnames class com.energyict.protocolimpl.instromet.v555.Instromet555

-keepnames class com.energyict.protocolimpl.instromet.v444.Instromet444

-keepnames class com.energyict.protocolimpl.iec1107.abba230.ABBA230

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.MBusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.elster.MBusDevice

-keepnames class com.energyict.protocolimpl.dlms.idis.IDIS {
    public *;   # For usage in protocolimpl-v2
}
-keepnames class com.energyict.protocolimpl.dlms.idis.registers.* {
    public *;
}
-keepnames class com.energyict.protocolimpl.dlms.idis.IDISObjectList {
    public *;
}

-keepnames class com.energyict.protocolimpl.dlms.idis.IDISMBus

# Moved it to the commons package
#-keepnames class SDKSampleProtocol

-keepnames class test.com.energyict.protocolimpl.eicttest.EICTTestProtocol

-keepnames class com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging

-keepnames class com.energyict.protocolimpl.elster.opus.Opus

-keepnames class com.energyict.protocolimpl.kenda.medo.Medo

-keepnames class com.energyict.protocolimpl.kenda.meteor.Meteor

-keepnames class com.energyict.protocolimpl.edmi.mk10.MK10

-keepnames class com.energyict.protocolimpl.cm10.CM10

-keepnames class com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83

-keepnames class com.energyict.protocolimpl.iec1107.sdc.Sdc

-keepnames class com.energyict.protocolimpl.ametek.Jem10

-keepnames class  com.energyict.protocolimpl.ametek.JemStar

-keepnames class  com.energyict.protocolimpl.eig.nexus1272.Nexus1272

-keepnames class com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200

-keep public class com.energyict.protocolimpl.base.* {
	*;
}

-keepnames class com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging

-keepnames class com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx

-keep class com.energyict.protocolimpl.dlms.eictz3.EictZ3 {
	public *;
}

-keepnames class com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200

-keepnames class com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150

-keepnames class com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50

-keepnames class com.energyict.protocolimpl.dlms.actarisace6000.ACE6000

-keepnames class com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX

-keepnames class com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300

-keep public class com.energyict.protocolimpl.dlms.as220.*{
	*;
}
-keep public class com.energyict.protocolimpl.dlms.as220.emeter.*{
	*;
}
-keep public class com.energyict.protocolimpl.dlms.as220.gmeter.*{
	*;
}
-keep public class com.energyict.protocolimpl.dlms.as220.plc.*{
	*;
}
-keep public class com.energyict.protocolimpl.dlms.as220.plc.statistics.*{
	*;
}
#-keepnames class com.energyict.protocolimpl.dlms.as220.AS220

#-keep public class com.energyict.protocolimpl.dlms.as220.DLMSSNAS220{
#	public *;
#}

# Keep annotations, 
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes Signature

-keepnames class com.vodafone.gdsp.ws.package-info
-keep public class com.vodafone.gdsp.ws.*{
	*;
}

-keepnames class com.energyict.protocolimpl.modbus.socomec.a40.A40

-keepnames class com.energyict.protocolimpl.modbus.socomec.a20.A20

-keepnames class com.energyict.protocolimpl.iec1107.as220.AS220

-keepnames class com.energyict.protocolimpl.iec1107.a1440.A1440

-keep public class com.energyict.smartmeterprotocolimpl.eict.ukhub.* {
	*;
}


-keep public class com.energyict.protocolimpl.generic.messages.MessageHandler

-keep public class com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging.* {
	*;
}

-keepnames class com.energyict.protocolimpl.modbus.socomec.countis.ci.Ci

-keepnames class com.energyict.protocolimpl.dlms.as220.GasDevice

-keepnames class com.energyict.protocolimpl.iec1107.indigo.pxar.IndigoPXAR

#-keepnames class DL220
-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.*{
	*;
}

-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.commands.*{
	*;
}

-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.objects.*{
	*;
}

-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.profile.*{
	*;
}
-keep public class com.energyict.protocolimpl.iec1107.instromet.dl220.registers.*{
	*;
}

-keepnames class test.com.energyict.protocolimpl.dlms.SimpleDLMSProtocol

-keepnames class com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent.SM150E

-keepnames class com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis.Echodis

-keepnames class com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW

-keepnames class com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253

-keepnames class com.energyict.protocolimpl.coronis.waveflowDLMS.A1800

-keepnames class com.energyict.protocolimpl.coronis.waveflowDLMS.AS253

-keepnames class com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2

-keepnames class com.energyict.protocolimpl.coronis.waveflow.waveflowV210.WaveFlowV210

-keepnames class com.energyict.protocolimpl.coronis.wavesense.WaveSense

-keepnames class com.energyict.protocolimpl.coronis.amco.rtm.RTM

-keepnames class com.energyict.protocolimpl.din19244.poreg2.Poreg2

-keepnames class com.energyict.protocolimpl.din19244.poreg2.Poreg2P

-keepnames class com.energyict.protocolimpl.coronis.wavelog.WaveLog

-keepnames class com.energyict.protocolimpl.coronis.wavetalk.WaveTalk

-keepnames class com.energyict.protocolimpl.coronis.wavetherm.WaveTherm

-keepnames class com.energyict.protocolimpl.dlms.elgama.G3B

# Keep the parsing of the CodeTableToXML
-keep public class com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml{
    public *;
}

-keepnames class com.energyict.protocolimpl.coronis.core.RegisterCache

-keepclasseswithmembers public class com.energyict.protocolimpl.coronis.core.RegisterCache {
    public void cacheRegisters(java.util.List);
}

-keepnames class test.com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol

-keepnames class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3

-keepnames class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.EMeter

-keepnames class com.energyict.smartmeterprotocolimpl.eict.webrtuz3.MbusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.elster.apollo.AS300

-keepnames class com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300P

-keepnames class com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub

-keepnames class com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas

-keepnames class com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay

-keepnames class com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R

-keepnames class com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ZigbeeGas

-keepnames class com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.InHomeDisplay

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.MbusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.MbusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.WatchTalk

-keepnames class com.energyict.protocolimpl.base.RtuDiscoveredEvent

-keepnames class com.energyict.protocolimpl.dlms.JanzC280.JanzC280

-keepnames class com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372

-keepnames class com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice

-keepnames class com.energyict.protocolimpl.EMCO.FP93

-keepnames class com.energyict.protocolimpl.modbus.multilin.epm2200.EPM2200

-keepnames class com.energyict.smartmeterprotocolimpl.actaris.sl7000.ActarisSl7000

-keepnames class com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol

-keepnames class com.energyict.genericprotocolimpl.gatewayz3.GateWayZ3

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.MBusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.REMIDatalogger

-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.MbusDevice

-keepnames class com.energyict.smartmeterprotocolimpl.iskra.mt880.IskraMT880

-keepnames class com.energyict.smartmeterprotocolimpl.iskra.mt880.IskraMT880

-keepnames class com.energyict.protocolimpl.modbus.generic.Generic

-keepnames class com.energyict.protocolimpl.dlms.a1800.A1800

-keepnames class com.energyict.protocolimpl.modbus.socomec.countis.e44.E44

-keepnames class com.energyict.protocolimpl.iec1107.emh.nxt4.NXT4

-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.MbusControlLog { public *; protected *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.DisconnectControlLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540FraudDetectionLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540StandardEventLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540MBusLog { public *; }
-keep public class com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540MbusControlLog { public *; }

-keepnames class com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5560
-keepnames class com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.E35C