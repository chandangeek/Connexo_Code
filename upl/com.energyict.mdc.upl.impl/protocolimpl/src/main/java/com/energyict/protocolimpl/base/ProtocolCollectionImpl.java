/*
 * ProtocolCollection.java
 *
 * Created on 18 maart 2005, 16:15
 */

package com.energyict.protocolimpl.base;

import java.io.*;
import java.util.*;

import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolCollection;

/**
 *
 * @author  Koen
 */
public class ProtocolCollectionImpl implements ProtocolCollection {

    List protocolclasses,protocolnames;

    /** Creates a new instance of ProtocolCollection */
    public ProtocolCollectionImpl() {
        buildDefaultProtocols();
    }

    public void buildDefaultProtocols() {
        protocolclasses = new ArrayList();
        protocolnames = new ArrayList();
        
// Protocol SDK        
//        protocolclasses.add("com.energyict.protocolimpl.sdksample.SDKSampleProtocol");protocolnames.add("SDK Sample Protocol");        
        
// EIServer        
        protocolclasses.add("com.energyict.protocolimpl.metcom.Metcom3");protocolnames.add("SCTM L&G Metcom3 datalogger");
        protocolclasses.add("com.energyict.protocolimpl.metcom.Metcom2");protocolnames.add("SCTM L&G Metcom2 datalogger");
        protocolclasses.add("com.energyict.protocolimpl.rtuplusbus.rtuplusbus");protocolnames.add("RTU+ bus EnergyICT V4");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco");protocolnames.add("IEC1107 FLAG IskraEmeco VDEW");
        protocolclasses.add("com.energyict.protocolimpl.siemens7ED62.Siemens7ED62");protocolnames.add("SCTM Siemens 7ED62");
        protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSEICT");protocolnames.add("DLMS EnergyICT WebRTU & RTU V5");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1700.ABBA1700");protocolnames.add("IEC1107 FLAG Elster A1700");
        protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSEMO");protocolnames.add("DLMS Enermet E700");
        protocolclasses.add("com.energyict.protocolimpl.dukepower.DukePower");protocolnames.add("Dukepower EnergyICT WebRTU & RTU V5");
        protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSZMD");protocolnames.add("DLMS Siemens ZMD");
        protocolclasses.add("com.energyict.protocolimpl.actarissevc.SEVC");protocolnames.add("IEC1107 Actaris SEVC-D gascorrector");
        protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSLNSL7000");protocolnames.add("DLMS-LN Actaris SL7000");
        protocolclasses.add("com.energyict.protocolimpl.pact.pripact.PRIPact");protocolnames.add("PRI PACT meter");
        protocolclasses.add("com.energyict.protocolimpl.iec870.datawatt.DataWatt");protocolnames.add("IEC870-5-101 Datawatt logger");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup");protocolnames.add("IEC1107 FLAG Kamstrup UNIGAS");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1500.ABBA1500");protocolnames.add("IEC1107 FLAG VDEW ABB A1500");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.ferranti.Ferranti");protocolnames.add("IEC1107 Ferranti Real-Time gas measuring");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62");protocolnames.add("IEC1107 VDEW Siemens 7ED62");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.indigo.IndigoPlus");protocolnames.add("IEC1107 Actaris Indigo+");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.ppm.PPM");protocolnames.add("Programmable Polyphase Meter (Issue 2)");
        protocolclasses.add("com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x");protocolnames.add("SCTM Enermet E70x (metcom2)");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X");protocolnames.add("IEC1107 Enermet E70x (customer specific config)");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.ppmi1.PPM");protocolnames.add("Programmable Polyphase Meter (Issue 1)");
        protocolclasses.add("com.energyict.protocolimpl.gmc.u1600.U1600");protocolnames.add("GMC U1600 datalogger");
        protocolclasses.add("com.energyict.protocolimpl.sctm.ekm.EKM");protocolnames.add("SCTM Siemens EKM (metcom2)");
        protocolclasses.add("com.energyict.protocolimpl.sctm.mtt3a.MTT3A");protocolnames.add("SCTM Siemens MTT3A (metcom3)");
        protocolclasses.add("com.energyict.protocolimpl.sctm.fag.FAG");protocolnames.add("SCTM Siemens FAG");
        protocolclasses.add("com.energyict.protocolimpl.sctm.faf.FAF20");protocolnames.add("SCTM Siemens FAF20");
        protocolclasses.add("com.energyict.protocolimpl.sctm.fbc.FBC");protocolnames.add("SCTM Siemens FBC");
        protocolclasses.add("com.energyict.protocolimpl.sctm.fcl.FCL");protocolnames.add("SCTM Siemens FCL");
        protocolclasses.add("com.energyict.protocolimpl.sctm.faf.FAF10");protocolnames.add("SCTM Siemens FAF10");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X");protocolnames.add("IEC1107 Enermet E60x (customer specific config)");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew");protocolnames.add("IEC1107 VDEW EnergyICT RTU+/WebRTU");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.unilog.Unilog");protocolnames.add("IEC1107 Kamstrup unilog");
        protocolclasses.add("com.energyict.protocolimpl.emon.ez7.EZ7");protocolnames.add("EMON EZ7 protocol logger");
        protocolclasses.add("com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus");protocolnames.add("Elster Alpha+ (A2) meter");
        protocolclasses.add("com.energyict.protocolimpl.transdata.markv.MarkV");protocolnames.add("Transdata MarkV meter/recorder");
        protocolclasses.add("com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu");protocolnames.add("Energy ICT Modbus RTU");
        protocolclasses.add("com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic");protocolnames.add("Elster Alpha (A1) meter");
        protocolclasses.add("com.energyict.protocolimpl.ge.kv.GEKV");protocolnames.add("General Electric KV meter");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.a140.A140");protocolnames.add("IEC1107 Elster a140");
        protocolclasses.add("com.energyict.protocolimpl.ge.kv2.GEKV2");protocolnames.add("General Electric KV2 meter");
        protocolclasses.add("com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd");protocolnames.add("IEC870-5-102Ziv 5 Ctd");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1140.ABBA1140");protocolnames.add("IEC1107 FLAG Elster A1140");
        protocolclasses.add("com.energyict.protocolimpl.elster.a3.AlphaA3");protocolnames.add("Elster Alpha A3 meter");
        protocolclasses.add("com.energyict.protocolimpl.edmi.mk6.MK6");protocolnames.add("EDMI MK6 meter");
        protocolclasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4");protocolnames.add("L&G S4 meter DGCOM");
        protocolclasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4");protocolnames.add("L&G S4 meter ANSI");
        protocolclasses.add("com.energyict.protocolimpl.edf.trimaran.Trimaran");protocolnames.add("Trimaran CVE meter");
        protocolclasses.add("com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys");protocolnames.add("L&G MAXsys 2510");
        protocolclasses.add("com.energyict.protocolimpl.powermeasurement.ion.Ion");protocolnames.add("Power Measurement Ion");
        protocolclasses.add("com.energyict.protocolimpl.landisgyr.sentry.s200.S200");protocolnames.add("Landis+Gyr Sentry S200 recorder");
        protocolclasses.add("com.energyict.protocolimpl.itron.fulcrum.Fulcrum");protocolnames.add("Itron/Schlumberger Fulcrum meter");
        protocolclasses.add("com.energyict.protocolimpl.itron.quantum.Quantum");protocolnames.add("Itron/Schlumberger Quantum meter");
        protocolclasses.add("com.energyict.protocolimpl.itron.vectron.Vectron");protocolnames.add("Itron/Schlumberger Vectron meter");
        protocolclasses.add("com.energyict.protocolimpl.itron.datastar.Datastar");protocolnames.add("Itron/Schlumberger Datastar recorder");
        protocolclasses.add("com.energyict.protocolimpl.itron.sentinel.Sentinel");protocolnames.add("Itron/Schlumberger Sentinel meter ANSI");
        protocolclasses.add("com.energyict.protocolimpl.itron.quantum1000.Quantum1000");protocolnames.add("Itron/Schlumberger Quantum1000 meter Mini DLMS");
        protocolclasses.add("com.energyict.protocolimpl.enermet.e120.E120");protocolnames.add("Enermet E120");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer");protocolnames.add("IEC1107 CEWE CewePrometer");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer");protocolnames.add("IEC1107 CEWE Prometer");
        protocolclasses.add("com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus");protocolnames.add("Trimaran ICE meter");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1350.ABBA1350");protocolnames.add("IEC1107 FLAG VDEW ABB A1350");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.zmd.Zmd");protocolnames.add("IEC1107 FLAG VDEW L&G ZMD");
        protocolclasses.add("com.energyict.protocolimpl.modbus.squared.pm800.PM800");protocolnames.add("SquareD Modbus meter PM800");
        protocolclasses.add("com.energyict.protocolimpl.modbus.ge.pqm2.PQM2");protocolnames.add("GE Modbus meter PQM2");
        protocolclasses.add("com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X");protocolnames.add("Iskra ME372 & ME375 DLMS meters");
        protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE");protocolnames.add("Enerdis Recdigit CDTe Modbus");
        protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr");protocolnames.add("Enerdis Recdigit CDTpr Modbus");
        protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800");protocolnames.add("Enerdis Recdigit 1800 Modbus");
        protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct");protocolnames.add("Enerdis Recdigit Cct Modbus");
        protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower");protocolnames.add("Enerdis Recdigit Power Modbus");
        protocolclasses.add("com.energyict.protocolimpl.mbus.generic.Generic");protocolnames.add("Generic MBUS meter implementation");
        protocolclasses.add("com.energyict.protocolimpl.mbus.nzr.pn16.PN16");protocolnames.add("NZR PN16 MBUS meter");
        protocolclasses.add("com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770");protocolnames.add("Hydrometer Sharky Heat type 770 MBUS meter");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ");protocolnames.add("EMH 4 quadrant combi meter LZQJ VDEW");
        protocolclasses.add("com.energyict.protocolimpl.instromet.v555.Instromet555");protocolnames.add("Instromet 555 meter");        
        protocolclasses.add("com.energyict.protocolimpl.modbus.northerndesign.cube350.Cube350");protocolnames.add("EIMeter Northern Design meter (Cube350) Modbus");        
        protocolclasses.add("com.energyict.protocolimpl.modbus.squared.pm750.PM750");protocolnames.add("Merlin Gerin PM750 Power Meter Modbus");        
        protocolclasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200");protocolnames.add("Cutler Hammer IQ200 meter with mMINT module INCOM<->Modbus");        
        protocolclasses.add("com.energyict.protocolimpl.modbus.socomec.a20.A20");protocolnames.add("Socomec Diris A20 meter Modbus");      
        protocolclasses.add("com.energyict.protocolimpl.instromet.v444.Instromet444");protocolnames.add("Instromet 444 meter");        
        protocolclasses.add("com.energyict.protocolimpl.iec1107.sdc.Sdc");protocolnames.add("IEC 1107 SdC");
        protocolclasses.add("com.energyict.protocolimpl.dlms.flex.Flex");protocolnames.add("DLMS LandysGyr Flex");
        protocolclasses.add("com.energyict.protocolimpl.modbus.socomec.a40.A40");protocolnames.add("Socomec Diris A40 meter Modbus");      
        protocolclasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230");protocolnames.add("Cutler Hammer IQ230 meter with Modbus");   
        protocolclasses.add("com.energyict.protocolimpl.dlms.actarisace6000.ACE6000");protocolnames.add("DLMS Actaris ACE6000");
        protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSZMD_EXT");protocolnames.add("DLMS Siemens ZMD [no profile support]");
        protocolclasses.add("com.energyict.protocolimpl.edf.trimarancje.Trimaran");protocolnames.add("Trimaran CJE meter");
        protocolclasses.add("com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P");protocolnames.add("Trimaran 2P meter");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.abba230.ABBA230");protocolnames.add("Elster A230 meter");
        protocolclasses.add("com.energyict.protocolimpl.CM32.CM32");protocolnames.add("CM32 meter");
        protocolclasses.add("com.energyict.protocolimpl.elster.opus.Opus");protocolnames.add("Elster Opus meter");
        protocolclasses.add("com.energyict.protocolimpl.kenda.meteor.Meteor");protocolnames.add("Kenda Meteor meter");
        protocolclasses.add("com.energyict.protocolimpl.kenda.medo.Medo");protocolnames.add("Kenda Medo meter");
        protocolclasses.add("com.energyict.protocolimpl.cm10.CM10");protocolnames.add("CM10 meter");
        protocolclasses.add("com.energyict.protocolimpl.edmi.mk10.MK10");protocolnames.add("EDMI MK10-4 meter");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83");protocolnames.add("IskraEmeco MT83x meter");
        protocolclasses.add("com.energyict.protocolimpl.dlms.eictz3.EictZ3");protocolnames.add("NTA compatible EICT DLMS Z3");
        protocolclasses.add("com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200");protocolnames.add("Flonidan UNIFLO 1200 PTZ Modbus meter");
        protocolclasses.add("com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging");protocolnames.add("DLMS Z3 Messaging");
        protocolclasses.add("com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx");protocolnames.add("DLMS Elster Instromet EK2xx");
        protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200");protocolnames.add("Enerdis Enerium 200 modbus meter");
        protocolclasses.add("com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX");protocolnames.add("Schneider Compact NSX Modbus meter");
        protocolclasses.add("com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300");protocolnames.add("IEC1107 Kamstrup UNIGAS300 meter");
        protocolclasses.add("com.energyict.protocolimpl.dlms.as220.AS220");protocolnames.add("Elster AS220 DLMS meter");
    }  
    
    public String getProtocolName(int index) throws IOException {
        return (String)protocolnames.get(index);
    }
    public String getProtocolClassName(int index) throws IOException {
        return (String)protocolclasses.get(index);
    }

    /**
     * Getter for property protocolclasses.
     * @return Value of property protocolclasses.
     */
    public java.util.List getProtocolclasses() {
        return protocolclasses;
    }

    public String getProtocolVersion(int index) throws IOException {
        ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
        return (String)protocolnames.get(index)+": "+pi.getMeterProtocol().getProtocolVersion();
    }
    public String getProtocolRevision(int index) throws IOException {
        ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
        return pi.getMeterProtocol().getProtocolVersion();
    }

    public String getProtocolVersions() throws IOException {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<protocolclasses.size();i++) {
            strBuff.append(getProtocolVersion(i)+"\n");
        }
        return strBuff.toString();
    }
    public int getSize() {
        return protocolclasses.size();
    }
    /**
     * Getter for property protocolnames.
     * @return Value of property protocolnames.
     */
    public java.util.List getProtocolnames() {
        return protocolnames;
    }

    
    static public void main(String[] args) {
        
        ProtocolCollectionImpl o = new ProtocolCollectionImpl();
        Iterator it = o.getProtocolclasses().iterator();
        while(it.hasNext()) {
            String className = (String)it.next();
            System.out.println("-keepnames class "+className+"\n");
        }
                
        
    }
}

