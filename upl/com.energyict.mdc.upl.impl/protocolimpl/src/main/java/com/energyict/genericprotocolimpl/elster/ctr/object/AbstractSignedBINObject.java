package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.AccessDescriptor;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:00:24
 */
public abstract class AbstractSignedBINObject<T extends AbstractSignedBINObject> extends AbstractCTRObject<T> {

    //Parse the raw data & fill in the object's properties
    public T parse(byte[] rawData, int ptr, AttributeType type) {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static

        CTRObjectID id = this.getId();

        if (type.hasIdentifier()) {
            ptr += 2; //Skip the Id bytes
        }
        
        if (type.hasQualifier()) {
            Qualifier qlf = new Qualifier(parser.parseQlf(rawData, ptr));
            this.setQlf(qlf);
            if (qlf.isInvalid()) {
                return (T) this;   //If the QLF is 0xFF (invalid), return an empty object
            }
            ptr += qlf.getLength();
        }

        if (type.hasValueFields()) {
            int[] valueLength = this.getValueLengths(id);
            this.setValue(parser.parseSignedBINValue(this, id, rawData, ptr, valueLength));
            ptr += sum(valueLength);  //There might be multiple value fields
        }

        if (type.hasAccessDescriptor()) {
            AccessDescriptor access = new AccessDescriptor(parser.parseAccess(rawData, ptr));
            this.setAccess(access);
            ptr += access.LENGTH;
        }

        if (type.hasDefaultValue()) {
            this.setDefault(parser.parseDefault(id, this.getValue()));
        }

        this.setSymbol(getSymbol(id));

        return (T) this;
    }
}