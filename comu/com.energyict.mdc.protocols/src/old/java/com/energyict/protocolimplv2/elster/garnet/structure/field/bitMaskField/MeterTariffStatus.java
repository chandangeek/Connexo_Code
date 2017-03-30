/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class MeterTariffStatus extends AbstractBitMaskField<MeterTariffStatus> {

    public static final int LENGTH = 1; // The length expressed in nr of bits

    private BitSet tariffStatusBitMask;
    private int tariffStatusCode;
    private TariffStatus tariffStatus;

    public MeterTariffStatus() {
        this.tariffStatusBitMask = new BitSet(LENGTH);
        this.tariffStatus = TariffStatus.UNKNOWN;
    }

    public MeterTariffStatus(TariffStatus tariffStatus) {
        this.tariffStatus = tariffStatus;
    }

    public BitSet getBitMask() {
        return tariffStatusBitMask;
    }

    @Override
    public MeterTariffStatus parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        tariffStatusBitMask = bitSet.get(startPos, startPos + LENGTH);
        tariffStatusCode = convertBitSetToInt(tariffStatusBitMask);
        tariffStatus = TariffStatus.fromTariffCode(tariffStatusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getTariffStatusCode() {
        return tariffStatusCode;
    }

    public String getTariffStatusInfo() {
        if (!this.tariffStatus.equals(TariffStatus.UNKNOWN)) {
            return tariffStatus.getTariffInfo();
        } else {
            return (tariffStatus.getTariffInfo() + " " + tariffStatus);
        }
    }

    private enum TariffStatus {
        CONVENTIONAL(0, "Conventional / one tariff"),
        TOU(1, "Time of Use"),
        UNKNOWN(-1, "Unknown tariff");

        private final int tariffCode;
        private final String tariffInfo;

        private TariffStatus(int tariffCode, String tariffInfo) {
            this.tariffCode = tariffCode;
            this.tariffInfo = tariffInfo;
        }

        public String getTariffInfo() {
            return tariffInfo;
        }

        public int getTariffCode() {
            return tariffCode;
        }

        public static TariffStatus fromTariffCode(int tariffCode) {
            for (TariffStatus version : TariffStatus.values()) {
                if (version.getTariffCode() == tariffCode) {
                    return version;
                }
            }
            return TariffStatus.UNKNOWN;
        }
    }
}