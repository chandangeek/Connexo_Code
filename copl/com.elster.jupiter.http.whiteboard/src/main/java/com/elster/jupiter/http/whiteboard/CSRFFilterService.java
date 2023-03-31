/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */


package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 2/05/2020 (11:09)
 */
@ProviderType
public interface CSRFFilterService {
    public void createCSRFToken(String sessionId);
    public void removeUserSession(String sessionId);
    public String getCSRFToken(String sessionId);

    public boolean handleCSRFSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
