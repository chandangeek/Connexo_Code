/*
 * ProtocolCollection.java
 *
 * Created on 18 maart 2005, 16:15
 */

package com.energyict.protocolimpl.base.protocolcollections;

import com.energyict.protocol.ProtocolCollection;
import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Koen
 */
public class ProtocolCollectionImpl implements ProtocolCollection {

    private final List<String> protocolClasses;
    private final List<String> protocolNames;

    private static final Map<String, String> METER_PROTOCOLS = new HashMap<String, String>();

    static {
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sdksample.SDKSampleProtocol", "SDK Sample Protocol");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.metcom.Metcom3", "SCTM L&G Metcom3 datalogger");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.metcom.Metcom2", "SCTM L&G Metcom2 datalogger");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.rtuplusbus.rtuplusbus", "RTU+ bus EnergyICT V4");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco", "IEC1107 FLAG IskraEmeco VDEW");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.siemens7ED62.Siemens7ED62", "SCTM Siemens 7ED62");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.DLMSEICT", "DLMS EnergyICT WebRTU & RTU V5");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.abba1700.ABBA1700", "IEC1107 FLAG Elster A1700");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.DLMSEMO", "DLMS Enermet E700");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dukepower.DukePower", "Dukepower EnergyICT WebRTU & RTU V5");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.DLMSZMD", "DLMS Siemens ZMD");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD", "SmartMeter DLMS Siemens ZMD");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.actarissevc.SEVC", "IEC1107 Actaris SEVC-D gascorrector");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.DLMSLNSL7000", "DLMS-LN Actaris SL7000");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.pact.pripact.PRIPact", "PRI PACT meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec870.datawatt.DataWatt", "IEC870-5-101 Datawatt logger");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup", "IEC1107 FLAG Kamstrup UNIGAS");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.abba1500.ABBA1500", "IEC1107 FLAG VDEW ABB A1500");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.ferranti.Ferranti", "IEC1107 Ferranti Real-Time gas measuring");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62", "IEC1107 VDEW Siemens 7ED62");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.indigo.IndigoPlus", "IEC1107 Actaris Indigo+");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.ppm.PPM", "Programmable Polyphase Meter (Issue 2)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x", "SCTM Enermet E70x (metcom2)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X", "IEC1107 Enermet E70x (customer specific config)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.ppmi1.PPM", "Programmable Polyphase Meter (Issue 1)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.gmc.u1600.U1600", "GMC U1600 datalogger");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.ekm.EKM", "SCTM Siemens EKM (metcom2)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.mtt3a.MTT3A", "SCTM Siemens MTT3A (metcom3)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.fag.FAG", "SCTM Siemens FAG");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.faf.FAF20", "SCTM Siemens FAF20");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.fbc.FBC", "SCTM Siemens FBC");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.fcl.FCL", "SCTM Siemens FCL");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.sctm.faf.FAF10", "SCTM Siemens FAF10");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X", "IEC1107 Enermet E60x (customer specific config)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew", "IEC1107 VDEW EnergyICT RTU+/WebRTU");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.unilog.Unilog", "IEC1107 Kamstrup unilog");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.emon.ez7.EZ7", "EMON EZ7 protocol logger");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus", "Elster Alpha+ (A2) meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.transdata.markv.MarkV", "Transdata MarkV meter/recorder");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu", "Energy ICT Modbus RTU");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic", "Elster Alpha (A1) meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.ge.kv.GEKV", "General Electric KV meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.a140.A140", "IEC1107 Elster a140");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.ge.kv2.GEKV2", "General Electric KV2 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd", "IEC870-5-102Ziv 5 Ctd");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.abba1140.ABBA1140", "IEC1107 FLAG Elster A1140");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.elster.a3.AlphaA3", "Elster Alpha A3 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.edmi.mk6.MK6", "EDMI MK6 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4", "L&G S4 meter DGCOM");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4", "L&G S4 meter ANSI");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.edf.trimaran.Trimaran", "Trimaran CVE meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys", "L&G MAXsys 2510");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.powermeasurement.ion.Ion", "Power Measurement Ion");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.landisgyr.sentry.s200.S200", "Landis+Gyr Sentry S200 recorder");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.itron.fulcrum.Fulcrum", "Itron/Schlumberger Fulcrum meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.itron.quantum.Quantum", "Itron/Schlumberger Quantum meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.itron.vectron.Vectron", "Itron/Schlumberger Vectron meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.itron.datastar.Datastar", "Itron/Schlumberger Datastar recorder");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.itron.sentinel.Sentinel", "Itron/Schlumberger Sentinel meter ANSI");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.itron.quantum1000.Quantum1000", "Itron/Schlumberger Quantum1000 meter Mini DLMS");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.enermet.e120.E120", "Enermet E120");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer", "IEC1107 CEWE CewePrometer");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer", "IEC1107 CEWE Prometer");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus", "Trimaran ICE meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.edf.trimaranplus.pmepmi.TrimaranPMEPMI", "Trimaran PME PMI meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.abba1350.ABBA1350", "IEC1107 FLAG VDEW ABB A1350");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.zmd.Zmd", "IEC1107 FLAG VDEW L&G ZMD");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.squared.pm800.PM800", "SquareD Modbus meter PM800");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.ge.pqm2.PQM2", "GE Modbus meter PQM2");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X", "Iskra ME372 & ME375 DLMS meters");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE", "Enerdis Recdigit CDTe Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr", "Enerdis Recdigit CDTpr Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800", "Enerdis Recdigit 1800 Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct", "Enerdis Recdigit Cct Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower", "Enerdis Recdigit Power Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.mbus.generic.Generic", "Generic MBUS meter implementation");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.mbus.nzr.pn16.PN16", "NZR PN16 MBUS meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770", "Hydrometer Sharky Heat type 770 MBUS meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ", "EMH 4 quadrant combi meter LZQJ VDEW");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.instromet.v555.Instromet555", "Instromet 555 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.eimeter.EIMeter", "EIMeter modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.squared.pm750.PM750", "Merlin Gerin PM750 Power Meter Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200", "Cutler Hammer IQ200 meter with mMINT module INCOM<->Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.socomec.a20.A20", "Socomec Diris A20 meter Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.instromet.v444.Instromet444", "Instromet 444 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.sdc.Sdc", "IEC 1107 SdC");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.flex.Flex", "DLMS LandysGyr Flex");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.socomec.a40.A40", "Socomec Diris A40 meter Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230", "Cutler Hammer IQ230 meter with Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.actarisace6000.ACE6000", "DLMS Actaris ACE6000");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.DLMSZMD_EXT", "DLMS Siemens ZMD [no profile support]");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.edf.trimarancje.Trimaran", "Trimaran CJE meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P", "Trimaran 2P meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.abba230.ABBA230", "Elster A230 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.CM32.CM32", "CM32 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.elster.opus.Opus", "Elster Opus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.kenda.meteor.Meteor", "Kenda Meteor meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.kenda.medo.Medo", "Kenda Medo meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.cm10.CM10", "CM10 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.edmi.mk10.MK10", "EDMI MK10-4 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83", "IskraEmeco MT83x meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.eictz3.EictZ3", "NTA compatible EICT DLMS Z3");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200", "Flonidan UNIFLO 1200 PTZ Modbus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging", "DLMS Z3 Messaging");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx", "DLMS Elster Instromet EK2xx");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200", "Enerdis Enerium 200 modbus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150", "Enerdis Enerium 150 modbus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50", "Enerdis Enerium 50 modbus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX", "Schneider Compact NSX Modbus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300", "IEC1107 Kamstrup UNIGAS300 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.as220.AS220", "Elster AS220 DLMS meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.as220.AS220", "Elster AS220 IEC1107 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.a1440.A1440", "Elster A1440 IEC1107 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4s", "Siemsens S4s IEC1107 meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.socomec.countis.ci.Ci", "Socomec Countis Ci meter Modbus");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.as220.GasDevice", "Elster AS220 Gas meter slave");
        METER_PROTOCOLS.put("com.elster.protocolimpl.iec1107.instromet.dl220.DL220", "Elster DL220");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.eig.nexus1272.Nexus1272", "Nexus 1272");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.landisgyr.us.maxsys2510.MaxSys", "L&G MAXsys 2510 (US)");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.elster.a1800.A1800", "Elster A1800");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.elgama.G3B", "DLMS ElGama G3B");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.din19244.poreg2.Poreg2", "Poreg 2");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.din19244.poreg2.Poreg2P", "Poreg 2P");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.idis.IDIS", "IDIS");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.idis.IDISMBus", "IDISMbus");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.elster.apollo.AS300", "Smart AS300");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis.Echodis", "WaveFlow 100mW Echodis");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent.SM150E", "WaveFlow 100mW Severntrent SM150E");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflow.waveflowV210.WaveFlowV210", "WaveFlow V210");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2", "WaveFlow V2");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflow.waveflowV1.WaveFlowV1", "WaveFlow V1");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.wavesense.WaveSense", "WaveSense");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.wavetherm.WaveTherm", "WaveTherm");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.wavelog.WaveLog", "WaveLog");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.wavetalk.WaveTalk", "WaveTalk");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.amco.rtm.RTM", "RTM");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflowDLMS.A1800", "WaveFlow AC A1800");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253", "WaveFlow AC AS1253");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.coronis.waveflowDLMS.AS253", "WaveFlow AC AS253");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol", "SDK SmartMeter protocol (multipleLoadProfiles)");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3", "SmartMeter - WebRTUZ3 DLMS protocol");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", "SmartMeter - NTA DSMR 2.3 WebRTUKP protocol");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382", "SmartMeter - NTA DSMR 2.3 Iskra Mx382 protocol");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150", "Enerdis Enerium 150 modbus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50", "Enerdis Enerium 50 modbus meter");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.modbus.northerndesign.eimeterflex.EIMeterFlexSM352", "EIMeter Flex SM352 submetering module");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.JanzC280.JanzC280", "DLMS Janz C280");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350", "DSMR 4.0 Landis & Gyr E350 DLMS meter");
        METER_PROTOCOLS.put("com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372", "SmartMeter IskraMx372");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.EMCO.FP93", "FP93 Flow Processor");
        METER_PROTOCOLS.put("com.energyict.protocolimpl.dlms.elster.as300d.AS300D", "DLMS Elster AS300D");

    }

    /**
     * Creates a new instance of ProtocolCollection
     */
    public ProtocolCollectionImpl() {
        this.protocolClasses = new ArrayList<String>();
        this.protocolNames = new ArrayList<String>();

        for (String classNameKey : METER_PROTOCOLS.keySet()) {
            this.protocolClasses.add(classNameKey);
            this.protocolNames.add(METER_PROTOCOLS.get(classNameKey));
        }
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

