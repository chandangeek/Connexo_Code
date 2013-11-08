package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Folder;
import com.energyict.protocolimpl.edf.messages.*;
import com.energyict.protocolimpl.edf.messages.objects.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;

public class Service08 extends AbstractFolderAction {
    
    public void execute(Folder folder) 
        throws SQLException, BusinessException {
        
        
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Device rtu = (Device)i.next();
        
                /* 2 - 8 -> read indexes */
                MessageContent mr = new MessageReadIndexes( );
                mr.setOrdinal(0);
                createMessage( rtu, mr);
         
                /* 9 -> read profile */
                mr = new MessageReadBillingValues( );
                mr.setOrdinal(1);
                createMessage( rtu, mr);
    
                /* 9 -> read profile */
                mr = new MessageReadLoadProfiles( );
                mr.setOrdinal(1);
                createMessage( rtu, mr);

                /* 10 -> set time  (not the sync action, but the set) */
                mr = new MessageWriteRegister( "0.0.1.0.0.255", new Integer( 0 ) );
                mr.setOrdinal(2);
                createMessage( rtu, mr);
                
                /* 11 -> set activity calendar */
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
                dayProfile.addSegment( new DayProfileSegment( "0:0:0:0", "1:2:3:4:5:6", 1 ) );
                  
                Calendar calendar = Calendar.getInstance();
                boolean dst = calendar.get( Calendar.DST_OFFSET ) > 0;
                CosemCalendar cosemCalendar = new CosemCalendar( calendar, dst );
                
                ac.setActivatePassiveCalendarTime( cosemCalendar );

                mr = new MessageWriteRegister( "0.0.13.0.0.255", ac );
                mr.setOrdinal(3);
         
                /* 12 - 13 -> set moving peak */
                Integer m = new Integer( 1 );
                MessageExecuteAction a = 
                    new MessageExecuteAction( "0.0.10.0.125.255", 1, m );
                a.setOrdinal(3);
                createMessage(rtu, a);
                
                /* 14-> set demand management 
                 * for LG the second value must be one of: 
                 * 3 000, 6 000, 9 000, 12 000, 15 000, 18 000
                 */
                DemandManagement dm = new DemandManagement( 6000, 6000 );
                mr = new MessageWriteRegister( "0.0.16.0.1.255", dm);
                mr.setOrdinal(4);
                createMessage(rtu, mr);
                
                /* 15 -> set TIC config */
                mr = new MessageWriteRegister( "0.0.96.3.2.255", new Integer( 0 ));
                mr.setOrdinal(5);
                createMessage( rtu, mr);
                
                /* 16 -> set load profile */
                mr = new MessageWriteRegister( "1.0.99.1.0.255", new Integer( 900 ));
                mr.setOrdinal(6);
                createMessage( rtu, mr);
                
                
                /* 17 -> Read Summa */
                addMessage(rtu, "1.0.1.8.0.255",       7);
                
                /* 18 -> Read tarif 1 */
                addMessage(rtu, "1.0.1.8.1.255",       8);
                
                
                /* 19 -> Read tarfi 2 */
                addMessage(rtu, "1.0.1.8.2.255",       9);
                
                
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
