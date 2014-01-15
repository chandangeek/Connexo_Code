package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.interval.BasicConsumption;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

/**
 * Represents a register reading
 */
public interface RegisterReading<R extends Register> extends BusinessObject, RegisterReadingRecord<R> {

    /**
     * The 'valid' state code
     */
    int VALID = 0;
    /**
     * The 'modified' state code
     */
    int MODIFIED = 1;
    /**
     * The 'revoked' state code
     */
    int REVOKED = 2;
    /**
     * The 'estimated' state code
     */
    int ESTIMATED = 4;
    /**
     * The 'suspect' state code
     */
    int SUSPECT = 8;
    /**
     * The 'suspect' state code
     */
    int WRAP_AROUND = 16;
    /**
     * The 'start' indicator, readings marked by this flag indicate the begin of
     * an active metering period
     */
    int START = 32;
    /**
     * The 'stop' indicator, readings marked by this flag indicate the end of an
     * active metering period
     */
    int STOP = 64;
    /**
     * Indicates that someone overruled the consumption meaning that its value is not equal
     * to the calculated consumption based on the previous reading
     */
    int OVERRULED_CONSUMPTION = 128;

    /**
     * Indicates that there was a overflow value registered (larger than the defined max)
     * The value is the clipped one and the original overflow value is stored in the text field
     */
    int OVERFLOW = 256;

    final String OVERFLOW_TEXT = "OVERFLOW VALUE = ";

    /**
     * Returns the reading's value
     *
     * @return the register value
     */
    BigDecimal getValue();

    /**
     * Returns the time the register reading was performed
     *
     * @return the read time
     */
    Date getReadTime();

    /**
     * Returns the start of the measurement period covered by this reading. Most
     * registers are since the start of measurement. In this case null is
     * returned
     *
     * @return the from date or null
     */
    Date getFromTime();

    /**
     * Returns the end of the measurement period covered by this reading. For
     * most registers this will be equal to the read time. For billing point
     * register, this is the time of the billing point
     *
     * @return the to time
     */
    Date getToTime();

    /**
     * Returns the time the metered event took place. For most registers this
     * will be null. For maximum demand registers, this is the interval time the
     * maximum demand was registerd
     *
     * @return the event time or null
     */
    Date getEventTime();

    /**
     * Returns the consumption between this reading and the previous
     *
     * @return the consumption or null
     */
    BasicConsumption getConsumption();

    /**
     * Returns the consumption amount as a single value without period and unit
     *
     * @return the amount of the consumption
     */
    BigDecimal getConsumptionAmount();

    /**
     * Returns the reading's register
     *
     * @return the register
     */
    R getRegister();

    /**
     * Returns the id of the rtu register for this reading
     *
     * @return the id of the rtu register
     */
    int getRegisterId();

    /**
     * Returns the reading's text
     *
     * @return the text
     */
    String getText();

    /**
     * Returns the reading's state code
     *
     * @return the state code
     * @deprecated user getReadingState()
     */
    @Deprecated
    int getState();

    /**
     * Tests if the reading is valid
     *
     * @return true if valid , false otherwise
     */
    boolean isValid();

    /**
     * Tests if the reading has been modified
     *
     * @return true if modified , false otherwise
     */
    boolean isModified();

    /**
     * Tests if the reading has been revoked
     *
     * @return true if revoked , false otherwise
     */
    boolean isRevoked();

    /**
     * Tests if the reading has been estimated
     *
     * @return true if estimated , false otherwise
     */
    boolean isEstimated();

    /**
     * Tests if the reading is suspect
     *
     * @return true if suspect , false otherwise
     */
    boolean isSuspect();

    /**
     * Tests if the reading is a wrap_around value
     *
     * @return true if the value is a wrap_around value , false otherwise
     */
    boolean isWrapAround();

    /**
     * Tests if this readings is a start reading, indicating the beginning of an
     * active metering period
     */
    boolean isStart();

