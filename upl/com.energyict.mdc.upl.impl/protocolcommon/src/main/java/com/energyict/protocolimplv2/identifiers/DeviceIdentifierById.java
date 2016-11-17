package com.energyict.protocolimplv2.identifiers;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:31
 * Author: khe
 */

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;

import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s database identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
@XmlRootElement
public class DeviceIdentifierById implements DeviceIdentifier {

    private int id;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DeviceIdentifierById() {
    }

    public DeviceIdentifierById(int id) {
        super();
        this.id = id;
    }

    public DeviceIdentifierById(long id) {
        this((int) id);
    }

    // used for reflection
    public DeviceIdentifierById(String id) {
        super();
        this.id = Integer.parseInt(id);
    }

    @Override
    public Device findDevice () {
        Device device = getDeviceFactory().find(this.id);
        if (device == null) {
            throw NotFoundException.notFound(Device.class, this.toString());
        }
        else {
            return device;
        }
    }

    @Override
    public String toString () {
        return "id " + this.id;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeviceIdentifierById)) {
            return false;
        }
        DeviceIdentifierById identifier = (DeviceIdentifierById) obj;
        if (identifier.getId() != this.getId()) {
            return false;
        }
        return true;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @Override
    public String getIdentifier() {
        return Integer.toString(id);
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.DataBaseId;
    }

    private DeviceFactory getDeviceFactory() {
        return DeviceFactoryProvider.instance.get().getDeviceFactory();
    }
}
