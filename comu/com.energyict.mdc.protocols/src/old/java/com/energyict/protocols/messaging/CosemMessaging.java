/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;

import java.util.List;

/**
 * This interface is intended to be implemented by protocols that support sending messages to execute custom cosem methods.
 *
 * @author Isabelle
 */
public interface CosemMessaging extends AdvancedMessaging {

    /**
     * Return all Cosem Classes for which messages can be sent
     *
     * @return the Cosem classes
     */
    List<CosemClass> getCosemClasses();

}
