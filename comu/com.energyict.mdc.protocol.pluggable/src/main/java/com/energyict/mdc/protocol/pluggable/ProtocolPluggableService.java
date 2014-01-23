package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.List;

/**
 * Provides services that relate to {@link com.energyict.mdc.pluggable.PluggableClass}es
 * that relate to device protocols.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (13:46)
 */
public interface ProtocolPluggableService {

    public static String COMPONENTNAME = "PPC";

    /**
     * Loads the pluggable protocol class with the specified javaClassName
     * from whichever OSGi bundle actually contains the class.
     * This is different from the same method provided by
     * {@link com.energyict.mdc.protocol.api.services.DeviceProtocolService}
     * that attempts the same from the bundle that contains that specific service
     * but may fail if the class is not actually contained in that bundle
     *
     * @param javaClassName the javaClassName to use to model the new class
     * @return the newly created DeviceProtocol
     */
    public Class loadProtocolClass(String javaClassName);

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
     * Finds all {@link DeviceProtocolPluggableClass}es that are defined
     * and active in the system.
     *
     * @return The List of all DeviceProtocolPluggableClasses
     */
    public List<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses();

    public DeviceProtocolPluggableClass findDeviceProtocolPluggableClass(long id);

    public List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(String className);

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

    public InboundDeviceProtocolPluggableClass findInboundDeviceProtocolPluggableClass(long id);

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
     * @see PluggableClass#setProperty(PropertySpec, Object)
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
     * @see PluggableClass#setProperty(PropertySpec, Object)
     * @see PluggableClass#save()
     * @see com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol#getPropertySpecs()
     */
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass (String name, String javaClassName, TypedProperties properties);

    public void deleteInboundDeviceProtocolPluggableClass(long id);

    public List<ConnectionTypePluggableClass> findConnectionTypePluggableClassByClassName(String javaClassName);

    public ConnectionTypePluggableClass findConnectionTypePluggableClass(long id);

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
     * @see PluggableClass#setProperty(PropertySpec, Object)
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
     * @see PluggableClass#setProperty(PropertySpec, Object)
     * @see PluggableClass#save()
     * @see com.energyict.mdc.protocol.api.ConnectionType#getPropertySpecs()
     */
    public ConnectionTypePluggableClass newConnectionTypePluggableClass (String name, String javaClassName, TypedProperties properties);

    public String createOriginalAndConformRelationNameBasedOnJavaClassname (Class clazz);

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

}