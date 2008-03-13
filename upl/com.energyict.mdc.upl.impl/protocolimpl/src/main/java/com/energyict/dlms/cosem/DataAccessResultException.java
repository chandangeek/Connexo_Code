/*
 * DataAccessResultException.java
 *
 * Created on 5 december 2007, 8:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.cosem;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public class DataAccessResultException extends IOException {
    
    private int dataAccessResult;
        
    /** Creates a new instance of DataAccessResultException */
    public DataAccessResultException(int dataAccessResult) {
        this(dataAccessResult,"Cosem Data-Access-Result exception "+evalDataAccessResult(dataAccessResult));
    }
    public DataAccessResultException(int dataAccessResult, String message) {
        super(message);
        this.setDataAccessResult(dataAccessResult);
    }
    
    public String toString() {
        return super.toString()+", "+evalDataAccessResult(getDataAccessResult());
    }
    
    
    public boolean isEvalDataAccessResultStandard() {
        return (dataAccessResult <=14) || (dataAccessResult == 250);
        
    }
    
    static public String evalDataAccessResult(int val) {
        String strErr;
        switch(val) {
            case 0: strErr = "success";
            case 1: strErr = "Hardware fault";break;
            case 2: strErr = "Temporary fauilure";break;
            case 3: strErr = "R/W denied";break;
            case 4: strErr = "Object undefined";break;
            case 9: strErr = "Object class inconsistent";break;
            case 11: strErr = "Object unavailable";break;
            case 12: strErr = "Type unmatched";break;
            case 13: strErr = "Scope of access violated";break;
            case 14: strErr = "Data block unavailable";break;
            case 250: strErr = "Other reason";break;
            default: strErr = "Unknown data-access-result code "+val;break;
        }
        return strErr;
        
    } // private void evalDataAccessResult(int val) throws IOException            

    public int getDataAccessResult() {
        return dataAccessResult;
    }

    private void setDataAccessResult(int dataAccessResult) {
        this.dataAccessResult = dataAccessResult;
    }
            
}
