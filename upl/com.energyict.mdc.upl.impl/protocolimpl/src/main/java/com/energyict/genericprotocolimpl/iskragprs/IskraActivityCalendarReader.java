package com.energyict.genericprotocolimpl.iskragprs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.energyict.cbo.ApplicationException;
import com.energyict.genericprotocolimpl.common.tou.ActivityCalendar;
import com.energyict.genericprotocolimpl.common.tou.CosemCalendar;
import com.energyict.genericprotocolimpl.common.tou.DayProfile;
import com.energyict.genericprotocolimpl.common.tou.DayProfileSegment;
import com.energyict.genericprotocolimpl.common.tou.OctetString;
import com.energyict.genericprotocolimpl.common.tou.SeasonProfile;
import com.energyict.genericprotocolimpl.common.tou.SpecialDay;
import com.energyict.genericprotocolimpl.common.tou.WeekProfile;
import com.energyict.protocol.ProtocolUtils;

/**
 * 
 * @author igh
 * 
 * Changes:
 * GNA |02022009| Made some changes related to the TimeZone. The deviation bytes in the dateTime must be used otherwise everything is assumed
 * 					to be in GMT.
 *
 */

public class IskraActivityCalendarReader implements com.energyict.genericprotocolimpl.common.tou.ActivityCalendarReader {

	private ActivityCalendar activityCalendar;
	private TimeZone deviceTimeZone;
	private TimeZone localTimeZone;
	private int deviation;
	
	public IskraActivityCalendarReader(ActivityCalendar activityCalendar, TimeZone deviceTimeZone, TimeZone localTimeZone) {
		this.activityCalendar = activityCalendar;
		this.deviceTimeZone = deviceTimeZone;
		this.localTimeZone = localTimeZone;
		this.deviation = this.localTimeZone.getOffset(Calendar.getInstance(this.deviceTimeZone).getTimeInMillis())/60000;
	}
	
	public void read(InputStream stream) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(stream);
			Element topElement = document.getDocumentElement();
			read(topElement);
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	protected void read(Element element) throws IOException{
		String nodeName = element.getNodeName();
		if ("P2LPCTariff".equals(nodeName)){
			readActivityCalendar(element);
		}
		else
			throw new ApplicationException("Unknown tag found in xml userfile: " + element.getNodeName());
	}
	
	protected void readActivityCalendar(Element element) throws IOException{
		readCalendarName(element);
		readSeasons(element);
		readWeeks(element);
		readDays(element);
		readSpecialDays(element);
	}
	
	protected void readSpecialDays(Element element) {
		try {
			NodeList specialDays = element.getElementsByTagName("SpecialDay");
			for (int i = 0; i < specialDays.getLength(); i++) {
				Element specialDay = (Element) specialDays.item(i);
				int index = Integer.parseInt(specialDay.getAttribute("Index"));
				byte[] date = getSpecialDay(specialDay.getAttribute("Date"));
				int dayId = Integer.parseInt(specialDay.getAttribute("DayId"));
				//System.out.println(index + ", " + specialDay.getAttribute("Date") + ", " + dayId);
				activityCalendar.addSpecialDay(new SpecialDay(index, date, dayId));
			}
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid special day");
		}
		
	}
	
	protected void readDays(Element element) {
		try {
			NodeList days = element.getElementsByTagName("DayProfile");
			//System.out.println("days: " + days.getLength());
			for (int i = 0; i < days.getLength(); i++) {
				Element day = (Element) days.item(i);
				int dayId = Integer.parseInt(day.getAttribute("DayId"));
				//System.out.println(dayId);
				DayProfile dayProfile = new DayProfile(dayId);
				readDay(day, dayProfile);
				activityCalendar.addPassiveDayProfiles(dayProfile);
				
			}
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid day profile");
		}
	}
	
	protected void readDay(Element element, DayProfile dayprofile) {
		try {
			NodeList actions = element.getElementsByTagName("DayProfileAction");
			//System.out.println("actions: " + actions.getLength());
			for (int i = 0; i < actions.getLength(); i++) {
				Element action = (Element) actions.item(i);
				String startTime = action.getAttribute("StartTime");
				String script = action.getAttribute("Script");
				String selector = action.getAttribute("Selector");
				DayProfileSegment daySegment = 
					new DayProfileSegment(
							newDaySegment(startTime), 
							getScript(script),
							getSelector(selector));
				//System.out.println(startTime + ": " + ProtocolUtils.outputHexString(newDaySegment(startTime)));
				//System.out.println(script + ": " + ProtocolUtils.outputHexString(getScript(script)));
				//System.out.println(getSelector(selector));
				dayprofile.addSegment(daySegment);
			}
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid day profile");
		}
	}
	
	protected int getSelector(String selector) {
		try {
			return Integer.parseInt(selector);
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid day profile: selector should be a number");
		}
	}
	
