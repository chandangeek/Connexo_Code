/*
 * DLMSUtils.java
 *
 * Created on 17 januari 2003, 15:55
 */

package com.energyict.dlms;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author  Koen
 * 
 * |GNA| 19012009 - Added a valid description for Abstract objects that have no description, otherwise these are not stored correctly in database
 */
public class DLMSUtils implements DLMSCOSEMGlobals {
    
    private static final int DEBUG=0;
    /** Creates a new instance of DLMSUtils */
    public DLMSUtils() {
    }

    public static byte attrSN2LN(short snAttr) {
        return attrSN2LN((int)snAttr & 0xFFFF);
    }
    public static byte attrSN2LN(int snAttr) {
        return (byte)(snAttr/8+1);
    }
    public static int attrLN2SN(int lnAttr) {
        return (lnAttr-1)*8; 
    }
    

    public static byte[] getAXDRLengthEncoding(int length) {
        if (length<128) {
            return new byte[]{(byte)length};
        }
        else {

            int val2Check=1;
            int count=0;
            while(true) {
                if ((val2Check<<(8*count))>length)
                    break;;
                count++;
            }
            byte[] lengthEncoding = new byte[1+count];
            lengthEncoding[0] = (byte)(0x80 | (lengthEncoding.length-1));
            for (int i=0;i<(lengthEncoding.length-1);i++) {
                lengthEncoding[(lengthEncoding.length-1)-i]=(byte)(length>>(8*i));
            }
            return lengthEncoding;
        }
    }
    
    
    static public void main(String[] args) {
        byte[] data = new byte[]{(byte)0x21,(byte)0xFF};
        System.out.println(getAXDRLengthOffset(data,0));
        System.out.println(getAXDRLength(data,0));
    }
    
    public static int getAXDRLengthOffset(byte[] byteBuffer,int iOffset)
    {
       if ((byteBuffer[iOffset] & (byte)0x80) != 0)
       {
          return  ((byteBuffer[iOffset] & 0x7f)+1);
       }
       else return 1; 
    }
    
    public static long getAXDRLength(byte[] byteBuffer,int iOffset)
    {
       int i;
       long lLength=0;
       int iNROfBytes;
       
       if ((byteBuffer[iOffset] & (byte)0x80) != 0)
       {
           iNROfBytes=(byteBuffer[iOffset]&0x7F);
           for (i=0;i<iNROfBytes;i++)
           {
              lLength |= (((long)byteBuffer[iOffset+i+1]&0xFF) << (8*((iNROfBytes-1)-i)));   
           }
       }
       else
       {
           lLength = ((long)byteBuffer[iOffset] & 0xFF);
       }
       
       return lLength;
       
    } // public static long getAXDRLength(byte[] byteBuffer,int iOffset)
    
    public static long parseValue2long(byte[] byteBuffer) throws IOException {
        return parseValue2long(byteBuffer,0);
    }
    
    public static long parseValue2long(byte[] byteBuffer,int iOffset) throws IOException {
        switch (byteBuffer[iOffset])
        {
            case TYPEDESC_NULL: 
                return (long)0;
            
            case TYPEDESC_FLOATING_POINT:
            case TYPEDESC_OCTET_STRING:
            case TYPEDESC_VISIBLE_STRING:
            case TYPEDESC_TIME:
            case TYPEDESC_BCD:
            case TYPEDESC_BITSTRING:                
            case TYPEDESC_STRUCTURE:
            case TYPEDESC_ARRAY:
            case  TYPEDESC_COMPACT_ARRAY:
                throw new IOException("parseValue2int() error");
            
            case  TYPEDESC_ENUM:
            case TYPEDESC_BOOLEAN:
                return (long)byteBuffer[iOffset+1]&0xff;
                
            case TYPEDESC_DOUBLE_LONG:
            case TYPEDESC_DOUBLE_LONG_UNSIGNED:
                return (long)ProtocolUtils.getInt(byteBuffer,iOffset+1);
                
            case  TYPEDESC_UNSIGNED:
            case  TYPEDESC_INTEGER:
                return (long)byteBuffer[iOffset+1]&0xff;
            
            case  TYPEDESC_LONG_UNSIGNED:                
            case  TYPEDESC_LONG:
                return (long)ProtocolUtils.getShort(byteBuffer,iOffset+1);
                
            case  TYPEDESC_LONG64:
                return (long)ProtocolUtils.getLong(byteBuffer,iOffset+1);
                
            default:    
                throw new IOException("parseValue2long() error, unknown type "+byteBuffer[iOffset]);
        } // switch (byteBuffer[iOffset])
        
    } // public long parseValue2long(byte[] byteBuffer,int iOffset) throws IOException

