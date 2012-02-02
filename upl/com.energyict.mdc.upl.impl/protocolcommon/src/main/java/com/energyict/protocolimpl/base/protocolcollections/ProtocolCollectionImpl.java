/*
 * ProtocolCollection.java
 *
 * Created on 18 maart 2005, 16:15
 */

package com.energyict.protocolimpl.base.protocolcollections;

import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Koen
 */
public class ProtocolCollectionImpl implements ProtocolCollection {

    List<String> protocolClasses;
    List<String> protocolNames;

    /**
     * Creates a new instance of ProtocolCollection
     */
    public ProtocolCollectionImpl() {

    }

    private void buildDefaultProtocols() {
        this.protocolClasses = new ArrayList<String>();
        this.protocolNames = new ArrayList<String>();


        this.protocolClasses.add("com.energyict.protocolimpl.sdksample.SDKSampleProtocol");
        this.protocolNames.add("SDK Sample Protocol");
        this.protocolClasses.add("com.energyict.protocolimpl.metcom.Metcom3");
        this.protocolNames.add("SCTM L&G Metcom3 datalogger");
        this.protocolClasses.add("com.energyict.protocolimpl.metcom.Metcom2");
        this.protocolNames.add("SCTM L&G Metcom2 datalogger");
        this.protocolClasses.add("com.energyict.protocolimpl.rtuplusbus.rtuplusbus");
        this.protocolNames.add("RTU+ bus EnergyICT V4");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco");
        this.protocolNames.add("IEC1107 FLAG IskraEmeco VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.siemens7ED62.Siemens7ED62");
        this.protocolNames.add("SCTM Siemens 7ED62");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSEICT");
        this.protocolNames.add("DLMS EnergyICT WebRTU & RTU V5");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1700.ABBA1700");
        this.protocolNames.add("IEC1107 FLAG Elster A1700");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSEMO");
        this.protocolNames.add("DLMS Enermet E700");
        this.protocolClasses.add("com.energyict.protocolimpl.dukepower.DukePower");
        this.protocolNames.add("Dukepower EnergyICT WebRTU & RTU V5");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSZMD");
        this.protocolNames.add("DLMS Siemens ZMD");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD");
        this.protocolNames.add("SmartMeter DLMS Siemens ZMD");
        this.protocolClasses.add("com.energyict.protocolimpl.actarissevc.SEVC");
        this.protocolNames.add("IEC1107 Actaris SEVC-D gascorrector");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSLNSL7000");
        this.protocolNames.add("DLMS-LN Actaris SL7000");
        this.protocolClasses.add("com.energyict.protocolimpl.pact.pripact.PRIPact");
        this.protocolNames.add("PRI PACT meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec870.datawatt.DataWatt");
        this.protocolNames.add("IEC870-5-101 Datawatt logger");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup");
        this.protocolNames.add("IEC1107 FLAG Kamstrup UNIGAS");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1500.ABBA1500");
        this.protocolNames.add("IEC1107 FLAG VDEW ABB A1500");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.ferranti.Ferranti");
        this.protocolNames.add("IEC1107 Ferranti Real-Time gas measuring");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62");
        this.protocolNames.add("IEC1107 VDEW Siemens 7ED62");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.indigo.IndigoPlus");
        this.protocolNames.add("IEC1107 Actaris Indigo+");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.ppm.PPM");
        this.protocolNames.add("Programmable Polyphase Meter (Issue 2)");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x");
        this.protocolNames.add("SCTM Enermet E70x (metcom2)");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X");
        this.protocolNames.add("IEC1107 Enermet E70x (customer specific config)");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.ppmi1.PPM");
        this.protocolNames.add("Programmable Polyphase Meter (Issue 1)");
        this.protocolClasses.add("com.energyict.protocolimpl.gmc.u1600.U1600");
        this.protocolNames.add("GMC U1600 datalogger");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.ekm.EKM");
        this.protocolNames.add("SCTM Siemens EKM (metcom2)");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.mtt3a.MTT3A");
        this.protocolNames.add("SCTM Siemens MTT3A (metcom3)");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.fag.FAG");
        this.protocolNames.add("SCTM Siemens FAG");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.faf.FAF20");
        this.protocolNames.add("SCTM Siemens FAF20");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.fbc.FBC");
        this.protocolNames.add("SCTM Siemens FBC");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.fcl.FCL");
        this.protocolNames.add("SCTM Siemens FCL");
        this.protocolClasses.add("com.energyict.protocolimpl.sctm.faf.FAF10");
        this.protocolNames.add("SCTM Siemens FAF10");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X");
        this.protocolNames.add("IEC1107 Enermet E60x (customer specific config)");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew");
        this.protocolNames.add("IEC1107 VDEW EnergyICT RTU+/WebRTU");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.unilog.Unilog");
        this.protocolNames.add("IEC1107 Kamstrup unilog");
        this.protocolClasses.add("com.energyict.protocolimpl.emon.ez7.EZ7");
        this.protocolNames.add("EMON EZ7 protocol logger");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus");
        this.protocolNames.add("Elster Alpha+ (A2) meter");
        this.protocolClasses.add("com.energyict.protocolimpl.transdata.markv.MarkV");
        this.protocolNames.add("Transdata MarkV meter/recorder");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu");
        this.protocolNames.add("Energy ICT Modbus RTU");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic");
        this.protocolNames.add("Elster Alpha (A1) meter");
        this.protocolClasses.add("com.energyict.protocolimpl.ge.kv.GEKV");
        this.protocolNames.add("General Electric KV meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.a140.A140");
        this.protocolNames.add("IEC1107 Elster a140");
        this.protocolClasses.add("com.energyict.protocolimpl.ge.kv2.GEKV2");
        this.protocolNames.add("General Electric KV2 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd");
        this.protocolNames.add("IEC870-5-102Ziv 5 Ctd");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1140.ABBA1140");
        this.protocolNames.add("IEC1107 FLAG Elster A1140");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.a3.AlphaA3");
        this.protocolNames.add("Elster Alpha A3 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.edmi.mk6.MK6");
        this.protocolNames.add("EDMI MK6 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4");
        this.protocolNames.add("L&G S4 meter DGCOM");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4");
        this.protocolNames.add("L&G S4 meter ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimaran.Trimaran");
        this.protocolNames.add("Trimaran CVE meter");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys");
        this.protocolNames.add("L&G MAXsys 2510");
        this.protocolClasses.add("com.energyict.protocolimpl.powermeasurement.ion.Ion");
        this.protocolNames.add("Power Measurement Ion");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.sentry.s200.S200");
        this.protocolNames.add("Landis+Gyr Sentry S200 recorder");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.fulcrum.Fulcrum");
        this.protocolNames.add("Itron/Schlumberger Fulcrum meter");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.quantum.Quantum");
        this.protocolNames.add("Itron/Schlumberger Quantum meter");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.vectron.Vectron");
        this.protocolNames.add("Itron/Schlumberger Vectron meter");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.datastar.Datastar");
        this.protocolNames.add("Itron/Schlumberger Datastar recorder");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.sentinel.Sentinel");
        this.protocolNames.add("Itron/Schlumberger Sentinel meter ANSI");
        this.protocolClasses.add("com.energyict.protocolimpl.itron.quantum1000.Quantum1000");
        this.protocolNames.add("Itron/Schlumberger Quantum1000 meter Mini DLMS");
        this.protocolClasses.add("com.energyict.protocolimpl.enermet.e120.E120");
        this.protocolNames.add("Enermet E120");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer");
        this.protocolNames.add("IEC1107 CEWE CewePrometer");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer");
        this.protocolNames.add("IEC1107 CEWE Prometer");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus");
        this.protocolNames.add("Trimaran ICE meter");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimaranplus.pmepmi.TrimaranPMEPMI");
        this.protocolNames.add("Trimaran PME PMI meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba1350.ABBA1350");
        this.protocolNames.add("IEC1107 FLAG VDEW ABB A1350");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.zmd.Zmd");
        this.protocolNames.add("IEC1107 FLAG VDEW L&G ZMD");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.squared.pm800.PM800");
        this.protocolNames.add("SquareD Modbus meter PM800");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.ge.pqm2.PQM2");
        this.protocolNames.add("GE Modbus meter PQM2");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X");
        this.protocolNames.add("Iskra ME372 & ME375 DLMS meters");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE");
        this.protocolNames.add("Enerdis Recdigit CDTe Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr");
        this.protocolNames.add("Enerdis Recdigit CDTpr Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800");
        this.protocolNames.add("Enerdis Recdigit 1800 Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct");
        this.protocolNames.add("Enerdis Recdigit Cct Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower");
        this.protocolNames.add("Enerdis Recdigit Power Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.mbus.generic.Generic");
        this.protocolNames.add("Generic MBUS meter implementation");
        this.protocolClasses.add("com.energyict.protocolimpl.mbus.nzr.pn16.PN16");
        this.protocolNames.add("NZR PN16 MBUS meter");
        this.protocolClasses.add("com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770");
        this.protocolNames.add("Hydrometer Sharky Heat type 770 MBUS meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ");
        this.protocolNames.add("EMH 4 quadrant combi meter LZQJ VDEW");
        this.protocolClasses.add("com.energyict.protocolimpl.instromet.v555.Instromet555");
        this.protocolNames.add("Instromet 555 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.northerndesign.cube350.Cube350");
        this.protocolNames.add("EIMeter Northern Design meter (Cube350) Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.squared.pm750.PM750");
        this.protocolNames.add("Merlin Gerin PM750 Power Meter Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200");
        this.protocolNames.add("Cutler Hammer IQ200 meter with mMINT module INCOM<->Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.socomec.a20.A20");
        this.protocolNames.add("Socomec Diris A20 meter Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.instromet.v444.Instromet444");
        this.protocolNames.add("Instromet 444 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.sdc.Sdc");
        this.protocolNames.add("IEC 1107 SdC");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.flex.Flex");
        this.protocolNames.add("DLMS LandysGyr Flex");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.socomec.a40.A40");
        this.protocolNames.add("Socomec Diris A40 meter Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230");
        this.protocolNames.add("Cutler Hammer IQ230 meter with Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.actarisace6000.ACE6000");
        this.protocolNames.add("DLMS Actaris ACE6000");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.DLMSZMD_EXT");
        this.protocolNames.add("DLMS Siemens ZMD [no profile support]");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimarancje.Trimaran");
        this.protocolNames.add("Trimaran CJE meter");
        this.protocolClasses.add("com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P");
        this.protocolNames.add("Trimaran 2P meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.abba230.ABBA230");
        this.protocolNames.add("Elster A230 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.CM32.CM32");
        this.protocolNames.add("CM32 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.opus.Opus");
        this.protocolNames.add("Elster Opus meter");
        this.protocolClasses.add("com.energyict.protocolimpl.kenda.meteor.Meteor");
        this.protocolNames.add("Kenda Meteor meter");
        this.protocolClasses.add("com.energyict.protocolimpl.kenda.medo.Medo");
        this.protocolNames.add("Kenda Medo meter");
        this.protocolClasses.add("com.energyict.protocolimpl.cm10.CM10");
        this.protocolNames.add("CM10 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.edmi.mk10.MK10");
        this.protocolNames.add("EDMI MK10-4 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83");
        this.protocolNames.add("IskraEmeco MT83x meter");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.eictz3.EictZ3");
        this.protocolNames.add("NTA compatible EICT DLMS Z3");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200");
        this.protocolNames.add("Flonidan UNIFLO 1200 PTZ Modbus meter");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging");
        this.protocolNames.add("DLMS Z3 Messaging");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx");
        this.protocolNames.add("DLMS Elster Instromet EK2xx");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200");
        this.protocolNames.add("Enerdis Enerium 200 modbus meter");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150");
        this.protocolNames.add("Enerdis Enerium 150 modbus meter");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50");
        this.protocolNames.add("Enerdis Enerium 50 modbus meter");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX");
        this.protocolNames.add("Schneider Compact NSX Modbus meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300");
        this.protocolNames.add("IEC1107 Kamstrup UNIGAS300 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.as220.AS220");
        this.protocolNames.add("Elster AS220 DLMS meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.as220.AS220");
        this.protocolNames.add("Elster AS220 IEC1107 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.a1440.A1440");
        this.protocolNames.add("Elster A1440 IEC1107 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4s");
        this.protocolNames.add("Siemsens S4s IEC1107 meter");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.socomec.countis.ci.Ci");
        this.protocolNames.add("Socomec Countis Ci meter Modbus");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.as220.GasDevice");
        this.protocolNames.add("Elster AS220 Gas meter slave");
        this.protocolClasses.add("com.elster.protocolimpl.iec1107.instromet.dl220.DL220");
        this.protocolNames.add("Elster DL220");
        this.protocolClasses.add("com.energyict.protocolimpl.eig.nexus1272.Nexus1272");
        this.protocolNames.add("Nexus 1272");
        this.protocolClasses.add("com.energyict.protocolimpl.landisgyr.us.maxsys2510.MaxSys");
        this.protocolNames.add("L&G MAXsys 2510 (US)");
        this.protocolClasses.add("com.energyict.protocolimpl.elster.a1800.A1800");
        this.protocolNames.add("Elster A1800");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.elgama.G3B");
        this.protocolNames.add("DLMS ElGama G3B");
        this.protocolClasses.add("com.energyict.protocolimpl.din19244.poreg2.Poreg2");
        this.protocolNames.add("Poreg 2");
        this.protocolClasses.add("com.energyict.protocolimpl.din19244.poreg2.Poreg2P");
        this.protocolNames.add("Poreg 2P");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.idis.IDIS");
        this.protocolNames.add("IDIS");
        this.protocolClasses.add("com.energyict.protocolimpl.dlms.idis.IDISMBus");
        this.protocolNames.add("IDISMbus");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.elster.apollo.AS300");
        this.protocolNames.add("Smart AS300");

        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis.Echodis");
        this.protocolNames.add("WaveFlow 100mW Echodis");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent.SM150E");
        this.protocolNames.add("WaveFlow 100mW Severntrent SM150E");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow.waveflowV210.WaveFlowV210");
        this.protocolNames.add("WaveFlow V210");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2");
        this.protocolNames.add("WaveFlow V2");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflow.waveflowV1.WaveFlowV1");
        this.protocolNames.add("WaveFlow V1");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavesense.WaveSense");
        this.protocolNames.add("WaveSense");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavetherm.WaveTherm");
        this.protocolNames.add("WaveTherm");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavelog.WaveLog");
        this.protocolNames.add("WaveLog");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.wavetalk.WaveTalk");
        this.protocolNames.add("WaveTalk");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.amco.rtm.RTM");
        this.protocolNames.add("RTM");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflowDLMS.A1800");
        this.protocolNames.add("WaveFlow AC A1800");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253");
        this.protocolNames.add("WaveFlow AC AS1253");
        this.protocolClasses.add("com.energyict.protocolimpl.coronis.waveflowDLMS.AS253");
        this.protocolNames.add("WaveFlow AC AS253");


        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol");
        this.protocolNames.add("SDK SmartMeter protocol (multipleLoadProfiles)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3");
        this.protocolNames.add("SmartMeter - WebRTUZ3 DLMS protocol");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP");
        this.protocolNames.add("SmartMeter - NTA DSMR 2.3 WebRTUKP protocol");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382");
        this.protocolNames.add("SmartMeter - NTA DSMR 2.3 Iskra Mx382 protocol");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150");
        this.protocolNames.add("Enerdis Enerium 150 modbus meter");
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50");
        this.protocolNames.add("Enerdis Enerium 50 modbus meter");
        
        this.protocolClasses.add("com.energyict.protocolimpl.modbus.northerndesign.eimeterflex.EIMeterFlexSM352"); 
        this.protocolNames.add("EIMeter Flex SM352 submetering module");

        this.protocolClasses.add("com.energyict.protocolimpl.dlms.JanzC280.JanzC280");
        this.protocolNames.add("DLMS Janz C280");
        
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350");
        this.protocolNames.add("DSMR 4.0 Landis & Gyr E350 DLMS meter");

        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372");
        this.protocolNames.add("SmartMeter IskraMx372");

    }

    public String getProtocolName(int index) throws IOException {
        return getProtocolNames().get(index);
    }

    public String getProtocolClassName(int index) throws IOException {
        return getProtocolClasses().get(index);
    }

    /**
     * Getter for property protocolClasses.
     *
     * @return Value of property protocolClasses.
     */
    public List<String> getProtocolClasses() {
        if (this.protocolClasses == null) {
            buildDefaultProtocols();
        }
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
        if (this.protocolNames == null) {
            buildDefaultProtocols();
        }
        return this.protocolNames;
    }


    static public void main(String[] args) {

        ProtocolCollectionImpl o = new ProtocolCollectionImpl();
        Iterator<String> it = o.getProtocolClasses().iterator();
        while (it.hasNext()) {
            String className = it.next();
            System.out.println("-keepnames class " + className + "\n");
        }


    }
}

