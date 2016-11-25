package com.energyict.protocolimplv2.identifiers;

import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierType;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocol.exceptions.identifier.DuplicateException;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s System title to uniquely identify it.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:06
 */
@XmlRootElement
public class DeviceIdentifierBySystemTitle implements ServerDeviceIdentifier {
    public static final String DEVICE_SYSTEM_TITLE = "DeviceSystemTitle";
    public static final PropertySpec SYSTEM_TITLE_PROPERTY_SPEC = PropertySpecFactory.stringPropertySpec(DEVICE_SYSTEM_TITLE);

    private final String systemTitle;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DeviceIdentifierBySystemTitle() {
        systemTitle = "";
    }

    public DeviceIdentifierBySystemTitle(String systemTitle) {
        super();
        this.systemTitle = systemTitle;
    }

    @Override
    public Device findDevice() {
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw NotFoundException.notFound(Device.class, this.toString());
            } else {
                if (this.allDevices.size() > 1) {
                    throw DuplicateException.duplicateFoundFor(Device.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        this.allDevices = getDeviceFactory().findByNotInheritedProtocolProperty(SYSTEM_TITLE_PROPERTY_SPEC, systemTitle);
    }

    @Override
    public String toString() {
        return "device with system title " + this.systemTitle;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public String getSystemTitle() {
        return systemTitle;
    }

    @Override
    public String getIdentifier() {
        return systemTitle;
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.SystemTitle;
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
