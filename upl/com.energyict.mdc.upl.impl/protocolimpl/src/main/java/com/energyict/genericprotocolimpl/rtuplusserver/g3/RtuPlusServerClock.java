package com.energyict.genericprotocolimpl.rtuplusserver.g3;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Clock;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 7/03/12
 * Time: 18:25
 */
public class RtuPlusServerClock extends AbstractDlmsSessionTask {

    public static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");
    private long timeDifference = 0;

    public RtuPlusServerClock(DlmsSession session, RtuPlusServerTask task) {
        super(session, task);
    }

    public final void validateClock() throws IOException, BusinessException {
        readClock();
        if (getCommunicationProfile().getAdHoc() && getCommunicationProfile().getForceClock()) {
            getLogger().severe("Ad hoc 'Force write clock' option is enabled for [" + getCommunicationProfile().displayString() + "]!");
            writeClock();
        } else if (getCommunicationProfile().getWriteClock()) {
            final int minDiff = getCommunicationProfile().getMinimumClockDifference();
            final int maxDiff = getCommunicationProfile().getMaximumClockDifference();
            final int diff = (int) (getTimeDifference() / 1000);
            if (diff < minDiff) {
                getLogger().info("Time difference [" + diff + " s] is less than configured minimum clock difference [" + minDiff + " s]. Skipping clock sync.");
            } else if (diff > maxDiff) {
                if (getCommunicationProfile().getCollectOutsideBoundary()) {
                    getLogger().severe("Time difference [" + diff + " s] exceeds the configured maximum clock difference [" + maxDiff + " s]! Skipping clock sync.");
                } else {
                    throw new BusinessException("Time difference [" + diff + " s] exceeds the configured maximum clock difference [" + maxDiff + " s] and 'collect outside boundary' disabled.");
                }
            } else {
                getLogger().warning("Time difference [" + diff + " s] exceeds the configured minimum clock difference [" + maxDiff + " s]! Writing clock.");
                writeClock();
            }
        }
    }

    private final Date readClock() throws IOException {
        final Date meterTime = getCosemObjectFactory().getClock(CLOCK_OBIS_CODE).getDateTime();
        final Date systemTime = new Date();
        this.timeDifference = Math.abs(systemTime.getTime() - meterTime.getTime());
        getLogger().info("Meter clock [" + format(meterTime) + "] and system time [" + format(systemTime) + "] have a time difference of [" + timeDifference + " ms].");
        return meterTime;
    }

    public final long getTimeDifference() {
        return this.timeDifference;
    }

    private final void writeClock() throws IOException {
        try {
            final Date currentTime = new Date();
            getLogger().severe("Setting clock to system time [" + format(currentTime) + "]!");
            final AXDRDateTime dateTime = new AXDRDateTime(currentTime);
            final Clock clock = getCosemObjectFactory().getClock(CLOCK_OBIS_CODE);
            clock.setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new NestedIOException(e, "Could not write the clock!");
        }
    }

}
