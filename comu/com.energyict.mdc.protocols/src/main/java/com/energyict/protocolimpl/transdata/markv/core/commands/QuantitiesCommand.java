/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * QuantitiesCommand.java
 *
 * Created on 10 augustus 2005, 16:49
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.mdc.common.ObisCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author koen
 */
abstract public class QuantitiesCommand extends AbstractCommand {

    List registerIdentifications;
    private static final int QUANTITIES_STRING_LENGTH=12;
    abstract protected CommandIdentification getCommandIdentification();

    /** Creates a new instance of QuantitiesCommand */
    public QuantitiesCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("QuantitiesCommand "+getCommandIdentification()+"\n");
        for(int i=0;i<registerIdentifications.size();i++) {
            strBuff.append(registerIdentifications.get(i)+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(String strData) throws IOException {


//System.out.println(strData);

        BufferedReader br = new BufferedReader(new StringReader(strData));
        registerIdentifications = new ArrayList();
        while(true) {
            String register = br.readLine();
            if ((register == null) || (register.length() > QUANTITIES_STRING_LENGTH) || (register.getBytes()[0] == 0))
                break;
//           if (register.length() != QUANTITIES_STRING_LENGTH)
//                continue;
//            if ((register == null) || (register.length() != QUANTITIES_STRING_LENGTH))
//                break;;
            //parseRegister(register);
            RegisterIdentification gri = new RegisterIdentification(register,getCommandFactory().getMarkV().getTimeZone());
//System.out.println("parse "+register+" to "+gri);
            registerIdentifications.add(gri);
        } // while(true)
    }


    public int getNrOfGeneralRegisterIdentifications() {
        return registerIdentifications.size();
    }
    public RegisterIdentification getGeneralRegisterIdentification(int index) {
        return (RegisterIdentification)registerIdentifications.get(index);
    }

    public List findRegisterIdentifications(ObisCode obisCode) {
        List ris=new ArrayList();
        Iterator it = registerIdentifications.iterator();
        while(it.hasNext()) {
            RegisterIdentification ri = (RegisterIdentification)it.next();
            if (ri.getRegisterDataId().getObisCode().equals(obisCode)) {
                ris.add(ri);
            }
        }
        return ris;
    }

}
