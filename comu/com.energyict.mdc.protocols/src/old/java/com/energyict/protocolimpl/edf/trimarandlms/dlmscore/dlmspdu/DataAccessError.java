/*
 * DataAccessError.java
 *
 * Created on 21 februari 2007, 13:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu;

/**
 *
 * @author Koen
 */
public class DataAccessError {
    
    static final int hardware_fault = 1;
    static final int temporary_failure = 2;
    static final int read_write_denied = 3;
    static final int object_undefined = 4;
    static final int object_class_inconsistent = 9;
    static final int object_unavailable = 11;
    static final int type_unmatched = 12;
    static final int scope_of_access_violated = 13;
    
    
    /** Creates a new instance of DataAccessError */
    public DataAccessError() {
    }

    static String getDescription(int error) {
        switch(error) {
            
            case hardware_fault:
                return "hardware_fault";
            case temporary_failure:
                return "temporary_failure";
            case read_write_denied:
                return "read_write_denied";
            case object_undefined:
                return "object_undefined";
            case object_class_inconsistent:
                return "object_class_inconsistent";
            case object_unavailable:
                return "object_unavailable";
            case type_unmatched:
                return "type_unmatched";
            case scope_of_access_violated:
                return "scope_of_access_violated";
                
            default: return "Unknown DataAccessError code "+error;
        }
    }
}
