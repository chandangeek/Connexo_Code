/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractRegisterFactory.java
 *
 * Created on 30 maart 2007, 16:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public abstract class AbstractRegisterFactory {

    protected abstract void init();

    List registers = new ArrayList();

    private Modbus modBus;
    private FunctionCodeFactory functionCodeFactory;
    private boolean zeroBased;

    ParserFactory parserFactory=null;

    protected void initParsers() {
        // default parsers
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val |= values[i]<<(i*16);
                }
                return BigDecimal.valueOf(val);
            }
        });
    } //private void initParsers()

    protected List getRegisters() {
        return registers;
    }
    /**
     * Creates a new instance of AbstractRegisterFactory
     */
    public AbstractRegisterFactory(Modbus modBus) {
        this.setModBus(modBus);
        setFunctionCodeFactory(new FunctionCodeFactory(modBus));
        initParsers();
        init();
    }

    public AbstractRegister findRegister(String name) throws IOException {
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            AbstractRegister register = (AbstractRegister)it.next();
            if (register.getName().compareTo(name)==0) {
                register.setRegisterFactory(this);
                return register;
            }
        }
        throw new NoSuchRegisterException("Register reg name "+name+" is not supported!");
    }

    public AbstractRegister findRegister(int reg) throws IOException {
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            AbstractRegister register = (AbstractRegister)it.next();
            if (register.getReg()==reg) {
                register.setRegisterFactory(this);
                return register;
            }
        }
        throw new NoSuchRegisterException("Register reg id "+reg+" is not supported!");
    }

    public AbstractRegister findRegister(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            AbstractRegister register = (AbstractRegister)it.next();

            if ((register.getObisCode()!=null) && (register.getObisCode().equals(obisCode))) {
                register.setRegisterFactory(this);
                return register;
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    } // public HoldingRegister findRegister(ObisCode obc) throws IOException

    public Modbus getModBus() {
        return modBus;
    }

    public void setModBus(Modbus modBus) {
        this.modBus = modBus;
    }

    public FunctionCodeFactory getFunctionCodeFactory() {
        return functionCodeFactory;
    }

    public void setFunctionCodeFactory(FunctionCodeFactory functionCodeFactory) {
        this.functionCodeFactory = functionCodeFactory;
    }

    public boolean isZeroBased() {
        return zeroBased;
    }

    public void setZeroBased(boolean zeroBased) {
        this.zeroBased = zeroBased;
    }

    public ParserFactory getParserFactory() {
        if (parserFactory == null)
            parserFactory = new ParserFactory();
        return parserFactory;
    }
}
