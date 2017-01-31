/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DemandResetProtocol.java
 *
 * Created on 22 september 2005, 14:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.mdc.protocol.api;

import java.io.IOException;

/**
 * DemandResetProtocol interface, to perform a manual billing reset on a device
 * using protocoltester. This interface is not supported by EiServer and
 * CommServer. Only for testing purposes!!!
 *
 * @author Koen
 */
public interface DemandResetProtocol {

    /**
     * Execute a billing reset on the device. After receiving the Demand Reset
     * command the meter executes a demand reset by doing a snap shot of all
     * energy and demand registers.
     *
     * @throws IOException
     */
    void resetDemand() throws IOException;

}