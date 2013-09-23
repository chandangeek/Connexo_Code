package com.energyict.protocolimplv2.nta.abstractnta;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.common.AbstractDlmsProtocol;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.tasks.support.*;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23Properties;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;
import com.energyict.smartmeterprotocolimpl.common.MasterMeter;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMeterInfo;

import java.io.IOException;
import java.util.List;

/**
 * @author: sva
 * @since: 31/10/12 (10:32)
 */
public abstract class AbstractNtaProtocol extends AbstractDlmsProtocol implements MasterMeter, SimpleMeter {  //ToDo: implement the WakeUpProtocolSupport interface?

    private static final int ObisCodeBFieldIndex = 1;

    private static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode monthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");

    /**
     * The used {@link ComposedMeterInfo}
     */
    private ComposedMeterInfo meterInfo;

    /**
     * The used {@link MeterTopology}
     */
    private MeterTopology meterTopology;

    /**
     * The <code>Properties</code> used for this protocol
     */
    private Dsmr23Properties dsmr23Properties;

    /**
     * Get the AXDRDateTimeDeviationType for this DeviceType
     *
     * @return the requested type
     */
    public abstract AXDRDateTimeDeviationType getDateTimeDeviationType();

    /**
     * Get the used RegisterFactory
     *
     * @return the register factory
     */
    public abstract DeviceRegisterSupport getRegisterFactory();

    /**
     * Get the used LoadProfileBuilder
     *
     * @return the loadProfile builder
     */
    public abstract DeviceLoadProfileSupport getLoadProfileBuilder();

    /**
     * Get the used DeviceLogBookFactory
     *
     * @return the device logBook factory
     */
    public abstract DeviceLogBookSupport getDeviceLogBookFactory();

    /**
     * Provide the correct DeviceMessageSupport.
     * Do NOT return null. If your implementation doesn't use message,
     * then create an implementation which doesn't return any messages.
     *
     * @return the used DeviceMessageSupport
     */
    public abstract DeviceMessageSupport getMessageProtocol();

    @Override
    protected void initAfterConnect() {
        try {
            searchForSlaveDevices();
        } catch (ConnectionException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
    }

    @Override
    public void searchForSlaveDevices() throws ConnectionException {
        getMeterTopology().searchForSlaveDevices();
    }

    public MeterTopology getMeterTopology() {
          if (this.meterTopology == null) {
              this.meterTopology = new MeterTopology(this);
          }
          return meterTopology;
      }

    /**
     * 'Lazy' getter for the {@link #meterInfo}
     *
     * @return the {@link #meterInfo}
     */
    public ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(), supportsBulkRequests());
        }
        return meterInfo;
    }

    @Override
    public String getSerialNumber() {
        try {
            return getMeterInfo().getSerialNr();
        } catch (IOException e) {
            String message = "Could not retrieve the serialnumber of the meter. " + e.getMessage();
            getLogger().finest(message);
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
    }

    @Override
    protected String getFirmwareVersion() {
        try {
            return getMeterInfo().getFirmwareVersion();
        } catch (IOException e) {
            String message = "Could not fetch the firmwareVersion. " + e.getMessage();
            getLogger().finest(message);
            return "Unknown version";
        }
    }

    @Override
    public int requestConfigurationChanges() throws IOException {
        return getMeterInfo().getConfigurationChanges();
    }

    @Override
    public int getPhysicalAddress() {
        return 0;   // the 'Master' has physicalAddress 0
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public ObisCode getPhysicalAddressCorrectedObisCode(final ObisCode obisCode, final String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(dailyObisCode) || obisCode.equalsIgnoreBChannel(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }
        if ((address == 0 && obisCode.getB() != -1) || obisCode.getB() == 128) { // then don't correct the obisCode
            return obisCode;
        }
        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
        }
        return null;
    }

    /**
     * Return the serialNumber of the meter which corresponds with the B-Field of the given ObisCode
     *
     * @param obisCode the ObisCode
     * @return the serialNumber
     */
    public String getSerialNumberFromCorrectObisCode(ObisCode obisCode) {
        return getMeterTopology().getSerialNumber(obisCode);
    }

    /**
     * Search for the physicalAddress of the meter with the given serialNumber
     *
     * @param serialNumber the serialNumber of the meter
     * @return the requested physical address or -1 when it could not be found
     */
    public int getPhysicalAddressFromSerialNumber(final String serialNumber) {
        return getMeterTopology().getPhysicalAddress(serialNumber);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getDeviceLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> rtuRegisters) {
        return getRegisterFactory().readRegisters(rtuRegisters);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessageProtocol().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageProtocol().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageProtocol().updateSentMessages(sentMessages);
    }

    public DlmsProtocolProperties getProtocolProperties() {
        if (this.dsmr23Properties == null) {
            this.dsmr23Properties = new Dsmr23Properties();
        }
        return this.dsmr23Properties;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        try {
            return getMeterTopology().getDeviceTopology();
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
    }
}
