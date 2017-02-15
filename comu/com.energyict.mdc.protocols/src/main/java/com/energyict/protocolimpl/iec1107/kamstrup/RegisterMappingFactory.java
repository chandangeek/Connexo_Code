/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterMappingFactory.java
 *
 * Created on 15 september 2006, 14:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.kamstrup;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class RegisterMappingFactory {

    List registerMappings= new ArrayList();

    /** Creates a new instance of RegisterMappingFactory */
    public RegisterMappingFactory() {
        initRegisterMapping();
    }

    private void initRegisterMapping() {

        getRegisterMappings().add(new RegisterMapping("Converter Unconverted volume (Ch.1) Vm1","1:13.0.0", ObisCode.fromString("7.1.0.3.0.255")));
        getRegisterMappings().add(new RegisterMapping("Converter Unconverted volume (Ch.2) Vm2","2:13.0.0", ObisCode.fromString("7.2.0.3.0.255")));
        getRegisterMappings().add(new RegisterMapping("converter error corrected volume (Ch.1) Vc","13.1.0", ObisCode.fromString("7.1.96.50.0.255")));
        getRegisterMappings().add(new RegisterMapping("Converter converted volume (Ch.1) Vb","23.2.0", ObisCode.fromString("7.1.0.3.3.255")));
        getRegisterMappings().add(new RegisterMapping("Measured, disturbed volume, Ve","1:12.0.0", ObisCode.fromString("7.1.96.50.1.255")));
        getRegisterMappings().add(new RegisterMapping("Measured Temperature","0:41.0.0", ObisCode.fromString("7.1.0.6.2.255")));
        getRegisterMappings().add(new RegisterMapping("Measured absolute pressure","0:42.0.0", ObisCode.fromString("7.1.0.6.1.255")));
        getRegisterMappings().add(new RegisterMapping("Conversion factor","0:52.0.0", ObisCode.fromString("7.1.0.4.0.255")));
        getRegisterMappings().add(new RegisterMapping("Correction factor","0:51.0.0", ObisCode.fromString("7.1.0.4.1.255")));
        getRegisterMappings().add(new RegisterMapping("Compressibility factor","0:53.0.0", ObisCode.fromString("7.1.0.4.2.255")));
        getRegisterMappings().add(new RegisterMapping("Actual normalised flow 5 minutes avg.","1:43.0.0", ObisCode.fromString("7.1.96.50.2.255")));
        getRegisterMappings().add(new RegisterMapping("Actual normalised flow 60 minutes avg.","2:43.0.0", ObisCode.fromString("7.1.96.50.3.255")));

        getRegisterMappings().add(new RegisterMapping("Unigas Error code","97.97.0", ObisCode.fromString("7.1.96.50.4.255")));

        getRegisterMappings().add(new RegisterMapping("Time","0.9.1", ObisCode.fromString("7.1.96.50.5.255")));
        getRegisterMappings().add(new RegisterMapping("Date","0.9.2", ObisCode.fromString("7.1.96.50.6.255")));
        getRegisterMappings().add(new RegisterMapping("TimeDate","0.9.1 0.9.2", ObisCode.fromString("7.1.96.50.7.255")));
        getRegisterMappings().add(new RegisterMapping("1107 device address","C.90.1", ObisCode.fromString("7.1.96.50.8.255")));
        getRegisterMappings().add(new RegisterMapping("UNIGAS software revision number","C.90.2", ObisCode.fromString("7.1.96.50.9.255")));
        getRegisterMappings().add(new RegisterMapping("CI software revision number","C.90.3", ObisCode.fromString("7.1.96.50.10.255")));

        getRegisterMappings().add(new RegisterMapping("actual status bits","C.5", ObisCode.fromString("7.1.96.50.11.255")));
    }

    public List getRegisterMappings() {
        return registerMappings;
    }

    private void setRegisterMappings(List registerMappings) {
        this.registerMappings = registerMappings;
    }

    public String findRegisterCode(ObisCode obisCode) throws IOException {
        Iterator it = getRegisterMappings().iterator();
        while(it.hasNext()) {
            RegisterMapping rm = (RegisterMapping)it.next();
            if (rm.getObisCode().equals(obisCode))
                return rm.getRegisterCode();
        }

        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    } // public String findRegisterCode(ObisCode obisCode) throws IOException

    public RegisterMapping findRegisterMapping(ObisCode obisCode) throws IOException {
        Iterator it = getRegisterMappings().iterator();
        while(it.hasNext()) {
            RegisterMapping rm = (RegisterMapping)it.next();
            if (rm.getObisCode().equals(obisCode))
                return rm;
        }

        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    } // public String findRegisterCode(ObisCode obisCode) throws IOException
}
