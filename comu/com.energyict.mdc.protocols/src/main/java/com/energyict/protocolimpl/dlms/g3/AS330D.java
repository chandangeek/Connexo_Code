package com.energyict.protocolimpl.dlms.g3;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.base.RTUCache;
import com.energyict.protocolimpl.dlms.common.AbstractDlmsSessionProtocol;
import com.energyict.protocolimpl.dlms.g3.events.G3Events;
import com.energyict.protocolimpl.dlms.g3.messaging.G3Messaging;
import com.energyict.protocolimpl.dlms.g3.profile.G3Profile;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 10:32
 */
public class AS330D extends AbstractDlmsSessionProtocol {

    @Override
    public String getProtocolDescription() {
        return "Elster AS330D DLMS (G3 Linky)";
    }

    private static final String TIMEOUT = "timeout";

    private final OrmClient ormClient;
    private final CalendarService calendarService;
    protected G3Properties properties;

    private G3Clock clock;
    private G3DeviceInfo info;
    private G3RegisterMapper registerMapper;
    private G3Profile profile;
    private G3Events events;
    private G3Messaging messaging;
    private G3Cache cache = new G3Cache();

    @Inject
    public AS330D(PropertySpecService propertySpecService, CalendarService calendarService, OrmClient ormClient) {
        super(propertySpecService);
        this.calendarService = calendarService;
        this.ormClient = ormClient;
    }

    protected CalendarService getCalendarService() {
        return calendarService;
    }

    @Override
    protected G3Properties getProperties() {
        if (properties == null) {
            properties = new G3Properties();
        }
        return properties;
    }

    public String getProtocolVersion() {
        return "$Date: 2012-03-28 15:35:22 +0200 (Wed, 28 Mar 2012) $";
    }

    @Override
    protected void doInit() {
        this.clock = new G3Clock(getSession());
        this.info = new G3DeviceInfo(getSession());
        this.registerMapper = new G3RegisterMapper(this);
        this.profile = new G3Profile(getSession(), getProperties().getProfileType(), cache);
        this.events = new G3Events(getSession());
        initMessaging();
    }

    @Override
    public void connect() throws IOException {
        getSession().init();
        try {
            getSession().getDLMSConnection().connectMAC();
        } catch (DLMSConnectionException e) {
            throw new NestedIOException(e, "Exception occurred while connection DLMSStream");
        }
        connectWithRetries();
        validateSerialNumber();
    }

    private void connectWithRetries() throws IOException {
        int tries = 0;
        while (true) {
            IOException exception;
            try {
                getSession().getDLMSConnection().setRetries(0);   //AARQ retries are handled here
                getSession().createAssociation(getProperties().getAARQTimeout());
                return;
            } catch (DataAccessResultException e) {
                throw e;
            } catch (IOException e) {
                exception = e;
            } finally {
                getSession().getDLMSConnection().setRetries(getProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getProperties().getAARQRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getProperties().getAARQRetries() + 1) + "] tries.");
                throw new NestedIOException(exception);
            } else {
                if ((exception.getMessage() != null) && exception.getMessage().toLowerCase().contains(TIMEOUT)) {
                    if (getLogger().isLoggable(Level.INFO)) {
                        getLogger().info("Unable to establish association after [" + tries + "/" + (getProperties().getAARQRetries() + 1) + "] tries, due to timeout. Retrying.");
                    }
            } else {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().info("Unable to establish association after [" + tries + "/" + (getProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                }
                try {
                    getSession().getAso().releaseAssociation();
                    } catch (DLMSConnectionException e) {
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
                }
                getSession().getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
            }
        }
    }

    protected void initMessaging() {
        setMessaging(new G3Messaging(getSession(), getProperties(), this.calendarService));
    }

    @Override
    protected String readSerialNumber() throws IOException {
        return this.info.getSerialNumber();
    }

    public String getFirmwareVersion() throws IOException {
        return this.info.getFirmwareVersions();
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (to == null) {       //Avoid unspecified date in the range descriptor for the SagemCom meter.
            to = new Date();
        }
        final ProfileData profileData = this.profile.getProfileData(from, to);
        if (includeEvents) {
            profileData.setMeterEvents(this.events.getMeterEvents(from, to));
        } else {
            profileData.setMeterEvents(new ArrayList<>(0));
        }
        profileData.sort();
        return profileData;
    }

    public int getNumberOfChannels() throws IOException {
        return this.profile.getNumberOfChannels();
    }

    public int getProfileInterval() throws IOException {
        return getProperties().getProfileInterval();   //Skip this check. This can be fixed though, by making this protocol a smartmeter protocol
    }

    public Date getTime() throws IOException {
        return this.clock.getTime();
    }

    public void setTime() throws IOException {
        this.clock.setTime();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return this.registerMapper.readRegister(obisCode);
        } catch (DataAccessResultException e) {
            throw new NoSuchRegisterException("Error while reading out register " + obisCode + ": " + e.getMessage());
        }
    }

    public G3Messaging getMessaging() {
        if (this.messaging == null) {
            this.initMessaging();
        }
        return messaging;
    }

    protected void setMessaging(G3Messaging messaging) {
        this.messaging = messaging;
    }

    @Override
    public String writeValue(MessageValue value) {
        return getMessaging().writeValue(value);
    }

    @Override
    public String writeMessage(Message msg) {
        return getMessaging().writeMessage(msg);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getMessaging().applyMessages(messageEntries);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getMessaging().writeTag(tag);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessaging().queryMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return getMessaging().getMessageCategories();
    }

    @Override
    public Object getCache() {
        this.cache.setFrameCounter(getSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
        return this.cache;
    }

    @Override
    public void setCache(Object cache) {
        if ((cache != null) && (cache instanceof G3Cache)) {
            this.cache = (G3Cache) cache;
            this.getProperties().getSecurityProvider().setInitialFrameCounter(this.cache.getFrameCounter());    //Get this from the last session
        }
    }

    @Override
    public Object fetchCache(int rtuid) throws SQLException {
        if (rtuid != 0) {
            RTUCache rtuCache = new RTUCache(rtuid, ormClient);
            try {
                return rtuCache.getCacheObject();
            } catch (NotFoundException e) {
                return new G3Cache();
            } catch (IOException e) {
                return new G3Cache();
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    @Override
    public void updateCache(final int rtuid, final Object cacheObject) throws SQLException {
        if (rtuid != 0) {
            try {
                this.ormClient.execute(new VoidTransaction() {
                    @Override
                    protected void doPerform() {
                        try {
                            G3Cache dc = (G3Cache) cacheObject;
                            new RTUCache(rtuid, ormClient).setBlob(dc);
                        }
                        catch (SQLException e) {
                            throw new LocalSQLException(e);
                        }
                    }
                });
            }
            catch (LocalSQLException e) {
                throw e.getCause();
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    private class LocalSQLException extends RuntimeException {
        private LocalSQLException(SQLException cause) {
            super(cause);
        }

        @Override
        public SQLException getCause() {
            return (SQLException) super.getCause();
        }
    }

}