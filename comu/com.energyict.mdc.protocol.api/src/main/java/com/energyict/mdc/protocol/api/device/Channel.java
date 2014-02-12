package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Protectable;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.DateTime;
import com.energyict.mdc.common.interval.IntervalRecord;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.interval.RawIntervalRecord;
import com.energyict.mdc.common.interval.SingleTimeSeries;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Represents a single load profile on a data logger or energy meter.
 * Channel objects are created automatically when an Device is created.
 */

public interface Channel extends NamedBusinessObject, SingleTimeSeries, Protectable, CanGoOffline<OfflineLoadProfileChannel> {

    /**
     * Returns the device the receiver belongs to.
     *
     * @return the receiver's device.
     */
    Device getDevice();

    /**
     * Returns the channel used as a reference for validating data,
     * or null if no reference channel is used.
     *
     * @return the reference Channel for validation or null.
     */
    Channel getValidationReferenceChannel();

    /**
     * Returns the time series used as a reference for validating data,
     * found in the first validation rule that contains a reference channel
     * or null if no reference time series is used.
     *
     * @return the SingleTimeSeries
     */
    SingleTimeSeries getReferenceTimeSeries();

    /**
     * Returns a string describing the receiver when used in a virtual meter formula.
     *
     * @return the receiver's formula reference.
     */
    String formulaReference();

    /**
     * Returns the interval data that has been modified since the argument.
     * Implementation note: This method can be slow.
     *
     * @param since modification date.
     * @return a <CODE>List</CODE> of <CODE>IntervalRecord</CODE> in undefined order.
     */
    List<IntervalRecord> getChangedIntervalData(Date since);

    /**
     * Returns the Date, corresponding to the receiver's latest interval that has been validated.
     * All interval data before this date has passed the validation process,
     * and can be considered as final.
     * The interval ending at the returned date either
     * <UL>
     * <LI>has not been inspected by the validation process</LI>
     * <LI>is missing</LI>
     * <LI>has been marked suspect by the validation process,
     * and has not been confirmed or updated</LI>
     *
     * @return the Date of the latest validated interval.
     */
    Date getValidationLastValidDate();

    /**
     * Returns the number of "suspect" values for this channel. These are the values for which a validation rule has failed, and
     * has hence been marked as "suspect".
     *
     * @return The number of "suspect" values for this channel.
     */
    int getNumberOfSuspects();

    /**
     * Returns the date of the receiver's last interval.
     * This is the last interval available,
     * determined by querying the channel's data.
     * Under normal circumstances it should be equal to getLastReading().
     *
     * @return the Date of the latest interval.
     */
    java.util.Date getLastIntervalDate();

    /**
     * Returns the configured interval in seconds.
     * Equivalent to getRtu().getIntervalInSeconds().
     *
     * @return the interval in seconds.
     */
    int getIntervalInSeconds();

    /**
     * Returns the receiver's configured unit.
     * Equivalent to getPhenomenon().getUnit().
     *
     * @return the configured unit.
     */
    Unit getUnit();

