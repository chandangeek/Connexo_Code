/*
 * RecordTemplate_1.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocol.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class RecordTemplate {
    
    /** Creates a new instance of RecordTemplate_1 */
    public RecordTemplate(byte[] data,int offset) throws IOException {
        
    }
    
    public RecordTemplate() {}
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new RecordTemplate()));
    } 
    
    static public int size() {
        return 0;
    }
    

    
}
