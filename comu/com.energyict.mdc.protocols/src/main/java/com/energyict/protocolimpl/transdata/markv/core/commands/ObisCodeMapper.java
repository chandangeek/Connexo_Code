/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 5 september 2005, 14:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.transdata.markv.MarkV;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author koen
 */
public class ObisCodeMapper {

    MarkV markV;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(MarkV markV) {
        this.markV=markV;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        List rdis = RegisterIdentification.getRegisterDataIds();
        Iterator it = rdis.iterator();
        while(it.hasNext()) {
            RegisterDataId rdi = (RegisterDataId)it.next();
            if (rdi.getObisCode().equals(obisCode)) {
                return new RegisterInfo(rdi.getObisCode()+", "+rdi.getDescription());
            }
        }
        throw new NoSuchRegisterException("Register with ObisCode "+obisCode+" does not exist!");
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        List ris = markV.getCommandFactory().getCCCommand().findRegisterIdentifications(obisCode);

        if (ris.size() == 1) {
            RegisterIdentification ri = ((RegisterIdentification)ris.get(0));
            if (ri.getStrValue() != null)
                return new RegisterValue(obisCode,ri.getStrValue());
            else
                return new RegisterValue(obisCode,new Quantity(ri.getValue(),obisCode.getUnitElectricity(0)));
        }
        else if (ris.size() == 3) {
            Date date1=((RegisterIdentification)ris.get(1)).getDate();
            Date date2=((RegisterIdentification)ris.get(2)).getDate();
            Date eventDate = new Date(date1.getTime()+date2.getTime());
            return new RegisterValue(obisCode,new Quantity((((RegisterIdentification)ris.get(0)).getValue()),obisCode.getUnitElectricity(0)),eventDate);
        }
        else throw new NoSuchRegisterException("Register with ObisCode "+obisCode+" does not exist!");
    }

}
