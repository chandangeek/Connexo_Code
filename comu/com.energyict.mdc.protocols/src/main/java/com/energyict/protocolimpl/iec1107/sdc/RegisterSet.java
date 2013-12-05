/*
 * RegisterSet.java
 *
 * Created on 25 oktober 2004, 17:25
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.cbo.TimeZoneManager;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.base.DataParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.NoSuchObjectException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;
/**
 *
 * @author  gna
 * <B>@Beginchanges</B><BR>
 * GN|29012008| created the parsing of the registerdump
 * @Endchanges
 */
public class RegisterSet {

    private static final int NR_OF_REGISTERS = 32;
    Register[] registers = new Register[NR_OF_REGISTERS];
    int billingPoint;
    boolean used = false;
    TimeZone timeZone;
    Calendar calendar;
	private Date mdTimestamp = null;
	private Date billingTimestamp = null;
    int id = 0;
    int dateType = 0;

    /** Creates a new instance of RegisterSet */
    public RegisterSet(byte[] data,TimeZone timeZone) throws IOException {
        this.timeZone=timeZone;
        parse(data);
    }

    private void parse(byte[] data) throws IOException {
       String strData = new String(data);
       strData = strData.substring(1, strData.length()-1);
       StringTokenizer strTok = new StringTokenizer(strData,"\r\n");
       calendar = Calendar.getInstance(timeZone);
       while(strTok.hasMoreTokens()) {
           String strRegisterExpression = strTok.nextToken();
           if (strRegisterExpression.compareTo("([4])") != 0) {
			parseRegisterExpression(strRegisterExpression);
		}
       }
    }

    private void parseRegisterExpression(String strRegisterExpression) throws IOException {

    	DataParser dp = new DataParser(TimeZoneManager.getTimeZone("GMT"));
    	String str = dp.parseBetweenBrackets(strRegisterExpression,0);
    	int type;
    	String typeString = strRegisterExpression.substring(0,strRegisterExpression.indexOf('('));
    	typeString = checkStringForNumbers(typeString);

    	if ( typeString.compareTo("DIAG") == 0 ) {
			dateType = 1;
		} else if ( typeString.substring(0, 2).compareTo("DL") == 0 ){
    		dateType = 2;
    		setDates(dateType, typeString, str);
    	}

    	else{
//    		// Tariff Program
//    		if ( typeString.compareTo("PR") == 0 ){}

    		// Serial Number
    		if ( ( typeString.compareTo("NS") == 0 ) | ( Integer.parseInt(typeString) == 99 ) ){
    			type = getType(typeString);
    			Quantity qu = new Quantity(new BigDecimal(str),Unit.getUndefined());
    			registers[id] = new Register(type,qu,null,null); id++;
    		}

//    		// TI Ratio
//    		else if ( ( Integer.parseInt(typeString) == 90 ) ){}

    		// a date or a registervalue
    		else {

    			if (dateType == 1) {
					setDates(dateType, typeString, str);
				}

    			if ( (used) & (Integer.parseInt(typeString) != 66) & ( typeString.substring(0, 2).compareTo("DL") != 0 ) ){
    				type = getType(typeString);

        			String acronym = str.split("\\*")[1];

        			if (acronym.compareToIgnoreCase("varh") == 0) {
						acronym = "varh";
					}

        			String val = str.split("\\*")[0];

        			Quantity quantity = new Quantity(new BigDecimal(val),Unit.get(acronym));

                   	registers[id] = new Register(type, quantity, mdTimestamp, billingTimestamp);
                   	id ++;
//        			used = true;
    			}

    		}
//        	else used = false;
    	}

    }

    private String checkStringForNumbers(String typeString) {
    	int checker = 0;
    	int[] place = {0,0,0,0,0};
    	for(int i=0; i<typeString.length(); i++){
    		boolean check = Character.isDigit(typeString.charAt(i));

    		if(!check){
    			place[checker] = i;
    			checker++;
    		}
    	}

    	if ( place [0] == 2 ) {
			typeString = typeString.substring(0, 2) + Integer.parseInt(typeString.substring(2, 3),16);
		}

		return typeString;
	}

