package com.energyict.protocolimplv2.dlms.landisAndGyr;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.protocolimplv2.HdlcDlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.Manufacturer;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.landisAndGyr.logbooks.ZMYLogBookFactory;
import com.energyict.protocolimplv2.dlms.landisAndGyr.messages.ZMYMessageExecutor;
import com.energyict.protocolimplv2.dlms.landisAndGyr.messages.ZMYMessaging;
import com.energyict.protocolimplv2.dlms.landisAndGyr.profiles.ZMYLoadProfileBuilder;
import com.energyict.protocolimplv2.dlms.landisAndGyr.properties.ZMYDlmsConfigurationSupport;
import com.energyict.protocolimplv2.dlms.landisAndGyr.properties.ZMYProperties;
import com.energyict.protocolimplv2.dlms.landisAndGyr.registers.ZMYRegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.energyict.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;

public class ZMY extends AbstractDlmsProtocol implements SerialNumberSupport {

	private NlsService nlsService;
	private Converter converter;

	private HasDynamicProperties zmyConfSupport     = new ZMYDlmsConfigurationSupport(getPropertySpecService());
	private DlmsProperties zmyProperties            = new ZMYProperties();
	private DeviceRegisterSupport registerFactory   = new ZMYRegisterFactory(this);
	private LoadProfileBuilder loadProfileBuilder   = new ZMYLoadProfileBuilder(this);
	private DeviceLogBookSupport logBookFactory     = new ZMYLogBookFactory(this);
	private ZMYMessaging zmyMessaging               = new ZMYMessaging(new ZMYMessageExecutor(this), converter, nlsService, this.getPropertySpecService());

	public ZMY(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter) {
		super(propertySpecService, collectedDataFactory, issueFactory);
		this.nlsService = nlsService;
		this.converter = converter;
	}

	@Override
	public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
		this.offlineDevice = offlineDevice;
		getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
		setDlmsSession(new HdlcDlmsSession(comChannel, getDlmsSessionProperties()));
	}

	@Override
	public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
		return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
	}

	@Override
	public DeviceFunction getDeviceFunction() {
		return DeviceFunction.NONE;
	}

	@Override
	public ManufacturerInformation getManufacturerInformation() {
		return new ManufacturerInformation(Manufacturer.LandisAndGyr);
	}

	@Override
	public List<? extends ConnectionType> getSupportedConnectionTypes() {
		return Arrays.asList(
				(ConnectionType) new OutboundTcpIpConnectionType());
	}

	@Override
	public String getProtocolDescription() {
		return "Landis+Gyr E570 DLMS";
	}

	@Override
	public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
		return loadProfileBuilder.fetchLoadProfileConfiguration(loadProfilesToRead);
	}

	@Override
	public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
		return loadProfileBuilder.getLoadProfileData(loadProfiles);
	}

	@Override
	public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
		return logBookFactory.getLogBookData(logBooks);
	}

	@Override
	public List<DeviceMessageSpec> getSupportedMessages() {
		return zmyMessaging.getSupportedMessages();
	}

	@Override
	public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
		return zmyMessaging.executePendingMessages(pendingMessages);
	}

	@Override
	public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
		return zmyMessaging.updateSentMessages(sentMessages);
	}

	@Override
	public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
		return Optional.empty();
	}

	@Override
	public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
		return zmyMessaging.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
	}

	@Override
	public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
		return Arrays.asList(
				new SerialDeviceProtocolDialect(this.getPropertySpecService(), nlsService),
				new TcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService));
	}

	@Override
	public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
		return registerFactory.readRegisters(registers);
	}

	@Override
	public String getVersion() {
		return "$Date: 2022-01-27 13:00:00 +0300 (Thu, 27 Jan 2022)$";
	}

	@Override
	protected HasDynamicProperties getDlmsConfigurationSupport() {
		return zmyConfSupport;
	}

	@Override
	public DlmsProperties getDlmsSessionProperties() {
		return zmyProperties;
	}

	/**
	 * Method to check whether the cache needs to be read out or not, if so the read will be forced
	 */
	@Override
	protected void checkCacheObjects() {
		if (getDeviceCache() == null) {
			setDeviceCache(new DLMSCache());
		}
		DLMSCache dlmsCache = getDeviceCache();

		if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().getProperties().getTypedProperty(READCACHE_PROPERTY, false)) {
			readObjectList();
			dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
		} else {
			getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
		}
	}
}
