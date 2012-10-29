package test.com.energyict.protocolimplV2.edmi.mk10;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.LogBook;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.shadow.messages.DeviceMessageShadow;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineRtu;
import com.energyict.mdw.offline.OfflineRtuRegister;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;

import java.util.Date;
import java.util.List;

/**
 * @author: sva
 * @since: 29/10/12 (9:58)
 */
public class MK10 implements DeviceProtocol {
    @Override
    public void init(OfflineRtu offlineDevice, ComChannel comChannel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void terminate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void logOn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void daisyChainedLogOn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void logOff() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void daisyChainedLogOff() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSerialNumber() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTime(Date timeToSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Date getTime() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedLogBook> getMeterEvents(List<LogBook> logBooks) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceMessageSpec> getSupportedStandardMessages() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CollectedMessage executePendingMessages(List<DeviceMessageShadow> pendingMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CollectedData updateSentMessages(List<DeviceMessageShadow> sentMessages) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRtuRegister> rtuRegisters) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addProperties(TypedProperties properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
