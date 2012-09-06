package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.*;
import com.energyict.cpo.BusinessObject;
import com.energyict.cpo.BusinessObjectFactory;
import com.energyict.cpo.Dependent;
import com.energyict.cpo.TypedProperties;
import com.energyict.cuo.core.DesktopDecorator;
import com.energyict.dynamicattributes.AttributeType;
import com.energyict.dynamicattributes.BusinessObjectType;
import com.energyict.interval.DateTime;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.shadow.tasks.*;
import com.energyict.mdc.tasks.*;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;
import com.energyict.mdw.offline.OfflineRtu;
import com.energyict.mdw.relation.Relation;
import com.energyict.mdw.relation.RelationAttributeType;
import com.energyict.mdw.relation.RelationTransaction;
import com.energyict.mdw.relation.RelationType;
import com.energyict.mdw.sdp.ServiceDeliveryPoint;
import com.energyict.mdw.shadow.*;
import com.energyict.mdw.task.ChangeRtuTypeTransaction;
import com.energyict.metadata.TypeId;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterData;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 25-okt-2010
 * Time: 8:58:57
 */
public class DummyRtu implements Rtu {

    private TimeZone timeZone;
    private DummyChannel channel;

    public DummyRtu(TimeZone timeZone) {
        this(timeZone, null);
    }

