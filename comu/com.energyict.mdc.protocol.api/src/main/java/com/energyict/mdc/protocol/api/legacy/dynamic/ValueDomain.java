package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.BusinessObjectFactory;
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
    private transient BusinessObjectFactory factory;

    /**
     * Create a ValueDomain with just a value type. (i.e. no referenced objects, no lookup value).
     *
     * @param valueType E.g. 'java.lang.String.class' or 'com.energyict.ean.Ean18.class'.
     */
    public ValueDomain(Class valueType) {
        this.valueType = valueType;
        this.reference = false;
        this.referenceableTypes = new TypeId[0];
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
    }

    /**
     * Alternative constructor providing the factory
     *
     * @param factory      the business object factory
     */
    public ValueDomain(BusinessObjectFactory factory) {
        this.factory = factory;
        this.reference = true;
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

    public BusinessObjectFactory getFactory() {
        if (this.reference) {
            return factory;
        }
        else {
            return null;
        }
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