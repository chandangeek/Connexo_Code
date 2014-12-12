package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/05/11
 * Time: 13:42
 */
public class InstallationDateDiscover {

    public static final Date FIRST_POSSIBLE_DATE = ProtocolTools.getDateFromYYYYMMddhhmmss("2010-01-01 00:00:00");
    private final List<MeterEvent> meterEvents;

    public InstallationDateDiscover(List<MeterEvent> meterEvents) {
        this.meterEvents = meterEvents;
        Collections.sort(meterEvents);
    }

    public Date getInstallationDateFromEvents() {
        for (MeterEvent meterEvent : meterEvents) {
            if (meterEvent.getProtocolCode() == 0x38) {
                Date installationDate = getDateFromSetClockEvent(meterEvent.getMessage());
                if ((installationDate != null) && (installationDate.after(FIRST_POSSIBLE_DATE))) {
                    return installationDate;
                }
            }
        }
        return null;
    }

    private Date getDateFromSetClockEvent(String message) {
        String[] split = message.split("'");
        if ((split != null) && (split.length >= 2)) {
            try {
                return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(split[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
