
package com.energyict.protocolimpl.ametek;
//com.energyict.protocolimpl.ametek.Jem10

import com.energyict.protocol.ProtocolUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.ametek.JemProtocolConnection;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;

/**
 *
 * @author cju
 * 
 */
public class Jem10 extends Jem implements MessageProtocol  {

	final private static long TIMEOUT = 5000;
	/** Creates a new instance of SDKSampleProtocol */
	public Jem10() {
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {        

		//getLogger().info("call overrided method getProfileData("+from+","+includeEvents+")");  
		//getLogger().info("--> here we read the profiledata from the meter and construct a profiledata object");  

		pd = new ProfileData();

		Calendar calFrom = Calendar.getInstance(getTimeZone());
		calFrom.setTime(from);
		Calendar calTo = Calendar.getInstance(getTimeZone());

		int dateRangeCmd = 0xff;
		int dateRng = Calendar.getInstance(getTimeZone()).get(Calendar.DAY_OF_YEAR)-calFrom.get(Calendar.DAY_OF_YEAR);
		if(dateRng<45)
			dateRangeCmd = dateRng;
		if(to==null)
			to = new Date();

		calTo.setTime(to);

		byte[] send = new byte[]{(byte)getInfoTypeNodeAddressNumber(),0x44,0x01,0x10,0x02,(byte)dateRangeCmd,0x10,0x03};
		byte[] check = connection.getCheck(send, send.length);

		outputStream.write(ack);
		outputStream.write(send);
		outputStream.write(check);

//		int inval=0;
//		List inList = new ArrayList();
//		ByteArrayOutputStream dataOutStream = new ByteArrayOutputStream();
//
//		boolean in10 = false;
//
//		int byteCount = 0;
//		int pinval = 0;
//		int loc=0;
//		boolean notRead = true;
//		long timer = new Date().getTime() + TIMEOUT;
//		while (new Date().getTime() < timer){// && notRead){
//			while(inputStream.available()>0)
//			{
//				notRead=false;
//				inval = inputStream.read();
//				inList.add(new Integer(inval));
//
//				logData(inval);
//
//				if(!in10 && (pinval==0x10 && inval==0x17 || pinval==0x10 && inval==0x03))
//					in10 = true;
//				else if(in10)
//				{
//					if(byteCount>0)
//						in10=false;
//
//					byteCount++;
//
//					if(!in10)
//					{
//						byte[] ba = new byte[inList.size()-2 - loc];
//						for(int i=loc; i< inList.size()-2; i++)
//						{
//							ba[i-loc] = (((Integer)inList.get(i+2)).byteValue());
//
//							if(((Integer)inList.get(i)).byteValue()==0x10 && 
//									((Integer)inList.get(i+1)).byteValue()==0x02)
//								in10 = true;
//							else if(in10 && ((Integer)inList.get(i+1)).byteValue()!=0x10 &&
//									(((Integer)inList.get(i+2)).byteValue()==0x10 && 
//											((Integer)inList.get(i+3)).byteValue()==0x17 ||
//											((Integer)inList.get(i+2)).byteValue()==0x10 && 
//											((Integer)inList.get(i+3)).byteValue()==0x03)
//							)
//								in10=false;
//
//							if(in10)
//							{
//								if(((Integer)inList.get(i+2)).byteValue()!=0x10)
//									dataOutStream.write(Integer.parseInt(inList.get(i+2).toString()));
//								else if(((Integer)inList.get(i+2)).byteValue()==0x10 && ((Integer)inList.get(i+3)).byteValue()==0x10)
//								{
//									dataOutStream.write(Integer.parseInt(inList.get(i+3).toString()));
//									i++;
//									ba[i-loc] = (((Integer)inList.get(i+2)).byteValue());
//								}
//							}
//						}
//						if(!verifyCheck(ba))
//							throw new IOException("Invalid Checksum.");
//
//						in10 = false;
//
//						outputStream.write(new byte[]{0x06});
//
//						loc = inList.size();
//						byteCount = 0;
//						System.out.println();
//					}
//				}
//
//				if(pinval==0x10 && inval==0x10)
//					pinval = 0;
//				else
//					pinval = inval;
//			}
//		}
//
//		InputStream dataInStream = new ByteArrayInputStream(dataOutStream.toByteArray());
		ByteArrayInputStream bais = new ByteArrayInputStream(connection.receiveResponse().toByteArray());
		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		
//		while (bais.available()>0)
//		{
//			baos.write(bais.read());
//		}
//		for (int i=0; i<100; i++){
//			pd = new ProfileData();
//			ByteArrayInputStream tempbais = new ByteArrayInputStream(baos.toByteArray()); 
		processHeader(bais);
		processInterval(bais, calFrom, calTo);
//		}
		return pd;
	}

	protected String getRegistersInfo(int extendedLogging) throws IOException {
		//getLogger().info("call overrided method getRegistersInfo("+extendedLogging+")");
		//getLogger().info("--> You can provide info about meter register configuration here. If the ExtendedLogging property is set, that info will be logged.");
		return "1.register number.0.0.0.255 Misc";
	}

	protected void processInterval(InputStream byteStream, Calendar cal, Calendar calTo) throws IOException
	{
		int eventVal = 0;
		int len = 2;
		int eventIndicator = 0x8000;
		int intervalIndicator = 0x4000;
		int powerOutEvent = 0x40;
		int eiStatus = 0;
		Date startDate = cal.getTime();
		Date now = calTo.getTime();
		cal.setTimeInMillis(0);
		boolean noDate = true;
		Date startTime = cal.getTime();
		ArrayList dataList = new ArrayList();
		Date lastDate=null;

		ParseUtils.roundDown2nearestInterval(cal,getProfileInterval());

		long val = convertHexToLong(byteStream, len);

		while(byteStream.available()>0) {
			List values = new ArrayList();

			boolean readMore = false;
			if((val & eventIndicator) == eventIndicator) {
				eventVal = (int)(val & 0xff);
				readMore = true;
			}
			for(int i=0; i<channelCount; i++) {
				if (readMore)
					val = convertHexToLong(byteStream, len);
				else
					readMore = true;

				if((val & intervalIndicator) == intervalIndicator) {
					val = val ^ intervalIndicator;				
				}
				values.add(new BigDecimal(val));				
			}

			if(eventVal>0) {
				try {
					startTime = getShortDateFormatter().parse(convertHexToString(byteStream, 5, true));
					Date endTime = null;
					if(byteStream.available()>0 && !( ((val = convertHexToLong(byteStream, len))& intervalIndicator) == intervalIndicator)) {
						String s = Long.toHexString(val);
						while (s.length()<4)
							s="0"+s;
						s+= convertHexToString(byteStream,3,true);
						endTime = getShortDateFormatter().parse(s);
						val = convertHexToLong(byteStream, len);
					}
					
					if ((eventVal & powerOutEvent) == powerOutEvent) { //powerOutage
						eventVal=0;
						noDate = false;
						eiStatus = IntervalStateBits.POWERDOWN;
						if(endTime.getTime() - startTime.getTime() < getProfileInterval()){
							eiStatus = IntervalStateBits.POWERDOWN | IntervalStateBits.POWERUP;
							continue;
						}
						if(cal.getTime().getTime() >= startDate.getTime() && cal.getTime().before(now)) {
							IntervalData id = new IntervalData(cal.getTime(), eiStatus);
							id.addValues(values);
							pd.addInterval(id);
						}

						values = new ArrayList();
						cal.setTime(endTime);
						ParseUtils.roundUp2nearestInterval(cal, getProfileInterval());
						eiStatus = IntervalStateBits.POWERUP;
						for(int i=0; i<channelCount; i++) {
							values.add(new BigDecimal(0));
						}
					}
					else if((eventVal & 0x80) == 0x80) { //midnight
						eventVal=0;
						noDate = false;
						cal.setTime(startTime);
					}
					else{
						eventVal=0;
						Calendar tempCal = (Calendar)cal.clone();
						tempCal.setTime(startTime);
						ParseUtils.roundDown2nearestInterval(tempCal, getProfileInterval());
						if(lastDate==null || (lastDate!=null && tempCal.getTime().equals(lastDate) ||
								tempCal.getTime().before(lastDate)))
							continue;
					}
//					pd.addEvent(new MeterEvent(now,MeterEvent.APPLICATION_ALERT_START, "SDK Sample"));

				}
				catch(Exception e) {
					new IOException(e.getMessage());
				}
				
				eventVal = 0;
			}
			else if (byteStream.available()>0)
				val = convertHexToLong(byteStream, len);
			
			if(noDate){
				dataList.add(values);
			}
			else{
				if(dataList.size()>0){
					Calendar c = (Calendar)cal.clone();
					c.setTime(startTime);
					processList(dataList, c, startDate, now);
					dataList = new ArrayList();
				}
				if(cal.getTime().getTime() >= startDate.getTime() && cal.getTime().before(now)) {
					IntervalData id = new IntervalData(cal.getTime(), eiStatus);
					id.addValues(values);
					pd.addInterval(id);
				}
				lastDate = cal.getTime();
				cal.add(Calendar.SECOND, getProfileInterval());
				eiStatus=0;
			}
		}
	}

	private void processHeader(InputStream byteStream) throws IOException
	{
		try
		{
			time = getDateFormatter().parse(convertHexToString(byteStream, 12, false));
			byteStream.read(); //Eat day of week byte
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}

		channelCount = (int)convertHexToLong(byteStream, 1);

		if (getProfileInterval()<=0)
			throw new IOException("load profile interval must be > 0 sec. (is "+getProfileInterval()+")");

		for(int i=0; i<channelCount; i++)
			pd.addChannel(new ChannelInfo(i,i, null, null));


	}

	protected void processRegisters(InputStream byteStream) throws IOException
	{
		int startOffset = 0;
		int len = 2;
		int pos = startOffset;

		while(byteStream.available()>0)
		{
			int registerNumber = (int)convertHexToLongLE(byteStream, len);

			pos += len;

			int bt = byteStream.read();
			byteStream.skip(3);

			pos += 4;

			long val = convertHexToLongLE(byteStream, 4);

			pos += 4;

			RegisterValue rv = null;

			ObisCode ob = new ObisCode(1, registerNumber, 0, 0, 0, 0);

			int type = 0;

			if((bt & 0x80) == 0x80)
				type = 1;

			switch(type)
			{
			case 0:
				float f = val;		    	
				if((bt & 4) == 4 || (bt & 1) == 1)
					f *= .1;
				else if((bt & 8) == 8 || (bt & 2) == 2)
					f *= .01;
				else if((bt & 12) == 12 || (bt & 3) == 3)
					f *= .001;
				rv = new RegisterValue(ob, new Quantity(new BigDecimal(f), Unit.getUndefined()), new Date(), null , new Date(), time);
				break;
			case 1:
				Calendar cal = Calendar.getInstance(getTimeZone());
				cal.set(1990,0,1,0,0,0);
				Date tstamp = new Date (cal.getTimeInMillis() + (val*1000));
				time = tstamp;
				break;
			}

			if(rv!=null)
				registerValues.put(new Integer(ob.getB()), rv);			
		}

	}

	private String convertHexToString(InputStream byteStream, int length, boolean pad) throws IOException
	{
		String instr = "";
		for(int i=0; i<length; i++)
		{
			int inval = byteStream.read();
			String zeropad = "";
			if (pad && Integer.toHexString(inval & 0xff).length()<2)
				zeropad = "0";
			instr += zeropad + Integer.toHexString(inval & 0xff);
		}

		return instr; 	
	}

	/*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
	 *******************************************************************************************/   
	protected void retrieveRegisters() throws IOException
	{
		registerValues = new HashMap();

		int dateRangeCmd = 0xff;
//		if(to!=null)
//		dateRangeCmd = 0xff;

		byte[] send = new byte[]{(byte)getInfoTypeNodeAddressNumber(),0x52,0x06,0x10,0x02,(byte)dateRangeCmd,0x10,0x03};
		byte[] check = connection.getCheck(send, send.length);

		outputStream.write(ack);
		outputStream.write(send);
		outputStream.write(check);

//		int inval=0;
//		List inList = new ArrayList();
//		ByteArrayOutputStream dataOutStream = new ByteArrayOutputStream();
//
//		boolean in10 = false;
//
//		int byteCount = 0;
//		int pinval = 0;
//		int loc=0;
//
//		boolean notRead = true;
//		long timer = new Date().getTime() + TIMEOUT;
//		while (new Date().getTime() < timer && notRead){
//			while(inputStream.available()>0)
//			{
//				notRead=false;
//				inval = inputStream.read();
//				inList.add(new Integer(inval));
//
//				logData(inval);
//
//				if(!in10 && (pinval==0x10 && inval==0x17 || pinval==0x10 && inval==0x03))
//					in10 = true;
//				else if(in10)
//				{
//					if(byteCount>0)
//						in10=false;
//
//					byteCount++;
//
//					if(!in10)
//					{
//						byte[] ba = new byte[inList.size()-2 - loc];
//						for(int i=loc; i< inList.size()-2; i++)
//						{
//							ba[i-loc] = (((Integer)inList.get(i+2)).byteValue());
//
//							if(((Integer)inList.get(i)).byteValue()==0x10 && 
//									((Integer)inList.get(i+1)).byteValue()==0x02 &&
//									!(((Integer)inList.get(i+2)).byteValue()==0x10 && 
//											((Integer)inList.get(i+3)).byteValue()==0x03)
//							)
//								in10 = true;
//							else if(in10 && ((Integer)inList.get(i+1)).byteValue()!=0x10 &&
//									(((Integer)inList.get(i+2)).byteValue()==0x10 && 
//											((Integer)inList.get(i+3)).byteValue()==0x17 ||
//											((Integer)inList.get(i+2)).byteValue()==0x10 && 
//											((Integer)inList.get(i+3)).byteValue()==0x03)
//							)
//								in10=false;
//
//							if(in10)
//							{
//								if(((Integer)inList.get(i+2)).byteValue()!=0x10)
//									dataOutStream.write(Integer.parseInt(inList.get(i+2).toString()));
//								else if(((Integer)inList.get(i+2)).byteValue()==0x10 && ((Integer)inList.get(i+3)).byteValue()==0x10)
//								{
//									dataOutStream.write(Integer.parseInt(inList.get(i+3).toString()));
//									i++;
//									ba[i-loc] = (((Integer)inList.get(i+2)).byteValue());
//								}
//							}
//						}
//						if(!verifyCheck(ba))
//							throw new IOException("Invalid Checksum.");
//
//						in10 = false;
//
//						outputStream.write(new byte[]{0x06});
//
//						loc = inList.size();
//						byteCount = 0;
//						System.out.println();
//					}
//
//				}
//
//				if(pinval==0x10 && inval==0x10)
//					pinval = 0;
//				else
//					pinval = inval;
//			}
//		}
//
//		InputStream dataInStream = new ByteArrayInputStream(dataOutStream.toByteArray());
		ByteArrayInputStream bais = new ByteArrayInputStream(connection.receiveResponse().toByteArray());
		processRegisters(bais);

	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
		//getLogger().info("call overrided method getNumberOfChannels() (return 2 as sample)");
		//getLogger().info("--> report the nr of load profile channels in the meter here");
		if(this.channelCount==0){
			byte[] send = new byte[]{(byte)0xFE,0x56,0x08,0x10,0x02,0x10,0x03};

			byte[] check = connection.getCheck(send, send.length);

			outputStream.write(ack);
			outputStream.write(send);
			outputStream.write(check);

//			int inval=0;
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			boolean notRead = true;
//			long timer = new Date().getTime() + TIMEOUT;
//			while (new Date().getTime() < timer && notRead){
//				while(inputStream.available()>0)
//				{
//					notRead=false;
//					inval = inputStream.read();
//					baos.write(inval);
//				}
//			}
//			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ByteArrayInputStream bais = new ByteArrayInputStream(connection.receiveResponse().toByteArray());
			bais.skip(8);
			this.channelCount=bais.read();
		}
		return this.channelCount;
	}

	public Date getTime() throws IOException {
		//getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
		//getLogger().info("--> request the metertime here");
		byte[] send = new byte[]{(byte)0xFE,0x54,0x02,0x10,0x02,0x10,0x03};

		byte[] check = connection.getCheck(send, send.length);

		outputStream.write(ack);
		outputStream.write(send);
		outputStream.write(check);

//		int inval=0;
//		List inList = new ArrayList();
//
//		boolean in10 = false;
//
//		String instr = "";
//		int pinval = 0;
//
//		boolean notRead = true;
//		long timer = new Date().getTime() + TIMEOUT;
//		while (new Date().getTime() < timer && notRead){
//			while(inputStream.available()>0)
//			{
//				notRead=false;
//				inval = inputStream.read();
//				inList.add(new Integer(inval));
//
//				logData(inval);
//
//				if(!in10 && pinval==0x10 && inval==0x2)
//					in10 = true;
//				else if(in10)
//				{
//					if(inval!=0x10 || (inval==0x10 && !instr.endsWith("6")))
//						instr += Integer.toHexString(inval);
//				}
//
//				if(instr.length()==12)
//					in10=false;
//
//				pinval = inval;
//			}
//		}
//
//		if(inList.size()<1)
//			throw new IOException("Meter did not respond.");
//
//		byte[] ba = new byte[inList.size()-2];
//		for(int i=0; i< inList.size()-2; i++)
//			ba[i] = (((Integer)inList.get(i+2)).byteValue());
//
//		if(!verifyCheck(ba))
//			throw new IOException("Invalid Checksum.");
		String instr = "";
		ByteArrayInputStream bais = new ByteArrayInputStream(connection.receiveResponse().toByteArray());
		for (int i = 0; i<12; i++)
			instr += Integer.toHexString(bais.read());

		try
		{
			Date date = getDateFormatter().parse(instr);

			return date;
		}
		catch (Exception e)
		{
			throw new IOException(e.getMessage());
		}
	}


	public void setTime() throws IOException {
		//getLogger().info("call setTime() (this method is called automatically when needed)");
		//getLogger().info("--> sync the metertime with the systemtime here");
		Calendar cal = Calendar.getInstance();
		int yy = cal.get(Calendar.YEAR)%100;
		//Month starts with 1 on meter, 0 in java (for Jan)
		int mm = cal.get(Calendar.MONTH)+1;
		int dd = cal.get(Calendar.DAY_OF_MONTH);
		int hh = cal.get(Calendar.HOUR_OF_DAY);
		int mn = cal.get(Calendar.MINUTE);
		int ss = cal.get(Calendar.SECOND);
		int w = cal.get(Calendar.DAY_OF_WEEK); 

		byte[] send = new byte[]{(byte)0xFE,0x54,0x05,0x10,0x02,
				(byte)(yy/10), (byte)(yy%10),
				(byte)(mm/10), (byte)(mm%10),
				(byte)(dd/10), (byte)(dd%10),
				(byte)(hh/10), (byte)(hh%10),
				(byte)(mn/10), (byte)(mn%10),
				(byte)(ss/10), (byte)(ss%10),
				(byte)(w), 0x10,0x03};

		byte[] check = connection.getCheck(send, send.length);

		outputStream.write(ack);
		outputStream.write(send);
		outputStream.write(check);

		ByteArrayInputStream bais = new ByteArrayInputStream(connection.receiveResponse().toByteArray());
		int inval=0;

		inval = bais.read();
		if (inval!=6)
			throw new IOException("Failed to set time");
	}

	public String getProtocolVersion() {
		////getLogger().info("call getProtocolVersion()");
		return "$Revision: 1.3 $";
	}


	public String getFirmwareVersion() throws IOException, UnsupportedException {
		//getLogger().info("call getFirmwareVersion()");
		//getLogger().info("--> report the firmware version and other important meterinfo here");
		System.out.println();
		byte[] send = new byte[]{(byte)getInfoTypeNodeAddressNumber(),0x06,0x01,0x10,0x02,0x10,0x03};
		byte[] check = connection.getCheck(send, send.length);

		outputStream.write(ack);
		outputStream.write(send);
		outputStream.write(check);

//		int inval=0;
//		int pinval=0;
//		List inList = new ArrayList();
//
//		boolean in10 = false;
//
//		String instr = "";
//		int count = 0;
//		boolean notRead = true;
//		long timer = new Date().getTime() + TIMEOUT;
//		while (new Date().getTime() < timer && notRead){
//			while(inputStream.available()>0)
//			{
//				notRead=false;
//				inval = inputStream.read();
//				inList.add(new Integer(inval));
//
//				if(!in10 && pinval==0x10 && inval==0x2)
//					in10 = true;
//				else if(in10)
//				{
//					count++;
//					if(count<16)
//						continue;
//
//					char c = (char)inval;
//					if(inval!=0x10 || (inval==0x10 && instr.charAt(instr.length()-1)!=(char)0x10))
//						instr += String.valueOf(c);
//				}
//
//				if(instr.length()==8)
//					in10=false;
//
//				pinval=inval;
//			}
//		}
//
//		byte[] ba = new byte[inList.size()-2];
//		for(int i=0; i< inList.size()-2; i++)
//			ba[i] = (((Integer)inList.get(i+2)).byteValue());
//
//		if(!verifyCheck(ba))
//			throw new IOException("Invalid Checksum.");

		String instr = "";

		ByteArrayInputStream bais = new ByteArrayInputStream(connection.receiveResponse().toByteArray());
		bais.skip(15);
		for(int i = 0; i<8; i++){
			instr += String.valueOf((char)bais.read());
		}
		return instr;
	}


}
