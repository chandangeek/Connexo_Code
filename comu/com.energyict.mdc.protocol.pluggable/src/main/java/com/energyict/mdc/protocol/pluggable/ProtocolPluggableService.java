package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.common.TypedProperties;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link com.energyict.mdc.pluggable.PluggableClass}es
 * that relate to device protocols.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (13:46)
 */
public interface ProtocolPluggableService {

    public static String COMPONENTNAME = "PPC";

    public ProtocolDeploymentListenerRegistration register(ProtocolDeploymentListener listener);

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
    public Object createProtocol(String className);

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
    public Object createDeviceProtocolMessagesFor(String javaClassName);

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
    public Object createDeviceProtocolSecurityFor(String javaClassName);

    /**
     * Finds all device protocols that are covered by the license.
     *
     * @return a list of all available licensed protocols.
     */
    public List<LicensedProtocol> getAllLicensedProtocols();

    /**
     * Tests if the specified java class name is covered by the license.
     *
     * @param javaClassName The name of the java class
     * @return A flag that indicates if the java class is covered by the license
     */
    public boolean isLicensedProtocolClassName (String javaClassName);

    /**
     * Finds all {@link DeviceProtocolPluggableClass}es that are defined
     * and active in the system.
     *
     * @return The List of all DeviceProtocolPluggableClasses
     */
    public Finder<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses();

    public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long id);

    public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassByName(String name);

    public List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassesByClassName(String className);

    /**
     * Deletes the {@link DeviceProtocolPluggableClass} that is uniquely
     * identified by the specified id.
     *
     * @param id The id of the obsolete DeviceProtocolPluggableClass
     */
    public void deleteDeviceProtocolPluggableClass (long id);

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} with the specified name
     * and implemented by the specified java class name.
     *
     * @param name The name for the PluggableClass
     * @param className The name of java class that implements the DeviceProtocolPluggableClass
     * @return The newly created DeviceProtocolPluggableClass
     */
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className);

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} with the specified name
     * and implemented by the specified java class name.
     *
     * @param name The name for the PluggableClass
     * @param className The name of java class that implements the DeviceProtocolPluggableClass
     * @param typedProperties The TypedProperties
     * @return The newly created DeviceProtocolPluggableClass
     */
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties typedProperties);

    public List<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClassByClassName(String javaClassName);

    public Optional<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClass(long id);

    public List<InboundDeviceProtocolPluggableClass> findAllInboundDeviceProtocolPluggableClass();

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
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass (String name, String javaClassName);

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
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass (String name, String javaClassName, TypedProperties properties);

    public void deleteInboundDeviceProtocolPluggableClass(long id);

    public List<ConnectionTypePluggableClass> findConnectionTypePluggableClassByClassName(String javaClassName);
    public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClassByName(String name);

    public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClass(long id);

    public List<ConnectionTypePluggableClass> findAllConnectionTypePluggableClasses();

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
    public ConnectionTypePluggableClass newConnectionTypePluggableClass (String name, String javaClassName);

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
    public ConnectionTypePluggableClass newConnectionTypePluggableClass (String name, String javaClassName, TypedProperties properties);

    public String createOriginalAndConformRelationNameBasedOnJavaClassname (Class<?> clazz);

    public String createConformRelationTypeName (String name);

    public String createConformRelationAttributeName (String name);

    /**
     * Returns a {@link DeviceProtocolDialectUsagePluggableClass} for the
     * dialect of the {@link DeviceProtocolPluggableClass} with the specified name.
     *
     * @param pluggableClass The DeviceProtocolPluggableClass
     * @param dialectName The name of the dialect
     * @return The DeviceProtocolDialectUsagePluggableClass
     */
    public DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass (DeviceProtocolPluggableClass pluggableClass, String dialectName);

    public boolean isDefaultAttribute(RelationAttributeType attributeType);

    /**
     * Finds the {@link RelationType} that holds the security properties
     * for the specified {@link DeviceProtocolPluggableClass}.
     *
     * @param deviceProtocolPluggableClass The DeviceProtocolPluggableClass
     * @return The RelationType
     */
    public RelationType findSecurityPropertyRelationType(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    /**
     * Load the jsoned Cache object for the given string.
     * The unmarshalling should happen in the bundle which 'knows' the cache objects.
     * Note that if the bundle that created the cache in the first place
     * is currently missing, then Optional.empty() is returned.
     *
     * @param jsonCache the json representation of the cache
     * @return the unmarshalled object
     */
    public Optional<Object> unMarshallDeviceProtocolCache(String jsonCache);

    public String marshallDeviceProtocolCache(Object legacyCache);

    public ConnectionType createConnectionType(String javaClassName);

    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass);

    /**
     * Tests if the SecurityPropertySet has any {@link com.energyict.mdc.dynamic.relation.Relation}s
     * that hold security property values for Devices that use the {@link DeviceProtocol}
     * to communicate.
     *
     * @param securityPropertySet The SecurityPropertySet
     * @param deviceProtocol The DeviceProtocol
     * @return A flag that indicates if the SecurityPropertySet is in use on Devices
     */
    public boolean hasSecurityRelations(RelationParticipant securityPropertySet, DeviceProtocol deviceProtocol);

}