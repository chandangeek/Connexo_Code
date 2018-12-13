/*
 * RegisterSet.java
 *
 * Created on 25 oktober 2004, 17:25
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class RegisterSet {

    private static final int NR_OF_REGISTERS = 32;
    Register[] registers = new Register[NR_OF_REGISTERS];
    int billingPoint;
    boolean used;
    TimeZone timeZone;

    /** Creates a new instance of RegisterSet */
    public RegisterSet(byte[] data,TimeZone timeZone) throws IOException {
        this.timeZone=timeZone;
        parse(data);
    }

    private void parse(byte[] data) throws IOException {
       String strData = new String(data);
       StringTokenizer strTok = new StringTokenizer(strData,"\r\n");
       while(strTok.hasMoreTokens()) {
           String strRegisterExpression = strTok.nextToken();
           if (strRegisterExpression.compareTo("([4])") != 0)
               parseRegisterExpression(strRegisterExpression);
       }
    }

    private void parseRegisterExpression(String strRegisterExpression) throws IOException {
       // (registerset,registernumber,registertype[,interval in minutes])(value.*kvar)([md timestamp yyyy,mm,dd,hh,mm,ss])([billing timestamp yyyy,mm,dd,hh,mm,ss])
       // OR
       // ([4]) means register is not existing
       //DataParser dp = new DataParser(TimeZoneManager.getTimeZone("GMT"));
       DataParser dp = new DataParser(timeZone);
       String str = dp.parseBetweenBrackets(strRegisterExpression,0);
       billingPoint = Integer.parseInt(str.split(",")[0]);
       int id = Integer.parseInt(str.split(",")[1])-1;
       int type = Integer.parseInt(str.split(",")[2]);

       str = dp.parseBetweenBrackets(strRegisterExpression,1);
       if (str.length()!=0) {
           String acronym = str.split("\\*")[1];
           String val = str.split("\\*")[0];

           Quantity quantity = new Quantity(new BigDecimal(val),Unit.get(acronym));
           Date mdTimestamp=null;
           try {
              mdTimestamp = dp.parseDateTime(dp.parseBetweenBrackets(strRegisterExpression,2));
           }
           catch(DataParseException e) {
              mdTimestamp = null;
           }
           Date billingTimestamp=null;
           try {
              billingTimestamp = dp.parseDateTime(dp.parseBetweenBrackets(strRegisterExpression,3));
           }
           catch(DataParseException e) {
              billingTimestamp = null;
           }
           registers[id] = new Register(type,quantity,mdTimestamp,billingTimestamp);
           used = true;
       }
       else used = false;
    }







    // methods to search for a certain register starting from an obiscode

    public String toString() {
        if (isUsed()) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("RegisterSet "+getBillingPoint()+"\n");
            for (int i=0;i< registers.length ; i++) {
                if (registers[i] != null)
                    strBuff.append("register "+i+", "+registers[i].toString()+"\n");
            }
            return strBuff.toString();
        }
        else return "RegisterSet "+getBillingPoint()+" is not used\n";
    }


    /**
     * Getter for property registers.
     * @return Value of property registers.
     */
    public com.energyict.protocolimpl.iec1107.enermete70x.Register[] getRegisters() {
        return this.registers;
    }
    public Register getRegister(int index) throws NoSuchRegisterException {
        if ((index >= NR_OF_REGISTERS) || (index < 0))
            throw new NoSuchRegisterException("RegisterSet, getRegister, register with id "+index+" invalid!");
        if (registers[index]==null)
            throw new NoSuchRegisterException("RegisterSet, getRegister, register with id "+index+" invalid!");
        return registers[index];
    }

    static public void main(String[] args) {
        try {
           RegisterSet registerSet = new RegisterSet(ProtocolUtils.readFile("GTR.txt"),TimeZone.getTimeZone("GMT"));
           System.out.println(registerSet.toString());
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for property billingPoint.
     * @return Value of property billingPoint.
     */
    public int getBillingPoint() {
        return billingPoint;
    }

    /**
     * Getter for property used.
     * @return Value of property used.
     */
    public boolean isUsed() {
        return used;
    }



}
