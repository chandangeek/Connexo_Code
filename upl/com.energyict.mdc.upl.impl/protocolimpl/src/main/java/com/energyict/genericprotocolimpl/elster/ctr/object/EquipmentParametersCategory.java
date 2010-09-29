package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

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

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";
        int z = id.getZ();

        switch(id.getY()) {
            case 0: switch(z) {
                case 0: symbol = "ALL_PA";
                case 1: symbol = "CCODE";
                case 2: symbol = "CIA";
                case 3: symbol = "CCA";
                case 4: symbol = "VF";
                case 5: symbol = "CAP";
                case 7: symbol = "VS_PRO";

            }
            case 1: switch(z) {
                case 1: symbol = "Emsize";
                case 2: symbol = "EAsize";
                case 3: symbol = "ETsize";
            }
            case 2: switch(z) {
                case 0: symbol = "NSA";
                case 2: symbol = "VHA";
                case 3: symbol = "NC";
                case 4: symbol = "NOOG";
                case 5: symbol = "ID-SFTW";
            }
            case 3: switch (z) {
                case 0: symbol = "VFM";
                case 1: symbol = "VFA";
            }
            case 4: switch(z) {
                case 0: symbol = "Add";
                case 1: symbol = "MAC_Add";
            }
        }
        return symbol;
    }

    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        int z = id.getZ();
        switch(id.getY()) {
            case 0: if (z == 9) {overflow = 8;}
            case 1: switch(z) {

                case 3:
                case 2: if(valueNumber == 0) {overflow = 127;}
                case 1: if(valueNumber == 0) {overflow = 2;}
            }
            case 2: if (z==3) {overflow = 15;}
            
        }

        return new BigDecimal(overflow);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0: valueLength = new int[]{12,5,4,6,3,4,1}; break;
                case 1: valueLength = new int[]{12}; break;
                case 2: valueLength = new int[]{5}; break;
                case 3: valueLength = new int[]{4}; break;
                case 4: valueLength = new int[]{6}; break;
                case 5: valueLength = new int[]{3}; break;
                case 7: valueLength = new int[]{4}; break;
                case 9: valueLength = new int[]{1}; break;

            }
            case 1: switch(z) {
                case 1:
                case 2: valueLength = new int[]{1,2}; break;
                case 3: valueLength = new int[]{1,1}; break;
            }
            case 2: switch(z) {
                case 0: valueLength = new int[]{16}; break;
                case 2: valueLength = new int[]{4}; break;
                case 3: valueLength = new int[]{1}; break;
                case 4: valueLength = new int[]{2}; break;
                case 5: valueLength = new int[]{4,4,3}; break;
            }
            case 3: valueLength = new int[]{10}; break;
            case 4: switch(z) {
                case 0: valueLength = new int[]{2}; break;
                case 1: valueLength = new int[]{6}; break;
            }
        }
        return valueLength;
    }

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        return null;        //There's no units in this category
    }

}
