package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.RegisterFactory;

/** @author fbo */

public class ProfileParser {

	/* debug */
	//private boolean parseFirstDayOnly = true;// KV 22072005 unused code
	public boolean DBG = false;

	private PPM ppm = null;
	private RegisterFactory rFactory = null;
	private Date meterTime;

	/* constants Byte Size */
	static final int AA_BS = 3;

	Assembler[] assemblerTable = new Assembler[256];
	Assembly assembly = null;

	ProfileData targetProfileData = null;
	//IntervalData targetIntervalData = null;// KV 22072005 unused code

	private int nrOfChannels = 1;
	private int integrationPeriod = 30;
	private ScalingFactor scalingFactor = null;
	private LoadProfileDefinition loadDef = null;

	private FFAssembler ffAssembler = new FFAssembler();
	private DayAssembler dayAssembler = new DayAssembler();
	private NumberAssembler numberAssembler = new NumberAssembler();
	private AAAssembler aaAssembler = new AAAssembler();

	public ProfileParser( PPM ppm, RegisterFactory registerFactory, 
			Date meterTime, LoadProfileDefinition loadDef, boolean dbg) 
															throws IOException {

		this.DBG = dbg;
		
		this.ppm = ppm;
		this.rFactory = registerFactory;
		this.meterTime = meterTime;
		this.loadDef = loadDef;
		nrOfChannels = loadDef.getNrOfChannels();

		if (!DBG) {
			integrationPeriod = rFactory.getIntegrationPeriod().intValue() * 60;
			scalingFactor = rFactory.getScalingFactor();
		} else {
			integrationPeriod = 1800;
			scalingFactor = ScalingFactor.REGISTER_CATEGORY_3;
		}

		setAssemblerTable(0xFF, 0xFF, ffAssembler);
		setAssemblerTable(0xAA, 0xAA, aaAssembler);
		setAssemblerTable(0xE4, 0xE4, dayAssembler);
		setAssemblerTable(0x00, 0x99, numberAssembler);

	}

	public void setInput(InputStream inputStream) {
		this.assembly = new Assembly(inputStream);
	}

	private void setAssemblerTable(int from, int to, Assembler assembler) {
		for (int i = from; i <= to; i++) {
			if (i >= 0 && i < assemblerTable.length) {
				assemblerTable[i] = assembler;
			}
		}
	}

	private int getNrOfChannels() throws IOException {
		return nrOfChannels;
	}

	public void match() throws IOException {

		int character = assembly.read();
		do {
			assembly.push(new Byte((byte) character));
			assemblerTable[character].workOn(assembly);
			character = assembly.read();
		} while (character != -1);

		if( assembly.getTarget() != null )
			dayAssembler.createProfileData( (Day) assembly.getTarget() );
		
		if( ! ((Day)assembly.getTarget()).isEmpty() )
			System.out.println(assembly.getTarget());
         
	}

	public ProfileData getProfileData() throws IOException {
		if( targetProfileData == null )
		targetProfileData = new ProfileData();
		targetProfileData.setChannelInfos(loadDef.toChannelInfoList());
		match();
		
		return targetProfileData;
	}

	public static long hex2dec(byte value) {
		return Long.parseLong(Long.toHexString(value & 0xFF));
	}

public interface Assembler {
	public abstract void workOn(Assembly a) throws IOException;
}

class FFAssembler implements Assembler {
	public void workOn(Assembly ta) {
		((Byte) ta.pop()).byteValue();
	}
}

class DayAssembler implements Assembler {

	int dayNr = 0;

	public void workOn(Assembly ta) throws IOException {

		if( ta.getTarget() != null ) {
			createProfileData( (Day) ta.getTarget() );
			if(DBG) System.out.println( "Day\n" + ta.getTarget() );
		}
		
		((Byte) ta.pop()).byteValue();
		byte[] date = new byte[2];
		assembly.read(date, 0, 2);
		Day day = new Day((int) hex2dec(date[0]), (int) hex2dec(date[1]));

		ta.setTarget(day);
		dayNr++;
	}
	
	/* Create the profile data after a complete day has been parsed.  The time 
	 * is actually not that important, but it's just a good time. */
	public void createProfileData( Day aDay ){
		
		if( aDay.isEmpty() ) return;
		
		for( int hi = 0; hi < aDay.reading.length; hi ++ ){
			
			if( ! aDay.reading[hi].isEmpty() ){			
				IntervalData i = new IntervalData(aDay.reading[hi].date);
				
				if(aDay.status[hi] != null )
					i.setEiStatus(aDay.status[hi].getEIStatus());
			
				for( int vi = 0; vi < aDay.reading[hi].value.length; vi ++ )
					i.addValue( aDay.reading[hi].value[vi] );
			
				targetProfileData.addInterval(i);
			}
			
		}
	}

}

class NumberAssembler implements Assembler {

	int byteNr;
	int[] val = null;

