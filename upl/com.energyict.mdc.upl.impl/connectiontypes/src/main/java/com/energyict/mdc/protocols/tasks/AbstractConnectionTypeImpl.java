package com.energyict.mdc.protocols.tasks;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Serves as the root for components that intend to implement
 * the ConnectionType interface.
 * Mostly provides code reuse opportunities for storing properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (15:19)
 */
@XmlRootElement
@XmlSeeAlso({ConnectionTypeImpl.class})
public abstract class AbstractConnectionTypeImpl implements ConnectionType {

    private TypedProperties properties = TypedProperties.empty();

    public AbstractConnectionTypeImpl() {
        super();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.properties = TypedProperties.copyOf(properties);
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    protected Object getProperty(String propertyName) {
        return this.getAllProperties().getProperty(propertyName);
    }

    protected Object getProperty(String propertyName, Object defaultValue) {
        return this.getAllProperties().getProperty(propertyName, defaultValue);
    }

    protected void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // Prepare the comChannel for disconnect
        comChannel.prepareForDisConnect();

        // Do the actual disconnect
        // Note: For most connectionTypes actual disconnect is not needed, so do nothing.
        // Should be overridden in those connectionTypes requiring a disconnect.
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.getClass() == obj.getClass();
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }
}