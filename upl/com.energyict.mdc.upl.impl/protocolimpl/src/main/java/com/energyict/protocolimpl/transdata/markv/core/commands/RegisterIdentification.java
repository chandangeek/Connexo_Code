/*
 * RegisterIdentification.java
 *
 * Created on 1 september 2005, 11:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
/**
 *
 * @author Koen
 */
public class RegisterIdentification {
    
    
    private static final int MAX_NR_OF_CHANNELS=8;
    private static final int NR_OF_REGISTERTYPES=9;
    
    private int id;
    private boolean sign;
    private int format;
           
    private BigDecimal value;
    private Date date;
    private String strValue;
    
    private static final int FORMAT_TYPE_VOID=0;
    private static final int FORMAT_TYPE_NUMBER=1;
    private static final int FORMAT_TYPE_TD=2;
    
//    int[] FORMAT_TYPE = {0,0,1,1,1,1,1,0,
//                         0,1,1,1,1,1,2,2,
//                         2,2,2,2,2,2,2,2}; 
//    
//    int[] FORMAT_NROFDIGITS = {0,0,6,6,6,6,6,0,
//                               0,5,5,5,5,5,0,0,
//                               0,0,0,0,0,0,0,0};
                          
    // KV void type replaced by int 6 digits!
    int[] FORMAT_TYPE = {1,0,1,1,1,1,1,0,
                         0,1,1,1,1,1,2,2,
                         2,2,2,2,2,2,2,2}; 
    
    int[] FORMAT_NROFDIGITS = {6,0,6,6,6,6,6,0,
                               0,5,5,5,5,5,0,0,
                               0,0,0,0,0,0,0,0};
                               
    int[] FORMAT_TDMASK = {0,0,0,0,0,0,0,0,
                           0,0,0,0,0,0,123,321,
                           456,546,645,654,432,234,243,342};
                               
    int[] FORMAT_DECIMALS = {0,0,4,3,2,1,0,0,
                             0,4,3,2,1,0,0,0,
                             0,0,0,0,0,0,0,0};
                        
    //abstract protected List getRegisterDataIds();
    
    static List registerDataIds;
    static {
        registerDataIds = new ArrayList();
        registerDataIds.addAll(GeneralRegisterIdentification.getRegisterDataIds());
        registerDataIds.addAll(InstantaneousQuantitiesRegisterIdentification.getRegisterDataIds());
        registerDataIds.addAll(SelfReadRegisterIdentification.getRegisterDataIds());
        registerDataIds.addAll(TOURegisterIdentification.getRegisterDataIds());
        registerDataIds.addAll(OtherDataIds.getRegisterDataIds());
    }
                
    private RegisterDataId registerDataId;
    
    /** Creates a new instance of RegisterIdentification */
    static public List getRegisterDataIds() {
        return registerDataIds;
    }
    
    public RegisterIdentification(String strData, TimeZone timeZone) throws IOException {
        parse(strData, timeZone);
    }
    
    public String toString() {
       return getRegisterDataId().toString()+", value="+getValue()+", date="+getDate();
    }
    
    
    
    
    public boolean isVoidType() {
        return FORMAT_TYPE[getFormat()]==FORMAT_TYPE_VOID;
    }  
    public boolean isNumberType() {
        return FORMAT_TYPE[getFormat()]==FORMAT_TYPE_NUMBER;
    }  
    public boolean isTDType() {
        return FORMAT_TYPE[getFormat()]==FORMAT_TYPE_TD;
    }  
    
