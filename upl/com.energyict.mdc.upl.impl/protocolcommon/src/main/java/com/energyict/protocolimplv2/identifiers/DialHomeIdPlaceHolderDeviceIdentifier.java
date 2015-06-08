package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierType;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocolimplv2.MdcManager;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s Call Home ID to uniquely identify it.
 *
 * @author: khe
 * @since: 25/09/14 (10:00)
 */
@XmlRootElement
public class DialHomeIdPlaceHolderDeviceIdentifier implements ServerDeviceIdentifier {

    public static final PropertySpec CALL_HOME_ID_PROPERTY_SPEC = PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);

    private final CallHomeIdPlaceHolder callHomeIdPlaceHolder;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DialHomeIdPlaceHolderDeviceIdentifier() {
        callHomeIdPlaceHolder = new CallHomeIdPlaceHolder();
    }

    public DialHomeIdPlaceHolderDeviceIdentifier(CallHomeIdPlaceHolder callHomeIdPlaceHolder) {
        super();
        this.callHomeIdPlaceHolder = callHomeIdPlaceHolder;
    }

    @Override
    public Device findDevice() {
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with callHomeID " + this.callHomeIdPlaceHolder.getSerialNumber() + " not found");
            } else {
                if (this.allDevices.size() > 1) {
                    throw MdcManager.getComServerExceptionFactory().createDuplicateException(Device.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        this.allDevices = getDeviceFactory().findByNotInheritedProtocolProperty(CALL_HOME_ID_PROPERTY_SPEC, callHomeIdPlaceHolder.getSerialNumber());
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeIdPlaceHolder.getSerialNumber();
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public CallHomeIdPlaceHolder getCallHomeIdPlaceHolder() {
        return callHomeIdPlaceHolder;
    }

    @Override
    public String getIdentifier() {
        return callHomeIdPlaceHolder.getSerialNumber();
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.CallHomeId;
    }

    @Override
    public List<OfflineDevice> getAllDevices() {
        if (this.allDevices == null) {
            fetchAllDevices();
        }
        List<OfflineDevice> allOfflineDevices = new ArrayList<>();
        OfflineDeviceContext offlineDeviceContext = new DeviceOfflineFlags();
        for (Device deviceToGoOffline : this.allDevices) {
            allOfflineDevices.add(deviceToGoOffline.goOffline(offlineDeviceContext));
        }
        return allOfflineDevices;
    }

    private DeviceFactory getDeviceFactory() {
        return DeviceFactoryProvider.instance.get().getDeviceFactory();
    }
}