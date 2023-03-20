/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.cbo.Unit;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.MeasureUnit;

public class UnitFactory {

    public static Unit findConnexoUnitFor(String unitName, int unitScale) {
        return Unit.get(unitName, unitScale);
    }

    public static Unit from(TelegramVariableDataRecord record, InboundContext inboundContext) {
        if (MeasureUnit.NONE.equals(record.getVif().getMeasureUnit())){
            return Unit.getUndefined();
        }

        String unitName = record.getVif().getMeasureUnit().getValue();
        int unitScale = record.getVif().getMultiplier();

        try {
            return UnitFactory.findConnexoUnitFor(unitName, unitScale);
        } catch (Exception ex){
            inboundContext.getLogger().info("Cannot get a Connexo unit for " + unitName + " with scale " + unitScale);
        }

        return Unit.getUndefined();
    }
}
