package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.*;
import com.energyict.cpo.*;
import com.energyict.cuo.core.DesktopDecorator;
import com.energyict.dynamicattributes.AttributeType;
import com.energyict.dynamicattributes.BusinessObjectType;
import com.energyict.interval.DateTime;
import com.energyict.interval.IntervalRecord;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterMapping;
import com.energyict.mdw.core.*;
import com.energyict.mdw.datacollection.DataCollectionMonitor;
import com.energyict.mdw.estimation.EstimationProcedure;
import com.energyict.mdw.offline.OfflineLoadProfileChannel;
import com.energyict.mdw.relation.*;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.validation.ValidationRuleShadow;
import com.energyict.mdw.task.ValidationSession;
import com.energyict.mdw.validation.ValidationRule;
import com.energyict.metadata.TypeId;

import javax.swing.Icon;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 25-okt-2010
 * Time: 8:47:21
 */
public class DummyChannel implements Channel {

    private int loadProfileIndex;
    private final int intervalInSeconds;
    private Calendar lastReading;
    private Rtu rtu;
    
    public DummyChannel(int loadProfileIndex, int intervalInSeconds, Calendar lastReading) {
        this(loadProfileIndex, intervalInSeconds, lastReading, null);
    }

    public ChannelShadow getShadow() {
        return new ChannelShadow(this);
    }

    public DummyChannel(int loadProfileIndex, int intervalInSeconds, Calendar lastReading, Rtu rtu) {
        this.loadProfileIndex = loadProfileIndex;
        this.intervalInSeconds = intervalInSeconds;
        this.lastReading = lastReading;
        this.rtu = rtu;
    }

    public Rtu getRtu() {
        return rtu;
    }

    public void setRtu(Rtu rtu) {
        this.rtu = rtu;
    }

    public void update(ChannelShadow shadow) throws SQLException, BusinessException {
        //setName(shadow.getName());
        //rtuId = shadow.getRtuId();
        //ordinal = shadow.getOrdinal();
        if (lastReading == null) {
            lastReading = Calendar.getInstance();
        }
        lastReading.setTime(shadow.getLastReading());

        //validation = shadow.getValidation();
        //validationLastCheckedValue = shadow.getValidationLastCheckedValue();
        //validationLastRunOfValidation = shadow.getValidationLastRunOfValidation();
        //validateOnStore = shadow.getValidateOnStore();
        //multiplier = shadow.getMultiplier();
        //externalName = trim(shadow.getExternalName());
        //phenomenonId = shadow.getPhenomenonId();
        //rawPhenomenonId = shadow.getRawPhenomenonId();
        //numberOfFractionDigits = shadow.getNumberOfFractionDigits();
        //autoFillPowerDown = shadow.getAutoFillPowerDown();
        loadProfileIndex = shadow.getLoadProfileIndex();
        //cumulative = shadow.getCumulative();
        //basic = shadow.getBasic();
        //lockDate = shadow.getLockDate();
        //interval = shadow.getInterval();
        //this.validationLastValidDate = shadow.getValidationLastValidDate();
        //this.overflowValue = shadow.getOverflowValue();
        //this.validationWindowStart = shadow.getValidationWindowStart();
        //this.validationWindowEnd = shadow.getValidationWindowEnd();
        //loadProfileId = shadow.getLoadProfileId();
        //defaultEstimationProcedureId = shadow.getDefaultEstimationProcedureId();
        // clear derived fields
    }

    public String getName() {
        return "DummyChannel";
    }

    public String getExternalName() {
        return "DummyExternalName";
    }

    public Channel getValidationReferenceChannel() {
        return null;
    }

    public SingleTimeSeries getReferenceTimeSeries() {
        return null;
    }

    public String formulaReference() {
        return null;
    }

    public Quantity getConsumption(Date from, Date to) {
        return null;
    }

    public Quantity getConsumption(Date from, Date to, Integer codeval) {
        return null;
    }

    public Quantity getConsumption(Date from, Date to, Code code, Integer codeval) {
        return null;
    }


    public boolean isVirtualMeter() {
        return false;
    }

    public boolean isChannel() {
        return true;
    }

    public SingleTimeSeries getTimeSeries(String name) {
        return null;
    }

    public SingleTimeSeries getTimeSeries(int index) {
        return null;
    }

    public Phenomenon getPhenomenon(String name) {
        return null;
    }

    public Phenomenon getPhenomenon(int index) {
        return null;
    }

    public TimeZone getTimeZone() {
        return getRtu().getTimeZone();
    }

    public boolean hasIntervalState() {
        return false;
    }

    public boolean hasModDate() {
        return false;
    }

