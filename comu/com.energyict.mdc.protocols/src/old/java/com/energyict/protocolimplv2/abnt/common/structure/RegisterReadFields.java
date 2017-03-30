/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.field.parser.BcdEncodedFieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.FieldParser;

/**
 * @author sva
 * @since 11/09/2014 - 15:27
 */
public enum RegisterReadFields {

    ch1GeneralTotal(0, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch1PeakTariffTotal(1, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    uferPeakTariff(2, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch1OffPeakTariffTotal(3, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    uferOffPeakTariff(4, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch1ReservedTariffTotal(5, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    uferReservedTariff(6, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch1DemandOfLastInterval(7, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1PeakTariffMaximumDemand(8, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrPeakTariff(9, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1OffPeakTariffMaximumDemand(10, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrOffPeakTariff(11, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1ReservedTariffMaximumDemand(12, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrReservedTariff(13, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1PeakTariffAccumulatedDemand(14, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrAccumulatedPeakTariff(15, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1OffPeakTariffAccumulatedDemand(16, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrAccumulatedOffPeakTariff(17, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1ReservedTariffAccumulatedDemand(18, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrAccumulatedReservedTariff(19, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2GeneralTotal(20, ChannelGroup.GROUP_2_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch2PeakTariffTotal(21, ChannelGroup.GROUP_2_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch2PeakTariffReverseTotal(22, ChannelGroup.GROUP_2_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch2OffPeakTariffTotal(23, ChannelGroup.GROUP_2_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch2OffPeakTariffReverseTotal(24, ChannelGroup.GROUP_2_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch2ReservedTariffTotal(25, ChannelGroup.GROUP_2_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch2ReservedTariffReverseTotal(26, ChannelGroup.GROUP_2_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch2DemandOfLastInterval(27, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2PeakTariffMaximumDemand(28, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2PeakTariffReverseMaximumDemand(29, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2OffPeakTariffMaximumDemand(30, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2OffPeakTariffReverseMaximumDemand(31, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2ReservedTariffMaximumDemand(32, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2reservedTariffReverseMaximumDemand(33, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2PeakTariffAccumulatedDemand(34, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2PeakTariffReverseAccumulatedDemand(35, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2OffPeakTariffAccumulatedDemand(36, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2OffPeakTariffReverseAccumulatedDemand(37, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2ReservedTariffAccumulatedDemand(38, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch2reservedTariffReverseAccumulatedDemand(39, ChannelGroup.GROUP_2_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3GeneralTotal(40, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch3PeakTariffTotal(41, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch3PeakTariffReverseTotal(42, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch3OffPeakTariffTotal(43, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch3OffPeakTariffReverseTotal(44, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch3ReservedTariffTotal(45, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch3ReservedTariffReverseTotal(46, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch3DemandOfLastInterval(47, ChannelGroup.GROUP_3_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3PeakTariffMaximumDemand(48, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3PeakTariffReverseMaximumDemand(49, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3OffPeakTariffMaximumDemand(50, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3OffPeakTariffReverseMaximumDemand(51, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3ReservedTariffMaximumDemand(52, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3reservedTariffReverseMaximumDemand(53, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3PeakTariffAccumulatedDemand(54, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3PeakTariffReverseAccumulatedDemand(55, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3OffPeakTariffAccumulatedDemand(56, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3OffPeakTariffReverseAccumulatedDemand(57, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3ReservedTariffAccumulatedDemand(58, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch3reservedTariffReverseAccumulatedDemand(59, ChannelGroup.GROUP_3_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1FourthTariffTotal(60, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    uferFourthTariff(61, ChannelGroup.GROUP_1_ENERGY, new BcdEncodedFieldParser(RegisterReadResponse.ENERGY_LENGTH)),
    ch1FourthTariffMaximumDemand(62, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrFourthTariff(63, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    ch1FourthTariffAccumulatedDemand(64, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH)),
    dmcrAccumulatedFourthTariff(65, ChannelGroup.GROUP_1_DEMAND, new BcdEncodedFieldParser(RegisterReadResponse.DEMAND_LENGTH));

    private final int code;
    private final ChannelGroup channelGroup;
    private final FieldParser fieldParser;

    RegisterReadFields(int code, ChannelGroup channelGroup, FieldParser fieldParser) {
        this.code = code;
        this.channelGroup = channelGroup;
        this.fieldParser = fieldParser;
    }

    public int getCode() {
        return code;
    }

    public FieldParser getFieldParser() {
        return fieldParser;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public static RegisterReadFields fromCode(int code) {
        for (RegisterReadFields registerReadFields : values()) {
            if (registerReadFields.getCode() == code) {
                return registerReadFields;
            }
        }
        return null;
    }
}