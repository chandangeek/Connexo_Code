/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

/**
 * This class represents a parameter for a Cosem method.
 *
 * @author Isabelle
 */
public class CosemParameter {

    private CosemDataType type;
    private String description;

    /**
     * Create a Cosem parameter
     */
    public CosemParameter() {
    }

    /**
     * Create a Cosem parameter
     *
     * @param description The description of this Cosem parameter
     * @param type        The type of this Cosem parameter
     */
    public CosemParameter(String description, CosemDataType type) {
        this.description = description;
        this.type = type;
    }

    /**
     * Returns the type of this Cosem parameter
     *
     * @return the type of this Cosem parameter
     */
    public CosemDataType getType() {
        return type;
    }

    /**
     * Set the type of this Cosem parameter
     *
     * @param type the type of this Cosem parameter
     */
    public void setType(CosemDataType type) {
        this.type = type;
    }

    /**
     * Returns the description of this Cosem parameter
     *
     * @return the description of this Cosem parameter
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of this Cosem parameter
     *
     * @param description the description of this Cosem parameter
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
