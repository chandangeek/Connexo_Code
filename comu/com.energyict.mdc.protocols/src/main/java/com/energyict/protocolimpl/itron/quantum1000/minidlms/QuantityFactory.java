/*
 * QuantityFactory.java
 *
 * Created on 13 december 2006, 16:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ObisCodeExtensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class QuantityFactory {

    static List<QuantityId> quantities = new ArrayList<>();

    static {
        quantities.add(new QuantityId(0,null,-1,-1,null));
        quantities.add(new QuantityId(1,"W delivered aggregate",1,1, Unit.get("W")));
        quantities.add(new QuantityId(2,"W delivered phase A",1,21,Unit.get("W")));
        quantities.add(new QuantityId(3,"W delivered phase B",1,41,Unit.get("W")));
        quantities.add(new QuantityId(4,"W delivered phase C",1,61,Unit.get("W")));
        quantities.add(new QuantityId(5,"W received aggregate",1,2,Unit.get("W")));
        quantities.add(new QuantityId(6,"W received phase A",1,22,Unit.get("W")));
        quantities.add(new QuantityId(7,"W received phase B",1,42,Unit.get("W")));
        quantities.add(new QuantityId(8,"W received phase C",1,62,Unit.get("W")));
        quantities.add(new QuantityId(9,"W net aggregate",1,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET,Unit.get("W")));
        quantities.add(new QuantityId(10,"W net phase A",1,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE1,Unit.get("W")));
        quantities.add(new QuantityId(11,"W net phase B",1,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE2,Unit.get("W")));
        quantities.add(new QuantityId(12,"W net phase C",1,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE3,Unit.get("W")));
        quantities.add(new QuantityId(13,"W delivered aggregate SLC",129,1,Unit.get("W")));
        quantities.add(new QuantityId(14,"W received aggregate SLC",129,2,Unit.get("W")));
        quantities.add(new QuantityId(15,"W net aggregateSLC",129,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET,Unit.get("W")));
        quantities.add(new QuantityId(16,"VAR delivered aggregate",1,3,Unit.get("var")));
        quantities.add(new QuantityId(17,"VAR delivered phase A",1,23,Unit.get("var")));
        quantities.add(new QuantityId(18,"VAR delivered phase B",1,43,Unit.get("var")));
        quantities.add(new QuantityId(19,"VAR delivered phase C",1,63,Unit.get("var")));
        quantities.add(new QuantityId(20,"VAR received aggregate",1,4,Unit.get("var")));
        quantities.add(new QuantityId(21,"VAR received phase A",1,24,Unit.get("var")));
        quantities.add(new QuantityId(22,"VAR received phase B",1,44,Unit.get("var")));
        quantities.add(new QuantityId(23,"VAR received phase C",1,64,Unit.get("var")));
        quantities.add(new QuantityId(24,"VAR quadrant 1 aggregate",1,5,Unit.get("var")));
        quantities.add(new QuantityId(25,"VAR quadrant 2 aggregate",1,6,Unit.get("var")));
        quantities.add(new QuantityId(26,"VAR quadrant 3 aggregate",1,7,Unit.get("var")));
        quantities.add(new QuantityId(27,"VAR quadrant 4 aggregate",1,8,Unit.get("var")));
        quantities.add(new QuantityId(28,"VAR quadrant 1 phase A",1,25,Unit.get("var")));
        quantities.add(new QuantityId(29,"VAR quadrant 2 phase A",1,26,Unit.get("var")));
        quantities.add(new QuantityId(30,"VAR quadrant 3 phase A",1,27,Unit.get("var")));
        quantities.add(new QuantityId(31,"VAR quadrant 4 phase A",1,28,Unit.get("var")));
        quantities.add(new QuantityId(32,"VAR quadrant 1 phase B",1,45,Unit.get("var")));
        quantities.add(new QuantityId(33,"VAR quadrant 2 phase B",1,46,Unit.get("var")));
        quantities.add(new QuantityId(34,"VAR quadrant 3 phase B",1,47,Unit.get("var")));
        quantities.add(new QuantityId(35,"VAR quadrant 4 phase B",1,48,Unit.get("var")));
        quantities.add(new QuantityId(36,"VAR quadrant 1 phase C",1,65,Unit.get("var")));
        quantities.add(new QuantityId(37,"VAR quadrant 2 phase C",1,66,Unit.get("var")));
        quantities.add(new QuantityId(38,"VAR quadrant 3 phase C",1,67,Unit.get("var")));
        quantities.add(new QuantityId(39,"VAR quadrant 4 phase C",1,68,Unit.get("var")));
        quantities.add(new QuantityId(40,"VAR net aggregate",1,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET,Unit.get("var")));
        quantities.add(new QuantityId(41,"VAR net phase A",1,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE1,Unit.get("var")));
        quantities.add(new QuantityId(42,"VAR net phase B",1,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE2,Unit.get("var")));
        quantities.add(new QuantityId(43,"VAR net phase C",1,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE3,Unit.get("var")));
        quantities.add(new QuantityId(44,"VAR delivered aggregate SLC",129,3,Unit.get("var")));
        quantities.add(new QuantityId(45,"VAR received aggregate SLC",129,4,Unit.get("var")));
        quantities.add(new QuantityId(46,"VAR quadrant 1 aggregate SLC",129,5,Unit.get("var")));
        quantities.add(new QuantityId(47,"VAR quadrant 2 aggregate SLC",129,6,Unit.get("var")));
        quantities.add(new QuantityId(48,"VAR quadrant 3 aggregate SLC",129,7,Unit.get("var")));
        quantities.add(new QuantityId(49,"VAR quadrant 4 aggregate SLC",129,8,Unit.get("var")));
        quantities.add(new QuantityId(50,"VAR net aggregate SLC",129,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET,Unit.get("var")));
        quantities.add(new QuantityId(51,"VA arithmetic delivered aggregate",148,9,Unit.get("VA")));
        quantities.add(new QuantityId(52,"VA arithmetic delivered phase A",148,29,Unit.get("VA")));
        quantities.add(new QuantityId(53,"VA arithmetic delivered phase B",148,49,Unit.get("VA")));
        quantities.add(new QuantityId(54,"VA arithmetic delivered phase C",148,69,Unit.get("VA")));
        quantities.add(new QuantityId(55,"VA arithmetic received aggregate",148,10,Unit.get("VA")));
        quantities.add(new QuantityId(56,"VA arithmetic received phase A",148,30,Unit.get("VA")));
        quantities.add(new QuantityId(57,"VA arithmetic received phase B",148,50,Unit.get("VA")));
        quantities.add(new QuantityId(58,"VA arithmetic received phase C",148,70,Unit.get("VA")));
        quantities.add(new QuantityId(59,"VA arithmetic total aggregate",148,200,Unit.get("VA")));
        quantities.add(new QuantityId(60,"VA arithmetic total phase A",148,201,Unit.get("VA")));
        quantities.add(new QuantityId(61,"VA arithmetic total phase B",148,202,Unit.get("VA")));
        quantities.add(new QuantityId(62,"VA arithmetic total phase C",148,203,Unit.get("VA")));
        quantities.add(new QuantityId(63,"VA vectorial delivered aggregate",1,9,Unit.get("VA")));
        quantities.add(new QuantityId(64,"VA vectorial delivered phase A",1,29,Unit.get("VA")));
        quantities.add(new QuantityId(65,"VA vectorial delivered phase B",1,49,Unit.get("VA")));
        quantities.add(new QuantityId(66,"VA vectorial delivered phase C",1,69,Unit.get("VA")));
        quantities.add(new QuantityId(67,"VA vectorial received aggregate",1,10,Unit.get("VA")));
        quantities.add(new QuantityId(68,"VA vectorial received phase A",1,30,Unit.get("VA")));
        quantities.add(new QuantityId(69,"VA vectorial received phase B",1,50,Unit.get("VA")));
        quantities.add(new QuantityId(70,"VA vectorial received phase C",1,70,Unit.get("VA")));
        quantities.add(new QuantityId(71,"VA vectorial total aggregate",1,204,Unit.get("VA")));
        quantities.add(new QuantityId(72,"VA vectorial total phase A",1,205,Unit.get("VA")));
        quantities.add(new QuantityId(73,"VA vectorial total phase B",1,206,Unit.get("VA")));
        quantities.add(new QuantityId(74,"VA vectorial total phase C",1,207,Unit.get("VA")));
        quantities.add(new QuantityId(75,"VA vectorial delivered",1,208,Unit.get("VA")));
        quantities.add(new QuantityId(76,"VA vectorial received",1,209,Unit.get("VA")));
        quantities.add(new QuantityId(77,"VA vectorial total aggregate",1,210,Unit.get("VA")));
        quantities.add(new QuantityId(78,"Q delivered aggregate",1,ObisCodeExtensions.OBISCODE_C_Q_DELIVERED,Unit.get("var")));
        quantities.add(new QuantityId(79,"Q received aggregate",1,ObisCodeExtensions.OBISCODE_C_Q_RECEIVED,Unit.get("var")));
        quantities.add(new QuantityId(80,"Q net aggregate SLC",129,211,Unit.get("var")));
        quantities.add(new QuantityId(81,"Q delivered aggregate SLC",129,ObisCodeExtensions.OBISCODE_C_Q_DELIVERED,Unit.get("var")));
        quantities.add(new QuantityId(82,"Q received aggregate SLC",129,ObisCodeExtensions.OBISCODE_C_Q_RECEIVED,Unit.get("var")));
        quantities.add(new QuantityId(83,"Q net aggregate SLC",129,212,Unit.get("var")));
        quantities.add(new QuantityId(84,"Volt average",1,12,Unit.get("V")));
        quantities.add(new QuantityId(85,"Volts phase A",1,32,Unit.get("V")));
        quantities.add(new QuantityId(86,"Volts phase B",1,52,Unit.get("V")));
        quantities.add(new QuantityId(87,"Volts phase C",1,72,Unit.get("V")));
        quantities.add(new QuantityId(88,"Amps average",1,11,Unit.get("A")));
        quantities.add(new QuantityId(89,"Amps phase A",1,31,Unit.get("A")));
        quantities.add(new QuantityId(90,"Amps phase B",1,51,Unit.get("A")));
        quantities.add(new QuantityId(91,"Amps phase C",1,71,Unit.get("A")));
        quantities.add(new QuantityId(92,"Amps neutral",1,91,Unit.get("A")));
        quantities.add(new QuantityId(93,"Analog input 0 net",1,213,Unit.get("")));
        quantities.add(new QuantityId(94,"Analog input 1 net",2,213,Unit.get("")));
        quantities.add(new QuantityId(95,"Analog input 2 net",3,213,Unit.get("")));
        quantities.add(new QuantityId(96,"Analog input 3 net",4,213,Unit.get("")));
        quantities.add(new QuantityId(97,"Analog input 4 net",5,213,Unit.get("")));
        quantities.add(new QuantityId(98,"Analog input 5 net",6,213,Unit.get("")));
        quantities.add(new QuantityId(99,"Analog input 6 net",7,213,Unit.get("")));
        quantities.add(new QuantityId(100,"Analog input 7 net",8,213,Unit.get("")));
        quantities.add(new QuantityId(101,"Digital Pulse input 0 net",1,82,Unit.get("")));
        quantities.add(new QuantityId(102,"Digital Pulse input 1 net",2,82,Unit.get("")));
        quantities.add(new QuantityId(103,"Digital Pulse input 2 net",3,82,Unit.get("")));
        quantities.add(new QuantityId(104,"Digital Pulse input 3 net",4,82,Unit.get("")));
        quantities.add(new QuantityId(105,"Digital Pulse input 4 net",5,82,Unit.get("")));
        quantities.add(new QuantityId(106,"Digital Pulse input 5 net",6,82,Unit.get("")));
        quantities.add(new QuantityId(107,"Digital Pulse input 6 net",7,82,Unit.get("")));
        quantities.add(new QuantityId(108,"Digital Pulse input 7 net",8,82,Unit.get("")));
        quantities.add(new QuantityId(109,"Power factor phase A",1,33,Unit.get("")));
        quantities.add(new QuantityId(110,"Power factor phase B",1,53,Unit.get("")));
        quantities.add(new QuantityId(111,"Power factor phase C",1,73,Unit.get("")));
        quantities.add(new QuantityId(112,"Summed register 0 net",1,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(113,"Summed register 1 net",2,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(114,"Summed register 2 net",3,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(115,"Summed register 3 net",4,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(116,"Summed register 4 net",5,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(117,"Summed register 5 net",6,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(118,"Summed register 6 net",7,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(119,"Summed register 7 net",8,ObisCodeExtensions.OBISCODE_C_TOTALIZER,Unit.get("")));
        quantities.add(new QuantityId(120,"VA arithmetic quadrant 1 aggregate",148,214,Unit.get("VA")));
        quantities.add(new QuantityId(121,"VA arithmetic quadrant 2 aggregate",148,215,Unit.get("VA")));
        quantities.add(new QuantityId(122,"VA arithmetic quadrant 3 aggregate",148,216,Unit.get("VA")));
        quantities.add(new QuantityId(123,"VA arithmetic quadrant 4 aggregate",148,217,Unit.get("VA")));
        quantities.add(new QuantityId(124,"VA arithmetic quadrant 1 phase A",148,218,Unit.get("VA")));
        quantities.add(new QuantityId(125,"VA arithmetic quadrant 2 phase A",148,219,Unit.get("VA")));
        quantities.add(new QuantityId(126,"VA arithmetic quadrant 3 phase A",148,220,Unit.get("VA")));
        quantities.add(new QuantityId(127,"VA arithmetic quadrant 4 phase A",148,221,Unit.get("VA")));
        quantities.add(new QuantityId(128,"VA arithmetic quadrant 1 phase B",148,222,Unit.get("VA")));
        quantities.add(new QuantityId(129,"VA arithmetic quadrant 2 phase B",148,223,Unit.get("VA")));
        quantities.add(new QuantityId(130,"VA arithmetic quadrant 3 phase B",148,224,Unit.get("VA")));
        quantities.add(new QuantityId(131,"VA arithmetic quadrant 4 phase B",148,225,Unit.get("VA")));
        quantities.add(new QuantityId(132,"VA arithmetic quadrant 1 phase C",148,226,Unit.get("VA")));
        quantities.add(new QuantityId(133,"VA arithmetic quadrant 2 phase C",148,227,Unit.get("VA")));
        quantities.add(new QuantityId(134,"VA arithmetic quadrant 3 phase C",148,228,Unit.get("VA")));
        quantities.add(new QuantityId(135,"VA arithmetic quadrant 4 phase C",148,229,Unit.get("VA")));
        quantities.add(new QuantityId(136,"VA vectorial quadrant 1 aggregate",1,214,Unit.get("VA")));
        quantities.add(new QuantityId(137,"VA vectorial quadrant 2 aggregate",1,215,Unit.get("VA")));
        quantities.add(new QuantityId(138,"VA vectorial quadrant 3 aggregate",1,216,Unit.get("VA")));
        quantities.add(new QuantityId(139,"VA vectorial quadrant 4 aggregate",1,217,Unit.get("VA")));
        quantities.add(new QuantityId(140,"VA vectorial quadrant 1 phase A",1,218,Unit.get("VA")));
        quantities.add(new QuantityId(141,"VA vectorial quadrant 2 phase A",1,219,Unit.get("VA")));
        quantities.add(new QuantityId(142,"VA vectorial quadrant 3 phase A",1,220,Unit.get("VA")));
        quantities.add(new QuantityId(143,"VA vectorial quadrant 4 phase A",1,221,Unit.get("VA")));
        quantities.add(new QuantityId(144,"VA vectorial quadrant 1 phase B",1,222,Unit.get("VA")));
        quantities.add(new QuantityId(145,"VA vectorial quadrant 2 phase B",1,223,Unit.get("VA")));
        quantities.add(new QuantityId(146,"VA vectorial quadrant 3 phase B",1,224,Unit.get("VA")));
        quantities.add(new QuantityId(147,"VA vectorial quadrant 4 phase B",1,225,Unit.get("VA")));
        quantities.add(new QuantityId(148,"VA vectorial quadrant 1 phase C",1,226,Unit.get("VA")));
        quantities.add(new QuantityId(149,"VA vectorial quadrant 2 phase C",1,227,Unit.get("VA")));
        quantities.add(new QuantityId(150,"VA vectorial quadrant 3 phase C",1,228,Unit.get("VA")));
        quantities.add(new QuantityId(151,"VA vectorial quadrant 4 phase C",1,229,Unit.get("VA")));
        quantities.add(new QuantityId(152,"Power factor aggregate",1,13,Unit.get("")));
        quantities.add(new QuantityId(153,"Power factor (arithmetic va) aggregate",148,230,Unit.get("")));
        quantities.add(new QuantityId(154,"Power factor (arithmetic va) phase A",148,231,Unit.get("")));
        quantities.add(new QuantityId(155,"Power factor (arithmetic va) phase B",148,232,Unit.get("")));
        quantities.add(new QuantityId(156,"Power factor (arithmetic va) phase C",148,233,Unit.get("")));
        quantities.add(new QuantityId(157,"Power factor (vectorial va) aggregate",1,230,Unit.get("")));
        quantities.add(new QuantityId(158,"Power factor (vectorial va) phase A",1,231,Unit.get("")));
        quantities.add(new QuantityId(159,"Power factor (vectorial va) phase B",1,232,Unit.get("")));
        quantities.add(new QuantityId(160,"Power factor (vectorial va) phase C",1,233,Unit.get("")));
        quantities.add(new QuantityId(161,"Watts for power factor aggregate",128,1,Unit.get("W")));
        quantities.add(new QuantityId(162,"Watts for power factor phase A",128,21,Unit.get("W")));
        quantities.add(new QuantityId(163,"Watts for power factor phase B",128,41,Unit.get("W")));
        quantities.add(new QuantityId(164,"Watts for power factor phase C",128,61,Unit.get("W")));
        quantities.add(new QuantityId(165,"VA for power factor aggregate",128,204,Unit.get("VA")));
        quantities.add(new QuantityId(166,"VA for power factor phase A",128,205,Unit.get("VA")));
        quantities.add(new QuantityId(167,"VA for power factor phase B",128,206,Unit.get("VA")));
        quantities.add(new QuantityId(168,"VA for power factor phase C",128,207,Unit.get("VA")));
        quantities.add(new QuantityId(169,"Percent THD volts phase A US",149,32,Unit.get("%")));
        quantities.add(new QuantityId(170,"Percent THD amps phase A US",149,31,Unit.get("%")));
        quantities.add(new QuantityId(171,"Percent THD volts phase A Europe",150,32,Unit.get("%")));
        quantities.add(new QuantityId(172,"Percent THD amps phase A Europe",150,31,Unit.get("%")));
        quantities.add(new QuantityId(173,"Percent THD volts phase B US",149,52,Unit.get("%")));
        quantities.add(new QuantityId(174,"Percent THD amps phase B US",149,51,Unit.get("%")));
        quantities.add(new QuantityId(175,"Percent THD volts phase B Europe",150,52,Unit.get("%")));
        quantities.add(new QuantityId(176,"Percent THD amps phase B Europe",150,51,Unit.get("%")));
        quantities.add(new QuantityId(177,"Percent THD volts phase C US",149,72,Unit.get("%")));
        quantities.add(new QuantityId(178,"Percent THD amps phase C US",149,71,Unit.get("%")));
        quantities.add(new QuantityId(179,"Percent THD volts phase C Europe",150,72,Unit.get("%")));
        quantities.add(new QuantityId(180,"Percent THD amps phase C Europe",150,71,Unit.get("%")));
        quantities.add(new QuantityId(181,"Watts Fundamental Delivered Aggregate",151,1,Unit.get("W")));
        quantities.add(new QuantityId(182,"Watts Fundamental Delivered Phase A",151,21,Unit.get("W")));
        quantities.add(new QuantityId(183,"Watts Fundamental Delivered Phase B",151,41,Unit.get("W")));
        quantities.add(new QuantityId(184,"Watts Fundamental Delivered Phase C",151,61,Unit.get("W")));
        quantities.add(new QuantityId(185,"Watts Fundamental Received Aggregate",151,2,Unit.get("W")));
        quantities.add(new QuantityId(186,"Watts Fundamental Received Phase A",151,22,Unit.get("W")));
        quantities.add(new QuantityId(187,"Watts Fundamental Received Phase B",151,42,Unit.get("W")));
        quantities.add(new QuantityId(188,"Watts Fundamental Received Phase C",151,62,Unit.get("W")));
        quantities.add(new QuantityId(189,"Watts Fundamental Net Aggregate",151,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET,Unit.get("W")));
        quantities.add(new QuantityId(190,"Watts Fundamental Net Phase A",151,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE1,Unit.get("W")));
        quantities.add(new QuantityId(191,"Watts Fundamental Net Phase B",151,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE2,Unit.get("W")));
        quantities.add(new QuantityId(192,"Watts Fundamental Net Phase C",151,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE3,Unit.get("W")));
        quantities.add(new QuantityId(193,"VAR Fundamental Delivered Aggregate",151,3,Unit.get("var")));
        quantities.add(new QuantityId(194,"VAR Fundamental Delivered Phase A",151,23,Unit.get("var")));
        quantities.add(new QuantityId(195,"VAR Fundamental Delivered Phase B",151,43,Unit.get("var")));
        quantities.add(new QuantityId(196,"VAR Fundamental Delivered Phase C",151,63,Unit.get("var")));
        quantities.add(new QuantityId(197,"VAR Fundamental Received Aggregate",151,4,Unit.get("var")));
        quantities.add(new QuantityId(198,"VAR Fundamental Received Phase A",151,24,Unit.get("var")));
        quantities.add(new QuantityId(199,"VAR Fundamental Received Phase B",151,44,Unit.get("var")));
        quantities.add(new QuantityId(200,"VAR Fundamental Received Phase C",151,64,Unit.get("var")));
        quantities.add(new QuantityId(201,"VAR Fundamental Net Aggregate",151,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET,Unit.get("var")));
        quantities.add(new QuantityId(202,"VAR Fundamental Net Phase A",151,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE1,Unit.get("var")));
        quantities.add(new QuantityId(203,"VAR Fundamental Net Phase B",151,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE2,Unit.get("var")));
        quantities.add(new QuantityId(204,"VAR Fundamental Net Phase C",151,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE3,Unit.get("var")));
        quantities.add(new QuantityId(205,"VAR Fundamental Quadrant 1 Aggregate",151,5,Unit.get("var")));
        quantities.add(new QuantityId(206,"VAR Fundamental Quadrant 1 Phase A",151,25,Unit.get("var")));
        quantities.add(new QuantityId(207,"VAR Fundamental Quadrant 1 Phase B",151,45,Unit.get("var")));
        quantities.add(new QuantityId(208,"VAR Fundamental Quadrant 1 Phase C",151,65,Unit.get("var")));
        quantities.add(new QuantityId(209,"VAR Fundamental Quadrant 2 Aggregate",151,6,Unit.get("var")));
        quantities.add(new QuantityId(210,"VAR Fundamental Quadrant 2 Phase A",151,26,Unit.get("var")));
        quantities.add(new QuantityId(211,"VAR Fundamental Quadrant 2 Phase B",151,46,Unit.get("var")));
        quantities.add(new QuantityId(212,"VAR Fundamental Quadrant 2 Phase C",151,66,Unit.get("var")));
        quantities.add(new QuantityId(213,"VAR Fundamental Quadrant 3 Aggregate",151,7,Unit.get("var")));
        quantities.add(new QuantityId(214,"VAR Fundamental Quadrant 3 Phase A",151,27,Unit.get("var")));
        quantities.add(new QuantityId(215,"VAR Fundamental Quadrant 3 Phase B",151,47,Unit.get("var")));
        quantities.add(new QuantityId(216,"VAR Fundamental Quadrant 3 Phase C",151,67,Unit.get("var")));
        quantities.add(new QuantityId(217,"VAR Fundamental Quadrant 4 Aggregate",151,8,Unit.get("var")));
        quantities.add(new QuantityId(218,"VAR Fundamental Quadrant 4 Phase A",151,28,Unit.get("var")));
        quantities.add(new QuantityId(219,"VAR Fundamental Quadrant 4 Phase B",151,48,Unit.get("var")));
        quantities.add(new QuantityId(220,"VAR Fundamental Quadrant 4 Phase C",151,68,Unit.get("var")));
        quantities.add(new QuantityId(221,"VA arithmetic unbalance total aggregate",1,234,Unit.get("VA")));
        quantities.add(new QuantityId(222,"VA arithmetic distortion total aggregate",1,235,Unit.get("VA")));
        quantities.add(new QuantityId(223,"VA vectorial Quadrant 1 Aggregate SLC",129,214,Unit.get("VA")));
        quantities.add(new QuantityId(224,"VA vectorial Quadrant 2 Aggregate SLC",129,215,Unit.get("VA")));
        quantities.add(new QuantityId(225,"VA vectorial Quadrant 3 Aggregate SLC",129,216,Unit.get("VA")));
        quantities.add(new QuantityId(226,"VA vectorial Quadrant 4 Aggregate SLC",129,217,Unit.get("VA")));
        quantities.add(new QuantityId(227,"Power factor (vectorial va) aggregate SLC",129,230,Unit.get("")));
        quantities.add(new QuantityId(228,"Watts Fundamental Delivered Aggregate SLC",152,1,Unit.get("W")));
        quantities.add(new QuantityId(229,"Watts Fundamental Received Aggregate SLC",152,2,Unit.get("W")));
        quantities.add(new QuantityId(230,"Watts Fundamental Net Aggregate SLC",152,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET,Unit.get("W")));
        quantities.add(new QuantityId(231,"VAR Fundamental Delivered Aggregate SLC",152,3,Unit.get("var")));
        quantities.add(new QuantityId(232,"VAR Fundamental Received Aggregate SLC",152,4,Unit.get("var")));
        quantities.add(new QuantityId(233,"VAR Fundamental Net Aggregate SLC",152,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET,Unit.get("var")));
        quantities.add(new QuantityId(234,"VAR Fundamental Quadrant 1 Aggregate SLC",152,5,Unit.get("var")));
        quantities.add(new QuantityId(235,"VAR Fundamental Quadrant 2 Aggregate SLC",152,6,Unit.get("var")));
        quantities.add(new QuantityId(236,"VAR Fundamental Quadrant 3 Aggregate SLC",152,7,Unit.get("var")));
        quantities.add(new QuantityId(237,"VAR Fundamental Quadrant 4 Aggregate SLC",152,8,Unit.get("var")));
        quantities.add(new QuantityId(238,"Frequency",1,14,Unit.get("Hz")));
        quantities.add(new QuantityId(239,"Watts Transformer Loss Aggregate",130,1,Unit.get("W")));
        quantities.add(new QuantityId(240,"Watts Transformer Loss Phase A",130,21,Unit.get("W")));
        quantities.add(new QuantityId(241,"Watts Transformer Loss Phase B",130,41,Unit.get("W")));
        quantities.add(new QuantityId(242,"Watts Transformer Loss Phase C",130,61,Unit.get("W")));
        quantities.add(new QuantityId(243,"Vars Transformer Loss Aggregate",130,3,Unit.get("var")));
        quantities.add(new QuantityId(244,"Vars Transformer Loss Phase A",130,23,Unit.get("var")));
        quantities.add(new QuantityId(245,"Vars Transformer Loss Phase B",130,43,Unit.get("var")));
        quantities.add(new QuantityId(246,"Vars Transformer Loss Phase C",130,63,Unit.get("var")));
        quantities.add(new QuantityId(247,"Watts Line Loss Aggregate",131,1,Unit.get("W")));
        quantities.add(new QuantityId(248,"Watts Line Loss Phase A",131,21,Unit.get("W")));
        quantities.add(new QuantityId(249,"Watts Line Loss Phase B",131,41,Unit.get("W")));
        quantities.add(new QuantityId(250,"Watts Line Loss Phase C",131,61,Unit.get("W")));
        quantities.add(new QuantityId(251,"Vars Line Loss Aggregate",131,3,Unit.get("var")));
        quantities.add(new QuantityId(252,"Vars Line Loss Phase A",131,23,Unit.get("var")));
        quantities.add(new QuantityId(253,"Vars Line Loss Phase B",131,43,Unit.get("var")));
        quantities.add(new QuantityId(254,"Vars Line Loss Phase C",131,63,Unit.get("var")));
        quantities.add(new QuantityId(255,"Watts Net Phase A SLC",129,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE1,Unit.get("W")));
        quantities.add(new QuantityId(256,"Watts Net Phase B SLC",129,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE2,Unit.get("W")));
        quantities.add(new QuantityId(257,"Watts Net Phase C SLC",129,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE3,Unit.get("W")));
        quantities.add(new QuantityId(258,"VAR Net Phase A SLC",129,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE1,Unit.get("var")));
        quantities.add(new QuantityId(259,"VAR Net Phase B SLC",129,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE2,Unit.get("var")));
        quantities.add(new QuantityId(260,"VAR Net Phase C SLC",129,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE3,Unit.get("var")));
        quantities.add(new QuantityId(261,"Watts Fundamental Net Phase A SLC",152,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE1,Unit.get("W")));
        quantities.add(new QuantityId(262,"Watts Fundamental Net Phase B SLC",152,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE2,Unit.get("W")));
        quantities.add(new QuantityId(263,"Watts Fundamental Net Phase C SLC",152,ObisCodeExtensions.OBISCODE_C_ACTIVE_NET_PHASE3,Unit.get("W")));
        quantities.add(new QuantityId(264,"VAR Fundamental Net Phase A SLC",152,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE1,Unit.get("var")));
        quantities.add(new QuantityId(265,"VAR Fundamental Net Phase B SLC",152,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE2,Unit.get("var")));
        quantities.add(new QuantityId(266,"VAR Fundamental Net Phase C SLC",152,ObisCodeExtensions.OBISCODE_C_REACTIVE_NET_PHASE3,Unit.get("var")));
        quantities.add(new QuantityId(267,"Volt squared Phase A SLC",129,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE_PHASE1,Unit.get("V2")));
        quantities.add(new QuantityId(268,"Volt squared Phase B SLC",129,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE_PHASE2,Unit.get("V2")));
        quantities.add(new QuantityId(269,"Volt squared Phase C SLC",129,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE_PHASE3,Unit.get("V2")));
        quantities.add(new QuantityId(270,"Amp squared Phase A SLC",129,ObisCodeExtensions.OBISCODE_C_AMPSQUARE_PHASE1,Unit.get("A2")));
        quantities.add(new QuantityId(271,"Amp squared Phase B SLC",129,ObisCodeExtensions.OBISCODE_C_AMPSQUARE_PHASE2,Unit.get("A2")));
        quantities.add(new QuantityId(272,"Amp squared Phase C SLC",129,ObisCodeExtensions.OBISCODE_C_AMPSQUARE_PHASE3,Unit.get("A2")));
        quantities.add(new QuantityId(273,"Meter Input 1 quantity 1",141,244,Unit.get("")));
        quantities.add(new QuantityId(274,"Meter Input 1 quantity 2",142,244,Unit.get("")));
        quantities.add(new QuantityId(275,"Meter Input 1 quantity 3",143,244,Unit.get("")));
        quantities.add(new QuantityId(276,"Meter Input 1 quantity 4",144,244,Unit.get("")));
        quantities.add(new QuantityId(277,"Meter Input 1 quantity 5",145,244,Unit.get("")));
        quantities.add(new QuantityId(278,"Meter Input 1 quantity 6",146,244,Unit.get("")));
        quantities.add(new QuantityId(279,"Meter Input 2 quantity 1",141,245,Unit.get("")));
        quantities.add(new QuantityId(280,"Meter Input 2 quantity 2",142,245,Unit.get("")));
        quantities.add(new QuantityId(281,"Meter Input 2 quantity 3",143,245,Unit.get("")));
        quantities.add(new QuantityId(282,"Meter Input 2 quantity 4",144,245,Unit.get("")));
        quantities.add(new QuantityId(283,"Meter Input 2 quantity 5",145,245,Unit.get("")));
        quantities.add(new QuantityId(284,"Meter Input 2 quantity 6",146,245,Unit.get("")));
        quantities.add(new QuantityId(285,"Meter Input 3 quantity 1",141,246,Unit.get("")));
        quantities.add(new QuantityId(286,"Meter Input 3 quantity 2",142,246,Unit.get("")));
        quantities.add(new QuantityId(287,"Meter Input 3 quantity 3",143,246,Unit.get("")));
        quantities.add(new QuantityId(288,"Meter Input 3 quantity 4",144,246,Unit.get("")));
        quantities.add(new QuantityId(289,"Meter Input 3 quantity 5",145,246,Unit.get("")));
        quantities.add(new QuantityId(290,"Meter Input 3 quantity 6",146,246,Unit.get("")));
        quantities.add(new QuantityId(291,"Meter Input 4 quantity 1",141,247,Unit.get("")));
        quantities.add(new QuantityId(292,"Meter Input 4 quantity 2",142,247,Unit.get("")));
        quantities.add(new QuantityId(293,"Meter Input 4 quantity 3",143,247,Unit.get("")));
        quantities.add(new QuantityId(294,"Meter Input 4 quantity 4",144,247,Unit.get("")));
        quantities.add(new QuantityId(295,"Meter Input 4 quantity 5",145,247,Unit.get("")));
        quantities.add(new QuantityId(296,"Meter Input 4 quantity 6",146,247,Unit.get("")));
        quantities.add(new QuantityId(297,"Meter Input 5 quantity 1",141,248,Unit.get("")));
        quantities.add(new QuantityId(298,"Meter Input 5 quantity 2",142,248,Unit.get("")));
        quantities.add(new QuantityId(299,"Meter Input 5 quantity 3",143,248,Unit.get("")));
        quantities.add(new QuantityId(300,"Meter Input 5 quantity 4",144,248,Unit.get("")));
        quantities.add(new QuantityId(301,"Meter Input 5 quantity 5",145,248,Unit.get("")));
        quantities.add(new QuantityId(302,"Meter Input 5 quantity 6",146,248,Unit.get("")));
        quantities.add(new QuantityId(303,"Meter Input 6 quantity 1",141,249,Unit.get("")));
        quantities.add(new QuantityId(304,"Meter Input 6 quantity 2",142,249,Unit.get("")));
        quantities.add(new QuantityId(305,"Meter Input 6 quantity 3",143,249,Unit.get("")));
        quantities.add(new QuantityId(306,"Meter Input 6 quantity 4",144,249,Unit.get("")));
        quantities.add(new QuantityId(307,"Meter Input 6 quantity 5",145,249,Unit.get("")));
        quantities.add(new QuantityId(308,"Meter Input 6 quantity 6",146,249,Unit.get("")));
        quantities.add(new QuantityId(309,"Volt Squared Aggregate",1,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE,Unit.get("V2")));
        quantities.add(new QuantityId(310,"Volt squared Phase A",1,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE_PHASE1,Unit.get("V2")));
        quantities.add(new QuantityId(311,"Volt squared Phase B",1,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE_PHASE2,Unit.get("V2")));
        quantities.add(new QuantityId(312,"Volt squared Phase C",1,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE_PHASE3,Unit.get("V2")));
        quantities.add(new QuantityId(313,"Amp squared Aggregate",1,ObisCodeExtensions.OBISCODE_C_AMPSQUARE,Unit.get("A2")));
        quantities.add(new QuantityId(314,"Amp squared Phase A",1,ObisCodeExtensions.OBISCODE_C_AMPSQUARE_PHASE1,Unit.get("A2")));
        quantities.add(new QuantityId(315,"Amp squared Phase B",1,ObisCodeExtensions.OBISCODE_C_AMPSQUARE_PHASE2,Unit.get("A2")));
        quantities.add(new QuantityId(316,"Amp squared Phase C",1,ObisCodeExtensions.OBISCODE_C_AMPSQUARE_PHASE3,Unit.get("A2")));
        quantities.add(new QuantityId(317,"Volt line-line A-B",147,32,Unit.get("V")));
        quantities.add(new QuantityId(318,"Volt line-line B-C",147,52,Unit.get("V")));
        quantities.add(new QuantityId(319,"Volt line-line C-A",147,72,Unit.get("V")));
        quantities.add(new QuantityId(320,"Volt line-line Average",147,12,Unit.get("V")));
        quantities.add(new QuantityId(321,"Volt fundamental Phase A",153,32,Unit.get("V")));
        quantities.add(new QuantityId(322,"Volt fundamental Phase B",153,52,Unit.get("V")));
        quantities.add(new QuantityId(323,"Volt fundamental Phase C",153,72,Unit.get("V")));
        quantities.add(new QuantityId(324,"Volt fundamental Average",153,12,Unit.get("V")));
        quantities.add(new QuantityId(325,"Volt line-neutral Phase A",1,236,Unit.get("V")));
        quantities.add(new QuantityId(326,"Volt line-neutral Phase B",1,237,Unit.get("V")));
        quantities.add(new QuantityId(327,"Volt line-neutral Phase C",1,238,Unit.get("V")));
        quantities.add(new QuantityId(328,"Volt line-neutral Average",1,92,Unit.get("V")));
        quantities.add(new QuantityId(329,"Volt fundamental line-line A-B",154,32,Unit.get("V")));
        quantities.add(new QuantityId(330,"Volt fundamental line-line B-C",154,52,Unit.get("V")));
        quantities.add(new QuantityId(331,"Volt fundamental line-line C-A",154,72,Unit.get("V")));
        quantities.add(new QuantityId(332,"Volt fundamental average",154,239,Unit.get("V")));
        quantities.add(new QuantityId(333,"Volt fundamental line-neutral Phase A",153,236,Unit.get("V")));
        quantities.add(new QuantityId(334,"Volt fundamental line-neutral Phase B",153,237,Unit.get("V")));
        quantities.add(new QuantityId(335,"Volt fundamental line-neutral Phase C",153,238,Unit.get("V")));
        quantities.add(new QuantityId(336,"Volt fundamental line-neutral Average",153,92,Unit.get("V")));
        quantities.add(new QuantityId(337,"Displacement PF Phase A PF",1,240,Unit.get("")));
        quantities.add(new QuantityId(338,"Displacement Phase B PF",1,241,Unit.get("")));
        quantities.add(new QuantityId(339,"Displacement Phase C PF",1,242,Unit.get("")));
        quantities.add(new QuantityId(340,"Displacement Average",1,243,Unit.get("")));

    }

    /** Creates a new instance of QuantityFactory */
    private QuantityFactory() {
    }

    public static QuantityId findQuantityId(int id) throws IOException {
        Iterator<QuantityId> it = quantities.iterator();

        while(it.hasNext()) {
            QuantityId qid = it.next();
            if (qid.getId() == id) {
                return qid;
            }
        }

        throw new IOException("QuantityFactory, findQuantityId, invalid id="+id);

    }

}