    public Folder getFolder() {
        return null;
    }

    public Date getValidationLastCheckedValue() {
        return null;
    }

    public Date getLastReading() {
        return lastReading == null ? null : lastReading.getTime();
    }

    public void rename(String name) throws BusinessException, SQLException {

    }

    public String getPath() {
        return null;
    }

    public String getNameSeparator() {
        return null;
    }

    public String getFullName() {
        return null;
    }

    public String getParentName() {
        return null;
    }

    public List<IntervalRecord> getIntervalData(DateTime from, DateTime to) {
        return null;
    }

    public List<IntervalRecord> getIntervalData(Date from, Date to) {
        return null;
    }

    public List<IntervalRecord> getIntervalData(TimePeriod period) {
        return null;
    }

    public List<IntervalRecord> getIntervalData(Date from, Date to, boolean includeValidation) {
        return null;
    }

    public List<IntervalRecord> getChangedIntervalData(Date since) {
        return null;
    }

    public List<IntervalRecord> getChangedIntervalData(Date since, Date from, Date to) {
        return null;
    }

    public int getFieldCount() {
        return 0;
    }

    public String[] getFieldNames() {
        return new String[0];
    }

    public Unit[] getFieldUnits() {
        return new Unit[0];
    }

    public List<ChannelJournalEntry> getChannelJournal(Date date) {
        return null;
    }

    public List<ChannelJournalEntry> getChannelJournal(DateTime from, DateTime to) {
        return null;
    }

    public List<ChannelJournalEntry> getChannelJournal(TimePeriod period) {
        return null;
    }

    public List<VirtualMeterField> getReferencingFields() {
        return null;
    }

    public List<VirtualMeter> getReferencingVirtualMeters() {
        return null;
    }

    public List<MeterPortfolio> getReferencingPortfolios() {
        return null;
    }

    public Date getValidationLastValidDate() {
        return null;
    }

    public int getNumberOfSuspects() {
        return 0;
    }

    public Date getLastIntervalDate() {
        return null;
    }

    public int getIntervalInSeconds() {
        return intervalInSeconds;
    }

    public int getIndexFor(String fieldName) {
        return 0;
    }

    public Unit getUnit() {
        return null;
    }

    public boolean add(DateTime dateTime, int code, int value, int intervalState, boolean overrule) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(DateTime dateTime, int code, int value, boolean overrule) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(DateTime dateTime, int code, int value) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(Date date, int code, int value, boolean overrule) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(Date date, int code, int value) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(DateTime dateTime, int code, int value, int status) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(DateTime dateTime, int code, BigDecimal value, int status) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(DateTime dateTime, int code, Quantity value, int status) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(Date date, int code, Quantity quantity, int status, boolean overrule) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(DateTime dateTime, int code, BigDecimal value, int status, boolean overrule) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(Date date, int code, BigDecimal value) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(Date date, int code, BigDecimal value, boolean overrule) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(Date date, int code, BigDecimal value, int intervalState, boolean overrule) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(Date date, int code, BigDecimal value, int intervalState, boolean overrule, boolean clearValidation) throws SQLException, BusinessException {
        return false;
    }

    public boolean add(final Date date, final int code, final BigDecimal value, final int intervalState, final boolean overrule, final BigDecimal rawValue) throws SQLException, BusinessException {
        return false;  //TODO implement proper functionality.
    }

    public boolean add(final Date date, final int code, final BigDecimal value, final int intervalState, final boolean overrule, final BigDecimal rawValue, final boolean journal) throws SQLException, BusinessException {
        return false;  //TODO implement proper functionality.
    }

    public boolean overrule(DateTime dateTime, int code, int value) throws SQLException, BusinessException {
        return false;
    }

    public void journal(IntervalRecord old, BigDecimal newValue, int newCode, int newFlags) throws SQLException {

    }

    public int getOrdinal() {
        return 0;
    }

    public Date getCreateDate() {
        return null;
    }

    public int getRtuId() {
        return 0;
    }

    public boolean getValidation() {
        return false;
    }

    public boolean useDaylightTime() {
        return false;
    }

    public boolean getValidationFixedLimit() {
        return false;
    }

    public Date getValidationLastRunOfValidation() {
        return null;
    }

    public BigDecimal getValidationLowerLimit() {
        return null;
    }

    public int getValidationReferenceChannelId() {
        return 0;
    }

    public BigDecimal getValidationUpperLimit() {
        return null;
    }

    public void updateLastReadingIfLater(Date execDate) throws SQLException, BusinessException {
        if (execDate != null) {
            if (lastReading == null) {
                lastReading = Calendar.getInstance(getTimeZone());
                lastReading.setTime(execDate);
            } else {
                if (execDate.after(lastReading.getTime())) {
                    lastReading.setTime(execDate);
                }
            }
        }
    }

