/*
 * ProtocolCollection.java
 *
 * Created on 18 maart 2005, 16:15
 */

package com.energyict.protocolimpl.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.ProtocolCollection;
import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolInstantiator;

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
		this.protocolclasses = new ArrayList();
		this.protocolnames = new ArrayList();

		// Protocol SDK
		//        protocolclasses.add("com.energyict.protocolimpl.sdksample.SDKSampleProtocol");protocolnames.add("SDK Sample Protocol");

		// EIServer
		this.protocolclasses.add("com.energyict.protocolimpl.metcom.Metcom3");this.protocolnames.add("SCTM L&G Metcom3 datalogger");
		this.protocolclasses.add("com.energyict.protocolimpl.metcom.Metcom2");this.protocolnames.add("SCTM L&G Metcom2 datalogger");
		this.protocolclasses.add("com.energyict.protocolimpl.rtuplusbus.rtuplusbus");this.protocolnames.add("RTU+ bus EnergyICT V4");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco");this.protocolnames.add("IEC1107 FLAG IskraEmeco VDEW");
		this.protocolclasses.add("com.energyict.protocolimpl.siemens7ED62.Siemens7ED62");this.protocolnames.add("SCTM Siemens 7ED62");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSEICT");this.protocolnames.add("DLMS EnergyICT WebRTU & RTU V5");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1700.ABBA1700");this.protocolnames.add("IEC1107 FLAG Elster A1700");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSEMO");this.protocolnames.add("DLMS Enermet E700");
		this.protocolclasses.add("com.energyict.protocolimpl.dukepower.DukePower");this.protocolnames.add("Dukepower EnergyICT WebRTU & RTU V5");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSZMD");this.protocolnames.add("DLMS Siemens ZMD");
		this.protocolclasses.add("com.energyict.protocolimpl.actarissevc.SEVC");this.protocolnames.add("IEC1107 Actaris SEVC-D gascorrector");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSLNSL7000");this.protocolnames.add("DLMS-LN Actaris SL7000");
		this.protocolclasses.add("com.energyict.protocolimpl.pact.pripact.PRIPact");this.protocolnames.add("PRI PACT meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec870.datawatt.DataWatt");this.protocolnames.add("IEC870-5-101 Datawatt logger");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup");this.protocolnames.add("IEC1107 FLAG Kamstrup UNIGAS");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1500.ABBA1500");this.protocolnames.add("IEC1107 FLAG VDEW ABB A1500");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.ferranti.Ferranti");this.protocolnames.add("IEC1107 Ferranti Real-Time gas measuring");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62");this.protocolnames.add("IEC1107 VDEW Siemens 7ED62");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.indigo.IndigoPlus");this.protocolnames.add("IEC1107 Actaris Indigo+");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.ppm.PPM");this.protocolnames.add("Programmable Polyphase Meter (Issue 2)");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x");this.protocolnames.add("SCTM Enermet E70x (metcom2)");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X");this.protocolnames.add("IEC1107 Enermet E70x (customer specific config)");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.ppmi1.PPM");this.protocolnames.add("Programmable Polyphase Meter (Issue 1)");
		this.protocolclasses.add("com.energyict.protocolimpl.gmc.u1600.U1600");this.protocolnames.add("GMC U1600 datalogger");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.ekm.EKM");this.protocolnames.add("SCTM Siemens EKM (metcom2)");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.mtt3a.MTT3A");this.protocolnames.add("SCTM Siemens MTT3A (metcom3)");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.fag.FAG");this.protocolnames.add("SCTM Siemens FAG");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.faf.FAF20");this.protocolnames.add("SCTM Siemens FAF20");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.fbc.FBC");this.protocolnames.add("SCTM Siemens FBC");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.fcl.FCL");this.protocolnames.add("SCTM Siemens FCL");
		this.protocolclasses.add("com.energyict.protocolimpl.sctm.faf.FAF10");this.protocolnames.add("SCTM Siemens FAF10");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X");this.protocolnames.add("IEC1107 Enermet E60x (customer specific config)");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew");this.protocolnames.add("IEC1107 VDEW EnergyICT RTU+/WebRTU");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.unilog.Unilog");this.protocolnames.add("IEC1107 Kamstrup unilog");
		this.protocolclasses.add("com.energyict.protocolimpl.emon.ez7.EZ7");this.protocolnames.add("EMON EZ7 protocol logger");
		this.protocolclasses.add("com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus");this.protocolnames.add("Elster Alpha+ (A2) meter");
		this.protocolclasses.add("com.energyict.protocolimpl.transdata.markv.MarkV");this.protocolnames.add("Transdata MarkV meter/recorder");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu");this.protocolnames.add("Energy ICT Modbus RTU");
		this.protocolclasses.add("com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic");this.protocolnames.add("Elster Alpha (A1) meter");
		this.protocolclasses.add("com.energyict.protocolimpl.ge.kv.GEKV");this.protocolnames.add("General Electric KV meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.a140.A140");this.protocolnames.add("IEC1107 Elster a140");
		this.protocolclasses.add("com.energyict.protocolimpl.ge.kv2.GEKV2");this.protocolnames.add("General Electric KV2 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd");this.protocolnames.add("IEC870-5-102Ziv 5 Ctd");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1140.ABBA1140");this.protocolnames.add("IEC1107 FLAG Elster A1140");
		this.protocolclasses.add("com.energyict.protocolimpl.elster.a3.AlphaA3");this.protocolnames.add("Elster Alpha A3 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.edmi.mk6.MK6");this.protocolnames.add("EDMI MK6 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4");this.protocolnames.add("L&G S4 meter DGCOM");
		this.protocolclasses.add("com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4");this.protocolnames.add("L&G S4 meter ANSI");
		this.protocolclasses.add("com.energyict.protocolimpl.edf.trimaran.Trimaran");this.protocolnames.add("Trimaran CVE meter");
		this.protocolclasses.add("com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys");this.protocolnames.add("L&G MAXsys 2510");
		this.protocolclasses.add("com.energyict.protocolimpl.powermeasurement.ion.Ion");this.protocolnames.add("Power Measurement Ion");
		this.protocolclasses.add("com.energyict.protocolimpl.landisgyr.sentry.s200.S200");this.protocolnames.add("Landis+Gyr Sentry S200 recorder");
		this.protocolclasses.add("com.energyict.protocolimpl.itron.fulcrum.Fulcrum");this.protocolnames.add("Itron/Schlumberger Fulcrum meter");
		this.protocolclasses.add("com.energyict.protocolimpl.itron.quantum.Quantum");this.protocolnames.add("Itron/Schlumberger Quantum meter");
		this.protocolclasses.add("com.energyict.protocolimpl.itron.vectron.Vectron");this.protocolnames.add("Itron/Schlumberger Vectron meter");
		this.protocolclasses.add("com.energyict.protocolimpl.itron.datastar.Datastar");this.protocolnames.add("Itron/Schlumberger Datastar recorder");
		this.protocolclasses.add("com.energyict.protocolimpl.itron.sentinel.Sentinel");this.protocolnames.add("Itron/Schlumberger Sentinel meter ANSI");
		this.protocolclasses.add("com.energyict.protocolimpl.itron.quantum1000.Quantum1000");this.protocolnames.add("Itron/Schlumberger Quantum1000 meter Mini DLMS");
		this.protocolclasses.add("com.energyict.protocolimpl.enermet.e120.E120");this.protocolnames.add("Enermet E120");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer");this.protocolnames.add("IEC1107 CEWE CewePrometer");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer");this.protocolnames.add("IEC1107 CEWE Prometer");
		this.protocolclasses.add("com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus");this.protocolnames.add("Trimaran ICE meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.abba1350.ABBA1350");this.protocolnames.add("IEC1107 FLAG VDEW ABB A1350");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.zmd.Zmd");this.protocolnames.add("IEC1107 FLAG VDEW L&G ZMD");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.squared.pm800.PM800");this.protocolnames.add("SquareD Modbus meter PM800");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.ge.pqm2.PQM2");this.protocolnames.add("GE Modbus meter PQM2");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X");this.protocolnames.add("Iskra ME372 & ME375 DLMS meters");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE");this.protocolnames.add("Enerdis Recdigit CDTe Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr");this.protocolnames.add("Enerdis Recdigit CDTpr Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800");this.protocolnames.add("Enerdis Recdigit 1800 Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct");this.protocolnames.add("Enerdis Recdigit Cct Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower");this.protocolnames.add("Enerdis Recdigit Power Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.mbus.generic.Generic");this.protocolnames.add("Generic MBUS meter implementation");
		this.protocolclasses.add("com.energyict.protocolimpl.mbus.nzr.pn16.PN16");this.protocolnames.add("NZR PN16 MBUS meter");
		this.protocolclasses.add("com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770");this.protocolnames.add("Hydrometer Sharky Heat type 770 MBUS meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ");this.protocolnames.add("EMH 4 quadrant combi meter LZQJ VDEW");
		this.protocolclasses.add("com.energyict.protocolimpl.instromet.v555.Instromet555");this.protocolnames.add("Instromet 555 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.northerndesign.cube350.Cube350");this.protocolnames.add("EIMeter Northern Design meter (Cube350) Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.squared.pm750.PM750");this.protocolnames.add("Merlin Gerin PM750 Power Meter Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200");this.protocolnames.add("Cutler Hammer IQ200 meter with mMINT module INCOM<->Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.socomec.a20.A20");this.protocolnames.add("Socomec Diris A20 meter Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.instromet.v444.Instromet444");this.protocolnames.add("Instromet 444 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.sdc.Sdc");this.protocolnames.add("IEC 1107 SdC");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.flex.Flex");this.protocolnames.add("DLMS LandysGyr Flex");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.socomec.a40.A40");this.protocolnames.add("Socomec Diris A40 meter Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230");this.protocolnames.add("Cutler Hammer IQ230 meter with Modbus");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.actarisace6000.ACE6000");this.protocolnames.add("DLMS Actaris ACE6000");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.DLMSZMD_EXT");this.protocolnames.add("DLMS Siemens ZMD [no profile support]");
		this.protocolclasses.add("com.energyict.protocolimpl.edf.trimarancje.Trimaran");this.protocolnames.add("Trimaran CJE meter");
		this.protocolclasses.add("com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P");this.protocolnames.add("Trimaran 2P meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.abba230.ABBA230");this.protocolnames.add("Elster A230 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.CM32.CM32");this.protocolnames.add("CM32 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.elster.opus.Opus");this.protocolnames.add("Elster Opus meter");
		this.protocolclasses.add("com.energyict.protocolimpl.kenda.meteor.Meteor");this.protocolnames.add("Kenda Meteor meter");
		this.protocolclasses.add("com.energyict.protocolimpl.kenda.medo.Medo");this.protocolnames.add("Kenda Medo meter");
		this.protocolclasses.add("com.energyict.protocolimpl.cm10.CM10");this.protocolnames.add("CM10 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.edmi.mk10.MK10");this.protocolnames.add("EDMI MK10-4 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83");this.protocolnames.add("IskraEmeco MT83x meter");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.eictz3.EictZ3");this.protocolnames.add("NTA compatible EICT DLMS Z3");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200");this.protocolnames.add("Flonidan UNIFLO 1200 PTZ Modbus meter");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging");this.protocolnames.add("DLMS Z3 Messaging");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx");this.protocolnames.add("DLMS Elster Instromet EK2xx");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200");this.protocolnames.add("Enerdis Enerium 200 modbus meter");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX");this.protocolnames.add("Schneider Compact NSX Modbus meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300");this.protocolnames.add("IEC1107 Kamstrup UNIGAS300 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.dlms.as220.AS220");this.protocolnames.add("Elster AS220 DLMS meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.as220.AS220");this.protocolnames.add("Elster AS220 IEC1107 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.a1440.A1440");this.protocolnames.add("Elster A1440 IEC1107 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4s");this.protocolnames.add("Siemsens S4s IEC1107 meter");
		this.protocolclasses.add("com.energyict.protocolimpl.modbus.socomec.countis.ci.Ci");this.protocolnames.add("Socomec Countis Ci meter Modbus");

	}

	public String getProtocolName(int index) throws IOException {
		return (String)this.protocolnames.get(index);
	}
	public String getProtocolClassName(int index) throws IOException {
		return (String)this.protocolclasses.get(index);
	}

	/**
	 * Getter for property protocolclasses.
	 * @return Value of property protocolclasses.
	 */
	public java.util.List getProtocolclasses() {
		return this.protocolclasses;
	}

	public String getProtocolVersion(int index) throws IOException {
		ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
		return (String)this.protocolnames.get(index)+": "+pi.getMeterProtocol().getProtocolVersion();
	}
	public String getProtocolRevision(int index) throws IOException {
		ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
		return pi.getMeterProtocol().getProtocolVersion();
	}

	public String getProtocolVersions() throws IOException {
		StringBuffer strBuff = new StringBuffer();
		for (int i=0;i<this.protocolclasses.size();i++) {
			strBuff.append(getProtocolVersion(i)+"\n");
		}
		return strBuff.toString();
	}
	public int getSize() {
		return this.protocolclasses.size();
	}
	/**
	 * Getter for property protocolnames.
	 * @return Value of property protocolnames.
	 */
	public java.util.List getProtocolnames() {
		return this.protocolnames;
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

