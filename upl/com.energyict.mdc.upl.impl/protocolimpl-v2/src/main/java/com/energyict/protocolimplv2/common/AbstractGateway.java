package com.energyict.protocolimplv2.common;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Super class for every gateway protocol that implements the DeviceProtocol interface
 * <p/>
 * Copyrights EnergyICT
 * Date: 29/05/13
 * Time: 17:03
 * Author: khe
 */
public abstract class AbstractGateway implements DeviceProtocol {

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        // Do nothing
    }

    @Override
    public void logOn() {
        //Do nothing
    }

    @Override
    public void daisyChainedLogOn() {
        //Do nothing
    }

    @Override
    public void logOff() {
        //Do nothing
    }

    @Override
    public void daisyChainedLogOff() {
        //Do nothing
    }

    @Override
    public void terminate() {
        //Do nothing
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        List<DeviceProtocolCapabilities> capabilities = new ArrayList<>();
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_MASTER);
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_SESSION);
        return capabilities;
    }

    @Override
    public String getSerialNumber() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getSerialNumber");
    }

    @Override
    public Date getTime() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getTime");
    }

    @Override
    public void setTime(Date timeToSet) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "setTime");
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "fetchLoadProfileConfiguration");
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getLoadProfileData");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getLogBookData");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "format");
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "readRegisters");
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getDeviceTopology");
    }
}