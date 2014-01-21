package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.sql.SQLException;
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
     *
     *
     * @param name
     * @param className The name of java class that implements the DeviceProtocolPluggableClass
     * @return The newly created DeviceProtocolPluggableClass
     */
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className) throws BusinessException;

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} with the specified name
     * and implemented by the specified java class name.
     *
     *
     *
     * @param name
     * @param className The name of java class that implements the DeviceProtocolPluggableClass
     * @param typedProperties The TypedProperties
     * @return The newly created DeviceProtocolPluggableClass
     */
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties typedProperties) throws BusinessException;

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
     * @throws BusinessException Thrown when a business constraints was violated
     * @see PluggableClass#setProperty(PropertySpec, Object)
     * @see PluggableClass#save()
     */
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass (String name, String javaClassName) throws BusinessException;

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
     * @throws BusinessException Thrown when a business constraints was violated
     * @see PluggableClass#setProperty(PropertySpec, Object)
     * @see PluggableClass#save()
     * @see com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol#getPropertySpecs()
     */
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass (String name, String javaClassName, TypedProperties properties) throws BusinessException, SQLException;

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
     * @throws BusinessException Thrown when a business constraints was violated
     * @see PluggableClass#setProperty(PropertySpec, Object)
     * @see PluggableClass#save()
     */
    public ConnectionTypePluggableClass newConnectionTypePluggableClass (String name, String javaClassName) throws BusinessException;

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
     * @throws BusinessException Thrown when a business constraints was violated
     * @see PluggableClass#setProperty(PropertySpec, Object)
     * @see PluggableClass#save()
     * @see com.energyict.mdc.protocol.api.ConnectionType#getPropertySpecs()
     */
    public ConnectionTypePluggableClass newConnectionTypePluggableClass (String name, String javaClassName, TypedProperties properties) throws BusinessException, SQLException;

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