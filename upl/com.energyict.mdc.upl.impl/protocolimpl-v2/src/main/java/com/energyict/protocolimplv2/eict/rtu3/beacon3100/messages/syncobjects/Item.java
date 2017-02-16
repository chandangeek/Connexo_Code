package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;


import com.energyict.obis.ObisCode;

import java.util.List;

public class Item {
    ObisCode obisCode;

    public static Object findObisCode(ObisCode obisCode, List<Object> list){
        for(Object object : list){
            if(obisCode.equals(obisCode)){
                return object;
            }
        }
        return null;
    }
}
