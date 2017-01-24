/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class RegisterConfigurationBasePage extends AbstractBasePage {

/*
1D98 - Register 1, rate E mapping
1D99 - Register 1, TOU mapping
1D9A - Register 2, rate E mapping
1D9B - Register 2, TOU mapping
1D9C - Register 3, rate E mapping
1D9D - Register 4, rate E mapping
*/


    static List list = new ArrayList();
    static {
        list.add(new RegisterConfig(0,null,null, -1));
        list.add(new RegisterConfig(1, Unit.get("kWh"),"watthour energy", 1));
        list.add(new RegisterConfig(2,Unit.get("kvarh"),"reactive lagging energy", 5));
        list.add(new RegisterConfig(3,Unit.get("kvarh"),"reactive leading energy", 8));
        list.add(new RegisterConfig(4,Unit.get("kVAh"),"apparent lagging energy",9));
        list.add(new RegisterConfig(5,Unit.get("kW"),"demand",1));
        list.add(new RegisterConfig(6,Unit.get("kW"),"watt TOU demand",1));
        list.add(new RegisterConfig(7,Unit.get("kvar"),"reactive lagging demand",5));
        list.add(new RegisterConfig(8,Unit.get("kVA"),"apparent lagging demand",9));
        list.add(new RegisterConfig(9,Unit.get("kVA"),"apparent total demand",9));
    }

    private RegisterConfig register1RateEMapping;
    private RegisterConfig register1TOUMapping;
    private RegisterConfig register2RateEMapping;
    private RegisterConfig register2TOUMapping;
    private RegisterConfig register3RateEMapping;
    private RegisterConfig register4RateEMapping;



    /** Creates a new instance of RealTimeBasePage */
    public RegisterConfigurationBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterConfigurationBasePage:\n");
        strBuff.append("   register1RateEMapping="+getRegister1RateEMapping()+"\n");
        strBuff.append("   register1TOUMapping="+getRegister1TOUMapping()+"\n");
        strBuff.append("   register2RateEMapping="+getRegister2RateEMapping()+"\n");
        strBuff.append("   register2TOUMapping="+getRegister2TOUMapping()+"\n");
        strBuff.append("   register3RateEMapping="+getRegister3RateEMapping()+"\n");
        strBuff.append("   register4RateEMapping="+getRegister4RateEMapping()+"\n");
        return strBuff.toString();
    }


    private RegisterConfig findRegisterConfig(int id) throws IOException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            RegisterConfig rc = (RegisterConfig)it.next();
            if (rc.getId()==id)
                return rc;
        }
        throw new IOException("RegisterConfigurationBasePage, invalid id "+id);
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x1D98,6);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setRegister1RateEMapping(findRegisterConfig(ProtocolUtils.getInt(data,offset++,1)));
        setRegister1TOUMapping(findRegisterConfig(ProtocolUtils.getInt(data,offset++,1)));
        setRegister2RateEMapping(findRegisterConfig(ProtocolUtils.getInt(data,offset++,1)));
        setRegister2TOUMapping(findRegisterConfig(ProtocolUtils.getInt(data,offset++,1)));
        setRegister3RateEMapping(findRegisterConfig(ProtocolUtils.getInt(data,offset++,1)));
        setRegister4RateEMapping(findRegisterConfig(ProtocolUtils.getInt(data,offset++,1)));
    }

    public RegisterConfig getRegister1RateEMapping() {
        return register1RateEMapping;
    }

    public void setRegister1RateEMapping(RegisterConfig register1RateEMapping) {
        this.register1RateEMapping = register1RateEMapping;
    }

    public RegisterConfig getRegister1TOUMapping() {
        return register1TOUMapping;
    }

    public void setRegister1TOUMapping(RegisterConfig register1TOUMapping) {
        this.register1TOUMapping = register1TOUMapping;
    }

    public RegisterConfig getRegister2RateEMapping() {
        return register2RateEMapping;
    }

    public void setRegister2RateEMapping(RegisterConfig register2RateEMapping) {
        this.register2RateEMapping = register2RateEMapping;
    }

    public RegisterConfig getRegister2TOUMapping() {
        return register2TOUMapping;
    }

    public void setRegister2TOUMapping(RegisterConfig register2TOUMapping) {
        this.register2TOUMapping = register2TOUMapping;
    }

    public RegisterConfig getRegister3RateEMapping() {
        return register3RateEMapping;
    }

    public void setRegister3RateEMapping(RegisterConfig register3RateEMapping) {
        this.register3RateEMapping = register3RateEMapping;
    }

    public RegisterConfig getRegister4RateEMapping() {
        return register4RateEMapping;
    }

    public void setRegister4RateEMapping(RegisterConfig register4RateEMapping) {
        this.register4RateEMapping = register4RateEMapping;
    }


} // public class RealTimeBasePage extends AbstractBasePage
