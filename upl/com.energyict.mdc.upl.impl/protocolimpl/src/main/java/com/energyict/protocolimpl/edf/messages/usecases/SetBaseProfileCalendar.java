package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocolimpl.edf.messages.MessageContent;
import com.energyict.protocolimpl.edf.messages.MessageWriteRegister;
import com.energyict.protocolimpl.edf.messages.objects.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;

public class SetBaseProfileCalendar extends AbstractFolderAction {
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Rtu rtu = (Rtu)i.next();
                
                ActivityCalendar ac = new ActivityCalendar( "0","0" );
                
                Calendar c = Calendar.getInstance( );
                c.set(2008, 01, 01);
                
                SeasonProfile seasonProfile = new SeasonProfile( "0", c, false, "0" );
                ac.addPassiveSeasonProfiles(seasonProfile);
                
                WeekProfile weekProfile = new WeekProfile( (byte)0x00 );
                weekProfile.setMonday(0);
                weekProfile.setTuesday(0);
                weekProfile.setWednesday(0);
                weekProfile.setThursday(0);
                weekProfile.setFriday(0);
                weekProfile.setSaturday(0);
                weekProfile.setSunday(0);
                ac.addPassiveWeekProfiles( weekProfile );
                    
                DayProfile dayProfile = new DayProfile( 0x00 );
                ac.addPassiveDayProfiles(dayProfile);
                dayProfile.addSegment( new DayProfileSegment( "0:0:0:0", "0:0:0:0:0:0", 1 ) );
                  
                
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