    public void updateLastReading(Date execDate) throws SQLException, BusinessException {
        if (execDate != null) {
            if (lastReading == null) {
                lastReading = Calendar.getInstance(getTimeZone());
            }
            lastReading.setTime(execDate);
        }
    }

    public void dataAdded(Date lastChecked, Date lastReading) throws SQLException, BusinessException {

    }

    public void dataAdded(Date lastChecked, Date lastReading, boolean journal) throws SQLException, BusinessException {

    }

    public BigDecimal getMultiplier() {
        return null;
    }

    public Date getModificationDate() {
        return null;
    }

    public Date getModDate() {
        return null;
    }

    public int getValidationZeroLimit() {
        return 0;
    }

    public int getNumberOfFractionDigits() {
        return 0;
    }

    public Phenomenon getPhenomenon() {
        return null;
    }

    public String getColumnName() {
        return "dummyColumnName";
    }

    public List getIntervalDataWithFill(Date from, Date to) {
        return null;
    }

    public Quantity getConsumption(TimePeriod period) {
        return null;
    }

    public Quantity getMin(TimePeriod period) {
        return null;
    }

    public Quantity getMin(Date from, Date to) {
        return null;
    }

    public Quantity getMin(Date from, Date to, Integer intervalStateMask) {
        return null;
    }