	public void workOn(Assembly ta) throws IOException {

		Day day = (Day) ta.getTarget();
		int tempVal = (int) hex2dec(((Byte) ta.pop()).byteValue());

		if (day == null) return;

		getVal()[byteNr] = (byte) tempVal;
		byteNr++;

		if (byteNr != (nrOfChannels * 3 + 1)) return;
		
		if (day.readIndex < 48  // TODO can be 49 too ... // sh*t!
				&& day.reading[day.readIndex].date.before(meterTime) ) { 
               
			/* 1) create a status object */
			day.status[day.readIndex] = new LoadProfileStatus((byte) getVal()[0]);

			/* 2) create a reading */
			for (int vi = 0; vi < nrOfChannels; vi++) {
				day.reading[day.readIndex].value[vi] = constructValue(
						getVal(), (vi * 3) + 1);

			}

			/* 3) some debugging info */
			day.readingString[day.readIndex] = " ->"
					+ getVal()[0] + " " + getVal()[1] + " "
					+ getVal()[2] + " " + getVal()[3];

		}
		byteNr = 0;
		day.readIndex++;
	
	}

	int[] getVal() {
		if (val == null)
			val = new int[(nrOfChannels * 3) + 1];
		return val;
	}

        
// KV 22072005 unused code???????????        
//	private void createIntervalData(Date endDate, int[] iArray)
//			throws IOException {
//		IntervalData i = ProfileParser.this.targetIntervalData = new IntervalData(endDate);
//		i.setEiStatus(constructStatus(iArray).getEIStatus());
//		for (int ci = 0; ci < nrOfChannels; ci++)
//			i.addValue(constructValue(iArray, 1 + ci));
//
//	}

	private LoadProfileStatus constructStatus(int[] iArray) {
		return new LoadProfileStatus((byte) iArray[0]);
	}

	private BigDecimal constructValue(int[] iArray, int i)
			throws IOException {
		long v = iArray[i] * 10000;
		v += (iArray[i + 1] * 100);
		v += iArray[i + 2];
		return scalingFactor.toProfileNumber(v);
	}

}

class AAAssembler implements Assembler {

	public void workOn(Assembly ta) throws IOException {
		
		ta.pop();	/* clear Stack, and NumberAssembler */
		ProfileParser.this.numberAssembler.byteNr = 0;
		
		System.out.println( assembly );
		
		byte[] jmpSize = new byte[2]; ta.read(jmpSize, 0, 2);
		
		long jmp = Long.parseLong( PPMUtils.toHexaString( jmpSize[1] ) 
				                 + PPMUtils.toHexaString( jmpSize[0] ), 16) 
					- 3;
		
		System.out.println( "jump Size = " + jmp );
		
        if (ta.getTarget() != null){/* Calculate number of hours under jump */
            Day aDay = (Day) ta.getTarget();
            aDay.readIndex += ( jmp + 3 ) /  ( 1 + ( 3 * nrOfChannels));
        } 
        
        
		for (int i = 0; i < jmp; i++)
			ta.read();
		
		System.out.println( assembly );
        
	}

}

/** This class is mainly meant for debugging purposes, it can display itself
 * in an pretty and structured way. */
class Day {

	int readIndex = 0;

	int day = 0;
	int month = 0;

	LoadProfileStatus[] status = null;
	Interval[] reading = null;
	String[] readingString = null;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM H:mm");

	public Day(int day, int month) throws IOException {
		Calendar c = ProtocolUtils.getCalendar( ppm.getTimeZone() );
		c.set(c.get(Calendar.YEAR), month - 1, day, 0, 0, 0);

		int iSec = ProfileParser.this.integrationPeriod;
		int iPerDay = 86400 /* =secs/day */ / iSec + 1;

		this.day = day;
		this.month = month;
		
		reading = new Interval[iPerDay];
		status = new LoadProfileStatus[iPerDay];
		readingString = new String[iPerDay];

		for (int i = 0; i < iPerDay; i++) {
			c.add(Calendar.SECOND, iSec);
			reading[i] = new Interval();
			reading[i].date = c.getTime();
		}
	}
	
	boolean isEmpty( ){
		for( int i = 0; i < reading.length; i ++ )
			if( ! reading[i].isEmpty() ) return false;
		return true;		
	}

	class Interval {
		Date date;
		BigDecimal[] value;

		Interval() {
			value = new BigDecimal[loadDef.getNrOfChannels()];
		}
		
		boolean isEmpty( ){
			for( int i = 0; i < value.length; i ++ )
				if( value[i] != null ) return false;
			return true;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("[ ");
			for (int i = 0; i < value.length; i++)
				sb.append(value[i] + " ");

			sb.append(" ]");
			return sb.toString();
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Day :: day " + day + " month " + month + "\n");

		for (int i = 0; i < status.length; i++) {
			sb.append("[" + sdf.format(reading[i].date) + "]#");

			for (int ii = 0; ii < reading[i].value.length; ii++) {
				sb.append(" [" + reading[i].value[ii] + "]");
				sb.append(" [" + loadDef.toList().get(ii) + "]");
			}

			sb.append(" - " + status[i]);
			sb.append(" " + readingString[i] + "\n");
		}
		sb.append("\n");
		return sb.toString();
	}

}

}


