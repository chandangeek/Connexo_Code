package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 15-okt-2010
 * Time: 9:42:46
 */
public class CommercialParametersCategory extends AbstractUnsignedBINObject {

    public CommercialParametersCategory(CTRObjectID id) {
        this.setId(id);
    }

    @Override
    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        return null;
    }

    @Override
    protected String parseSymbol(CTRObjectID id) {
        if (id.getZ() == 4) {
            return "ID-PT";
        } else {
            return "PT";
        }
    }

    @Override
    public int[] parseValueLengths(CTRObjectID id) {
        if (id.getZ() == 4) {
            return new int[]{2, 2, 2, 3};
        } else {
            return new int[]{1, 2, 3, 1, 17, 17, 2, 30};
        }
    }

    @Override
    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return null;
    }
}