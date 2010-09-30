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
public class CommunicationCategory extends AbstractStringObject<CommunicationCategory> {

    @Override
    public CommunicationCategory parse(byte[] rawData, int offset) {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static
        CTRObjectID id = this.getId();
        offset += 2; //Skip the Id bytes

        this.setQlf(parser.parseQlf(rawData, offset));
        offset += 1;

        int[] valueLength = this.parseValueLengths(id);

        this.setValue(parser.parseBCDValue(this, id, rawData, offset, valueLength));
        offset += sum(valueLength);  //There might be multiple value fields

        this.setAccess(parser.parseAccess(rawData, offset));
        offset += 1;

        this.setDefault(parser.parseDefault(id));

        this.setSymbol(parseSymbol(id));

        return this;
    }

    public CommunicationCategory(CTRObjectID id) {
        this.setId(id);
    }

    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return null;
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        switch (id.getY()) {
            case 0x0C:
                valueLength = new int[]{1};
                break;
            case 0x0E:
                valueLength = new int[]{112};
                break;
        }
        return valueLength;
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
        switch (id.getY()) {
            case 0x0C:
                unit = Unit.get(BaseUnit.UNITLESS); //TODO: decibell;
        }
        return unit;

    }


    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0x0C:
                symbol = "GSM";
            case 0x0E:
                symbol = "GPRS_S";
        }
        return symbol;
    }


}