    public static String parseValue2String(byte[] byteBuffer,int iOffset) throws IOException
    {
        switch (byteBuffer[iOffset])
        {
            case TYPEDESC_NULL: 
                return String.valueOf(0);
            
            case TYPEDESC_FLOATING_POINT:
            case TYPEDESC_TIME:
            case TYPEDESC_BCD:
            case TYPEDESC_BITSTRING:                
            case TYPEDESC_STRUCTURE:
            case TYPEDESC_ARRAY:
            case  TYPEDESC_COMPACT_ARRAY:
                throw new IOException("parseValue2int() error");

                
            case TYPEDESC_OCTET_STRING:
            case TYPEDESC_VISIBLE_STRING:
                byte[] bstr = new byte[byteBuffer[iOffset+1]];
                for(int i=0;i<bstr.length;i++) {
                    bstr[i] = byteBuffer[iOffset+2+i];
                }
                return new String(bstr);
                
            case  TYPEDESC_ENUM:
            case TYPEDESC_BOOLEAN:
                return String.valueOf((long)byteBuffer[iOffset+1]&0xff);
                
            case TYPEDESC_DOUBLE_LONG:
            case TYPEDESC_DOUBLE_LONG_UNSIGNED:
                return String.valueOf((long)ProtocolUtils.getInt(byteBuffer,iOffset+1));
                
            case  TYPEDESC_UNSIGNED:
            case  TYPEDESC_INTEGER:
                return String.valueOf((long)byteBuffer[iOffset+1]&0xff);
            
            case  TYPEDESC_LONG_UNSIGNED:                
            case  TYPEDESC_LONG:
                return String.valueOf((long)ProtocolUtils.getShort(byteBuffer,iOffset+1));
                
            case  TYPEDESC_LONG64:
                return String.valueOf((long)ProtocolUtils.getLong(byteBuffer,iOffset+1));
                
            default:    
                throw new IOException("parseValue2int() error, unknown type.");
        } // switch (byteBuffer[iOffset])
        
    } // public String parseValue2String(byte[] byteBuffer,int iOffset) throws IOException
    
