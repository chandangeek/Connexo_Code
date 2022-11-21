/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory.mappings;

import com.energyict.mdc.protocol.inbound.mbus.factory.status.CellInfoFactory;
import com.energyict.obis.ObisCode;

import java.util.function.Function;

public enum CellInfoMapping {
    /* See Blue Book page 467 */
    CELL_ID                 ("0.0.96.12.7.255", CellInfoFactory::extractCellId ),
    SIGNAL_STRENGTH         ("0.0.96.12.5.255", CellInfoFactory::extractSignalStrength ),
    SIGNAL_QUALITY          ("0.0.96.12.6.255", CellInfoFactory::extractSignalQuality ),
    TRANSMISSION_POWER      ("0.0.96.12.8.255", CellInfoFactory::extractTransmissionPower ),
    EXTENDED_COVERAGE       ("0.0.96.12.9.255", CellInfoFactory::extractExtendedCodeCoverage ),
    ACCUMULATED_TX_TIME     ("0.0.96.12.10.255", CellInfoFactory::extractAccumulatedTxTime ),
    ACCUMULATED_RX_TIME     ("0.0.96.12.11.255", CellInfoFactory::extractAccumulatedRxTime ),
    RELEASE_ASSIST_ENABLED  ("0.0.96.12.12.255", CellInfoFactory::extractReleaseAssistEnable ),
    PAIRED_METER_ID         ("0.0.96.1.1.255", CellInfoFactory::extractPairedMeterId ),
    ;

    private final ObisCode obisCode;
    private final Function<byte[], Object> decodingFunction;

    CellInfoMapping(String obisCode, Function<byte[], Object> decodingFunction) {
        this.obisCode = ObisCode.fromString(obisCode);
        this.decodingFunction = decodingFunction;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public Object extractValue(byte[] data) {
        return decodingFunction.apply(data);
    }
}
