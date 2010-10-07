package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveConverter;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:00:24
 */
public abstract class AbstractStringObject<T extends AbstractStringObject> extends AbstractCTRObject<T> {

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
            this.setValue(parser.parseStringValue(this, id, rawData, ptr, valueLength));
            ptr += sum(valueLength);  //There might be multiple value fields
        }

        if (type.hasAccessDescriptor()) {
            this.setAccess(parser.parseAccess(rawData, ptr));
            ptr += 1;
        }

        if (type.hasDefaultValue()) {
            this.setDefault(parser.parseDefault(id));
        }

        this.setSymbol(parseSymbol(id));

        return (T) this;
    }

}