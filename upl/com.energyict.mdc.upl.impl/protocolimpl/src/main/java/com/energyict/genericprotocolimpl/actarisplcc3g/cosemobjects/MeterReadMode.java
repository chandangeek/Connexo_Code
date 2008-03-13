package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.util.HashMap;
import java.util.Map;

import com.energyict.dlms.axrdencoding.TypeEnum;

public class MeterReadMode {
 
    private static  Map map = new HashMap( );
    
    public final static MeterReadMode DEFAULT       = add(0, "Default");
    public final static MeterReadMode REAL_MODE     = add(1, "Real mode");
    public final static MeterReadMode FICTIF_MODE   = add(2, "Fictif mode");

    private int id;
    private String description;
    
    public static MeterReadMode get(int id) {
        return (MeterReadMode)map.get(""+id);
    }
    
    private static MeterReadMode add(int id, String description) {
        MeterReadMode result = new MeterReadMode(id, description);
        map.put(""+id, result);
        return result;
    }
    
    private MeterReadMode(int id, String description) {
        this.id = id;
        this.description = description;
    }
    
    public String toString( ){
        return "MeterReadMode [" +id + ", " + description + "]";
    }

    public TypeEnum toTypeEnum( ){
        return new TypeEnum( id );
    }
    
    public MeterReadMode fromTypeEnum(TypeEnum typeEnum) {
        return get( typeEnum.getValue() );
    }
    
    public static void main(String [] args) {
        System.out.println( MeterReadMode.get(0) );
        System.out.println( MeterReadMode.get(1) );
        System.out.println( MeterReadMode.get(2) );
    }
    
}
