/*
 *
 *  * Copyright (c) 2020  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users;

import aQute.bnd.annotation.ProviderType;

import javax.inject.Inject;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 1/30/2020 (15:44)
 */
@ProviderType
public interface CSRFService {
    public String getCSRFToken(String sessionId);
    public void addCSRFToken(String sessionId, String token);
    public void romoveToken(String sessionId);


}
