package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

import java.sql.SQLException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 15/11/11
 * Time: 9:05
 */
public class CtrSmsStoreObject extends StoreObject {

    private final int channelBackLog;

    public CtrSmsStoreObject(int channelBackLog) {
        this.channelBackLog = channelBackLog;
    }

    public int getChannelBackLog() {
        return channelBackLog;
    }

    @Override
    protected void store(Channel channel, ProfileData profileData) throws BusinessException, SQLException {
        // Store the original last reading date in case we have to restore it ...
        Date realLastReading = channel.getLastReading();
        super.store(channel, profileData);

        Date lastReadingAfterUpdate = channel.getLastReading();
        Date firstIntervalTime = getFirstIntervalTime(profileData);

        if ((firstIntervalTime != null) && (realLastReading != null) && (lastReadingAfterUpdate != null)) {
            int interval = channel.getIntervalInSeconds() * 1000;
            Date expectedLastReading = new Date(firstIntervalTime.getTime() - interval);

            // Check if we have a gap in the profile last reading dates. If there is, fix it in an appropriate way.
            if (realLastReading.before(expectedLastReading)) {
                Date channelBackLogDate = getChannelBackLogDate();

                // Restore the old last reading date, but take the channelBackLog property in account
                if (channelBackLogDate.after(realLastReading)) {
                    // Increase the last reading to the channelBackLog date. We are only supposed to read 'channelBackLog' number of days back
                    ChannelShadow shadow = channel.getShadow();
                    shadow.setLastReading(channelBackLogDate);
                    channel.update(shadow);
                } else {
                    // Restore the original last reading date.
                    ChannelShadow shadow = channel.getShadow();
                    shadow.setLastReading(realLastReading);
                    channel.update(shadow);
                }
            }
        }


    }

    private Date getChannelBackLogDate() {
        Calendar channelBackLogDate = Calendar.getInstance();
        channelBackLogDate.add(Calendar.DAY_OF_MONTH, -channelBackLog);
        channelBackLogDate.set(Calendar.MILLISECOND, 0);
        channelBackLogDate.set(Calendar.SECOND, 0);
        channelBackLogDate.set(Calendar.MINUTE, 0);
        channelBackLogDate.set(Calendar.HOUR_OF_DAY, 0);
        return channelBackLogDate.getTime();
    }

    private Date getFirstIntervalTime(ProfileData profileData) {
        profileData.sort();
        List<IntervalData> intervalDatas = profileData.getIntervalDatas();
        if ((intervalDatas == null) || intervalDatas.isEmpty()) {
            return null;
        } else {
            return intervalDatas.get(0).getEndTime();
        }
    }

}