	protected void readWeeks(Element element) {
		try {
			NodeList weeks = element.getElementsByTagName("WeekProfile");
			//System.out.println("weeks: " + weeks.getLength());
			for (int i = 0; i < weeks.getLength(); i++) {
				Element week = (Element) weeks.item(i);
				String name = week.getAttribute("Name");
				WeekProfile weekProfile = new WeekProfile(name);
				weekProfile.setMonday(Integer.parseInt(week.getAttribute("Monday")));
				weekProfile.setTuesday(Integer.parseInt(week.getAttribute("Tuesday")));
				weekProfile.setWednesday(Integer.parseInt(week.getAttribute("Wednesday")));
				weekProfile.setThursday(Integer.parseInt(week.getAttribute("Thursday")));
				weekProfile.setFriday(Integer.parseInt(week.getAttribute("Friday")));
				weekProfile.setSaturday(Integer.parseInt(week.getAttribute("Saturday")));
				weekProfile.setSunday(Integer.parseInt(week.getAttribute("Sunday")));
				//System.out.println(weekProfile);
				activityCalendar.addPassiveWeekProfiles(weekProfile);
			}
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid week profile");
		}
	}
	
	protected void readSeasons(Element element) {
		NodeList seasons = element.getElementsByTagName("Season");
		//System.out.println("seasons: " + seasons.getLength());
		for (int i = 0; i < seasons.getLength(); i++) {
			Element season = (Element) seasons.item(i);
			String name = season.getAttribute("Name");
			String start = season.getAttribute("Start");
			String weekProfile = season.getAttribute("WeekProfile");
			SeasonProfile seasonProfile = 
				new SeasonProfile(name, newSeason(start), weekProfile);
			//System.out.println(name + ", " + ProtocolUtils.outputHexString(newSeason(start)) + ", " + weekProfile);
			activityCalendar.addPassiveSeasonProfiles(seasonProfile);
		}
	}
	
	protected void readCalendarName(Element element) throws IOException{
		NodeList names = element.getElementsByTagName("Calendar");
		//System.out.println(names.getLength());
		if (names.getLength() != 0){
			String name = getName(((Element) names.item(0)).getAttribute("Name"));
			//System.out.println("name: " + name);
			activityCalendar.setPassiveCalendarName(new OctetString(name));
			String activateTime = ((Element) names.item(0)).getAttribute("ActivateTime");
			if ((activateTime != null) && (!"".equals(activateTime)))
				activityCalendar.setActivatePassiveCalendarTime(
						getActivateTime(activateTime));
		} else {
			throw new ApplicationException("No calendar name found");
		}	
	}
	
	protected String getName(String value) {
		try {
			int val = Integer.parseInt(value);
			if (val > 255)
				throw new ApplicationException("Calendar name should be a number smaller then 256");
			return "" + (char) ProtocolUtils.convertHexLSB(val) + 
		    (char) ProtocolUtils.convertHexMSB(val);
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Calendar name is not a number: " + value);
		}
	}
	
	protected CosemCalendar getActivateTime(String value) throws IOException {
		try {
			
			int index = value.indexOf('.');
			int day = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf('.');
			int month = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf(' ');
			int year = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			int hours = Integer.parseInt(value.substring(0, 2));
			int minutes = Integer.parseInt(value.substring(3, 5));
			int seconds = Integer.parseInt(value.substring(6, 8));
			//System.out.println("seconds = " + seconds);
			
			byte[] bytes = new byte[12];
			bytes[0]= (byte) (year >> 8);
			bytes[1] = (byte) year;
			bytes[2]= (byte) month;	//month
			bytes[3]= (byte) day;		//day
			bytes[4]= (byte) 0xFF;
			bytes[5]= (byte) hours;
			bytes[6]= (byte) minutes;
			bytes[7]= (byte) seconds;
			bytes[8]= (byte) 0x00;
			bytes[9]= (byte) ((this.deviation>>8)&0xFF);
			bytes[10]= (byte) (this.deviation&0xFF);
			bytes[11]= (byte) 0x00;
			return new CosemCalendar(new OctetString(bytes));
		}
		catch (IndexOutOfBoundsException e) {
			throw new ApplicationException("Invalid activate time");
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid activate time");
		}
	}
	
