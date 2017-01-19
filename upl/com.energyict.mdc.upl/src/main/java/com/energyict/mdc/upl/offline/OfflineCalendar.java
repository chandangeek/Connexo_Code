package com.energyict.mdc.upl.offline;

/**
 * Represents the offline version of a Calendar.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-25 (16:27)
 */
public interface OfflineCalendar extends Offline {

    long getId();

    String getMRID();

    String getName();

    boolean isGhost();

}