package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.cbo.Unit;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;

public class UnitFactory {

    public static Unit findConnexoUnitFor(String unitName, int unitScale) {
        // TODO -> use this scale properly
        Unit unit = Unit.get(unitName, 0);
        return unit;
    }

    public static Unit from(TelegramVariableDataRecord record, InboundContext inboundContext) {
        String unitName = record.getVif().getmUnit().getValue();
        int unitScale = record.getVif().getMultiplier();

        Unit unit;
        try {
            UnitFactory.findConnexoUnitFor(unitName, unitScale);
        } catch (Exception ex){
            inboundContext.getLogger().log("Cannot get a Connexo unit for " + unitName + " with scale " + unitScale);
        }

        return Unit.getUndefined();
    }
}
