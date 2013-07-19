package com.energyict.license;

import com.energyict.mdw.core.LicensedProtocol;
import com.energyict.mdw.core.ProtocolFamily;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Models the known protocols that are supported by the licensing mechanism.
 * A protocol is considered to be covered by the license if
 * the protocol is either explicitly mentioned as covered or if
 * the protocol's {@link FamilyRule} is covered by the license.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-18 (12:10)
 */
public enum LicensedProtocolRule implements LicensedProtocol {

    /* Please keep the list of protocols organized by protocol generation
     * to make it easier to modify the list that is expected to become big
     * and therefore difficult to maintain.
     * New protocols are required to be of type DeviceProtocol
     * and should be added at the end of the list.
     * The id of the new protocol should be incremented from the last
     * protocol in the current list.
     */
    A1440(1, "com.energyict.protocolimpl.iec1107.a1440.A1440", FamilyRule.IEC),
    ABBA1500(2, "com.energyict.protocolimpl.iec1107.abba1500.ABBA1500", FamilyRule.IEC),
    ECHELON(3, "com.energyict.rtuprotocol.Echelon", FamilyRule.ECHELON),
    EIWEB(4, "com.energyict.rtuprotocol.EIWeb", FamilyRule.ENERGYICT),
    EK88(5, "com.elster.protocolimpl.lis100.EK88", FamilyRule.ELSTER, FamilyRule.LIS),
    FERRANTI(6, "com.energyict.protocolimpl.iec1107.ferranti.Ferranti", FamilyRule.FERRANTI),
    INSTROMET444(7, "com.energyict.protocolimpl.instromet.v555.Instromet444", FamilyRule.ELSTER),
    INSTROMET555(8, "com.energyict.protocolimpl.instromet.v444.Instromet444", FamilyRule.ELSTER),
    LIS100(9, "com.elster.protocolimpl.lis100.LIS100", FamilyRule.ELSTER, FamilyRule.LIS),
    MEDO(10, "com.energyict.protocolimpl.kenda.medo.Medo", FamilyRule.KENDA),
    MK10(11, "com.energyict.protocolimpl.edmi.mk10.MK10", FamilyRule.EDMI),
    QUANTUM1000_ITRON(12, "com.energyict.protocolimpl.itron.quantum1000.Quantum1000", FamilyRule.ITRON),
    RTM(13, "com.energyict.protocolimpl.coronis.amco.rtm.RTM", FamilyRule.CORONIS),
    RTU_PLUS_BUS(14, "com.energyict.protocolimpl.rtuplusbus.rtuplusbus", FamilyRule.ENERGYICT),
    SENTINEL(15, "com.energyict.protocolimpl.itron.sentinel.Sentinel", FamilyRule.ITRON),
    SIEMENS_7ED62(16, "com.energyict.protocolimpl.siemens7ED62.Siemens7ED62", FamilyRule.SIEMENS),
    SDC(17, "com.energyict.protocolimpl.iec1107.sdc.Sdc", FamilyRule.IEC),
    QUANTUM_SCHLUMBERGER(18, "com.energyict.protocolimpl.itron.quantum.Quantum", FamilyRule.ITRON),
    FULCRUM(19, "com.energyict.protocolimpl.itron.quantum.Fulcrum", FamilyRule.ITRON),
    DATASTAR(20, "com.energyict.protocolimpl.itron.datastar.Datastar", FamilyRule.ITRON),
    VECTRON(21, "com.energyict.protocolimpl.itron.vectron.Vectron", FamilyRule.ITRON),
    EZ7(22, "com.energyict.protocolimpl.emon.ez7.EZ7", FamilyRule.EMON),
    JEMSTAR(23, "com.energyict.protocolimpl.ametek.JemStar", FamilyRule.AMETEK),
    JEM10(24, "com.energyict.protocolimpl.ametek.Jem10", FamilyRule.AMETEK),
    MK6(25, "com.energyict.protocolimpl.edmi.mk6.MK6", FamilyRule.EDMI),
    INDIGO_PLUS_SAMPLE(26, "com.energyict.protocolimpl.sampleiec1107.indigo.IndigoPlus", FamilyRule.IEC, FamilyRule.SAMPLE),
    WAVE_SENSE(27, "com.energyict.protocolimpl.coronis.wavesense.WaveSense", FamilyRule.CORONIS),
    ALPHA_PLUS(28, "com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus", FamilyRule.ELSTER),
    ALPHA_BASIC(29, "com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic", FamilyRule.ELSTER),
    EICT_TEST(30, "com.energyict.protocolimpl.eicttest.EICTTestProtocol", FamilyRule.ENERGYICT, FamilyRule.TEST),
    S4_DGCOM(31, "com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4", FamilyRule.LANDISGYR),
    G3B(32, "com.energyict.protocolimpl.dlms.elgama.G3B", FamilyRule.DLMS),
    JANZC280(33, "com.energyict.protocolimpl.dlms.JanzC280.JanzC280", FamilyRule.DLMS),
    IDIS(34, "com.energyict.protocolimpl.dlms.idis.IDIS", FamilyRule.DLMS, FamilyRule.IDIS),
    IDIS_MBUS(35, "com.energyict.protocolimpl.dlms.idis.IDISMBus", FamilyRule.DLMS, FamilyRule.IDIS),
    WAVE_THERM(36, "com.energyict.protocolimpl.coronis.wavetherm.WaveTherm", FamilyRule.CORONIS),
    PROMETER(37, "com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer", FamilyRule.IEC),
    GEKV(38, "com.energyict.protocolimpl.ge.kv.GEKV", FamilyRule.GE),
    S200(39, "com.energyict.protocolimpl.landisgyr.sentry.s200.S200", FamilyRule.LANDISGYR),
    AS253(40, "com.energyict.protocolimpl.coronis.waveflowDLMS.AS253", FamilyRule.CORONIS, FamilyRule.DLMS),
    AS1253(41, "com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253", FamilyRule.CORONIS, FamilyRule.DLMS),
    A1800_CORONIS(42, "com.energyict.protocolimpl.coronis.waveflowDLMS.A1800", FamilyRule.CORONIS, FamilyRule.DLMS),
    WAVE_LOG(43, "com.energyict.protocolimpl.coronis.wavelog.WaveLog", FamilyRule.CORONIS),
    CEWE_PROMETER(44, "com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer", FamilyRule.IEC),
    MK10_STUB(45, "com.energyict.genericprotocolimpl.edmi.mk10.executer.Mk10Stub", FamilyRule.EDMI, FamilyRule.TEST),
    SDK_SAMPLE_PROTOCOL(46, "com.energyict.protocolimpl.sdksample.SDKSampleProtocol", FamilyRule.ENERGYICT, FamilyRule.SAMPLE),
    TRIMARAN_CJE(47, "com.energyict.protocolimpl.edf.trimarancje.Trimaran", FamilyRule.EDF, FamilyRule.TRIMARAN),
    GEKV2(48, "com.energyict.protocolimpl.ge.kv2.GEKV2", FamilyRule.GE),
    CM32(49, "com.energyict.protocolimpl.CM32.CM32"),
    NEXUS1272(50, "com.energyict.protocolimpl.eig.nexus1272.Nexus1272"),
    IQ230(51, "com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230", FamilyRule.MODBUS),
    IQ200(52, "com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200", FamilyRule.MODBUS),
    A40(53, "com.energyict.protocolimpl.modbus.socomec.a40.A40", FamilyRule.MODBUS, FamilyRule.SOCOMEC),
    ENERIUM200(54, "com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    ENERIUM50(55, "com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    ENERIUM150(56, "com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    COMPACT_NSX(57, "com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX", FamilyRule.MODBUS, FamilyRule.SCHNEIDER),
    CI(58, "com.energyict.protocolimpl.modbus.socomec.countis.ci.Ci", FamilyRule.MODBUS, FamilyRule.SOCOMEC),
    A20(59, "com.energyict.protocolimpl.modbus.socomec.a20.A20", FamilyRule.MODBUS, FamilyRule.SOCOMEC),
    EICT_MODBUS_RTU(60, "com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu", FamilyRule.MODBUS, FamilyRule.ENERGYICT),
    PM750(61, "com.energyict.protocolimpl.modbus.squared.pm750.PM750", FamilyRule.MODBUS, FamilyRule.SQUARED),
    HAWKEYE(62, "com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye", FamilyRule.MODBUS, FamilyRule.VERIS),
    EIMETER_FLEX_SM352(63, "com.energyict.protocolimpl.modbus.northerndesign.eimeterflex.EIMeterFlexSM352", FamilyRule.MODBUS, FamilyRule.NORTHERN_DESIGN),
    REC_DIGIT_CDTE(64, "com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    REC_DIGIT_CDTPR(65, "com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    REC_DIGIT_CCT(66, "com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    EIMETER(67, "com.energyict.protocolimpl.modbus.eimeter.EIMeter", FamilyRule.MODBUS, FamilyRule.ENERGYICT),
    REC_DIGIT1800(68, "com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    REC_DIGIT_POWER(69, "com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower", FamilyRule.MODBUS, FamilyRule.ENERDIS),
    EICT_VERIS(70, "com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.EictVeris", FamilyRule.MODBUS, FamilyRule.ENERGYICT),
    UNIFLO1200(71, "com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200", FamilyRule.MODBUS, FamilyRule.FLONIDAN),
    PQM2(72, "com.energyict.protocolimpl.modbus.ge.pqm2.PQM2", FamilyRule.MODBUS, FamilyRule.GE),
    PM800(73, "com.energyict.protocolimpl.modbus.squared.pm800.PM800", FamilyRule.MODBUS, FamilyRule.SQUARED),
    EPM2200(74, "com.energyict.protocolimpl.modbus.multilin.epm2200.EPM2200", FamilyRule.MODBUS),
    GENERIC_MODBUS_DISCOVER(75, "com.energyict.protocolimpl.modbus.core.discover.GenericModbusDiscover", FamilyRule.MODBUS),
    S4S(76, "com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.S4s", FamilyRule.LANDISGYR),
    SHARKY770(77, "com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770", FamilyRule.MBUS),
    MBUS_GENERIC(78, "com.energyict.protocolimpl.mbus.generic.Generic", FamilyRule.MBUS),
    PN16(79, "com.energyict.protocolimpl.mbus.nzr.pn16.PN16", FamilyRule.MBUS),
    TRIMARAN2P(80, "com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P", FamilyRule.EDF, FamilyRule.TRIMARAN),
    ENERMET_E70X_IEC(81, "com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X", FamilyRule.IEC, FamilyRule.ENERMET),
    ENERMET_E60X_IEC(82, "com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X", FamilyRule.IEC, FamilyRule.ENERMET),
    POREG2(83, "com.energyict.protocolimpl.din19244.poreg2.Poreg2", FamilyRule.ISKRA),
    POREG2P(84, "com.energyict.protocolimpl.din19244.poreg2.Poreg2P", FamilyRule.ISKRA),
    S4_ANSI(85, "com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4", FamilyRule.LANDISGYR),
    E120(86, "com.energyict.protocolimpl.enermet.e120.E120", FamilyRule.ENERMET),
    TRIMARAN_PLUS(87, "com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus", FamilyRule.EDF, FamilyRule.TRIMARAN),
    TRIMARAN(88, "com.energyict.protocolimpl.edf.trimaran.Trimaran", FamilyRule.EDF, FamilyRule.TRIMARAN),
    WAVE_FLOW_V1(89, "com.energyict.protocolimpl.coronis.waveflow.waveflowV1.WaveFlowV1", FamilyRule.CORONIS),
    WAVE_FLOW_V2_OLD(90, "com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2", FamilyRule.CORONIS),
    WAVE_FLOW_V210(91, "com.energyict.protocolimpl.coronis.waveflow.waveflowV210.WaveFlowV210", FamilyRule.CORONIS),
    HYDREKA(92, "com.energyict.protocolimpl.coronis.waveflow.hydreka.Hydreka", FamilyRule.CORONIS),
    ALPHA_A3(93, "com.energyict.protocolimpl.elster.a3.AlphaA3", FamilyRule.ELSTER),
    A1800_ELSTER(94, "com.energyict.protocolimpl.elster.a1800.A1800", FamilyRule.ELSTER),
    MARKV(95, "com.energyict.protocolimpl.transdata.markv.MarkV"),
    CM10(96, "com.energyict.protocolimpl.cm10.CM10"),
    U1600(97, "com.energyict.protocolimpl.gmc.u1600.U1600"),
    OPUS(98, "com.energyict.protocolimpl.elster.opus.Opus", FamilyRule.ELSTER),
    WAVE_TALK(99, "com.energyict.protocolimpl.coronis.wavetalk.WaveTalk", FamilyRule.CORONIS),
    SM150E(100, "com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent.SM150E", FamilyRule.CORONIS),
    ECHODIS(101, "com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis.Echodis", FamilyRule.CORONIS),
    FP93(102, "com.energyict.protocolimpl.EMCO.FP93"),
    METEOR(103, "com.energyict.protocolimpl.kenda.meteor.Meteor", FamilyRule.KENDA),
    DLMS_Z3_MESSAGING(104, "com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging", FamilyRule.DLMS, FamilyRule.ENERGYICT),
    PRI_PACT(105, "com.energyict.protocolimpl.pact.pripact.PRIPact", FamilyRule.PRI),
    MT83(106, "com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83", FamilyRule.IEC),
    ISKRA_EMECO(107, "com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco", FamilyRule.IEC),
    SEVC(108, "com.energyict.protocolimpl.actarissevc.SEVC", FamilyRule.ACTARIS),
    A140(109, "com.energyict.protocolimpl.iec1107.a140.A140", FamilyRule.IEC),
    PPM(110, "com.energyict.protocolimpl.iec1107.ppm.PPM", FamilyRule.IEC),
    ACE6000(111, "com.energyict.protocolimpl.dlms.actarisace6000.ACE6000", FamilyRule.DLMS),
    EICT_RTU_VDEW(112, "com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew", FamilyRule.IEC, FamilyRule.ENERGYICT),
    ION(113, "com.energyict.protocolimpl.powermeasurement.ion.Ion", FamilyRule.POWER_MEASUREMENT),
    MAX_SYS_US(114, "com.energyict.protocolimpl.landisgyr.us.maxsys2510.MaxSys", FamilyRule.LANDISGYR),
    DUKE_POWER(115, "com.energyict.protocolimpl.dukepower.DukePower"),
    KAMSTRUP(116, "com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup", FamilyRule.IEC, FamilyRule.KAMSTRUP),
    ABBA1700(117, "com.energyict.protocolimpl.iec1107.abba1700.ABBA1700", FamilyRule.IEC),
    MAX_SYS(118, "com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys", FamilyRule.LANDISGYR),
    LZQJ(119, "com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ", FamilyRule.IEC),
    UNI_LOG(120, "com.energyict.protocolimpl.iec1107.unilog.Unilog", FamilyRule.IEC),
    EICT_Z3(121, "com.energyict.protocolimpl.dlms.eictz3.EictZ3", FamilyRule.DLMS, FamilyRule.ENERGYICT),
    UNIGAS300(122, "com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300", FamilyRule.IEC, FamilyRule.KAMSTRUP),
    FLEX(123, "com.energyict.protocolimpl.dlms.flex.Flex", FamilyRule.DLMS),
    DLMSEMO(124, "com.energyict.protocolimpl.dlms.DLMSEMO", FamilyRule.DLMS),
    DLMSZMD(125, "com.energyict.protocolimpl.dlms.DLMSZMD", FamilyRule.DLMS),
    DLMSZMD_EXT(126, "com.energyict.protocolimpl.dlms.DLMSZMD_EXT", FamilyRule.DLMS),
    DLMS_EICT(127, "com.energyict.protocolimpl.dlms.DLMSEICT", FamilyRule.DLMS, FamilyRule.ENERGYICT),
    ABBA230(128, "com.energyict.protocolimpl.iec1107.abba230.ABBA230", FamilyRule.IEC),
    DATA_WATT(129, "com.energyict.protocolimpl.iec870.datawatt.DataWatt", FamilyRule.IEC),
    AS300D_ELSTER(130, "com.energyict.protocolimpl.dlms.elster.as300d.AS300D", FamilyRule.DLMS),
    AS300D_G3(131, "com.energyict.protocolimpl.dlms.g3.AS330D", FamilyRule.DLMS, FamilyRule.G3),
    SAGEM_COM(132, "com.energyict.protocolimpl.dlms.g3.SagemCom", FamilyRule.DLMS, FamilyRule.G3),
    SAGEM_COM_CX10006(133, "com.energyict.protocolimpl.dlms.prime.SagemComCX10006", FamilyRule.DLMS, FamilyRule.PRIME),
    ZIV(134, "com.energyict.protocolimpl.dlms.prime.ZIV", FamilyRule.DLMS, FamilyRule.PRIME),
    PRIME_METER(135, "com.energyict.protocolimpl.dlms.prime.PrimeMeter", FamilyRule.DLMS, FamilyRule.PRIME),
    AS330D(136, "com.energyict.protocolimpl.dlms.prime.AS330D", FamilyRule.DLMS, FamilyRule.PRIME),
    LGE450(137, "com.energyict.protocolimpl.dlms.prime.LGE450", FamilyRule.DLMS, FamilyRule.PRIME),
    CIRWATT(138, "com.energyict.protocolimpl.dlms.prime.Cirwatt", FamilyRule.DLMS, FamilyRule.PRIME),
    EK2XX(139, "com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx", FamilyRule.DLMS, FamilyRule.ELSTER),
    DLMS(140, "com.elster.protocolimpl.dlms.Dlms", FamilyRule.DLMS),
    DLMS_EK280_DUMMY(141, "com.elster.genericprotocolimpl.dlms.ek280.debug.DummyDlms", FamilyRule.DLMS, FamilyRule.TEST),
    DLMS_EK280(142, "com.elster.protocolimpl.dlms.EK280", FamilyRule.DLMS, FamilyRule.ELSTER),
    DLMS_SIMPLE(143, "com.energyict.protocolimpl.dlms.SimpleDLMSProtocol", FamilyRule.DLMS, FamilyRule.TEST),
    DLMS_LNSL7000(144, "com.energyict.protocolimpl.dlms.DLMSLNSL7000", FamilyRule.DLMS, FamilyRule.ACTARIS),
    ABBA1350(145, "com.energyict.protocolimpl.iec1107.abba1350.ABBA1350", FamilyRule.IEC),
    AS220_DLMS(146, "com.energyict.protocolimpl.dlms.as220.AS220", FamilyRule.DLMS),
    PPMI(147, "com.energyict.protocolimpl.iec1107.ppmi1.PPM", FamilyRule.IEC),
    DSFG(148, "com.elster.protocolimpl.dsfg.Dsfg", FamilyRule.DSFG),
    METCOM2(149, "com.energyict.protocolimpl.metcom.Metcom2", FamilyRule.METCOM),
    METCOM3(150, "com.energyict.protocolimpl.metcom.Metcom3", FamilyRule.METCOM),
    ENERMET_E70X_SCTM(151, "com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x", FamilyRule.ENERMET),
    EKM(152, "com.energyict.protocolimpl.sctm.ekm.EKM", FamilyRule.SCTM),
    METCOM_3FAF(153, "com.energyict.protocolimpl.metcom.Metcom3FAF", FamilyRule.METCOM),
    FAF10(154, "com.energyict.protocolimpl.sctm.faf.FAF10", FamilyRule.SCTM),
    FAF20(155, "com.energyict.protocolimpl.sctm.faf.FAF20", FamilyRule.SCTM),
    METCOM_3FAG(156, "com.energyict.protocolimpl.metcom.Metcom3FAG", FamilyRule.METCOM),
    FAG(157, "com.energyict.protocolimpl.sctm.fag.FAG", FamilyRule.SCTM),
    METCOM_3FBC(158, "com.energyict.protocolimpl.metcom.Metcom3FBC", FamilyRule.METCOM),
    FBC(159, "com.energyict.protocolimpl.sctm.fbc.FBC", FamilyRule.SCTM),
    MTT3A(160, "com.energyict.protocolimpl.sctm.mtt3a.MTT3A", FamilyRule.SCTM),
    METCOM_3FCL(161, "com.energyict.protocolimpl.metcom.Metcom3FCL", FamilyRule.METCOM),
    FCL(162, "com.energyict.protocolimpl.sctm.fcl.FCL", FamilyRule.SCTM),
    AS220_IEC(163, "com.energyict.protocolimpl.iec1107.as220.AS220", FamilyRule.IEC),
    ZIV5CTD(164, "com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd", FamilyRule.IEC),
    S4S_SIEMENS(165, "com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4s", FamilyRule.IEC, FamilyRule.SIEMENS),
    DL220_INSTROMET(166, "com.energyict.protocolimpl.iec1107.instromet.dl220.DL220", FamilyRule.IEC, FamilyRule.LIS),
    LIS200(167, "com.elster.protocolimpl.lis200.LIS200", FamilyRule.ELSTER, FamilyRule.LIS),
    EK220(168, "com.elster.protocolimpl.lis200.EK220", FamilyRule.ELSTER, FamilyRule.LIS),
    EK230(169, "com.elster.protocolimpl.lis200.EK230", FamilyRule.ELSTER, FamilyRule.LIS),
    EK260(170, "com.elster.protocolimpl.lis200.EK260", FamilyRule.ELSTER, FamilyRule.LIS),
    EK280(171, "com.elster.protocolimpl.lis200.EK280", FamilyRule.ELSTER, FamilyRule.LIS),
    DL220_LIS200(172, "com.elster.protocolimpl.lis200.DL220", FamilyRule.ELSTER, FamilyRule.LIS),
    DL240(173, "com.elster.protocolimpl.lis200.DL240", FamilyRule.ELSTER, FamilyRule.LIS),
    DL210(174, "com.elster.protocolimpl.lis200.DL210", FamilyRule.ELSTER, FamilyRule.LIS),
    INDIGO_PLUS(175, "com.energyict.protocolimpl.iec1107.indigo.IndigoPlus", FamilyRule.IEC),
    INDIGO_PXAR(176, "com.energyict.protocolimpl.iec1107.indigo.pxar.IndigoPXAR", FamilyRule.IEC),
    ABBA1140(177, "com.energyict.protocolimpl.iec1107.abba1140.ABBA1140", FamilyRule.IEC),
    ISKRA_ME37X(178, "com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X", FamilyRule.DLMS, FamilyRule.ISKRA),
    ZMD(179, "com.energyict.protocolimpl.iec1107.zmd.Zmd", FamilyRule.IEC, FamilyRule.LANDISGYR),

    // SmartMeterProtocols
    MX372(180, "com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice", FamilyRule.ISKRA, FamilyRule.DLMS),
    DSMR23_MBUS_EICT(181, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice", FamilyRule.DSMR, FamilyRule.ENERGYICT, FamilyRule.NTA, FamilyRule.DLMS),
    DSMR40_MBUS(182, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.Dsmr40MbusProtocol", FamilyRule.DSMR, FamilyRule.MBUS, FamilyRule.NTA, FamilyRule.DLMS),
    DSMR23_MBUS_ISKRA(183, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.MbusDevice", FamilyRule.DSMR, FamilyRule.MBUS, FamilyRule.NTA, FamilyRule.DLMS),
    DSMR40_MBUS_XEMEX(184, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.MbusDevice", FamilyRule.DSMR, FamilyRule.MBUS, FamilyRule.NTA, FamilyRule.DLMS),
    DSMR40_MBUS_LANDISGYR(185, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice", FamilyRule.DSMR, FamilyRule.LANDISGYR, FamilyRule.MBUS, FamilyRule.NTA, FamilyRule.DLMS),
    DSMR40_MBUS_ELSTER(186, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.elster.MBusDevice", FamilyRule.DSMR, FamilyRule.ELSTER, FamilyRule.MBUS, FamilyRule.DLMS),
    DSMR40_MBUS_IBM(187, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.MBusDevice", FamilyRule.DSMR, FamilyRule.MBUS, FamilyRule.IBM, FamilyRule.DLMS),
    WEB_RTU_Z3_SLAVE(188, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.SlaveMeter", FamilyRule.ENERGYICT, FamilyRule.DLMS),
    WEB_RTU_Z3_MBUS(189, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.MbusDevice", FamilyRule.ENERGYICT, FamilyRule.MBUS, FamilyRule.DLMS),
    WEB_RTU_Z3_EMETER(190, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.EMeter", FamilyRule.ENERGYICT, FamilyRule.DLMS),
    SDK_SMART_METER_PROTOCOL(191, "com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol", FamilyRule.SAMPLE, FamilyRule.ENERGYICT),
    ACTARIS_SL7000(192, "com.energyict.smartmeterprotocolimpl.actaris.sl7000.ActarisSl7000", FamilyRule.ACTARIS),
    ZMD_SMART(193, "com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD", FamilyRule.LANDISGYR, FamilyRule.DLMS),
    ISKRA_MX372(194, "com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372", FamilyRule.ISKRA, FamilyRule.DLMS),
    ZIGBEE_GAS(195, "com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas", FamilyRule.ENERGYICT, FamilyRule.DLMS),
    WEB_RTU_Z3(196, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3", FamilyRule.ENERGYICT, FamilyRule.DLMS),
    MX382(197, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382", FamilyRule.DSMR, FamilyRule.ISKRA, FamilyRule.NTA, FamilyRule.DLMS),
    E350(198, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350", FamilyRule.DSMR, FamilyRule.LANDISGYR, FamilyRule.NTA, FamilyRule.DLMS),
    KAIFA(199, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa", FamilyRule.DSMR, FamilyRule.IBM, FamilyRule.NTA, FamilyRule.DLMS),
    REMI(200, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.REMIDatalogger", FamilyRule.DSMR, FamilyRule.NTA, FamilyRule.DLMS),
    WEB_RTU_KP(201, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", FamilyRule.DSMR, FamilyRule.NTA, FamilyRule.ENERGYICT, FamilyRule.DLMS),
    DSMR40(202, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.Dsmr40Protocol", FamilyRule.DSMR, FamilyRule.NTA, FamilyRule.DLMS),
    UK_HUB(203, "com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub", FamilyRule.ENERGYICT, FamilyRule.DLMS),
    IN_HOME_DISPLAY(204, "com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay", FamilyRule.ENERGYICT, FamilyRule.DLMS),
    AS300(205, "com.energyict.smartmeterprotocolimpl.elster.apollo.AS300", FamilyRule.ELSTER, FamilyRule.DLMS),
    AS300DPET(206, "com.energyict.smartmeterprotocolimpl.elster.apollo5.AS300DPET", FamilyRule.ELSTER, FamilyRule.DLMS),

    // DeviceProtocols
    ELSTER_MBUS(207, "com.energyict.protocolimplv2.nta.elster.MbusDevice", FamilyRule.ELSTER, FamilyRule.NTA, FamilyRule.MBUS, FamilyRule.DLMS),
    WAVE_FLOW_V2(208, "com.energyict.protocolimplv2.coronis.waveflow.waveflowV2.WaveFlowV2", FamilyRule.CORONIS),
    APOLLO(209, "com.energyict.protocolimplv2.elster.am100r.apollo.ApolloMeter", FamilyRule.ELSTER, FamilyRule.DLMS),
    RTU_PLUS(210, "com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer", FamilyRule.ENERGYICT, FamilyRule.G3),
    GATE_WAY_Z3(211, "com.energyict.protocolimplv2.eict.gatewayz3.GateWayZ3", FamilyRule.ENERGYICT, FamilyRule.DLMS),
    AM100(212, "com.energyict.protocolimplv2.nta.elster.AM100", FamilyRule.ELSTER, FamilyRule.NTA, FamilyRule.DLMS),
    MTU155(213, "com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155"),
    WEB_RTU_WAVENIS_GATEWAY(214, "com.energyict.protocolimplv2.coronis.muc.WebRTUWavenisGateway", FamilyRule.CORONIS, FamilyRule.ENERGYICT),
    ACE4000_OUTBOUND(215, "com.energyict.protocolimplv2.ace4000.ACE4000Outbound", FamilyRule.ACTARIS),
    ACE4000_MBUS(216, "com.energyict.protocolimplv2.ace4000.ACE4000MBus", FamilyRule.ACTARIS),
    SDK_DEVICE_PROTOCOL(217, "com.energyict.protocolimplv2.sdksample.SDKDeviceProtocol", FamilyRule.SAMPLE);

    private int code;
    private String className;
    private Set<ProtocolFamily> families;

    LicensedProtocolRule (int code, String className, ProtocolFamily... families) {
        this.code = code;
        this.className = className;
        this.families = new HashSet<>(Arrays.asList(families));
    }

    @Override
    public int getCode () {
        return code;
    }

    @Override
    public String getClassName () {
        return className;
    }

    @Override
    public Set<ProtocolFamily> getFamilies () {
        return this.families;
    }


    /**
     * Returns the LicensedProtocolRule that is uniquely identified by the code.
     *
     * @param code The code
     * @return The LicensedProtocolRule or <code>null</code> if no LicensedProtocolRule is uniquely identified by the code
     */
    public static LicensedProtocol fromCode (int code) {
        for (LicensedProtocolRule protocol : values()) {
            if (code == protocol.code) {
                return protocol;
            }
        }
        return null;
    }

    /**
     * Returns the LicensedProtocolRule that is implemented by the class
     * whose name is the specified <code>className</code>.
     *
     * @param className The name of a protocol class
     * @return The LicensedProtocolRule or <code>null</code> if no LicensedProtocolRule is implemented by a class by that name
     */
    public static LicensedProtocol fromClassName (String className) {
        for (LicensedProtocolRule protocol : values()) {
            if (className.equals(protocol.className)) {
                return protocol;
            }
        }
        return null;
    }

}