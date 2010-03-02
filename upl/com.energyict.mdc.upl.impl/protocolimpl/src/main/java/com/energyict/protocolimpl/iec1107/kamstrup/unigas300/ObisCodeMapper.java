/*
 * ObisCodeMapper.java
 *
 * Created on 15 september 2006, 13:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import java.io.IOException;
import java.util.Iterator;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.vdew.DateQuantityPair;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

	private static final RegisterMappingFactory	REGISTER_MAPPING_FACTORY	= new RegisterMappingFactory();
	private Unigas300 unigas300;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Unigas300 unigas300) {
        this.unigas300=unigas300;
    }


    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = REGISTER_MAPPING_FACTORY.getRegisterMappings().iterator();
        while(it.hasNext()) {
            RegisterMapping rm = (RegisterMapping)it.next();
            strBuff.append(rm.getObisCode()+", "+rm.getDescription()+", "+rm.getRegisterCode()+"\n");

        }
        return strBuff.toString();
    }

	public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        RegisterMapping rm = REGISTER_MAPPING_FACTORY.findRegisterMapping(obisCode);
        return new RegisterInfo(rm.getDescription()+", "+rm.getRegisterCode());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        String registerCode = REGISTER_MAPPING_FACTORY.findRegisterCode(obisCode);
        Object o = unigas300.getUnigas300Registry().getRegister(registerCode);
        if (o instanceof Quantity) {
            return new RegisterValue(obisCode,(Quantity)o);
        }
        else if (o instanceof Integer) {
            return new RegisterValue(obisCode,new Quantity((Integer)o,Unit.get("")));
        }
        else if (o instanceof String) {
            return new RegisterValue(obisCode,(String)o);
        }
        else if (o instanceof DateQuantityPair) {
            return new RegisterValue(obisCode,((DateQuantityPair)o).getQuantity(), ((DateQuantityPair)o).getDate());
        }

        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

}
