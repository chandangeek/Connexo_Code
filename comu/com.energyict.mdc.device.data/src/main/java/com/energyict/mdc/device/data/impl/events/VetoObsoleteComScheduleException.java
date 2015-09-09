package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a {@link ComSchedule}
 * is being deleted while it is still being used by one or more
 * {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-03 (14:26)
 */
public class VetoObsoleteComScheduleException extends LocalizedException {

    protected VetoObsoleteComScheduleException(Thesaurus thesaurus, ComSchedule comSchedule) {
        super(thesaurus, MessageSeeds.VETO_COM_SCHEDULE_DELETION, comSchedule.getName());
        this.set("comScheduleName", comSchedule.getName());
        this.set("comScheduleId", comSchedule.getId());
    }

}