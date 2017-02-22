package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.map.HashedMap;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:38
 */
@XmlRootElement
public class Beacon3100ProtocolConfiguration {
	
	/** The {@link Set} of {@link Properties} we don't sync. */
	private static final Set<String> IGNORED_PROPERTY_NAMES = new HashSet<>();
	
	/** Contains overrides. */
	private static final Map<String, Object> OVERRIDES = new HashedMap();

	/** Setup the ignores property names set. */
	static {
		IGNORED_PROPERTY_NAMES.add(IDIS.CALLING_AP_TITLE);
		
		// We'll need to override this one, default for the management client is true.
		OVERRIDES.put(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, Boolean.TRUE);
	}
	
	/**
	 * Indicates whether the given property should be synced to the DC.
	 * 
	 * @param 		propertyName		The name of the property.
	 * 
	 * @return		<code>true</code> if the prop should be passed, <code>false</code> if not.
	 */
	private static final boolean shouldSync(final String propertyName) {
		return !IGNORED_PROPERTY_NAMES.contains(propertyName);
	}
	
	/**
	 * Gets an optional override for the given property.
	 * 
	 * @param 	propertyName		The name of the property.
	 * 
	 * @return	THe overridden value, <code>null</code> if there is none.
	 */
	private static final Object getOverride(final String propertyName) {
		return OVERRIDES.get(propertyName);
	}
	
    private String className;

    /**
     * The protocol general properties and the dialect properties of the master device.
     * The security and connection properties are not included, they are stored in other sync objects.
     */
    private TypedProperties properties;

    public Beacon3100ProtocolConfiguration(String className, TypedProperties properties) {
        this.className = className;
        this.properties = properties;
    }

    //JSon constructor
    private Beacon3100ProtocolConfiguration() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromString(getClassName()));

        final Array protocolTypedProperties = new Array();
        for (String name : getProperties().propertyNames()) {
        	final Object overriddenValue = getOverride(name);
        	
            final Object value = overriddenValue != null ? overriddenValue : this.getProperties().getProperty(name);

            //Only add if the type of the value is supported
            if (name != null && 
            	shouldSync(name) &&
            	value != null && 
            	Beacon3100ProtocolTypedProperty.isSupportedType(value)) {
                protocolTypedProperties.addDataType(new Beacon3100ProtocolTypedProperty(name, value).toStructure());
            }
        }
        
        structure.addDataType(protocolTypedProperties);

        return structure;
    }

    @XmlAttribute
    public String getClassName() {
        return className;
    }

    @XmlAttribute
    public TypedProperties getProperties() {
        return properties;
    }
}