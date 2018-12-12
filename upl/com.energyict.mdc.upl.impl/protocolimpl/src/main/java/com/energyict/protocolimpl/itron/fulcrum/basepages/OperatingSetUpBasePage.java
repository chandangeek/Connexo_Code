/*
 * OperatingSetUpBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.itron.fulcrum.*;
import java.io.*;
import java.util.*;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

/**
 *
 * @author Koen
 */
public class OperatingSetUpBasePage extends AbstractBasePage {
    
    private boolean dstEnabled;
    
    /** Creates a new instance of OperatingSetUpBasePage */
    public OperatingSetUpBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new RealTimeBasePage(null)));
    }     
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x27E5, 1);
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setDstEnabled((data[0] & 0x04) == 0x04);
    }

    public boolean isDstEnabled() {
        return dstEnabled;
    }

    private void setDstEnabled(boolean dstEnabled) {
        this.dstEnabled = dstEnabled;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
