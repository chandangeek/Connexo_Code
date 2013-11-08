/*
 * WriteReply.java
 *
 * Created on 1 december 2006, 16:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class WriteReply extends AbstractCommandResponse {
    
    /** Creates a new instance of WriteReply */
    public WriteReply() {
    }
    
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new WriteReply()));
//    }
    
    protected void parse(byte[] rawData) {
        int offset=0;
        offset++; // skip read response
        
    }
    
} // public class WriteReply extends AbstractCommandResponse
