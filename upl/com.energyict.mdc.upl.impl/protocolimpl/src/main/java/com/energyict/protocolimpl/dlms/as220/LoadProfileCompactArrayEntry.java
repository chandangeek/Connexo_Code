package com.energyict.protocolimpl.dlms.as220;

import java.util.*;

import com.energyict.protocol.ProtocolUtils;

public class LoadProfileCompactArrayEntry {

	
	int value;
	int intervalInSeconds;
	boolean dst;
	boolean partialValue;
	Date date;
	Date time;
	boolean badTime;
	int markerType;
	
	final int VALUE=0;
	final int VALUE_CORRUPTED=1;
	final int VALUE_DATE=2;
	final int VALUE_TIME=3;
	final int[] intervals =new int[]{10*60,15*60,30*60,60*60}; // in seconds 
	

	
	public LoadProfileCompactArrayEntry(long rawValue,TimeZone timeZone) {
		
		int tempBit3130 = (int)((rawValue>>30) & 0x03);
		
		dst = (int)((value>>21)&1) == 1;
		badTime = (int)((value>>29)&1) == 1;
		
		switch(tempBit3130) {
		
			case VALUE_CORRUPTED:		
			case VALUE: {
				value = (int)(rawValue & 0x1FFFFF);
				intervalInSeconds = intervals[(int)((value>>22)&0x3)];
			} break;
			
			case VALUE_DATE: {
				markerType = (int)(value>>24) & 0x7;
				int day = (int)(value) & 0x1F;
				int month = (int)(value>>5) & 0xF;
				int year = (int)((value>>9) & 0x7F)+2000;
				Calendar calendar = ProtocolUtils.getCleanCalendar(timeZone);
				calendar.set(Calendar.DATE, day);
				calendar.set(Calendar.MONTH, month-1);
				calendar.set(Calendar.YEAR, year);
				date = calendar.getTime();
			} break;
			
			case VALUE_TIME: {
				markerType = (int)(value>>24) & 0x7;
				int seconds = (int)(value) & 0x3F;
				int minutes = (int)(value>>6) & 0x3F;
				int hours = (int)(value>>12) & 0x1F;
				Calendar calendar = ProtocolUtils.getCleanCalendar(timeZone);
				calendar.set(Calendar.SECOND, seconds);
				calendar.set(Calendar.MINUTE, minutes);
				calendar.set(Calendar.HOUR_OF_DAY, hours);
				time = calendar.getTime();
			} break;
		}
	}

//    public LoadProfileCompactArrayEntry() {
//    }
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new LoadProfileCompactArrayEntry()));
//    } 	
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileCompactArrayEntry:");
        strBuff.append("   partialValue="+isPartialValue()+", ");
        strBuff.append("   badTime="+isBadTime()+", ");
        strBuff.append("   dst="+isDst()+", ");
        strBuff.append("   intervalInSeconds="+getIntervalInSeconds()+", ");
        strBuff.append("   date="+getDate()+", ");
        strBuff.append("   time="+getTime()+", ");
        strBuff.append("   markerType="+getMarkerType()+", ");
        strBuff.append("   value="+getValue());
        
        return strBuff.toString();
    }
    
	public int getValue() {
		return value;
	}

	public int getIntervalInSeconds() {
		return intervalInSeconds;
	}

	public boolean isDst() {
		return dst;
	}

	public boolean isPartialValue() {
		return partialValue;
	}



	public boolean isBadTime() {
		return badTime;
	}

	public int getMarkerType() {
		return markerType;
	}

	public Date getDate() {
		return date;
	}

	public Date getTime() {
		return time;
	}
	
	
	
}
