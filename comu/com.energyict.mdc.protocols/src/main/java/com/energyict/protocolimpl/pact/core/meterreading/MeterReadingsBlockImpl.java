/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterReadingsBlock.java
 *
 * Created on 19 maart 2004, 11:37
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import java.io.IOException;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public abstract class MeterReadingsBlockImpl implements MeterReadingsBlock {
    
    abstract protected void parse() throws IOException;
    abstract protected String print();
    
    private int typeId;
    private byte[] data;
    private boolean multipleSet;
    private TimeZone timeZone;
    
    /** Creates a new instance of MeterReadingsBlock */
    public MeterReadingsBlockImpl(byte[] data) {
        this(data,false,null);
    }
    
    public MeterReadingsBlockImpl(byte[] data, TimeZone timeZone) {
        this(data,false,timeZone);
    }
    
    public MeterReadingsBlockImpl(byte[] data, boolean multipleSet) {
        this(data,multipleSet,null);
    }
    
    public MeterReadingsBlockImpl(byte[] data, boolean multipleSet, TimeZone timeZone) {
        this.multipleSet=multipleSet;
        this.timeZone=timeZone;
        setData(data);
        //process();
    }
    
    private void process() {
        if (getData() != null) {
            setTypeId((int)getData()[0]&0xFF);
            try {
                parse();
            }
            catch(IOException e) {
                e.printStackTrace(); // should never happen
            }
        }        
    }
    
    public String toString() {
        return print();
    }
    
    /** Getter for property typeId.
     * @return Value of property typeId.
     *
     */
    public int getTypeId() {
        return typeId;
    }
    
    /** Setter for property typeId.
     * @param typeId New value of property typeId.
     *
     */
    protected void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    
    /** Getter for property data.
     * @return Value of property data.
     *
     */
    protected byte[] getData() {
        return this.data;
    }
    
    /** Setter for property data.
     * @param data New value of property data.
     *
     */
    public void setData(byte[] data) {
    	if(data != null){
    		this.data = data.clone();
    	}
        process();
    }
    
    /** Getter for property multipleSet.
     * @return Value of property multipleSet.
     *
     */
    public boolean isMultipleSet() {
        return multipleSet;
    }
    
    /** Setter for property multipleSet.
     * @param multipleSet New value of property multipleSet.
     *
     */
    public void setMultipleSet(boolean multipleSet) {
        this.multipleSet = multipleSet;
    }
    
    /** Getter for property timeZone.
     * @return Value of property timeZone.
     *
     */
    public java.util.TimeZone getTimeZone() {
        return timeZone;
    }
    
    /** Setter for property timeZone.
     * @param timeZone New value of property timeZone.
     *
     */
    public void setTimeZone(java.util.TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
}