    public static String getInfoLN(byte[] LN)
    {
      int A,B,C,D,E,F;
      String str = "";

      A=(int)LN[0]&0xFF;
      B=(int)LN[1]&0xFF;
      C=(int)LN[2]&0xFF;
      D=(int)LN[3]&0xFF;
      E=(int)LN[4]&0xFF;
      F=(int)LN[5]&0xFF;
      
      // A  
      //if (A == 0) str += " Abstract");
      //if (A == 1) str += " Electricity");

      // C
      if (A==1) // electricity related objects
      {
          if ((B>=1) && (B<=64)) str += " channel"+Integer.toString((int)B&0xFF);
          if ((B>=65) && (B<=127)) str += " ???, reserved";
          if ((B>=128) && (B<=254)) str += " ???, manufacturer specific";
          if (B>=255) str += " ???, reserved";
          
          if ((C == 0) && (D==0) && (E<=9) && (F==255)) str += " Elektricity ID obj "+(E+1)+" (data or register)";
          if ((C == 0) && (D==0) && (E==255) && (F==255)) str += " Elektricity ID's obj(profile)";
          if ((C == 0) && (D==1) && (E==0) && (F==255)) str += " Billing period counter obj (data or register)";
          if ((C == 0) && (D==1) && (E==1) && (F==255)) str += " Number of available billing period data obj (data or register)";
          
          if ((C == 0) && (D==2) && (E==0) && (F==255)) str += " Configuration program version NR obj (data or register)";
          if ((C == 0) && (D==2) && (E==2) && (F==255)) str += " Time switch program NR obj (data or register)";
          if ((C == 0) && (D==2) && (E==3) && (F==255)) str += " RCR program NR obj (data or register)";

          if ((C == 0) && (D==9) && (E==10) && (F==255)) str += " Clock synchronization method (data or register)";
          if ((C == 0) && (D==11) && (E==1) && (F==255)) str += " Measurement algorithjm for active power (data or register)";
          if ((C == 0) && (D==11) && (E==2) && (F==255)) str += " Measurement algorithjm for active energy (data or register)";
          if ((C == 0) && (D==11) && (E==3) && (F==255)) str += " Measurement algorithjm for reactive power (data or register)";
          if ((C == 0) && (D==11) && (E==4) && (F==255)) str += " Measurement algorithjm for reactive energy (data or register)";
          if ((C == 0) && (D==11) && (E==5) && (F==255)) str += " Measurement algorithjm for apparent power (data or register)";

          if ((C == 0) && (D==11) && (E==6) && (F==255)) str += " Measurement algorithjm for apparent energy (data or register)";
          if ((C == 0) && (D==11) && (E==7) && (F==255)) str += " Measurement algorithjm for power factor calculation (data or register)";
          
          if (C == 1) str += " SUM(Li) active power+";
          if (C == 2) str += " SUM(Li) active power-";
          if (C == 3) str += " SUM(Li) reactive power+";
          if (C == 4) str += " SUM(Li) reactive power-";
          if (C == 5) str += " SUM(Li) reactive power QI";
          if (C == 6) str += " SUM(Li) reactive power QII";
          if (C == 7) str += " SUM(Li) reactive power QIII";
          if (C == 8) str += " SUM(Li) reactive power QIV";
          if (C == 9) str += " SUM(Li) apparent power+";
          if (C == 10) str += " SUM(Li) apparent power-";
          if (C == 11) str += " Current, any phase";
          if (C == 12) str += " Voltage, any phase";
          if (C == 13) str += " Apparent power factor";
          if (C == 14) str += " Supply frequency";
          if (C == 15) str += " SUM(Li) Active power QI+QIV+QII+QIII";
          if (C == 16) str += " SUM(Li) Active power QI+QIV-QII-QIII";
          if (C == 17) str += " SUM(Li) Active power QI";
          if (C == 18) str += " SUM(Li) Active power QII";
          if (C == 19) str += " SUM(Li) Active power QIII";
          if (C == 20) str += " SUM(Li) Active power QIV";
          if (C == 21) str += " L1 active power+";
          if (C == 22) str += " L1 active power-";
          if (C == 23) str += " L1 reactive power+";
          if (C == 24) str += " L1 reactive power-";
          if (C == 25) str += " L1 reactive power QI";
          if (C == 26) str += " L1 reactive power QII";
          if (C == 27) str += " L1 reactive power QIII";
          if (C == 28) str += " L1 reactive power QIV";
          if (C == 29) str += " L1 apparent power+";
          if (C == 30) str += " L1 apparent power-";
          if (C == 33) str += " L1 power factor";
          if (C == 34) str += " L1 frequency";
          if (C == 35) str += " L1 active power QI+QIV+QII+QIII";
          if (C == 36) str += " L1 active power QI+QIV-QII-QIII";
          if (C == 37) str += " L1 active power QI";
          if (C == 38) str += " L1 active power QII";
          if (C == 39) str += " L1 active power QIII";
          if (C == 40) str += " L1 active power QIV";
          if (C == 41) str += " L2 active power+";
          if (C == 42) str += " L2 active power-";
          if (C == 43) str += " L2 reactive power+";
          if (C == 44) str += " L2 reactive power-";
          if (C == 45) str += " L2 reactive power QI";
          if (C == 46) str += " L2 reactive power QII";
          if (C == 47) str += " L2 reactive power QIII";
          if (C == 48) str += " L2 reactive power QIV";
          if (C == 49) str += " L2 apparent power+";
          if (C == 50) str += " L2 apparent power-";
          
          if (C == 53) str += " L2 power factor";
          if (C == 54) str += " L2 frequency";
          if (C == 55) str += " L2 active power QI+QIV+QII+QIII";
          if (C == 56) str += " L2 active power QI+QIV-QII-QIII";
          if (C == 57) str += " L2 active power QI";
          if (C == 58) str += " L2 active power QII";
          if (C == 59) str += " L2 active power QIII";
          if (C == 60) str += " L2 active power QIV";
          if (C == 61) str += " L3 active power+";
          if (C == 62) str += " L3 active power-";
          if (C == 63) str += " L3 reactive power+";
          if (C == 64) str += " L3 reactive power-";
          if (C == 65) str += " L3 reactive power QI";
          if (C == 66) str += " L3 reactive power QII";
          if (C == 67) str += " L3 reactive power QIII";
          if (C == 68) str += " L3 reactive power QIV";
          if (C == 69) str += " L3 apparent power+";
          if (C == 70) str += " L3 apparent power-";

          if ((C == 31) && (D!=7)) str += " ???";
          if ((C == 32) && (D!=7)) str += " ???";
          if ((C == 51) && (D!=7)) str += " ???";
          if ((C == 52) && (D!=7)) str += " ???";
          if ((C == 71) && (D!=7)) str += " ???";
          if ((C == 72) && (D!=7)) str += " ???";
          
          int match=0;
          if (((C == 31) || (C == 32)|| (C == 51)|| (C == 52)|| (C == 71)|| (C == 72)) &&
               (D==7) && (E==0))
          {
             str += " Total";
             match=1;
          }
          if (((C == 31) || (C == 32)|| (C == 51)|| (C == 52)|| (C == 71)|| (C == 72)) &&
               (D==7) && (E>=1) && (E<=127))
          {
             str += " harmonic "+E;
             match=1;
          }
          if (((C == 31) || (C == 32)|| (C == 51)|| (C == 52)|| (C == 71)|| (C == 72)) &&
               (D==7) && (E>=128) && (E<=254))
          {
             str += " Manufacturer specific";
             match=1;
          }
          if (((C == 31) || (C == 32)|| (C == 51)|| (C == 52)|| (C == 71)|| (C == 72)) &&
               (D==7) && (E==255))
          {
             str += " Reserved";
             match=1;
          }
          if ((match == 0) && (C != 0))
          {
              if (E==0) str += " Total";  
              if ((E>=1) && (E<=63)) str += " Rate "+E;  
              if ((E>=128) && (E<=254)) str += " Manufactures specific";  
              if (E==255) str += " Reserved";  
          }
          
          if (C == 73) str += " L3 power factor";
          if (C == 74) str += " L3 frequency";
          if (C == 75) str += " L3 active power QI+QIV+QII+QIII";
          if (C == 76) str += " L3 active power QI+QIV-QII-QIII";
          if (C == 77) str += " L3 active power QI";
          if (C == 78) str += " L3 active power QII";
          if (C == 79) str += " L3 active power QIII";
          if (C == 80) str += " L3 active power QIV";
          
          if ((C == 81) && (D==7)) str += " Angle measurement "+E;
          
          
          if (C == 82) str += " Unitless quantity";
          if ((C >= 83) && (C <= 90)) str += " ???";
          if (C == 91) str += " L0 current (N)";
          if (C == 92) str += " L0 voltage (N)";
          if ((C >= 93) && (C <= 95)) str += " ???";
          if (C == 96) str += " El. rel. services, see 5.4.7";
          if (C == 97) str += " El. rel. error messages";
          if (C == 98) str += " El. list";
          
//          if (C == 99)  str += " El. profiles, see 5.4.10";
          if ((C==99) && (D==1) && (E>=0) && (E<=127) && (F==255))  str += " Load profile object "+E+" with recording period 1 (profile)";
          if ((C==99) && (D==2) && (E>=0) && (E<=127) && (F==255))  str += " Load profile object "+E+" with recording period 2 (profile)";
          if ((C==99) && (D==3) && (E==0) && (F==255))  str += " Load profile during test (profile)";
          if ((C==99) && (D==10) && (E==1) && (F==255))  str += " Dips voltage profile (profile)";
          if ((C==99) && (D==10) && (E==2) && (F==255))  str += " Swells voltage profile (profile)";
          if ((C==99) && (D==10) && (E==3) && (F==255))  str += " Cuts voltage profile (profile)";
          if ((C==99) && (D==11) && (E>=1) && (E<=127) && (F==255))  str += " Voltage harmonic profile "+E+" (profile)";
          if ((C==99) && (D==12) && (E>=1) && (E<=127) && (F==255))  str += " Current harmonic profile "+E+" (profile)";
          if ((C==99) && (D==13) && (E==0) && (F==255))  str += " Voltage unbalance profile (profile)";
          if ((C==99) && (D==98) && (F==255))  str += " Event log +"+E+" (profile)";
          if ((C==99) && (D==99) && (F==255))  str += " Certification data log +"+E+" (profile)";
          
          if ((C >= 100) && (C <= 127)) str += " ???, reserved";
          if ((C >= 128) && (C <= 254)) str += " ???, manufacturer specific";
          if (C == 255) str += " ???, reserved";
          
          if ((C!=0)&&(C!=96)&&(C!=97)&&(C!=98)&&(C!=99))
          {
             if (D==0) str += " Billing period average (since last reset)";  
             if (D==1) str += " Cumulative minimum 1";  
             if (D==2) str += " Cumulative maximum 1";  
             if (D==3) str += " Minimum 1";  
             if (D==4) str += " Current average 1";  
             if (D==5) str += " Last average 1";  
             if (D==6) str += " Maximum 1";  
             if (D==7) str += " Instantaneous value";  
             if (D==8) str += " Time integral 1";  
             if (D==9) str += " Time integral 2";  
             if (D==10) str += " Time integral 3";  
             if (D==11) str += " Cumulative minimum 2";  
             if (D==12) str += " Cumulative maximum 2";  
             if (D==13) str += " Minimum 2";  
             if (D==14) str += " Current average 2";  
             if (D==15) str += " Last average 2";  
             if (D==16) str += " Maximum 2";  
             if ((D>=17) && (D<=20)) str += " ???";  
             if (D==21) str += " Cumulative minimum 3";  
             if (D==22) str += " Cumulative maximum 3";  
             if (D==23) str += " Minimum 3";  
             if (D==24) str += " Current average 3";  
             if (D==25) str += " Last average 3";  
             if (D==26) str += " Maximum 3";  
             if (D==27) str += " Current average 5";  
             if (D==28) str += " Current average 6";  
             if (D==29) str += " Time integral 5";  
             if (D==30) str += " Time integral 6";  
             if (D==31) str += " Under limit threshold";  
             if (D==32) str += " Under limit occurence counter";  
             if (D==33) str += " Under limit duration";  
             if (D==34) str += " Under limit magnitude";  
             if (D==35) str += " Over limit threshold";  
             if (D==36) str += " Over limit occurence counter";  
             if (D==37) str += " Over limit duration";  
             if (D==38) str += " Over limit magnitude";  
             if (D==39) str += " Missing threshold";  
             if (D==40) str += " Missing occurence counter";  
             if (D==41) str += " Missing duration";  
             if (D==42) str += " Missing magnitude";  
             if ((D>=43) && (D<=54)) str += " ???";  
             if (D==55) str += " Test average";  
             if ((D>=56) && (D<=57)) str += " ???";  
             if (D==58) str += " Time integral 4";   
             if ((D>=128) && (D<=254)) str += " ???, manufacturer specific codes";  
             if (D==255) str += " Reserved";   
          }
          
      } // if (A==1)
      else if (A==0) // abstract objects
      {
          if ((B>=1) && (B<=64)) str += " object/channel"+Integer.toString((int)B&0xFF);
          else if ((B>=65) && (B<=127)) str += " ???, reserved";
          else if ((B>=128) && (B<=254)) str += " ???, manufacturer specific";
          else if (B>=255) str += " ???, reserved";
          
          if (C == 0) str += " gen. purpose"; 
          else if ((C == 1) && (D==0) && (F==255)) str += " Clock object "+E; 
          else if ((C == 2) && (D==0) && (E==0) && (F==255)) str += " PSTN modem configuration"; 
          else if ((C == 2) && (D==1) && (E==0) && (F==255)) str += " PSTN auto dial"; 
          else if ((C >= 3) && (C<=9)) str += " ???"; 
          else if ((C == 2) && (D==2) && (E==0) && (F==255)) str += " PSTN auto answer"; 

          else if ((C == 10) && (D==0) && (E==0) && (F==255)) str += " Global meter reset"; 
          else if ((C == 10) && (D==0) && (E==1) && (F==255)) str += " MDI reset / end of billing period"; 
          else if ((C == 10) && (D==0) && (E==100) && (F==255)) str += " Tarrification script table"; 
          else if ((C == 10) && (D==0) && (E==101) && (F==255)) str += " Activate test mode"; 
          else if ((C == 10) && (D==0) && (E==102) && (F==255)) str += " Activate normal mode"; 
          else if ((C == 10) && (D==0) && (E==103) && (F==255)) str += " Set output signals"; 
          else if ((C == 10) && (D==0) && (E==125) && (F==255)) str += " Broadcast script table"; 
          
          else if ((C == 11) && (D==0) && (E==0) && (F==255)) str += " Special days table"; 
          else if ((C == 12) && (D==0) && (F==255)) str += " Schedule object "+E; 
          else if ((C == 13) && (D==0) && (E==0) && (F==255)) str += " Activity calendar"; 
          else if ((C == 14) && (D==0) && (E==0) && (F==255)) str += " Register activation"; 
          else if ((C == 15) && (D==0) && (E==0) && (F==255)) str += " End of billing period (IC single action schedule)"; 
          else if ((C == 20) && (D==0) && (E==0) && (F==255)) str += " IEC optical port setup obj"; 
          else if ((C == 20) && (D==0) && (E==1) && (F==255)) str += " IEC electrical port setup obj"; 
          
          else if ((C == 21) && (D==0) && (E==0) && (F==255)) str += " general local port readout (IC profile)"; 
          else if ((C == 21) && (D==0) && (E==1) && (F==255)) str += " general display readout (IC profile)"; 
          else if ((C == 21) && (D==0) && (E==2) && (F==255)) str += " alternate display readout (IC profile)"; 
          else if ((C == 21) && (D==0) && (E==3) && (F==255)) str += " service display readout (IC profile)"; 
          else if ((C == 21) && (D==0) && (E==4) && (F==255)) str += " list of configurable meter data (IC profile)"; 
          else if ((C == 21) && (D==0) && (E>=5) && (F==255)) str += " additional readout profile "+(E-4)+" (IC profile)"; 

          else if ((C == 22) && (D==0) && (E==0) && (F==255)) str += " IEC HDLC setup obj"; 
          else if ((C == 23) && (D==0) && (E==0) && (F==255)) str += " IEC twisted pair setup"; 
          else if ((C >= 24) && (C<=39)) str += " ???"; 
          else if ((C == 40) && (D==0) && (E==0) && (F==255)) str += " Current Association"; 
          else if ((C == 40) && (D==0) && (E>=1) && (F==255)) str += " Association instance "+E; 

          else if ((C == 41) && (D==0) && (E==0) && (F==255)) str += " SAP assignment obj"; 
          else if ((C == 42) && (D==0) && (E==0) && (F==255)) str += " COSEM logical device name (data or register)"; 
          else if ((C >= 43) && (C<=64)) str += " ???"; 
          
          else if ((C == 65) && (F==255)) str += " Utility tables D="+D+" E="+E; 

          else if ((C >= 66) && (C<=95)) str += " ???"; 
          
          else if ((C == 96) && (D==1) && (E<=9) && (F==255)) str += " Device ID "+(E+1)+" obj (data or register)";           
          else if ((C == 96) && (D==1) && (E==255) && (F==255)) str += " Device ID's object (profile)";           
          
          else if ((C == 96) && (D==2) && (E==0) && (F==255)) str += " Number of configuration program changes obj (data)";           
          else if ((C == 96) && (D==2) && (E==1) && (F==255)) str += " Date of last configuration program changes obj (data)";           
          else if ((C == 96) && (D==2) && (E==2) && (F==255)) str += " Date of last time switch program change object (data)";
          else if ((C == 96) && (D==2) && (E==3) && (F==255)) str += " Date of last ripple control receiver program change obj (data)";

          else if ((C == 96) && (D==2) && (E==4) && (F==255)) str += " Status of security switches (data)";
          else if ((C == 96) && (D==2) && (E==5) && (F==255)) str += " Date of last calibration (data)";
          else if ((C == 96) && (D==2) && (E==6) && (F==255)) str += " Date of next configuration program change (data)";
          else if ((C == 96) && (D==2) && (E==7) && (F==255)) str += " Time of activation of the passive calendar (data)";
          
          else if ((C == 96) && (D==2) && (E==10) && (F==255)) str += " Number of protected configuration program changes obj (data)";
          else if ((C == 96) && (D==2) && (E==11) && (F==255)) str += " Date of last protected configuration program changes obj (data)";

          else if ((C == 96) && (D==3) && (E==1) && (F==255)) str += " State of input control signals (data or register or extended register)";
          else if ((C == 96) && (D==3) && (E==2) && (F==255)) str += " State of output control signals (data or register or extended register)";
          
          else if ((C == 96) && (D==4) && (E==0) && (F==255)) str += " State of the internal control signals (data or register or extended register)";

          else if ((C == 96) && (D==5) && (E==0) && (F==255)) str += " internal operating status (data or register or extended register)";

          else if ((C == 96) && (D==6) && (E==0) && (F==255)) str += " Battery use time counter (register or extended register)";
          else if ((C == 96) && (D==6) && (E==1) && (F==255)) str += " Battery charge display (register or extended register)";
          else if ((C == 96) && (D==6) && (E==2) && (F==255)) str += " Date of next change (register or extended register)";
          else if ((C == 96) && (D==6) && (E==3) && (F==255)) str += " Battery voltage (register or extended register)";
          
          else if ((C == 96) && (D==7) && (E==0) && (F==255)) str += " Total failure of all 3 phases longer than internal autonomy  (data or profile)";
          else if ((C == 96) && (D==7) && (E==1) && (F==255)) str += " Phase L1 (data or profile)";
          else if ((C == 96) && (D==7) && (E==2) && (F==255)) str += " Phase L2 (data or profile)";
          else if ((C == 96) && (D==7) && (E==3) && (F==255)) str += " Phase L3 (data or profile)";
          
          else if ((C == 96) && (D==8) && (E==0) && (F==255)) str += " Time of operation  (data or register)";
          else if ((C == 96) && (D==8) && (E>=1) && (E<=63) && (F==255)) str += " Time of registration rate "+E+" (data or register)";
          else if ((C == 96) && (D==8) && (E==255) && (F==255)) str += " Time of registration (profile)";

          else if ((C == 96) && (D==9) && (E==0) && (F==255)) str += " Ambient temperature  (data or register or extended register)";

          else if ((C == 96) && (D>=50) && (D<=96)) str += " Manufacturer specific abstract objects (data or register or extended register or profile)";

          else if ((C == 96) && (D==99) && (E==8) && (F==255)) str += " Name of the standard data set (data or register)";
          
          else if ((C == 97) && (D==97) && (E<=9) && (F==255)) str += " Error "+(E+1)+" obj (data)";
          else if ((C == 97) && (D==97) && (E==255) && (F==255)) str += " Error profile obj (profile)";
          
          else if ((C == 98) && (D==1)) str += " Date of billing periods E="+E+" F="+F+" (profile)";

          else if (C == 127) str += " Inactive object";
          
          else if ((C >= 128) && (C<=255)) str += " ???, manufacturer specific"; 
          
          else str += " ???, Unknown description";
          
      }
      else
      {
          str += " ???, value A = "+A+" unknown"; 
      }
   
      
      return str;
      
    } // protected String getInfoLN(byte[] LN)

  
    
    public static byte[] hexStringToByteArray(String str){
    	if(str.length() == 1){
    		str = "0"+str;
    	}
    	byte[] data = new byte[str.length()/2];
    	int offset = 0;
    	int endOffset = 2;
    	for(int i = 0; i < data.length; i++){
    		data[i] = (byte)Integer.parseInt(str.substring(offset, endOffset), 16);
    		offset = endOffset;
    		endOffset += 2;
    	}
    	return data;
    }
    
}
