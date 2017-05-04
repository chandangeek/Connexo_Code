/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Cosem method which can be executed on a given Cosem object.
 * A Cosem method can be used to examine or modify the values of the attributes of a Cosem object.
 *
 * @author Isabelle
 */
public class CosemMethod {

    private int methodId;
    private String description;

    /**
     * Return the parameters needed for the Cosem method
     *
     * @return the parameters
     */
    private List<CosemParameter> parameters = new ArrayList<CosemParameter>();


    /**
     * Create a Cosem method
     *
     * @param methodId    The id that needs to be sent to execute this method
     * @param description The description of this method
     */
    public CosemMethod(int methodId, String description) {
        this.methodId = methodId;
        this.description = description;
    }

    /**
     * Return the methodId needed that needs to be sent to execute this method
     *
     * @return the methodId
     */
    public int getMethodId() {
        return methodId;
    }

    /**
     * Set the methodId
     *
     * @param methodId The methodId needed that needs to be sent to execute this method
     */
    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    /**
     * Return the description of this method
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     *
     * @param description The description for this method
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Add a parameters for the CosemMethod
     *
     * @param parameter The parameter to be added
     */
    public void addParameter(CosemParameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Remove a parameters for the CosemMethod
     *
     * @param parameter The parameter to be removed
     */
    public void removeParameter(int index) {
        parameters.remove(index);
    }

    /**
     * Get all parameters needed to execute this CosemMethod
     *
     * @return all parameters
     */
    public List<CosemParameter> getParameters() {
        return parameters;
    }

    /**
     * Get the number of parameters needed to execute this CosemMethod
     *
     * @return the number of parameters
     */
    public int getParameterCount() {
        return parameters.size();
    }

    /**
     * Get the parameter for the given index
     *
     * @param index The index
     */
    public CosemParameter getParameter(int index) {
        return parameters.get(index);
    }

}
