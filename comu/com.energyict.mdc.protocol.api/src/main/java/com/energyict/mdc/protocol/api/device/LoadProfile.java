package com.energyict.mdc.protocol.api.device;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.interval.IntervalRecord;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * LoadProfile represents a loadprofile on a data logger or energy meter.
 * Each LoadProfile has a number of channels to store load profile data.
 */
public interface LoadProfile<C extends Channel> extends IdBusinessObject, CanGoOffline<OfflineLoadProfile> {

    /**
     * return the end time of the last interval read from the device.
     *
     * @return end time of the last interval.
     */
    public Date getLastReading();

    public ObisCode getDeviceObisCode();

    /**
     * Returns the Device for the LoadProfile object.
     *
     * @return the Device.
     */
    public BaseDevice getDevice();

    /**
     * Returns the receiver's {@link Channel}s.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects
     */
    public List<C> getChannels();

    /**
     * Returns the receiver's {@link Channel}s AND
     * the channels of all slave devices belonging to
     * LoadProfiles of the same type.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects
     */
    public List<C> getAllChannels();

    /**
     * Updates the last reading if the argument is later than
     * the current last reading.
     *
     * @param execDate the new last reading.
     * @throws SQLException      if a database exception occurred
     * @throws BusinessException if a business exception occurred
     */
    public void updateLastReadingIfLater(Date execDate) throws SQLException, BusinessException;

    /**
     * Updates the last reading.
     *
     * @param execDate the new last reading
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    public void updateLastReading(Date execDate) throws SQLException, BusinessException;

    /**
     * Indicates if this is a virtual load profile i.e. if the Device of this load profile needs a proxy for load profile AND
     * the B-field of the Obis code of the load profile type of this load profile does NOT contain a wildcard.
     *
     * @return boolean
     */
    boolean isVirtualLoadProfile();

    public List<IntervalRecord> getIntervalData(Interval period, Date flashBackDate, boolean showMissingValues);

    public TimeDuration getInterval();

    public int getLoadProfileTypeId ();

    public ObisCode getLoadProfileTypeObisCode ();

}