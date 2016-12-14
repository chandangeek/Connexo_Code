package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.protocol.exceptions.identifier.DuplicateException;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s Call Home ID to uniquely identify it.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
@XmlRootElement
public class DialHomeIdDeviceIdentifier implements ServerDeviceIdentifier {

    public static final PropertySpec CALL_HOME_ID_PROPERTY_SPEC = PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);

    private final String callHomeID;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DialHomeIdDeviceIdentifier() {
        callHomeID = "";
    }

    public DialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
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
        this.allDevices = getDeviceFactory().findByNotInheritedProtocolProperty(CALL_HOME_ID_PROPERTY_SPEC, callHomeID);
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeID;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public String getCallHomeID() {
        return callHomeID;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "CallHomeId";
        }

        @Override
        public Object getValue(String role) {
            if ("callHomeId".equals(role)) {
                return callHomeID;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}