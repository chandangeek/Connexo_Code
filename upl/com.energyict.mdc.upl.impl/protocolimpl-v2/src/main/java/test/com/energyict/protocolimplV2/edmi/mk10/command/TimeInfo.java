/*
 * TimeInfo.java
 *
 * Created on 27 maart 2006, 14:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package test.com.energyict.protocolimplV2.edmi.mk10.command;

import test.com.energyict.protocolimplV2.edmi.mk10.MK10Generic;
import test.com.energyict.protocolimplV2.edmi.mk10.core.DateTimeBuilder;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author koen
 */
public class TimeInfo {

	//private final int CLOCK_COMMAND_READ_DST = 0xF061; // dst corrected time
	private final int CLOCK_COMMAND_READ_STD = 0xF03D; // std time
	private final int CLOCK_COMMAND_WRITE_STD = 0xF03D; // std time
	private final int DST_USED = 0xF015;
	MK10Generic mk10;

	/** Creates a new instance of TimeInfo */
	public TimeInfo(MK10Generic mk10) {
		this.mk10=mk10;
	}

	// Not used for the moment. All reported times are standard time!
	//    public void verifyTimeZone() throws IOException {
	//        if (mk10.getCommandFactory().getReadCommand(DST_USED).getRegister().getBigDecimal().intValue()==0) {
	//            return ProtocolUtils.getWinterTimeZone(mk10.getTimeZone());
	//        }
	//        else
	//            return mk10.getTimeZone();
	//    }

	public void setTime() throws IOException {
		byte[] data = DateTimeBuilder.getDDMMYYHHMMSSDataFromDate(new Date(), mk10.getTimeZone());
		mk10.getCommandFactory().writeCommand(CLOCK_COMMAND_WRITE_STD, data);
	}

	public Date getTime() throws IOException {
		return mk10.getCommandFactory().getReadCommand(CLOCK_COMMAND_READ_STD).getRegister().getDate();
	}

	//    private void start() {
	//        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("ECT"));
	//        cal1.set(Calendar.HOUR_OF_DAY,15);
	//        cal1.getTime().getTime();
	//        System.out.println("KV_DEBUG>"+cal1.getTime());
	//
	//        Calendar cal3 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
	//        cal3.setTime(cal1.getTime());
	//        System.out.println("KV_DEBUG>"+cal3.getTime()+", "+cal3.getTimeInMillis()/1000);
	//        System.out.println("KV_DEBUG>"+cal3.get(Calendar.HOUR_OF_DAY));
	//
	//        Calendar cal = Calendar.getInstance();
	//        cal.setTime(cal1.getTime());
	//
	//        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
	//        cal2.clear();
	//        cal2.set(Calendar.YEAR,cal.get(Calendar.YEAR));
	//        cal2.set(Calendar.MONTH,cal.get(Calendar.MONTH));
	//        cal2.set(Calendar.DAY_OF_MONTH,cal.get(Calendar.DAY_OF_MONTH));
	//        cal2.set(Calendar.HOUR_OF_DAY,cal.get(Calendar.HOUR_OF_DAY));
	//        cal2.set(Calendar.MINUTE,cal.get(Calendar.MINUTE));
	//        cal2.set(Calendar.SECOND,cal.get(Calendar.SECOND));
	//        System.out.println("KV_DEBUG>"+cal2.getTime()+", "+cal2.getTimeInMillis()/1000);
	//
	//
	//
	//        TimeZone tz = ProtocolUtils.getWinterTimeZone(TimeZone.getTimeZone("ECT"));
	//        System.out.println(tz.useDaylightTime());
	//
	//    }
	//
	//    public static void main(String[] args) {
	//        TimeInfo to = new TimeInfo(null);
	//        to.start();
	//    }

}
