package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link com.energyict.mdc.pluggable.PluggableClass}es
 * that relate to device protocols.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (13:46)
 */
@ProviderType
public interface ProtocolPluggableService {

    String COMPONENTNAME = "PPC";

    void addLicensedProtocolService(LicensedProtocolService licensedProtocolService);
    void addDeviceProtocolService(DeviceProtocolService deviceProtocolService);
    void addInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService);
    void addConnectionTypeService(ConnectionTypeService connectionTypeService);

    ProtocolDeploymentListenerRegistration register(ProtocolDeploymentListener listener);

    /**
     * Creates an instance of the pluggable protocol class with the specified name
     * from whichever OSGi bundle actually contains the class.
     * This is different from the same method provided by
     * {@link com.energyict.mdc.protocol.api.services.DeviceProtocolService}
     * that attempts the same from the bundle that contains that specific service
     * but may fail if the class is not actually contained in that bundle.
     *
     * @param className the fully qualified Class name
     * @return the newly created DeviceProtocol
     */
    Object createProtocol(String className);

    /**
     * Create a DeviceProtocol messages related object
     * for the given javaClassName.
     * This is different from the same method provided by
     * {@link com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService}
     * that attempts the same from the bundle that contains that specific service
     * but may fail if the class is not actually contained in that bundle
     *
     * @param javaClassName the javaClassName to use as model for the DeviceProtocol messages related object
     * @return the created message related object
     */
    Object createDeviceProtocolMessagesFor(String javaClassName);

    /**
     * Creates a DeviceProtocol security related object
     * for the given javaClassName.
     * This is different from the same method provided by
     * {@link com.energyict.mdc.protocol.api.services.DeviceProtocolService}
     * that attempts the same from the bundle that contains that specific service
     * but may fail if the class is not actually contained in that bundle
     *
     * @param javaClassName the javaClassName to use as model for the DeviceProtocol security related object
     * @return the created security related object
     */
    Object createDeviceProtocolSecurityFor(String javaClassName);

    /**
     * Finds all device protocols that are covered by the license.
     *
     * @return a list of all available licensed protocols.
     */
    List<LicensedProtocol> getAllLicensedProtocols();

    /**
     * Tests if the specified java class name is covered by the license.
     *
     * @param javaClassName The name of the java class
     * @return A flag that indicates if the java class is covered by the license
     */
    boolean isLicensedProtocolClassName(String javaClassName);

    /**
     * Finds all {@link DeviceProtocolPluggableClass}es that are defined
     * and active in the system.
     *
     * @return The List of all DeviceProtocolPluggableClasses
     */
    Finder<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses();

    LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long id);

    Optional<DeviceProtocolPluggableClass> findAndLockDeviceProtocolPluggableClassByIdAndVersion(long id, long version);

    Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassByName(String name);

    List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassesByClassName(String className);

    /**
     * Deletes the {@link DeviceProtocolPluggableClass} that is uniquely
     * identified by the specified id.
     *
     * @param id The id of the obsolete DeviceProtocolPluggableClass
     */
    void deleteDeviceProtocolPluggableClass(long id);

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} with the specified name
     * and implemented by the specified java class name.
     *
     * @param name The name for the PluggableClass
     * @param className The name of java class that implements the DeviceProtocolPluggableClass
     * @return The newly created DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className);

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} with the specified name
     * and implemented by the specified java class name.
     *
     * @param name The name for the PluggableClass
     * @param className The name of java class that implements the DeviceProtocolPluggableClass
     * @param typedProperties The TypedProperties
     * @return The newly created DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties typedProperties);

    List<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClassByClassName(String javaClassName);

    Optional<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClass(long id);

    Optional<InboundDeviceProtocolPluggableClass> findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(long id, long version);

    List<InboundDeviceProtocolPluggableClass> findAllInboundDeviceProtocolPluggableClass();

    /**
     * Returns a new {@link InboundDeviceProtocolPluggableClass} who's properties can be completed
     * by calling the setProperty method.
     * The following business constraints are checked:
     * <ul>
     * <li>There is only one InboundDeviceProtocolPluggableClass with the same name</li>
     * <li>The java class exists and can be loaded from the classpath</li>
     * <li>The java class effectively implements the {@link com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol} interface</li>
     * </ul>
     *
     * @param name The name of the PluggableClass
     * @param javaClassName The name of the java implementation class
     * @return The PluggableClass that is not yet saved
     * @see PluggableClass#setProperty(com.elster.jupiter.properties.PropertySpec, Object)
     * @see PluggableClass#save()
     */
    InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName);

    /**
     * Returns a new {@link InboundDeviceProtocolPluggableClass} that is already saved to the database.
     * The following business constraints are checked:
     * <ul>
     * <li>There is only one InboundDeviceProtocolPluggableClass with the same name</li>
     * <li>The java class exists and can be loaded from the classpath</li>
     * <li>The java class effectively implements the {@link com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol} interface</li>
     * <li>All of the java class' required property specs have a value</li>
     * <li>All of the specified properties are compatible with the java class' property specs</li>
     * </ul>
     *
     * @param name The name of the PluggableClass
     * @param javaClassName The name of the java implementation class
     * @param properties The TypedProperties
     * @return The PluggableClass that is not yet saved
     * @see PluggableClass#setProperty(com.elster.jupiter.properties.PropertySpec, Object)
     * @see PluggableClass#save()
     * @see com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol#getPropertySpecs()
     */
    InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName, TypedProperties properties);

    void deleteInboundDeviceProtocolPluggableClass(long id);

    List<ConnectionTypePluggableClass> findConnectionTypePluggableClassByClassName(String javaClassName);
    Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClassByName(String name);

    Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClass(long id);

    List<ConnectionTypePluggableClass> findAllConnectionTypePluggableClasses();

    /**
     * Returns a new {@link ConnectionTypePluggableClass} who's properties can be completed
     * by calling the setProperty method.
     * The following business constraints are checked:
     * <ul>
     * <li>There is only one ConnectionTypePluggableClass with the same name</li>
     * <li>The java class exists and can be loaded from the classpath</li>
     * <li>The java class effectively implements the {@link com.energyict.mdc.protocol.api.ConnectionType} interface</li>
     * </ul>
     *
     * @param name The name of the PluggableClass
     * @param javaClassName The name of the java implementation class
     * @return The PluggableClass that is not yet saved
     * @see PluggableClass#setProperty(com.elster.jupiter.properties.PropertySpec, Object)
     * @see PluggableClass#save()
     */
    ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName);

    /**
     * Returns a new {@link ConnectionTypePluggableClass} that is already saved to the database.
     * The following business constraints are checked:
     * <ul>
     * <li>There is only one ConnectionTypePluggableClass with the same name</li>
     * <li>The java class exists and can be loaded from the classpath</li>
     * <li>The java class effectively implements the {@link com.energyict.mdc.protocol.api.ConnectionType} interface</li>
     * <li>All of the java class' required property specs have a value</li>
     * <li>All of the specified properties are compatible with the java class' property specs</li>
     * </ul>
     *
     * @param name The name of the PluggableClass
     * @param javaClassName The name of the java implementation class
     * @param properties The TypedProperties
     * @return The PluggableClass that is not yet saved
     * @see PluggableClass#setProperty(com.elster.jupiter.properties.PropertySpec, Object)
     * @see PluggableClass#save()
     * @see com.energyict.mdc.protocol.api.ConnectionType#getPropertySpecs()
     */
    ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName, TypedProperties properties);

    /**
     * Returns a {@link DeviceProtocolDialectUsagePluggableClass} for the
     * dialect of the {@link DeviceProtocolPluggableClass} with the specified name.
     *
     * @param pluggableClass The DeviceProtocolPluggableClass
     * @param dialectName The name of the dialect
     * @return The DeviceProtocolDialectUsagePluggableClass
     */
    DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass(DeviceProtocolPluggableClass pluggableClass, String dialectName);

    /**
     * Load the jsoned Cache object for the given string.
     * The unmarshalling should happen in the bundle which 'knows' the cache objects.
     * Note that if the bundle that created the cache in the first place
     * is currently missing, then Optional.empty() is returned.
     *
     * @param jsonCache the json representation of the cache
     * @return the unmarshalled object
     */
    Optional<Object> unMarshallDeviceProtocolCache(String jsonCache);

    String marshallDeviceProtocolCache(Object legacyCache);

    ConnectionType createConnectionType(String javaClassName);

    InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass);

    /**
     * Adapts a {@link com.energyict.mdc.upl.properties.PropertySpec UPL property spec}
     * to the Connexo interface.
     *
     * @param uplPropertySpec The UPL PropertySpec
     * @return The Connexo PropertySpec
     */
    PropertySpec adapt(com.energyict.mdc.upl.properties.PropertySpec uplPropertySpec);

    /**
     * Adapts a Connexo {@link PropertySpec} to the corresponding
     * {@link com.energyict.mdc.upl.properties.PropertySpec UPL interface}.
     *
     * @param propertySpec The Connexo PropertySpec
     * @return The UPL PropertySpec
     */
    com.energyict.mdc.upl.properties.PropertySpec adapt(PropertySpec propertySpec);

    /**
     * Adapts a {@link com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel UPL authentication device access level}
     * to the Connexo interface.
     *
     * @param uplLevel The UPL AuthenticationDeviceAccessLevel
     * @return The Connexo {@link AuthenticationDeviceAccessLevel}
     */
    AuthenticationDeviceAccessLevel adapt(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel uplLevel);

    /**
     * Adapts a {@link com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel UPL encryption device access level}
     * to the Connexo interface.
     *
     * @param uplLevel The UPL EncryptionDeviceAccessLevel
     * @return The Connexo {@link EncryptionDeviceAccessLevel}
     */
    EncryptionDeviceAccessLevel adapt(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel uplLevel);

    /**
     * Adapts a {@link com.energyict.mdc.upl.messages.DeviceMessageCategory Connexo device message category}
     * to the UPL interface.
     * @param connexoCategory The Connexo device message category
     * @return The UPL device message category
     */
    com.energyict.mdc.upl.messages.DeviceMessageCategory adapt(DeviceMessageCategory connexoCategory);

    /**
     * Adapts a {@link com.energyict.mdc.upl.messages.DeviceMessageSpec Connexo device message specification}
     * to the UPL interface.
     * @param connexoSpec The Connexo device message specification
     * @return The UPL device message specification
     */
    com.energyict.mdc.upl.messages.DeviceMessageSpec adapt(DeviceMessageSpec connexoSpec);

    OfflineDevice adapt(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice);

}