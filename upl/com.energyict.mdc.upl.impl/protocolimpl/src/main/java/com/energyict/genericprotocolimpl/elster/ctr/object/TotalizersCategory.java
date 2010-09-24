package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class TotalizersCategory extends AbstractSimpleBINObject{

    public TotalizersCategory(CTRObjectID id) {
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

        int[] valueLength = this.parseValueLengths();

        this.setValue(parser.parseBINValue(this, id, rawData, offset, valueLength));
        offset += sum(valueLength);  //There might be multiple value fields

        this.setAccess(parser.parseAccess(rawData, offset));
        offset +=1;

        this.setDefault(null);

        this.setSymbol(parser.parseSymbol(id));

    }

    @Override
    public BigDecimal parseOverflowValue() {
        return null;

    }

    protected int[] parseValueLengths() {
        int[] valueLength;
        valueLength = new int[]{4};
        return valueLength;
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();

        //Category: Totalizers
        if (x == 0x02) {
            unit = Unit.get("m3");
        }

        return unit;
    }

}
