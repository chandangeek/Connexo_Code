package com.energyict.protocolimpl.dlms.as220;


public class LoadProfileCompactArrayEntry {

	private static final int DEBUG=0;

	int value;
	int intervalInSeconds;
	boolean dst;
	boolean badTime;

	int markerType;
	int bit3130;

	int seconds;
	int minutes;
	int hours;
	int day;
	int month;
	int year;

	final int VALUE=0;
	final int VALUE_PARTIAL=1;
	final int VALUE_DATE=2;
	final int VALUE_TIME=3;
	final int[] intervals =new int[]{600,900,1800,3600}; // in seconds

	public LoadProfileCompactArrayEntry(long rawValue) {

		bit3130 = (int)((rawValue>>30) & 0x03);
		dst = (int)((rawValue>>21)&1) == 1;
		badTime = (int)((rawValue>>29)&1) == 1;

		switch(bit3130) {

			case VALUE_PARTIAL:
			case VALUE: {
				value = (int)(rawValue & 0x1FFFFF);
				intervalInSeconds = intervals[(int)((rawValue>>22)&0x3)];
			} break;

			case VALUE_DATE: {
				markerType = (int)(rawValue>>24) & 0x7;
				day = (int)(rawValue) & 0x1F;
				month = ((int)(rawValue>>5) & 0xF)-1;
				year = (int)((rawValue>>9) & 0x7F)+2000;
				if (DEBUG>=1) {
					System.out.println("KV_DEBUG> "+day+"/"+month+"/"+year);
				}
			} break;

			case VALUE_TIME: {
				markerType = (int)(rawValue>>24) & 0x7;
				seconds = (int)(rawValue) & 0x3F;
				minutes = (int)(rawValue>>6) & 0x3F;
				hours = (int)(rawValue>>12) & 0x1F;
				if (DEBUG>=1) {
					System.out.println("KV_DEBUG> "+hours+":"+minutes+":"+seconds);
				}
			} break;
		}
	}

	public boolean isValue() {
		return bit3130 == 0;
	}
	public boolean isPartialValue() {
		return bit3130 == 1;
	}
	public boolean isDate() {
		return bit3130 == 2;
	}
	public boolean isTime() {
		return bit3130 == 3;
	}

	public boolean isStartOfLoadProfile() {
		return markerType == 0;
	}
	public boolean isChangeOfIntegrationtime() {
		return markerType == 1;
	}
	public boolean isPowerOff() {
		return markerType == 2;
	}
	public boolean isPowerOn() {
		return markerType == 3;
	}
	public boolean isChangeclockOldTime() {
		return markerType == 4;
	}
	public boolean isChangeclockNewTime() {
		return markerType == 5;
	}


//    public LoadProfileCompactArrayEntry() {
//    }
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new LoadProfileCompactArrayEntry()));
//    }

    @Override
	public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        if ((bit3130 == VALUE) || (bit3130 == VALUE_PARTIAL)) {
            strBuff.append("partialValue="+isPartialValue()+", ");
            strBuff.append("badTime="+isBadTime()+", ");
            strBuff.append("dst="+isDst()+", ");
            strBuff.append("intervalInSeconds="+getIntervalInSeconds()+", ");
            strBuff.append("value="+getValue());
        }
        else if (bit3130 == VALUE_DATE) {
            strBuff.append("date="+day+"/"+month+"/"+year+", ");
            strBuff.append("markerType="+getMarkerType());
        }
        else if (bit3130 == VALUE_TIME) {
            strBuff.append("time="+hours+":"+minutes+":"+seconds+", ");
            strBuff.append("markerType="+getMarkerType());
        }
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


	public boolean isBadTime() {
		return badTime;
	}

	public int getMarkerType() {
		return markerType;
	}

	public int getSeconds() {
		return seconds;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getHours() {
		return hours;
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}





}
