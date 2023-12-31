/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecurityPropertySpecProvider;
import com.energyict.mdc.common.protocol.security.SecuritySuite;

import aQute.bnd.annotation.ConsumerType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Models named set of security properties whose values
 * are managed against a Device.
 * The exact set of {@link PropertySpec}s
 * that are used is determined by the {@link AuthenticationDeviceAccessLevel} and/or
 * {@link EncryptionDeviceAccessLevel} and/or {@link SecuritySuite},{@link RequestSecurityLevel}
 * and/or {@link ResponseSecurityLevel} select in the SecurityPropertySet.
 * That in turn depends on the actual {@link DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-14 (10:29)
 */
@ConsumerType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface SecurityPropertySet extends HasName, HasId, SecurityPropertySpecProvider {

    void setName(String name);

    @XmlAttribute
    AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel();

    @XmlAttribute
    EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel();

    Object getClient();

    /**
     * Returns an optional {@link com.energyict.mdc.upl.properties.PropertySpec} containing the
     * {@link com.energyict.mdc.upl.properties.PropertySpec} to use for the 'client'. This property
     * spec can then be used to validate the value of the client field.
     * <p>
     * If 'client' should not be used(~ communication with the device doesn't
     * need a client), then an empty Optional will be returned.
     *
     * @return The 'client' PropertySpec or an empty Optional in case the 'client' is not supported
     */
    Optional<PropertySpec> getClientSecurityPropertySpec();

    SecuritySuite getSecuritySuite();

    RequestSecurityLevel getRequestSecurityLevel();

    ResponseSecurityLevel getResponseSecurityLevel();

    List<ConfigurationSecurityProperty> getConfigurationSecurityProperties();

    void addConfigurationSecurityProperty(String name, SecurityAccessorType keyAccessor);

    void updateConfigurationSecurityProperty(String configurationSecurityPropertyName, SecurityAccessorType keyAccessor);

    void removeConfigurationSecurityProperty(String configurationSecurityPropertyName);

    /**
     * Gets the Set of {@link PropertySpec}s that are the result of the selected security levels.
     *
     * @return The Set of PropertySpecs
     */
    Set<PropertySpec> getPropertySpecs();


    DeviceConfiguration getDeviceConfiguration();

    void setAuthenticationLevelId(int authenticationLevelId);

    void setEncryptionLevelId(int encryptionLevelId);

    void setClient(Object client);

    void setSecuritySuiteId(int securitySuiteId);

    void setRequestSecurityLevelId(int requestSecurityLevelId);

    void setResponseSecurityLevelId(int responseSecurityLevelId);

    void update();

    @XmlAttribute
    long getVersion();

    @XmlElement(name = "type")
    default String getXmlType() {
        return getClass().getName();
    }

    default void setXmlType(String ignore) {
    }
}