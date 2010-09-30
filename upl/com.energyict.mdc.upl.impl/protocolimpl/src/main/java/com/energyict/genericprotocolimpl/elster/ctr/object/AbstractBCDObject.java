package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveConverter;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:00:24
 */
public abstract class AbstractBCDObject<T extends AbstractBCDObject> extends AbstractCTRObject<T> {

    public T parse(byte[] rawData, int offset) {
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

        return (T) this;
    }

}