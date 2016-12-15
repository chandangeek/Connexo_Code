package com.energyict.dlms.cosem.attributeobjects.dataprotection;

import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * Created by cisac on 12/15/2016.
 */
public enum KeyInfoType {
    IDENTIFIED_KEY(0),
    WRAPED_KEY(1),
    AGREED_KEY(2);

    private final int id;

    private KeyInfoType(int id){
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    public TypeEnum getTypeEnum() {
        return new TypeEnum(id);
    }
}
