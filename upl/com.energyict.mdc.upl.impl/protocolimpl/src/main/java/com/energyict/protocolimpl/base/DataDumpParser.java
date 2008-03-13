/*
 * DataDumpParser.java
 *
 * Created on 24 januari 2005, 14:12
 */

package com.energyict.protocolimpl.base;

import java.io.*;
import java.util.*;
import java.math.*;
import java.text.*;

import com.energyict.protocol.*;
import com.energyict.cbo.*;

/**
 *
 * @author  Koen
 */
public class DataDumpParser {
    
    String strFrame;
    
    static public final String ENERMET_DUMP_DATETIME_SIGNATURE="yy-MM-dd HH:mm";
    String datetimeSignature;
    
    /** Creates a new instance of SCTMDumpData */
    public DataDumpParser(byte[] frame) throws IOException {
        this(frame,ENERMET_DUMP_DATETIME_SIGNATURE);
    }
    public DataDumpParser(byte[] frame,String datetimeSignature) throws IOException {
        this.datetimeSignature=datetimeSignature;
        if (frame == null) throw new IOException("SCTMDumpData, frame data == null!");
        strFrame = new String(frame);
    }
    
    public String toString() {
        return strFrame;
    }

    public String getRegisterFFStrValue(String strReg) throws IOException {
        String strRegister=strReg;
        String strValue = searchRegister(strRegister);
        return strValue.substring(0,strValue.indexOf("\r"));
    }
    
    public String getRegisterStrValue(String strReg) throws IOException {
        String strRegister="\n"+strReg;
        String strValue = searchRegister(strRegister);
        return strValue.substring(0,strValue.indexOf("\r"));
//        String strBaseRegister = findBaseRegister(strRegister);
//        if (strBaseRegister != null) {
//           return searchRegister(strBaseRegister);
//        }
//        else return strValue;
    }
    
    public Date getRegisterDateTime(String strReg, TimeZone timeZone) throws IOException {
        String strRegister="\n"+strReg;
        strRegister=searchRegister(strRegister);
        strRegister = strRegister.substring(1,strRegister.indexOf("\r"));
        if (datetimeSignature.compareTo(ENERMET_DUMP_DATETIME_SIGNATURE)==0) {
            try {
                
                if (strRegister.length() < (datetimeSignature.length()+1))
                    return null;
                
                strRegister = strRegister.substring(strRegister.length()-(datetimeSignature.length()+1),strRegister.length()-1);
                return ProtocolUtils.parseDateTimeWithTimeZone(strRegister, datetimeSignature, timeZone);
            }
            catch(ParseException e) {
                return null;
            }
        }
        else {
            throw new IOException("SCTMDumpData, getRegisterDateTime, Invalid datetime signature ("+datetimeSignature+")");
        }
    }
    
    
    public int getBillingCounter() {
        
        int billingCounter=0;
        int index1=-1;
        
        while(true) {
            index1 = strFrame.indexOf('*',index1+1);
            if (index1 == -1) break;
            int index2 = strFrame.indexOf('(',index1);


            try {
                int value = Integer.parseInt(strFrame.substring(index1+1,index2));
                if (value>billingCounter) billingCounter = value;     
            }
            catch(NumberFormatException e) {
                // absorb
            }
        }
        
        return billingCounter;
    }
    
    
    public Quantity getRegister(String strReg) throws IOException {
        String strRegister="\n"+strReg;
        Quantity quantity = parseQuantity(searchRegister(strRegister));
        String strBaseRegister = findBaseRegister(strRegister);
        if (strBaseRegister != null) {
           Quantity quantityUnit = parseQuantity(searchRegister(strBaseRegister));
           return new Quantity(quantity.getAmount(),quantityUnit.getUnit());
        }
        else return quantity;
    }
    
    private String findBaseRegister(String strRegister) throws IOException {
        if (hasBaseRegister(strRegister,".&")) return doGetBaseRegister(strRegister,".&"); // KV 04102004
        if (hasBaseRegister(strRegister,".*")) return doGetBaseRegister(strRegister,".*"); // KV 04102004
        if (hasBaseRegister(strRegister,"&")) return doGetBaseRegister(strRegister,"&");
        if (hasBaseRegister(strRegister,"*")) return doGetBaseRegister(strRegister,"*");
        return null;
    }
    
    private boolean hasBaseRegister(String strRegister,String strDelimiter) {
        if (strRegister.indexOf(strDelimiter) != -1) return true;
        else return false;
    }
    
    private String doGetBaseRegister(String strRegister,String strDelimiter) throws IOException {
        //StringTokenizer st = new StringTokenizer(strRegister,strDelimiter);
        //return st.nextToken();
        // KV 04102004
        return strRegister.substring(0,strRegister.indexOf(strDelimiter));
    }
    
    private String searchRegister(String strRegister) throws IOException {
       int iIndex = strFrame.indexOf(strRegister+"(");
       if (iIndex == -1) throw new NoSuchRegisterException("DataDumpParser, searchRegister, register not found");       
       return strFrame.substring(iIndex);//-1);
    }
    
    private Quantity parseQuantity(String val) throws IOException {
       StringBuffer stringBufferVal = new StringBuffer();
       StringBuffer stringBufferUnit = new StringBuffer();
       int state=0;
       for (int i=0;i<val.length();i++) {
           if (val.charAt(i) == '(') state=1;
           else if (val.charAt(i) == '*') state=2;
           else if (val.charAt(i) == ')') break;
           else if (((val.charAt(i) >= '0') && (val.charAt(i) <= '9')) || (val.charAt(i) == '.')) {
               if (state==1) stringBufferVal.append(val.charAt(i)); 
           }
           else {
               if (state==2) stringBufferUnit.append(val.charAt(i)); 
           }
       } // for (i=0;i<frame.length;i++)
       
       Quantity quantity = new Quantity(new BigDecimal(stringBufferVal.toString()), 
                               Unit.get(stringBufferUnit.toString()));
       return quantity;
    }
    
