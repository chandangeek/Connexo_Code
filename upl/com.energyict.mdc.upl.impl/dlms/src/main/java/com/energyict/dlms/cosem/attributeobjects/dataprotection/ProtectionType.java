package com.energyict.dlms.cosem.attributeobjects.dataprotection;

import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * Created by cisac on 12/14/2016.
 */
public enum ProtectionType {
    AUTHENTICATION(0),
    ENCRYPTION(1),
    AUTHENTICATION_AND_ENCRYPTION(2),
    DIGITAL_SIGNATURE(3);

    private final int id;

    private ProtectionType(int id){
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    public TypeEnum getTypeEnum() {
        return new TypeEnum(id);
    }
}
