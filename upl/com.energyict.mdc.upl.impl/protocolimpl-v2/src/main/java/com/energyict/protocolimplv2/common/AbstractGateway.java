package com.energyict.protocolimplv2.common;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolcommon.exceptions.CodingException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        throw CodingException.unsupportedMethod(this.getClass(), "getSerialNumber");
    }

    @Override
    public Date getTime() {
        throw CodingException.unsupportedMethod(this.getClass(), "getTime");
    }

    @Override
    public void setTime(Date timeToSet) {
        throw CodingException.unsupportedMethod(this.getClass(), "setTime");
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw CodingException.unsupportedMethod(this.getClass(), "fetchLoadProfileConfiguration");
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw CodingException.unsupportedMethod(this.getClass(), "getLoadProfileData");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw CodingException.unsupportedMethod(this.getClass(), "getLogBookData");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        throw CodingException.unsupportedMethod(this.getClass(), "format");
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw CodingException.unsupportedMethod(this.getClass(), "readRegisters");
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw CodingException.unsupportedMethod(this.getClass(), "getDeviceTopology");
    }
}