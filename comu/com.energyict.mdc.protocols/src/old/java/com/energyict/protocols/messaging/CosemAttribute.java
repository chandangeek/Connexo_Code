package com.energyict.protocols.messaging;


/**
 * This class represents a Cosem attribute.
 * A Cosem object can have a number of attributes.
 * The information of the Cosem object is organized in attributes.  They represent the characteristics
 * of an object by means of attribute values.
 *
 * @author Isabelle
 */
public class CosemAttribute {

    private int attributeId;
    private String description;
    private CosemDataType type;

    /**
     * Create a Cosem class.
     *
     * @param attributeId the attributeId for this Cosem attribute
     * @param description the description for this Cosem attribute
     * @param type        the type for this Cosem attribute
     */
    public CosemAttribute(int attributeId, String description, CosemDataType type) {
        this.attributeId = attributeId;
        this.description = description;
        this.type = type;
    }

    /**
     * Get the Attribute Id
     *
     * @return the attributeId for this Cosem attribute
     */
    public int getAttributeId() {
        return attributeId;
    }

    /**
     * Set the attributeId
     *
     * @param attributeId the attributeId for this Cosem attribute
     */
    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * Get the description
     *
     * @return the description for this Cosem attribute
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     *
     * @param description the description for this Cosem attribute
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the type
     *
     * @return the type for this Cosem attribute
     */
    public CosemDataType getType() {
        return type;
    }

    /**
     * Set the type
     *
     * @param type {@link CosemDataType} for this Cosem attribute
     */
    public void setType(CosemDataType type) {
        this.type = type;
    }

}
