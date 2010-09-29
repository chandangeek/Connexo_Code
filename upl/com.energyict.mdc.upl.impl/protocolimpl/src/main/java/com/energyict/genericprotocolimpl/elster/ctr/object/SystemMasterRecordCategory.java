package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class SystemMasterRecordCategory extends AbstractUnsignedBINObject {

    public SystemMasterRecordCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";
        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0: symbol = "PDR";
                case 1: symbol = "NCG";
                case 3: symbol = "TypeMis";
                case 2: symbol = "TypeImp";
                case 4: symbol = "Local";
                case 5: symbol = "GEO";
                case 6: symbol = "RagSoc";
                case 7: symbol = "CodUte";

            }
            case 2: switch(z) {
                case 0: symbol = "An_CONT";
                case 1: symbol = "An_CORR";
            }
        }
        return symbol;
    }


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return null; //There's no overflow values available in this category.
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0: valueLength = new int[]{7}; break;
                case 1:
                case 3:
                case 2: valueLength = new int[]{1}; break;
                case 4: valueLength = new int[]{60}; break;
                case 5: valueLength = new int[]{3,3,2}; break;
                case 6:
                case 7: valueLength = new int[]{30}; break;
            }
            case 1: valueLength = new int[]{10,10,16}; break;
            case 2: switch(z) {
                case 0: valueLength = new int[]{4,3,13}; break;
                case 1: valueLength = new int[]{4,16}; break;
            }
        }
        return valueLength;
    }

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
        switch(id.getY()) {
            case 0: switch(id.getZ()) {
                case 5:
                    if (valueNumber ==2) {unit = Unit.get(BaseUnit.METER);}
            }
        }
        return unit;
    }

}