    /**
     * Tests if this readings is a stop reading, indicating the end of an active
     * metering period
     */
    boolean isStop();

    /**
     * Returns true if this reading holds an overruled consumption
     *
     * @return true of this reading holds an overruled consumption, false else
     */
    boolean isOverruledConsumption();

    /**
     * set the receiver's state valid
     *
     * @throws BusinessException if a business error occurred
     * @throws SQLException      if a database error occurred.
     * @deprecated use clearState
     */
    @Deprecated
    void setValid() throws BusinessException, SQLException;

    /**
     * Revokes the receiver and recalculates the next reading (if available & not modified)
     *
     * @throws BusinessException if a business error occurred
     * @throws SQLException      if a database error occurred.
     */
    void revoke() throws BusinessException, SQLException;

    /**
     * Revokes the receiver without recalculation of the next reading
     *
     * @throws BusinessException if a business error occurred
     * @throws SQLException      if a database error occurred.
     */
    void revokeWithoutRecalculation() throws BusinessException, SQLException;

    /**
     * Returns the multiplier used to calculate the consumption from the given
     * reading value
     *
     * @return the multiplier
     */
    BigDecimal getMultiplier();

    /**
     * Returns the next non obsolete reading (based on to time)
     *
     * @return the next RegisterReading
     */
    RegisterReading getNext();

    /**
     * Returns the previous non obsolete reading (based on to time)
     *
     * @return the previous RegisterReading
     */
    RegisterReading getPrevious();

    boolean canExpand();

    /**
     * Returns the flags on this reading
     *
     * @return an integer value representing a binary combination of custom
     *         usable flags
     */
    int getFlags();

    /**
     * Clears the flag at the specified bit position
     *
     * @param bitPos the bit position
     * @throws SQLException if a database error occurs
     * @throws BusinessException
     *                      if a business error occurs
     */
    void clearFlag(int bitPos) throws SQLException, BusinessException;

    /**
     * Sets the flag at the specified bit position
     *
     * @param bitPos the bit position to set
     * @throws SQLException if a database error occurs
     * @throws BusinessException
     *                      if a business error occurs
     */
    void setFlag(int bitPos) throws SQLException, BusinessException;

    /**
     * Sets all the flags according to the input parameter
     *
     * @param flags the flags to set
     * @throws SQLException      if a database error occurs
     * @throws BusinessException if a business error occurs
     */
    void updateFlags(int flags) throws SQLException, BusinessException;

    /**
     * Sets the estimate state
     *
     * @throws SQLException if a database error occurs
     * @throws BusinessException
     *                      if a business error occurs
     */
    void setEstimated() throws BusinessException, SQLException;

    /**
     * Sets the stop state
     *
     * @throws SQLException if a database error occurs
     * @throws BusinessException
     *                      if a business error occurs
     */
    void setStop() throws BusinessException, SQLException;

    /**
     * Sets the start state
     *
     * @throws SQLException if a database error occurs
     * @throws BusinessException
     *                      if a business error occurs
     */
    void setStart() throws BusinessException, SQLException;

    /**
     * Clears the state according to the input parameter
     *
     * @param stateToClear the state to clear  (bitmap)
     * @throws SQLException if a database error occurs
     * @throws BusinessException
     *                      if a business error occurs
     */
    void clearState(int stateToClear) throws SQLException, BusinessException;

    /**
     * @return the modification date for this reading
     */
    Date getModDate();

    /**
     * Checks if the  validation State can be set to <Code>RegisterValidationState.Confirm</Code>
     *
     * @return true if the the reading can be confirmed, false else
     */
    public boolean canConfirm();

    /**
     * Set the validation State to <Code>RegisterValidationState.Confirm</Code>
     *
     * @throws BusinessException if a business exception occurred
     * @throws SQLException      if a database error occurred.
     */
    public void confirm() throws BusinessException, SQLException;

}
