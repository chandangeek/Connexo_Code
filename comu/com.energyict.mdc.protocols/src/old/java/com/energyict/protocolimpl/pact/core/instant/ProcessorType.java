/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProcessorType.java
 *
 * Created on 12 mei 2004, 16:57
 */

package com.energyict.protocolimpl.pact.core.instant;

/**
 *
 * @author  Koen
 */
public class ProcessorType {
    
    public static final int TYPE_P0X=0;
    public static final int TYPE_E3X=1;
    public static final int TYPE_E100=2;
    public static final int TYPE_E200=3;
    public static final int TYPE_E2XX=4;
    private String[] processorTypes={"P0x","E3x","E100","E200","E2xx"};
    
    private byte[] data;
    private int processortype;
    
    /** Creates a new instance of ProcessorType */
    public ProcessorType(byte[] data) {
    	if(data != null){
    		this.data=data.clone();
    	}
        parse();
    }
    
    public String toString() {
        return processorTypes[getProcessortype()];
    }
    
    private void parse() {
        //if (data[0] == (byte)0xFA) {
        if (data.length < 2) {
			setProcessortype(TYPE_P0X);
		} else {
            if (data[0] == (byte)0x8C) {
				setProcessortype(TYPE_E100);
			} else if (data[0] == (byte)0x90) {
				setProcessortype(TYPE_E200);
			} else if ((((int)data[0]&0xff) >= ((int)0x91&0xff)) && (((int)data[1]&0xff) <= ((int)0x9F&0xff))) {
				setProcessortype(TYPE_E2XX);
			} else if ((((int)data[0]&0xff) >= ((int)0xD0&0xff)) && (((int)data[1]&0xff) <= ((int)0xE1&0xff))) {
				setProcessortype(TYPE_E3X);
			}
        }
    }
    
    /**
     * Getter for property processortype.
     * @return Value of property processortype.
     */
    public int getProcessortype() {
        return processortype;
    }
    
    /**
     * Setter for property processortype.
     * @param processortype New value of property processortype.
     */
    public void setProcessortype(int processortype) {
        this.processortype = processortype;
    }
    
}
