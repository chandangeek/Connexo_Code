package com.energyict.license;

import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.ProtocolFamily;

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
    A1440(1, "com.energyict.protocolimpl.iec1107.a1440.A1440", FamilyRule.ELSTER_IEC),
    ABBA1500(2, "com.energyict.protocolimpl.iec1107.abba1500.ABBA1500"),
    EIWEB(3, "com.energyict.rtuprotocol.EIWeb", FamilyRule.EICT_RTU_EMS),
    EK88(4, "com.elster.protocolimpl.lis100.EK88"),
    INSTROMET444(5, "com.energyict.protocolimpl.instromet.v444.Instromet444"),
    INSTROMET555(6, "com.energyict.protocolimpl.instromet.v555.Instromet555"),
    MEDO(7, "com.energyict.protocolimpl.kenda.medo.Medo"),
    MK10(8, "com.energyict.protocolimpl.edmi.mk10.MK10", FamilyRule.EDMI),
    QUANTUM1000_ITRON(9, "com.energyict.protocolimpl.itron.quantum1000.Quantum1000"),
    RTM(10, "com.energyict.protocolimpl.coronis.amco.rtm.RTM", FamilyRule.CORONIS),
    RTU_PLUS_BUS(11, "com.energyict.protocolimpl.rtuplusbus.rtuplusbus", FamilyRule.EICT_RTU_EMS),
    SENTINEL(12, "com.energyict.protocolimpl.itron.sentinel.Sentinel"),
    SIEMENS_7ED62(13, "com.energyict.protocolimpl.siemens7ED62.Siemens7ED62"),
    SDC(14, "com.energyict.protocolimpl.iec1107.sdc.Sdc"),
    QUANTUM_SCHLUMBERGER(15, "com.energyict.protocolimpl.itron.quantum.Quantum"),
    SDK_DEVICE_PROTOCOL_WITH_ALL_PROPERTIES(16, "test.com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithAllProperties", FamilyRule.TEST),
    DATASTAR(17, "com.energyict.protocolimpl.itron.datastar.Datastar"),
    VECTRON(18, "com.energyict.protocolimpl.itron.vectron.Vectron"),
    EZ7(19, "com.energyict.protocolimpl.emon.ez7.EZ7"),
    JEMSTAR(20, "com.energyict.protocolimpl.ametek.JemStar"),
    MK6(21, "com.energyict.protocolimpl.edmi.mk6.MK6"),
    WAVE_SENSE(22, "com.energyict.protocolimpl.coronis.wavesense.WaveSense", FamilyRule.CORONIS),
    ALPHA_PLUS(23, "com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus"),
    ALPHA_BASIC(24, "com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic"),
    EICT_TEST(25, "test.com.energyict.protocolimpl.eicttest.EICTTestProtocol", FamilyRule.TEST),
    S4_DGCOM(26, "com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4"),
    G3B(27, "com.energyict.protocolimpl.dlms.elgama.G3B"),
    JANZC280(28, "com.energyict.protocolimpl.dlms.JanzC280.JanzC280"),
    IDIS(29, "com.energyict.protocolimpl.dlms.idis.IDIS", FamilyRule.ELSTER_IDIS, FamilyRule.ELSTER_IDIS_P1),
    IDIS_MBUS(30, "com.energyict.protocolimpl.dlms.idis.IDISMBus", FamilyRule.ELSTER_IDIS, FamilyRule.ELSTER_IDIS_P1),
    WAVE_THERM(31, "com.energyict.protocolimpl.coronis.wavetherm.WaveTherm", FamilyRule.CORONIS),
    PROMETER(32, "com.energyict.protocolimpl.iec1107.cewe.prometer.Prometer", FamilyRule.CEWE_PROMETER),
    GEKV(33, "com.energyict.protocolimpl.ge.kv.GEKV", FamilyRule.KV_ANSI),
    S200(34, "com.energyict.protocolimpl.landisgyr.sentry.s200.S200"),
    AS253(35, "com.energyict.protocolimpl.coronis.waveflowDLMS.AS253"),
    AS1253(36, "com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253"),
    A1800_CORONIS(37, "com.energyict.protocolimpl.coronis.waveflowDLMS.A1800"),
    WAVE_LOG(38, "com.energyict.protocolimpl.coronis.wavelog.WaveLog", FamilyRule.CORONIS),
    CEWE_PROMETER(39, "com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer", FamilyRule.CEWE_PROMETER),
    SDK_SAMPLE_PROTOCOL(41, "test.com.energyict.protocolimpl.sdksample.SDKSampleProtocol", FamilyRule.TEST),
    TRIMARAN_CJE(42, "com.energyict.protocolimpl.edf.trimarancje.Trimaran"),
    GEKV2(43, "com.energyict.protocolimpl.ge.kv2.GEKV2", FamilyRule.KV_ANSI),
    CM32(44, "com.energyict.protocolimpl.CM32.CM32"),
    NEXUS1272(45, "com.energyict.protocolimpl.eig.nexus1272.Nexus1272"),
    IQ230(46, "com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230", FamilyRule.EICT_SUBMETER_EMS),
    IQ200(47, "com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200", FamilyRule.EICT_SUBMETER_EMS),
    A40(48, "com.energyict.protocolimpl.modbus.socomec.a40.A40", FamilyRule.EICT_SUBMETER_EMS),
    ENERIUM200(49, "com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200", FamilyRule.ENERDIS_ENERIUM),
    ENERIUM50(50, "com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50", FamilyRule.ENERDIS_ENERIUM),
    ENERIUM150(51, "com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150", FamilyRule.ENERDIS_ENERIUM),
    COMPACT_NSX(52, "com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX", FamilyRule.EICT_SUBMETER_EMS),
    CI(53, "com.energyict.protocolimpl.modbus.socomec.countis.ci.Ci", FamilyRule.EICT_SUBMETER_EMS),
    A20(54, "com.energyict.protocolimpl.modbus.socomec.a20.A20", FamilyRule.EICT_SUBMETER_EMS),
    EICT_MODBUS_RTU(55, "com.energyict.protocolimpl.modbus.eictmodbusrtu.EictModbusRtu", FamilyRule.EICT_SUBMETER_EMS),
    PM750(56, "com.energyict.protocolimpl.modbus.squared.pm750.PM750", FamilyRule.EICT_SUBMETER_EMS),
    HAWKEYE(57, "com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye", FamilyRule.EICT_SUBMETER_EMS),
    EIMETER_FLEX_SM352(58, "com.energyict.protocolimpl.modbus.northerndesign.eimeterflex.EIMeterFlexSM352", FamilyRule.EICT_SUBMETER_EMS),    //Deprecated
    REC_DIGIT_CDTE(59, "com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtE", FamilyRule.ENERDIS_RECDIGIT),
    REC_DIGIT_CDTPR(60, "com.energyict.protocolimpl.modbus.enerdis.cdt.RecDigitCdtPr", FamilyRule.ENERDIS_RECDIGIT),
    REC_DIGIT_CCT(61, "com.energyict.protocolimpl.modbus.enerdis.recdigitcct.RecDigitCct", FamilyRule.ENERDIS_RECDIGIT),
    EIMETER(62, "com.energyict.protocolimpl.modbus.eimeter.EIMeter", FamilyRule.EICT_SUBMETER_EMS),
    REC_DIGIT1800(63, "com.energyict.protocolimpl.modbus.enerdis.recdigit1800.RecDigit1800", FamilyRule.ENERDIS_RECDIGIT),
    REC_DIGIT_POWER(64, "com.energyict.protocolimpl.modbus.enerdis.recdigitpower.RecDigitPower", FamilyRule.ENERDIS_RECDIGIT),
    EICT_VERIS(65, "com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.EictVeris", FamilyRule.EICT_SUBMETER_EMS),
    UNIFLO1200(66, "com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200"),
    PQM2(67, "com.energyict.protocolimpl.modbus.ge.pqm2.PQM2", FamilyRule.EICT_SUBMETER_EMS),
    PM800(68, "com.energyict.protocolimpl.modbus.squared.pm800.PM800", FamilyRule.EICT_SUBMETER_EMS),
    EPM2200(69, "com.energyict.protocolimpl.modbus.multilin.epm2200.EPM2200", FamilyRule.EICT_SUBMETER_EMS),
    S4S(70, "com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.S4s"),
    SHARKY770(71, "com.energyict.protocolimpl.mbus.hydrometer.sharky770.Sharky770", FamilyRule.EICT_SUBMETER_EMS),
    MBUS_GENERIC(72, "com.energyict.protocolimpl.mbus.generic.Generic", FamilyRule.EICT_SUBMETER_EMS),
    PN16(73, "com.energyict.protocolimpl.mbus.nzr.pn16.PN16", FamilyRule.EICT_SUBMETER_EMS),
    TRIMARAN2P(74, "com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P"),
    ENERMET_E70X_IEC(75, "com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X"),
    ENERMET_E60X_IEC(76, "com.energyict.protocolimpl.iec1107.enermete60x.EnermetE60X"),
    POREG2(77, "com.energyict.protocolimpl.din19244.poreg2.Poreg2", FamilyRule.POREG),
    POREG2P(78, "com.energyict.protocolimpl.din19244.poreg2.Poreg2P", FamilyRule.POREG),
    S4_ANSI(79, "com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4"),
    E120(80, "com.energyict.protocolimpl.enermet.e120.E120"),
    TRIMARAN_PLUS(81, "com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus"),
    TRIMARAN(82, "com.energyict.protocolimpl.edf.trimaran.Trimaran"),
    WAVE_FLOW_V1(83, "com.energyict.protocolimpl.coronis.waveflow.waveflowV1.WaveFlowV1", FamilyRule.CORONIS),
    WAVE_FLOW_V2_OLD(84, "com.energyict.protocolimpl.coronis.waveflow.waveflowV2.WaveFlowV2", FamilyRule.CORONIS),
    WAVE_FLOW_V210(85, "com.energyict.protocolimpl.coronis.waveflow.waveflowV210.WaveFlowV210", FamilyRule.CORONIS),
    HYDREKA(86, "com.energyict.protocolimpl.coronis.waveflow.hydreka.Hydreka", FamilyRule.CORONIS),
    ALPHA_A3(87, "com.energyict.protocolimpl.elster.a3.AlphaA3"),
    A1800_ELSTER(88, "com.energyict.protocolimpl.elster.a1800.A1800"),
    MARKV(89, "com.energyict.protocolimpl.transdata.markv.MarkV"),
    CM10(90, "com.energyict.protocolimpl.cm10.CM10"),
    U1600(91, "com.energyict.protocolimpl.gmc.u1600.U1600"),
    OPUS(92, "com.energyict.protocolimpl.elster.opus.Opus"),
    WAVE_TALK(93, "com.energyict.protocolimpl.coronis.wavetalk.WaveTalk", FamilyRule.CORONIS),
    SM150E(94, "com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent.SM150E", FamilyRule.CORONIS),
    ECHODIS(95, "com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis.Echodis", FamilyRule.CORONIS),
    FP93(96, "com.energyict.protocolimpl.EMCO.FP93"),
    METEOR(97, "com.energyict.protocolimpl.kenda.meteor.Meteor"),
    PRI_PACT(98, "com.energyict.protocolimpl.pact.pripact.PRIPact"),
    MT83(99, "com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83"),
    ISKRA_EMECO(100, "com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco"),
    SEVC(101, "com.energyict.protocolimpl.actarissevc.SEVC"),
    A140(102, "com.energyict.protocolimpl.iec1107.a140.A140"),
    PPM(103, "com.energyict.protocolimpl.iec1107.ppm.PPM", FamilyRule.PPM),
    ACE6000(104, "com.energyict.protocolimpl.dlms.actarisace6000.ACE6000"),
    EICT_RTU_VDEW(105, "com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew", FamilyRule.EICT_RTU_EMS),
    ION(106, "com.energyict.protocolimpl.powermeasurement.ion.Ion"),
    MAX_SYS_US(107, "com.energyict.protocolimpl.landisgyr.us.maxsys2510.MaxSys"),
    DUKE_POWER(108, "com.energyict.protocolimpl.dukepower.DukePower", FamilyRule.EICT_RTU_EMS),
    KAMSTRUP(109, "com.energyict.protocolimpl.iec1107.kamstrup.Kamstrup"),
    ABBA1700(110, "com.energyict.protocolimpl.iec1107.abba1700.ABBA1700"),
    MAX_SYS(111, "com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys"),
    LZQJ(112, "com.energyict.protocolimpl.iec1107.emh.lzqj.LZQJ"),
    UNI_LOG(113, "com.energyict.protocolimpl.iec1107.unilog.Unilog"),
    UNIGAS300(114, "com.energyict.protocolimpl.iec1107.kamstrup.unigas300.Unigas300"),
    FLEX(115, "com.energyict.protocolimpl.dlms.flex.Flex"),
    DLMSEMO(116, "com.energyict.protocolimpl.dlms.DLMSEMO"),
    DLMS_EICT(117, "com.energyict.protocolimpl.dlms.DLMSEICT", FamilyRule.EICT_RTU_EMS),
    ABBA230(118, "com.energyict.protocolimpl.iec1107.abba230.ABBA230"),
    DATA_WATT(119, "com.energyict.protocolimpl.iec870.datawatt.DataWatt"),
    AS300D_G3(120, "com.energyict.protocolimpl.dlms.g3.AS330D", FamilyRule.G3_LINKY_DLMS),
    SAGEM_COM(121, "com.energyict.protocolimpl.dlms.g3.SagemCom", FamilyRule.G3_LINKY_DLMS),
    SAGEM_COM_CX10006(122, "com.energyict.protocolimpl.dlms.prime.SagemComCX10006"),
    ZIV(123, "com.energyict.protocolimpl.dlms.prime.ZIV"),
    PRIME_METER(124, "com.energyict.protocolimpl.dlms.prime.PrimeMeter", FamilyRule.PRIME),
    AS330D(125, "com.energyict.protocolimpl.dlms.prime.AS330D", FamilyRule.PRIME),
    LGE450(126, "com.energyict.protocolimpl.dlms.prime.LGE450", FamilyRule.PRIME),
    CIRWATT(127, "com.energyict.protocolimpl.dlms.prime.Cirwatt", FamilyRule.PRIME),
    EK2XX(128, "com.energyict.protocolimpl.dlms.elster.ek2xx.EK2xx"),
    DLMS_SIMPLE(129, "test.com.energyict.protocolimpl.dlms.SimpleDLMSProtocol", FamilyRule.TEST),
    DLMS_LNSL7000(130, "com.energyict.protocolimpl.dlms.DLMSLNSL7000"),
    ABBA1350(131, "com.energyict.protocolimpl.iec1107.abba1350.ABBA1350"),
    AS220_DLMS(132, "com.energyict.protocolimpl.dlms.as220.AS220", FamilyRule.ELSTER_MULTI_FREQ),
    PPMI(133, "com.energyict.protocolimpl.iec1107.ppmi1.PPM", FamilyRule.PPM),
    DSFG(134, "com.elster.protocolimpl.dsfg.Dsfg"),
    ENERMET_E70X_SCTM(135, "com.energyict.protocolimpl.sctm.enermete70x.EnermetE70x"),
    EKM(136, "com.energyict.protocolimpl.sctm.ekm.EKM", FamilyRule.LG_SCTM),
    FAF10(137, "com.energyict.protocolimpl.sctm.faf.FAF10", FamilyRule.LG_SCTM),
    FAF20(138, "com.energyict.protocolimpl.sctm.faf.FAF20", FamilyRule.LG_SCTM),
    FAG(139, "com.energyict.protocolimpl.sctm.fag.FAG", FamilyRule.LG_SCTM),
    FBC(140, "com.energyict.protocolimpl.sctm.fbc.FBC", FamilyRule.LG_SCTM),
    MTT3A(141, "com.energyict.protocolimpl.sctm.mtt3a.MTT3A"),
    FCL(142, "com.energyict.protocolimpl.sctm.fcl.FCL", FamilyRule.LG_SCTM),
    AS220_IEC(143, "com.energyict.protocolimpl.iec1107.as220.AS220", FamilyRule.ELSTER_IEC),
    ZIV5CTD(144, "com.energyict.protocolimpl.iec870.ziv5ctd.Ziv5Ctd"),
    S4S_SIEMENS(145, "com.energyict.protocolimpl.iec1107.siemenss4s.SiemensS4s"),
    EK220(146, "com.elster.protocolimpl.lis200.EK220", FamilyRule.ELSTER_Ek_LIS200),
    EK230(147, "com.elster.protocolimpl.lis200.EK230", FamilyRule.ELSTER_Ek_LIS200),
    EK260(148, "com.elster.protocolimpl.lis200.EK260", FamilyRule.ELSTER_Ek_LIS200),
    EK280(149, "com.elster.protocolimpl.lis200.EK280", FamilyRule.ELSTER_Ek_LIS200),
    DL220_LIS200(150, "com.elster.protocolimpl.lis200.DL220", FamilyRule.ELSTER_DL_LIS200),
    DL240(151, "com.elster.protocolimpl.lis200.DL240", FamilyRule.ELSTER_DL_LIS200),
    DL210(152, "com.elster.protocolimpl.lis200.DL210", FamilyRule.ELSTER_DL_LIS200),
    INDIGO_PLUS(153, "com.energyict.protocolimpl.iec1107.indigo.IndigoPlus"),
    ABBA1140(154, "com.energyict.protocolimpl.iec1107.abba1140.ABBA1140"),
    ISKRA_ME37X(155, "com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X"),
    ZMD(156, "com.energyict.protocolimpl.iec1107.zmd.Zmd"),

    // SmartMeterProtocols
    MX372(157, "com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice", FamilyRule.ISKRA_PRE_NTA),
    DSMR23_MBUS_EICT(158, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice", FamilyRule.EICT_NTA),
    DSMR23_MBUS_ISKRA(159, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.MbusDevice", FamilyRule.EICT_NTA),
    DSMR40_MBUS_XEMEX(160, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.MbusDevice", FamilyRule.XEMEX),
    DSMR40_MBUS_LANDISGYR(161, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice", FamilyRule.DSMR_NTA),
    DSMR40_MBUS_ELSTER(162, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.elster.MBusDevice", FamilyRule.DSMR_NTA),
    DSMR40_MBUS_IBM(163, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.MBusDevice", FamilyRule.DSMR_NTA),
    WEB_RTU_Z3_MBUS(165, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.MbusDevice", FamilyRule.EICT_Z3, FamilyRule.EICT_RTU_EMS),
    WEB_RTU_Z3_EMETER(166, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.EMeter", FamilyRule.EICT_Z3, FamilyRule.EICT_RTU_EMS),
    ACTARIS_SL7000(167, "com.energyict.smartmeterprotocolimpl.actaris.sl7000.ActarisSl7000"),
    ZMD_SMART(168, "com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD"),
    ISKRA_MX372(169, "com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372", FamilyRule.ISKRA_PRE_NTA),
    UKHUB_ZIGBEE_GAS(170, "com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas", FamilyRule.ELSTER_SSWG_IC),
    WEB_RTU_Z3(171, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3", FamilyRule.EICT_Z3, FamilyRule.EICT_RTU_EMS),
    MX382(172, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382", FamilyRule.EICT_NTA),
    E350(173, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350", FamilyRule.DSMR_NTA),
    KAIFA(174, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa", FamilyRule.DSMR_NTA),
    REMI(175, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.REMIDatalogger", FamilyRule.XEMEX),
    WEB_RTU_KP(176, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", FamilyRule.EICT_NTA),
    UK_HUB(177, "com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub", FamilyRule.ELSTER_SSWG_IC),
    UKHUB_IHD(178, "com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay", FamilyRule.ELSTER_SSWG_IC),
    AS300(179, "com.energyict.smartmeterprotocolimpl.elster.apollo.AS300", FamilyRule.ELSTER_SSWG_IC),
    AS300DPET(180, "com.energyict.smartmeterprotocolimpl.elster.apollo5.AS300DPET"),
    AM110R(181, "com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R", FamilyRule.ELSTER_SSWG_EC),
    AM110R_ZIGBEE_GAS(182, "com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ZigbeeGas", FamilyRule.ELSTER_SSWG_EC),
    AM110R_ZIGBEE_IHD(183, "com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.InHomeDisplay", FamilyRule.ELSTER_SSWG_EC),
    AS300P(184, "com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300P", FamilyRule.ELSTER_SSWG_EC),

    // DeviceProtocols
    ELSTER_MBUS(185, "com.energyict.protocolimplv2.nta.elster.MbusDevice", FamilyRule.ELSTER_AM100),
    RTU_PLUS_G3(186, "com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer", FamilyRule.G3_LINKY_DLMS, FamilyRule.G3_PLC),
    AM100(188, "com.energyict.protocolimplv2.nta.elster.AM100", FamilyRule.ELSTER_AM100),
    MTU155(189, "com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155"),
    WEB_RTU_WAVENIS_GATEWAY(190, "test.com.energyict.protocolimplv2.coronis.muc.WebRTUWavenisGateway", FamilyRule.CORONIS),
    ACE4000_OUTBOUND(191, "com.energyict.protocolimplv2.ace4000.ACE4000Outbound", FamilyRule.ACTARIS),
    ACE4000_MBUS(192, "com.energyict.protocolimplv2.ace4000.ACE4000MBus", FamilyRule.ACTARIS),
    SDK_DEVICE_PROTOCOL(193, "test.com.energyict.protocolimplv2.sdksample.SDKDeviceProtocol", FamilyRule.TEST),
    RTU_PLUS_IDIS(194, "com.energyict.protocolimplv2.eict.rtuplusserver.idis.RtuPlusServer", FamilyRule.ELSTER_IDIS),
    AS220_GAS(195, "com.energyict.protocolimpl.dlms.as220.GasDevice", FamilyRule.ELSTER_MULTI_FREQ),

    // Rtu+ Server
    RtuServer(196, "com.energyict.rtuprotocol.RtuServer", FamilyRule.EICT_RTU_EMS, FamilyRule.ELSTER_MULTI_FREQ, FamilyRule.ELSTER_IDIS),

    EIWEB_DEVICE_PROTOCOL(197, "com.energyict.protocolimplv2.eict.eiweb.EIWeb", FamilyRule.EICT_RTU_EMS),
    EIMETER_FLEX_SLAVE_MODULE(198, "com.energyict.protocolimpl.modbus.energyict.EIMeterFlexSlaveModule", FamilyRule.EICT_SUBMETER_EMS),

    WEB_RTU_KP_V2(199, "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", FamilyRule.EICT_NTA),
    CX20009_V2(200, "com.energyict.protocolimplv2.edp.CX20009", FamilyRule.EDP_DLMS),
    JANZ_B280_V2(201, "com.energyict.protocolimplv2.edp.JanzB280", FamilyRule.EDP_DLMS),
    GENERIC_MODBUS(202, "com.energyict.protocolimpl.modbus.generic.Generic", FamilyRule.EICT_SUBMETER_EMS),
    DSMR23_MBUS_EICT_V2(203, "com.energyict.protocolimplv2.nta.dsmr23.eict.MbusDevice", FamilyRule.EICT_NTA),
    ISKRA_MT880(205, "com.energyict.smartmeterprotocolimpl.iskra.mt880.IskraMT880"),
    ITRON_FULCRUM(206, "com.energyict.protocolimpl.itron.fulcrum.Fulcrum"),
    JANZ_B280(207, "com.energyict.protocolimpl.dlms.edp.JanzB280", FamilyRule.EDP_DLMS),
    CX20009(208, "com.energyict.protocolimpl.dlms.edp.CX20009", FamilyRule.EDP_DLMS),
    SIEMENS_7ED62_VDEW(209, "com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62"),
    WATCHTALK(210, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.WatchTalk", FamilyRule.EICT_NTA),
    WATCHTALK_MBUS(211, "com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.MbusDevice", FamilyRule.EICT_NTA),
    EK280_DLMS(212, "com.elster.protocolimpl.dlms.EK280"),
    CUBE350(213, "com.energyict.protocolimpl.modbus.northerndesign.cube350.Cube350", FamilyRule.EICT_SUBMETER_EMS),
    RTU_PLUS_EIWEBPLUS(214, "com.energyict.protocolimplv2.eict.rtuplusserver.eiwebplus.RtuServer", FamilyRule.EICT_RTU_EMS, FamilyRule.ELSTER_IDIS),
    SDK_SMART_SAMPLE_PROTOCOL(215, "test.com.energyict.smartmeterprotocolimpl.sdksample.SDKSmartMeterProtocol", FamilyRule.TEST),
    ELSTER_GARNET_CONCENTRATOR(216, "com.energyict.protocolimplv2.elster.garnet.GarnetConcentrator", FamilyRule.ELSTER_GARNET),
    ELSTER_GARNET_AC100C(217, "com.energyict.protocolimplv2.elster.garnet.A100C", FamilyRule.ELSTER_GARNET),
    ELSTER_A1800(218, "com.energyict.protocolimpl.dlms.a1800.A1800"),
    ELSTER_AM540(219, "com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540"),
    SAGEMCOM_CX2009_DSMR(220, "com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.CX20009"),
    SOCOMEC_COUNTIS_E44(221, "com.energyict.protocolimpl.modbus.socomec.countis.e44.E44", FamilyRule.EICT_SUBMETER_EMS),
    EMH_NXT4(222, "com.energyict.protocolimpl.iec1107.emh.nxt4.NXT4"),
    ELSTER_A1055(223, "com.energyict.protocolimplv2.abnt.elster.A1055"),
    AM540(224, "com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540", FamilyRule.ELSTER_IDIS, FamilyRule.ELSTER_IDIS_P2, FamilyRule.G3_PLC),
    AM500(225, "com.energyict.protocolimplv2.dlms.idis.am500.AM500", FamilyRule.ELSTER_IDIS, FamilyRule.ELSTER_IDIS_P1),
    AM500_MBUS_DEVICE(226, "com.energyict.protocolimplv2.dlms.idis.am500.MBusDevice", FamilyRule.ELSTER_IDIS, FamilyRule.ELSTER_IDIS_P1),
    AM130(227, "com.energyict.protocolimplv2.dlms.idis.am130.AM130", FamilyRule.ELSTER_IDIS_P2),
    AM130_MBUS_DEVICE(228, "com.energyict.protocolimplv2.dlms.idis.am130.MBusDevice", FamilyRule.ELSTER_IDIS_P2),
    AM540_MBUS_DEVICE(229, "com.energyict.protocolimplv2.nta.dsmr50.elster.am540.MBusDevice", FamilyRule.ELSTER_IDIS, FamilyRule.ELSTER_IDIS_P2),
/*
    //TODO add these crypto protocol entries to seperate LicensedProtocolRules that will be handled in different protocol bundles
    //Crypto protocols
    CryptoAM540(230, "com.enexis.smartmeterprotocolimpl.nta.dsmr50.elster.am540.CryptoAM540"),
    CryptoCX20009(231, "com.enexis.smartmeterprotocolimpl.nta.dsmr50.sagemcom.CryptoCX20009"),
    Eict_CryptoMbusDevice(232, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.eict.CryptoMbusDevice", FamilyRule.EICT_NTA),
    CryptoWebRTUKP(233, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.eict.CryptoWebRTUKP", FamilyRule.EICT_NTA),
    Iskra_CryptoMbusDevice(234, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.iskra.CryptoMbusDevice", FamilyRule.EICT_NTA),
    CryptoMx382(235, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.iskra.CryptoMx382", FamilyRule.EICT_NTA),
    Elster_CryptoMbusDevice(236, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.elster.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CryptoKaifa(237, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.ibm.CryptoKaifa", FamilyRule.DSMR_NTA),
    Ibm_CryptoMbusDevice(238, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.ibm.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CryptoE350(239, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.landisgyr.CryptoE350", FamilyRule.DSMR_NTA),
    Landis_Gyr_CryptoMbusDevice(240, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.landisgyr.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    Itron_CryptoMbusDevice(241, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.itron.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    Dummy_CryptoMbusDevice(242, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.common.CryptoMbusDevice", FamilyRule.DSMR_NTA),

    CryptoWebRtuKpWithBreaker(243, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.eict.CryptoWebRtuKpWithBreaker", FamilyRule.EICT_NTA),
    Eict_CryptoMbusDeviceWithBreaker(244, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.eict.CryptoMbusDeviceWithBreaker", FamilyRule.EICT_NTA),
    CryptoMx382WithBreaker(245, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.iskra.CryptoMx382WithBreaker", FamilyRule.EICT_NTA),
    Iskra_CryptoMbusDeviceWithBreaker(246, "com.enexis.smartmeterprotocolimpl.nta.dsmr23.iskra.CryptoMbusDeviceWithBreaker", FamilyRule.EICT_NTA),
    CryptoE350WithBreaker(247, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.landisgyr.CryptoE350WithBreaker", FamilyRule.DSMR_NTA),
    CryptoKaifaWithBreaker(248, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.ibm.CryptoKaifaWithBreaker", FamilyRule.DSMR_NTA),
    Ibm_CryptoMbusDeviceWithBreaker(249, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.ibm.CryptoMbusDeviceWithBreaker", FamilyRule.DSMR_NTA),
    Landis_Gyr_CryptoMbusDeviceWithBreaker(250, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.landisgyr.CryptoMbusDeviceWithBreaker", FamilyRule.DSMR_NTA),
    Dummy_CryptoMbusDeviceWithBreaker(251, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.common.CryptoMbusDeviceWithBreaker", FamilyRule.DSMR_NTA),
    Elster_CryptoMbusDeviceWithBreaker(252, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.elster.CryptoMbusDeviceWithBreaker", FamilyRule.DSMR_NTA),
    Itron_CryptoMbusDeviceWithBreaker(253, "com.enexis.smartmeterprotocolimpl.nta.dsmr40.itron.CryptoMbusDeviceWithBreaker", FamilyRule.DSMR_NTA),*/

    PM5560(254, "com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5560"),
    PM5561(280, "com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5561"),
    WEB_RTU_Z3_V2(255, "com.energyict.protocolimplv2.eict.webrtuz3.WebRTUZ3", FamilyRule.EICT_Z3, FamilyRule.EICT_RTU_EMS),
    WEB_RTU_Z3_MBUS_DEVICE_V2(256, "com.energyict.protocolimplv2.eict.webrtuz3.MBusDevice", FamilyRule.EICT_Z3, FamilyRule.EICT_RTU_EMS),
    WEB_RTU_Z3_EMETER_V2(257, "com.energyict.protocolimplv2.eict.webrtuz3.EMeter", FamilyRule.EICT_Z3, FamilyRule.EICT_RTU_EMS),
    TRANSPARENT_GATEWAY(258, "com.energyict.protocolimplv2.eict.gateway.TransparentGateway", FamilyRule.EICT_RTU_EMS),
    G3_AS330D_V2(259, "com.energyict.protocolimplv2.dlms.g3.AS330D", FamilyRule.G3_LINKY_DLMS, FamilyRule.G3_PLC),
    G3_BEACON_3100(260, "com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100", FamilyRule.G3_PLC, FamilyRule.ELSTER_IDIS_P2),
    E35C(261, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.E35C"),
    ELSTER_A1_THEMISPLUS(262, "com.elster.protocolimpl.dlms.A1"),
    DL230_LIS200(263, "com.elster.protocolimpl.lis200.DL230"),
    AM540_IDIS_P2_DEVICE(264, "com.energyict.protocolimplv2.dlms.idis.am540.AM540", FamilyRule.ELSTER_IDIS_P2, FamilyRule.G3_PLC),
    AM540_IDIS_P2_MBUS_DEVICE(265, "com.energyict.protocolimplv2.dlms.idis.am540.MBusDevice", FamilyRule.ELSTER_IDIS_P2),

    FP93B(266, "com.energyict.protocolimpl.modbus.emco.FP93B"),
    VLM20(267, "com.energyict.protocolimpl.modbus.spiraxsarco.VLM20"),
    RIM20(268, "com.energyict.protocolimpl.modbus.spiraxsarco.RIM20"),
/*

    CryptoE350_42(269, "com.enexis.smartmeterprotocolimpl.nta.dsmr42.landisgyr.CryptoE350", FamilyRule.DSMR_NTA),
    CryptoKaifa_42(270, "com.enexis.smartmeterprotocolimpl.nta.dsmr42.ibm.Cryptokaifa", FamilyRule.DSMR_NTA),
    Landis_Gyr_CryptoMbusDevice_42(271, "com.enexis.smartmeterprotocolimpl.nta.dsmr42.landisgyr.CryptoMbusDevice", FamilyRule.EICT_NTA),
    Elster_CryptoMbusDevice_42(272, "com.enexis.smartmeterprotocolimpl.nta.dsmr42.elster.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    Dummy_CryptoMbusDevice_42(273, "com.enexis.smartmeterprotocolimpl.nta.dsmr42.common.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    Itron_CryptoMbusDevice_42(274, "com.enexis.smartmeterprotocolimpl.nta.dsmr42.itron.CryptoMbusDevice", FamilyRule.DSMR_NTA),

    CryptoWebRTUKP_42(275, "com.enexis.protocolimplv2.nta.dsmr23.eict.CryptoWebRTUKP", FamilyRule.EICT_NTA),
    CryptoRtuPlusServer_42(276, "com.enexis.protocolimplv2.rtuplusserver.g3.CryptoRtuPlusServer", FamilyRule.G3_LINKY_DLMS, FamilyRule.G3_PLC),
*/

    Mx382_IDIS(278, "com.energyict.protocolimplv2.dlms.idis.iskra.mx382.Mx382"),
    MIGRATION_DUMMY_PROTOCOL(279, "com.energyict.protocolimpl.migration.DummyProtocol"),
    T210D(281, "com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.T210D"),
    QUAD4(282, "com.elster.us.protocolimpl.landisgyr.quad4.Quad4"),

    //Evn, Salzburg crypto protocols
    CRYPTO_AM540(283, "com.energyict.protocolimplv2.dlms.idis.am540.CryptoAM540", FamilyRule.G3_PLC, FamilyRule.ELSTER_IDIS_P2),
    CRYPTO_BEACON3100(284, "com.energyict.protocolimplv2.eict.rtu3.beacon3100.CryptoBeacon3100", FamilyRule.G3_PLC, FamilyRule.ELSTER_IDIS_P2),

    T210D_MBUS_DEVICE(285, "com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.MBusDevice"),
    MK10_V2(286, "com.energyict.protocolimplv2.edmi.mk10.MK10", FamilyRule.EDMI),
    MK6_V2(287, "com.energyict.protocolimplv2.edmi.mk6.MK6"),

//CRYPTO_T210D(288, "com.sagemcom.protocolimplv2.dlms.idis.T210D.CryptoT210D"),

    AM132 (289, "com.energyict.protocolimplv2.dlms.idis.am132.AM132"),
    CRYPTO_AM132(290, "com.energyict.protocolimplv2.dlms.idis.am132.CryptoAM132"),
    MINI_MAX(291, "com.elster.us.protocolimplv2.mercury.minimax.MiniMax"),
    A1860(293, "com.energyict.protocolimplv2.dlms.a1860.A1860"),
    CRYPTO_HS3300(294, "com.energyict.protocolimplv2.dlms.idis.hs3300.CryptoHS3300"),
    HONEYWELL_HS3300(295, "com.energyict.protocolimplv2.dlms.idis.hs3300.HS3300"),

    // Enexis non-crypto
    SAGEMCOM_T210(296, "com.energyict.protocolimplv2.nta.esmr50.sagemcom.T210", FamilyRule.DSMR_NTA),
    SAGEMCOM_XS210(297, "com.energyict.protocolimplv2.nta.esmr50.sagemcom.XS210", FamilyRule.DSMR_NTA),//not to be used; the above protocol SAGEMCOM_T210 also offers support for XS210
    LANDYSGYR_E350(298, "com.energyict.protocolimplv2.nta.dsmr40.landisgyr.E350"),
    IBM_KAIFA(299, "com.energyict.protocolimplv2.nta.dsmr40.ibm.Kaifa"),

    AM122 (300, "com.energyict.protocolimplv2.dlms.idis.am122.AM122"),

    //Enexis protocols
    ISKRA_MX382(301, "com.energyict.protocolimplv2.nta.dsmr23.Iskra.Mx382"),

    DSMR40_MBUS_PROTOCOL_V2(302, "com.energyict.protocolimplv2.nta.dsmr40.common.Dsmr40MbusProtocol"), //not to be sold or licensed to customers

    CRYPTO_ISKRA_MX382(303, "com.energyict.protocolimplv2.nta.dsmr23.iskra.CryptoMx382", FamilyRule.DSMR_NTA),
    CRYPTO_WEB_RTU_KP(304, "com.energyict.protocolimplv2.nta.dsmr23.eict.CryptoWebRTUKP", FamilyRule.EICT_NTA),

    CRYPTO_E350_DSMR40(305,  "com.energyict.protocolimplv2.nta.dsmr40.landisgyr.CryptoE350", FamilyRule.DSMR_NTA),
    CRYPTO_KAIFA_DSMR40(306, "com.energyict.protocolimplv2.nta.dsmr40.ibm.CryptoKaifa", FamilyRule.DSMR_NTA),

    CRYPTO_E350_DSMR42(307, "com.energyict.protocolimplv2.nta.dsmr42.landysgir.CryptoE350", FamilyRule.DSMR_NTA),
    CRYPTO_KAIFA_DSMR42(308, "com.energyict.protocolimplv2.nta.dsmr42.ibm.CryptoKaifa", FamilyRule.DSMR_NTA),

    CRYPTO_T210(309, "com.energyict.protocolimplv2.nta.esmr50.sagemcom.CryptoT210", FamilyRule.DSMR_NTA),


    CRYPTO_ISKRA_MX382_MBUS(310, "com.energyict.protocolimplv2.nta.dsmr23.iskra.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CRYPTO_WEB_RTU_KP_MBUS(311, "com.energyict.protocolimplv2.nta.dsmr23.eict.CryptoMbusDevice", FamilyRule.EICT_NTA),

    CRYPTO_LG_DSMR40_MBUS(312, "com.energyict.protocolimplv2.nta.dsmr40.landisgyr.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CRYPTO_ITRON_DMSR40_MBUS(313, "com.energyict.protocolimplv2.nta.dsmr40.itron.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CRYPTO_GENERIC_DSMR40_MBUS(314, "com.energyict.protocolimplv2.nta.dsmr40.common.CryptoMbusDevice", FamilyRule.EICT_NTA),//not to be sold or licensed to customers

    CRYPTO_LG_DSMR42_MBUS(315, "com.energyict.protocolimplv2.nta.dsmr42.landysgir.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CRYPTO_ITRON_DMSR42_MBUS(316, "com.energyict.protocolimplv2.nta.dsmr42.itron.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CRYPTO_ELSTER_DSR42_MBUS(317, "com.energyict.protocolimplv2.nta.dsmr42.elster.CryptoMbusDevice", FamilyRule.EICT_NTA),
    CRYPTO_GENERIC_DSMR42_MBUS(318, "com.energyict.protocolimplv2.nta.dsmr42.common.CryptoMbusDevice", FamilyRule.EICT_NTA),

    CRYPTO_ITRON_EMSR50_MBUS(319, "com.energyict.protocolimplv2.nta.esmr50.itron.CryptoMbusDevice", FamilyRule.DSMR_NTA),
    CRYPTO_ELSTER_EMSR50_MBUS(320, "com.energyict.protocolimplv2.nta.esmr50.elster.CryptoMbusDevice", FamilyRule.EICT_NTA),
    CRYPTO_GENERIC_ESMR50_MBUS(321, "com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50MbusDevice", FamilyRule.EICT_NTA),//not to be sold or licensed to customers

    HONEYWELL_AS300N(322, "com.energyict.protocolimplv2.dlms.hon.as300n.AS300N"),

    SAGEMCOM_T210_CATM(323, "com.energyict.protocolimplv2.nta.esmr50.sagemcom.T210CatM", FamilyRule.DSMR_NTA),
    CRYPTO_T210_CATM(324, "com.energyict.protocolimplv2.nta.esmr50.sagemcom.CryptoT210CatM", FamilyRule.DSMR_NTA),

    EI6_THEMISUNO(325, "com.energyict.protocolimplv2.dlms.a2.A2", FamilyRule.EICT_THEMISUNO),
    EI7_THEMISUNO(326, "com.energyict.protocolimplv2.dlms.ei7.EI7", FamilyRule.EICT_THEMISUNO),

    ACUD_GAS(327, "com.energyict.protocolimplv2.dlms.acud.AcudGas", FamilyRule.ACUD),
    ACUD_WATER(328, "com.energyict.protocolimplv2.dlms.acud.AcudWater", FamilyRule.ACUD),
    ACUD_ELECTRICITY(329, "com.energyict.protocolimplv2.dlms.acud.AcudElectricity", FamilyRule.ACUD),
    ACUD_Gateway(330, "com.energyict.protocolimplv2.dlms.acud.AcudGateway", FamilyRule.ACUD),

    ELSTER_AS253(331, "com.energyict.protocolimplv2.dlms.as253.AS253"),
    ELSTER_AS1253(332, "com.energyict.protocolimplv2.dlms.as253.AS1253"),

    WEBCATCH_DEVICE_PROTOCOL(333, "com.energyict.protocolimplv2.eict.webcatch.WebCatch", FamilyRule.EICT_RTU_EMS),
    ELSTER_AS3000(334, "com.energyict.protocolimplv2.dlms.as3000.AS3000"),
    SL7000(335, "com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000"),

    EI6_2021_THEMISUNO(336, "com.energyict.protocolimplv2.dlms.ei6v2021.EI6v2021", FamilyRule.EICT_THEMISUNO),

    // Deprecated
    FERRANTI(10001, "com.energyict.protocolimpl.iec1107.ferranti.Ferranti"),
    DLMS_Z3_MESSAGING(10002, "com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging"),
    INDIGO_PXAR(10003, "com.energyict.protocolimpl.iec1107.indigo.pxar.IndigoPXAR"),
    INDIGO_PLUS_SAMPLE(10005, "com.energyict.protocolimpl.sampleiec1107.indigo.IndigoPlus"),
    WAVE_FLOW_V2(10006, "com.energyict.protocolimplv2.coronis.waveflow.waveflowV2.WaveFlowV2"),
    ECHELON(10008, "com.energyict.rtuprotocol.Echelon"),
    EICT_Z3(10009, "com.energyict.protocolimpl.dlms.eictz3.EictZ3"),
    DLMSZMD(10010, "com.energyict.protocolimpl.dlms.DLMSZMD"),
    DLMSZMD_EXT(10011, "com.energyict.protocolimpl.dlms.DLMSZMD_EXT"),
    GATE_WAY_Z3(10012, "com.energyict.protocolimplv2.eict.gatewayz3.GateWayZ3", FamilyRule.EICT_Z3, FamilyRule.EICT_RTU_EMS),
    CORONIS_WAVECELL(10013, "com.energyict.genericprotocolimpl.coronis.wavecell.WaveCellGateway", FamilyRule.CORONIS),
    ELSTER_A1_THEMISPLUS_GENERIC(10014, "com.elster.genericprotocolimpl.dlms.a1.A1"),
    ELSTER_EK155(10015, "com.energyict.genericprotocolimpl.elster.ek155.EK155"),
    ELSTER_EK280(10016, "com.elster.genericprotocolimpl.dlms.ek280.EK280"),
    IDIS_RTUPLUSSERVER(10017, "com.energyict.genericprotocolimpl.rtuplusserver.idis.RtuPlusServer", FamilyRule.ELSTER_IDIS),
    MK10_STUB(10018, "com.energyict.genericprotocolimpl.edmi.mk10.executer.Mk10Stub", FamilyRule.EDMI, FamilyRule.TEST),

    //Not released, only technical class
    JEM10(20001, "com.energyict.protocolimpl.ametek.Jem10"),
    METCOM_3FAF(20002, "com.energyict.protocolimpl.metcom.Metcom3FAF"),
    METCOM_3FAG(20003, "com.energyict.protocolimpl.metcom.Metcom3FAG"),
    METCOM_3FBC(20004, "com.energyict.protocolimpl.metcom.Metcom3FBC"),
    METCOM_3FCL(20005, "com.energyict.protocolimpl.metcom.Metcom3FCL"),
    METCOM_2(20006, "com.energyict.protocolimpl.metcom.Metcom2"),
    METCOM_3(20007, "com.energyict.protocolimpl.metcom.Metcom3"),
    DSMR40_MBUS_PROTOCOL(20008, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.Dsmr40MbusProtocol"),
    DSMR40_PROTOCOL(20009, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.Dsmr40Protocol"),
    GENERIC_MODBUS_DISCOVER(20010, "com.energyict.protocolimpl.modbus.core.discover.GenericModbusDiscover"),
    DSMR40_MBUS_DUMMY(20011, "com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.MBusDevice", FamilyRule.DSMR_NTA),
    WEBRTUZ3_SLAVEMETER(20012, "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.SlaveMeter", FamilyRule.EICT_Z3),
    KAIFA_SMETS(20013, "com.energyict.smartmeterprotocolimpl.kaifa.Kaifa"),
    US_SEL(20014, "com.elster.us.protocolimplv2.sel.SEL");


    private int code;
    private String className;
    private Set<ProtocolFamily> families;

    LicensedProtocolRule(int code, String className, ProtocolFamily... families) {
        this.name();
        this.code = code;
        this.className = className;
        this.families = new HashSet<>(Arrays.asList(families));
    }

    /**
     * Returns the LicensedProtocolRule that is uniquely identified by the code.
     *
     * @param code The code
     * @return The LicensedProtocolRule or <code>null</code> if no LicensedProtocolRule is uniquely identified by the code
     */
    public static LicensedProtocol fromCode(int code) {
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
    public static LicensedProtocol fromClassName(String className) {
        for (LicensedProtocolRule protocol : values()) {
            if (className.equals(protocol.className)) {
                return protocol;
            }
        }
        return null;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Set<ProtocolFamily> getFamilies() {
        return this.families;
    }

    @Override
    public String getName() {
        return name();
    }
}