	private int getType(String str) throws NoSuchObjectException{

    	int type = Integer.parseInt(str);

    	if ( (type >= 67) & (type <= 99) ){
    		billingPoint = 0;
    		return type;
    	}

    	else if ( (type >= 671) & (type <= 839) ){
    		billingPoint = type % 10;
    		return type/10;
    	}
    	else if ( (type >= 6710) & (type <= 8316) ){
    		billingPoint = type % 100;
    		return type/100;
    	} else {
			throw new NoSuchObjectException("Type "+str+" is not supported");
		}

    }

    private void setDates(int type, String typeString, String str){
		if ( (dateType == 1) & !used ){
    		// Time
    		if ( ( typeString.compareTo("REL") == 0 ) | ( Integer.parseInt(typeString) == 65) ){
    			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str.substring(0, 2)));
    			calendar.set(Calendar.MINUTE, Integer.parseInt(str.substring(2, 4)));
    			calendar.set(Calendar.SECOND, Integer.parseInt(str.substring(4, 6)));
    		}

    		// Date
    		else if ( ( typeString.compareTo("CAL") == 0 ) | ( Integer.parseInt(typeString) == 66) ){
    			calendar.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
    			calendar.set(Calendar.MONTH, ( Integer.parseInt(str.substring(4, 6)) -1 ));
    		   	calendar.set(Calendar.DATE, Integer.parseInt(str.substring(6, 8)));
    		   	mdTimestamp = calendar.getTime();
    		   	billingTimestamp = calendar.getTime();
    		   	used = true;
    		}
		}

		else if ( (dateType == 2) & !used ){

			calendar.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
			calendar.set(Calendar.MONTH, ( Integer.parseInt(str.substring(5, 7)) -1 ));
			calendar.set(Calendar.DATE, Integer.parseInt(str.substring(8, 10)));

			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str.substring(11, 13)));
			calendar.set(Calendar.MINUTE, Integer.parseInt(str.substring(14, 16)));
			calendar.set(Calendar.SECOND, Integer.parseInt(str.substring(17, 19)));

		   	mdTimestamp = Calendar.getInstance(timeZone).getTime();
		   	billingTimestamp = calendar.getTime();
			used = true;
		}
    }


    // methods to search for a certain register starting from an obiscode

    public String toString() {
        if (isUsed()) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("RegisterSet "+getBillingPoint()+"\n");
            for (int i=0;i< registers.length ; i++) {
                if (registers[i] != null) {
					strBuff.append("register "+i+", "+registers[i].toString()+"\n");
				}
            }
            return strBuff.toString();
        } else {
			return "RegisterSet "+getBillingPoint()+" is not used\n";
		}
    }


    /**
     * Getter for property registers.
     * @return Value of property registers.
     */
    public com.energyict.protocolimpl.iec1107.sdc.Register[] getRegisters() {
        return this.registers;
    }
    public Register getRegister(int index) throws NoSuchRegisterException {
    	index = getRegisterIndex(index);

        if ((index >= NR_OF_REGISTERS) || (index < 0)) {
			throw new NoSuchRegisterException("RegisterSet, getRegister, register with id "+index+" invalid!");
		}
        if (registers[index]==null) {
			throw new NoSuchRegisterException("RegisterSet, getRegister, register with id "+index+" invalid!");
		}
        return registers[index];
    }

    private int getRegisterIndex(int typeNumber) throws NoSuchRegisterException{
    	int number = -1;
    	int teller = 0;
//    	do{
//    		if (registers[teller].type == typeNumber)
//    			number = teller;
//    		teller++;
//    	}while( (number == -1) & (registers[teller] != null));

    	for(teller = 0; teller  < registers.length; teller++){
    		if(registers[teller] != null){
    			if(registers[teller].type == typeNumber) {
					return teller;
				}
    		}
    	}

    	throw new NoSuchRegisterException("RegisterSet, getRegisterIndex, typeNumber not found in registerset.");
    }

    static public void main(String[] args) {
        try {
           RegisterSet registerSet = new RegisterSet(com.energyict.protocol.ProtocolUtils.readFile("GTR.txt"),TimeZone.getTimeZone("GMT"));
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
