package com.energyict.protocolimpl.dlms.g3;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.RTUCache;
import com.energyict.protocolimpl.dlms.common.AbstractDlmsSessionProtocol;
import com.energyict.protocolimpl.dlms.g3.events.G3Events;
import com.energyict.protocolimpl.dlms.g3.messaging.G3Messaging;
import com.energyict.protocolimpl.dlms.g3.profile.G3Profile;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
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
public class AS330D extends AbstractDlmsSessionProtocol implements SerialNumberSupport, CachingProtocol {

    private static final String TIMEOUT = "timeout";
    protected G3Properties properties;

    private G3Clock clock;
    private G3DeviceInfo info;
    private G3RegisterMapper registerMapper;
    private G3Profile profile;
    private G3Events events;
    private G3Messaging messaging;
    private G3Cache cache = new G3Cache();

    @Override
    protected G3Properties getProperties() {
        if (properties == null) {
            properties = new G3Properties();
        }
        return properties;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:12 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    protected void doInit() {
        this.clock = new G3Clock(getSession());
        this.info = new G3DeviceInfo(getSession().getCosemObjectFactory());
        this.registerMapper = new G3RegisterMapper(getSession().getCosemObjectFactory(), getSession().getTimeZone(), getSession().getLogger());
        this.profile = new G3Profile(getSession(), getProperties().getProfileType(), cache, "");
        this.events = new G3Events(getSession());
        initMessaging();
    }

    @Override
    public String getSerialNumber() {
        try {
            return readSerialNumber().trim();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getProperties().getRetries() + 1);
        }
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
        setMessaging(new G3Messaging(getSession(), getProperties(), calendarFinder));
    }

    @Override
    protected String readSerialNumber() throws IOException {
        return this.info.getSerialNumber();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return this.info.getFirmwareVersions();
    }

    @Override
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

    @Override
    public int getNumberOfChannels() throws IOException {
        return this.profile.getNumberOfChannels();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getProperties().getProfileInterval();   //Skip this check. This can be fixed though, by making this protocol a smartmeter protocol
    }

    @Override
    public Date getTime() throws IOException {
        return this.clock.getTime();
    }

    @Override
    public void setTime() throws IOException {
        this.clock.setTime();
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return this.registerMapper.readRegister(obisCode);
        } catch (DataAccessResultException e) {
            throw new NoSuchRegisterException("Error while reading out register " + obisCode + ": " + e.getMessage());
        }
    }

    public G3Messaging getMessaging() {
        if (this.messaging == null) {
            this.messaging = new G3Messaging(getSession(), getProperties(), calendarFinder);
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
    public Serializable getCache() {
        this.cache.setFrameCounter(getSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
        return this.cache;
    }

    @Override
    public void setCache(Serializable cache) {
        if ((cache != null) && (cache instanceof G3Cache)) {
            this.cache = (G3Cache) cache;
            this.getProperties().getSecurityProvider().setInitialFrameCounter(this.cache.getFrameCounter());    //Get this from the last session
        }
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) throws SQLException, ProtocolCacheFetchException {
        if (deviceId != 0) {
            RTUCache rtuCache = new RTUCache(deviceId);
            try {
                return rtuCache.getCacheObject(connection);
            } catch (IOException e) {
                return new G3Cache();
            }
        } else {
            throw new IllegalArgumentException("invalid device identifier!");
        }
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
        if (deviceId != 0) {
            G3Cache dc = (G3Cache) cacheObject;
            new RTUCache(deviceId).setBlob(dc, connection);
        } else {
            throw new IllegalArgumentException("invalid device identifier!");
        }
    }

}