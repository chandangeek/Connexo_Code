package com.energyict.mdc.protocol.api.device;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Protectable;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Register represents a single register in a rtu
 */
public interface Register extends IdBusinessObject, Protectable, CanGoOffline<OfflineRegister> {

    /**
     * Returns the register's rtu id
     *
     * @return the rtu id
     */
    int getDeviceId();

    /**
     * Returns the register's rtu
     *
     * @return the rtu
     */
    Device getDevice();

    public ObisCode getDeviceObisCode ();

    /**
     * Returns the receiver's path
     *
     * @return the path or null
     */
    public String getPath();

    /**
     * Returns the register multiplier
     *
     * @return the multiplier
     */
    BigDecimal getMultiplier();

    /**
     * Returns the register multiplier valid on the passed date
     *
     * @return the multiplier
     */
    BigDecimal getMultiplier(Date date);

    /**
     * Tests if this register is cumulative
     *
     * @return true if the register is cumulative, false otherwise
     */
    boolean isCumulative();

    /**
     * Returns the register's wrap around value
     *
     * @return the wrap around value
     */
    BigDecimal getModulo();

    /**
     * Returns the first reading whose to date is after or equal to the argument
     *
     * @param date test date
     * @return the reading or null
     */
    RegisterReading getReadingAfterOrEqual(Date date);

    /**
     * Returns the last reading whose to date is before or equal to the argument
     *
     * @param date test date
     * @return the reading or null
     */
    RegisterReading getReadingBeforeOrEqual(Date date);

    /**
     * Returns a <code>List</code> of <code>RtuRegisterReadings</code> for the
     * given period based on the totime
     *
     * @param period period you want the readings from
     * @return List of <code>RtuRegisterReadings</code>
     */
    List<RegisterReading> getReadings(Interval period);

    /**
     * Returns a <code>List</code> of <code>RtuRegisterReadings</code> for the
     * given period based on the readtime
     *
     * @param period period you want the readings from
     * @return List of <code>RtuRegisterReadings</code>
     */
    List<RegisterReading> getReadingsByReadTime(Interval period);

    /**
     * Returns a <code>List</code> of <code>RtuRegisterReadings</code> for the
     * register
     *
     * @return List of <code>RtuRegisterReadings</code>
     */
    List<RegisterReading> getReadings();

    /**
     * Returns a <code>List</code> of <code>RtuRegisterReadings</code> for the
     * register
     *
     * @param includeRevoked indicates whether revoked readings should be
     *                       returned also
     * @return List of <code>RtuRegisterReadings</code>
     */
    List<RegisterReading> getReadings(boolean includeRevoked);

    /**
     * Return the <code>RtuRegisterReadings</code> for the given read time
     *
     * @param readTime time the register was read
     * @return the <code>RtuRegisterReadings</code>
     */
    RegisterReading getReadingAt(Date readTime);

    /**
     * Return the last reading for this register
     *
     * @return the <code>RegisterReading</code>
     */
    RegisterReading getLastReading();

    /**
     * Return the &quot;toTime&quot of the last reading for this register
     *
     * @return the <code>RegisterReading</code>
     * @see RegisterReading
     */
    Date getLastReadingDate();

    /**
     * stores the reading represented by the argument
     *
     * @param registerValue the bew reading
     * @return true if stored, false otherwise
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     * @deprecated Use store(RegisterReadingShadow) instead
     */
    @Deprecated
    boolean store(RegisterValue registerValue) throws SQLException, BusinessException;

    /**
     * stores the reading represented by the argument
     *
     * @param registerValue         the bew reading
     * @param updateValidationDates true if validation dates (last checked and last reading) need to be updated
     * @return true if stored, false otherwise
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     * @deprecated Use store(RegisterReadingShadow, boolean) instead
     */
    @Deprecated
    boolean store(RegisterValue registerValue, boolean updateValidationDates) throws SQLException, BusinessException;

    /**
     * Checks if the receiver is the prime register for the given channel
     *
     * @param channel channel for which you want to check
     * @return true if the receiver is the channel's prime register, false if
     *         not
     */
    boolean isPrimeRegister(Channel channel);

    /**
     * Checks if the receiver is the &quot;Time Of Use&quot; register for the
     * given channel
     *
     * @param channel channel for which you want to check
     * @return true if the receiver is the channel's &quot;Time Of Use&quot;
     *         register, false if not
     */
    boolean isTouRegister(Channel channel); // Time Of Use register

    boolean isLinkedTo (Channel channel);

    /**
     * Returns the <code>Unit</code> of the receiver .
     *
     * @return the {@link Unit} of this register
     * @see Unit
     */
    Unit getUnit();

    /**
     * Returns the receiver's overruled group id or zero if the default group
     * defined on the register mapping is not overruled.
     *
     * @return the group id or zero
     */
    int getRtuRegisterGroupId();

    public ObisCode getRegisterMappingObisCode ();

    public ObisCode getRegisterSpecObisCode();

    /**
     * Returns the store channel. This is the channel used to expand the
     * register readings based on the reference profile.
     *
     * @return the channel or null.
     */
    Channel getStoreChannel();

    /**
     * Returns the id of the store channel
     *
     * @return the store channel id or 0
     */
    int getStoreChannelId();

    /**
     * Returns if consumptions should be store normalized
     *
     * @return if consumptions should be store normalized
     */
    boolean getNormalize();

    /**
     * Returns the id of the (hidden) store parameter
     *
     * @return the (hidden) store paramter's id or 0
     */
    int getStoreParameterId();

    /**
     * Returns true if the register is active on the given date.
     * A register is active when :
     * <ul>
     * <li>it has readings that are not marked as as stop reading before or at the given date</li>
     * <li>the nearest start or stop reading before or at the given date is marked as a start reading</li>
     * <li> the nearest start or stop reading before or at the given date is marked as a start/stop reading</li>
     * </ul>
     * A register is inactive when:
     * <ul>
     * <li>the nearest start or stop reading before or at the given date is marked as a stop reading </li>
     * <li>it has no readings before or at the given date</li>
     * </ul>
     *
     * @param date to check
     * @return the boolean result
     */
    boolean isActive(Date date);

    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification date
     */
    Date getModDate();

    /**
     * Returns the last X readings for the register
     *
     * @param numberOfReadings to return
     * @return List of RtuRegisterReadings (can be empty)
     *         throws SQLException if if a database error occured.
     */
    public List<RegisterReading> getLastXReadings(int numberOfReadings);

    Date getCreationDate();

    MultiplierMode getMultiplierMode();

    BigDecimal getOverflowValue();

    int getNumberOfDigits();

    int getNumberOfFractionDigits();

    boolean isMultiplierOverruled();

    boolean isMultiplierModeOverruled();

    boolean isNumberOfDigitsOverruled();

    boolean isNumberOfFractionDigitsOverruled();

    boolean isOverflowOverruled();

}