/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Coda;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Counter_Q;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.DataArray;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Index_Q;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Type;

import java.util.ArrayList;
import java.util.List;

public class ArrayQueryResponseStructure extends Data<ArrayQueryResponseStructure> {

    private Index_Q index_A;
    private Counter_Q counter_A;
    private CTRObjectID id;
    private Type type;
    private Coda coda;
    private DataArray data;
    private List<AbstractCTRObject> arrayData;

    public ArrayQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                id.getBytes(),
                type.getBytes(),
                index_A.getBytes(),
                counter_A.getBytes(),
                coda.getBytes(),
                data.getBytes()
        ));
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public ArrayQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType attributeType;

        int ptr = offset;

        id = new CTRObjectID().parse(rawData, ptr);
        ptr += id.getLength();

        type = new Type().parse(rawData, ptr);
        ptr += type.getLength();

        attributeType = createAttributeTypeConformType(type);

        index_A = new Index_Q().parse(rawData, ptr);
        ptr += index_A.getLength();

        counter_A = new Counter_Q().parse(rawData, ptr);
        ptr += counter_A.getLength();

        coda = new Coda().parse(rawData, ptr);
        ptr += coda.getLength();

        data = new DataArray(rawData.length - ptr).parse(rawData, ptr);

        //Check the length of the objects by parsing the first object
        arrayData = new ArrayList<AbstractCTRObject>();
        AbstractCTRObject obj = factory.parse(rawData, ptr, attributeType, id.toString());
        arrayData.add(obj);
        ptr += obj.getLength();

        //Parse the remaining objects
        int remainingValidElements = counter_A.getCounter_Q() - 1;
        while ((ptr <= rawData.length - arrayData.get(0).getBytes().length) && (remainingValidElements-- >= 0)) {
            obj = factory.parse(rawData, ptr, attributeType, id.toString());
            arrayData.add(obj);
            ptr += obj.getBytes().length;
        }

        return this;
    }

    private AttributeType createAttributeTypeConformType(Type type) throws CTRParsingException {
        AttributeType attributeType = new AttributeType(0x00);
        attributeType.setHasIdentifier(false);

        switch (type.getType()) {
            case 2:
                attributeType.setHasQualifier(true);
                attributeType.setHasValueFields(true);
                break;
            case 3:
                attributeType.setHasQualifier(false);
                attributeType.setHasValueFields(true);
                break;
            default:
                throw new CTRParsingException("Format of array elements (" + type.getType() + ") not yet supported.");
        }
        return attributeType;
    }

    public Index_Q getIndex_A() {
        return index_A;
    }

    public void setIndex_A(Index_Q index_A) {
        this.index_A = index_A;
    }

    public Counter_Q getCounter_A() {
        return counter_A;
    }

    public void setCounter_A(Counter_Q counter_A) {
        this.counter_A = counter_A;
    }

    public CTRObjectID getId() {
        return id;
    }

    public void setId(CTRObjectID id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Coda getCoda() {
        return coda;
    }

    public void setCoda(Coda coda) {
        this.coda = coda;
    }

    public DataArray getData() {
        return data;
    }

    public void setData(DataArray data) {
        this.data = data;
    }

    public List<AbstractCTRObject> getArrayData() {
        return arrayData;
    }

    public void setArrayData(List<AbstractCTRObject> arrayData) {
        this.arrayData = arrayData;
    }
}