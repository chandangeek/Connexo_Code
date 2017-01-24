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
public class EquipmentParametersCategory extends AbstractStringObject {

    public EquipmentParametersCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";
        int z = id.getZ();

        switch(id.getY()) {
            case 0: switch(z) {
                case 0: symbol = "ALL_PA"; break;
                case 1: symbol = "CCODE"; break;
                case 2: symbol = "CIA";   break;
                case 3: symbol = "CCA";  break;
                case 4: symbol = "VF";   break;
                case 5: symbol = "CAP"; break;
                case 7: symbol = "VS_PRO"; break;
                case 9: symbol = "SYNCT"; break;

            }  break;
            case 1: switch(z) {
                case 1: symbol = "Emsize"; break;
                case 2: symbol = "EAsize"; break;
                case 3: symbol = "ETsize"; break;
            } break;
            case 2: switch(z) {
                case 0: symbol = "NSA";  break;
                case 2: symbol = "VHA"; break;
                case 3: symbol = "NC";  break;
                case 4: symbol = "NOOG"; break;
                case 5: symbol = "ID-SFTW"; break;
            }   break;
            case 3: switch (z) {
                case 0: symbol = "VFM"; break;
                case 1: symbol = "VFA"; break;
            }     break;
            case 4: switch(z) {
                case 0: symbol = "Add"; break;
                case 1: symbol = "MAC_Add"; break;
            }   break;
            case 5: symbol = "PADL"; break;
        }
        return symbol;
    }

    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        int z = id.getZ();
        switch(id.getY()) {
            case 0: if (z == 9) {overflow = 8;}  break;
            case 1: switch(z) {
                case 3: if(valueNumber == 1) {overflow = 127;} break;
            } break;
            case 2: if (z==3) {overflow = 15;} break;

        }

        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0: valueLength = new int[]{12,5,4,6,3,5,1}; break;
                case 1: valueLength = new int[]{12}; break;
                case 2: valueLength = new int[]{5}; break;
                case 3: valueLength = new int[]{4}; break;
                case 4: valueLength = new int[]{6}; break;
                case 5: valueLength = new int[]{3}; break;
                case 7: valueLength = new int[]{4}; break;
                case 9: valueLength = new int[]{1}; break;

            }  break;
            case 1: switch(z) {
                case 1:
                case 2: valueLength = new int[]{1,2}; break;
                case 3: valueLength = new int[]{1,1}; break;
            } break;
            case 2: switch(z) {
                case 0: valueLength = new int[]{16}; break;
                case 2: valueLength = new int[]{4}; break;
                case 3: valueLength = new int[]{1}; break;
                case 4: valueLength = new int[]{2}; break;
                case 5: valueLength = new int[]{4,4,3}; break;
            } break;
            case 3: valueLength = new int[]{10}; break;
            case 4: switch(z) {
                case 0: valueLength = new int[]{2}; break;
                case 1: valueLength = new int[]{6}; break;
            }  break;
            case 5: switch (z) {
                    case 2: valueLength = new int[]{1}; break;
                    default: valueLength = new int[]{1, 4, 1, 1, 1, 1, 2}; break;
                }
        }
        return valueLength;
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        if (id.getY() == 5) {
            if (valueNumber == 3) {return Unit.get(BaseUnit.SECOND, -3);}         //milliseconds
            if (valueNumber == 4) {return Unit.get(BaseUnit.SECOND);}
        }
        return Unit.get(BaseUnit.UNITLESS);        //There's no units in this category
    }

}
