package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.cbo.Unit;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Measure_Unit;

public class UnitFactory {

    public static Unit findConnexoUnitFor(String unitName, int unitScale) {
        Unit unit = Unit.get(unitName, unitScale);
        return unit;
    }

    public static Unit from(TelegramVariableDataRecord record, InboundContext inboundContext) {
        if (Measure_Unit.NONE.equals(record.getVif().getmUnit())){
            return Unit.getUndefined();
        }

        String unitName = record.getVif().getmUnit().getValue();
        int unitScale = record.getVif().getMultiplier();

        try {
            return UnitFactory.findConnexoUnitFor(unitName, unitScale);
        } catch (Exception ex){
            inboundContext.getLogger().info("Cannot get a Connexo unit for " + unitName + " with scale " + unitScale);
        }

        return Unit.getUndefined();
    }
}
