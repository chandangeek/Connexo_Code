/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataDumpParser.java
 *
 * Created on 24 januari 2005, 14:12
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 *
 * @author  Koen
 */
public class DataDumpParser {

    String strFrame;

    public static final String ENERMET_DUMP_DATETIME_SIGNATURE="yy-MM-dd HH:mm";
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


    public Set<Integer> getBillingPoints() {
        Set<Integer> billingPoints = new HashSet<>();
        int index1 = -1;
        while (true) {
            index1 = strFrame.indexOf('*', index1 + 1);
            if (index1 == -1) {
                break;
            }

            int index2 = strFrame.indexOf('(', index1);
            try {
                billingPoints.add(Integer.parseInt(strFrame.substring(index1 + 1, index2)));
            } catch (NumberFormatException e) {
                // absorb
            }
        }
        return new TreeSet<>(billingPoints);
    }

    public int getBillingCounter() {
        return getBillingCounter(getBillingPoints());
    }

    public int getBillingCounter(Set<Integer> billingPoints) {
        Integer[] bplist = billingPoints.toArray(new Integer[billingPoints.size()]);
        for (int i = 0; i < bplist.length; i++) {
            int currentPoint = bplist[i];
            if ((i + 1) < bplist.length) {
                int nextPoint = bplist[i + 1];
                if (nextPoint > (currentPoint + 1)) {
                    return currentPoint;
                }
            } else {
                return currentPoint;
            }
        }
        return 0;
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

    private String findBaseRegister(String strRegister) {
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
        return strRegister.contains(strDelimiter);
    }

    private String doGetBaseRegister(String strRegister,String strDelimiter) {
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

    private Quantity parseQuantity(String val) {
       StringBuilder stringBufferVal = new StringBuilder();
       StringBuilder stringBufferUnit = new StringBuilder();
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
       }

        return new Quantity(new BigDecimal(stringBufferVal.toString()),
                                Unit.get(stringBufferUnit.toString()));
    }

    public String getStrFrame() {
        return strFrame;
    }

}