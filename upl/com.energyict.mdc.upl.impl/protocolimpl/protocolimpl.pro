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

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepnames class com.energyict.protocolimpl.base.*

-keepnames class com.energyict.protocolimpl.meteridentification.*

-keepnames class com.energyict.protocolimpl.iec1107identification.*

-keepnames class com.energyict.protocolimpl.dlms.DLMSCache

-keepnames class com.energyict.protocolimpl.metcom.Metcom3

-keepnames class com.energyict.protocolimpl.metcom.Metcom2

-keepnames class com.energyict.protocolimpl.rtuplusbus.rtuplusbus

-keepnames class com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco

-keepnames class com.energyict.protocolimpl.siemens7ED62.Siemens7ED62

-keepnames class com.energyict.protocolimpl.dlms.DLMSEICT

-keepnames class com.energyict.protocolimpl.iec1107.abba1700.ABBA1700

-keepnames class com.energyict.protocolimpl.dlms.DLMSEMO

-keepnames class com.energyict.protocolimpl.dukepower.DukePower

-keepnames class com.energyict.protocolimpl.dlms.DLMSZMD

-keepnames class com.energyict.protocolimpl.actarissevc.SEVC

-keepnames class com.energyict.protocolimpl.dlms.DLMSLNSL7000

-keepnames class com.energyict.protocolimpl.pact.pripact.PRIPact

-keepnames class com.energyict.protocolimpl.iec870.datawatt.DataWatt

-keepnames class com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup

-keepnames class com.energyict.protocolimpl.iec1107.abba1500.ABBA1500

-keepnames class com.energyict.protocolimpl.iec1107.ferranti.Ferranti

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

-keepnames class com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic

-keepnames class com.energyict.protocolimpl.ge.kv.GEKV

-keepnames class com.energyict.protocolimpl.iec1107.a140.A140

-keepnames class com.energyict.protocolimpl.ge.kv2.GEKV2

-keepnames class com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd

-keepnames class com.energyict.protocolimpl.iec1107.abba1140.ABBA1140

-keepnames class com.energyict.protocolimpl.elster.a3.AlphaA3

-keepnames class com.energyict.protocolimpl.edmi.mk6.MK6

-keepnames class com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4

-keepnames class com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4

-keepnames class com.energyict.protocolimpl.edf.trimaran.Trimaran

-keepnames class com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys

-keepnames class com.energyict.protocolimpl.powermeasurement.ion.Ion

-keepnames class com.energyict.protocolimpl.landisgyr.sentry.s200.S200

-keepnames class com.energyict.protocolimpl.itron.fulcrum.Fulcrum

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

-keepnames class com.energyict.genericprotocolimpl.iskrap2lpc.Concentrator

-keepnames class com.energyict.genericprotocolimpl.iskrap2lpc.Meter

-keepnames class com.energyict.genericprotocolimpl.iskrap2lpc.MbusDevice

-keepnames class com.energyict.genericprotocolimpl.iskragprs.IskraMx37x

-keepnames class com.energyict.genericprotocolimpl.iskragprs.MbusDevice

-keepnames class com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000

-keepnames class com.energyict.protocolimpl.sdksample.SDKSampleProtocol

-keepnames class com.energyict.genericprotocolimpl.webrtukp.WebRTUKP

-keepnames class com.energyict.genericprotocolimpl.webrtukp.MbusDevice

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

-keepnames class com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200

-keepnames class com.energyict.genericprotocolimpl.Z3.DLMSZ3Messaging

# Use this statement to exclude an entire folder
# Stubs generated by axis can NOT be obfuscated
-keep public class com.energyict.genericprotocolimpl.iskrap2lpc.stub.* {
	*;
}

-keep public class com.energyict.protocolimpl.base.* {
	*;
}

-keepnames class com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging

-keepnames class com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx

-keepnames class com.energyict.protocolimpl.dlms.eictz3.EictZ3

-keepnames class com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200

-keepnames class com.energyict.protocolimpl.dlms.actarisace6000.ACE6000

-keepnames class com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX

-keepnames class com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300

-keepnames class com.energyict.protocolimpl.dlms.as220.AS220

# Keep annotations, 
-keepattributes *Annotation*

-keepnames class com.vodafone.gdsp.ws.package-info
-keep public class com.vodafone.gdsp.ws.*{
	*;
}


-keepnames class com.energyict.genericprotocolimpl.webrtukp.MeterToolProtocol