    /**
     * Adds an interval reading to the receiver.
     *
     * @param dateTime      date and time the interval ends.
     * @param code          tariff code or zero.
     * @param value         the measured value.
     * @param intervalState the recorder's interval state.
     * @param overrule      if true, overwrite any existing interval record.
     *                      if false, and a reading exists for the given dateTime, the reading is not updated and
     *                      the method returns false.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated use add(Date date, int code , BigDecimal value, int intervalState , boolean
     *             overrule)
     */
    @Deprecated
    boolean add(DateTime dateTime, int code, int value, int intervalState, boolean overrule) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver
     *
     * @param dateTime date and time the interval ends.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @param overrule if true, overwrite any existing interval record.
     *                 if false, and a reading exists for the given dateTime, the reading is not updated and
     *                 the method returns false.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated use add(Date date, int code , BigDecimal value, boolean
     *             overrule
     */
    @Deprecated
    boolean add(DateTime dateTime, int code, int value, boolean overrule) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver
     *
     * @param dateTime date and time the interval ends.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated use add(Date date , int code, BigDecimal value)
     */
    @Deprecated
    boolean add(DateTime dateTime, int code, int value) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver
     *
     * @param date     end of interval.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @param overrule if true, overwrite any existing interval record.
     *                 if false, and a reading exists for the given dateTime, the reading is not updated and
     *                 the method returns false.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    boolean add(Date date, int code, int value, boolean overrule) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param date  end of interval.
     * @param code  tariff code or zero.
     * @param value the measured value.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    boolean add(Date date, int code, int value) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param dateTime end of interval.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @param status   the recorder's interval state.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated us add(Date, int code , BigDecimal value , int status)
     */
    @Deprecated
    boolean add(DateTime dateTime, int code, int value, int status) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param dateTime end of interval.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @param status   the recorder's interval state.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated use add(Date date, int code , BigDecimal value)
     */
    @Deprecated
    boolean add(DateTime dateTime, int code, BigDecimal value, int status) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param dateTime end of interval.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @param status   the recorder's interval state/
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated replace DateTime arguments by Date
     */
    @Deprecated
    boolean add(DateTime dateTime, int code, Quantity value, int status) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param date     the end of interval.
     * @param quantity the quantity to add
     * @param overrule true ot overrule a previous reading for the same interval.
     *                 false otherwise.
     * @param code     tariff code or zero.
     * @param status   the recorder's interval state/
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    boolean add(Date date, int code, Quantity quantity, int status, boolean overrule) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param dateTime end of interval.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @param status   the recorder's interval state/
     * @param overrule if true, overwrite any existing interval record.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated use add(Date date, int code , BigDecimal value , int status , boolean overrule)
     */
    @Deprecated
    boolean add(DateTime dateTime, int code, BigDecimal value, int status, boolean overrule) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param date  end of interval.
     * @param code  tariff code or zero.
     * @param value the measured value.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    boolean add(Date date, int code, BigDecimal value) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param date     end of interval.
     * @param code     tariff code or zero.
     * @param value    the measured value.
     * @param overrule if true, overwrite any existing interval record.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    boolean add(Date date, int code, BigDecimal value, boolean overrule) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param date          end of interval.
     * @param code          tariff code or zero.
     * @param value         the measured value.
     * @param intervalState the recorder's interval state/
     * @param overrule      if true, overwrite any existing interval record.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    boolean add(Date date, int code, BigDecimal value, int intervalState, boolean overrule) throws SQLException, BusinessException;

    /**
     * Adds an interval reading to the receiver.
     *
     * @param date            end of interval.
     * @param code            tariff code or zero.
     * @param value           the measured value.
     * @param intervalState   the recorder's interval state/
     * @param overrule        if true, overwrite any existing interval record.
     * @param clearValidation if true, existing validation entries will be journaled and deleted when overruled
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    boolean add(Date date, int code, BigDecimal value, int intervalState, boolean overrule, boolean clearValidation) throws SQLException, BusinessException;

    boolean add(Date date, int code, BigDecimal value, int intervalState, boolean overrule, BigDecimal rawValue) throws SQLException, BusinessException;

    boolean add(Date date, int code, BigDecimal value, int intervalState, boolean overrule, BigDecimal rawValue, boolean journal) throws SQLException, BusinessException;

    /**
     * updates the interval reading for the given dateTime.
     *
     * @param dateTime date and time the interval ends.
     * @param code     the new tariff code or zero.
     * @param value    the new value.
     * @return true if a record was added or updated, false otherwise.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     * @deprecated
     */
    @Deprecated
    boolean overrule(DateTime dateTime, int code, int value) throws SQLException, BusinessException;

    /**
     * Returns the ordinal of the channel (The 1 based index of the channel within the Device).
     *
     * @return the receiver's ordinal.
     */
    int getOrdinal();

    /**
     * Returns the time this Channel was created.
     *
     * @return creation date.
     */
    Date getCreateDate();

    /**
     * Returns the id of the Device the receiver belongs to.
     *
     * @return the Device id.
     */
    int getDeviceId();

    /**
     * tests if validation is active for this Channel.
     *
     * @return true, if validation is active , false otherwhise.
     */
    boolean getValidation();

    /**
     * Test if the receiver's uses a time zone with daylight saving time.
     * Equivalent to getRtu().useDaylightTime().
     *
     * @return true if the receiver's time zone uses daylight saving time,
     *         false otherwise.
     */
    boolean useDaylightTime();

    /**
     * indicates whether validation is based on fixed limits.
     * Only applicable if a validation reference channel is used.
     * The valid value interval is:
     * <UL>
     * <LI> if reference channel is null:   lowerlimit to upperlimit</LI>
     * <LI> if reference channel is not null and fixed limit false:
     * referencevalue - lowerlimit to referencevalue + upperlimit</LI>
     * <LI> if reference channel is not null and fixed limit true:
     * referencevalue * (1 - lowerlimit/100) to referencevalue * (1 +
     * upperlimit/100)</LI>
     * </UL>
     *
     * @return true, if validation uses fixed limits.
     *         false, if validation uses relative limits
     * @deprecated replaced by validation rules
     */
    @Deprecated
    boolean getValidationFixedLimit();

    /**
     * Returns the time of the last execution of the validation process for
     * this channel.
     *
     * @return the last validation execution time.
     */
    Date getValidationLastRunOfValidation();

    /**
     * Returns the lower validation limit.
     *
     * @return the lower validation limit.
     * @deprecated replaced by validation rules
     */
    @Deprecated
    BigDecimal getValidationLowerLimit();

    /**
     * Returns the id of the validation reference channel or zero.
     *
     * @return the validation reference channel id or zero.
     * @deprecated replaced by validation rules
     */
    @Deprecated
    int getValidationReferenceChannelId();

    /**
     * Returns the upper limit for validation.
     *
     * @return the validation upper limit.
     * @deprecated replaced by validation rules
     */
    @Deprecated
    BigDecimal getValidationUpperLimit();

    /**
     * updates the channel's last reading with the argument,
     * if the argument is later than the channel's current last reading.
     *
     * @param execDate the new last reading.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    void updateLastReadingIfLater(java.util.Date execDate) throws SQLException, BusinessException;

    /**
     * updates the channel's last reading with the argument.
     *
     * @param execDate the new last reading.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    void updateLastReading(java.util.Date execDate) throws SQLException, BusinessException;

    /**
     * called at the end of a data insertion.
     * The channel's last checked and last reading date will be updated if required.
     * Last checked will be updated if the lastChecked argument is before the channel's current last checked.
     * Last reading will be updated if the lastReading argument is after the channel's current last reading.
     * This method additionally signals the IntervalDataAddedEvent with the receiver as source argument.
     * Updating the last checked will result in clearing and journaling the validation entries
     *
     * @param lastChecked the last checked candidate date
     * @param lastReading the last reading candidate date
     * @throws SQLException      if a database error occurs
     * @throws BusinessException if a business error occurs
     */
    void dataAdded(java.util.Date lastChecked, java.util.Date lastReading) throws SQLException, BusinessException;

    /**
     * called at the end of a data insertion.
     * The channel's last checked and last reading date will be updated if required.
     * Last checked will be updated if the lastChecked argument is before the channel's current last checked.
     * Last reading will be updated if the lastReading argument is after the channel's current last reading.
     * This method additionally signals the IntervalDataAddedEvent with the receiver as source argument.
     * Validation entries can optionally be journaled when setting the last check back in the past by means of
     * the journal boolean
     *
     * @param lastChecked the last checked candidate date
     * @param lastReading the last reading candidate date
     * @param journal     indicates whether journaling of the validation entries is required before deleting the entries
     * @throws SQLException      if a database error occurs
     * @throws BusinessException if a business error occurs
     */
    void dataAdded(java.util.Date lastChecked, java.util.Date lastReading, boolean journal) throws SQLException, BusinessException;


    /**
     * Returns the configured multiplier.
     * All incoming interval data are multiplied by this value,
     * before being stored in the database.
     *
     * @return the receiver's multiplier.
     */
    BigDecimal getMultiplier();

    /**
     * returns the last date this Channel's configuration was modified.
     *
     * @return the last modification Date.
     * @deprecated use getModDate()
     */
    @Deprecated
    Date getModificationDate();

    /**
     * returns the last date this Channel's configuration was modified.
     *
     * @return the last modification Date.
     */
    Date getModDate();

    /**
     * Returns the minimum number of consecutive intervals
     * with a zero interval value, that will cause the validation
     * process to mark these records as suspect.
     * A zero value disables this validation feature.
     * A value of 1 has the same effect as setting the
     * validation lowerlimit to a small positive value.
     *
     * @return Value of property validationZeroLimit.
     * @deprecated replaced by validation rules
     */
    @Deprecated
    int getValidationZeroLimit();

    /**
     * Returns the number of decimal digits for interval values.
     * Used for
     * <UL>
     * <LI>rounding incoming values</LI>
     * <LI>reporting channel data</LI>
     * </UL>
     *
     * @return the number of fraction digits.
     */
    int getNumberOfFractionDigits();

    /**
     * Returns the receiver's phenomenon.
     * The phenomenon describes the measure
     * physical dimension and the unit.
     *
     * @return the Phenomenon
     */
    Phenomenon getPhenomenon();

    /**
     * Returns the receiver's phenomenon id.
     *
     * @return the Phenomenon id.
     */
    int getPhenomenonId();

    /**
     * Returns the Phenomenon of the raw value
     *
     * @return the Phenomenon of the raw value
     */
    Phenomenon getRawPhenomenon();

    /**
     * return the maximum allowed cumulative value for this channel + 1
     * E.g. in case of a 6 digits index register the value would be 1000000
     *
     * @return the overflow Value
     */
    BigDecimal getOverflowValue();

    /**
     * Adds interval data from a virtual meter field to the receiver.
     *
     * @param field the virtual meter field.
     * @param from  start date.
     * @param to    end date.
     * @return the number of interval records added or updated.
     * @throws BusinessException if a business error occured.
     * @throws SQLException      if a database error occured.
     */
    int addAll(SingleTimeSeries field, Date from, Date to) throws BusinessException, SQLException;

    /**
     * Adds interval data from a virtual meter field multiplied with a constant to the receiver.
     *
     * @param field      the virtual meter field.
     * @param from       start date.
     * @param to         end date.
     * @param multiplier value used to multiply each record value with
     * @return the number of interval records added or updated.
     * @throws BusinessException if a business error occured.
     * @throws SQLException      if a database error occured.
     */
    int addAll(SingleTimeSeries field, Date from, Date to, BigDecimal multiplier) throws BusinessException, SQLException;

    /**
     * Adds interval data with the given code
     * from a virtual meter field
     * multiplied with a constant to the receiver.
     *
     * @param field      the virtual meter field.
     * @param from       start date.
     * @param to         end date.
     * @param multiplier value used to multiply each record value with
     * @param code       code to match
     * @return the number of interval records added or updated.
     * @throws BusinessException if a business error occured.
     * @throws SQLException      if a database error occured.
     */
    int addAll(SingleTimeSeries field, Date from, Date to, BigDecimal multiplier, int code) throws BusinessException, SQLException;

    /**
     * Removes all interval data between from and to date.
     *
     * @param from beginning op period to delete.
     * @param to   end of period to delete.
     * @throws BusinessException if a business error occured.
     * @throws SQLException      if a database error occured.
     */
    void removeAll(Date from, Date to) throws BusinessException, SQLException;

    /**
     * Removes all interval data between from and to date. An extra condition is that the code value needs to match
     * the argument and there should be a corresponding entry in the referenced field
     *
     * @param from  beginning op period to delete.
     * @param to    end of period to delete.
     * @param field the reference field
     * @param code  the code value
     * @throws BusinessException if a business error occured.
     * @throws SQLException      if a database error occured.
     */
    void removeAll(Date from, Date to, SingleTimeSeries field, int code) throws BusinessException, SQLException;

    /**
     * updates the last checked timestamp to the argument,
     * only if the argument is earlier than the current last checked timestamp.
     * All validation records related to suspect intervals after the argument
     * are deleted.
     *
     * @param execDate new last checked timestamp.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business error occured.
     */
    void updateLastCheckedIfEarlier(Date execDate) throws SQLException, BusinessException;

    /**
     * Updates the last checked timestamp.
     *
     * @param execDate the new last checked value.
     * @throws SQLException      if a dabase error occured.
     * @throws BusinessException if a business exception occured.
     */
    void updateLastChecked(Date execDate) throws SQLException, BusinessException;

    /**
     * Recalculates the derived validation fields (lastvalid and numberofsuspects) and updates
     * the database
     *
     * @throws SQLException      if a database error occurs
     * @throws BusinessException if a business error occurs
     */
    void updateCalculatedValidationFields() throws SQLException, BusinessException;

    /**
     * Executes the validationprocedure for the channel.
     *
     * @return true if validation succeeded,
     *         false otherwise
     * @throws BusinessException if a BusinessException occured
     * @throws SQLException      if a dabase error occured.
     */
    boolean executeValidation() throws BusinessException, SQLException;

    /**
     * indicates if the validationprocedure for the channel can be executed
     *
     * @return true if validation can be executed
     */
    boolean isValidationPossible();

    /**
     * tests if the receiver has a validation reference channel
     *
     * @return true if the receveiver has a reference channel.
     *         false otherwhise
     */
    boolean hasValidationReferenceChannel();

    /**
     * returns the upper limit load profile used for validation
     *
     * @param from retrieval start date
     * @param to   retrieval end date
     * @return a List of IntervalRecord objects
     */
    List<IntervalRecord> getValidationUpperLimit(Date from, Date to);

    /**
     * returns the upper limit load profile used for validation
     *
     * @param from retrieval start date
     * @param to   retrieval end date
     * @return a List of IntervalData objects
     */
    List<IntervalRecord> getValidationLowerLimit(Date from, Date to);

    /**
     * returns the prime register for the channel
     *
     * @return the Channel's prime register or null
     */
    Register getPrimeRegister();

    /**
     * tests if the channel has a prime register
     *
     * @return true if the channel has a prime register. False otherwise
     */
    boolean hasPrimeRegister();

    /**
     * returns the intervaldata for a given period ,
     * as known by the system at a certain time.
     *
     * @param from start date of period.
     * @param to   end date of period.
     * @param when date at what time the returned data are actual.
     * @return a List of IntervalRecord objects.
     */
    List<IntervalRecord> getIntervalDataAt(Date from, Date to, Date when);

    /**
     * Returns intervaldata for the given period,
     * filling missing intervals.
     * If includeValidationStatus is true, missing interval records will contain the current validation entry details
     *
     * @param from                    retrieval start date
     * @param to                      retrieval end date
     * @param includeValidationStatus indicates whether we want the validation details to be appended in the records or not
     * @param when                    date at what time the returned data are actual.
     * @return a List of IntervalRecord objects
     */
    List<? extends IntervalRecord> getIntervalDataWithFill(Date from, Date to, boolean includeValidationStatus, Date when);

    /**
     * Returns intervaldata for the given period,
     * filling missing intervals
     *
     * @param from retrieval start date
     * @param to   retrieval end date
     * @param when date at what time the returned data are actual.
     * @return a List of IntervalRecord objects
     */
    List<IntervalRecord> getIntervalDataWithFillAt(Date from, Date to, Date when);


    /**
     * indicates whether validation is performed immediately when storing new data.
     *
     * @return true if validation is performed after storing,
     *         false if validation is performed in batch.
     */
    boolean getValidateOnStore();

    /**
     * converts, if possible, the quantity amount to an appropriate amount with respect to the channels unit
     *
     * @param qty quantity to convert
     * @return the converted amount
     * @throws BusinessException if a BusinessException occured
     */
    BigDecimal extractAmount(Quantity qty) throws BusinessException;

    /**
     * converts, if possible, the raw quantity amount to an appropriate amount with respect to the channels unit
     *
     * @param qty quantity to convert
     * @return the converted amount
     * @throws BusinessException if a BusinessException occured
     */
    BigDecimal extractRawAmount(Quantity qty) throws BusinessException;

    /**
     * indicates if missing values between a power down event and
     * a power up event will automatically be filled with zero values.
     *
     * @return true if auto fill on power down is on , false otherwise
     */
    boolean getAutoFillPowerDown();

    /**
     * tests if this object is being used by other objects
     *
     * @return true if in use , false otherwise.
     */
    boolean isInUse();

    /**
     * tests if this object is being used by other objects
     *
     * @param includingValdiationRules if true, include validation rules in the test.
     * @return true if in use , false otherwise.
     */
    boolean isInUse(boolean includingValdiationRules);

    /**
     * Restores previous data to the current interval
     *
     * @param from start date of period.
     * @param to   end date of period.
     * @param when date at what time the returned data are actual.
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business error occured.
     */
    void restore(Date from, Date to, Date when) throws BusinessException, SQLException;

    /**
     * Returns a list of channels that reference this channel. (via validation rule)
     *
     * @return a <CODE>List</CODE> of <CODE>Channels</CODE>.
     */
    List<Channel> getReferencingChannels();

    /**
     * Returns the receiver's load profile index
     *
     * @return Value of property loadProfileIndex
     */
    int getLoadProfileIndex();

    /**
     * tests if cumulative register values (true) or register advances (false)
     * should be read
     *
     * @return true if cumulative register values should be read
     * @deprecated use getValueCalculationMethod
     */
    @Deprecated
    boolean getCumulative();

    /**
     * tests if basic data (true) or engineering units (false)
     * should be read
     *
     * @return true if basic data should be read
     * @deprecated user getReadingMethod
     */
    @Deprecated
    boolean getBasic();

    String getIntervalTable();

    /**
     * Returns the receiver's lock date. All data before this date cannot be modified any more.
     *
     * @return the receiver's lock date
     */
    Date getLockDate();

    /**
     * returns a <Code>List</Code> of Time Of Use registers for the channel
     *
     * @return the Channel's Time Of Use registers
     */
    List<Register> getTouRegisters();

    /**
     * tests if the channel has Time Of Use registers
     *
     * @return true if the channel has Time Of Use registers. False otherwise
     */
    boolean hasTouRegisters();

    /**
     * Returns a list of Device (device) registers that reference this channel.
     *
     * @return a <CODE>List</CODE> of <CODE>RtuRegisters</CODE>.
     */
    List<Register> getReferencingRtuRegisters();


    /**
     * returns true if the channel has at least one data record
     *
     * @return true if the channel has at least one data record
     */
    boolean hasIntervalData();

    /**
     * locks the channel's timeseries up to the argument.
     * If the argument is earlier than the current lock date, no change is made.
     *
     * @param when lock date
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    void lockTo(Date when) throws SQLException, BusinessException;

    /**
     * unlocks the channel's timeseries down to the argument.
     * If the argument is later than the current lock date, no change is made.
     *
     * @param when unlock date
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business exception occured.
     */
    void unLockTo(Date when) throws SQLException, BusinessException;

    /**
     * This method returns the time interval (as a TimeDuration object) of the
     * channel, which expresses the sample rate of the channel.
     *
     * @return the time interval between samples in this channel
     */
    TimeDuration getInterval();

    /**
     * Tests if the channel has support for storing the raw value
     *
     * @return true if raw values are supported
     */
    boolean hasRawValue();

    /**
     * cfr. DeviceType isChannelJournalUsed
     *
     * @return true if a modifications are strored in the journal table
     */
    boolean isChannelJournalUsed();

    /**
     * The validation on a channel validates all <code>IntervalRecord</code>s between the <code>validationLastCheckedValue</code> date
     * and the <code>lastReading</code> date. When validating the channeldata, the validationLastCheckedValue is updated,
     * so that when running a second validation session validated <code>IntervalRecords</code> are disguarded.
     * By setting the <code>validationWindowStart</code> property, an integer value N representing a number of days,
     * the earliest date the <code>validationLastCheckedValue</code> will be set, is  the date of the day of validation - N days.
     * By setting the <code>validationWindowStart</code> The next vaproperty validation will start on the max(validationLastCheckedValue, today - validationWindowstart)
     *
     * @return the number of days till now from which the validation will start.
     */
    int getValidationWindowStart();

    /**
     * Normally the validation on a channel validates all <code>IntervalRecord</code>s between the <code>validationLastCheckedValue</code> date
     * and the <code>lastReading</code> date. By setting the <code>validationWindowEnd</code> property, an integer value representing a number of days,
     * validation will always start at last this  number of days from now. This is a way to assure  that intervalRecords after a certain date
     * are not processed any more by the validation procedure, and so slow down the whole validation process.
     * By setting the <code>validationWindowEnd</code> property validation will end on the min(lastReading, today + validationWindow)
     *
     * @return the number of days from now at which the validation will end.
     */
    int getValidationWindowEnd();

    /**
     * Returns the id of the LoadProfile associated with this channel, 0 if no LoadProfile associated
     *
     * @return the id of the LoadProfile
     */
    int getLoadProfileId();

    /**
     * Returns the LoadProfile associated with this channel, null if no LoadProfile associated
     *
     * @return the LoadProfile
     */
    LoadProfile getLoadProfile();

    /**
     * Returns the id of the <CODE>DataCollectionMonitor</CODE> associated with this channel, 0 if none is associated
     *
     * @return the id of the <CODE>DataCollectionMonitor</CODE>
     */
    int getDataCollectionMonitorId();

    ObisCode getDeviceRegisterMappingObisCode();

    Unit  getDeviceRegisterMappingUnit();

    int getRtuRegisterMappingId();

    /**
     * will always return true for the moment
     * will be removed
     */
    @Deprecated
    boolean isStoreData();

    ValueCalculationMethod getValueCalculationMethod();

    ReadingMethod getReadingMethod();

    MultiplierMode getMultiplierMode();

    BigDecimal calculateValueFromRaw(RawIntervalRecord rec, BigDecimal previousRawValue) throws BusinessException;

    Quantity applyMultiplier(Quantity amount, Date date);
}
