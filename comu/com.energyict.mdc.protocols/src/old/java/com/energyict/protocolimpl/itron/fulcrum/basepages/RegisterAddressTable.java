/*
 * RegisterAddressTable.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class RegisterAddressTable extends AbstractBasePage {

    final int MAX_NR_OF_SELFREAD_REGISTERS=100;
    List selfReadRegisterAddress;

    /** Creates a new instance of RegisterAddressTable */
    public RegisterAddressTable(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterAddressTable:\n");
        for (int i=0;i<getSelfReadRegisterAddress().size();i++) {
            strBuff.append("       selfReadRegisterAddress "+i+" ="+(SelfReadRegisterAddress)getSelfReadRegisterAddress().get(i)+"\n");
        }
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x25EC, 100*5);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        selfReadRegisterAddress = new ArrayList();
        for(int i=0;i<MAX_NR_OF_SELFREAD_REGISTERS;i++) {
            SelfReadRegisterAddress s =new SelfReadRegisterAddress(data,offset);
            if (s.getRegisterAddress() == 0xFFFF) break;
            selfReadRegisterAddress.add(s);
            offset+=SelfReadRegisterAddress.size();
        }
    }

    public List getSelfReadRegisterAddress() {
        return selfReadRegisterAddress;
    }

    private void setSelfReadRegisterAddress(List selfReadRegisterAddress) {
        this.selfReadRegisterAddress = selfReadRegisterAddress;
    }


} // public class RealTimeBasePage extends AbstractBasePage
