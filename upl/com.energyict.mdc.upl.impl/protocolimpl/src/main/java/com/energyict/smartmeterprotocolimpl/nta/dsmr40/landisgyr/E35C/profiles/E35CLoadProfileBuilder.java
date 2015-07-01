package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.profiles;

import com.energyict.cbo.Utils;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author sva
 * @since 23/06/2015 - 11:43
 */
public class E35CLoadProfileBuilder extends LGLoadProfileBuilder {

    public E35CLoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    @Override
    protected void enableDsmr40SelectiveAccessFormat(ProfileGeneric profile) {
        // Not needed for the E35C module
    }

    @Override
    protected DLMSProfileIntervals getDLMSProfileIntervals(ProfileGeneric profile, LoadProfileReader lpr, Calendar fromCalendar, Calendar toCalendar) throws IOException {
        return new E35CProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), LGDLMSProfileIntervals.DefaultClockMask,
                getStatusMasksMap().get(lpr), this.channelMaskMap.get(lpr), getProfileIntervalStatusBits());
    }

    /**
     * Construct a valid calendar for the given date<br/>
     * For the Landis&Gyr E35C module the dates should be transmitted in standard TimeZone (without DST)
     */
    @Override
    protected Calendar getCalendar(Date date) {
        // Profile data is always transmitted in standard timezone (~ winter timezone without DST)!
        Calendar calendar = Calendar.getInstance(Utils.getStandardTimeZone(getMeterProtocol().getTimeZone()));
        calendar.setTime(date);
        return calendar;
    }

    @Override
    public ProfileIntervalStatusBits getProfileIntervalStatusBits() {
        return new E35CLoadProfileIntervalStatusBits();
    }
}