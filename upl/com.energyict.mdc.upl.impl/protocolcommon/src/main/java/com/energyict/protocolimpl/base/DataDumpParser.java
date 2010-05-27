/*
 * DataDumpParser.java
 *
 * Created on 24 januari 2005, 14:12
 */

package com.energyict.protocolimpl.base;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

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
        if (frame == null) {
			throw new IOException("SCTMDumpData, frame data == null!");
		}
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
                
                if (strRegister.length() < (datetimeSignature.length()+1)) {
					return null;
				}
                
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
            if (index1 == -1) {
				break;
			}
            int index2 = strFrame.indexOf('(',index1);


            try {
                int value = Integer.parseInt(strFrame.substring(index1+1,index2));
                if (value>billingCounter) {
					billingCounter = value;
				}     
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
        } else {
			return quantity;
		}
    }
    
    private String findBaseRegister(String strRegister) throws IOException {
        if (hasBaseRegister(strRegister,".&")) {
			return doGetBaseRegister(strRegister,".&"); // KV 04102004
		}
        if (hasBaseRegister(strRegister,".*")) {
			return doGetBaseRegister(strRegister,".*"); // KV 04102004
		}
        if (hasBaseRegister(strRegister,"&")) {
			return doGetBaseRegister(strRegister,"&");
		}
        if (hasBaseRegister(strRegister,"*")) {
			return doGetBaseRegister(strRegister,"*");
		}
        return null;
    }
    
    private boolean hasBaseRegister(String strRegister,String strDelimiter) {
        if (strRegister.indexOf(strDelimiter) != -1) {
			return true;
		} else {
			return false;
		}
    }
    
    private String doGetBaseRegister(String strRegister,String strDelimiter) throws IOException {
        //StringTokenizer st = new StringTokenizer(strRegister,strDelimiter);
        //return st.nextToken();
        // KV 04102004
        return strRegister.substring(0,strRegister.indexOf(strDelimiter));
    }
    
    private String searchRegister(String strRegister) throws IOException {
       int iIndex = strFrame.indexOf(strRegister+"(");
       if (iIndex == -1) {
			throw new NoSuchRegisterException("DataDumpParser, searchRegister, register not found");
       }       
       return strFrame.substring(iIndex);//-1);
    }
    
    private Quantity parseQuantity(String val) throws IOException {
       StringBuffer stringBufferVal = new StringBuffer();
       StringBuffer stringBufferUnit = new StringBuffer();
       int state=0;
       for (int i=0;i<val.length();i++) {
           if (val.charAt(i) == '(') {
			state=1;
		} else if (val.charAt(i) == '*') {
			state=2;
		} else if (val.charAt(i) == ')') {
			break;
		} else if (((val.charAt(i) >= '0') && (val.charAt(i) <= '9')) || (val.charAt(i) == '.')) {
               if (state==1) {
				stringBufferVal.append(val.charAt(i));
			} 
           }
           else {
               if (state==2) {
				stringBufferUnit.append(val.charAt(i));
			} 
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
        	
/*        	byte[] dataDump = DLMSUtils.hexStringToByteArray("02302e302e30283030393438303239290d0a302e302e31283030393438303239290d0a302e302e32283030303030303030290d0a302e392e31283131303230302" +
        			"90d0a302e392e3228303930383138290d0a312e362e3128302e3330342a6b572928303930383035313733303030290d0a312e362e312a303628302e3236382a6b572928303930373033313331353030290d0a312e3" +
        			"62e312a303528302e3235332a6b572928303930363235313631353030290d0a312e362e312a303428302e3233312a6b572928303930353130313630303030290d0a312e362e312a303328302e3236382a6b5729283" +
        			"03930343135313534353030290d0a312e362e312a303228302e3237302a6b572928303930333130303634353030290d0a312e362e312a303128302e3237352a6b572928303930323234313330303030290d0a312e3" +
        			"62e312a303028302e3030302a6b572928303030303030303030303030290d0a312e362e312a393928302e3030302a6b572928303030303030303030303030290d0a312e362e312a393828302e3030302a6b5729283" +
        			"03030303030303030303030290d0a312e362e312a393728302e3030302a6b572928303030303030303030303030290d0a312e362e312a393628302e3030302a6b572928303030303030303030303030290d0a312e3" +
        			"62e312a393528302e3030302a6b572928303030303030303030303030290d0a312e362e312a393428302e3030302a6b572928303030303030303030303030290d0a312e362e312a393328302e3030302a6b5729283" +
        			"03030303030303030303030290d0a312e362e312a393228302e3030302a6b572928303030303030303030303030290d0a322e362e3128302e3035332a6b572928303930373237313930303030290d0a322e362e312" +
        			"a303628302e3038322a6b572928303930363330303733303030290d0a322e362e312a303528302e3139312a6b572928303930363137313433303030290d0a322e362e312a303428302e3133322a6b5729283039303" +
        			"53139313230303030290d0a322e362e312a303328302e3131322a6b572928303930343037313334353030290d0a322e362e312a303228302e3131312a6b572928303930333234303933303030290d0a322e362e312" +
        			"a303128302e3133392a6b572928303930323235323133303030290d0a322e362e312a303028302e3030302a6b572928303030303030303030303030290d0a322e362e312a393928302e3030302a6b5729283030303" +
        			"03030303030303030290d0a322e362e312a393828302e3030302a6b572928303030303030303030303030290d0a322e362e312a393728302e3030302a6b572928303030303030303030303030290d0a322e362e312" +
        			"a393628302e3030302a6b572928303030303030303030303030290d0a322e362e312a393528302e3030302a6b572928303030303030303030303030290d0a322e362e312a393428302e3030302a6b5729283030303" +
        			"03030303030303030290d0a322e362e312a393328302e3030302a6b572928303030303030303030303030290d0a322e362e312a393228302e3030302a6b572928303030303030303030303030290d0a312e382e312" +
        			"830303431372e3737312a6b5768290d0a312e382e312a30362830303336302e3038302a6b5768290d0a312e382e312a30352830303238362e3036312a6b5768290d0a312e382e312a30342830303232342e3639322" +
        			"a6b5768290d0a312e382e312a30332830303135372e3233322a6b5768290d0a312e382e312a30322830303037382e3230342a6b5768290d0a312e382e312a30312830303030332e3536322a6b5768290d0a312e382" +
        			"e312a30302830303030302e3030302a6b5768290d0a312e382e312a39392830303030302e3030302a6b5768290d0a312e382e312a39382830303030302e3030302a6b5768290d0a312e382e312a393728303030303" +
        			"02e3030302a6b5768290d0a312e382e312a39362830303030302e3030302a6b5768290d0a312e382e312a39352830303030302e3030302a6b5768290d0a312e382e312a39342830303030302e3030302a6b5768290" +
        			"d0a312e382e312a39332830303030302e3030302a6b5768290d0a312e382e312a39322830303030302e3030302a6b5768290d0a312e382e322830303131382e3036312a6b5768290d0a312e382e322a30362830303" +
        			"039352e3939332a6b5768290d0a312e382e322a30352830303036352e3633372a6b5768290d0a312e382e322a30342830303034312e3539382a6b5768290d0a312e382e322a30332830303032332e3236342a6b576" +
        			"8290d0a312e382e322a30322830303030362e3238392a6b5768290d0a312e382e322a30312830303030312e3237322a6b5768290d0a312e382e322a30302830303030302e3030302a6b5768290d0a312e382e322a3" +
        			"9392830303030302e3030302a6b5768290d0a312e382e322a39382830303030302e3030302a6b5768290d0a312e382e322a39372830303030302e3030302a6b5768290d0a312e382e322a39362830303030302e303" +
        			"0302a6b5768290d0a312e382e322a39352830303030302e3030302a6b5768290d0a312e382e322a39342830303030302e3030302a6b5768290d0a312e382e322a39332830303030302e3030302a6b5768290d0a312" +
        			"e382e322a39322830303030302e3030302a6b5768290d0a322e382e312830303030322e3338392a6b5768290d0a322e382e312a30362830303030322e3338362a6b5768290d0a322e382e312a30352830303030322" +
        			"e3338362a6b5768290d0a322e382e312a30342830303030312e3733392a6b5768290d0a322e382e312a30332830303030312e3030372a6b5768290d0a322e382e312a30322830303030302e3037312a6b5768290d0" +
        			"a322e382e312a30312830303030302e3030332a6b5768290d0a322e382e312a30302830303030302e3030302a6b5768290d0a322e382e312a39392830303030302e3030302a6b5768290d0a322e382e312a3938283" +
        			"0303030302e3030302a6b5768290d0a322e382e312a39372830303030302e3030302a6b5768290d0a322e382e312a39362830303030302e3030302a6b5768290d0a322e382e312a39352830303030302e3030302a6" +
        			"b5768290d0a322e382e312a39342830303030302e3030302a6b5768290d0a322e382e312a39332830303030302e3030302a6b5768290d0a322e382e312a39322830303030302e3030302a6b5768290d0a322e382e3" +
        			"22830303035372e3032312a6b5768290d0a322e382e322a30362830303035332e3637312a6b5768290d0a322e382e322a30352830303034372e3236312a6b5768290d0a322e382e322a30342830303033302e33323" +
        			"02a6b5768290d0a322e382e322a30332830303031372e3033312a6b5768290d0a322e382e322a30322830303030372e3832312a6b5768290d0a322e382e322a30312830303030302e3839372a6b5768290d0a322e3" +
        			"82e322a30302830303030302e3030302a6b5768290d0a322e382e322a39392830303030302e3030302a6b5768290d0a322e382e322a39382830303030302e3030302a6b5768290d0a322e382e322a3937283030303" +
        			"0302e3030302a6b5768290d0a322e382e322a39362830303030302e3030302a6b5768290d0a322e382e322a39352830303030302e3030302a6b5768290d0a322e382e322a39342830303030302e3030302a6b57682" +
        			"90d0a322e382e322a39332830303030302e3030302a6b5768290d0a322e382e322a39322830303030302e3030302a6b5768290d0a332e382e312830303331312e3733392a6b76617268290d0a332e382e312a30362" +
        			"830303236372e3637312a6b76617268290d0a332e382e312a30352830303231322e3938332a6b76617268290d0a332e382e312a30342830303136372e3736392a6b76617268290d0a332e382e312a3033283030313" +
        			"1352e3935362a6b76617268290d0a332e382e312a30322830303035362e3034392a6b76617268290d0a332e382e312a30312830303030322e3633382a6b76617268290d0a332e382e312a30302830303030302e303" +
        			"0302a6b76617268290d0a332e382e312a39392830303030302e3030302a6b76617268290d0a332e382e312a39382830303030302e3030302a6b76617268290d0a332e382e312a39372830303030302e3030302a6b7" +
        			"6617268290d0a332e382e312a39362830303030302e3030302a6b76617268290d0a332e382e312a39352830303030302e3030302a6b76617268290d0a332e382e312a39342830303030302e3030302a6b766172682" +
        			"90d0a332e382e312a39332830303030302e3030302a6b76617268290d0a332e382e312a39322830303030302e3030302a6b76617268290d0a332e382e322830303234322e3234392a6b76617268290d0a332e382e3" +
        			"22a30362830303230322e3732302a6b76617268290d0a332e382e322a30352830303135392e3531372a6b76617268290d0a332e382e322a30342830303132342e3532302a6b76617268290d0a332e382e322a30332" +
        			"830303038352e3037332a6b76617268290d0a332e382e322a30322830303034352e3537362a6b76617268290d0a332e382e322a30312830303030352e3439322a6b76617268290d0a332e382e322a3030283030303" +
        			"0302e3030302a6b76617268290d0a332e382e322a39392830303030302e3030302a6b76617268290d0a332e382e322a39382830303030302e3030302a6b76617268290d0a332e382e322a39372830303030302e303" +
        			"0302a6b76617268290d0a332e382e322a39362830303030302e3030302a6b76617268290d0a332e382e322a39352830303030302e3030302a6b76617268290d0a332e382e322a39342830303030302e3030302a6b7" +
        			"6617268290d0a332e382e322a39332830303030302e3030302a6b76617268290d0a332e382e322a39322830303030302e3030302a6b76617268290d0a342e382e312830303030302e3030302a6b76617268290d0a3" +
        			"42e382e312a30362830303030302e3030302a6b76617268290d0a342e382e312a30352830303030302e3030302a6b76617268290d0a342e382e312a30342830303030302e3030302a6b76617268290d0a342e382e3" +
        			"12a30332830303030302e3030302a6b76617268290d0a342e382e312a30322830303030302e3030302a6b76617268290d0a342e382e312a30312830303030302e3030302a6b76617268290d0a342e382e312a30302" +
        			"830303030302e3030302a6b76617268290d0a342e382e312a39392830303030302e3030302a6b76617268290d0a342e382e312a39382830303030302e3030302a6b76617268290d0a342e382e312a3937283030303" +
        			"0302e3030302a6b76617268290d0a342e382e312a39362830303030302e3030302a6b76617268290d0a342e382e312a39352830303030302e3030302a6b76617268290d0a342e382e312a39342830303030302e303" +
        			"0302a6b76617268290d0a342e382e312a39332830303030302e3030302a6b76617268290d0a342e382e312a39322830303030302e3030302a6b76617268290d0a342e382e322830303030302e3030302a6b7661726" +
        			"8290d0a342e382e322a30362830303030302e3030302a6b76617268290d0a342e382e322a30352830303030302e3030302a6b76617268290d0a342e382e322a30342830303030302e3030302a6b76617268290d0a3" +
        			"42e382e322a30332830303030302e3030302a6b76617268290d0a342e382e322a30322830303030302e3030302a6b76617268290d0a342e382e322a30312830303030302e3030302a6b76617268290d0a342e382e3" +
        			"22a30302830303030302e3030302a6b76617268290d0a342e382e322a39392830303030302e3030302a6b76617268290d0a342e382e322a39382830303030302e3030302a6b76617268290d0a342e382e322a39372" +
        			"830303030302e3030302a6b76617268290d0a342e382e322a39362830303030302e3030302a6b76617268290d0a342e382e322a39352830303030302e3030302a6b76617268290d0a342e382e322a3934283030303" +
        			"0302e3030302a6b76617268290d0a342e382e322a39332830303030302e3030302a6b76617268290d0a342e382e322a39322830303030302e3030302a6b76617268290d0a33312e323528302e383235362a41290d0" +
        			"a33322e32352835392e33382a56290d0a33332e3235282d302e30312a502f53290d0a35312e323528302e383832302a41290d0a35322e32352835392e32322a56290d0a35332e323528302e30302a502f53290d0a3" +
        			"7312e323528302e383731362a41290d0a37322e32352835392e33332a56290d0a37332e3235282d302e30332a502f53290d0a462e46283030303030303030290d0a210d0a0320");

        	DataDumpParser ddp = new DataDumpParser(dataDump);

        	ddp.getBillingCounter();*/
        	
            //String str = new String("1-1:F.F(00)\r\n1-1:0.0.0(62127388)\r\n1-1:0.1.0(01)\r\n1-1:1.2.1(00000.0*kW)\r\n1-1:1.2.2(00000.0*kW)\r\n1-1:1.2.3(00000.0*kW)\r\n1-1:1.2.4(00000.0*kW)\r\n1-1:1.6.1(000.0*kW)\r\n1-1:1.6.2(000.0*kW)\r\n1-1:1.6.3(000.0*kW)\r\n1-1:1.6.4(000.0*kW)\r\n1-1:1.8.1(0002000.030*kWh)\r\n1-1:1.8.2(0000000.0*kWh)\r\n1-1:1.8.3(0000000.0*kWh)\r\n1-1:1.8.4(0000000.0*kWh)\r\n1-1:5.8.1(9000000.123*kvarh)\r\n1-1:5.8.2(0000000.0*kvarh)\r\n1-1:5.8.3(0000000.0*kvarh)\r\n1-1:5.8.4(0000000.0*kvarh)\r\n1-1:8.8.1(0000000.0*kvarh)\r\n1-1:8.8.2(0000000.0*kvarh)\r\n1-1:8.8.3(0000000.0*kvarh)\r\n1-1:8.8.4(0000000.0*kvarh)\r\n!\r\n");
            //String str = new String("9.2(123456789*Wh)\r\n10.2(0001234*kWh)\r\n10.2&01(1234.567)\r\n9.2*00(0.56789)\r\n10.2*99(0000000)\r\n10.2*98(0000000)\r\n10.2*97(0000000)");
            
//            byte[] frame = readFile();
//            DataDumpParser dumpData = new DataDumpParser(frame);
////            q = dumpData.getRegister("8.1"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
////            System.out.println(q.toString());
////            q = dumpData.getRegister("8.1.&22"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
////            System.out.println(q.toString());
////            q = dumpData.getRegister("8.1"); //dumpData.getRegister("10.2"); //dumpData.getRegister("1-1:5.8.1");
////            System.out.println(q.toString());
//            
////            q = dumpData.getRegister("1-1:1.6.1*02");
////            System.out.println(q.toString());
////            System.out.println(dumpData.getRegisterStrValue("1-1:1.6.1*02"));
////            
////            q = dumpData.getRegister("1-1:1.6.3");
////            System.out.println("1-1:1.6.3 --> "+q.toString());
////            
//            String str = dumpData.getRegisterStrValue("1.6.3*06");
//            Quantity q = dumpData.getRegister("1.6.3*06");
//            Date d = dumpData.getRegisterDateTime("1.6.3*06",TimeZone.getTimeZone("ECT"));
//            System.out.println("1.6.3*06 --> "+str+", q="+q+", d="+d);
//            
//            str = dumpData.getRegisterStrValue("1.6.3*6");
//            q = dumpData.getRegister("1.6.3*6");
//            d = dumpData.getRegisterDateTime("1.6.3*6",TimeZone.getTimeZone("ECT"));
//            System.out.println("1.6.3*6 --> "+str+", q="+q+", d="+d);
//
//            
//            str = dumpData.getRegisterStrValue("1.6.3");
//            q = dumpData.getRegister("1.6.3");
//            d = dumpData.getRegisterDateTime("1.6.3",TimeZone.getTimeZone("ECT"));
//            System.out.println("1.6.3 --> "+str+", q="+q+", d="+d);
//
//            str = dumpData.getRegisterStrValue("0.1.0");
//            q = dumpData.getRegister("0.1.0");
//            d = dumpData.getRegisterDateTime("0.1.0",TimeZone.getTimeZone("ECT"));
//            System.out.println("0.1.0 --> "+str+", q="+q+", d="+d);
//            
//            str = dumpData.getRegisterStrValue("0.1.0*06");
//            q = dumpData.getRegister("0.1.0*06");
//            d = dumpData.getRegisterDateTime("0.1.0*06",TimeZone.getTimeZone("ECT"));
//            System.out.println("0.1.0*06 --> "+str+", q="+q+", d="+d);
//
//            str = dumpData.getRegisterStrValue("0.1.0*6");
//            q = dumpData.getRegister("0.1.0*6");
//            d = dumpData.getRegisterDateTime("0.1.0*6",TimeZone.getTimeZone("ECT"));
//            System.out.println("0.1.0*6 --> "+str+", q="+q+", d="+d);
//            
            
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
