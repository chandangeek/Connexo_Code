/*
 * RealTimeBasePage.java
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
public class AddressWriteBasePage extends AbstractBasePage {
    
    
    private int address;
    private byte[] data;
    
    /** Creates a new instance of RealTimeBasePage */
    public AddressWriteBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new RealTimeBasePage(null)));
    }     
    
    protected BasePageDescriptor preparebuild() throws IOException {
        BasePageDescriptor bs = new BasePageDescriptor(getAddress(), getData().length);
        bs.setData(getData());
        return bs;
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        //getBasePagesFactory().getFulcrum().getTimeZone()
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
