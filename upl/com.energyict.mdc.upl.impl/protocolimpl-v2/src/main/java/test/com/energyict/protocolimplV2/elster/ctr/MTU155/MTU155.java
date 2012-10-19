package test.com.energyict.protocolimplV2.elster.ctr.MTU155;

import com.energyict.comserver.adapters.common.ComChannelInputStreamAdapter;
import com.energyict.comserver.adapters.common.ComChannelOutputStreamAdapter;
import com.energyict.comserver.exceptions.LegacyProtocolException;
import com.energyict.cpo.BigDecimalPropertySpec;
import com.energyict.cpo.BooleanPropertySpec;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.LogBook;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.ServerComChannel;
import com.energyict.mdc.shadow.messages.DeviceMessageShadow;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineRtu;
import com.energyict.mdw.offline.OfflineRtuRegister;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import test.com.energyict.mdc.tasks.CtrDeviceProtocolDialect;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author: sva
 * @since: 16/10/12 (10:10)
 */
public class MTU155 implements DeviceProtocol {

    public static final String DEBUG_PROPERTY_NAME = "Debug";
    public static final String CHANNEL_BACKLOG_PROPERTY_NAME = "ChannelBacklog";
    public static final String EXTRACT_INSTALLATION_DATE_PROPERTY_NAME = "ExtractInstallationDate";
    public static final String REMOVE_DAY_PROFILE_OFFSET_PROPERTY_NAME = "RemoveDayProfileOffset";

    /**
     * The offline rtu
     */
    private OfflineRtu offlineRtu;

    /**
     * Collection of all TypedProperties.
     */
    private MTU155Properties properties;

    /**
     * The Cache of the current RTU
     */
    private DeviceProtocolCache deviceCache;

    /**
     * The request factory, to be used to communicate with the MTU155
     */
    private RequestFactory requestFactory;

    /**
     * Legacy logger
     */
    private Logger protocolLogger;
    private TypedProperties allProperties;

    @Override
    public void init(OfflineRtu offlineDevice, ComChannel comChannel) {
        this.offlineRtu = offlineDevice;
        updateRequestFactory(comChannel);
    }

    @Override
    public void terminate() {
        // - not needed -
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> required = new ArrayList<PropertySpec>();
        return required;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optional = new ArrayList<PropertySpec>();
        optional.add(debugPropertySpec());
        optional.add(channelBacklogPropertySpec());
        optional.add(extractInstallationDatePropertySpec());
        optional.add(removeDayProfileOffsetPropertySpec());
        return optional;
    }

    private PropertySpec debugPropertySpec() {
        return new BooleanPropertySpec(DEBUG_PROPERTY_NAME);
    }

    private PropertySpec channelBacklogPropertySpec() {
        return new BigDecimalPropertySpec(CHANNEL_BACKLOG_PROPERTY_NAME);
    }

    private PropertySpec extractInstallationDatePropertySpec() {
        return new BigDecimalPropertySpec(EXTRACT_INSTALLATION_DATE_PROPERTY_NAME);
    }

    private PropertySpec removeDayProfileOffsetPropertySpec() {
        return new BigDecimalPropertySpec(REMOVE_DAY_PROFILE_OFFSET_PROPERTY_NAME);
    }

    @Override
    public void logOn() {
        // not needed
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        if (getProperties().isSendEndOfSession()) {
            getRequestFactory().sendEndOfSession();
        }
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    /**
     *  Read out the serial number of the device
     *  Note: This reads out the serial number of the Convertor
     *  The serial numbers of MTU155 and attached Gas device are not read/checked!
     **/
    public String getSerialNumber() {
        return getRequestFactory().getMeterInfo().getConverterSerialNumber();
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceCache = deviceProtocolCache;

        // Remark: for CTR protocol, the cache object is not used and is always empty.
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceCache;
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getRequestFactory().getMeterInfo().setTime(timeToSet);
        } catch (CTRException e) {
            throw new LegacyProtocolException(e);
        }
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;  //To change body of implemented methods use File | Settings | File Templates. //ToDo
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;  //To change body of implemented methods use File | Settings | File Templates. //ToDo
    }

    @Override
    public Date getTime() {
        try {
            return getRequestFactory().getMeterInfo().getTime();
        } catch (CTRException e) {
            throw new LegacyProtocolException(e);
        }
    }

    @Override
    public List<CollectedLogBook> getMeterEvents(List<LogBook> logBooks) {  //ToDo
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedStandardMessages() {
        return null;  //To change body of implemented methods use File | Settings | File Templates. //ToDo
    }

    @Override
    public CollectedMessage executePendingMessages(List<DeviceMessageShadow> pendingMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates. //ToDo
    }

    @Override
    public CollectedData updateSentMessages(List<DeviceMessageShadow> sentMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates. //ToDo
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<DeviceProtocolDialect>(1);
        dialects.add(new CtrDeviceProtocolDialect());
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        if (this.allProperties != null) {
            this.allProperties.setAllProperties(dialectProperties); // this will add the dialectProperties to the deviceProperties
        } else {
            this.allProperties = dialectProperties;
        }
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRtuRegister> rtuRegisters) {
        return null;  //To change body of implemented methods use File | Settings | File Templates. //ToDo
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;  //To change body of implemented methods use File | Settings | File Templates. //ToDo
    }

    @Override
    public String getVersion() {
        // return this.meterProtocol.getVersion();
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        if (this.allProperties != null) {
            this.allProperties.setAllProperties(properties); // this will add the properties to the existing properties
        } else {
            this.allProperties = properties;
        }
    }

    public MTU155Properties getMTU155Properties() {
        if (this.properties == null) {
            this.properties = new MTU155Properties(allProperties);
        }
        return this.properties;
    }

    public MTU155Properties getProperties() {
        return properties;
    }

    private void updateRequestFactory(ComChannel comChannel) {
        this.requestFactory = new GprsRequestFactory(new ComChannelInputStreamAdapter((ServerComChannel) comChannel),
                new ComChannelOutputStreamAdapter((ServerComChannel) comChannel),
                getLogger(),
                getProperties(),
                getTimeZone());
    }

    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public Logger getLogger() {       //ToDo: this is a temporary solution - should be removed again & substituted by a proper replacement
        if (protocolLogger == null) {
            protocolLogger = Logger.getLogger(this.getClass().getName());
        }
        return protocolLogger;
    }

    public TimeZone getTimeZone() {
        return offlineRtu.getTimeZone();
    }
}
