/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.itron.protocol.*;
import com.energyict.protocolimpl.itron.quantum.*;
import java.io.*;
import java.util.*;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;

/**
 *
 * @author Koen
 */
public class GeneralSetUpBasePage extends AbstractBasePage {
    
    private boolean dstEnabled;
    
    /** Creates a new instance of RealTimeBasePage */
    public GeneralSetUpBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TemplateBasePage(null)));
    }     
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(663,1);
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setDstEnabled((((int)data[0]&0xFF) & 0x40) == 0x40); 
        //getBasePagesFactory().getFulcrum().getTimeZone()
    }

    public boolean isDstEnabled() {
        return dstEnabled;
    }

    public void setDstEnabled(boolean dstEnabled) {
        this.dstEnabled = dstEnabled;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