    public DummyRtu(TimeZone timeZone, DummyChannel channel) {
        this.timeZone = timeZone;
        this.channel = channel;
        if (channel != null) {
            this.channel.setRtu(this);
        }
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public RtuShadow getShadow() {
        return null;
    }

    public void update(RtuShadow shadow) throws SQLException, BusinessException {

    }

    public void moveToFolder(Folder folder) throws SQLException, BusinessException {

    }

    public void add(DateTime dateTime, int code, List<Quantity> values) throws SQLException, BusinessException {

    }

    public void add(DateTime dateTime, int code, List<Quantity> values, int status) throws SQLException, BusinessException {

    }

    public void add(DateTime dateTime, int code, Quantity quantity, int status, int channelId) throws SQLException, BusinessException {

    }

    public Folder getFolder() {
        return null;
    }

    public RtuType getRtuType() {
        return null;
    }

    public ChannelType getChannelType() {
        return null;
    }

    public TimeZone getDeviceTimeZone() {
        return timeZone != null ? timeZone : TimeZone.getDefault();
    }

    public TimeZone getTimeZone() {
        return timeZone != null ? timeZone : TimeZone.getDefault();
    }

    public List<Channel> getChannels() {
        return null;
    }

    public List<Channel> getNonAssignedChannels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OutboundConnectionTask createOutboundConnectionTask(OutboundConnectionTaskShadow shadow) throws BusinessException, SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OutboundConnectionTask getOutboundConnectionTask() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ScheduledComTask> getScheduledComTasks() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ScheduledComTask createScheduledComTask(ScheduledComTaskShadow shadow) throws BusinessException, SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<CommunicationScheduler> getCommunicationSchedulers() {
        return null;
    }

    public String getDeviceId() {
        return null;
    }

    public String getSerialNumber() {
        return null;
    }

    public String getEmailAddress() {
        return null;
    }

    public String getEncryptionPassword() {
        return null;
    }

    public int getFolderId() {
        return 0;
    }

    public String getIpAddress() {
        return null;
    }

    public Date getLastLogbook() {
        return null;
    }

    public Date getLastReading() {
        return null;
    }

    public String getMacAddress() {
        return null;
    }

    public String getNodeAddress() {
        return null;
    }

    public String getPhoneNumber() {
        return null;
    }

    public int getRtuTypeId() {
        return 0;
    }

    public void addEvent(Date date, int code, String message) throws BusinessException, SQLException {

    }

    public void addEvent(int code, String message) throws BusinessException, SQLException {

    }

    public void addEvent(RtuEventShadow rtuEventShadow) throws BusinessException, SQLException {

    }

    public List<RtuEvent> getEvents() {
        return null;
    }

    public List<RtuEvent> getEvents(Date from, Date to) {
        return null;
    }

    /**
     * Returns the rtu events that fulfill the conditions in the RtuEventFilter
     * events are returned in descending time order (youngest event first).
     *
     * @param filter RtuEventFilter specifying the conditions
     * @return a List of RtuEvent objects.
     */
    public List<RtuEvent> getEvents(RtuEventFilter filter) {
        return new ArrayList<RtuEvent>();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateIpAddress(String ipAddress) throws SQLException, BusinessException {

    }

    public void updateLastLogbookIfLater(Date execDate) throws SQLException, BusinessException {

    }

    public void updateLastLogbook(Date execDate) throws SQLException, BusinessException {

    }

    /**
     * Updates the total number of suspects register readings on this device
     *
     * @throws java.sql.SQLException
     */
    public void updateNumberOfSuspectReadings() throws SQLException {
        //TODO implement proper functionality.
    }

    /**
     * Updates the total number of suspect intervals on this device
     *
     * @throws java.sql.SQLException
     */
    public void updateNumberOfSuspectIntervals() throws SQLException {
        //TODO implement proper functionality.
    }

    /**
     * Updates the number of failed schedules on this device
     *
     * @throws java.sql.SQLException
     */
    public void updateNumberOfFailedSchedules() throws SQLException {
        //TODO implement proper functionality.
    }

    public void updateNumberOfActiveAlarms() throws SQLException, BusinessException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void store(ProfileData profileData) throws SQLException, BusinessException {
        store(profileData, true);
    }

    public void store(ProfileData profileData, boolean checkLastReading) throws SQLException, BusinessException {
        if (channel != null) {
            Date lastIntervalTime = getLastIntervalTime(profileData);
            if (checkLastReading) {
                channel.updateLastReadingIfLater(lastIntervalTime);
            } else {
                channel.updateLastReading(lastIntervalTime);
            }
        }
    }

    private Date getLastIntervalTime(ProfileData profileData) {
        profileData.sort();
        List<IntervalData> intervalDatas = profileData.getIntervalDatas();
        if ((intervalDatas == null) || intervalDatas.isEmpty()) {
            return null;
        } else {
            return intervalDatas.get(intervalDatas.size() - 1).getEndTime();
        }
    }

    public void store(MeterReadingData meterReadingData) throws SQLException, BusinessException {

    }

    public void store(MeterData meterData, boolean checkLastReading) throws SQLException, BusinessException {

    }

    public void store(MeterData meterData) throws SQLException, BusinessException {

    }

    public int getStoreVersion() {
        return 0;
    }

    public boolean changeRtuType(RtuType newType) throws BusinessException, SQLException {
        return false;
    }

    public ChangeRtuTypeTransaction createChangeRtuTypeTransaction(RtuType newType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SerialCommunicationSettings getCommunicationSettings() {
        return null;
    }

    public boolean getOverruleCommunicationSettings() {
        return false;
    }

    public String getPassword() {
        return null;
    }

    public String getModemInit() {
        return null;
    }

    public int getIntervalInSeconds() {
        return 0;
    }

    public boolean useDaylightTime() {
        return false;
    }

    public String getName() {
        return null;
    }

    public String getExternalName() {
        return null;
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

    public Channel getChannel(String name) {
        return null;
    }

    public Channel getChannel(int index) {
        return null;
    }

    public List<VirtualMeter> getReferencingVirtualMeters() {
        return null;
    }

    public List<Rtu> getReferencingRtus() {
        return null;
    }

    public List<Rtu> getDownstreamRtus() {
        return null;
    }

    public void removeEvent(RtuEvent event) throws BusinessException, SQLException {

    }

    public String getPostDialCommand() {
        return null;
    }

    public int getCalendarId() {
        return 0;
    }

    public int getCalendarOffset() {
        return 0;
    }

    public DialCalendar getDialCalendar() {
        return null;
    }

    public List<CommunicationScheduler> getPossibleFallbackSchedulers() {
        return null;
    }

    public List<CommunicationScheduler> getPossibleFallbackSchedulers(CommunicationScheduler scheduler) {
        return null;
    }

    public List<RtuRegister> getRegisters() {
        return null;
    }

    public RtuRegister getRegister(ObisCode code) {
        return null;
    }

    public Date getModDate() {
        return null;
    }

    public void startBulk() {

    }

    public void stopBulk() {

    }

    public boolean isPrototype() {
        return false;
    }

    public boolean willTypeChangeLoseRegisterData(RtuType newType) {
        return false;
    }

    public List<RtuMessage> getMessages() {
        return null;
    }

    public List<RtuMessage> getPendingMessages() {
        return null;
    }

    public List<RtuMessage> getSentMessages() {
        return null;
    }

    public Date getNextDialin() {
        return null;
    }

    public String getDialHomeId() {
        return null;
    }

    public boolean supportsMessaging() {
        return false;
    }

    public CommunicationProtocol getProtocol() {
        return null;
    }

    public boolean isGateway() {
        return false;
    }

    public Rtu getGateway() {
        return null;
    }

    public void updateGateway(Rtu gateway) throws SQLException, BusinessException {

    }

    public void updateInterval(int newIntervalInSeconds) throws SQLException, BusinessException {

    }

    public String getNetworkId() {
        return null;
    }

    public RtuMessage createMessage(RtuMessageShadow shadow) throws BusinessException, SQLException {
        return null;
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

    public CommunicationScheduler createCommunicationScheduler(CommunicationSchedulerShadow schedulerShadow) throws SQLException, BusinessException {
        return null;
    }

    @Override
    public ProtocolTypeUsage createProtocolTypeUsage(ProtocolTypeUsageShadow protocolTypeUsageShadow) throws BusinessException, SQLException {
        return null;
    }

    @Override
    public List<ProtocolTypeUsage> getProtocolTypeUsages() {
        return new ArrayList<ProtocolTypeUsage>(0);
    }

    public RelationTransaction getNewRelationTransaction() {
        return null;
    }

    public List<RtuRegister> getRegisters(ProductSpec productSpec) {
        return null;
    }

    public List<LoadProfile> getLoadProfiles() {
        return null;
    }

    public LoadProfile addLoadProfile(LoadProfileShadow loadProfileShadow) throws BusinessException, SQLException {
        return null;
    }

    public void removeLoadProfile(LoadProfile profile) throws BusinessException, SQLException {

    }

    public void updateLastReadingIfLater(Date execDate) throws SQLException, BusinessException {

    }

    public void updateLastReading(Date execDate) throws SQLException, BusinessException {

    }

    public Integer getRoleId() {
        return null;
    }

    public Role getRole() {
        return null;
    }

    public String getIntervalTable() {
        return null;
    }

    public Password getEIWebEncryptionPassword() {
        return null;
    }

    public Password getDevicePassword() {
        return null;
    }

    public boolean isAddingChannelsOrLoadProfilesAllowed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean execute(ChangeRtuTypeTransaction transaction) throws BusinessException, SQLException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNumberOfFailedSchedules() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNumberOfSuspectIntervals() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNumberOfSuspectReadings() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNumberOfAlarms() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDataCollectionStatusOk() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDataValidationStatusOk() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAlarmStatus() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @return the <Code>ServiceDeliveryPoint</Code> this device is installed at for the given date
     */
    public ServiceDeliveryPoint getServiceDeliveryPoint(final Date when) {
        return null;  //TODO implement proper functionality.
    }

    /**
     * Notification method to signal that something on the rtu's load profile(s) changed
     */
    public void loadProfilesChanged() throws BusinessException, SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
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

    public TypedProperties getProperties() {
        return null;
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

    public BusinessObjectType getBusinessObjectType() {
        return null;
    }

    public Object get(String key) {
        return null;
    }

    public Object get(String key, Date date) {
        return null;
    }

    public Object get(AttributeType attrib) {
        return null;
    }

    public Object get(AttributeType attrib, Date date) {
        return null;
    }

    public int getId() {
        return 0;
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

    public Dependent getOwner() {
        return null;
    }

    public DesktopDecorator asDesktopDecorator() {
        return null;
    }

    public boolean isAuthorized(UserAction action) {
        return false;
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     *
     * @return The Offline version of this object
     */
    public OfflineRtu goOffline() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
