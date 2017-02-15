/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SEVCRegisterFactory.java
 *
 * Created on 17 juni 2003, 11:33
 */

package com.energyict.protocolimpl.actarissevc;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.ProtocolException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author  Koen
 */
public class SEVCRegisterFactory {

    private Map registers = new HashMap();

    /** Creates a new instance of SEVCRegisterFactory */
    public SEVCRegisterFactory() {
        initRegisters();
        initLocals();
    }

    protected Map getRegisters() {
       return registers;
    }

    private void initRegisters() {
       registers.put("IVCA", new SEVCRegister("PVA",6,0,16,SEVCDataParse.SEVC_VOLUME,Unit.get("m3")));
       registers.put("IVBA", new SEVCRegister("PVA",6,16,16,SEVCDataParse.SEVC_VOLUME,Unit.get("m3")));
       registers.put("CVBA", new SEVCRegister("PVA",6,32,16,SEVCDataParse.SEVC_VOLUME,Unit.get("m3")));
       registers.put("AL_COURS", new SEVCRegister("PVA",6,48,4,SEVCDataParse.SEVC_BINARY_MASK,Unit.get("m3")));
       registers.put("AL_MEMO", new SEVCRegister("PVA",6,50,4,SEVCDataParse.SEVC_BINARY_MASK,null));

       registers.put("IVC", new SEVCRegister("PVN",7,0,16,SEVCDataParse.SEVC_VOLUME,Unit.get("m3")));
       registers.put("IVB", new SEVCRegister("PVN",7,16,16,SEVCDataParse.SEVC_VOLUME,Unit.get("m3")));
       registers.put("CVC", new SEVCRegister("PVN",7,32,16,SEVCDataParse.SEVC_VOLUME,Unit.get("m3")));
       registers.put("PIM", new SEVCRegister("PVN",7,48,2,SEVCDataParse.SEVC_INTEGER_BINARY,null));

       registers.put("Ttest", new SEVCRegister("PTE",1,0,8,SEVCDataParse.SEVC_FLOATING_POINT,Unit.get("°C")));
       registers.put("Ptest", new SEVCRegister("PTE",1,8,8,SEVCDataParse.SEVC_FLOATING_POINT,Unit.get("bar")));
       registers.put("Ctest", new SEVCRegister("PTE",1,16,8,SEVCDataParse.SEVC_FLOATING_POINT,null));
       registers.put("Ztest", new SEVCRegister("PTE",1,24,8,SEVCDataParse.SEVC_FLOATING_POINT,null));
       registers.put("Qtest", new SEVCRegister("PTE",1,32,8,SEVCDataParse.SEVC_FLOATING_POINT,Unit.get("m3/h")));

       registers.put("HOV", new SEVCRegister("PCD",18,0,2,SEVCDataParse.SEVC_BYTE,Unit.get(BaseUnit.MINUTE)));
       registers.put("SAV", new SEVCRegister("PCD",18,2,4,SEVCDataParse.SEVC_SHORT,Unit.get(BaseUnit.MINUTE)));

    }

    private void initLocals() {
        Iterator iterator = registers.values().iterator();
        while(iterator.hasNext()) {
            SEVCRegister reg = (SEVCRegister)iterator.next();
            reg.setSEVCRegisterFactory(this);
        }
    }

    public SEVCRegister init(String str) {
       return (SEVCRegister)registers.get(str);
    }

    public Number getValue(String str,SEVCIEC1107Connection sevciec1107Connection) throws ProtocolException,IOException {
      SEVCRegister register = (SEVCRegister)registers.get(str);
      if (register == null) throw new ProtocolException("SEVC: register "+str+" does not exist!");
      return register.doGetMeterRegisterValue(register,sevciec1107Connection);
    }

}
