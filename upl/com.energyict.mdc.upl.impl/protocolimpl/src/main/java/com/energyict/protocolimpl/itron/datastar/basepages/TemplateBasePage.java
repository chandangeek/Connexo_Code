/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocol.*; 
import com.energyict.protocolimpl.itron.protocol.*;
import com.energyict.protocolimpl.itron.vectron.*;
import java.io.*;
import java.util.*;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.base.*;
/**
 *
 * @author Koen
 */
public class TemplateBasePage extends AbstractBasePage {
    
    
    /** Creates a new instance of RealTimeBasePage */
    public TemplateBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TemplateBasePage(null)));
    }     
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x0,0x0);
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        //getBasePagesFactory().getFulcrum().getTimeZone()
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