	protected byte[] getScript(String value) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(value, ".");
			if (tokenizer.countTokens() != 6)
				throw new ApplicationException("Invalid DayProfileAction Script");
			byte[] bytes = new byte[6];
			bytes[0]= (byte) Integer.parseInt(tokenizer.nextToken());
			bytes[1]= (byte) Integer.parseInt(tokenizer.nextToken());
			bytes[2]= (byte) Integer.parseInt(tokenizer.nextToken());
			bytes[3]= (byte) Integer.parseInt(tokenizer.nextToken());
			bytes[4]= (byte) Integer.parseInt(tokenizer.nextToken());
			bytes[5]= (byte) Integer.parseInt(tokenizer.nextToken());
			return bytes;
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid DayProfileAction Script");
		}
	}
	
	protected byte[] getSpecialDay(String value) {
		try {
			int index = value.indexOf('.');
			int day = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf('.');
			int month = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf(' ');
			int year = Integer.parseInt(value.substring(0, index));
			
			byte[] bytes = new byte[12];
			bytes[0]= (byte) (year >> 8);
			bytes[1] = (byte) year;
			bytes[2]= (byte) month;	//month
			bytes[3]= (byte) day;		//day
			bytes[4]= (byte) 0xFF;
			return bytes;
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid special day date: + " + value);
		}
	}
	
	protected byte[] newDaySegment(String value) {
		try {
			int index = value.indexOf(':');
			int hour = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf(':');
			int min = Integer.parseInt(value.substring(0, index));
			
			
			byte[] bytes = new byte[4];
			bytes[0] = (byte) hour;
			bytes[1] = (byte) min;
			bytes[2]= (byte) 0x00;
			bytes[3]= (byte) 0x00;
			return bytes;
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid DayProfileAction StartTime");
		}
	}
	
	protected byte[] newSeason(String value) {
		try {
			int index = value.indexOf('.');
			int day = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf('.');
			int month = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf(' ');
			int year = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			index = value.indexOf(' ');
			int dayOfWeek = Integer.parseInt(value.substring(0, index));
			value = value.substring(index + 1);
			int hours = Integer.parseInt(value.substring(0, 2));
			int minutes = Integer.parseInt(value.substring(3, 5));
			int seconds = Integer.parseInt(value.substring(6, 8));
			
			byte[] bytes = new byte[12];
			bytes[0]= (byte) (year >> 8);
			bytes[1] = (byte) year;
			bytes[2]= (byte) month;	//month
			bytes[3]= (byte) day;		//day
			bytes[4]= (byte) dayOfWeek;
			bytes[5] = (byte) hours;
			bytes[6] = (byte) minutes;
			bytes[7] = (byte) seconds;
			bytes[8]= (byte) 0x00;
			bytes[9]= (byte) ((this.deviation>>8)&0xFF);
			bytes[10]= (byte) (this.deviation&0xFF);
			bytes[11]= (byte) 0x00;
			return bytes;
		}
		catch (NumberFormatException e) {
			throw new ApplicationException("Invalid season start");
		}
	}
	
	private TimeZone getTimeZone(){
		return this.deviceTimeZone;
	}
	
	public static void main(String args[]) throws Exception {
		
		int t = TimeZone.getTimeZone("Europe/Brussels").getOffset(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis())/3600000;
		System.out.println(t);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"));
		
		
		
		System.out.println("GMT: " + cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("GMT offset in minutes; " + cal.getTimeZone().getRawOffset());
		System.out.println("Europe: " + cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("Europe offset in minutes: " + cal2.getTimeZone().getRawOffset());
		System.out.println("Europe offset: " + cal2.getTimeZone().useDaylightTime());
		
//		InputStream stream = new FileInputStream(new File("C:/Iskra/tariff.xml"));
//		IskraActivityCalendarReader reader = new IskraActivityCalendarReader(new ActivityCalendar(), null);
//		reader.read(stream);
		
		/*CosemCalendar cal = new CosemCalendar(new OctetString("7:d7:c:5:4:e:35:e:0:80:0:0:"));
		
		byte[] bytes = new byte[12];
		bytes[0]= (byte) 0xFF;
		bytes[1]= (byte) 0xFF;
		bytes[2]= (byte) 0x01;
		bytes[3]= (byte) 0x01;
		bytes[4]= (byte) 0xFF;
		bytes[5]= (byte) 0x00;
		bytes[6]= (byte) 0x00;
		bytes[7]= (byte) 0x00;
		bytes[8]= (byte) 0x00;
		bytes[9]= (byte) 0x00;
		bytes[10]= (byte) 0x00;
		bytes[11]= (byte) 0x00;
		OctetString octetString = new OctetString(bytes);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 65535);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		CosemCalendar cosemCalendar = new CosemCalendar(calendar, true);
	
		
		//CosemCalendar cal2 = new CosemCalendar(new OctetString("FFFF0101FF00000000000000"));
		CosemCalendar cal2 = new CosemCalendar(octetString);
		System.out.println(cal.getCalendar().getTime() + ", " + ProtocolUtils.outputHexString(cal.getOctetString().getOctets()));
		System.out.println(cal2.getCalendar().getTime() + ", " + ProtocolUtils.outputHexString(cal2.getOctetString().getOctets()));
		System.out.println(cosemCalendar.getCalendar().getTime() + ", " + ProtocolUtils.outputHexString(cosemCalendar.getOctetString().getOctets()));
		
		CosemCalendar cal3 = new CosemCalendar(new OctetString(bytes2));
		System.out.println(cal3.getCalendar().getTime() + ", " + ProtocolUtils.outputHexString(cal3.getOctetString().getOctets()));
		
		*/
		
		/*System.out.println(
				ProtocolUtils.outputHexString(
						new com.energyict.genericprotocolimpl.common.tou.OctetString("11:12:13:14:").getOctets()));
		*/
		
		/*byte[] bytes = new byte[2];
		bytes[0]= (byte) (65535  >> 8);
		bytes[1] = (byte) 65535 ;
		System.out.println(ProtocolUtils.outputHexString(bytes));*/

	}
	
}
