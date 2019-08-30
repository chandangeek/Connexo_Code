/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.users.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by bbl on 9/03/2016.
 */
@ProviderType
public interface HttpAuthenticationService {

    String USERPRINCIPAL = "com.elster.jupiter.userprincipal";

    boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void logout(HttpServletRequest request, HttpServletResponse response);

    String createToken(User user, String ipAddress);

    String getSsoX509Certificate();

    Cookie createTokenCookie(String cookieValue, String cookiePath);

}
