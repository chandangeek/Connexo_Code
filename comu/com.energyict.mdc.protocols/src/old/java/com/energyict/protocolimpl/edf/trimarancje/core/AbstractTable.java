/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractTable.java
 *
 * Created on 23 juni 2006, 15:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
abstract public class AbstractTable {
    
    final int DEBUG=0;
    
    abstract protected void parse(byte[] data) throws IOException;
    abstract protected int getCode();
    private int length;
    private int pointer;
    
    private DataFactory dataFactory;
    
    /**
     * This is a shiftable years table. The meter will not contain more then ten years of data,
     * but because the ProfileTimestamp has only a unit from the year in it, we have to be able to
     * loop over a decennium.
     */
    private int[] decenniumYears = new int[10];
    private long currentMillis;
            
    /** Creates a new instance of AbstractTable */
    public AbstractTable(DataFactory dataFactory) {
        this.setDataFactory(dataFactory);
        setLength(0);
        setPointer(0);
		setCurrentTime(Calendar.getInstance(getTimeZone()).getTimeInMillis());
		constructDecenniumTable();
    }

    public DataFactory getDataFactory() { 
        return dataFactory;
    }

    private void setDataFactory(DataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }
    
    public void invoke() throws IOException {
        // KV 09082006 retry mechanism
        int retries=0;
        while(true) {
            try {
//                parse(getDataFactory().getTrimaran().getSPDUFactory().enq(getCode(),getLength()).getData());
            	parse(getDataFactory().getTrimaran().getSPDUFactory().enq(getCode(), getLength(), getPointer()).getData());
                break;
            }
            catch(IOException e) {
                if (retries++>=3){
                    throw new IOException(e.toString()+", "+getLogInfo());
                } else if (DEBUG>=1) {
					System.out.println("KV_DEBUG> AbstractTable, invoke(), "+e.toString()+", retry "+retries);
				}
            }
            catch(ArrayIndexOutOfBoundsException e) {
                if (retries++>=3) {
					throw new IOException(e.toString()+", "+getLogInfo());
				} else if (DEBUG>=1) {
					System.out.println("KV_DEBUG> AbstractTable, invoke(), "+e.toString()+", retry "+retries);
				}
            }
        } // while(true)
        
    } // public void invoke() throws IOException

    private String getLogInfo() {
        switch(getCode()) {
            case 12:
                return "requesting meter status ...";
            case 11:
                return "requesting activity calendar";
            case 8:
                return "requesting load profile";
            case 2:
                return "requesting information of current period";
            case 3:
                return "requesting information of previous period";
            default: 
                return "unknown requesting code "+getCode();     
        }
    }
    
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
	/**
	 * @return the pointer
	 */
	public int getPointer() {
		return pointer;
	}
	/**
	 * @param pointer the pointer to set
	 */
	public void setPointer(int pointer) {
		this.pointer = pointer;
	}
	
	/**
	 * Constructs a table of the last TEN years.
	 */
	protected void constructDecenniumTable(){
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTimeInMillis(getCurrentMillis());
		int year = cal.get(Calendar.YEAR);
		int yearUnit = year%10;
		for(int i = 0; i < 10; i++){
			this.decenniumYears[yearUnit] = year--;
			if(yearUnit == 0){
				yearUnit = 9;
			} else {
				yearUnit--;
			}
		}
	}
	
	/**
	 * Getter for the decenniumYear table
	 * @return the current decenniumYears table
	 */
	protected int[] getDecenniumYearTable(){
		return this.decenniumYears;
	}
	
	/**
	 * Setter for the currentMillis
	 * 
	 * @param currentTimeInMillis
	 */
	protected void setCurrentTime(long currentTimeInMillis) {
		this.currentMillis = currentTimeInMillis;
	}

	/**
	 * Getter for the currentMillis
	 * @return the current millis
	 */
	private long getCurrentMillis(){
		return this.currentMillis;
	}
	
	/**
	 * @return the meter his {@link TimeZone}
	 */
    private TimeZone getTimeZone() {
		if (getDataFactory() == null) {
			return TimeZone.getDefault();
		} else {
			return getDataFactory().getTrimaran().getTimeZone();
		}
	}
    
}
