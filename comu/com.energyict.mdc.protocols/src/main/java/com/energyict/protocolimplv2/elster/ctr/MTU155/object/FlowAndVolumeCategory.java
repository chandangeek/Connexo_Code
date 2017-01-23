package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class FlowAndVolumeCategory extends AbstractUnsignedBINObject {

    public FlowAndVolumeCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
                case 0: symbol = "Qm"; break;
                case 1: symbol = "Vm"; break;
                case 2: symbol = "Qb"; break;
                case 3: symbol = "Vb"; break;
                case 7: symbol = "Qcb_min"; break;
                case 6: symbol = "Qcm_min"; break;
                case 9: symbol = "Qcm_max"; break;
                case 0x0A: symbol = "Qcb_max"; break;
                case 0x0F: symbol = "Ve"; break;
                case 0x0D: symbol = "Vcontr"; break;
                case 0x0E: symbol = "Vpre"; break;
                case 0x0C: switch (id.getZ()) {
                    case 2: symbol = "Qcontr"; break;
                    case 4:
                    case 3: symbol = "Qnom"; break;
                } break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        double scale = 1.0;
        double overflow = getCommonOverflow(unit);

        if (Unit.get(BaseUnit.CUBICMETER).equals(unit)) {overflow = 9999999.0;}
        if (Unit.get(BaseUnit.CUBICMETERPERHOUR).equals(unit)) {overflow = 999999.0;}

        if (valueNumber == 0) {
            switch (id.getY()) {
                case 0:
                case 6:
                case 9: scale = 0.1; break;
            }
        }
        return new BigDecimal((int) (overflow*scale));
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength;
        switch(id.getY()) {
                case 6:
                case 7:
                case 9:
                case 0x0A:
                    valueLength = new int[]{3,1,1};
                    switch (id.getZ()) {
                        case 4: valueLength = new int[]{3,1,1,1}; break;
                        case 5:
                        case 6: valueLength = new int[]{3,1,1,1,1}; break;
                    }
                break;
                default: valueLength = new int[]{3}; break;
        }
        return valueLength;
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();

        // Category: flow or volume
        if (x == 0x01) {
            if ((y == 0x01) || (y == 0x03) || (y >= 0x0D)) {
                unit = Unit.get(BaseUnit.CUBICMETER, getQlf().getKmoltFactor());
            } else {
                unit = Unit.get(BaseUnit.CUBICMETERPERHOUR, getQlf().getKmoltFactor());
                if (y == 0x06 || y == 0x07 || y == 0x09 || y == 0x0A) {
                    if (z == 0x04) {
                        if (valueNumber == 1) {unit = Unit.get(BaseUnit.DAY);}
                        if (valueNumber == 2) {unit = Unit.get(BaseUnit.HOUR);}
                        if (valueNumber == 3) {unit = Unit.get(BaseUnit.MINUTE);}
                    }
                    if (z < 0x04) {
                        if (valueNumber == 1) {unit = Unit.get(BaseUnit.HOUR);}
                        if (valueNumber == 2) {unit = Unit.get(BaseUnit.MINUTE);}
                    }
                    if (z > 0x04) {
                        if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                        if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                        if (valueNumber == 3) {unit = Unit.get(BaseUnit.HOUR);}
                        if (valueNumber == 4) {unit = Unit.get(BaseUnit.MINUTE);}
                    }
                }
            }
        }
        return unit;
    }
}
