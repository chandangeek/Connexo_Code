package com.energyict.edf.messages.usecases;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;

import com.energyict.edf.messages.MessageContent;
import com.energyict.edf.messages.MessageWriteRegister;
import com.energyict.edf.messages.objects.ActivityCalendar;
import com.energyict.edf.messages.objects.SeasonProfile;
import com.energyict.edf.messages.objects.WeekProfile;
import com.energyict.edf.messages.objects.DayProfile;
import com.energyict.edf.messages.objects.CosemCalendar;
import com.energyict.edf.messages.objects.DayProfileSegment;
import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;

public class SetDayNightProfileCalendar extends AbstractFolderAction {
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Rtu rtu = (Rtu)i.next();
                
                    
                ActivityCalendar ac = new ActivityCalendar( "0","0" );
                
                Calendar c = Calendar.getInstance( );
                c.set(2008, 01, 01);
                
                SeasonProfile seasonProfile = new SeasonProfile( "1", c, false, "1" );
                ac.addPassiveSeasonProfiles(seasonProfile);
                
                WeekProfile weekProfile = new WeekProfile( (byte)0x01 );
                weekProfile.setMonday(1);
                weekProfile.setTuesday(1);
                weekProfile.setWednesday(1);
                weekProfile.setThursday(1);
                weekProfile.setFriday(1);
                weekProfile.setSaturday(1);
                weekProfile.setSunday(1);
                ac.addPassiveWeekProfiles( weekProfile );
                    
                DayProfile dayProfile = new DayProfile( 0x01 );
                ac.addPassiveDayProfiles(dayProfile);
                dayProfile.addSegment( new DayProfileSegment( "0:0:0:0", "1:2:3:4:5:6", 0x2) );
                dayProfile.addSegment( new DayProfileSegment( "0:6:0:0", "1:2:3:4:5:6", 0x3 ) );
                dayProfile.addSegment( new DayProfileSegment( "2:2:0:0", "1:2:3:4:5:6", 0x2 ) );
    
                Calendar calendar = Calendar.getInstance();
                boolean dst = calendar.get( Calendar.DST_OFFSET ) > 0;
                CosemCalendar cosemCalendar = new CosemCalendar( calendar, dst );
                
                ac.setActivatePassiveCalendarTime( cosemCalendar );
                
                MessageContent mr = new MessageWriteRegister( "0.0.13.0.0.255", ac );
                mr.setOrdinal(0);
                
                createMessage(rtu, mr);
                
                
            }
            
        } catch( Exception ex ){
            
            
            ex.printStackTrace();
            throw new BusinessException( ex );
            
        }
        

        
    }
    
    public String getVersion() {
        return " $ Revision: 1 $ ";
    }
    
}
