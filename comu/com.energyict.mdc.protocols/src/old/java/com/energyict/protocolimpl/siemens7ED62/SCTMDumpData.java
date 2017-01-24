/*
 * SCTMDumpData.java
 *
 * Created on 6 februari 2003, 9:22
 */

package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

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
public class SCTMDumpData {

	private static final String FRAME_COUNTER_INDEX = new String(new byte[]{41, 13, 10, 49, 40});
	private static final char STARTING_BRACKET = '(';
	private static final char CLOSING_BRACKET = ')';
	private static final char ASTERISK = '*';

	public static final String ENERMET_DUMP_DATETIME_SIGNATURE="yy-MM-dd HH:mm";

    //byte[] frame;
    private String strFrame;
    private int bufferId;

    private String datetimeSignature;

    /** Creates a new instance of SCTMDumpData */
    public SCTMDumpData(byte[] frame, int bufferId) throws IOException {
        this(frame,ENERMET_DUMP_DATETIME_SIGNATURE,bufferId);
    }
    public SCTMDumpData(byte[] frame,String datetimeSignature, int bufferId) throws IOException {
        this.datetimeSignature=datetimeSignature;
        this.bufferId=bufferId;
        if (frame == null) {
			throw new NoSuchRegisterException("SCTMDumpData, no dump data to parse register value!");
		}

//        FileOutputStream fos = new FileOutputStream(new File("C:\\dumpDataByteArray.bin"));
//        fos.write(frame);

        strFrame = new String(frame);

        // filter out all leading zeros '0' and change .* by *
        strFrame = strFrame.replaceAll("\r\n0", "\r\n");
        strFrame = strFrame.replaceAll("\\.\\*", "*");

    }

    public String toString() {
        return strFrame;
    }

    public Date getRegisterDateTime(String strReg, TimeZone timeZone) throws IOException {
        String strRegister="\n"+strReg;
        strRegister=searchRegister(strRegister);
        strRegister = strRegister.substring(1,strRegister.indexOf("\r"));
        if (datetimeSignature.compareTo(ENERMET_DUMP_DATETIME_SIGNATURE)==0) {
            try {
                int beginIndex = strRegister.length()-(datetimeSignature.length()+1);
                if (beginIndex < 0) {
                    return null;
				}
                strRegister = strRegister.substring(beginIndex,strRegister.length()-1);
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

    /**
     * Search for and return the billingCounter. The BillingCounter is a field in the datadump.
     * As from 01/02/2010 we use the field in the datadump instead of looping over the billings
     * @return
     */
    public int getBillingCounter() {
        int billingCounter=0;
    	int index1 = -1;
    	int index2 = 0;
//    	String strTest = strFrame.substring(64, 69).getBytes();
    	if(strFrame.indexOf(FRAME_COUNTER_INDEX) < 0){
    		/* OLD and INCORRECT method
    		 * We still support this way of getting the billingCounter because this is commonly used code.
    		 * This way of doing always results in 99 (even after an overflow)
    		 */
    		index1=-1;
        while(true) {
    			index1 = strFrame.indexOf(ASTERISK,index1+1);
    			if (index1 == -1) {
					break;
				}
    			index2 = strFrame.indexOf(STARTING_BRACKET,index1);
    			if (index2 == -1) {
					break;
				}

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

    	} else {
    		/*
    		 * New and more correct method of getting the billingCounter.
    		 * The billingCounter will normally be the first value before an asterisk (*)
    		 * It is also the value between the '1(??)'
    		 */

    		index1 = strFrame.indexOf(FRAME_COUNTER_INDEX) + FRAME_COUNTER_INDEX.length();
    		index2 = strFrame.indexOf(CLOSING_BRACKET, index1);
    		billingCounter = Integer.parseInt(strFrame.substring(index1,index2));
    		return billingCounter;

    	}

    }

    public int getBillingCounterLength() {

        int length=0;
        int index1=-1;

        while(true) {
            index1 = strFrame.indexOf(ASTERISK,index1+1);
            if (index1 == -1) {
				break;
			}
            int index2 = strFrame.indexOf(STARTING_BRACKET,index1);
            if (index2 == -1) {
				break;
			}

            try {
                int value = Integer.parseInt(strFrame.substring(index1+1,index2));
                if (strFrame.substring(index1+1,index2).length()>length) {
					length = strFrame.substring(index1+1,index2).length();
				}
            }
            catch(NumberFormatException e) {
                // absorb
            }
        }

        return length;
    }

    public Quantity getRegister(String strReg) throws IOException {
        String strRegister="\n"+strReg;
        Quantity quantity = parseQuantity(searchRegister(strRegister));
        String strBaseRegister = findBaseRegister(strRegister);
        if (strBaseRegister != null) {
            Quantity quantityUnit;
            try {
                quantityUnit = parseQuantity(searchRegister(strBaseRegister));
                return new Quantity(quantity.getAmount(), quantityUnit.getUnit());
            } catch (NoSuchRegisterException e) {   // The baseUnit register cannot be read (most likely the base unit register does not exists)
                return quantity;
            }
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

    public String searchRegister(String strRegister) throws IOException {
       int iIndex = strFrame.indexOf(strRegister+STARTING_BRACKET);
       if (iIndex == -1) {
		throw new NoSuchRegisterException("SCTMDumpData, searchRegister, register not found");
	}
       return strFrame.substring(iIndex);//-1);
    }

    private Quantity parseQuantity(String val) throws IOException {
       StringBuffer stringBufferVal = new StringBuffer();
       StringBuffer stringBufferUnit = new StringBuffer();
       int state=0;
       for (int i=0;i<val.length();i++) {
           if (val.charAt(i) == STARTING_BRACKET) {
			state=1;
		} else if (val.charAt(i) == ASTERISK) {
			state=2;
		} else if (val.charAt(i) == CLOSING_BRACKET) {
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
            File file = new File("dumpekm.txt");
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

    /**
     * Getter for property bufferId.
     * @return Value of property bufferId.
     */
    public int getBufferId() {
        return bufferId;
    }

}
