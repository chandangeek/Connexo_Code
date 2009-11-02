package com.energyict.genericprotocolimpl.common.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.DayProfileActions;
import com.energyict.dlms.cosem.attributeobjects.DayProfiles;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.dlms.cosem.attributeobjects.WeekProfiles;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.CodeDayType;
import com.energyict.mdw.core.CodeDayTypeDef;

public class ActivityCalendarMessage {
	
	private Code ct;
	private Array dayArray;
	private Array seasonArray;
	private Array weekArray;
	private DLMSMeterConfig meterConfig;
	
	public ActivityCalendarMessage(Code ct, DLMSMeterConfig meterConfig){
		this.ct = ct;
		this.meterConfig = meterConfig;
		this.dayArray = new Array();
		this.seasonArray = new Array();
		this.weekArray = new Array();
	}

	/**
	 * Parsing of the codeTable to season-, week- and dayProfiles
	 * @throws IOException when CodeTable is not correctly configured
	 */
	public void parse() throws IOException{
		List calendars = ct.getCalendars();
		HashMap seasonsProfile = new HashMap();
		
		Iterator itr = calendars.iterator();
		while(itr.hasNext()){ 
			CodeCalendar cc = (CodeCalendar)itr.next();
			int seasonId = cc.getSeason();
			if(seasonId != 0){
				OctetString os = new OctetString(new byte[]{(byte) ((cc.getYear()==-1)?0xff:((cc.getYear()>>8)&0xFF)), (byte) ((cc.getYear()==-1)?0xff:(cc.getYear())&0xFF), 
						(byte) ((cc.getMonth()==-1)?0xFF:cc.getMonth()), (byte) ((cc.getDay()==-1)?0xFF:cc.getDay()), (byte) 0xFF, 0, 0, 0, 0, (byte) 0x80, 0, 0});
				seasonsProfile.put(os, seasonId);
			}
		}

//		int numberOfSeasons = getNumberOfSeasons();
		
		int weekCount = 0;
		Iterator seasonsPIt = seasonsProfile.entrySet().iterator();
		while(seasonsPIt.hasNext()){
			Map.Entry entry = (Map.Entry)seasonsPIt.next();
			OctetString dateTime = (OctetString)entry.getKey();
			
			int seasonProfileNameId = (Integer)entry.getValue();
			if(!seasonArrayExists(seasonProfileNameId, seasonArray)){
				
				String weekProfileName = Integer.toString(weekCount++);
				SeasonProfiles sp = new SeasonProfiles();
				sp.setSeasonProfileName(OctetString.fromString(Integer.toString(seasonProfileNameId))); 	// the seasonProfileName is the DB id of the season
				sp.setSeasonStart(dateTime);
				sp.setWeekName(OctetString.fromString(weekProfileName));
				seasonArray.addDataType(sp);
				if(!weekArrayExists(weekProfileName, weekArray)){
					WeekProfiles wp = new WeekProfiles();
					Iterator sIt = calendars.iterator();
					CodeDayType dayTypes[] = {null, null, null, null, null, null, null};
					CodeDayType any = null;
					while(sIt.hasNext()){
						CodeCalendar codeCal = (CodeCalendar)sIt.next();
						if(codeCal.getSeason() == seasonProfileNameId){
							switch(codeCal.getDayOfWeek()){
							case 1: {
								if(dayTypes[0] != null){
									if(dayTypes[0] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{dayTypes[0] = codeCal.getDayType();}}break;
							case 2: {
								if(dayTypes[1] != null){
									if(dayTypes[1] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{dayTypes[1] = codeCal.getDayType();}}break;
							case 3: {
								if(dayTypes[2] != null){
									if(dayTypes[2] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{dayTypes[2] = codeCal.getDayType();}}break;
							case 4: {
								if(dayTypes[3] != null){
									if(dayTypes[3] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{dayTypes[3] = codeCal.getDayType();}}break;
							case 5: {
								if(dayTypes[4] != null){
									if(dayTypes[4] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{dayTypes[4] = codeCal.getDayType();}}break;
							case 6: {
								if(dayTypes[5] != null){
									if(dayTypes[5] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{dayTypes[5] = codeCal.getDayType();}}break;
							case 7: {
								if(dayTypes[6] != null){
									if(dayTypes[6] != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{dayTypes[6] = codeCal.getDayType();}}break;
							case -1: {
								if(any != null){
									if(any != codeCal.getDayType()){throw new IOException("Season profiles are not correctly configured.");}
								}else{any = codeCal.getDayType();}}break;
							default: throw new IOException("Undefined daytype code received.");
							}
						}
					}
					
					wp.setWeekProfileName(OctetString.fromString(weekProfileName));
					for(int i = 0; i < dayTypes.length; i++){
						if(dayTypes[i] != null){
							wp.addWeekDay(dayTypes[i].getId());
						} else if(any != null){
							wp.addWeekDay(any.getId());
						} else {
							throw new IOException("Not all dayId's are correctly filled in.");
						}
					}
					weekArray.addDataType(wp);
					
				}
			}
		}
		List dayProfiles = ct.getDayTypesOfCalendar();
		Iterator dayIt = dayProfiles.iterator();
		while(dayIt.hasNext()){
			CodeDayType cdt = (CodeDayType)dayIt.next();
			DayProfiles dp = new DayProfiles();
			List definitions = cdt.getDefinitions();
			Array daySchedules = new Array();
			for(int i = 0; i < definitions.size(); i++){
				DayProfileActions dpa = new DayProfileActions();
				CodeDayTypeDef cdtd = (CodeDayTypeDef)definitions.get(i);
				int tStamp = cdtd.getTstampFrom();
				int hour = tStamp/10000;
				int min = (tStamp-hour*10000)/100;
				int sec = tStamp-(hour*10000)-(min*100);
				OctetString tstampOs = new OctetString(new byte[]{(byte)hour, (byte)min, (byte)sec, 0});
				Unsigned16 selector = new Unsigned16(cdtd.getCodeValue());
				dpa.setStartTime(tstampOs);
				dpa.setScriptLogicalName(new OctetString(this.meterConfig.getTariffScriptTable().getLNArray()));
				dpa.setScriptSelector(selector);
				daySchedules.addDataType(dpa);
			}
			dp.setDayId(new Unsigned8(cdt.getId()));
			dp.setDayProfileActions(daySchedules);
			dayArray.addDataType(dp);
		}
	
		checkForSingleSeasonStartDate();
	}
	
	/**
	 * Checks if a given seasonProfile already exists
	 * @param seasonProfileNameId - the id of the 'to-check' seasonProfile
	 * @param seasonArray - the complete seasonProfile list where you need to check in
	 * @return true if it exists, false otherwise
	 */
	private boolean seasonArrayExists(int seasonProfileNameId, Array seasonArray) {
		for(int i = 0; i < seasonArray.nrOfDataTypes(); i++){
			SeasonProfiles sp = (SeasonProfiles)seasonArray.getDataType(i);
			if(sp.getSeasonId() == seasonProfileNameId){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if a given weekProfile already exists
	 * @param weekProfileName - the id of the 'to-check' weekProfile
	 * @param weekArray - the complete weekProfile list where you need to check in
	 * @return true if it exists, false otherwise
	 */
	private boolean weekArrayExists(String weekProfileName, Array weekArray) {
		for(int i = 0; i < weekArray.nrOfDataTypes(); i++){
			WeekProfiles wp = (WeekProfiles)weekArray.getDataType(i);
			if(new String(wp.getWeekProfileName().getOctetStr()).equalsIgnoreCase(weekProfileName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the current seasonArray
	 */
	public Array getSeasonProfile() {
		return this.seasonArray;
	}
	
	/**
	 * If only one season is defined, then you must at least enter 1 value for the startDate.
	 */
	private void checkForSingleSeasonStartDate(){
		if(this.seasonArray.nrOfDataTypes() == 1){
			SeasonProfiles sp = (SeasonProfiles)this.seasonArray.getDataType(0);
			byte[] startTime = sp.getSeasonStart().getOctetStr();
			startTime[3] = 1;	// set the startTime to the first of the month
			sp.setSeasonStart(new OctetString(startTime));
			//TODO check if you need to add it again.
		}
	}

	/**
	 * @return the current weekArray 
	 */
	public Array getWeekProfile() {
		return this.weekArray;
	}

	/**
	 * @return the current dayArray
	 */
	public Array getDayProfile() {
		return this.dayArray;
	}
}
