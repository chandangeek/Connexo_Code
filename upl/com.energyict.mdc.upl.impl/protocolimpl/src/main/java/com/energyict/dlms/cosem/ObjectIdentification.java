/*
 * ObjectIdentification.java
 *
 * Created on 16 oktober 2007, 14:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.cosem;

import com.energyict.obis.*;

/**
 *
 * @author kvds
 */
public class ObjectIdentification {
    
    private ObisCode obisCode;
    private int classId;
    
    /** Creates a new instance of ObjectIdentification */
    public ObjectIdentification(ObisCode obisCode, int classId) {
        this.obisCode=obisCode;
        this.classId=classId;
    }
    
    /** Creates a new instance of ObjectIdentification */
    public ObjectIdentification(String obisCode, int classId) {
        this(ObisCode.fromString(obisCode), classId);
    }
    
    public ObisCode getObisCode( ){
        return obisCode;
    }
    
    public int getClassId( ){
        return classId;
    }
    
}
