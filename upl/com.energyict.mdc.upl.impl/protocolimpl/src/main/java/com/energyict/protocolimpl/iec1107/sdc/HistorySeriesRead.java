/*
 * HistorySeriesRead.java
 *
 * Created on 28 oktober 2004, 14:29
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class HistorySeriesRead extends AbstractDataReadingCommand {
    
    private static final int DEBUG=1;
    
    
    LoadProfileDataBlock loadProfileDataBlock=null;
    int nrOfIntervals=0;
    
    /** Creates a new instance of HistorySeriesRead */
    public HistorySeriesRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }
    
    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        Date firstStartingDate = null;
        Date stepDate = null; Calendar stepCalendar = Calendar.getInstance(timeZone);
        Date tempDate = null; Calendar tempCalendar = Calendar.getInstance(timeZone);
        List loadProfileEntries=new ArrayList();
        int profileInterval=-1;
        DataParser dp = new DataParser(timeZone);
        String strExpression = new String(data);
        if (DEBUG >=1)
            System.out.println(strExpression);        
        
        try {
        	int stringCounter = 0;
        	String dataString = strExpression.substring(1, strExpression.length() - 1);
            do{
            	
            	if ( dataString.compareTo("") != 0){
                	String str = dataString.substring(dataString.indexOf('(',stringCounter) + 1, dataString.indexOf(')',stringCounter + 1));
                	stringCounter = dataString.indexOf(')',stringCounter + 1);
                    if (stringContains(str)) { //  check for a date
                    	if (firstStartingDate == null){
    	                	profileInterval = Integer.parseInt(str.substring(0,2)) * 60;                	
    	                	firstStartingDate = dp.parseDateTime(str.substring(2, 22)); 
    	                	stepDate = firstStartingDate;
    	                	stepCalendar.setTime(stepDate);
                    	
    	                    if (profileInterval != getDataReadingCommandFactory().getSdc().getProfileInterval())
    	                         throw new IOException("HistorySeriesRead, parse, DataParseException, error different profileInterval bewteen meter ("+profileInterval+") and configured ("+getDataReadingCommandFactory().getSdc().getProfileInterval()+")");
                    	}
                    	
                    	
                    	else{
                    		tempDate = dp.parseDateTime(str.substring(2,22));
                    		tempCalendar.setTime(tempDate);
                    		
                    		while(stepCalendar.getTime().before(tempCalendar.getTime())){
                        			stepCalendar.add(Calendar.SECOND, profileInterval);
                        			if( stepCalendar.getTime().before(tempCalendar.getTime()) )
                        				loadProfileEntries.add(new LoadProfileEntry());
                        		}
                    	}
                    }
                    else {
                        int status;
                        if (str.compareTo("") == 0) {
                            loadProfileEntries.add(new LoadProfileEntry());
                        }
                        else {
                            status=0;
                            String str2 = str.split("\\*")[0];
                            BigDecimal bd=null;
                            if (str2.indexOf(',') != -1) {
                                status = Integer.parseInt(str2.split(",")[1]);
                                bd = new BigDecimal(str2.split(",")[0]);
                            }
                            else
                                bd = new BigDecimal(str2);
                            
//                            Unit unit = Unit.get(str.split("\\*")[1]);
                            Unit unit = Unit.get(getUnitCode(str.split("\\*")[1]));
                            loadProfileEntries.add(new LoadProfileEntry(new Quantity(bd,unit),status));
                            stepCalendar.add(Calendar.SECOND, profileInterval);
                        }
                        
                    }
            	}
            	else
            		stringCounter = 3;

            }while(stringCounter < dataString.length() - 3); // for (pos=0;pos<nrOfIntervals;pos++)
        }
        catch(DataParseException e) {
            throw new IOException("HistorySeriesRead, parse, DataParseException, probably wrong profileInterval, meter "+profileInterval+", configured "+getDataReadingCommandFactory().getSdc().getProfileInterval());
        }
        loadProfileDataBlock = new LoadProfileDataBlock(firstStartingDate,loadProfileEntries,profileInterval);
    }
    
    /**
     * Getter for property loadProfile.
     * @return Value of property loadProfile.
     */
    public LoadProfileDataBlock getLoadProfileDataBlock(Date from, int channel) throws IOException {
        int profileInterval = getDataReadingCommandFactory().getSdc().getProfileInterval();
        //Date to = new Date();
        //int period = (int)(to.getTime() - from.getTime());
        //int nrOfIntervalsToRetrieve = (period / 1000) / profileInterval;
        int nrOfIntervalsPerDay = (24*3600) / profileInterval;
        return getLoadProfileDataBlock(from, nrOfIntervalsPerDay, channel);
    }
    
    public LoadProfileDataBlock getLoadProfileDataBlock(Date from,int nrOfIntervals, int channel) throws IOException {
        this.nrOfIntervals=nrOfIntervals + 3;
//        Calendar cal = ProtocolUtils.getCleanCalendar(TimeZoneManager.getTimeZone("GMT"));
        Calendar cal = ProtocolUtils.getCleanCalendar(getDataReadingCommandFactory().getSdc().getTimeZone());
        cal.setTime(from);
        
//        retrieve("HRR","0");
        
//****************************************************************************
//        this is the correct one!
        retrieve("PRR",(channel)+","+
        		cal.get(Calendar.YEAR)+","+
        		(cal.get(Calendar.MONTH)+1)+","+
        		cal.get(Calendar.DAY_OF_MONTH));
//****************************************************************************
        
//        retrieve("HRR","0");
        
//        retrieve("HSZ","0"+","+cal.get(Calendar.YEAR)+
//        		","+cal.get(Calendar.MONTH)+
//        		","+cal.get(Calendar.DAY_OF_MONTH));
////        		","+cal.get(Calendar.HOUR)+
////        		","+cal.get(Calendar.MINUTE)+
////        		","+cal.get(Calendar.SECOND));
        
        /*HSZ(<series>, <number>, <year>, <month>, <day>, <hour>, <min>, <sec>)*/
        
//        retrieve("HSR",channel+","+
        
//        retrieve("HSZ",channel+","+
//                nrOfIntervals+","+
//                cal.get(Calendar.YEAR)+","+
//                (cal.get(Calendar.MONTH)+1)+","+
//                cal.get(Calendar.DAY_OF_MONTH)+","+
//                cal.get(Calendar.HOUR_OF_DAY)+","+
//                cal.get(Calendar.MINUTE)+","+
//                cal.get(Calendar.SECOND));
        
//        retrieve("HSR",channel+","+
//        nrOfIntervals+","+
//        cal.get(Calendar.YEAR)+","+
//        (cal.get(Calendar.MONTH)+1)+","+
//        cal.get(Calendar.DAY_OF_MONTH));//+","+
//        cal.get(Calendar.DAY_OF_WEEK)+","+		//history : 04/12/07
//        cal.get(Calendar.HOUR_OF_DAY)+","+
//        cal.get(Calendar.MINUTE)+","+
//        cal.get(Calendar.SECOND)+","+
//        cal.get(Calendar.AM_PM));				//history : 04/12/07
        
//        retrieve ("MVR",Integer.toString(1));
        
//        retrieve (new String("30C0"),new String("10"));
//        retrieve (new String("30D0"),new String("10"));
//        retrieve ("HSR",new String("1"));
        
        return getLoadProfileDataBlock();
    }
    
    public String toString() {
        return getLoadProfileDataBlock().toString();
    }
    
    /**
     * Getter for property loadProfileDataBlock.
     * @return Value of property loadProfileDataBlock.
     */
    private LoadProfileDataBlock getLoadProfileDataBlock() {
        return loadProfileDataBlock;
    }
    
    private int getUnitCode(String stringCode){
    	if ( stringCode.compareTo("WH") == 0 ){
    		return 30; // WattHour
    	}
    	else return 0;
    }
    
    private boolean stringContains(String string){
        return string.indexOf(",") > 0;
    }
    
    
}
