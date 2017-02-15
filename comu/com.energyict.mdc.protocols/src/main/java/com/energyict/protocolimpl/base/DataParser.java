/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataParser.java
 *
 * Created on 28 oktober 2004, 17:26
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class DataParser {

	TimeZone timeZone;

	/** Creates a new instance of DataParser */
	public DataParser() {
		this(null);
	}
	public DataParser(TimeZone timeZone) {
		this.timeZone=timeZone;
	}

	/*
	 *  Returns the Quantity object
	 *  @return Quantity
	 *  @throws DataParseException thrown when an exception happens
	 */
	public Quantity parseQuantityBetweenBrackets(byte[] data, int offset, int pos) throws IOException {
		String strData = doParseBetweenBrackets(data,offset,pos);
		BigDecimal bd;
		Unit unit;
		if (strData.indexOf('*') > 0) {
			// with unit info
			bd = new BigDecimal(strData.substring(0, strData.indexOf('*')));
			unit = Unit.get(strData.substring(strData.indexOf('*')+1));
		}
		else {
			// without unit info
			bd = new BigDecimal(strData);
			unit = Unit.get("");
		}
		return new Quantity(bd, unit == null ? Unit.getUndefined() : unit);

	}
	public String parseBetweenBrackets(byte[] data, int offset, int pos) throws IOException {
		return doParseBetweenBrackets(data,offset,pos);
	}
	public String parseBetweenBrackets(byte[] data, int offset) throws IOException {
		return doParseBetweenBrackets(data,offset,0);
	}
	public String parseBetweenBrackets(byte[] data) throws IOException {
		return doParseBetweenBrackets(data,0,0);
	}
	public String parseBetweenBrackets(String strExpression) throws IOException {
		return parseBetweenBrackets(strExpression,0);
	}
	public String parseBetweenBrackets(String strExpression,int pos) throws IOException {
		int fromIndex=0;
		if (pos < 0) {
			throw new DataParseException ("DataParse, parseBetweenBrackets, invalid position "+pos);
		}
		for (int i=-1;i<pos;i++) {
			int index = strExpression.indexOf('(',fromIndex);
			if (index == -1) {
				throw new DataParseException ("DataParse, parseBetweenBrackets, invalid position "+pos);
			}

			fromIndex = index+1;
		}
		return strExpression.substring(fromIndex,strExpression.indexOf(')',fromIndex));
	}

	public String doParseBetweenBrackets(byte[] data, int offset, int pos) throws IOException {
		int fromIndex=offset;
		if (pos < 0) {
			throw new DataParseException ("DataParse, parseBetweenBrackets, invalid position "+pos);
		}
		for (int i=-1;i<pos;i++) {
			int index = indexOf(data,(byte)'(',fromIndex);
			if (index == -1) {
				throw new DataParseException ("DataParse, parseBetweenBrackets, invalid position "+pos);
			}

			fromIndex = index+1;
		}
		return new String(ProtocolUtils.getSubArray(data,fromIndex,indexOf(data,(byte)')',fromIndex)-1));
	}

	private int indexOf(byte[] data,byte val,int fromIndex) {
        if(data != null){
            for (int i=fromIndex;i<data.length;i++) {
                if (data[i] == val) {
                    return i;
                }
            }
        }
		return -1;
	}

	/*
	 *  Parse a date time string from the following format YYYY,MM,DD,HH,MM,SS into a Date object
	 *  e.g. 2004,10,09,14,25,45 --> 9 Oktober 14h25'45"
	 *  @param String YYYY,MM,DD,HH,MM,SS
	 *  @return Date
	 *  @throws DataParseException thrown when an exception happens
	 */
	public Date parseDateTime(String strDateTime) throws IOException {
		StringTokenizer strTok = new StringTokenizer(strDateTime,",");
		if (strTok.countTokens() != 6) {
			throw new DataParseException("DataParse, parseDateTime, invalid pattern "+strDateTime);
		}
		if (this.timeZone == null) {
			throw new DataParseException("DataParse, parseDateTime, no TimeZone given! (timeZone==null)");
		}
		Calendar calendar = Calendar.getInstance(this.timeZone);
		calendar.clear();
		calendar.set(Calendar.YEAR,Integer.parseInt(strTok.nextToken()));
		calendar.set(Calendar.MONTH,Integer.parseInt(strTok.nextToken())-1);
		calendar.set(Calendar.DAY_OF_MONTH,Integer.parseInt(strTok.nextToken()));
		calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(strTok.nextToken()));
		calendar.set(Calendar.MINUTE,Integer.parseInt(strTok.nextToken()));
		calendar.set(Calendar.SECOND,Integer.parseInt(strTok.nextToken()));
		return calendar.getTime();
	}

}