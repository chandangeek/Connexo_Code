package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class FlowAndVolumeCategory extends AbstractSimpleBINObject{
     
    public FlowAndVolumeCategory(CTRObjectID id) {
        this.setId(id);
    }

    //Parse the raw data & fill in the object's properties
    @Override
    public void parse(byte[] rawData, int offset) {

         CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static
        CTRObjectID id = this.getId();
        offset +=2; //Skip the Id bytes

        this.setQlf(parser.parseQlf(rawData, offset));
        offset +=1;

        int[] valueLength = this.parseValueLengths(id);

        this.setValue(parser.parseBINValue(this, id, rawData, offset, valueLength));
        offset += sum(valueLength);  //There might be multiple value fields

        this.setAccess(parser.parseAccess(rawData, offset));
        offset +=1;

        this.setDefault(null);

        this.setSymbol(parser.parseSymbol(id));
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength;
        switch(id.getY()) {
                default: valueLength = new int[]{3}; break;
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
            }
        return valueLength;
    }

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();

        // Category: flow or volume
        if (x == 0x01) {
            if ((y == 0x01) || (y == 0x03) || (y >= 0x0D)) {
                unit = Unit.get("m3");
            } else {
                unit = Unit.get("m3/h");
                if (y == 0x06 || y == 0x07 || y == 0x09 || y == 0x0A) {
                    if (z == 0x04) {
                        if (valueNumber == 1) {unit = Unit.get(BaseUnit.DAY);}
                        if (valueNumber == 2) {unit = Unit.get(BaseUnit.HOUR);}
                        if (valueNumber == 3) {unit = Unit.get(BaseUnit.MINUTE);}
                    }
                    if (z <= 0x04) {
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