    private void parse(String strData, TimeZone timeZone) throws IOException {
        
//System.out.println(strData);

        setId(Integer.parseInt(strData.substring(0,3)));
        setSign(strData.indexOf("-")>=0);
        setId(getId()*(isSign()?-1:1));
        
        if (strData.length() >= 12)
           setFormat(Integer.parseInt(strData.substring(10,12)));
        
        setRegisterDataId(findRegisterDataId());
        
        // init values
        setDate(null);
        setValue(null);
        setStrValue(null);
        
        if (getRegisterDataId().getType() == RegisterDataId.STRING) {
            setStrValue(strData.substring(3,strData.length()));
        }
        else { // all other types follow the format?
            if (isNumberType()) {
                int temp = Integer.parseInt(strData.substring(3,9).trim());
                // KV_TO_DO, f(format) nr of digits can differ from 5 to 6...
                // Does this influences the received value?
                setValue(BigDecimal.valueOf(temp, FORMAT_DECIMALS[getFormat()]));
            }
            else if (isTDType()) {
                int temp;
                int field;
                Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);

                temp = Integer.parseInt(strData.substring(3,5));
                field = FORMAT_TDMASK[getFormat()]/100;
                buildCalendar(field,temp,cal);
                temp = Integer.parseInt(strData.substring(5,7));
                field = (FORMAT_TDMASK[getFormat()]%100)/10;
                buildCalendar(field,temp,cal);
                temp = Integer.parseInt(strData.substring(7,9));
                field = (FORMAT_TDMASK[getFormat()]%100)%10;
                buildCalendar(field,temp,cal);
                setDate(cal.getTime());
            }
            if (!(isTDRegisterTypeID() == isTDType()))
                throw new IOException("RegisterIdentification, parse(), Register ("+getRegisterDataId().getId()+") both registertype ("+getRegisterDataId().getType()+") and format ("+getFormat()+") must be of the same type!");
        }
        
    } // private void parse(String strData, TimeZone timeZone)

    
    private boolean isTDRegisterTypeID() {
        return (getRegisterDataId().getType()==RegisterDataId.TIME);
    }
    
    private RegisterDataId findRegisterDataId() throws IOException {
        Iterator it = registerDataIds.iterator();
        while(it.hasNext()) {
            RegisterDataId rdi = (RegisterDataId)it.next();
            if (rdi.getId()==getId()) {
                return rdi; 
            }
        }
        
        return new RegisterDataId(RegisterDataId.OTHER,RegisterDataId.LONG,getId(),-1, 0,0, "Unknown register, id="+getId());
        //throw new IOException("RegisterIdentification, findRegisterDataId(), no register data id found for id "+getId());
    }
    
    private void buildCalendar(int field, int val, Calendar cal) {
        switch(field) {
            case 1:
                cal.set(Calendar.SECOND,val);
            break;    
            case 2:
                cal.set(Calendar.MINUTE,val);
            break;    
            case 3:
                cal.set(Calendar.HOUR_OF_DAY,val);
            break;    
            case 4:
                cal.set(Calendar.DAY_OF_MONTH,val);
            break;    
            case 5:
                cal.set(Calendar.MONTH,val-1);
            break;    
            case 6:
                cal.set(Calendar.YEAR,val>=50?val+1900:val+2000);
            break;    
        }
    } // private void buildCalendar(int field, int val, Calendar cal)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    

    public RegisterDataId getRegisterDataId() {
        return registerDataId;
    }

    public void setRegisterDataId(RegisterDataId registerDataId) {
        this.registerDataId = registerDataId;
    }

    static public void main(String[] args) {
        try {
            
            
            RegisterIdentification ri = null; //new RegisterIdentification("005123456-05",TimeZone.getTimeZone("ECT"));
            
            
//            Iterator it = ri.registerDataIds.iterator();
//            while(it.hasNext()) {
//                RegisterDataId rdi = (RegisterDataId)it.next();
//                System.out.println(rdi);
//            }
            
            System.out.println(ri);
//            ri = new RegisterIdentification("024050810-19",TimeZone.getTimeZone("ECT"));
//            System.out.println(ri);
//            ri = new RegisterIdentification("301135200-15",TimeZone.getTimeZone("ECT"));
//            System.out.println(ri.getRegisterDataId().getObisCode().getUnitElectricity(0));
            ri = new RegisterIdentification("314090605 17",TimeZone.getTimeZone("ECT"));
            System.out.println(ri+", "+ri.getRegisterDataId().getObisCode().getUnitElectricity(0));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }
}
