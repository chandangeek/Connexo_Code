package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.common.Offline;

import aQute.bnd.annotation.ProviderType;

/**
 * Represents the offline version of a {@link com.elster.jupiter.calendar.Calendar}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-25 (16:27)
 */
@ProviderType
public interface OfflineCalendar extends Offline {

    long getId();

    String getMRID();

    String getName();

    boolean isGhost();

}