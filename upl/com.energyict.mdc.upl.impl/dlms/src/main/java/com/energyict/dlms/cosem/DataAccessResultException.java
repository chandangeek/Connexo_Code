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
public final class DataAccessResultException extends IOException {
	
	/** Required for serializable classes. */
	private static final long serialVersionUID = 1L;

	/** Enumerates the most frequent data access result codes. */
	public enum DataAccessResultCode {
		SUCCESS(0, "Success"),
		HARDWARE_FAULT(1, "Hardware fault"),
		TEMPORARY_FAILURE(2, "Temporary failure"),
		RW_DENIED(3, "R/W denied"),
		OBJECT_UNDEFINED(4, "Object undefined"),
		OBJECTCLASS_INCONSISTENT(9, "Object class inconsistent"),
		OBJECT_UNAVAILABLE(11, "Object unavailable"),
		TYPE_UNMATCHED(12, "Type unmatched"),
		ACCESS_SCOPE_VIOLATION(13, "Scope of access violation"),
		DATA_BLOCK_UNAVAILABLE(14, "Data block unavailable"),
		OTHER(255, "Other reason");
		
		/** This is the integer result code returned by the device. */
		private final int result;
		
		/** The description of the error. */
		private final String description;
		
		/**
		 * Create a new instance using the result and description.
		 * 
		 * @param 	result			The result.
		 * @param 	rescription		The description.
		 */
		private DataAccessResultCode(final int result, final String description) {
			this.result = result;
			this.description = description;
		}
		
		/**
		 * Returns the result code as it was returned by the device.
		 * 
		 * @return	The result code as it was returned by the device.
		 */
		public final int getResultCode() {
			return this.result;
		}
		
		/**
		 * Returns the description.
		 * 
		 * @return	The description.
		 */
		public final String getDescription() {
			return this.description;
		}
		
		/**
		 * Returns the corresponding data access result code.
		 * 
		 * @param 	resultCode		The result code.
		 * 
		 * @return	The corresponding {@link DataAccessResultCode}.
		 */
		private static final DataAccessResultCode byResultCode(final int resultCode) {
			for (final DataAccessResultCode code : values()) {
				if (code.result == resultCode) {
					return code;
				}
			}
			
			return null;
		}
	}
    
	/** The code as it was returned by the device. This is always filled in. */
    private final int dataAccessResult;
    
    /** This is a "parsed" version of the preceding field. */
    private final DataAccessResultCode dataAccessResultCode;
    
    /** Creates a new instance of DataAccessResultException */
    public DataAccessResultException(int dataAccessResult) {
        this(dataAccessResult,"Cosem Data-Access-Result exception "+evalDataAccessResult(dataAccessResult));
    }
    
    public DataAccessResultException(int dataAccessResult, String message) {
        super(message);
        
        this.dataAccessResult = dataAccessResult;
        this.dataAccessResultCode = DataAccessResultCode.byResultCode(dataAccessResult);
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
     
    /**
     * Returns the data access result code if there is one. 
     * 
     * @return	The data access result code if there is one.
     */
    public final DataAccessResultCode getCode() {
    	return this.dataAccessResultCode;
    }
}
