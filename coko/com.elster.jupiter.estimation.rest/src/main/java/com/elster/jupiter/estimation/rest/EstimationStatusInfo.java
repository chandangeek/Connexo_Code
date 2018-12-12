/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Estimation configuration status
 */
@XmlRootElement
public class EstimationStatusInfo {

    public boolean active;

    public EstimationStatusInfo(){}

    public EstimationStatusInfo(boolean active){
        this.active = active;
    }
}
