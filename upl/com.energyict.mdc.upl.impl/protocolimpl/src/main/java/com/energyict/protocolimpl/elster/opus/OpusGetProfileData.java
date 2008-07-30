package com.energyict.protocolimpl.elster.opus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.iskrap2lpc.ProtocolChannelMap;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ParseUtils;

public class OpusGetProfileData {
	// attributes
	private OpusCommandFactory ocf; 	// command factory
	private int profileInterval;
	
	
	public ProfileData getProfileData(
			Date fromTime, 
			Date toTime,
			boolean event, 
			ProtocolChannelMap channelMap, 
			OpusCommandFactory ocf,
			int numChan,
			int profileInterval, 
			int attempts, 
			int timeOut) throws IOException, UnsupportedException {
		this.ocf=ocf;
		this.profileInterval=profileInterval;
		// TODO use scanning instead of pointing because pointing can be corrupted 
		// put matrix received from the meter into a 2D matrix
		ArrayList<String[]> data= new ArrayList<String[]>(); // original data list (holds raw data strings)
		int[][] datamatrix; // holds raw VALUES, no header info (channels are columns, data sits in the rows)
		// instantiate the profile and interval data, 
		ProfileData pd = new ProfileData();		
		IntervalData id= new IntervalData();		// current interval data
		IntervalData previd= new IntervalData();	// previous interval data, is kept for power down and power up flags (are to be set before the occurence of a power down and after a power up)
		MeterEvent mev; 							// meter event flagging
		// build calendar objects
		Calendar cal1 = Calendar.getInstance();  	// removed getTimeZone()
		Calendar tempcal=Calendar.getInstance(); 	// to store data in for the interval data
		// command sequences
		int command=10, ident=0;// 10 is the value of today...69 of 60 days ago, this is theory (in reality every time a new date is set that does not correspond with the actual date in the meter a new register is opened)
		long millis=0,temp=0;
		// flagging
		boolean eventflag=false,powDownFlag=false,firstchan=true; // firstchan is not needed since the first channel is always read (might change in the future, this is done by the opus software, but increases transmission time)
		// END instantiations
		
		// build profile data object
		for(int i=0; i<numChan;i++ ){
			if(channelMap.isProtocolChannelEnabled(i)){	
				pd.addChannel(new ChannelInfo(ident,ident, "Elster Opus channel "+(i+1), Unit.get(BaseUnit.UNITLESS)));
				ident++;
				// more logical way of working:
				// does not work on EIserver
				// pd.addChannel(new ChannelInfo(ident++,i, "Elster Opus channel "+(i+1), Unit.get(BaseUnit.UNITLESS)));				
			}
		}
        // set timers
		// set to start of day
		cal1.setTime(fromTime);
		temp=cal1.getTimeInMillis()-getProfileInterval()*1500;// go back 1.5 interval
		cal1.setTimeInMillis(temp); //PD/PU midnight problem solution
		cal1.set(Calendar.HOUR_OF_DAY, 0);	// reset hour
		cal1.set(Calendar.MINUTE, 0);		// reset minutes
		cal1.set(Calendar.SECOND, 0);		// reset seconds
		cal1.set(Calendar.MILLISECOND, 0);
		
		// check properties
        if (getProfileInterval()<=0)
            throw new IOException("load profile interval must be > 0 sec. (is "+getProfileInterval()+")");
        ParseUtils.roundDown2nearestInterval(cal1,getProfileInterval());
        // start downloading
        // setTime(); // synchronize
        // id set
        while(cal1.getTime().before(toTime)) {
        	command=getCommandnr(cal1.getTime()); // download the specified day        	
        	// get the data
        	data=ocf.command(command, attempts, timeOut, cal1);
        	// put the data in a 2D matrix
        	if(data.size()>0){// data available test
        		// dump data in 2D matrix
        		datamatrix=processIntervalData(data,numChan);
        		// make the calendar object for that date
        		tempcal.setTime(cal1.getTime()); 		// set date
        		tempcal.set(Calendar.HOUR_OF_DAY, 0);	// reset hour
        		tempcal.set(Calendar.MINUTE, 0);		// reset minutes
        		tempcal.set(Calendar.SECOND, 0);		// reset seconds
        		tempcal.set(Calendar.MILLISECOND, 0);	// reset milliseconds
        		millis=tempcal.getTimeInMillis(); 		// start at 0:0:0h (interval 47 of previous day)
        	
        		for(int i=0; i<(3600*24/getProfileInterval()); i++){// 0->47
        			// save previous data
        			previd=new IntervalData(tempcal.getTime());
        			previd.setIntervalValues(id.getIntervalValues());
        			previd.setEiStatus(id.getEiStatus());        		
        			// 	generate step clock
        			millis+=(getProfileInterval()*1000); 	// now time correction        		
        			tempcal.setTimeInMillis(millis);		// set to now
        			id=new IntervalData(tempcal.getTime());
        			firstchan=true;
        			for(int ii=0;ii<numChan;ii++){// 0->12
        				if(channelMap.isProtocolChannelEnabled(ii) && ii!=0){ // process first channel for flagging (no idea if this is hardware req.
        				// check value
        				if(datamatrix[i][ii]>999990){
        					// value should be zero, because is false
        					id.addValue(0);
        					// event flagging
        					eventflag=true;    					
        					switch(datamatrix[i][ii]){ // special values
           						case 999996:
           							id.addEiStatus(IntervalStateBits.CORRUPTED);            					
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Data Overflow");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
           						case 999997:
           							id.addEiStatus(IntervalStateBits.CORRUPTED);
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Fuse Failure Delay");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
           						case 999998:
           							id.addEiStatus(IntervalStateBits.CORRUPTED);
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Lost Pulse");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
           						case 999999:
           							id.addEiStatus(IntervalStateBits.MISSING);
           							if(!powDownFlag){
           								long pdtemp=millis-getProfileInterval()*1000;
           								Calendar cal=Calendar.getInstance();
           								cal.setTimeInMillis(pdtemp); // set time one interval back
           								mev=new MeterEvent(cal.getTime(), MeterEvent.POWERDOWN);
           								if(tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           								powDownFlag=true;                					
           							}
           							break;
           						default:
           							// code 0 to 5 not implemented? No information
           							id.addEiStatus(IntervalStateBits.OTHER);
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Data Overflow");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
        					}
        					firstchan=false;// don't tag other channel recordings in pd
        				}else{
        					if(powDownFlag && eventflag){
        						powDownFlag=false;
        						mev=new MeterEvent(tempcal.getTime(), MeterEvent.POWERUP);
        						if(tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
        					}
        					eventflag=false;
        					// value is real value
        					if(channelMap.isProtocolChannelEnabled(ii)){ // skip first if disabled (eventflagging of first channel in case of flagged off is done)
        						id.addValue(datamatrix[i][ii]);
        					}
        				}// data & flag check
        				}// channel map
        			}// end ii for loop (channel)
        			if(tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){
   						pd.addInterval(id);        		
    				}
        		}// end i for loop (interval)        		
        	}// end data available test
        	// next day
        	temp=cal1.getTimeInMillis()+(3600*24*1000);
        	cal1.setTimeInMillis(temp);// date change        	
        }
        
        //pd.getMeterEvents().addAll(mev);
        //pd.addEvent(new MeterEvent(now,MeterEvent.APPLICATION_ALERT_START, "SDK Sample"));

		return pd;
	}

	private int getProfileInterval() {
		return this.profileInterval;
	}

	private int[][] processIntervalData(ArrayList<String[]> data,int numChan) throws UnsupportedException, IOException {
		int tel=-1;
		int channelBody=0;
		int[][] matrix=new int[(int) (3600*24/getProfileInterval())][numChan];
		String[] s;
		for(int index=1; index<data.size(); index++){
			s=data.get(index);
			if(s[0].charAt(0)=='S' && s[0].charAt(4)=='C'){
				// this is a channel header
				tel++;
				channelBody=0;
			}else{
				// channel body
				for(int i=0; i<8; i++){
					matrix[i+channelBody][tel]=Integer.parseInt(s[i]);
				}
				channelBody+=8;
			}			
		}
		return matrix;
	}

	private int getCommandnr(Date cal1) {
		// fix the bank bug here
		int command=10;
		int dateOffset=0;
		long now, then;
		Calendar calthen=Calendar.getInstance();
		calthen.setTime(cal1);
		Calendar calnow=Calendar.getInstance();
		calnow.set(Calendar.HOUR_OF_DAY, 0);
		calnow.set(Calendar.MINUTE, 0);
		calnow.set(Calendar.SECOND, 0);
		calnow.set(Calendar.MILLISECOND, 0);
		calthen.set(Calendar.HOUR_OF_DAY, 0);
		calthen.set(Calendar.MINUTE, 0);
		calthen.set(Calendar.SECOND, 0);
		calthen.set(Calendar.MILLISECOND, 0);
		now=calnow.getTimeInMillis(); 	// current date
		then=calthen.getTimeInMillis();	// requested date
		while(then<now){
			then+=3600*24*1000;
			command++;
			if(command==69){
				command=10;
				dateOffset+=59;
			}
		}
		this.ocf.setDateOffset(dateOffset); // change offset in factory
		return command;
	}

}
