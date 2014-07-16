package com.energyict.protocolimpl.base.protocolcollections;

import com.energyict.protocol.ProtocolCollection;
import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 26/03/12
 * Time: 9:40
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolCollectionImpl implements ProtocolCollection {

    List<String> protocolClasses, protocolNames;

    public ProtocolCollectionImpl(){
        buildDefaultProtocols();
    }

    public void buildDefaultProtocols() {
        this.protocolClasses = new ArrayList();
        this.protocolNames = new ArrayList();
        // Protocol Standard Drivers
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.ppmi1.PPM");  this.protocolNames.add("ABB/GE PPM Issue1 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.ppm.PPM");  this.protocolNames.add("ABB/GE PPM Issue2 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.indigo.IndigoPlus");  this.protocolNames.add("Actaris Indigo+ IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.actarissevc.SEVC");  this.protocolNames.add("Actaris SEVCD IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.ametek.JemStar");  this.protocolNames.add("Ametek JemStar");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer");  this.protocolNames.add("CEWE CEWEPrometer IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer");  this.protocolNames.add("CEWE Prometer IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.prime.Cirwatt");  this.protocolNames.add("Circutor Cirwatt B 410D DLMS (PRIME1.5)");
        this.protocolClasses.add("com.energyict.protocolimpl.iec870.datawatt.DataWatt");  this.protocolNames.add("DataWatt D15 IEC870-5-101");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P");  this.protocolNames.add("EDF Trimaran 2P");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimarancje.Trimaran");  this.protocolNames.add("EDF Trimaran CJE");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimaran.Trimaran");  this.protocolNames.add("EDF Trimaran CVE");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus");  this.protocolNames.add("EDF Trimaran+ ICE");
        this.protocolClasses.add("com.energyict.protocolimpl.edmi.mk10.MK10");  this.protocolNames.add("EDMI MK10 [Pull] CommandLine");
        this.protocolClasses.add("com.energyict.protocolimpl.edmi.mk6.MK6");  this.protocolNames.add("EDMI MK6 CommandLine");
        this.protocolClasses.add("com.energyict.protocolimpl.eig.nexus1272.Nexus1272");  this.protocolNames.add("Electro Industries Nexus 1272 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.elgama.G3B");  this.protocolNames.add("Elgama G3B DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.a1800.A1800");  this.protocolNames.add("Elster Alpha A1800 ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.a3.AlphaA3");  this.protocolNames.add("Elster Alpha A3 ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic");  this.protocolNames.add("Elster Alpha Basic");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus");  this.protocolNames.add("Elster Alpha Plus");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.a1440.A1440");  this.protocolNames.add("Elster AS1440 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.as220.AS220");  this.protocolNames.add("Elster AS220 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.as220.AS220");  this.protocolNames.add("Elster AS220/AS1440 AM500 DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.as220.GasDevice");  this.protocolNames.add("Elster AS220/AS1440 AM500 DLMS Mbus Slave");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba230.ABBA230");  this.protocolNames.add("Elster AS230 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.g3.AS330D");  this.protocolNames.add("Elster AS330D DLMS (G3 Linky)");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.prime.AS330D");  this.protocolNames.add("Elster AS330D DLMS (PRIME1.5)");
        this.protocolClasses.add("com.elster.protocolimpl.lis200.DL210");  this.protocolNames.add("Elster DL210 LIS200");
        this.protocolClasses.add("com.elster.protocolimpl.lis200.DL220");  this.protocolNames.add("Elster DL220 LIS200");
        this.protocolClasses.add("com.elster.protocolimpl.lis200.DL240");  this.protocolNames.add("Elster DL240 LIS200");
        this.protocolClasses.add("com.elster.protocolimpl.dsfg.Dsfg");  this.protocolNames.add("Elster DsfG");
        this.protocolClasses.add("com.elster.protocolimpl.lis200.EK220");  this.protocolNames.add("Elster EK220 LIS200");
        this.protocolClasses.add("com.elster.protocolimpl.lis200.EK230");  this.protocolNames.add("Elster EK230 LIS200");
        this.protocolClasses.add("com.elster.protocolimpl.lis200.EK260");  this.protocolNames.add("Elster EK260 LIS200");
        this.protocolClasses.add("com.elster.protocolimpl.dlms.EK280");  this.protocolNames.add("Elster EK280 DLMS_V1");
        this.protocolClasses.add("com.elster.protocolimpl.lis200.EK280");  this.protocolNames.add("Elster EK280 LIS200");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx");  this.protocolNames.add("Elster EK2xx DLMS_V1");
        this.protocolClasses.add("com.elster.protocolimpl.lis100.EK88");  this.protocolNames.add("Elster EK88 LIS100");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.opus.Opus");  this.protocolNames.add("Elster OPUS");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1140.ABBA1140");  this.protocolNames.add("Elster/ABB A1140 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1350.ABBA1350");  this.protocolNames.add("Elster/ABB A1350 IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.a140.A140");  this.protocolNames.add("Elster/ABB A140 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1500.ABBA1500");  this.protocolNames.add("Elster/ABB A1500 IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1700.ABBA1700");  this.protocolNames.add("Elster/ABB A1700 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.EMCO.FP93");  this.protocolNames.add("EMCO FP93 Steam Meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ");  this.protocolNames.add("EMH LZQJ IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.emon.ez7.EZ7");  this.protocolNames.add("EMON EZ7");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150");  this.protocolNames.add("Enerdis Enerium 150 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200");  this.protocolNames.add("Enerdis Enerium 200 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50");  this.protocolNames.add("Enerdis Enerium 50 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800");  this.protocolNames.add("Enerdis Recdigit 1800 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct");  this.protocolNames.add("Enerdis Recdigit CCT Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE");  this.protocolNames.add("Enerdis Recdigit CDT E Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr");  this.protocolNames.add("Enerdis Recdigit CDT PR Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower");  this.protocolNames.add("Enerdis Recdigit Power Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.generic.Generic");  this.protocolNames.add("EnergyICT Generic Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSEICT");  this.protocolNames.add("EnergyICT RTU DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.dukepower.DukePower");  this.protocolNames.add("EnergyICT RTU DukePower");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew");  this.protocolNames.add("EnergyICT RTU IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.rtuplusbus.rtuplusbus");  this.protocolNames.add("EnergyICT RTU RtuPlusBus");
        this.protocolClasses.add("com.energyict.protocolimpl.enermet.e120.E120");  this.protocolNames.add("Enermet E120 ODEP");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X");  this.protocolNames.add("Enernet E6xx IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSEMO");  this.protocolNames.add("Enernet E7xx DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X");  this.protocolNames.add("Enernet E7xx IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x");  this.protocolNames.add("Enernet E7xx SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200");  this.protocolNames.add("Flonidan UNIFLO Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.ge.kv.GEKV");  this.protocolNames.add("General Electric KV ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.ge.kv2.GEKV2");  this.protocolNames.add("General Electric KV2 ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.prime.PrimeMeter");  this.protocolNames.add("Generic PRIME E-Meter DLMS (PRIME1.5)");
        this.protocolClasses.add("com.energyict.protocolimpl.gmc.u1600.U1600");  this.protocolNames.add("GMC U1600");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.idis.IDIS");  this.protocolNames.add("IDIS DLMS (IDIS P1)");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.idis.IDISMBus");  this.protocolNames.add("IDIS DLMS (IDIS P1) Mbus Slave");
        this.protocolClasses.add("com.energyict.protocolimpl.instromet.v444.Instromet444");  this.protocolNames.add("Instromet EVHI 444");
        this.protocolClasses.add("com.energyict.protocolimpl.instromet.v555.Instromet555");  this.protocolNames.add("Instromet EVHI 555");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83");  this.protocolNames.add("Iskraemeco MT83 IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco");  this.protocolNames.add("Iskraemeco MT851 IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X");  this.protocolNames.add("Iskraemeco Mx372 DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.din19244.poreg2.Poreg2");  this.protocolNames.add("Iskraemeco Poreg 2 DIN19244");
        this.protocolClasses.add("com.energyict.protocolimpl.din19244.poreg2.Poreg2P");  this.protocolNames.add("Iskraemeco Poreg 2P DIN19244");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.actarisace6000.ACE6000");  this.protocolNames.add("Itron ACE6000 DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSLNSL7000");  this.protocolNames.add("Itron SL7000 DLMS_V1");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.datastar.Datastar");  this.protocolNames.add("Itron/Schlumberger Datastar");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.fulcrum.Fulcrum");  this.protocolNames.add("Itron/Schlumberger Fulcrum");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.quantum.Quantum");  this.protocolNames.add("Itron/Schlumberger Quantum");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.quantum1000.Quantum1000");  this.protocolNames.add("Itron/Schlumberger Quantum1000");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.sentinel.Sentinel");  this.protocolNames.add("Itron/Schlumberger Sentinel ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.vectron.Vectron");  this.protocolNames.add("Itron/Schlumberger Vectron");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.edp.JanzB280");  this.protocolNames.add("Janz B280 DLMS_V1");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.JanzC280.JanzC280");  this.protocolNames.add("Janz C280 DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300");  this.protocolNames.add("Kampstrup Unigas 300 IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup");  this.protocolNames.add("Kamstrup EVHI IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.unilog.Unilog");  this.protocolNames.add("Kamstrup Unilog IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.kenda.medo.Medo");  this.protocolNames.add("Kenda Medeo");
        this.protocolClasses.add("com.energyict.protocolimpl.kenda.meteor.Meteor");  this.protocolNames.add("Kenda Meteor");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.ekm.EKM");  this.protocolNames.add("L&G EKM SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.faf.FAF10");  this.protocolNames.add("L&G FAF10 SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.faf.FAF20");  this.protocolNames.add("L&G FAF20 SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.fag.FAG");  this.protocolNames.add("L&G FAG SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.fbc.FBC");  this.protocolNames.add("L&G FBC SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.fcl.FCL");  this.protocolNames.add("L&G FCL SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.mtt3a.MTT3A");  this.protocolNames.add("L&G MTT3A SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.flex.Flex");  this.protocolNames.add("L&G ZxF AD-FG DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.zmd.Zmd");  this.protocolNames.add("L&G/Siemens ZxD IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSZMD");  this.protocolNames.add("L&G/Siemens ZxD/ZMQ/ZMF/ZMG DLMS_V1");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys");  this.protocolNames.add("Landis&Gyr MaxSys 2510 SMD");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.us.maxsys2510.MaxSys");  this.protocolNames.add("Landis&Gyr MaxSys 2510 SMD");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4");  this.protocolNames.add("Landis&Gyr S4 ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4");  this.protocolNames.add("Landis&Gyr S4 DGCOM");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.S4s");  this.protocolNames.add("Landis&Gyr S4S DGCOM");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4s");  this.protocolNames.add("Landis&Gyr S4S IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.sentry.s200.S200");  this.protocolNames.add("Landis&Gyr Sentry S200");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.prime.LGE450");  this.protocolNames.add("Landis+Gyr E450 DLMS (PRIME1.5)");
        this.protocolClasses.add("com.energyict.protocolimpl.powermeasurement.ion.Ion");  this.protocolNames.add("Power Measurement ION");
        this.protocolClasses.add("com.energyict.protocolimpl.pact.pripact.PRIPact");  this.protocolNames.add("PRI PACT");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.prime.SagemComCX10006");  this.protocolNames.add("SagemCom CX10006 DLMS (PRIME1.5)");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.edp.CX20009");  this.protocolNames.add("SagemCom CX2000-9 DLMS_V1");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.g3.SagemCom");  this.protocolNames.add("SagemCom Linky DLMS (G3 Linky)");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.sdc.Sdc");  this.protocolNames.add("SDC LM30x IEC1107");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62");  this.protocolNames.add("Siemens 7ED62 IEC1107 VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.siemens7ED62.Siemens7ED62");  this.protocolNames.add("Siemens 7ED62 SCTM");
        this.protocolClasses.add("com.energyict.protocolimpl.cm10.CM10");  this.protocolNames.add("Siemens Energy Services Ltd CM10");
        this.protocolClasses.add("com.energyict.protocolimpl.CM32.CM32");  this.protocolNames.add("Siemens Energy Services Ltd CM32");
        this.protocolClasses.add("com.energyict.protocolimpl.transdata.markv.MarkV");  this.protocolNames.add("Transdata MarkV");
        this.protocolClasses.add("com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd");  this.protocolNames.add("Ziv 5CTD IEC870-102");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.prime.ZIV");  this.protocolNames.add("Ziv 5CTM E2C DLMS (PRIME1.5)");

        // RTU+Server Sub Drivers
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200");  this.protocolNames.add("Cutler-Hammer IQ200 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230");  this.protocolNames.add("Cutler-Hammer IQ230 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.northerndesign.eimeterflex.EIMeterFlexSM352");  this.protocolNames.add("Northern Design MultiCube Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.energyict.EIMeterFlexSlaveModule");  this.protocolNames.add("EnergyICT EIFlex Meter Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.northerndesign.cube350.Cube350");  this.protocolNames.add("Northern Design Cube350 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.eimeter.EIMeter");  this.protocolNames.add("EnergyICT EIMeter Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu");  this.protocolNames.add("EnergyICT RTU Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.multilin.epm2200.EPM2200");  this.protocolNames.add("GE Multilin EPM 2200 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.ge.pqm2.PQM2");  this.protocolNames.add("GE PQM2 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.mbus.generic.Generic");  this.protocolNames.add("Generic Mbus Protocol");
        this.protocolClasses.add("com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770");  this.protocolNames.add("Hydrometer Sharky 770 Mbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.squared.pm750.PM750");  this.protocolNames.add("Merlin Gerin PM750 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.mbus.nzr.pn16.PN16");  this.protocolNames.add("NZR PN16 Mbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX");  this.protocolNames.add("Schneider Electric Compact NSX Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.squared.pm800.PM800");  this.protocolNames.add("Schneider Electric SquareD PM800 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.socomec.countis.ci.Ci");  this.protocolNames.add("Socomec Countis Ci Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.socomec.a20.A20");  this.protocolNames.add("Socomec Diris A20 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.socomec.a40.A40");  this.protocolNames.add("Socomec Diris A40 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.EictVeris");  this.protocolNames.add("Veris EICT PowerScout 18 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye");  this.protocolNames.add("Veris Hawkeye h80xx Modbus");

        // Wavenis Sub Drivers
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavetherm.WaveTherm");  this.protocolNames.add("Coronis WaveTherm");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2");  this.protocolNames.add("Coronis WaveFlow V2");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavesense.WaveSense");  this.protocolNames.add("Coronis WaveSense");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavetalk.WaveTalk");  this.protocolNames.add("Coronis WaveTalk");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow.waveflowV210.WaveFlowV210");  this.protocolNames.add("Coronis Waveflow V210");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavelog.WaveLog");  this.protocolNames.add("Coronis WaveLog");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow.waveflowV1.WaveFlowV1");  this.protocolNames.add("Coronis WaveFlow V1");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis.Echodis");  this.protocolNames.add("Echodis WaveFlow");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflowDLMS.A1800");  this.protocolNames.add("Elster A1800 WaveFlow AC");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.amco.rtm.RTM");  this.protocolNames.add("Coronis RTM Wavenis");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent.SM150E");  this.protocolNames.add("Severntrent SM150E WaveFlow");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflowDLMS.AS253");  this.protocolNames.add("Elster AS253 WaveFlow AC");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253");  this.protocolNames.add("Elster AS1253 WaveFlow AC");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow.hydreka.Hydreka");  this.protocolNames.add("Hydreka WaveFlow");
    }

    public String getProtocolName(int index) throws IOException {
        return this.protocolNames.get(index);
    }
    public String getProtocolClassName(int index) throws IOException {
        return this.protocolClasses.get(index);
    }

    /**
     * Getter for property protocolClasses.
     * @return Value of property protocolClasses.
     */
    public List<String> getProtocolClasses() {
        return this.protocolClasses;
    }

    public String getProtocolVersion(int index) throws IOException {
        ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
        if (pi.getMeterProtocol() != null) {
            return getProtocolName(index) + ": " + pi.getMeterProtocol().getProtocolVersion();
        } else if (pi.getSmartMeterProtocol() != null) {
            return getProtocolName(index) + ": " + pi.getSmartMeterProtocol().getVersion();
        }
        throw new UnsupportedException("No VersionSupport for protocol " + getProtocolClassName(index));
    }

    public String getProtocolRevision(int index) throws IOException {
        ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
        if (pi.getMeterProtocol() != null) {
            return pi.getMeterProtocol().getProtocolVersion();
        } else if (pi.getSmartMeterProtocol() != null) {
            return pi.getSmartMeterProtocol().getVersion();
        }
        throw new UnsupportedException("No RevisionSupport for protocol " + getProtocolClassName(index));
    }

    public String getProtocolVersions() throws IOException {
        StringBuffer strBuff = new StringBuffer();
        for (int i = 0; i < getProtocolClasses().size(); i++) {
            strBuff.append(getProtocolVersion(i) + "\n");
        }
        return strBuff.toString();
    }

    public int getSize() {
        return getProtocolClasses().size();
    }

    /**
     * Getter for property protocolNames.
     *
     * @return Value of property protocolNames.
     */
    public List<String> getProtocolNames() {
        return this.protocolNames;
    }
}