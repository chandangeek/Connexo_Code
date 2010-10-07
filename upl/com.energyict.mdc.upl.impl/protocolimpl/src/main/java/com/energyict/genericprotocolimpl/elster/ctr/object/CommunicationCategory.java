package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.AccessDescriptor;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class CommunicationCategory<T extends CommunicationCategory> extends AbstractCTRObject<T> {

    //Parse the raw data & fill in the object's properties
    public T parse(byte[] rawData, int ptr, AttributeType type) {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static

        CTRObjectID id = this.getId();
        ptr += 2; //Skip the Id bytes

        if (type.hasQualifier()) {
            Qualifier qlf = new Qualifier(parser.parseQlf(rawData, ptr));
            this.setQlf(qlf);
            ptr += qlf.LENGTH;
        }

        if (type.hasValueFields()) {
            int[] valueLength = this.parseValueLengths(id);
            this.setValue(parser.parseBCDValue(this, id, rawData, ptr, valueLength));
            ptr += sum(valueLength);  //There might be multiple value fields
        }

        if (type.hasAccessDescriptor()) {
            AccessDescriptor access = new AccessDescriptor(parser.parseAccess(rawData, ptr));
            this.setAccess(access);
            ptr += access.LENGTH;
        }

        if (type.hasDefaultValue()) {
            this.setDefault(parser.parseDefault(id));
        }

        this.setSymbol(parseSymbol(id));

        return (T) this;
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
                unit = Unit.get(BaseUnit.UNITLESS); break; //TODO: decibell;
        }
        return unit;

    }


    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0x0C:
                symbol = "GSM"; break;
            case 0x0E:
                symbol = "GPRS_S"; break;
        }
        return symbol;
    }


}
