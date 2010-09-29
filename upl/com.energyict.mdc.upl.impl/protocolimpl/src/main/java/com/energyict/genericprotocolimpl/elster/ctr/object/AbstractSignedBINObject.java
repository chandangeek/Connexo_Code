package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveConverter;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:00:24
 */
public abstract class AbstractSignedBINObject extends AbstractCTRObject {

    private CTRAbstractValue[] value; //Binary value, with its unit & an overflowValue.

    //Parse the raw data & fill in the object's properties
    public void parse(byte[] rawData, int offset) {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static
        CTRObjectID id = this.getId();
        offset +=2; //Skip the Id bytes

        this.setQlf(parser.parseQlf(rawData, offset));
        offset +=1;

        int[] valueLength = this.parseValueLengths(id);

        this.setValue(parser.parseSignedBINValue(this, id, rawData, offset, valueLength));
        offset += sum(valueLength);  //There might be multiple value fields

        this.setAccess(parser.parseAccess(rawData, offset));
        offset +=1;

        this.setDefault(parser.parseDefault(id));

        this.setSymbol(parseSymbol(id));
    }

    public CTRAbstractValue[] getValue() {
            return value;
    }
    protected void setValue(CTRAbstractValue[] value) {
        this.value = value;
    }


    @Override
    public byte[] getBytes() {
        CTRObjectID id = getId();
        CTRPrimitiveConverter converter = new CTRPrimitiveConverter();
        int[] valueLengths = parseValueLengths(id);
        byte[] idBytes = converter.convertId(id);


        return null;
    }

}