    public Quantity getMin(Date from, Date to, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getMin(Date from, Date to, Code code, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getMax(TimePeriod period) {
        return null;
    }

    public Quantity getMax(Date from, Date to) {
        return null;
    }

    public Quantity getMax(Date from, Date to, Integer intervalStateMask) {
        return null;
    }

    public Quantity getMax(Date from, Date to, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getMax(Date from, Date to, Code code, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getAvg(TimePeriod period) {
        return null;
    }

    public Quantity getAvg(Date from, Date to) {
        return null;
    }

    public Quantity getAvg(Date from, Date to, Integer intervalStateMask) {
        return null;
    }

    public Quantity getAvg(Date from, Date to, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getAvg(Date from, Date to, Code code, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getStdDev(TimePeriod period) {
        return null;
    }

    public Quantity getStdDev(Date from, Date to) {
        return null;
    }

    public Quantity getStdDev(Date from, Date to, Integer intervalStateMask) {
        return null;
    }

    public Quantity getStdDev(Date from, Date to, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getStdDev(Date from, Date to, Code code, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getSum(TimePeriod period) {
        return null;
    }

    public Quantity getSum(Date from, Date to) {
        return null;
    }

    public Quantity getSum(Date from, Date to, Integer intervalStateMask) {
        return null;
    }

    public Quantity getSum(Date from, Date to, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public Quantity getSum(Date from, Date to, Code code, Integer codeval, Integer intervalStateMask) {
        return null;
    }

    public List<IntervalRecord> getTop(TimePeriod period, int n) {
        return null;
    }

    public List<IntervalRecord> getTop(Date from, Date to, int n) {
        return null;
    }

    public List<IntervalRecord> getTop(Date from, Date to, int n, Integer codeVal) {
        return null;
    }

    public List<IntervalRecord> getTop(Date from, Date to, int n, Code code, Integer codeVal, Integer mask) {
        return null;
    }

    public int getPhenomenonId() {
        return 0;
    }

    public Phenomenon getRawPhenomenon() {
        return null;
    }

    public int getRawPhenomenonId() {
        return 0;
    }

    public BigDecimal getOverflowValue() {
        return null;
    }

    public int addAll(SingleTimeSeries field, Date from, Date to) throws BusinessException, SQLException {
        return 0;
    }

    public int addAll(SingleTimeSeries field, Date from, Date to, BigDecimal multiplier) throws BusinessException, SQLException {
        return 0;
    }

    public int addAll(SingleTimeSeries field, Date from, Date to, BigDecimal multiplier, int code) throws BusinessException, SQLException {
        return 0;
    }

    public void removeAll(Date from, Date to) throws BusinessException, SQLException {

    }

    public void removeAll(Date from, Date to, SingleTimeSeries field, int code) throws BusinessException, SQLException {

    }

    public void updateLastCheckedIfEarlier(Date execDate) throws SQLException, BusinessException {

    }

    public void updateLastChecked(Date execDate) throws SQLException, BusinessException {

    }

    public void updateCalculatedValidationFields() throws SQLException, BusinessException {

    }

    public boolean executeValidation() throws BusinessException, SQLException {
        return false;
    }

    public boolean isValidationPossible() {
        return false;
    }

    public boolean hasValidationReferenceChannel() {
        return false;
    }

    public List<IntervalRecord> getValidationUpperLimit(Date from, Date to) {
        return null;
    }

    public List<IntervalRecord> getValidationLowerLimit(Date from, Date to) {
        return null;
    }

    public List<ValidationRule> getValidationRules() {
        return Collections.EMPTY_LIST;
    }

    public RtuRegister getPrimeRegister() {
        return null;
    }

    public boolean hasPrimeRegister() {
        return false;
    }

    public List<IntervalRecord> getIntervalDataAt(Date from, Date to, Date when) {
        return Collections.EMPTY_LIST;
    }

    public IntervalRecord getIntervalRecord(Date date) {
        return null;
    }

    public Set<Channel> allReferencedChannels(TimePeriod period) {
        return null;
    }

    public Set<Channel> referencedChannels(TimePeriod period) {
        return Collections.EMPTY_SET;
    }

    public String getViewName() {
        return null;
    }

    public List<? extends IntervalRecord> getIntervalDataWithFill(Date from, Date to, boolean includeValidationStatus, Date when) {
        return Collections.EMPTY_LIST;
    }

    public List<IntervalRecord> getIntervalDataWithFillAt(Date from, Date to, Date when) {
        return Collections.EMPTY_LIST;
    }

    public boolean getValidateOnStore() {
        return false;
    }

    public BigDecimal extractAmount(Quantity qty) throws BusinessException {
        return null;
    }

    public BigDecimal extractRawAmount(Quantity qty) throws BusinessException {
        return null;
    }

    public List<MeterRegister> getReferencingMeterRegisters() {
        return Collections.EMPTY_LIST;
    }

    public void startBulk() {

    }

    public void stopBulk() {

    }

    public IntervalDataStorer getIntervalStorer() {
        return null;
    }

    public boolean getAutoFillPowerDown() {
        return false;
    }

    public boolean isInUse() {
        return false;
    }

    public boolean isInUse(boolean includingValdiationRules) {
        return false;
    }

    public List<ValidationRule> getAllReferencingValidationRules() {
        return Collections.EMPTY_LIST;
    }

    public void updateValidationRules(List<Integer> removed, List<Integer> added, ChannelShadow shadow, Map<Integer, ValidationRuleShadow> shadowsToUpdate) throws SQLException, BusinessException {

    }

    public void restore(Date from, Date to, Date when) throws BusinessException, SQLException {

    }

    public List<Channel> getReferencingChannels() {
        return Collections.EMPTY_LIST;
    }

    public int getLoadProfileIndex() {
        return loadProfileIndex;
    }

    public boolean getCumulative() {
        return false;
    }

    public boolean getBasic() {
        return false;
    }

    public int getStoreVersion() {
        return 0;
    }

    public int bind(PreparedStatement stmnt, int offset, DateTime dateTime) throws SQLException {
        return 0;
    }

    public String getIntervalTable() {
        return null;
    }

    public Date getLockDate() {
        return null;
    }

    public List<RtuRegister> getTouRegisters() {
        return Collections.EMPTY_LIST;
    }

    public boolean hasTouRegisters() {
        return false;
    }

    public List<RtuRegister> getReferencingRtuRegisters() {
        return Collections.EMPTY_LIST;
    }

    public List<IntervalRecord> getFailedRecords(ValidationSession session, List<IntervalRecord> transactions) throws BusinessException, SQLException {
        return Collections.EMPTY_LIST;
    }

    public ChannelType getChannelType() {
        return null;
    }

    public boolean hasIntervalData() {
        return false;
    }

    public void lockTo(Date when) throws SQLException, BusinessException {

    }

    public void unLockTo(Date when) throws SQLException, BusinessException {

    }

    public TimeDuration getInterval() {
        return new TimeDuration(getIntervalInSeconds());
    }

    public boolean hasRawValue() {
        return false;
    }

    public boolean isChannelJournalUsed() {
        return false;
    }

    public int getValidationWindowStart() {
        return 0;
    }

    public int getValidationWindowEnd() {
        return 0;
    }

    public int getLoadProfileId() {
        return 0;
    }

    public LoadProfile getLoadProfile() {
        return null;
    }

    /**
     * Returns the id of the <CODE>DataCollectionMonitor</CODE> associated with this channel, 0 if none is associated
     *
     * @return the id of the <CODE>DataCollectionMonitor</CODE>
     */
    public int getDataCollectionMonitorId() {
        return 0;
    }

    /**
     * Returns the <CODE>DataCollectionMonitor</CODE> associated with this channel, null if none is associated
     *
     * @return the <CODE>DataCollectionMonitor</CODE>
     */
    public DataCollectionMonitor getDataCollectionMonitor() {
        return null;
    }

    /**
     * @return true if a <CODE>DataCollectionMonitor</CODE> was defined for the channel, false if not
     */
    public boolean hasDataCollectionMonitor() {
        return false;
    }

    public void updateInterval(TimeDuration interval) throws SQLException, BusinessException {

    }

    public EstimationProcedure getDefaultEstimationProcedure() {
        return null;
    }

    public int getIntervalCountIncludeNulls(TimePeriod period) {
        return 0;
    }

    public int getIntervalCount(TimePeriod period) {
        return 0;
    }

    public RtuRegisterMapping getRtuRegisterMapping() {
        return null;
    }

    public int getRtuRegisterMappingId() {
        return 0;
    }

    public boolean isStoreData() {
        return true;
    }

    /**
     * updates the <CODE>DataCollectionMonitor</CODE> for this channel to the specified one
     *
     * @param monitor the new <CODE>DataCollectionMonitor</CODE>
     * @throws com.energyict.cbo.BusinessException
     *                               if a business exception occurred
     * @throws java.sql.SQLException if a database error occurred
     */
    public void updateDataCollectionMonitor(final DataCollectionMonitor monitor) throws SQLException, BusinessException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public IntervalRecord getLastIntervalRecord(Date from, Date to, Code code, Integer codeValue, Integer mask) {
        return null;
    }

    public String getCanonicalName() {
        return null;
    }

    public int getDateFormat() {
        return 0;
    }

    public SqlBuilder getUnionViewBuilder(Date from, Date to, Date when, boolean needsWhere) throws BusinessException {
        return null;
    }

    public SqlBuilder getUnionViewBuilder(Date from, Date to, Date when, List<VirtualMeterField> fields, boolean needsWhere) throws BusinessException {
        return null;
    }

    public SqlBuilder getViewBuilder(Date from, Date to, Date when) throws BusinessException {
        return null;
    }

    public SqlBuilder getViewBuilder(Date from, Date to, Date when, List<VirtualMeterField> fields) throws BusinessException {
        return null;
    }

    public SqlBuilder getViewBuilder(Date from, Date to) throws BusinessException {
        return null;
    }

    public SqlBuilder getViewBuilder() throws BusinessException {
        return null;
    }

    public void delete() throws BusinessException, SQLException {

    }

    public boolean canDelete() {
        return false;
    }

    public BusinessObjectFactory getFactory() {
        return null;
    }

    public String[] getCopyAspects() {
        return new String[0];
    }

    public Class getAspectType(String name) {
        return null;
    }

    public TypeId getTypeId() {
        return null;
    }

    public BusinessObject getBusinessObject() {
        return null;
    }

    public String getType() {
        return null;
    }

    public String displayString() {
        return null;
    }

    public Icon getIcon() {
        return null;
    }

    public boolean proxies(BusinessObject obj) {
        return false;
    }

    public Relation getDefaultRelation() {
        return null;
    }

    public Relation getDefaultRelation(Date date) {
        return null;
    }

    public AttributeType getDefaultAttributeType() {
        return null;
    }

    public RelationType getDefaultRelationType() {
        return null;
    }

    public Object get(String attributeName, Date date) {
        return null;
    }

    public BusinessObjectType getBusinessObjectType() {
        return null;
    }

    public Object get(String attributeName) {
        return null;
    }

    public Object get(AttributeType attributeType, Date date) {
        return null;
    }

    public Object get(AttributeType attributeType) {
        return null;
    }

    public Dependent getOwner() {
        return null;
    }

    public Folder getContainer() {
        return null;
    }

    public boolean isInTree(Folder folder) {
        return false;
    }

    public FolderMember copy(CopyContext context) throws BusinessException, SQLException {
        return null;
    }

    public int getId() {
        return 0;
    }

    public DesktopDecorator asDesktopDecorator() {
        return null;
    }

    public boolean isAuthorized(UserAction action) {
        return false;
    }

    public List<RelationType> getAvailableRelationTypes() {
        return null;
    }

    public List<? extends AttributeType> getAvailableAttributeTypes() {
        return null;
    }

    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete) {
        return null;
    }

    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete, int fromRow, int toRow) {
        return null;
    }

    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return null;
    }

    public List<Relation> getRelations(RelationAttributeType attrib, TimePeriod period, boolean includeObsolete) {
        return null;
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     *
     * @return The Offline version of this object
     */
    @Override
    public OfflineLoadProfileChannel goOffline() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