    static public byte[] readFile() {
        try {
            File file = new File("zmd.txt");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();
            return data;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) {
        try {
            //String str = new String("1-1:F.F(00)\r\n1-1:0.0.0(62127388)\r\n1-1:0.1.0(01)\r\n1-1:1.2.1(00000.0*kW)\r\n1-1:1.2.2(00000.0*kW)\r\n1-1:1.2.3(00000.0*kW)\r\n1-1:1.2.4(00000.0*kW)\r\n1-1:1.6.1(000.0*kW)\r\n1-1:1.6.2(000.0*kW)\r\n1-1:1.6.3(000.0*kW)\r\n1-1:1.6.4(000.0*kW)\r\n1-1:1.8.1(0002000.030*kWh)\r\n1-1:1.8.2(0000000.0*kWh)\r\n1-1:1.8.3(0000000.0*kWh)\r\n1-1:1.8.4(0000000.0*kWh)\r\n1-1:5.8.1(9000000.123*kvarh)\r\n1-1:5.8.2(0000000.0*kvarh)\r\n1-1:5.8.3(0000000.0*kvarh)\r\n1-1:5.8.4(0000000.0*kvarh)\r\n1-1:8.8.1(0000000.0*kvarh)\r\n1-1:8.8.2(0000000.0*kvarh)\r\n1-1:8.8.3(0000000.0*kvarh)\r\n1-1:8.8.4(0000000.0*kvarh)\r\n!\r\n");
            //String str = new String("9.2(123456789*Wh)\r\n10.2(0001234*kWh)\r\n10.2&01(1234.567)\r\n9.2*00(0.56789)\r\n10.2*99(0000000)\r\n10.2*98(0000000)\r\n10.2*97(0000000)");
            
            byte[] frame = readFile();
            DataDumpParser dumpData = new DataDumpParser(frame);
//            q = dumpData.getRegister("8.1"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
//            System.out.println(q.toString());
//            q = dumpData.getRegister("8.1.&22"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
//            System.out.println(q.toString());
//            q = dumpData.getRegister("8.1"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
//            System.out.println(q.toString());
            
//            q = dumpData.getRegister("1-1:1.6.1*02");
//            System.out.println(q.toString());
//            System.out.println(dumpData.getRegisterStrValue("1-1:1.6.1*02"));
//            
//            q = dumpData.getRegister("1-1:1.6.3");
//            System.out.println("1-1:1.6.3 --> "+q.toString());
//            
            String str = dumpData.getRegisterStrValue("1.6.3*06");
            Quantity q = dumpData.getRegister("1.6.3*06");
            Date d = dumpData.getRegisterDateTime("1.6.3*06",TimeZone.getTimeZone("ECT"));
            System.out.println("1.6.3*06 --> "+str+", q="+q+", d="+d);
            
            str = dumpData.getRegisterStrValue("1.6.3*6");
            q = dumpData.getRegister("1.6.3*6");
            d = dumpData.getRegisterDateTime("1.6.3*6",TimeZone.getTimeZone("ECT"));
            System.out.println("1.6.3*6 --> "+str+", q="+q+", d="+d);

            
            str = dumpData.getRegisterStrValue("1.6.3");
            q = dumpData.getRegister("1.6.3");
            d = dumpData.getRegisterDateTime("1.6.3",TimeZone.getTimeZone("ECT"));
            System.out.println("1.6.3 --> "+str+", q="+q+", d="+d);

            str = dumpData.getRegisterStrValue("0.1.0");
            q = dumpData.getRegister("0.1.0");
            d = dumpData.getRegisterDateTime("0.1.0",TimeZone.getTimeZone("ECT"));
            System.out.println("0.1.0 --> "+str+", q="+q+", d="+d);
            
            str = dumpData.getRegisterStrValue("0.1.0*06");
            q = dumpData.getRegister("0.1.0*06");
            d = dumpData.getRegisterDateTime("0.1.0*06",TimeZone.getTimeZone("ECT"));
            System.out.println("0.1.0*06 --> "+str+", q="+q+", d="+d);

            str = dumpData.getRegisterStrValue("0.1.0*6");
            q = dumpData.getRegister("0.1.0*6");
            d = dumpData.getRegisterDateTime("0.1.0*6",TimeZone.getTimeZone("ECT"));
            System.out.println("0.1.0*6 --> "+str+", q="+q+", d="+d);
            
            
            //str = dumpData.getRegisterFFStrValue("F.F");
            //System.out.println("F.F --> "+str);
//            q = dumpData.getRegister("20"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
//            System.out.println(q.toString());
//            q = dumpData.getRegister("20.*22"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
//            System.out.println(q.toString());
//            Date date = dumpData.getRegisterDateTime("8.1",TimeZone.getTimeZone("WET"));
//            System.out.println(date);
//            
//            System.out.println("billingCounter = "+dumpData.getBillingCounter());
//            
//            q = dumpData.getRegister("10.2&01"); //dumpData.getRegister("1-1:5.8.1");
//            System.out.println(q.toString());
//            q = dumpData.getRegister("9.2*00"); //dumpData.getRegister("1-1:5.8.1");
//            System.out.println(q.toString());
        }
        catch(Exception  e) {
           System.out.println(e.getMessage());   
        }
        
        
    }
    
}
