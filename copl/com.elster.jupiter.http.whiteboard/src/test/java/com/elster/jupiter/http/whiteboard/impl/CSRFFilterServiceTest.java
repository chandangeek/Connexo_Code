package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.CSRFFilterService;
import com.elster.jupiter.users.CSRFService;
import com.elster.jupiter.users.impl.CSRFServiceImpl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CSRFFilterServiceTest {
    private CSRFFilterService csrfFilterService;
    private CSRFService csrfService;

    @Before
    public void init() throws InvalidKeySpecException, NoSuchAlgorithmException {
        csrfService = new CSRFServiceImpl();
        csrfFilterService = new CSRFFilterServiceImpl(csrfService);
    }

    @Test
    public void csrfTest() throws IOException {
        csrfFilterService.createCSRFToken("ConnexoSessionXYZ");
        String csrfToken = csrfFilterService.getCSRFToken("ConnexoSessionXYZ");
        assertNotNull(csrfToken);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getRequestURI()).thenReturn("https://localhost/an/example/of/your/url");
        when(httpServletRequest.getHeader("X-CSRF-TOKEN")).thenReturn(csrfToken);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        Cookie cookie = new Cookie("X-SESSIONID", "ConnexoSessionXYZ");
        Cookie cookies[] =new Cookie[]{cookie};
        when(httpServletRequest.getCookies()).thenReturn(cookies);
        assertTrue(csrfFilterService.handleCSRFSecurity(httpServletRequest, httpServletResponse));
        csrfFilterService.removeUserSession("ConnexoSessionXYZ");
        assertNull(csrfFilterService.getCSRFToken("ConnexoSessionXYZ"));
    }
}

