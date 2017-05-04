package com.energyict.protocols.messaging;

import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Cosem class object.
 * Each Cosem object has a class id and an {@link ObisCode}.
 * A Cosem object is a collection of attributes and methods.
 * The information of the Cosem object is organized in attributes.  They represent the characteristics
 * of an object by means of attribute values.
 * An object offers a number of methods to modify the values of the attributes, examples are the connect, disconnect methods.
 * For these methods, hardcoded messages are available, {@link ConnectMessaging} and {@link DisconnectMessaging}.
 *
 * @author Isabelle
 */
public class CosemClass {

    private int classId;
    private String description;
    private ObisCode obisCode;

    private List<CosemAttribute> attributes = new ArrayList<CosemAttribute>();
    private List<CosemMethod> methods = new ArrayList<CosemMethod>();

    /**
     * Create a Cosem class.
     *
     * @param classId     the classId for this Cosem Object
     * @param description the description for this Cosem Object
     */
    public CosemClass(int classId, String description) {
        this.classId = classId;
        this.description = description;
    }

    /**
     * Create a Cosem class.
     *
     * @param classId     the classId for this Cosem Object
     * @param description the description for this Cosem Object
     * @param code        the {@link ObisCode} for this Cosem Object
     */
    public CosemClass(int classId, String description, ObisCode code) {
        this(classId, description);
        this.obisCode = code;
    }

    /**
     * Get the class id
     *
     * @return the classId for this Cosem Object
     */
    public int getClassId() {
        return classId;
    }

    /**
     * Set the class id
     *
     * @param classId the classId for this Cosem Object
     */
    public void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * Get the description
     *
     * @return the description for this Cosem Object
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     *
     * @param description the description for this Cosem Object
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the number of attributes for this Cosem object
     *
     * @return the description for this Cosem Object
     */
    public int getAttributeCount() {
        return attributes.size();
    }

    /**
     * Add an attribute to this Cosem object
     *
     * @param cosemAttribute the cosemAttribute to be added
     */
    public void addAttribute(CosemAttribute cosemAttribute) {
        attributes.add(cosemAttribute);
    }

    /**
     * Remove an attribute to this Cosem object
     *
     * @param cosemAttribute the cosemAttribute to be removed
     */
    public void removeAttribute(int index) {
        attributes.remove(index);
    }

    /**
     * Returns the attribute for the given index
     *
     * @param index the index
     * @return the attribute for the given index
     */
    public CosemAttribute getAttribute(int index) {
        return (CosemAttribute) attributes.get(index);
    }

    /**
     * Returns the number of methods for this Cosem object
     *
     * @return the  number of methods for this Cosem Object
     */
    public int getMethodCount() {
        return methods.size();
    }

    /**
     * Add a method to this Cosem object
     *
     * @param cosemMethod the cosemMethod to be added
     */
    public void addMethod(CosemMethod cosemMethod) {
        methods.add(cosemMethod);
    }

    /**
     * Removed a method to this Cosem object
     *
     * @param cosemMethod the cosemMethod to be removed
     */
    public void removeMethod(int index) {
        methods.remove(index);
    }

    /**
     * Returns the method for the given index
     *
     * @param index the index
     * @return the method for the given index
     */
    public CosemMethod getMethod(int index) {
        return (CosemMethod) methods.get(index);
    }

    /**
     * Get the obisCode
     *
     * @return the obisCode for this Cosem Object
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * Set the obisCode
     *
     * @param obisCode the obisCode for this Cosem Object
     */
    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }


}
