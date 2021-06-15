package com.energyict.mdc.upl;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.journal.ProtocolLoggingSupport;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.security.DeviceSecuritySupport;
import com.energyict.mdc.upl.tasks.support.DeviceBasicSupport;
import com.energyict.mdc.upl.tasks.support.DeviceClockSupport;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.upl.tasks.support.DeviceProtocolConnectionFunctionSupport;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.mdc.upl.tasks.support.DeviceStatusInformationSupport;
import com.energyict.mdc.upl.tasks.support.DeviceTopologySupport;

import aQute.bnd.annotation.ConsumerType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Defines an Interface between the Data Collection System and a Device.
 * The interface can both be used at operational time and at configuration time.
 * <p>
 * Note that this is the current and preferred interface and that
 * {@link MeterProtocol} and {@link SmartMeterProtocol} are
 * legacy interfaces and are in de-facto deprecated.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (10:04)
 */
@ConsumerType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface DeviceProtocol
        extends HasDynamicProperties, DeviceProtocolDialectSupport,
        DeviceBasicSupport, DeviceAccessSupport, DeviceClockSupport,
        DeviceLoadProfileSupport, DeviceRegisterSupport, DeviceLogBookSupport,
        DeviceStatusInformationSupport, DeviceMessageSupport, DeviceSecuritySupport,
        ProtocolLoggingSupport,
        DeviceProtocolConnectionFunctionSupport, DeviceTopologySupport, DeviceCachingSupport, DeviceDescriptionSupport, ConnectionTypeSupport, FirmwareSignatureCheckSupport {

    /**
     * Models common properties that can be marked required or optional
     * by the actual DeviceProtocol implementation classes.
     */
    enum Property {
        RETRIES("Retries"),
        TIMEOUT("Timeout"),
        MUST_KEEP_LISTENING("MustKeepListening");

        private final String name;

        Property(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * Gets the implementation version.
     *
     * @return The version
     */
    String getVersion();

    /**
     * Initializes the DeviceProtocol, after the physical connection has been
     * created and before the protocol <i>logOn</i> occurs.
     * <p>
     * Implementers should save the arguments for future use.
     *
     * @param offlineDevice contains the complete definition/configuration of a Device
     * @param comChannel the used ComChannel where all read/write actions are going to be performed
     */
    void init(OfflineDevice offlineDevice, ComChannel comChannel);

    /**
     * This method is called by the collection software before the physical disconnect,
     * and after the protocol <i>logOff</i>. This can be used to free resources that
     * cannot be freed in the disconnect() method.
     */
    void terminate();

    /**
     * Gets the {@link DeviceProtocolCapabilities}.
     *
     * @return The DeviceProtocolCapabilities
     */
    List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities();

    DeviceFunction getDeviceFunction();

    ManufacturerInformation getManufacturerInformation();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public default String getXmlType() {
        return this.getClass().getName();
    }

    public default void setXmlType(String ignore) {
    }
}
