package com.energyict.dlms.cosem.attributeobjects.dataprotection;

import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * Created by cisac on 12/14/2016.
 */
public enum RestrictionType{
    NONE(0),
    RESTRICTION_BY_DATE(1),
    RESTRICTION_BY_ENTRY(2);

    private final int id;

    private RestrictionType(int id){
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    public TypeEnum getTypeEnum() {
        return new TypeEnum(id);
    }
}
