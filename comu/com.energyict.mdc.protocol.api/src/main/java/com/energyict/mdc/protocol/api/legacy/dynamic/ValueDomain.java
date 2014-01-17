package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.TypeId;

import java.io.Serializable;

/**
 * A ValueDomain describes the possible content of an attribute.
 *
 * @author Steven Willems.
 * @since Apr 21, 2009.
 */
public class ValueDomain implements Serializable {

    private Class valueType;

    private boolean reference;

    private TypeId[] referenceableTypes = new TypeId[0];

    private String lookupName;

    private transient BusinessObjectFactory factory;

    private int factoryId;

    private int attributeId;

    /**
     * Create a ValueDomain with just a value type. (i.e. no referenced objects, no lookup value).
     *
     * @param valueType E.g. 'java.lang.String.class' or 'com.energyict.ean.Ean18.class'.
     */
    public ValueDomain(Class valueType) {
        this.valueType = valueType;
        this.reference = false;
        this.referenceableTypes = new TypeId[0];
        this.lookupName = null;
    }

    /**
     * Create a ValueDomain with a value type and a lookup name.
     *
     * @param valueType  E.g. 'com.energyict.mdw.core.Lookup.class'.
     * @param lookupName Name of the lookup field.
     */
    public ValueDomain(Class valueType, String lookupName) {
        this.valueType = valueType;
        this.reference = false;
        this.referenceableTypes = new TypeId[0];
        this.lookupName = lookupName;
    }

    /**
     * Create a ValueDomain with referenced objects.
     *
     * @param valueType          E.g. 'com.energyict.mdw.core.FolderType.class'.
     *                           Shouldn't this be '...Folder.class' in stead of a FolderType?
     * @param referenceableTypes List of referenced object types.
     */
    public ValueDomain(Class valueType, TypeId[] referenceableTypes) {
        this.valueType = valueType;
        this.reference = true;
        this.referenceableTypes = referenceableTypes;
        this.lookupName = null;
    }

    /**
     * Alternative constructor providing the factory id and attribute id instead of the class
     * this allows working in a lazy way and only building the full domain properties when asked for it
     *
     * @param factId      ID of the business object factory
     * @param attributeId ID of the attribute, used to lazy fetch allowed subtypes if needed
     */
    public ValueDomain(int factId, int attributeId) {
        this.factoryId = factId;
        this.attributeId = attributeId;
        this.reference = true;
        this.lookupName = null;
    }

    /**
     * Alternative constructor providing the factory
     *
     * @param factory      the business object factory
     */
    public ValueDomain(BusinessObjectFactory factory) {
        this.factoryId = factory.getId();
        this.factory = factory;
        this.attributeId = 0;
        this.reference = true;
        this.lookupName = null;
    }

    /**
     * Get the type of the value in the attribute.
     * E.g. java.lang.String.class or com.energyict.ean.Ean18.class.
     *
     * @return The type of the attribute.
     */
    public Class getValueType() {
        if (valueType == null) {
            valueType = ((IdBusinessObjectFactory) getFactory()).getInstanceType();
        }
        return valueType;
    }

    /**
     * Flag that indicates if the attribute references an other Business Object.
     *
     * @return True if the attribute is a reference, false otherwise.
     */
    public boolean isReference() {
        return reference;
    }

    /**
     * Get an array of types that may be referenced in the attribute.
     *
     * @return An array of possible referenced types.
     */
    public TypeId[] getReferenceableTypes() {
        return referenceableTypes;
    }

    /**
     * Get the name of the lookup field in case the attribute contains a lookup value.
     *
     * @return The name of the lookup field.
     */
    public String getLookupName() {
        return lookupName;
    }

    /**
     * Get the main type id. This is the hard type id of the possible soft types.
     *
     * @return The main TypeId
     */
    public TypeId getAnyTypeId() {
        return Environment.DEFAULT.get().findFactory(getValueType().getName()).getTargetTypeId();
    }

    public BusinessObjectFactory getFactory() {
        if (!reference) {
            return null;
        }
        if (factory == null) {
            if (factoryId > 0) {
                factory = Environment.DEFAULT.get().findFactory(factoryId);
            } else {
                factory = getAnyTypeId().getFactory();
            }
        }
        return factory;
    }

    public int getFactoryId() {
        if (!reference) {
            return 0;
        }
        if (factoryId == 0) {
            factoryId = getFactory().getId();
        }
        return factoryId;
    }

    public boolean isValidReference(IdBusinessObject object) {
        if (object == null) {
            return true;
        }
        if (getReferenceableTypes()==null || getReferenceableTypes().length == 0) {
            return true;
        }

        return true;
    }

}